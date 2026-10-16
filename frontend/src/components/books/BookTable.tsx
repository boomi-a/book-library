import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import type { Book } from '../../types/book.types';

type SortKey = 'bookName' | 'author' | 'publisher' | 'category' | 'boughtAt' | 'read';
type SortDir = 'asc' | 'desc';

interface Props {
  books: Book[];
  onDelete: (id: string) => void;
  onToggleRead: (book: Book) => void;
}

const COLUMNS: { key: SortKey; label: string }[] = [
  { key: 'bookName', label: 'Title' },
  { key: 'author', label: 'Author' },
  { key: 'publisher', label: 'Publisher' },
  { key: 'category', label: 'Category' },
  { key: 'boughtAt', label: 'Bought' },
  { key: 'read', label: 'Status' },
];

const formatDate = (iso: string | null) => (iso ? new Date(iso).toLocaleDateString() : '');

// Empty values return null so they always sort to the bottom, whichever direction is chosen.
const sortValue = (book: Book, key: SortKey): string | number | null => {
  switch (key) {
    case 'boughtAt':
      return book.boughtAt ? new Date(book.boughtAt).getTime() : null;
    case 'read':
      return book.read ? 1 : 0;
    default:
      return book[key] ?? null;
  }
};

export const BookTable = ({ books, onDelete, onToggleRead }: Props) => {
  const [sort, setSort] = useState<{ key: SortKey; dir: SortDir } | null>(null);

  const sorted = useMemo(() => {
    if (!sort) return books;
    const direction = sort.dir === 'asc' ? 1 : -1;
    return [...books].sort((a, b) => {
      const va = sortValue(a, sort.key);
      const vb = sortValue(b, sort.key);
      if (va === null && vb === null) return 0;
      if (va === null) return 1;
      if (vb === null) return -1;
      if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * direction;
      return String(va).localeCompare(String(vb), undefined, { numeric: true, sensitivity: 'base' }) * direction;
    });
  }, [books, sort]);

  const toggleSort = (key: SortKey) =>
    setSort((prev) =>
      prev?.key === key ? { key, dir: prev.dir === 'asc' ? 'desc' : 'asc' } : { key, dir: 'asc' }
    );

  return (
    <div className="overflow-x-auto rounded-xl border border-black/10 bg-[var(--surface-1)]">
      <table className="min-w-full text-sm">
        <thead className="sticky top-0 bg-[var(--surface-1)] text-left text-xs font-medium uppercase tracking-wide text-[var(--text-secondary)]">
          <tr className="border-b border-black/10">
            {COLUMNS.map((col) => {
              const active = sort?.key === col.key;
              return (
                <th
                  key={col.key}
                  aria-sort={active ? (sort.dir === 'asc' ? 'ascending' : 'descending') : 'none'}
                  className="px-4 py-3"
                >
                  <button
                    onClick={() => toggleSort(col.key)}
                    className={`inline-flex items-center gap-1 uppercase hover:text-[var(--text-primary)] ${
                      active ? 'text-[var(--text-primary)]' : ''
                    }`}
                  >
                    {col.label}
                    <span aria-hidden="true" className="text-[10px]">
                      {active ? (sort.dir === 'asc' ? '▲' : '▼') : '↕'}
                    </span>
                  </button>
                </th>
              );
            })}
            <th className="px-4 py-3 text-right">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-black/5">
          {sorted.map((b) => (
            <tr key={b.id} className="hover:bg-black/[0.02]">
              <td className="max-w-xs px-4 py-2.5">
                <span className="block truncate font-medium text-[var(--text-primary)]" title={b.bookName}>
                  {b.bookName}
                </span>
                {b.comments && (
                  <span className="block truncate text-xs text-[var(--text-muted)]" title={b.comments}>
                    {b.comments}
                  </span>
                )}
              </td>
              <td className="px-4 py-2.5 text-[var(--text-secondary)]">{b.author ?? ''}</td>
              <td className="px-4 py-2.5 text-[var(--text-secondary)]">{b.publisher ?? ''}</td>
              <td className="px-4 py-2.5">
                {b.category && (
                  <span className="rounded bg-blue-100 px-2 py-0.5 text-xs text-blue-800">{b.category}</span>
                )}
              </td>
              <td className="whitespace-nowrap px-4 py-2.5 text-[var(--text-secondary)]">{formatDate(b.boughtAt)}</td>
              <td className="px-4 py-2.5">
                <label className="inline-flex cursor-pointer items-center gap-2 whitespace-nowrap">
                  <input
                    type="checkbox"
                    checked={b.read}
                    onChange={() => onToggleRead(b)}
                    aria-label={`Mark "${b.bookName}" as ${b.read ? 'unread' : 'read'}`}
                    className="h-4 w-4 cursor-pointer accent-blue-600"
                  />
                  <span className={`text-xs font-semibold ${b.read ? 'text-green-700' : 'text-orange-600'}`}>
                    {b.read ? 'Read' : 'Unread'}
                  </span>
                </label>
              </td>
              <td className="whitespace-nowrap px-4 py-2.5 text-right">
                <Link to={`/books/${b.id}/edit`} className="text-blue-600 hover:underline">
                  Edit
                </Link>
                <button onClick={() => onDelete(b.id)} className="ml-3 text-red-600 hover:underline">
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
