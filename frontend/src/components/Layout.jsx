import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import ChatPanel from './ChatPanel';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1 container mx-auto px-4 py-6">
        <Outlet />
      </main>
      {/* Logged-in users only (the chat needs the gateway's user id). On logout the
          panel is removed, so its conversation is forgotten. */}
      {isAuthenticated && <ChatPanel />}
    </div>
  );
}
