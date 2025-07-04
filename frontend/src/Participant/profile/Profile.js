/**
 * @file Profile.js
 * @description 
 * This component allows participants to view and update their profile information.
 * Features include:
 *  - Viewing and editing basic user information (name, email, password, description).
 *  - Uploading a new profile avatar image with validation checks.
 *  - Deleting the user account with confirmation.
 *  - Providing real-time feedback through Snackbar notifications.
 *  - Utilizing Material-UI components for consistent and user-friendly design.
 * The component communicates with the backend API for fetching, updating, and deleting user data.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect } from 'react';
import TopBar from '../TopSide/TopBar';
import Sidebar from '../TopSide/Sidebar';
import './Profile.css';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Snackbar, Alert } from '@mui/material';
import BadgeIcon from '@mui/icons-material/Badge';
import EmailIcon from '@mui/icons-material/Email';
import LockIcon from '@mui/icons-material/Lock';
import DescriptionIcon from '@mui/icons-material/Description';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

function Profile() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    description: '',
    role: localStorage.getItem('role') || ''
  });

  const [avatarDialogOpen, setAvatarDialogOpen] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState('');
  const [tempAvatar, setTempAvatar] = useState(null);
  const [tempAvatarUrl, setTempAvatarUrl] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'warning' }); // Orange color

  const showSnackbar = (message, severity = 'warning') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = (event, reason) => {
    if (event) {
      event.stopPropagation();
    }
    setSnackbar({ ...snackbar, open: false });
  };

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const response = await fetch('http://localhost:8080/users/profile', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
            'User-ID': localStorage.getItem('userId')
          }
        });
        const data = await response.json();
        if (response.ok) {
          setFormData({
            name: data.name || '',
            email: data.email || '',
            password: '',
            description: data.description || '',
            role: localStorage.getItem('role') || ''
          });
          setAvatarUrl(data.avatarUrl);
        }
      } catch (error) {
        console.error('Error fetching user data:', error);
      }
    };

    fetchUserData();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({ ...prevData, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password) {
      const passwordRegex = /^(?=.*[A-Z]).{8,}$/;
      if (!passwordRegex.test(formData.password)) {
        showSnackbar('Password must be at least 8 characters and contain at least one uppercase letter.', 'warning');
        return;
      }
    }

    try {
      const response = await fetch('http://localhost:8080/users/profile', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'User-ID': localStorage.getItem('userId')
        },
        body: JSON.stringify({
          ...formData,
          avatarUrl
        })
      });
      const data = await response.json();
      if (response.ok) {
        showSnackbar('Profile updated successfully!', 'success');
      } else {
        showSnackbar(data.message || 'Error updating profile.', 'error');
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      showSnackbar('Server error while updating profile.', 'error');
    }
  };

  const handleAvatarIconClick = () => {
    setAvatarDialogOpen(true);
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const newFileName = file.name.replace(/[^\w.-]/g, '_');
      const encodedFileName = encodeURIComponent(newFileName);
      const renamedFile = new File([file], encodedFileName, { type: file.type });

      if (tempAvatarUrl) {
        URL.revokeObjectURL(tempAvatarUrl);
      }

      const isValidImage = renamedFile.type.startsWith('image/');
      if (!isValidImage) {
        showSnackbar('Please select a valid image file.', 'warning');
        return;
      }

      const maxSize = 5 * 1024 * 1024;
      if (renamedFile.size > maxSize) {
        showSnackbar('File size exceeds the 5MB limit. Please select a smaller image.', 'warning');
        return;
      }

      setTempAvatar(renamedFile);
      setTempAvatarUrl(URL.createObjectURL(renamedFile));
    }
  };

  const handleAvatarSave = async () => {
    if (!tempAvatar) return;

    const formDataUpload = new FormData();
    formDataUpload.append('file', tempAvatar);

    try {
      const response = await fetch('http://localhost:8080/users/profile/avatar', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'User-ID': localStorage.getItem('userId')
        },
        body: formDataUpload
      });

      const data = await response.json();
      if (response.ok) {
        setAvatarUrl(data.avatarUrl);
        setTempAvatar(null);
        setTempAvatarUrl('');
        setAvatarDialogOpen(false);
        window.location.reload(true);
      } else {
        console.error('Error uploading avatar:', data);
        showSnackbar(data.message || 'Error uploading avatar.', 'error');
      }
    } catch (error) {
      console.error('Error uploading avatar:', error);
      showSnackbar('Failed to upload avatar.', 'error');
    }
  };

  const handleAvatarDialogClose = () => {
    if (tempAvatarUrl) {
      URL.revokeObjectURL(tempAvatarUrl);
    }
    setTempAvatar(null);
    setTempAvatarUrl('');
    setAvatarDialogOpen(false);

    const fileInput = document.querySelector('input[type="file"]');
    if (fileInput) {
      fileInput.value = '';
    }
  };

  const handleDeleteAccount = async () => {
    const userId = localStorage.getItem('userId');
    const role = localStorage.getItem('role');

    try {
      const response = await fetch(`http://localhost:8080/users/${userId}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
          'User-ID': userId,
          'User-Role': role
        }
      });

      if (response.ok) {
        showSnackbar('Your account has been deleted.', 'success');
        localStorage.clear();
        setTimeout(() => {
          window.location.href = '/';
        }, 1500);
      } else {
        const data = await response.json();
        showSnackbar(data.message || 'Error deleting account.', 'error');
      }
    } catch (error) {
      console.error('Error deleting account:', error);
      showSnackbar('Server error while deleting account.', 'error');
    } finally {
      setDeleteDialogOpen(false);
    }
  };

  useEffect(() => {
    return () => {
      if (avatarUrl) {
        URL.revokeObjectURL(avatarUrl);
      }
    };
  }, [avatarUrl]);

  return (
    <>
      <TopBar />
      <div className="Participantprofile-container">
        <Sidebar />
        <div className="Participantprofile-content">
          {/* 👇 The entire work badge, including the lanyard hole + avatar + form */}
          <div className="profile-badge">
            {/* The lanyard hole is ::before. Just put the avatar here directly */}
            <div
              className="profile-avatar"
              style={{ position: 'relative', marginTop: '-60px' }}
              onClick={handleAvatarIconClick}
            >
              <img
                className="avatar"
                src={avatarUrl || '/OIP.jpg'}
                alt="User Avatar"
                style={{
                  width: '100px',
                  height: '100px',
                  borderRadius: '50%',
                  border: '3px solid white',
                  backgroundColor: 'white',
                  boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
                }}
              />
              <div className="edit-icon" title="Edit Avatar">✏️</div>
            </div>

            {/* Form section */}
            <form className="profile-form" onSubmit={handleSubmit}>
              <div className="profile-form-columns">
                {/* Left Column */}
                <div className="form-left">
                  <div className="input-group">
                    <BadgeIcon style={{ marginRight: '8px' }} />
                    <label>Name</label>
                  </div>
                  <input type="text" name="name" value={formData.name} onChange={handleChange} placeholder="Enter name" />

                  <div className="input-group">
                    <EmailIcon style={{ marginRight: '8px' }} />
                    <label>Email</label>
                  </div>
                  <input type="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter email" />

                  <div className="input-group">
                    <LockIcon style={{ marginRight: '8px' }} />
                    <label>Password</label>
                  </div>
                  <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="Enter new password" />

                  <div className="input-group">
                    <AccountCircleIcon style={{ marginRight: '8px' }} />
                    <label>User Role</label>
                  </div>
                  <input type="text" name="role" value={formData.role} disabled style={{ backgroundColor: '#f3f3f3', cursor: 'not-allowed' }} />

                </div>

                {/* Right Column */}
                <div className="form-right">
                  <div className="input-group">
                    <DescriptionIcon style={{ marginRight: '8px' }} />
                    <label>Description</label>
                  </div>
                  <textarea name="description" value={formData.description || ''} onChange={handleChange} placeholder="Tell us about yourself"
                    style={{ minHeight: '150px' }} />
                  <button type="submit" className="save-button">💾 Save</button>
                  <button type="button" onClick={() => setDeleteDialogOpen(true)} className="delete-button">🗑️ Delete Account</button>
                </div>
              </div>
            </form>

          </div>
          {/* 👆 profile-badge end */}
        </div>
      </div>

      {/* Avatar Upload Dialog */}
      <Dialog open={avatarDialogOpen} onClose={handleAvatarDialogClose}>
        <DialogTitle>Upload Avatar</DialogTitle>
        <DialogContent>
          <input accept="image/*" type="file" onChange={handleAvatarChange} style={{ marginTop: '16px' }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleAvatarDialogClose}>Cancel</Button>
          <Button onClick={handleAvatarSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Delete Account Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Account</DialogTitle>
        <DialogContent>Are you sure you want to delete your account? This action is irreversible.</DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteAccount} color="error">Delete</Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for feedback */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={handleSnackbarClose}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );

}

export default Profile;
