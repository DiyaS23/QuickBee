import { useState, useEffect } from 'react';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  ClockIcon,
  CheckCircleIcon,
  TruckIcon,
  MapPinIcon,
  PhoneIcon
} from '@heroicons/react/24/outline';

const MyOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated()) {
      fetchOrders();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/orders/me');
      setOrders(response.data.content || response.data);
    } catch (err) {
      console.error('Error fetching orders:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'CONFIRMED': return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'ASSIGNED': return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
      case 'ACCEPTED': return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-200';
      case 'OUT_FOR_DELIVERY': return 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200';
      case 'DELIVERED': return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'CANCELLED': return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      default: return 'bg-neutral-100 text-neutral-800 dark:bg-neutral-900 dark:text-neutral-200';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING': return <ClockIcon className="h-4 w-4" />;
      case 'CONFIRMED': return <CheckCircleIcon className="h-4 w-4" />;
      case 'ASSIGNED': return <CheckCircleIcon className="h-4 w-4" />;
      case 'ACCEPTED': return <CheckCircleIcon className="h-4 w-4" />;
      case 'OUT_FOR_DELIVERY': return <TruckIcon className="h-4 w-4" />;
      case 'DELIVERED': return <CheckCircleIcon className="h-4 w-4" />;
      default: return <ClockIcon className="h-4 w-4" />;
    }
  };

  const getOrderProgress = (status) => {
    const steps = ['PENDING', 'CONFIRMED', 'ASSIGNED', 'ACCEPTED', 'OUT_FOR_DELIVERY', 'DELIVERED'];
    const currentIndex = steps.indexOf(status);
    return Math.max(0, (currentIndex / (steps.length - 1)) * 100);
  };

  const getStatusSteps = () => {
    return [
      { key: 'PENDING', label: 'Order Placed', icon: ClockIcon },
      { key: 'CONFIRMED', label: 'Confirmed', icon: CheckCircleIcon },
      { key: 'ASSIGNED', label: 'Partner Assigned', icon: CheckCircleIcon },
      { key: 'ACCEPTED', label: 'Accepted', icon: CheckCircleIcon },
      { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: TruckIcon },
      { key: 'DELIVERED', label: 'Delivered', icon: CheckCircleIcon }
    ];
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!isAuthenticated()) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
          Please Login
        </h2>
        <p className="text-neutral-600 dark:text-neutral-400 mb-6">
          You need to be logged in to view your orders.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      <h1 className="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
        My Orders
      </h1>

      {orders.length === 0 ? (
        <div className="text-center py-12">
          <ClockIcon className="mx-auto h-24 w-24 text-neutral-400 mb-4" />
          <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
            No orders yet
          </h2>
          <p className="text-neutral-600 dark:text-neutral-400 mb-6">
            Your order history will appear here once you place your first order.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="bg-white dark:bg-neutral-800 rounded-xl shadow-sm overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => setSelectedOrder(selectedOrder?.orderId === order.orderId ? null : order)}
            >
              <div className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                    Order #{order.orderId}
                  </h3>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium flex items-center space-x-1 ${getStatusColor(order.status)}`}>
                    {getStatusIcon(order.status)}
                    <span>{order.status.replace('_', ' ')}</span>
                  </span>
                </div>

                <div className="space-y-2 mb-4">
                  <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                    {order.items?.length || 0} items • ₹{order.total}
                  </p>
                  <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                    {new Date(order.createdAt).toLocaleDateString()} at {new Date(order.createdAt).toLocaleTimeString()}
                  </p>
                </div>

                {/* Progress Bar */}
                <div className="mb-4">
                  <div className="w-full bg-neutral-200 dark:bg-neutral-700 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full transition-all duration-500"
                      style={{ width: `${getOrderProgress(order.status)}%` }}
                    ></div>
                  </div>
                  <div className="flex justify-between mt-2 text-xs text-neutral-600 dark:text-neutral-400">
                    {getStatusSteps().map((step, index) => (
                      <span
                        key={step.key}
                        className={`flex items-center space-x-1 ${
                          getStatusSteps().findIndex(s => s.key === order.status) >= index
                            ? 'text-blue-600 dark:text-blue-400'
                            : ''
                        }`}
                      >
                        <step.icon className="h-3 w-3" />
                        <span className="hidden sm:inline">{step.label}</span>
                      </span>
                    ))}
                  </div>
                </div>

                {/* Order Items Preview */}
                {selectedOrder?.orderId === order.orderId && (
                  <div className="border-t border-neutral-200 dark:border-neutral-700 pt-4 mt-4">
                    <h4 className="font-medium text-neutral-900 dark:text-neutral-100 mb-3">
                      Order Items
                    </h4>
                    <div className="space-y-2">
                      {order.items?.map((item, index) => (
                        <div key={index} className="flex items-center justify-between text-sm">
                          <span className="text-neutral-700 dark:text-neutral-300">
                            {item.productName} × {item.quantity}
                          </span>
                          <span className="text-neutral-600 dark:text-neutral-400">
                            ₹{item.subtotal}
                          </span>
                        </div>
                      ))}
                    </div>

                    {/* Delivery Address */}
                    {order.address && (
                      <div className="mt-4 pt-4 border-t border-neutral-200 dark:border-neutral-700">
                        <div className="flex items-start space-x-2">
                          <MapPinIcon className="h-4 w-4 text-neutral-500 mt-0.5" />
                          <div className="text-sm text-neutral-600 dark:text-neutral-400">
                            <p className="font-medium text-neutral-900 dark:text-neutral-100">
                              {order.address.name}
                            </p>
                            <p>{order.address.line1}</p>
                            {order.address.line2 && <p>{order.address.line2}</p>}
                            <p>{order.address.city}, {order.address.state} - {order.address.pincode}</p>
                            <p className="flex items-center space-x-1 mt-1">
                              <PhoneIcon className="h-3 w-3" />
                              <span>{order.address.phone}</span>
                            </p>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyOrders;
