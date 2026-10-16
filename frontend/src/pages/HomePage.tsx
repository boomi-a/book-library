import { useEffect, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteBook, getBooks, importBooksFromExcel, updateBook } from '../api/bookApi';
import { createLibrary, getLibraries } from '../api/libraryApi';
import { BookFilter } from '../components/books/BookFilter';
import { BookTable } from '../components/books/BookTable';
import { CategoryBars } from '../components/home/CategoryBars';
import { PurchaseColumns } from '../components/home/PurchaseColumns';
import { RankedList } from '../components/home/RankedList';
import { ReadMeter } from '../components/home/ReadMeter';
import { StatTile } from '../components/home/StatTile';
import { Navbar } from '../components/layout/Navbar';
import { useAuthStore } from '../store/authStore';
import { useLibraryStore } from '../store/libraryStore';
import type { Book, BookFilters, BookImportResult, Library } from '../types/book.types';
import { computeLibraryStats } from '../utils/libraryStats';

const FIELD_LABELS: Record<string, string> = {
  bookName: 'Title',
  author: 'Author',
  publisher: 'Publisher',
  category: 'Category',
  boughtAt: 'Date bought',
  read: 'Read',
  comments: 'Comments',
};

const matchesFilters = (book: Book, filters: BookFilters) => {
  const search = filters.search?.trim().toLowerCase();
  if (search) {
    const inTitle = book.bookName.toLowerCase().includes(search);
    const inAuthor = (book.author ?? '').toLowerCase().includes(search);
    if (!inTitle && !inAuthor) return false;
  }
  const category = filters.category?.trim().toLowerCase();
  if (category && !(book.category ?? '').toLowerCase().includes(category)) return false;
  if (filters.isRead !== undefined && book.read !== filters.isRead) return false;
  return true;
};

