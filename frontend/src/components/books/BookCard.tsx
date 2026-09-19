import { Link } from 'react-router-dom';
import type { Book } from '../../types/book.types';

interface Props {
  book: Book;
  onDelete: (id: string) => void;
}

export const BookCard = ({ book, onDelete }: Props) => (
  <div
    className={`border rounded-lg p-4 shadow-sm transition-colors ${
      book.read ? 'bg-green-50 border-green-200' : 'bg-white'
    }`}
  >
    <div className="flex justify-between items-start gap-2">
      <div className="flex-1 min-w-0">
        <h3 className="font-semibold text-gray-900 truncate">{book.bookName}</h3>
        {book.author && <p className="text-sm text-gray-600 mt-0.5">{book.author}</p>}
        {book.publisher && (
          <p className="text-xs text-gray-400 mt-0.5">{book.publisher}</p>
        )}
        <div className="flex flex-wrap gap-1 mt-2">
          {book.category && (
            <span className="inline-block bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded">
              {book.category}
            </span>
          )}
          {book.boughtAt && (
            <span className="text-xs text-gray-400">
              Bought: {new Date(book.boughtAt).toLocaleDateString()}
            </span>
          )}
        </div>
        {book.comments && (
          <p className="text-xs text-gray-500 mt-2 line-clamp-2">{book.comments}</p>
        )}
      </div>
      <span
        className={`text-xs font-semibold shrink-0 px-2 py-1 rounded ${
          book.read
            ? 'bg-green-100 text-green-700'
            : 'bg-orange-100 text-orange-600'
        }`}
      >
        {book.read ? 'Read' : 'Unread'}
      </span>
    </div>
    <div className="flex gap-3 mt-3 pt-3 border-t border-gray-100">
      <Link to={`/books/${book.id}/edit`} className="text-blue-600 text-sm hover:underline">
        Edit
      </Link>
      <button
        onClick={() => onDelete(book.id)}
        className="text-red-600 text-sm hover:underline"
      >
        Delete
      </button>
    </div>
  </div>
);
