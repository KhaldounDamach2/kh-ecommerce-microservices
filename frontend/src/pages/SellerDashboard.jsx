import { useAuth } from "../context/AuthContext";

export default function SellerDashboard() {
  const { user } = useAuth();

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-2">
        Seller Dashboard — {user.email}
      </h1>
      <p className="text-gray-600 mb-8">
        Manage your store and product listings.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {[
          ["My Products", "Manage your product listings"],
          ["Orders", "View orders for your products"],
          ["Store Settings", "Update your store information"],
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
