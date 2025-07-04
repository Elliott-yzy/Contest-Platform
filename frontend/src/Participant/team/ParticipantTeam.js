/**
 * @file ParticipantTeam.js
 * @description 
 * This component renders the participant's team management area. 
 * 
 * Main Features:
 *  - Displays public teams and allows browsing, joining, and leaving teams.
 *  - Allows users to create new teams.
 *  - Provides a "My Teams" view where users can manage (edit/delete) their own teams.
 *  - Integrates Snackbar notifications for user feedback.
 * 
 * Key Components Used:
 *  - TeamList: Displays the list of public teams with join/leave actions.
 *  - TeamCreateDialog: Dialog for creating a new team.
 *  - MyTeamsDialog: Dialog for viewing and editing the user's own teams.
 * 
 * API Functions:
 *  - fetchJoinedTeams, fetchTeams, fetchMyTeams: For retrieving team information.
 *  - createTeam, joinTeam, leaveTeam, deleteTeam: For team actions.
 * 
 * Styling:
 *  - Uses a soft orange (#FF7F50) and deep orange (#FF5722) theme for buttons and highlights.
 * 
 * Developer: Beiqi Dai
 * Role: Participant
 */


import React, { useEffect, useState } from 'react';
import { Card, CardContent, Box, Typography, Button, Snackbar, Alert } from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';
import TeamCreateDialog from './TeamCreateDialog';
import MyTeamsDialog from './MyTeamsDialog';
import TeamList from './TeamList';
import {
  fetchJoinedTeams,
  fetchTeams,
  fetchMyTeams,
  createTeam,
  joinTeam,
  leaveTeam,
  deleteTeam
} from './teamApi';

function ParticipantTeam() {
  const [userData, setUserData] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [isSuccess, setIsSuccess] = useState(false);

  const [teams, setTeams] = useState([]);
  const [joinedTeams, setJoinedTeams] = useState(new Set());
  const [myTeams, setMyTeams] = useState([]);
  const [page, setPage] = useState(1);
  const [pages, setPages] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [order, setOrder] = useState('asc');

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch('http://localhost:8080/users/profile', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
            'User-ID': localStorage.getItem('userId')
          }
        });
        const data = await res.json();
        data.userId = localStorage.getItem('userId');
        console.log('[Debug] User data:', data);
        setUserData(data);
      } catch (error) {
        console.error('[Debug] Failed to fetch user data:', error);
      }
    })();
  }, []);

  useEffect(() => {
    if (userData?.userId) {
      console.log('[Debug] useEffect triggered, calling fetchTeams...');
      fetchJoinedTeams(userData, setJoinedTeams);
      fetchTeams({ page, keyword, sortBy, order }, setTeams, setPages);
    }
  }, [userData, page, keyword, sortBy, order]);

  if (!userData) return null;
  console.log('[Render] Current team list length:', teams.length);

  const handleUpdateMyTeams = () => {
    fetchMyTeams(userData, setMyTeams);
    fetchTeams({ page, keyword, sortBy, order }, setTeams, setPages);
  };

  return (
    <Card
      className="participant-team-sidebar participant-team-sticky"
      sx={{
        minWidth: '880px',
        maxWidth: '640px',
        margin: '0 auto',
        padding: 2,
        backgroundColor: '#fff',
        borderRadius: 2,
        boxShadow: 3
      }}
    >
      <CardContent>
        <Box display="flex" alignItems="center" mb={1}>
          <GroupsIcon sx={{ mr: 1 }} />
          <Typography variant="h6" fontWeight="bold">Teams</Typography>
        </Box>

        <Box mb={2} display="flex" gap={1}>
          <Button
            variant="contained"
            onClick={() => setDialogOpen(true)}
            sx={{
              backgroundColor: '#FF7F50',
              color: '#fff',
              '&:hover': {
                backgroundColor: '#FF5722'
              }
            }}
          >
            Create Team
          </Button>
          <Button
            variant="contained"
            onClick={() => {
              setViewDialogOpen(true);
              fetchMyTeams(userData, setMyTeams);
            }}
            sx={{
              backgroundColor: '#FF7F50',
              color: '#fff',
              '&:hover': {
                backgroundColor: '#FF5722'
              }
            }}
          >
            View My Teams
          </Button>
        </Box>

        {/* 👉 Adding a regional description list is for the public team */}
        <Box
          display="flex"
          alignItems="center"
          gap={1}
          mb={2}
          p={2}
        >
          <Typography variant="body1" fontWeight="medium" color="#FF5722">
            Browse and join public teams below 👇
          </Typography>
        </Box>

        <TeamList
          teams={teams}
          joinedTeams={joinedTeams}
          page={page}
          pages={pages}
          keyword={keyword}
          sortBy={sortBy}
          order={order}
          setPage={setPage}
          setKeyword={setKeyword}
          setSortBy={setSortBy}
          setOrder={setOrder}
          onJoin={(team) =>
            joinTeam(team, userData, setJoinedTeams, setSnackbarMessage, setIsSuccess, setSnackbarOpen)
          }
          onLeave={(id) =>
            leaveTeam(id, userData, setJoinedTeams, setSnackbarMessage, setIsSuccess, setSnackbarOpen)
          }
        />

        <TeamCreateDialog
          open={dialogOpen}
          onClose={() => setDialogOpen(false)}
          onCreate={(name, desc) =>
            createTeam(name, desc, userData, {
              onSuccess: () => {
                setSnackbarMessage('Team created!');
                setIsSuccess(true);
                setSnackbarOpen(true);
                setDialogOpen(false);
                fetchJoinedTeams(userData, setJoinedTeams);
                fetchTeams({ page: 1, keyword, sortBy, order }, setTeams, setPages);
              },
              onError: (msg) => {
                setSnackbarMessage(msg);
                setIsSuccess(false);
                setSnackbarOpen(true);
              }
            })
          }
        />

        <MyTeamsDialog
          open={viewDialogOpen}
          myTeams={myTeams}
          userData={userData}
          onClose={() => setViewDialogOpen(false)}
          onUpdate={handleUpdateMyTeams}
          onDelete={(id) =>
            deleteTeam(id, userData, {
              onSuccess: () => {
                setSnackbarMessage('Team deleted');
                setIsSuccess(true);
                setSnackbarOpen(true);
                setMyTeams((prev) => prev.filter((t) => t.id !== id));
                setJoinedTeams((prev) => {
                  const copy = new Set(prev);
                  copy.delete(id);
                  return copy;
                });
                fetchTeams({ page, keyword, sortBy, order }, setTeams, setPages);
              },
              onError: (msg) => {
                setSnackbarMessage(msg);
                setIsSuccess(false);
                setSnackbarOpen(true);
              }
            })
          }
        />

        <Snackbar
          open={snackbarOpen}
          autoHideDuration={3000}
          onClose={() => setSnackbarOpen(false)}
          anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        >
          <Alert
            onClose={() => setSnackbarOpen(false)}
            severity={isSuccess ? 'success' : 'error'}
            sx={{ width: '100%' }}
          >
            {snackbarMessage}
          </Alert>
        </Snackbar>
      </CardContent>
    </Card>
  );
}

export default ParticipantTeam;
