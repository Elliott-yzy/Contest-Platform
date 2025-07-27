/**
 * @file Submitbottom.js
 * @description 
 * This file defines two reusable dialog components for participant project management:
 * 
 * 1. SubmitDialog:
 *    - Allows participants to submit their work with title, description, and file upload.
 *    - Supports file type validation based on allowed file extensions.
 *    - Displays warning dialogs for invalid file types or missing file selections.
 * 
 * 2. DeleteDialog:
 *    - Confirms the deletion of a submitted work.
 *    - Provides a simple confirmation popup for delete actions.
 * 
 * Both dialogs use customized MUI Dialog components with consistent orange-themed styling.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Typography, TextField } from '@mui/material';
import { styled } from '@mui/material/styles';
import './Project.css';

// Custom pop-up window style
const CustomDialog = styled(Dialog)(({ theme }) => ({
  '& .MuiPaper-root': {
    borderRadius: 15,
    padding: theme.spacing(2),
    minWidth: 400,
    backgroundColor: '#f9f9f9',
  },
}));

// SubmitDialog support file type verification
export function SubmitDialog({ open, onClose, onSubmit, allowedTypes = [] }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [file, setFile] = useState(null);

  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMessage, setAlertMessage] = useState('');

  const handleSubmit = () => {
    if (!file) {
      setAlertMessage('Please select a file!');
      setAlertOpen(true);
      return;
    }
    onSubmit({ title, description, file });
    setTitle('');
    setDescription('');
    setFile(null);
  };

  const handleCloseAlert = () => {
    setAlertOpen(false);
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (!selectedFile) return;

    const fileName = selectedFile.name.toLowerCase();
    const fileExtension = fileName.split('.').pop(); // Take the file suffix

    // Check if the file type is allowed
    if (allowedTypes.length > 0 && !allowedTypes.map(type => type.toLowerCase()).includes(fileExtension)) {
      setAlertMessage(`Invalid file type! Allowed types: ${allowedTypes.join(', ')}`);
      setAlertOpen(true);
      return;
    }

    setFile(selectedFile);
  };

  return (
    <>
      <CustomDialog open={open} onClose={onClose}>
        <DialogTitle>
          <Typography variant="h6" component="div" sx={{ fontWeight: 'bold' }}>
            Submit Your Work
          </Typography>
        </DialogTitle>
        <DialogContent dividers>
          <TextField
            fullWidth
            label="Title"
            variant="outlined"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            sx={{ marginBottom: 2 }}
          />
          <TextField
            fullWidth
            label="Description"
            variant="outlined"
            multiline
            minRows={2}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            sx={{ marginBottom: 2 }}
          />
          <Button
            variant="outlined"
            component="label"
            sx={{
              borderColor: '#FF9800',
              color: '#FF9800',
              ':hover': { backgroundColor: '#FFE0B2' }
            }}
          >
            Choose File
            <input
              type="file"
              hidden
              onChange={handleFileChange}
            />
          </Button>

          {file && (
            <Typography variant="body2" sx={{ marginTop: 1 }}>
              Selected: {file.name}
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={onClose}
            variant="outlined"
            sx={{
              borderColor: '#FF9800',
              color: '#FF9800',
              ':hover': { backgroundColor: '#FFE0B2' }
            }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            sx={{
              backgroundColor: '#FF9800',
              ':hover': { backgroundColor: '#FB8C00' },
              color: 'white'
            }}
          >
            Submit
          </Button>
        </DialogActions>
      </CustomDialog>

      {/* inner pop-up remind the error */}
      <Dialog open={alertOpen} onClose={handleCloseAlert}>
        <DialogTitle>Warning</DialogTitle>
        <DialogContent dividers>
          <Typography>{alertMessage}</Typography>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={handleCloseAlert}
            sx={{
              color: '#FF9800'
            }}
          >
            OK
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

// DeleteDialog does not need to change, remains the same
export function DeleteDialog({ open, onClose, onDelete, fileName }) {
  return (
    <CustomDialog open={open} onClose={onClose}>
      <DialogTitle>
        <Typography variant="h6" component="div" sx={{ fontWeight: 'bold' }}>
          Delete Submission
        </Typography>
      </DialogTitle>
      <DialogContent dividers>
        <Typography variant="body1" gutterBottom>
          Are you sure you want to delete this file?
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={onClose}
          variant="outlined"
          sx={{
            borderColor: '#FF9800',
            color: '#FF9800',
            ':hover': { backgroundColor: '#FFE0B2' }
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={onDelete}
          variant="contained"
          sx={{
            backgroundColor: '#FF9800',
            ':hover': { backgroundColor: '#FB8C00' },
            color: 'white'
          }}
        >
          Delete
        </Button>
      </DialogActions>
    </CustomDialog>
  );
}
