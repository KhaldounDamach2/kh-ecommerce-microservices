import { useAuth } from "../context/AuthContext";

export default function CustomerDashboard() {
  const { user } = useAuth();

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <div className="flex items-center gap-3">
          <h1 className="text-3xl font-bold mb-2">
            Welcome back, {user.email}
          </h1>
          <span className="inline-block px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
            {user.role}
          </span>
        </div>
        <p className="text-gray-600">Manage your shopping experience.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {[
          ["Browse Products", "Coming soon"],
          ["My Orders", "Coming soon"],
          ["My Profile", "Coming soon"],
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
