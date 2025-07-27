/**
 * TeamCreateDialog.js
 * 
 * A simple dialog component for users to create new teams with a name and description.
 * Used in the Participant Team Management module.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  TextField,
  DialogActions,
  Button
} from '@mui/material';

function TeamCreateDialog({ open, onClose, onCreate }) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const handleSubmit = () => {
    onCreate(name, description);
    setName('');
    setDescription('');
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      sx={{
        '& .MuiDialog-paper': {
          borderRadius: '12px',
          padding: '12px'
        }
      }}
    >
      <DialogTitle sx={{ color: '#FF5722', fontWeight: 'bold' }}>Create Team</DialogTitle>
      <DialogContent>
        <TextField
          label="Team Name"
          value={name}
          onChange={e => setName(e.target.value)}
          fullWidth
          margin="dense"
          size="small"
        />
        <TextField
          label="Description"
          value={description}
          onChange={e => setDescription(e.target.value)}
          fullWidth
          multiline
          rows={3}
          margin="dense"
          size="small"
        />
      </DialogContent>
      <DialogActions>
        <Button
          onClick={onClose}
          variant="contained"
          sx={{
            backgroundColor: '#FF7F50',
            color: '#fff',
            '&:hover': { backgroundColor: '#FF5722' }
          }}
        >
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={!name.trim()}
          sx={{
            backgroundColor: '#FF7F50',
            color: '#fff',
            '&:hover': { backgroundColor: '#FF5722' }
          }}
        >
          Create
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default TeamCreateDialog;
