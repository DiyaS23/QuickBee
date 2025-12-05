import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  ShoppingBagIcon,
  PlusIcon,
  MinusIcon,
  TrashIcon,
  TruckIcon
} from '@heroicons/react/24/outline';

const Cart = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated()) {
      fetchCart();
    } else {
      setLoading(false);
    }
  }, [isAuthenticated]);

  const fetchCart = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/cart');
      setCart(response.data);
    } catch (err) {
      console.error('Error fetching cart:', err);
    } finally {
      setLoading(false);
    }
  };

  const updateItemQuantity = async (productId, quantity) => {
    if (quantity < 1) return;

    try {
      setUpdating(true);
      const response = await axios.put(`/api/cart/item/${productId}`, { quantity });
      setCart(response.data);
    } catch (err) {
      console.error('Error updating item:', err);
      alert('Failed to update item quantity');
    } finally {
      setUpdating(false);
    }
  };

  const removeItem = async (productId) => {
    try {
      setUpdating(true);
      const response = await axios.delete(`/api/cart/item/${productId}`);
      setCart(response.data);
    } catch (err) {
      console.error('Error removing item:', err);
      alert('Failed to remove item');
    } finally {
      setUpdating(false);
    }
  };

  const clearCart = async () => {
    if (!confirm('Are you sure you want to clear your cart?')) return;

    try {
      setUpdating(true);
      const response = await axios.delete('/api/cart');
      setCart(response.data);
    } catch (err) {
      console.error('Error clearing cart:', err);
      alert('Failed to clear cart');
    } finally {
      setUpdating(false);
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
        <ShoppingBagIcon className="mx-auto h-24 w-24 text-neutral-400 mb-4" />
        <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
          Please Login
        </h2>
        <p className="text-neutral-600 dark:text-neutral-400 mb-6">
          You need to be logged in to view your cart.
        </p>
        <Link
          to="/login"
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg transition-colors inline-block"
        >
          Login to Continue
        </Link>
      </div>
    );
  }

  if (!cart || !cart.items || cart.items.length === 0) {
    return (
      <div className="text-center py-12">
        <ShoppingBagIcon className="mx-auto h-24 w-24 text-neutral-400 mb-4" />
        <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
          Your cart is empty
        </h2>
        <p className="text-neutral-600 dark:text-neutral-400 mb-6">
          Add some delicious items to get started!
        </p>
        <Link
          to="/"
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg transition-colors inline-block"
        >
          Start Shopping
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
          Your Cart
        </h1>
        <button
          onClick={clearCart}
          disabled={updating}
          className="text-red-600 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300 text-sm font-medium disabled:opacity-50"
        >
          Clear Cart
        </button>
      </div>

      {/* Cart Items */}
      <div className="bg-white dark:bg-neutral-800 rounded-xl shadow-sm overflow-hidden">
        <div className="divide-y divide-neutral-200 dark:divide-neutral-700">
          {cart.items.map((item) => (
            <div key={item.productId} className="p-6">
              <div className="flex items-center space-x-4">
                <img
                  src={item.product?.imageUrl || '/placeholder-product.jpg'}
                  alt={item.product?.name}
                  className="w-20 h-20 object-cover rounded-lg"
                  onError={(e) => {
                    e.target.src = '/placeholder-product.jpg';
                  }}
                />

                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                    {item.product?.name}
                  </h3>
                  <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                    ₹{item.product?.price} each
                  </p>
                </div>

                <div className="flex items-center space-x-3">
                  <button
                    onClick={() => updateItemQuantity(item.productId, item.quantity - 1)}
                    disabled={updating || item.quantity <= 1}
                    className="p-1 rounded-md hover:bg-neutral-100 dark:hover:bg-neutral-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <MinusIcon className="h-4 w-4" />
                  </button>

                  <span className="w-8 text-center font-medium">
                    {item.quantity}
                  </span>

                  <button
                    onClick={() => updateItemQuantity(item.productId, item.quantity + 1)}
                    disabled={updating}
                    className="p-1 rounded-md hover:bg-neutral-100 dark:hover:bg-neutral-700 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <PlusIcon className="h-4 w-4" />
                  </button>
                </div>

                <div className="text-right">
                  <p className="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
                    ₹{item.subtotal}
                  </p>
                </div>

                <button
                  onClick={() => removeItem(item.productId)}
                  disabled={updating}
                  className="p-2 text-red-600 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300 disabled:opacity-50"
                >
                  <TrashIcon className="h-5 w-5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Cart Summary */}
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

        <Link
          to="/checkout"
          className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-6 rounded-lg font-medium transition-colors text-center block"
        >
          Proceed to Checkout
        </Link>
      </div>
    </div>
  );
};

export default Cart;
