/**
 * ResetPassword.js
 * 
 * This component allows users to reset their password using a secure token.
 * It validates the new password for strength and ensures confirmation matches.
 * Upon successful reset, the user is redirected to the homepage.
 * 
 * Developer: Zhaoyi Yang
 */


import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import './ResetPassword.css';

function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    if (!token) {
      setError('Invalid or missing reset token.');
      return;
    }

    const passwordRegex = /^(?=.*[A-Z])(?=.*\d).{8,}$/;
    if (!passwordRegex.test(newPassword)) {
      setError('❌ Password must be at least 8 characters long, include at least one uppercase letter and one number.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('❌ Passwords do not match!');
      return;
    }

    console.log("📦 Payload being sent:", {
      token,
      newPassword,
    });

    setIsSubmitting(true);
    try {
      const res = await fetch('http://localhost:8080/users/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          token,
          newPassword,
        }),
      });

      if (res.ok) {
        setSuccessMessage('✅ Password has been reset successfully! Redirecting...');
        setTimeout(() => navigate('/'), 8080);
      } else {
        const data = await res.json();
        setError(data.message || 'Reset failed');
      }
    } catch (err) {
      console.error('Reset password error:', err);
      setError('Server error. Please try again later.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="reset-password-container">
      <h2 className="reset-title">Reset Your Password</h2>
      <form onSubmit={handleSubmit} className="reset-password-form">
        <label>New Password</label>
        <input
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          required
        />

        <label>Confirm Password</label>
        <input
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Resetting...' : 'Reset Password'}
        </button>
        {error && <p className="error-message">{error}</p>}
        {successMessage && <p className="success-message">{successMessage}</p>}
      </form>
    </div>
  );
}

export default ResetPassword;
