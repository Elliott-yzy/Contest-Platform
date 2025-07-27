/**
 * @file EditTeamDialog.js
 * @description 
 * This component provides a dialog for participants to edit their team details.
 * 
 * Features include:
 *  - Editing team name and description.
 *  - Viewing team members if the user is the team creator.
 *  - Removing members from the team (only available to the creator, and cannot remove themselves).
 *  - Displays success and error notifications after actions.
 * 
 * External APIs used:
 *  - updateTeam: for updating team information.
 *  - removeTeamMember: for removing a team member.
 *  - getTeamDetail: for fetching team member lists.
 * 
 * The dialog is styled with a soft orange theme for a consistent user experience.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect } from 'react';
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, Alert, List, ListItem, ListItemText, Box
} from '@mui/material';
import { updateTeam, removeTeamMember, getTeamDetail } from './teamApi';

function EditTeamDialog({ open, onClose, team, userData, onUpdated }) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [members, setMembers] = useState([]);

  const isCreator = team?.createdBy === userData.userId;

  useEffect(() => {
    if (team) {
      setName(team.name || '');
      setDescription(team.description || '');
      setError('');
      setSuccess('');
      if (isCreator) {
        getTeamDetail(team.id)
          .then(data => setMembers(data.members || []))
          .catch(err => setError('Failed to load team members: ' + err.message));
      }
    }
  }, [team, isCreator]);

  const handleUpdate = () => {
    if (!name.trim()) {
      setError('Team name is required');
      return;
    }

    updateTeam(team.id, { name: name.trim(), description: description.trim() }, userData, {
      onSuccess: () => {
        setSuccess('Team updated successfully');
        setTimeout(() => {
          setSuccess('');
          onUpdated();
        }, 1000);
      },
      onError: (msg) => setError(msg)
    });
  };

  const handleRemove = async (member) => {
    const confirmed = window.confirm(`Remove ${member.name} from the team?`);
    if (!confirmed) return;

    try {
      await removeTeamMember(team.id, member.userId, userData);
      setMembers(prev => prev.filter(m => m.userId !== member.userId));
      alert('Member removed successfully');
      onUpdated();
    } catch (err) {
      alert('Failed to remove: ' + err.message);
    }
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
      <DialogTitle sx={{ color: '#FF5722', fontWeight: 'bold' }}>Edit Team</DialogTitle>
      <DialogContent>
        {error && <Alert severity="error" onClose={() => setError('')} sx={{ mb: 1 }}>{error}</Alert>}
        {success && <Alert severity="success" onClose={() => setSuccess('')} sx={{ mb: 1 }}>{success}</Alert>}

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

        {isCreator && members.length > 0 && (
          <Box mt={3}>
            <strong>Team Members</strong>
            <List dense>
              {members.map(member => {
                const canRemove = member.userId !== userData.userId;
                return (
                  <ListItem key={member.userId} divider>
                    <ListItemText
                      primary={member.name}
                      secondary={member.email || member.description}
                    />
                    {canRemove && (
                      <Button
                        size="small"
                        color="error"
                        onClick={() => handleRemove(member)}
                      >
                        Remove
                      </Button>
                    )}
                  </ListItem>
                );
              })}
            </List>
          </Box>
        )}
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
          onClick={handleUpdate}
          sx={{
            backgroundColor: '#FF7F50',
            color: '#fff',
            '&:hover': { backgroundColor: '#FF5722' }
          }}
        >
          Update
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default EditTeamDialog;
