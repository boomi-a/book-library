import type { Book } from '../types/book.types';

export interface CategoryCount {
  label: string;
  count: number;
}

export interface AuthorCount {
  name: string;
  count: number;
}

export interface MonthCount {
  key: string;
  label: string;
  count: number;
}

export interface LibraryStats {
  total: number;
  read: number;
  unread: number;
  readPct: number;
  authors: number;
  categories: number;
  uncategorized: number;
  boughtThisYear: number;
  withoutPurchaseDate: number;
  byCategory: CategoryCount[];
  topAuthors: AuthorCount[];
  purchasesByMonth: MonthCount[];
}

// One categorical slot per bar: keep the chart to 6 bars, folding the rest into "Other".
const MAX_CATEGORY_BARS = 6;
const TOP_AUTHORS = 5;
const MONTHS = 12;

const monthKey = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;

const countBy = (values: string[]) => {
  const counts = new Map<string, number>();
  for (const value of values) counts.set(value, (counts.get(value) ?? 0) + 1);
  return counts;
};

const sortByCount = <T extends { count: number }>(items: T[]) =>
  [...items].sort((a, b) => b.count - a.count);

export const computeLibraryStats = (books: Book[], now = new Date()): LibraryStats => {
  const total = books.length;
  const read = books.filter((b) => b.read).length;

  // Uncategorized books are reported separately so they don't swamp the category bars.
  const categorized = books.map((b) => b.category?.trim()).filter((c): c is string => !!c);
  const categoryCounts = countBy(categorized);
  let byCategory: CategoryCount[] = sortByCount(
    [...categoryCounts].map(([label, count]) => ({ label, count }))
  );
  if (byCategory.length > MAX_CATEGORY_BARS) {
    const head = byCategory.slice(0, MAX_CATEGORY_BARS - 1);
    const otherCount = byCategory
      .slice(MAX_CATEGORY_BARS - 1)
      .reduce((sum, c) => sum + c.count, 0);
    byCategory = [...head, { label: 'Other', count: otherCount }];
  }

  const authorCounts = countBy(
    books.map((b) => b.author?.trim()).filter((a): a is string => !!a)
  );
  const topAuthors = sortByCount(
    [...authorCounts].map(([name, count]) => ({ name, count }))
  ).slice(0, TOP_AUTHORS);

  const purchasesByKey = countBy(
    books.filter((b) => b.boughtAt).map((b) => monthKey(new Date(b.boughtAt as string)))
  );
  const purchasesByMonth: MonthCount[] = [];
  for (let i = MONTHS - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const key = monthKey(d);
    purchasesByMonth.push({
      key,
      label: d.toLocaleString('en', { month: 'short' }),
      count: purchasesByKey.get(key) ?? 0,
    });
  }

  const boughtThisYear = books.filter(
    (b) => b.boughtAt && new Date(b.boughtAt).getFullYear() === now.getFullYear()
  ).length;

  return {
    total,
    read,
    unread: total - read,
    readPct: total ? Math.round((read / total) * 100) : 0,
    authors: authorCounts.size,
    categories: categoryCounts.size,
    uncategorized: total - categorized.length,
    boughtThisYear,
    withoutPurchaseDate: books.filter((b) => !b.boughtAt).length,
    byCategory,
    topAuthors,
    purchasesByMonth,
  };
};
