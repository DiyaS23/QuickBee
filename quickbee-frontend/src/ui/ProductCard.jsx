import { Link } from 'react-router-dom';
import { StarIcon } from '@heroicons/react/24/solid';
import { ShoppingCartIcon } from '@heroicons/react/24/outline';

const ProductCard = ({ product, onAddToCart }) => {
  const handleAddToCart = (e) => {
    e.preventDefault();
    e.stopPropagation();
    onAddToCart(product);
  };

  return (
    <div className="bg-white dark:bg-neutral-800 rounded-xl shadow-sm hover:shadow-lg transition-shadow duration-300 overflow-hidden group">
      <Link to={`/product/${product.id}`} className="block">
        <div className="aspect-square overflow-hidden">
          <img
            src={product.imageUrl || '/placeholder-product.jpg'}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            onError={(e) => {
              e.target.src = '/placeholder-product.jpg';
            }}
          />
        </div>
        <div className="p-4">
          <h3 className="font-semibold text-lg text-neutral-900 dark:text-neutral-100 mb-2 line-clamp-2">
            {product.name}
          </h3>
          <p className="text-neutral-600 dark:text-neutral-400 text-sm mb-3 line-clamp-2">
            {product.description}
          </p>
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-1">
              <StarIcon className="h-4 w-4 text-yellow-400 fill-current" />
              <span className="text-sm text-neutral-600 dark:text-neutral-400">
                {product.rating || 4.5}
              </span>
            </div>
            <span className="text-xs text-neutral-500 dark:text-neutral-500 uppercase tracking-wide">
              {product.category}
            </span>
          </div>
          <div className="flex items-center justify-between">
            <div className="flex flex-col">
              <span className="text-2xl font-bold text-blue-600 dark:text-blue-400">
                ₹{product.price}
              </span>
              {product.originalPrice && product.originalPrice > product.price && (
                <span className="text-sm text-neutral-500 line-through">
                  ₹{product.originalPrice}
                </span>
              )}
            </div>
            <button
              onClick={handleAddToCart}
              className="bg-blue-600 hover:bg-blue-700 text-white p-2 rounded-lg transition-colors duration-200 flex items-center space-x-1"
            >
              <ShoppingCartIcon className="h-4 w-4" />
              <span className="text-sm font-medium">Add</span>
            </button>
          </div>
        </div>
      </Link>
    </div>
  );
};

export default ProductCard;
