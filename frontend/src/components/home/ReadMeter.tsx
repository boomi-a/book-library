interface Props {
  read: number;
  total: number;
}

export const ReadMeter = ({ read, total }: Props) => {
  const pct = total ? (read / total) * 100 : 0;
  return (
    <div className="mt-4">
      <div className="h-2.5 rounded bg-[var(--track)]">
        <div className="h-full rounded" style={{ width: `${pct}%`, background: 'var(--series-1)' }} />
      </div>
      <p className="mt-2 text-sm text-[var(--text-secondary)]">{read} of {total} read</p>
    </div>
  );
};
