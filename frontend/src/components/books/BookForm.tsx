import { useState } from 'react';
import type { Book, BookRequest } from '../../types/book.types';

interface Props {
  initial?: Book;
  onSubmit: (data: BookRequest) => void;
  loading?: boolean;
}

export const BookForm = ({ initial, onSubmit, loading }: Props) => {
  const [form, setForm] = useState<BookRequest>({
    bookName: initial?.bookName ?? '',
    author: initial?.author ?? '',
    publisher: initial?.publisher ?? '',
    category: initial?.category ?? '',
    boughtAt: initial?.boughtAt ? initial.boughtAt.slice(0, 16) : '',
    read: initial?.read ?? false,
    comments: initial?.comments ?? '',
  });

  const set = (field: keyof BookRequest, value: unknown) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const payload: BookRequest = {
      ...form,
      boughtAt: form.boughtAt ? new Date(form.boughtAt).toISOString() : undefined,
    };
    onSubmit(payload);
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Book Name *</label>
        <input
          required
          value={form.bookName}
          onChange={(e) => set('bookName', e.target.value)}
          className="border rounded px-3 py-2 w-full text-sm"
          placeholder="e.g. The Pragmatic Programmer"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Author</label>
        <input
          value={form.author}
          onChange={(e) => set('author', e.target.value)}
          className="border rounded px-3 py-2 w-full text-sm"
          placeholder="e.g. David Thomas"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Publisher</label>
        <input
          value={form.publisher}
          onChange={(e) => set('publisher', e.target.value)}
          className="border rounded px-3 py-2 w-full text-sm"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
        <input
          value={form.category}
          onChange={(e) => set('category', e.target.value)}
          className="border rounded px-3 py-2 w-full text-sm"
          placeholder="e.g. Technology, Fiction, Science..."
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Date Bought</label>
        <input
          type="datetime-local"
          value={form.boughtAt ?? ''}
          onChange={(e) => set('boughtAt', e.target.value)}
          className="border rounded px-3 py-2 w-full text-sm"
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Comments</label>
        <textarea
          value={form.comments}
          onChange={(e) => set('comments', e.target.value)}
          className="border rounded px-3 py-2 w-full text-sm h-24 resize-none"
          placeholder="Your notes about this book..."
        />
      </div>
      <label className="flex items-center gap-2 cursor-pointer">
        <input
          type="checkbox"
          checked={form.read}
          onChange={(e) => set('read', e.target.checked)}
          className="w-4 h-4"
        />
        <span className="text-sm font-medium text-gray-700">Mark as Read</span>
      </label>
      <button
        type="submit"
        disabled={loading}
        className="bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50 font-medium"
      >
        {loading ? 'Saving...' : initial ? 'Update Book' : 'Add Book'}
      </button>
    </form>
  );
};
