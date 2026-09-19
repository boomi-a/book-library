import type { UserRecord } from '../../api/adminApi';

interface Props {
  users: UserRecord[];
  onToggle: (user: UserRecord) => void;
}

export const UserTable = ({ users, onToggle }: Props) => (
  <div className="overflow-x-auto">
    <table className="w-full border-collapse text-sm">
      <thead>
        <tr className="bg-gray-100 text-left">
          {['Username', 'Email', 'Role', 'Status', 'Joined', 'Action'].map((h) => (
            <th key={h} className="border px-4 py-2 font-medium text-gray-700">
              {h}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {users.map((u) => (
          <tr key={u.id} className="hover:bg-gray-50">
            <td className="border px-4 py-2 font-medium">{u.username}</td>
            <td className="border px-4 py-2 text-gray-600">{u.email}</td>
            <td className="border px-4 py-2">
              <span className={u.role === 'ADMIN' ? 'text-purple-700 font-semibold' : ''}>
                {u.role}
              </span>
            </td>
            <td className="border px-4 py-2">
              <span className={u.active ? 'text-green-600 font-medium' : 'text-red-500 font-medium'}>
                {u.active ? 'Active' : 'Inactive'}
              </span>
            </td>
            <td className="border px-4 py-2 text-gray-500">
              {new Date(u.createdAt).toLocaleDateString()}
            </td>
            <td className="border px-4 py-2">
              <button
                onClick={() => onToggle(u)}
                className={`text-xs px-3 py-1 rounded font-medium ${
                  u.active
                    ? 'bg-red-100 text-red-600 hover:bg-red-200'
                    : 'bg-green-100 text-green-600 hover:bg-green-200'
                }`}
              >
                {u.active ? 'Deactivate' : 'Activate'}
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);