export const HomePage = () => {
  const username = useAuthStore((s) => s.user?.username);
  const { currentLibraryId, setCurrentLibraryId } = useLibraryStore();
  const [libraries, setLibraries] = useState<Library[]>([]);
  const [books, setBooks] = useState<Book[]>([]);
  const [filters, setFilters] = useState<BookFilters>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [importing, setImporting] = useState(false);
  const [importResult, setImportResult] = useState<BookImportResult | null>(null);
  const fileInput = useRef<HTMLInputElement>(null);

  const loadBooks = async () => {
    setLoading(true);
    setError('');
    try {
      if (!currentLibraryId) return;
      setBooks(await getBooks(currentLibraryId, {}));
    } catch {
      setError('Failed to load books.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getLibraries()
      .then((list) => {
        setLibraries(list);
        if (!list.some((l) => l.id === currentLibraryId)) {
          setCurrentLibraryId(list[0]?.id ?? null);
        }
      })
      .catch(() => setError('Failed to load libraries.'));
  }, []);

  useEffect(() => {
    loadBooks();
  }, [currentLibraryId]);

  const handleCreateLibrary = async () => {
    const name = window.prompt('Name for the new library, e.g. "Arul Books"')?.trim();
    if (!name) return;
    try {
      const created = await createLibrary(name);
      setLibraries((prev) => [...prev, created]);
      setCurrentLibraryId(created.id);
    } catch (err: any) {
      alert(err.response?.data?.message ?? 'Could not create library.');
    }
  };


  const stats = useMemo(() => computeLibraryStats(books), [books]);
  const visibleBooks = useMemo(() => books.filter((b) => matchesFilters(b, filters)), [books, filters]);
  const recentBooks = useMemo(
    () =>
      [...books]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5),
    [books]
  );

  const handleImport = async (file: File) => {
    if (!currentLibraryId) return;
    setImporting(true);
    setImportResult(null);
    try {
      setImportResult(await importBooksFromExcel(file, currentLibraryId));
      await loadBooks();
    } catch (err: any) {
      setImportResult({
        imported: 0,
        updated: 0,
        skipped: 0,
        errors: [err.response?.data?.message ?? 'Import failed.'],
        columns: {},
        unmatchedHeaders: [],
      });
    } finally {
      setImporting(false);
      if (fileInput.current) fileInput.current.value = '';
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this book?')) return;
    try {
      await deleteBook(id);
      setBooks((prev) => prev.filter((b) => b.id !== id));
    } catch {
      alert('Failed to delete book.');
    }
  };

  const handleToggleRead = async (book: Book) => {
    const nextRead = !book.read;
    setBooks((prev) => prev.map((b) => (b.id === book.id ? { ...b, read: nextRead } : b)));
    try {
      const saved = await updateBook(book.id, {
        bookName: book.bookName,
        author: book.author ?? undefined,
        publisher: book.publisher ?? undefined,
        category: book.category ?? undefined,
        boughtAt: book.boughtAt ?? undefined,
        read: nextRead,
        comments: book.comments ?? undefined,
      });
      setBooks((prev) => prev.map((b) => (b.id === saved.id ? saved : b)));
    } catch {
      setBooks((prev) => prev.map((b) => (b.id === book.id ? { ...b, read: book.read } : b)));
      alert('Failed to update book.');
    }
  };

  return (
    <>
      <Navbar />
      <div className="viz-root min-h-screen bg-[var(--page)]">
        <div className="mx-auto max-w-6xl p-6">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm text-[var(--text-secondary)]">
                Welcome back{username ? `, ${username}` : ''}
              </p>
              <div className="flex items-center gap-3">
                <select
                  value={currentLibraryId ?? ''}
                  onChange={(e) => setCurrentLibraryId(e.target.value)}
                  aria-label="Current library"
                  className="rounded border border-black/15 bg-[var(--surface-1)] px-2 py-1 text-2xl font-semibold text-[var(--text-primary)]"
                >
                  {libraries.map((l) => (
                    <option key={l.id} value={l.id}>
                      {l.name}
                    </option>
                  ))}
                </select>
                <button onClick={handleCreateLibrary} className="text-sm text-blue-600 hover:underline">
                  + New library
                </button>
              </div>
            </div>
            <div className="flex gap-2">
              <input
                ref={fileInput}
                type="file"
                accept=".xlsx"
                className="hidden"
                onChange={(e) => e.target.files?.[0] && handleImport(e.target.files[0])}
              />
              <button
                onClick={() => fileInput.current?.click()}
                disabled={importing}
                className="rounded border border-black/15 bg-[var(--surface-1)] px-4 py-2 text-sm font-medium text-[var(--text-primary)] hover:bg-white disabled:opacity-50"
              >
                {importing ? 'Importing...' : 'Import Excel'}
              </button>
              <Link
                to="/books/add"
                className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
              >
                + Add Book
              </Link>
            </div>
          </div>

          {importResult && (
            <div className="mt-4 rounded border border-black/10 bg-[var(--surface-1)] p-3 text-sm">
              <p className="font-medium text-[var(--text-primary)]">
                Added {importResult.imported} new book{importResult.imported !== 1 ? 's' : ''}
                {`, updated ${importResult.updated} existing`}
                {`, skipped ${importResult.skipped} duplicate${importResult.skipped !== 1 ? 's' : ''}`}
                {importResult.errors.length > 0 &&
                  `, ${importResult.errors.length} row${importResult.errors.length !== 1 ? 's' : ''} with errors`}
              </p>
              {importResult.errors.length > 0 && (
                <ul className="mt-2 max-h-40 list-disc overflow-y-auto pl-5 text-red-600">
                  {importResult.errors.map((msg, i) => (
                    <li key={i}>{msg}</li>
                  ))}
                </ul>
              )}
              <div className="mt-3 text-xs text-[var(--text-secondary)]">
                <p>
                  Mapped columns:{' '}
                  {Object.keys(importResult.columns).length === 0
                    ? 'none'
                    : Object.entries(importResult.columns)
                        .map(([field, header]) => `${FIELD_LABELS[field] ?? field} ← "${header}"`)
                        .join(', ')}
                </p>
                {importResult.unmatchedHeaders.length > 0 && (
                  <p className="mt-1">
                    Ignored columns: {importResult.unmatchedHeaders.map((h) => `"${h}"`).join(', ')}
                  </p>
                )}
              </div>
              <button onClick={() => setImportResult(null)} className="mt-2 text-blue-600 hover:underline">
                Dismiss
              </button>
            </div>
          )}

          <section className="mt-6 flex flex-wrap items-end gap-x-10 gap-y-4 rounded-2xl border border-black/10 bg-[var(--surface-1)] p-8">
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-[var(--text-secondary)]">
                Books in your library
              </p>
              <p className="text-7xl font-semibold leading-none text-[var(--text-primary)]">{stats.total}</p>
            </div>
            <p className="pb-2 text-[var(--text-secondary)]">
              {stats.read} read · {stats.unread} unread · {stats.authors} authors · {stats.categories} categories
            </p>
          </section>

          <section className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatTile label="Read" value={`${stats.readPct}%`}>
              <ReadMeter read={stats.read} total={stats.total} />
            </StatTile>
            <StatTile label="Authors" value={stats.authors} sub="distinct authors" />
            <StatTile label="Categories" value={stats.categories} sub="distinct categories" />
            <StatTile
              label="Bought this year"
              value={stats.boughtThisYear}
              sub={`${stats.withoutPurchaseDate} without a purchase date`}
            />
          </section>

          <section className="mt-4 grid gap-4 lg:grid-cols-5">
            <div className="rounded-xl border border-black/10 bg-[var(--surface-1)] p-5 lg:col-span-3">
              <h2 className="mb-4 text-sm font-semibold text-[var(--text-primary)]">Books by category</h2>
              {stats.total === 0 ? (
                <p className="text-sm text-[var(--text-muted)]">No books yet.</p>
              ) : (
                <>
                  <CategoryBars items={stats.byCategory} total={stats.total} />
                  {stats.uncategorized > 0 && (
                    <p className="mt-4 text-xs text-[var(--text-muted)]">
                      {stats.uncategorized} book{stats.uncategorized !== 1 ? 's' : ''} without a category not shown
                    </p>
                  )}
                </>
              )}
            </div>
            <div className="rounded-xl border border-black/10 bg-[var(--surface-1)] p-5 lg:col-span-2">
              <h2 className="mb-4 text-sm font-semibold text-[var(--text-primary)]">
                Bought in the last 12 months
              </h2>
              <PurchaseColumns months={stats.purchasesByMonth} />
            </div>
          </section>

          <section className="mt-4 grid gap-4 lg:grid-cols-2">
            <div className="rounded-xl border border-black/10 bg-[var(--surface-1)] p-5">
              <h2 className="mb-2 text-sm font-semibold text-[var(--text-primary)]">Top authors</h2>
              <RankedList items={stats.topAuthors} emptyText="No authors recorded yet." />
            </div>
            <div className="rounded-xl border border-black/10 bg-[var(--surface-1)] p-5">
              <h2 className="mb-2 text-sm font-semibold text-[var(--text-primary)]">Recently added</h2>
              {recentBooks.length === 0 ? (
                <p className="text-sm text-[var(--text-muted)]">Nothing added yet.</p>
              ) : (
                <ul className="divide-y divide-black/5">
                  {recentBooks.map((b) => (
                    <li key={b.id} className="flex items-center justify-between py-2 text-sm">
                      <span className="truncate text-[var(--text-primary)]">
                        {b.bookName}
                        {b.author && <span className="text-[var(--text-secondary)]"> · {b.author}</span>}
                      </span>
                      <span className="ml-4 shrink-0 text-xs text-[var(--text-muted)]">
                        {new Date(b.createdAt).toLocaleDateString()}
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </section>

          <section className="mt-10">
            <div className="mb-3 flex items-baseline justify-between">
              <h2 className="text-xl font-semibold text-[var(--text-primary)]">All books</h2>
              <p className="text-sm text-[var(--text-secondary)]">
                {visibleBooks.length} of {stats.total} shown
              </p>
            </div>

            <BookFilter filters={filters} onChange={setFilters} />

            {error && <p className="mb-4 text-sm text-red-600">{error}</p>}

            {loading ? (
              <p className="py-12 text-center text-[var(--text-muted)]">Loading...</p>
            ) : visibleBooks.length === 0 ? (
              <div className="py-16 text-center text-[var(--text-muted)]">
                <p className="text-lg">{stats.total === 0 ? 'No books yet.' : 'No books match these filters.'}</p>
                {stats.total === 0 && (
                  <p className="mt-1 text-sm">
                    <Link to="/books/add" className="text-blue-600 hover:underline">
                      Add your first book
                    </Link>
                  </p>
                )}
              </div>
            ) : (
              <BookTable books={visibleBooks} onDelete={handleDelete} onToggleRead={handleToggleRead} />
            )}
          </section>
        </div>
      </div>
    </>
  );
};
