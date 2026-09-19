import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteBook, getBooks } from '../api/bookApi';
import { BookCard } from '../components/books/BookCard';
import { BookFilter } from '../components/books/BookFilter';
import { Navbar } from '../components/layout/Navbar';
import type { Book, BookFilters } from '../types/book.types';

export const DashboardPage = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [filters, setFilters] = useState<BookFilters>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const loadBooks = async () => {
    setLoading(true);
    setError('');
    try {
      setBooks(await getBooks(filters));
    } catch {
      setError('Failed to load books.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBooks();
  }, [filters]);

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this book?')) return;
    try {
      await deleteBook(id);
      setBooks((prev) => prev.filter((b) => b.id !== id));
    } catch {
      alert('Failed to delete book.');
    }
  };

  return (
    <>
      <Navbar />
      <div className="max-w-5xl mx-auto p-6">
        <div className="flex justify-between items-center mb-5">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Library</h1>
            <p className="text-sm text-gray-500 mt-0.5">{books.length} book{books.length !== 1 ? 's' : ''}</p>
          </div>
          <Link
            to="/books/add"
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-sm font-medium"
          >
            + Add Book
          </Link>
        </div>

        <BookFilter filters={filters} onChange={setFilters} />

        {error && (
          <p className="text-red-600 text-sm mb-4">{error}</p>
        )}

        {loading ? (
          <p className="text-center py-12 text-gray-400">Loading...</p>
        ) : books.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-lg">No books found.</p>
            <p className="text-sm mt-1">
              <Link to="/books/add" className="text-blue-600 hover:underline">
                Add your first book
              </Link>
            </p>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {books.map((b) => (
              <BookCard key={b.id} book={b} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </div>
    </>
  );
};
