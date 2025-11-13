package com.quickbee.backend.service;

import com.quickbee.backend.model.DeliveryAssignment;
import com.quickbee.backend.model.Order;
import com.quickbee.backend.model.enums.AssignmentStatus;
import com.quickbee.backend.model.enums.OrderStatus;
import com.quickbee.backend.repository.DeliveryAssignmentRepository;
import com.quickbee.backend.repository.OrderRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class AssignmentService {

    private final DeliveryQueueService queueService;
    private final MongoTemplate mongoTemplate;
    private final OrderRepository orderRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final UserService userService; // optional for partner lookups / notifications

    // configurable timeouts (defaults for quick-commerce)
    private final long acceptanceTimeoutSeconds = 75;   // partner must accept within 45s
    private final long pickupTimeoutSeconds = 300;      // partner must pick up within 5min (300s)
    private final long deliveryWindowSeconds = 1200;    // deliver within 20min (1200s) - configurable

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public AssignmentService(DeliveryQueueService queueService,
                             MongoTemplate mongoTemplate,
                             OrderRepository orderRepository,
                             DeliveryAssignmentRepository assignmentRepository,
                             UserService userService) {
        this.queueService = queueService;
        this.mongoTemplate = mongoTemplate;
        this.orderRepository = orderRepository;
        this.assignmentRepository = assignmentRepository;
        this.userService = userService;
    }

    /**
     * Called when a partner becomes available. Attempts to assign one order (or up to capacity).
     * Returns true if assigned.
     */
    public boolean tryAssignToPartner(String partnerId) {
        String orderId = queueService.popOldestOrder();
        if (orderId == null) return false;

        // Atomically set assignedPartnerId and status = ASSIGNED only if current status is CONFIRMED and not assigned
        Query query = new Query(Criteria.where("_id").is(orderId)
                .and("status").is(OrderStatus.CONFIRMED)
                .and("assignedPartnerId").is(null));
        Update update = new Update()
                .set("assignedPartnerId", partnerId)
                .set("status", OrderStatus.ASSIGNED)
                .set("assignedAt", Instant.now())
                .set("updatedAt", Instant.now());

        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        Order updated = mongoTemplate.findAndModify(query, update, options, Order.class);

        if (updated == null) {
            // The order is no longer available — push back and return false
            queueService.enqueueOrderFront(orderId);
            return false;
        }

        // create assignment record
        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setOrderId(orderId);
        assignment.setPartnerId(partnerId);
        assignment.setStatus(AssignmentStatus.CREATED);
        assignment.setAssignedAt(Instant.now());
        assignment.setAttemptCount(0);
        assignmentRepository.save(assignment);
        // mark partner busy (important when capacity == 1)
        userService.markPartnerBusy(partnerId);

// remove any remaining occurrences of this order in the queue (safety)
        queueService.removeOrderFromQueue(orderId);

// Send push/notification to partner via userService (not implemented here)
        userService.notifyPartnerNewAssignment(partnerId, assignment);

// schedule acceptance timeout
        scheduler.schedule(() -> handleAcceptanceTimeout(assignment.getId()), acceptanceTimeoutSeconds, TimeUnit.SECONDS);
        return true;
    }

    private void handleAcceptanceTimeout(String assignmentId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return;
        DeliveryAssignment assignment = opt.get();
        if (assignment.getStatus() == AssignmentStatus.CREATED) {
            // mark timeout
            assignment.setStatus(AssignmentStatus.TIMEOUT);
            assignmentRepository.save(assignment);

            // free partner (set partner online true) - this may differ based on your partner model
            userService.freePartner(assignment.getPartnerId());

            // requeue order at front for immediate reassignment
            requeueOrderForRetry(assignment.getOrderId(), assignment.getAttemptCount() + 1);
        }
    }

    public boolean partnerAccepts(String assignmentId, String partnerId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return false;
        DeliveryAssignment assignment = opt.get();
        if (!assignment.getPartnerId().equals(partnerId)) return false;
        if (assignment.getStatus() != AssignmentStatus.CREATED) return false;

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment.setAcceptedAt(Instant.now());
        assignmentRepository.save(assignment);

        // Update order status to ACCEPTED
        Optional<Order> o = orderRepository.findById(assignment.getOrderId());
        if (o.isPresent()) {
            Order order = o.get();
            order.setStatus(OrderStatus.ACCEPTED);
            order.setAcceptedAt(Instant.now());
            orderRepository.save(order);
        }

        // schedule pickup timeout
        scheduler.schedule(() -> handlePickupTimeout(assignment.getId()), pickupTimeoutSeconds, TimeUnit.SECONDS);
        return true;
    }

    public boolean partnerRejects(String assignmentId, String partnerId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return false;
        DeliveryAssignment assignment = opt.get();
        if (!assignment.getPartnerId().equals(partnerId)) return false;
        if (assignment.getStatus() != AssignmentStatus.CREATED) return false;

        assignment.setStatus(AssignmentStatus.REJECTED);
        assignmentRepository.save(assignment);

        // free partner
        userService.freePartner(partnerId);

        // requeue order at front
        requeueOrderForRetry(assignment.getOrderId(), assignment.getAttemptCount() + 1);
        return true;
    }

    private void handlePickupTimeout(String assignmentId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return;
        DeliveryAssignment assignment = opt.get();
        if (assignment.getStatus() == AssignmentStatus.ACCEPTED) {
            assignment.setStatus(AssignmentStatus.PICKUP_TIMEOUT);
            assignmentRepository.save(assignment);

            // free partner
            userService.freePartner(assignment.getPartnerId());

            // requeue the order
            requeueOrderForRetry(assignment.getOrderId(), assignment.getAttemptCount() + 1);
        }
    }

    public boolean partnerPicked(String assignmentId, String partnerId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return false;
        DeliveryAssignment assignment = opt.get();
        if (!assignment.getPartnerId().equals(partnerId)) return false;
        if (assignment.getStatus() != AssignmentStatus.ACCEPTED) return false;

        // mark as picked (distinct from accepted)
        assignment.setStatus(AssignmentStatus.PICKED);
        assignment.setPickedAt(Instant.now());
        assignmentRepository.save(assignment);

        // Update order status to OUT_FOR_DELIVERY
        Optional<Order> o = orderRepository.findById(assignment.getOrderId());
        if (o.isPresent()) {
            Order order = o.get();
            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
            order.setPickedAt(Instant.now());
            orderRepository.save(order);
        }

        // schedule delivery window timeout
        scheduler.schedule(() -> handleDeliveryTimeout(assignment.getId()), deliveryWindowSeconds, TimeUnit.SECONDS);
        return true;
    }


    private void handleDeliveryTimeout(String assignmentId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return;
        DeliveryAssignment assignment = opt.get();
        if (assignment.getStatus() == AssignmentStatus.ACCEPTED || assignment.getStatus() == AssignmentStatus.CREATED|| assignment.getStatus() == AssignmentStatus.PICKED) {
            // Consider failed
            assignment.setStatus(AssignmentStatus.FAILED);
            assignmentRepository.save(assignment);

            // Update order -> keep as ASSIGNED or move to FAILED/CANCELLED depending on business rules
            Optional<Order> o = orderRepository.findById(assignment.getOrderId());
            if (o.isPresent()) {
                Order order = o.get();
                order.setStatus(OrderStatus.CANCELLED); // or another status you prefer
                orderRepository.save(order);
            }

            // free partner
            userService.freePartner(assignment.getPartnerId());
        }
    }

    public boolean partnerDelivered(String assignmentId, String partnerId) {
        Optional<DeliveryAssignment> opt = assignmentRepository.findById(assignmentId);
        if (!opt.isPresent()) return false;
        DeliveryAssignment assignment = opt.get();
        if (!assignment.getPartnerId().equals(partnerId)) return false;

        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletedAt(Instant.now());
        assignmentRepository.save(assignment);

        // Update order
        Optional<Order> o = orderRepository.findById(assignment.getOrderId());
        if (o.isPresent()) {
            Order order = o.get();
            order.setStatus(OrderStatus.DELIVERED);
            order.setDeliveredAt(Instant.now());
            orderRepository.save(order);
        }
// ensure removed from queue in case of duplicates (safety)
        queueService.removeOrderFromQueue(assignment.getOrderId());

// free partner
        userService.freePartner(partnerId);


        return true;
    }

    private void requeueOrderForRetry(String orderId, int attemptCount) {
        // increment attempt counter on latest assignment records is handled by the caller
        // push order to front so FIFO fairness gives it earliest chance
        queueService.enqueueOrderFront(orderId);

        // important: clear assignedPartnerId in order document so it can be assigned again
        Query q = new Query(Criteria.where("_id").is(orderId));
        Update u = new Update()
                .set("assignedPartnerId", null)
                .set("status", OrderStatus.CONFIRMED)
                .set("updatedAt", Instant.now());
        mongoTemplate.updateFirst(q, u, Order.class);
    }
}
