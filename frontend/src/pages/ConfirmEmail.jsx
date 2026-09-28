import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ConfirmEmail() {
  const { confirmEmail } = useAuth();
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const token = searchParams.get("token");

    if (!token) {
      setStatus("error");
      setMessage("No confirmation token in URL");
      return;
    }

    const confirm = async () => {
      try {
        const response = await confirmEmail(token);
        setStatus("success");
        setMessage(response.message || "Email confirmed!");
      } catch (err) {
        setStatus("error");
        setMessage(err.response?.data?.message || err.message);
      }
    };

    confirm();
  }, [confirmEmail, searchParams]);

  return (
    <div className="max-w-md mx-auto mt-12 p-6 bg-white rounded-lg shadow-md text-center">
      {status === "loading" && (
        <p className="text-gray-700">Confirming your email...</p>
      )}
      {status === "success" && (
        <div className="text-green-600">
          <p className="text-sm mb-4">{message}</p>
          <Link to="/login" className="text-blue-600 hover:underline">
            Go to Login
          </Link>
        </div>
      )}
      {status === "error" && (
        <div className="text-red-600">
          <p className="text-sm mb-4">{message}</p>
          <Link to="/login" className="text-blue-600 hover:underline">
            Go to Login
          </Link>
        </div>
      )}
    </div>
  );
}
