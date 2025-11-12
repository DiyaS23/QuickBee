package com.quickbee.backend.service;

import com.quickbee.backend.dto.CreatePaymentRequest;
import com.quickbee.backend.dto.CreatePaymentResponse;
import com.quickbee.backend.dto.VerifyPaymentRequest;
import com.quickbee.backend.model.Order;
import com.quickbee.backend.model.Payment;
import com.quickbee.backend.model.User;
import com.quickbee.backend.model.enums.OrderStatus;
import com.quickbee.backend.model.enums.PaymentStatus;
import com.quickbee.backend.repository.OrderRepository;
import com.quickbee.backend.repository.PaymentRepository;
import com.quickbee.backend.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;

    private RazorpayClient razorpayClient;
    private String keyId;
    private String keySecret;

    public PaymentService(@Value("${razorpay.key.id}") String keyId,
                          @Value("${razorpay.key.secret}") String keySecret) throws RazorpayException {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    private User getAuthenticatedUser() {
        String userEmail = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        return userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public CreatePaymentResponse createRazorpayOrder(CreatePaymentRequest request) throws RazorpayException {
        User user = getAuthenticatedUser();
        Order order = orderRepository.findById(request.getOurOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + request.getOurOrderId()));

        if (!order.getUserId().equals(user.getId())) {
            throw new SecurityException("Access Denied: You do not own this order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("This order is not pending payment. Its current status is: " + order.getStatus());
        }

        Payment payment = new Payment(order.getId(), user.getId(), order.getTotal());

        int amountInPaise = (int) (order.getTotal() * 100);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", order.getId());

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        String razorpayOrderId = razorpayOrder.get("id");
        payment.setRazorpayOrderId(razorpayOrderId);
        paymentRepository.save(payment);

        return new CreatePaymentResponse(razorpayOrderId, amountInPaise, this.keyId, "INR");
    }

    @Transactional
    public Order verifyPayment(VerifyPaymentRequest request) throws RazorpayException {

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpay_order_id())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpay_order_id());
            options.put("razorpay_payment_id", request.getRazorpay_payment_id());
            options.put("razorpay_signature", request.getRazorpay_signature());

            boolean isSignatureValid = Utils.verifyPaymentSignature(options, this.keySecret);

            if (isSignatureValid) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setRazorpayPaymentId(request.getRazorpay_payment_id());
                payment.setRazorpaySignature(request.getRazorpay_signature());
                paymentRepository.save(payment);

                order.setStatus(OrderStatus.CONFIRMED);
                return orderRepository.save(order);

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new RuntimeException("Payment verification failed: Invalid signature");
            }
        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed: " + e.getMessage());
        }
    }
}