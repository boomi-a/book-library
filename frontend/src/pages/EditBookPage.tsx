import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getBook, updateBook } from '../api/bookApi';
import { BookForm } from '../components/books/BookForm';
import { Navbar } from '../components/layout/Navbar';
import type { Book, BookRequest } from '../types/book.types';

export const EditBookPage = () => {
  const { id } = useParams<{ id: string }>();
  const [book, setBook] = useState<Book | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    if (!id) return;
    getBook(id)
      .then(setBook)
      .catch(() => setError('Book not found.'))
      .finally(() => setFetching(false));
  }, [id]);

  const handleSubmit = async (data: BookRequest) => {
    if (!id) return;
    setLoading(true);
    setError('');
    try {
      await updateBook(id, data);
      navigate('/dashboard');
    } catch {
      setError('Failed to update book. Please try again.');
      setLoading(false);
    }
  };

  return (
    <>
      <Navbar />
      <div className="max-w-lg mx-auto p-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Edit Book</h1>
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 p-3 rounded mb-4 text-sm">
            {error}
          </div>
        )}
        {fetching ? (
          <p className="text-gray-400">Loading...</p>
        ) : book ? (
          <BookForm initial={book} loading={loading} onSubmit={handleSubmit} />
        ) : null}
      </div>
    </>
  );
};
