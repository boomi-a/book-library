import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBook } from '../api/bookApi';
import { BookForm } from '../components/books/BookForm';
import { Navbar } from '../components/layout/Navbar';
import type { BookRequest } from '../types/book.types';

export const AddBookPage = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (data: BookRequest) => {
    setLoading(true);
    setError('');
    try {
      await createBook(data);
      navigate('/dashboard');
    } catch {
      setError('Failed to save book. Please try again.');
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar />
      <div className="max-w-lg mx-auto p-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Add New Book</h1>
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 p-3 rounded mb-4 text-sm">
            {error}
          </div>
        )}
        <BookForm loading={loading} onSubmit={handleSubmit} />
      </div>
    </>
  );
};
