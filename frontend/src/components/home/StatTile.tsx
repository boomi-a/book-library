import type { ReactNode } from 'react';

interface Props {
  label: string;
  value: string | number;
  sub?: string;
  children?: ReactNode;
}

export const StatTile = ({ label, value, sub, children }: Props) => (
  <div className="rounded-xl border border-black/10 bg-[var(--surface-1)] p-5">
    <p className="text-xs font-medium uppercase tracking-wide text-[var(--text-secondary)]">{label}</p>
    <p className="mt-2 text-3xl font-semibold text-[var(--text-primary)]">{value}</p>
    {sub && <p className="mt-1 text-sm text-[var(--text-secondary)]">{sub}</p>}
    {children}
  </div>
);
