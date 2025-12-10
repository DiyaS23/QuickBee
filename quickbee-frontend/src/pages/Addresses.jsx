import { useState, useEffect } from 'react';
import axios from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { PlusIcon, PencilIcon, TrashIcon } from '@heroicons/react/24/outline';

const Addresses = () => {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    line1: '',
    line2: '',
    city: '',
    state: '',
    pincode: '',
  });

  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated()) {
      fetchAddresses();
    }
  }, [isAuthenticated]);

  const fetchAddresses = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/user/addresses');
      setAddresses(response.data);
    } catch (err) {
      setError('Failed to load addresses');
      console.error('Error fetching addresses:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingAddress) {
        await axios.put(`/api/user/addresses/${editingAddress.id}`, formData);
      } else {
        await axios.post('/api/user/addresses', formData);
      }
      fetchAddresses();
      resetForm();
    } catch (err) {
      console.error('Error saving address:', err);
      if (err.response) {
        console.error('Backend error:', err.response.data);
      }
      alert('Failed to save address');
    }
  };

  const handleEdit = (address) => {
    setEditingAddress(address);
    setFormData({
      name: address.name || '',
      phone: address.phone || '',
      line1: address.line1 || '',
      line2: address.line2 || '',
      city: address.city || '',
      state: address.state || '',
      pincode: address.pincode || '',
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this address?')) return;
    try {
      await axios.delete(`/api/user/addresses/${id}`);
      fetchAddresses();
    } catch (err) {
      alert('Failed to delete address');
      console.error('Error deleting address:', err);
    }
  };

  const resetForm = () => {
    setShowForm(false);
    setEditingAddress(null);
    setFormData({
      name: '',
      phone: '',
      line1: '',
      line2: '',
      city: '',
      state: '',
      pincode: '',
    });
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
          onClick={fetchAddresses}
          className="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg transition-colors"
        >
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-neutral-900 dark:text-neutral-100">
          My Addresses
        </h1>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
        >
          <PlusIcon className="h-5 w-5" />
          <span>Add Address</span>
        </button>
      </div>

      {/* Form */}
      {showForm && (
        <div className="bg-white dark:bg-neutral-800 rounded-xl p-6 shadow-sm">
          <h2 className="text-xl font-semibold mb-4">
            {editingAddress ? 'Edit Address' : 'Add New Address'}
          </h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Address Name (e.g. Home, Office)
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                required
                className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
              />
            </div>

            {/* Phone */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Phone Number
              </label>
              <input
                type="text"
                value={formData.phone}
                onChange={(e) =>
                  setFormData({ ...formData, phone: e.target.value })
                }
                required
                className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
              />
            </div>

            {/* Address lines */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Address Line 1
              </label>
              <input
                type="text"
                value={formData.line1}
                onChange={(e) =>
                  setFormData({ ...formData, line1: e.target.value })
                }
                required
                className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Address Line 2 (optional)
              </label>
              <input
                type="text"
                value={formData.line2}
                onChange={(e) =>
                  setFormData({ ...formData, line2: e.target.value })
                }
                className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
              />
            </div>

            {/* City / State */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                  City
                </label>
                <input
                  type="text"
                  value={formData.city}
                  onChange={(e) =>
                    setFormData({ ...formData, city: e.target.value })
                  }
                  required
                  className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                  State
                </label>
                <input
                  type="text"
                  value={formData.state}
                  onChange={(e) =>
                    setFormData({ ...formData, state: e.target.value })
                  }
                  required
                  className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
                />
              </div>
            </div>

            {/* Pincode */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1">
                Pincode
              </label>
              <input
                type="text"
                value={formData.pincode}
                onChange={(e) =>
                  setFormData({ ...formData, pincode: e.target.value })
                }
                required
                className="w-full p-2 border border-neutral-300 dark:border-neutral-600 rounded-lg bg-white dark:bg-neutral-700 text-neutral-900 dark:text-neutral-100"
              />
            </div>

            {/* Actions */}
            <div className="flex space-x-4">
              <button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg transition-colors"
              >
                {editingAddress ? 'Update' : 'Add'} Address
              </button>
              <button
                type="button"
                onClick={resetForm}
                className="bg-neutral-600 hover:bg-neutral-700 text-white px-6 py-2 rounded-lg transition-colors"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Address list */}
      {addresses.length === 0 ? (
        <div className="text-center py-12">
          <h2 className="text-2xl font-bold text-neutral-900 dark:text-neutral-100 mb-4">
            No Addresses Found
          </h2>
          <p className="text-neutral-600 dark:text-neutral-400 mb-6">
            Add your first address to get started!
          </p>
          <button
            onClick={() => setShowForm(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg transition-colors"
          >
            Add Address
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {addresses.map((address) => (
            <div
              key={address.id}
              className="bg-white dark:bg-neutral-800 rounded-xl p-6 shadow-sm"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  {address.name && (
                    <p className="text-sm font-semibold text-blue-600 dark:text-blue-400 mb-1">
                      {address.name}
                    </p>
                  )}
                  <p className="text-neutral-900 dark:text-neutral-100 font-medium">
                    {address.line1}
                    {address.line2 && <span>, {address.line2}</span>}
                  </p>
                  <p className="text-neutral-600 dark:text-neutral-400 text-sm">
                    {address.city}, {address.state} {address.pincode}
                  </p>
                  {address.phone && (
                    <p className="text-neutral-600 dark:text-neutral-400 text-sm mt-1">
                      Phone: {address.phone}
                    </p>
                  )}
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleEdit(address)}
                    className="p-2 text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 transition-colors"
                  >
                    <PencilIcon className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => handleDelete(address.id)}
                    className="p-2 text-red-600 hover:text-red-700 dark:text-red-400 dark:hover:text-red-300 transition-colors"
                  >
                    <TrashIcon className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Addresses;
