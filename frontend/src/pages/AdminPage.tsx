import { useEffect, useState } from 'react';
import type { UserRecord } from '../api/adminApi';
import { activateUser, deactivateUser, getUsers } from '../api/adminApi';
import { UserTable } from '../components/admin/UserTable';
import { Navbar } from '../components/layout/Navbar';

export const AdminPage = () => {
  const [users, setUsers] = useState<UserRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getUsers()
      .then(setUsers)
      .catch(() => setError('Failed to load users.'))
      .finally(() => setLoading(false));
  }, []);

  const handleToggle = async (user: UserRecord) => {
    try {
      const updated = user.active ? await deactivateUser(user.id) : await activateUser(user.id);
      setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)));
    } catch {
      alert('Failed to update user status.');
    }
  };

  return (
    <>
      <Navbar />
      <div className="max-w-5xl mx-auto p-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">User Management</h1>

        {error && (
          <p className="text-red-600 text-sm mb-4">{error}</p>
        )}

        {loading ? (
          <p className="text-center py-12 text-gray-400">Loading...</p>
        ) : users.length === 0 ? (
          <p className="text-gray-400 text-center py-8">No users found.</p>
        ) : (
          <UserTable users={users} onToggle={handleToggle} />
        )}
      </div>
    </>
  );
};
