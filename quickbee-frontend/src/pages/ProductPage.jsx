import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import {
  StarIcon,
  ShoppingCartIcon,
  HeartIcon,
  TruckIcon,
  ShieldCheckIcon
} from '@heroicons/react/24/outline';
import { StarIcon as StarIconSolid } from '@heroicons/react/24/solid';

const ProductPage = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [addingToCart, setAddingToCart] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    fetchProduct();
  }, [id]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`/api/products/${id}`);
      setProduct(response.data);
    } catch (err) {
      console.error('Error fetching product:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!isAuthenticated()) {
      alert('Please login to add items to cart');
      return;
    }

    try {
      setAddingToCart(true);
      await axios.post('/api/cart/add', {
        productId: product.id,
        qty: quantity
      });
      alert('Product added to cart!');
    } catch (err) {
      alert('Failed to add product to cart');
      console.error('Error adding to cart:', err);
    } finally {
      setAddingToCart(false);
    }
  };

  const updateQuantity = (newQuantity) => {
    if (newQuantity >= 1 && newQuantity <= (product?.stockQuantity || 10)) {
      setQuantity(newQuantity);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="text-center py-12">
        <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
          Product not found
        </h2>
        <p className="text-neutral-600 dark:text-neutral-400">
          The product you're looking for doesn't exist.
        </p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
        {/* Product Image */}
        <div className="space-y-4">
          <div className="aspect-square overflow-hidden rounded-xl bg-neutral-100 dark:bg-neutral-800">
            <img
              src={product.imageUrl || '/placeholder-product.jpg'}
              alt={product.name}
              className="w-full h-full object-cover"
              onError={(e) => {
                e.target.src = '/placeholder-product.jpg';
              }}
            />
          </div>
        </div>

        {/* Product Details */}
        <div className="space-y-6">
          <div>
            <h1 className="text-3xl font-bold text-neutral-900 dark:text-neutral-100 mb-2">
              {product.name}
            </h1>
            <div className="flex items-center space-x-4 mb-4">
              <div className="flex items-center space-x-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <StarIconSolid
                    key={star}
                    className={`h-5 w-5 ${
                      star <= (product.rating || 4)
                        ? 'text-yellow-400 fill-current'
                        : 'text-neutral-300'
                    }`}
                  />
                ))}
                <span className="text-neutral-600 dark:text-neutral-400 ml-2">
                  {product.rating || 4.5} ({Math.floor(Math.random() * 100) + 10} reviews)
                </span>
              </div>
              <span className="text-sm text-neutral-500 dark:text-neutral-500 uppercase tracking-wide">
                {product.category}
              </span>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <span className="text-4xl font-bold text-blue-600 dark:text-blue-400">
              ₹{product.price}
            </span>
            {product.originalPrice && product.originalPrice > product.price && (
              <span className="text-xl text-neutral-500 line-through">
                ₹{product.originalPrice}
              </span>
            )}
          </div>

          <div className="flex items-center space-x-2 text-sm text-neutral-600 dark:text-neutral-400">
            <ShieldCheckIcon className="h-4 w-4" />
            <span>Fresh & Quality Assured</span>
          </div>

          <div className="flex items-center space-x-2 text-sm text-neutral-600 dark:text-neutral-400">
            <TruckIcon className="h-4 w-4" />
            <span>Free delivery on orders above ₹100</span>
          </div>

          {/* Quantity Selector */}
          <div className="space-y-3">
            <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Quantity
            </label>
            <div className="flex items-center space-x-4">
              <div className="flex items-center border border-neutral-300 dark:border-neutral-600 rounded-lg">
                <button
                  onClick={() => updateQuantity(quantity - 1)}
                  className="p-2 hover:bg-neutral-100 dark:hover:bg-neutral-700 rounded-l-lg transition-colors"
                  disabled={quantity <= 1}
                >
                  -
                </button>
                <span className="px-4 py-2 text-center min-w-12">
                  {quantity}
                </span>
                <button
                  onClick={() => updateQuantity(quantity + 1)}
                  className="p-2 hover:bg-neutral-100 dark:hover:bg-neutral-700 rounded-r-lg transition-colors"
                  disabled={quantity >= (product.stockQuantity || 10)}
                >
                  +
                </button>
              </div>
              <span className="text-sm text-neutral-600 dark:text-neutral-400">
                {product.stockQuantity || 10} available
              </span>
            </div>
          </div>

          {/* Add to Cart Button */}
          <button
            onClick={handleAddToCart}
            disabled={addingToCart}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-neutral-400 text-white py-4 px-6 rounded-lg font-medium transition-colors flex items-center justify-center space-x-2"
          >
            {addingToCart ? (
              <>
                <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                <span>Adding to Cart...</span>
              </>
            ) : (
              <>
                <ShoppingCartIcon className="h-5 w-5" />
                <span>Add to Cart - ₹{product.price * quantity}</span>
              </>
            )}
          </button>

          {/* Product Description */}
          <div className="border-t border-neutral-200 dark:border-neutral-700 pt-6">
            <h3 className="text-lg font-semibold text-neutral-900 dark:text-neutral-100 mb-3">
              Description
            </h3>
            <p className="text-neutral-600 dark:text-neutral-400 leading-relaxed">
              {product.description}
            </p>
          </div>

          {/* Additional Info */}
          <div className="grid grid-cols-2 gap-4 pt-6 border-t border-neutral-200 dark:border-neutral-700">
            <div className="text-center">
              <TruckIcon className="h-8 w-8 text-blue-600 mx-auto mb-2" />
              <p className="text-sm font-medium text-neutral-900 dark:text-neutral-100">
                Fast Delivery
              </p>
              <p className="text-xs text-neutral-600 dark:text-neutral-400">
                15-30 minutes
              </p>
            </div>
            <div className="text-center">
              <ShieldCheckIcon className="h-8 w-8 text-green-600 mx-auto mb-2" />
              <p className="text-sm font-medium text-neutral-900 dark:text-neutral-100">
                Quality Assured
              </p>
              <p className="text-xs text-neutral-600 dark:text-neutral-400">
                Fresh products
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductPage;
