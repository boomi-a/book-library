interface Props {
  items: { name: string; count: number }[];
  emptyText: string;
}

export const RankedList = ({ items, emptyText }: Props) => {
  if (items.length === 0) {
    return <p className="text-sm text-[var(--text-muted)]">{emptyText}</p>;
  }
  return (
    <ol className="divide-y divide-black/5">
      {items.map((item, i) => (
        <li key={item.name} className="flex items-center justify-between py-2 text-sm">
          <span className="text-[var(--text-primary)]">
            <span className="mr-2 text-[var(--text-muted)]">{i + 1}.</span>
            {item.name}
          </span>
          <span className="font-medium text-[var(--text-primary)]">
            {item.count} {item.count === 1 ? 'book' : 'books'}
          </span>
        </li>
      ))}
    </ol>
  );
};
