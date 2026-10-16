import type { CategoryCount } from '../../utils/libraryStats';

interface Props {
  items: CategoryCount[];
  total: number;
}

export const CategoryBars = ({ items, total }: Props) => {
  const max = Math.max(...items.map((i) => i.count), 1);
  return (
    <ul className="space-y-3">
      {items.map((item) => (
        <li key={item.label} className="group relative flex items-center gap-3 text-sm">
          <span className="w-32 shrink-0 truncate text-[var(--text-secondary)]">{item.label}</span>
          <div className="h-2.5 flex-1 rounded-r bg-[var(--track)]">
            <div
              className="h-full rounded-r"
              style={{ width: `${(item.count / max) * 100}%`, background: 'var(--series-1)' }}
            />
          </div>
          <span className="w-8 text-right font-medium text-[var(--text-primary)]">{item.count}</span>
          <span className="pointer-events-none absolute -top-8 left-32 z-10 hidden whitespace-nowrap rounded bg-[var(--text-primary)] px-2 py-1 text-xs text-[var(--surface-1)] group-hover:block">
            {item.count} of {total} books · {Math.round((item.count / total) * 100)}%
          </span>
        </li>
      ))}
    </ul>
  );
};
