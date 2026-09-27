import { Link } from "react-router-dom";

export default function Home() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-4">Welcome to E-Commerce</h1>
      <p className="text-gray-700">
        A platform for discovering and managing your online shopping experience.
      </p>
      <div className="mt-4 flex gap-4">
        <Link to="/register" className="text-blue-600 hover:underline">
          Register
        </Link>
        <Link to="/login" className="text-blue-600 hover:underline">
          Login
        </Link>
      </div>
    </div>
  );
}
