/**
 * TeamPage.js
 * 
 * Main page for participants to explore, create, join, and manage teams.
 * Combines ParticipantTeam view and MyTeamsDialog popups.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


// src/Participant/TeamPage.js
import React, { useEffect, useState } from 'react';
import TopBar from '../TopSide/TopBar';
import Sidebar from '../TopSide/Sidebar';
import ParticipantTeam from './ParticipantTeam';
import MyTeamsDialog from './MyTeamsDialog';
import '../project/Project.css'; // reuse existing CSS
import { Typography, Snackbar, Alert } from '@mui/material';
import GroupsIcon from '@mui/icons-material/Groups';

function TeamPage() {
  const [userData, setUserData] = useState(null);
  const [viewMode] = useState('explore'); // explore / my
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');
  const [success, setSuccess] = useState(true);
  const [myDialogOpen, setMyDialogOpen] = useState(false);
  const [myTeams, setMyTeams] = useState([]);

  // Fetch user information (same as in Project)
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
        console.log('[Debug] User data:', data);
        setUserData(data);
      } catch (error) {
        console.error('[Debug] Failed to fetch user data:', error);
      }
    })();
  }, []);

  return (
    <>
      <TopBar />
      <div className="participant-project-container">
        <Sidebar />
        <div className="participant-project-content">

          {/* Header changed to orange style */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            marginBottom: 20,
            padding: '10px 20px',
            backgroundColor: '#FFE3D3', // orange background
            borderRadius: 12,
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
          }}>
            <GroupsIcon sx={{ color: '#FF5722', fontSize: 36, mr: 2 }} />
            <Typography variant="h4" component="h2"
              sx={{
                fontWeight: 'bold',
                color: '#FF5722', // orange text
                textShadow: '1px 1px 2px rgba(0,0,0,0.2)'
              }}>
              Team Management
            </Typography>
          </div>

          {/* Main view content */}
          {userData && viewMode === 'explore' && (
            <ParticipantTeam
              userData={userData}
              onSnackbar={(msg, ok = true) => {
                setSnackbarMessage(msg);
                setSuccess(ok);
                setSnackbarOpen(true);
              }}
              onOpenMyTeams={(list) => {
                setMyTeams(list);
                setMyDialogOpen(true);
              }}
            />
          )}

          {/* My Teams Dialog */}
          <MyTeamsDialog
            open={myDialogOpen}
            onClose={() => setMyDialogOpen(false)}
            myTeams={myTeams}
            userData={userData}
            onUpdate={() => window.location.reload()}
          />

          {/* Global notification */}
          <Snackbar
            open={snackbarOpen}
            autoHideDuration={3000}
            onClose={() => setSnackbarOpen(false)}
            anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
          >
            <Alert
              onClose={() => setSnackbarOpen(false)}
              severity={success ? 'success' : 'error'}
              sx={{ width: '100%' }}
            >
              {snackbarMessage}
            </Alert>
          </Snackbar>
        </div>
      </div>
    </>
  );
}

export default TeamPage;
