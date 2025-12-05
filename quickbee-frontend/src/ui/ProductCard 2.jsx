import React from 'react'
import { Link } from 'react-router-dom'

const ProductCard = ({ product }) => {
  return (
    <Link
      to={`/product/${product.id}`}
      className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden group"
    >
      <div className="aspect-square bg-neutral-100 flex items-center justify-center">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
          />
        ) : (
          <div className="text-6xl">🛒</div>
        )}
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-neutral-900 mb-2 line-clamp-2">
          {product.name}
        </h3>
        <p className="text-neutral-600 text-sm mb-2 line-clamp-2">
          {product.description || 'No description available'}
        </p>
        <div className="flex items-center justify-between">
          <span className="text-lg font-bold text-primary-600">
            ₹{product.price}
          </span>
          {product.stockQuantity && (
            <span className="text-sm text-neutral-500">
              {product.stockQuantity} left
            </span>
          )}
        </div>
      </div>
    </Link>
  )
}

export default ProductCard
