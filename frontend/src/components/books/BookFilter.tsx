import type { BookFilters } from '../../types/book.types';

interface Props {
  filters: BookFilters;
  onChange: (filters: BookFilters) => void;
}

export const BookFilter = ({ filters, onChange }: Props) => (
  <div className="flex gap-3 flex-wrap mb-4">
    <input
      type="text"
      placeholder="Search by title or author..."
      value={filters.search ?? ''}
      onChange={(e) => onChange({ ...filters, search: e.target.value || undefined })}
      className="border rounded px-3 py-2 flex-1 min-w-48 text-sm"
    />
    <input
      type="text"
      placeholder="Category..."
      value={filters.category ?? ''}
      onChange={(e) => onChange({ ...filters, category: e.target.value || undefined })}
      className="border rounded px-3 py-2 w-36 text-sm"
    />
    <select
      value={filters.isRead === undefined ? '' : String(filters.isRead)}
      onChange={(e) =>
        onChange({
          ...filters,
          isRead: e.target.value === '' ? undefined : e.target.value === 'true',
        })
      }
      className="border rounded px-3 py-2 text-sm"
    >
      <option value="">All books</option>
      <option value="true">Read</option>
      <option value="false">Unread</option>
    </select>
  </div>
);
