import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import axios from '../api/axios';
import ProductCard from '../ui/ProductCard';
import { useAuth } from '../context/AuthContext';

const Home = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams, setSearchParams] = useSearchParams();

  const { isAuthenticated } = useAuth();

  const searchQuery = searchParams.get('search') || '';

  useEffect(() => {
    fetchProducts();
  }, [searchQuery]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      let url = '/api/products';
      let params = {};

      if (searchQuery) {
        url = '/api/products/search';
        params.q = searchQuery;
      }

      const response = await axios.get(url, { params });
      setProducts(response.data.content || response.data);
    } catch (err) {
      setError('Failed to load products');
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };



  const handleAddToCart = async (product) => {
    if (!isAuthenticated()) {
      // Redirect to login or show message
      alert('Please login to add items to cart');
      return;
    }

    try {
      await axios.post('/api/cart/add', {
        productId: product.id,
        qty: 1
      });
      alert('Product added to cart!');
    } catch (err) {
      alert('Failed to add product to cart');
      console.error('Error adding to cart:', err);
    }
  };



  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-96">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600 dark:text-red-400 text-lg">{error}</p>
        <button
          onClick={fetchProducts}
          className="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg transition-colors"
        >
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 rounded-2xl p-8 text-white text-center">
        <h1 className="text-4xl font-bold mb-4">Welcome to QuickBee</h1>
        <p className="text-xl opacity-90 mb-6">Fast delivery, fresh products, delivered in minutes!</p>
        <div className="flex justify-center space-x-4 text-sm">
          <div className="flex items-center space-x-2">
            <div className="w-2 h-2 bg-white rounded-full"></div>
            <span>Lightning Fast</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-2 h-2 bg-white rounded-full"></div>
            <span>Fresh Quality</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-2 h-2 bg-white rounded-full"></div>
            <span>Wide Variety</span>
          </div>
        </div>
      </div>



      {/* Products Grid */}
      {products.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-neutral-600 dark:text-neutral-400 text-lg">
            {searchQuery ? 'No products found matching your search.' : 'No products available.'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {products.map(product => (
            <ProductCard
              key={product.id}
              product={product}
              onAddToCart={handleAddToCart}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default Home;
