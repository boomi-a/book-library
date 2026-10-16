import type { MonthCount } from '../../utils/libraryStats';

interface Props {
  months: MonthCount[];
}

export const PurchaseColumns = ({ months }: Props) => {
  const max = Math.max(...months.map((m) => m.count), 1);
  const peak = months.reduce((a, b) => (b.count > a.count ? b : a), months[0]);
  // Only label the peak when it is a unique maximum; ties would leave an arbitrary label.
  const peakIsUnique = months.filter((m) => m.count === peak.count).length === 1;

  return (
    <div>
      <div className="flex h-44 items-end gap-2 border-b border-[var(--baseline)]">
        {months.map((m) => {
          const height = m.count === 0 ? 0 : Math.max((m.count / max) * 100, 4);
          return (
            <div key={m.key} className="group relative flex h-full flex-1 flex-col justify-end">
              {m.count > 0 && peakIsUnique && m.key === peak.key && (
                <span className="mb-1 text-center text-xs font-medium text-[var(--text-primary)]">{m.count}</span>
              )}
              <div
                className="w-full rounded-t"
                style={{ height: `${height}%`, background: 'var(--series-1)' }}
              />
              <span className="pointer-events-none absolute bottom-full left-1/2 z-10 mb-1 hidden -translate-x-1/2 whitespace-nowrap rounded bg-[var(--text-primary)] px-2 py-1 text-xs text-[var(--surface-1)] group-hover:block">
                {m.label}: {m.count} bought
              </span>
            </div>
          );
        })}
      </div>
      <div className="mt-2 flex gap-2">
        {months.map((m) => (
          <span key={m.key} className="flex-1 text-center text-xs text-[var(--text-muted)]">
            {m.label}
          </span>
        ))}
      </div>
    </div>
  );
};
