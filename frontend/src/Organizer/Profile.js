/**
 * @file OrganizerProfile.js
 * @description
 * This component allows an Organizer to view and update their profile information.
 * Functionalities include:
 *  - Update name, password, description, and avatar.
 *  - Upload a new avatar.
 *  - Delete their own account permanently.
 *  - Password must meet specific strength criteria (minimum 8 characters, at least one uppercase letter).
 * 
 * The email and role fields are read-only.
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useState, useEffect } from 'react';
import TopBar from './TopBar';
import Sidebar from './Sidebar';
import './Profile.css';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

function OrganizerProfile() {
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
        alert('❌ Password must be at least 8 characters and contain at least one uppercase letter.');
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
        alert('Profile updated successfully');
      } else {
        alert(data.message || 'Error updating profile');
      }
    } catch (error) {
      console.error('Error updating profile:', error);
    }
  };

  const handleAvatarIconClick = () => {
    setAvatarDialogOpen(true);
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (tempAvatarUrl) {
        URL.revokeObjectURL(tempAvatarUrl);
      }
      setTempAvatar(file);
      setTempAvatarUrl(URL.createObjectURL(file));
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
        if (avatarUrl) {
          URL.revokeObjectURL(avatarUrl);
        }
        setAvatarUrl(data.avatarUrl);

        window.location.reload();
      } else {
        alert(data.message || 'Error uploading avatar');
      }
    } catch (error) {
      console.error('Error uploading avatar:', error);
    }
  
    setTempAvatar(null);
    setTempAvatarUrl('');
    setAvatarDialogOpen(false);
  };

  const handleAvatarDialogClose = () => {
    if (tempAvatarUrl) {
      URL.revokeObjectURL(tempAvatarUrl);
    }
    setTempAvatar(null);
    setTempAvatarUrl('');
    setAvatarDialogOpen(false);
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
        alert('Your account has been deleted.');
        localStorage.clear();
        window.location.href = '/';
      } else {
        const data = await response.json();
        alert(data.message || 'Error deleting account.');
      }
    } catch (error) {
      console.error('Error deleting account:', error);
      alert('Server error while deleting account.');
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
      <div className="profile-container">
        <Sidebar />
        <div className="profile-content">
          <div className="profile-header">
            <div className="profile-avatar" onClick={handleAvatarIconClick}>
              <img
                className="avatar"
                src={avatarUrl || '/OIP.jpg'}
                alt="User Avatar"
                style={{ width: '80px', height: '80px', borderRadius: '50%' }}
              />
            </div>
          </div>

          <form className="profile-form" onSubmit={handleSubmit}>
            <label>Name</label>
            <input type="text" name="name" value={formData.name} onChange={handleChange} placeholder="Enter your name" />

            <label>Email</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter your email" />

            <label>Password</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange} placeholder="Enter your new password" />

            <label>Description</label>
            <textarea name="description" value={formData.description || ''} onChange={handleChange} placeholder="Tell us about yourself" />

            <label>User Role</label>
            <input type="text" name="role" value={formData.role} disabled style={{ backgroundColor: '#f3f3f3', cursor: 'not-allowed' }} />

            <button type="submit">Save</button>
            <button type="button" onClick={() => setDeleteDialogOpen(true)} style={{ marginTop: '10px', backgroundColor: '#f44336', color: 'white' }}>Delete Account</button>
          </form>
        </div>
      </div>

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

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Account</DialogTitle>
        <DialogContent>Are you sure you want to delete your account? This action is irreversible.</DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteAccount} color="error">Delete</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default OrganizerProfile;