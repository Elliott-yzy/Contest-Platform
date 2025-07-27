/**
 * @file MyTeamsDialog.js
 * @description 
 * This component displays a dialog listing the user's teams, allowing the creator to edit or delete teams.
 * 
 * Features include:
 *  - Listing all teams the user has joined or created.
 *  - Identifying whether the user is the creator of each team.
 *  - Providing Edit and Delete options for teams created by the user.
 *  - Displaying loading indicators and error alerts during data fetching.
 * 
 * Subcomponents:
 *  - EditTeamDialog: Opens when the user chooses to edit a team.
 * 
 * External APIs used:
 *  - getTeamCreator: Fetches the creator information of a team.
 *  - getTeamDetail: Fetches detailed team information.
 * 
 * Styling:
 *  - Dialog and buttons are styled with an orange theme for a consistent user experience.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
  Typography,
  Box,
  CircularProgress,
  Alert
} from '@mui/material';
import EditTeamDialog from './EditTeamDialog';
import { getTeamCreator, getTeamDetail } from './teamApi';

function MyTeamsDialog({ open, myTeams, onClose, onDelete, userData, onUpdate }) {
  const [editOpen, setEditOpen] = useState(false);
  const [teamToEdit, setTeamToEdit] = useState(null);
  const [creatorMap, setCreatorMap] = useState({}); // { teamId: creatorUserId }
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!open || myTeams.length === 0) return;

    const fetchCreators = async () => {
      setLoading(true);
      setError('');
      const newMap = {};

      for (const team of myTeams) {
        const teamId = team.id;
        try {
          const creator = await getTeamCreator(teamId, userData);
          newMap[teamId] = creator.id;
          console.log(`✅ Creator of team ${team.name}:`, creator.name, `(id: ${creator.id})`);
        } catch (err) {
          console.error(`❌ Failed to fetch creator of team ${team.name}`, err.message);
          newMap[teamId] = null;
        }
      }

      setCreatorMap(newMap);
      setLoading(false);
    };

    fetchCreators();
  }, [open, myTeams, userData]);

  const handleOpenEdit = async (team) => {
    try {
      const fullTeam = await getTeamDetail(team.id);
      setTeamToEdit(fullTeam);
      setEditOpen(true);
    } catch (err) {
      alert('Failed to load team info: ' + err.message);
    }
  };

  return (
    <>
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
        <DialogTitle sx={{ fontWeight: 'bold', color: '#FF5722' }}>My Teams</DialogTitle>
        <DialogContent dividers>
          {loading ? (
            <Box display="flex" justifyContent="center" py={4}>
              <CircularProgress />
            </Box>
          ) : error ? (
            <Alert severity="error">{error}</Alert>
          ) : myTeams.length > 0 ? (
            <List dense>
              {myTeams.map(team => {
                const creatorId = creatorMap[team.id];
                const isCreator = creatorId === userData.userId;

                console.log(`🔍 [${team.name}] created by ${creatorId}, current user = ${userData.userId}, isCreator = ${isCreator}`);

                return (
                  <ListItem
                    key={team.id}
                    divider
                    secondaryAction={
                      <Box display="flex" gap={1}>
                        {isCreator && (
                          <>
                            <Button
                              size="small"
                              variant="contained"
                              onClick={() => handleOpenEdit(team)}
                              sx={{
                                backgroundColor: '#FF7F50',
                                color: '#fff',
                                '&:hover': { backgroundColor: '#FF5722' }
                              }}
                            >
                              Edit
                            </Button>
                            <Button
                              size="small"
                              color="error"
                              onClick={() => onDelete(team.id)}
                            >
                              Delete
                            </Button>
                          </>
                        )}
                      </Box>
                    }
                  >
                    <ListItemText
                      primary={team.name}
                      secondary={team.description || 'No description'}
                    />
                  </ListItem>
                );
              })}
            </List>
          ) : (
            <Typography variant="body2">You have not joined any teams.</Typography>
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
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {teamToEdit && (
        <EditTeamDialog
          open={editOpen}
          onClose={() => setEditOpen(false)}
          team={teamToEdit}
          userData={userData}
          onUpdated={() => {
            setEditOpen(false);
            onUpdate();
          }}
        />
      )}
    </>
  );
}

export default MyTeamsDialog;
