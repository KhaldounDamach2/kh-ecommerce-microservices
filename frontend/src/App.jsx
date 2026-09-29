import { Routes, Route } from "react-router-dom";
import Layout from "./components/Layout.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Home from "./pages/Home.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import ConfirmEmail from "./pages/ConfirmEmail.jsx";
import CustomerDashboard from "./pages/CustomerDashboard.jsx";
import SellerDashboard from "./pages/SellerDashboard.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import NotFound from "./pages/NotFound.jsx";

import Products from "./pages/Products.jsx";
import ProductDetail from "./pages/ProductDetail.jsx";
import MyProducts from "./pages/seller/MyProducts.jsx";
import NewProduct from "./pages/seller/NewProduct.jsx";
import EditProduct from "./pages/seller/EditProduct.jsx";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/confirm" element={<ConfirmEmail />} />

        <Route
          path="/dashboard/customer"
          element={
            <ProtectedRoute allowedRoles={["CUSTOMER"]}>
              <CustomerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/seller"
          element={
            <ProtectedRoute allowedRoles={["SELLER"]}>
              <SellerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard/admin"
          element={
            <ProtectedRoute allowedRoles={["ADMIN"]}>
              <AdminDashboard />
            </ProtectedRoute>
          }
        />

        <Route path="/products" element={<Products />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route
          path="/seller/products"
          element={
            <ProtectedRoute allowedRoles={["SELLER"]}>
              <MyProducts />
            </ProtectedRoute>
          }
        />
        <Route
          path="/seller/products/new"
          element={
            <ProtectedRoute allowedRoles={["SELLER"]}>
              <NewProduct />
            </ProtectedRoute>
          }
        />
        <Route
          path="/seller/products/:id/edit"
          element={
            <ProtectedRoute allowedRoles={["SELLER"]}>
              <EditProduct />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;
