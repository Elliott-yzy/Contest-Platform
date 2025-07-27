/**
 * @file LoginModal.js
 * @description 
 * This component renders the login modal for users to sign into the platform.
 * It allows users to:
 *  - Log in with email, password, and selected role (admin, organizer, participant).
 *  - Navigate to different dashboards based on the logged-in role.
 *  - Request a password reset link if they forgot their password.
 * 
 * The component interacts with the backend login and forgot-password APIs,
 * manages login errors and success messages, and handles navigation after successful authentication.
 * Styling and layout are handled via the Login.css file.
 * 
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";

const LoginModal = ({ onClose, onShowRegister, role }) => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [resetMessage, setResetMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch("http://localhost:8080/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email,
          password,
          role: email === "admin@gmail.com" ? "Admin" : role.toUpperCase(),
        }),

      });

      const data = await res.json();

      if (res.ok) {
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("email", data.email);
        localStorage.setItem("token", data.accessToken);
        localStorage.setItem("role", data.role);

        if (data.role === "Admin") {
          navigate("/AdminDashboard");
        } else if (data.role === "Organizer") {
          navigate(`/OrganizerDashboard/${email}`);
        } else if (data.role === "Participant") {
          navigate(`/profile/${data.email}`);
        } else {
          navigate("/");
        }
        onClose();
      } else {
        console.error("Login failure", data.error || data.message);
        setError(data.error || data.message || "Login failed");
      }
    } catch (err) {
      console.error("Login request error", err);
      setError("Server error.");
    }
  };

  const handleForgotPassword = async () => {
    if (!email) {
      alert("Please enter your email first.");
      return;
    }

    try {
      const res = await fetch(`http://localhost:8080/users/forgot-password?email=${encodeURIComponent(email)}`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
      });

      if (res.ok) {
        setResetMessage("📧 Reset link sent to your email.");
      } else {
        setResetMessage("⚠️ Unable to send reset link. Please check the email.");
      }
    } catch (err) {
      console.error("Forgot the password and the request failed", err);
      setResetMessage("🚨 Server error. Please try again later.");
    }
  };

  return (
    <div className="login-overlay">
      <div className="login-modal">
        <h3 className="login-title">Sign in to our platform</h3>
        <button className="close-btn" onClick={onClose}>×</button>

        <form className="login-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Your email</label>
            <input
              type="email"
              id="email"
              placeholder="name@company.com"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Your password</label>
            <input
              type="password"
              id="password"
              placeholder="••••••••"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <button type="submit" className="login-button">
            Login to your account
          </button>

          {error && <p className="error-message">{error}</p>}

          <div className="login-extra-links">
            <p>
              Forgot your password?{" "}
              <button type="button" onClick={handleForgotPassword} className="login-link">
                Get help logging in
              </button>
            </p>

            <p>
              Don't have an account?{" "}
              <button type="button" onClick={onShowRegister} className="login-link">
                Sign up Now
              </button>
            </p>

            {resetMessage && <p className="reset-message">{resetMessage}</p>}
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginModal;
