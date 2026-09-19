import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

export const Navbar = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-blue-700 text-white px-6 py-3 flex justify-between items-center shadow">
      <Link to="/dashboard" className="text-xl font-bold tracking-tight">
        BookLibrary
      </Link>
      <div className="flex gap-4 items-center">
        {user?.role === 'ADMIN' && (
          <Link to="/admin" className="text-sm hover:underline">
            Admin Panel
          </Link>
        )}
        <span className="text-sm opacity-80">{user?.username}</span>
        <button
          onClick={handleLogout}
          className="bg-white text-blue-700 px-3 py-1 rounded text-sm font-medium hover:bg-blue-50"
        >
          Logout
        </button>
      </div>
    </nav>
  );
};
