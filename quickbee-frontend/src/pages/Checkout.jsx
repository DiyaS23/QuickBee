import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  MapPinIcon,
  CreditCardIcon,
  TruckIcon,
  CheckCircleIcon,
  ClockIcon
} from '@heroicons/react/24/outline';

const Checkout = () => {
  const [cart, setCart] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [loading, setLoading] = useState(true);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [showAddressForm, setShowAddressForm] = useState(false);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated()) {
      fetchCart();
      fetchAddresses();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated]);

  const fetchCart = async () => {
    try {
      const response = await axios.get('/api/cart');
      setCart(response.data);
    } catch (err) {
      console.error('Error fetching cart:', err);
    }
  };

  const fetchAddresses = async () => {
    try {
      const response = await axios.get('/api/user/addresses');
      setAddresses(response.data);
      if (response.data.length > 0 && !selectedAddress) {
        setSelectedAddress(response.data[0]);
      }
      setLoading(false);
    } catch (err) {
      console.error('Error fetching addresses:', err);
      setLoading(false);
    }
  };

  const handlePlaceOrder = async () => {
    if (!selectedAddress) {
      alert('Please select a delivery address');
      return;
    }

    if (!cart || cart.items.length === 0) {
      alert('Your cart is empty');
      return;
    }

    setPlacingOrder(true);

    try {
      // Create order
      const orderData = {
        addressId: selectedAddress.id,
        items: cart.items.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }))
      };

      const orderResponse = await axios.post('/api/orders', orderData);
      const order = orderResponse.data;

      // Create payment
      const paymentData = {
        orderId: order.orderId,
        amount: order.total
      };

      const paymentResponse = await axios.post('/api/payments/create-order', paymentData);
      const paymentOrder = paymentResponse.data;

      // Initialize Razorpay
      const options = {
        key: 'YOUR_RAZORPAY_KEY_ID', // Replace with your actual key
        amount: paymentOrder.amount,
        currency: 'INR',
        name: 'QuickBee',
        description: 'Order Payment',
        order_id: paymentOrder.razorpayOrderId,
        handler: async function (response) {
          try {
            // Verify payment
            const verifyData = {
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature
            };

            await axios.post('/api/payments/verify', verifyData);

            // Payment successful
            alert('Payment successful! Your order has been placed.');
            navigate('/orders');
          } catch (error) {
            console.error('Payment verification failed:', error);
            alert('Payment verification failed. Please contact support.');
          }
        },
        prefill: {
          name: selectedAddress.name,
          email: 'customer@example.com', // You might want to get this from user profile
          contact: selectedAddress.phone
        },
        theme: {
          color: '#3B82F6'
        }
      };

      const rzp = new window.Razorpay(options);
      rzp.open();

    } catch (error) {
      console.error('Error placing order:', error);
      alert('Failed to place order. Please try again.');
    } finally {
      setPlacingOrder(false);
    }
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
          You need to be logged in to checkout.
        </p>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
          Your cart is empty
        </h2>
        <p className="text-neutral-600 dark:text-neutral-400 mb-6">
          Add some items to your cart before checkout.
        </p>
        <button
          onClick={() => navigate('/')}
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg transition-colors"
        >
          Continue Shopping
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <h1 className="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
        Checkout
      </h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Left Column - Order Details */}
        <div className="space-y-6">
          {/* Delivery Address */}
          <div className="bg-white dark:bg-neutral-800 rounded-xl shadow-sm p-6">
            <div className="flex items-center space-x-2 mb-4">
              <MapPinIcon className="h-5 w-5 text-blue-600" />
              <h2 className="text-xl font-semibold text-neutral-900 dark:text-neutral-100">
                Delivery Address
              </h2>
            </div>

            {addresses.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-neutral-600 dark:text-neutral-400 mb-4">
                  No addresses found. Please add a delivery address.
                </p>
                <button
                  onClick={() => navigate('/addresses')}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
                >
                  Add Address
                </button>
              </div>
            ) : (
              <div className="space-y-3">
                {addresses.map((address) => (
                  <div
                    key={address.id}
                    onClick={() => setSelectedAddress(address)}
                    className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                      selectedAddress?.id === address.id
                        ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                        : 'border-neutral-200 dark:border-neutral-700 hover:border-neutral-300 dark:hover:border-neutral-600'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <p className="font-medium text-neutral-900 dark:text-neutral-100">
                          {address.name}
                        </p>
                        <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                          {address.line1}
                          {address.line2 && `, ${address.line2}`}
                        </p>
                        <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                          {address.city}, {address.state} - {address.pincode}
                        </p>
                        <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                          Phone: {address.phone}
                        </p>
                      </div>
                      {selectedAddress?.id === address.id && (
                        <CheckCircleIcon className="h-5 w-5 text-blue-600" />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Order Items */}
          <div className="bg-white dark:bg-neutral-800 rounded-xl shadow-sm p-6">
            <h2 className="text-xl font-semibold text-neutral-900 dark:text-neutral-100 mb-4">
              Order Items
            </h2>
            <div className="space-y-4">
              {cart.items.map((item) => (
                <div key={item.productId} className="flex items-center space-x-4">
                  <img
                    src={item.product?.imageUrl || '/placeholder-product.jpg'}
                    alt={item.product?.name}
                    className="w-16 h-16 object-cover rounded-lg"
                    onError={(e) => {
                      e.target.src = '/placeholder-product.jpg';
                    }}
                  />
                  <div className="flex-1">
                    <h3 className="font-medium text-neutral-900 dark:text-neutral-100">
                      {item.product?.name}
                    </h3>
                    <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                      ₹{item.product?.price} × {item.quantity}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-neutral-900 dark:text-neutral-100">
                      ₹{item.subtotal}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Column - Order Summary & Payment */}
        <div className="space-y-6">
          {/* Order Summary */}
          <div className="bg-white dark:bg-neutral-800 rounded-xl shadow-sm p-6">
            <h2 className="text-xl font-semibold text-neutral-900 dark:text-neutral-100 mb-4">
              Order Summary
            </h2>

            <div className="space-y-3 mb-6">
              <div className="flex justify-between text-neutral-600 dark:text-neutral-400">
                <span>Subtotal ({cart.totalItems} items)</span>
                <span>₹{cart.subtotal}</span>
              </div>
              <div className="flex justify-between text-neutral-600 dark:text-neutral-400">
                <span>Delivery Fee</span>
                <span>₹40</span>
              </div>
              <div className="border-t border-neutral-200 dark:border-neutral-700 pt-3">
                <div className="flex justify-between text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                  <span>Total</span>
                  <span>₹{cart.subtotal + 40}</span>
                </div>
              </div>
            </div>

            <div className="flex items-center space-x-2 text-sm text-neutral-600 dark:text-neutral-400 mb-6">
              <TruckIcon className="h-4 w-4" />
              <span>Estimated delivery: 15-30 minutes</span>
            </div>

            <div className="flex items-center space-x-2 text-sm text-neutral-600 dark:text-neutral-400 mb-6">
              <ClockIcon className="h-4 w-4" />
              <span>Payment method: Online Payment (Razorpay)</span>
            </div>

            <button
              onClick={handlePlaceOrder}
              disabled={placingOrder || !selectedAddress}
              className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-neutral-400 disabled:cursor-not-allowed text-white py-3 px-6 rounded-lg font-medium transition-colors flex items-center justify-center space-x-2"
            >
              {placingOrder ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  <span>Processing...</span>
                </>
              ) : (
                <>
                  <CreditCardIcon className="h-5 w-5" />
                  <span>Place Order & Pay</span>
                </>
              )}
            </button>

            {!selectedAddress && (
              <p className="text-red-600 dark:text-red-400 text-sm mt-2 text-center">
                Please select a delivery address
              </p>
            )}
          </div>

          {/* Payment Info */}
          <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-4">
            <div className="flex items-start space-x-3">
              <CreditCardIcon className="h-5 w-5 text-blue-600 mt-0.5" />
              <div className="text-sm">
                <p className="font-medium text-blue-900 dark:text-blue-100 mb-1">
                  Secure Payment
                </p>
                <p className="text-blue-700 dark:text-blue-300">
                  Your payment information is encrypted and secure. We use Razorpay for safe transactions.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
