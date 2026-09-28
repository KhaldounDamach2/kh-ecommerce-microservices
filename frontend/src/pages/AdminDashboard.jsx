import { useAuth } from "../context/AuthContext";

export default function AdminDashboard() {
  const { user } = useAuth();

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-2">
        Admin Control Panel — {user.email}
      </h1>
      <p className="text-gray-600 mb-8">Monitor and manage the platform.</p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {[
          ["Users", "Manage all users"],
          ["Products", "Moderate all products"],
          ["Orders", "View all orders"],
          ["Reports", "System analytics"],
        ].map(([title, description]) => (
          <div
            key={title}
            className="bg-white p-6 rounded-lg shadow hover:shadow-md transition"
          >
            <h2 className="text-xl font-semibold mb-2">{title}</h2>
            <p className="text-gray-600 text-sm">{description}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
