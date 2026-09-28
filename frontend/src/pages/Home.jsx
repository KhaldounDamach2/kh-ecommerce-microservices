import { Link } from "react-router-dom";

export default function Home() {
  return (
    <div className="max-w-2xl mx-auto py-16 text-center">
      <h1 className="text-3xl font-bold mb-4">
        E-Commerce Microservices Platform
      </h1>
      <p className="text-gray-700 mb-8">
        A modern platform for discovering, buying, and managing products through
        connected services.
      </p>
      <div className="flex justify-center gap-4">
        <Link
          to="/register"
          className="bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700"
        >
          Get Started
        </Link>
        <Link
          to="/login"
          className="bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700"
        >
          Login
        </Link>
      </div>
    </div>
  );
}
