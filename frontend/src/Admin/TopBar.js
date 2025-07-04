/**
 * @file TopBar.jsx
 * @description 
 * This component renders the top navigation bar for the admin interface.
 * It allows admin users to:
 *  - View a personalized welcome message with their email address.
 *  - View and click their avatar to navigate to the Profile page.
 *  - Log out from the platform with a confirmation dialog.
 * 
 * The component fetches the user's profile information (avatar) on mount,
 * manages logout by clearing localStorage and redirecting to the home page,
 * and uses Material-UI and React-Icons for styling and interaction.
 * 
 * Role: Admin
 * Developer: Zhaoyi Yang
 */


import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './TopBar.css';
import { FiLogOut } from 'react-icons/fi';
import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Button,
  Typography
} from '@mui/material';

function TopBar() {
  const [open, setOpen] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState('');
  const [email, setEmail] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    setEmail(localStorage.getItem("email"));

    const fetchUserAvatar = async () => {
      try {
        const response = await fetch('http://localhost:8080/users/profile', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });
        const data = await response.json();
        if (response.ok && data.avatarUrl) {
          setAvatarUrl(data.avatarUrl);
        }
      } catch (error) {
        console.error("Failed to fetch user avatar:", error);
      }
    };

    fetchUserAvatar();
  }, []);

  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  const handleConfirmLogout = async () => {
    try {
      await fetch("http://localhost:8080/users/logout", {
        method: "POST",
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });
    } catch (err) {
      console.error("Logout error:", err);
    }

    localStorage.clear();
    setOpen(false);
    window.location.href = "/";
  };

  const handleAvatarClick = () => {
    if (email) {
      navigate(`/AdminProfile`);
    }
  };

  return (
    <header className="topbar">
      <div className="topbar-content">
        <div className="welcome-text">
          <Typography variant="h6" className="welcome-line">
            🎉 Welcome back, Admin!
          </Typography>
          <Typography variant="body2" className="email-line">
            Logged in as: <strong>{email}</strong>
          </Typography>
        </div>

        <div className="topbar-actions">
          <img
            className="avatar"
            src={avatarUrl || "/OIP.jpg"}
            alt="User Avatar"
            onClick={handleAvatarClick}
          />
          <FiLogOut className="logout-icon" onClick={handleOpen} />
        </div>
      </div>

      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Log out reminder</DialogTitle>
        <DialogContent>
          <DialogContentText>Are you sure you want to log out?</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={handleConfirmLogout} color="error">Log out</Button>
        </DialogActions>
      </Dialog>
    </header>
  );
}

export default TopBar;
