/**
 * @file Project.js
 * @description 
 * This component allows participants to manage their contest participation and submissions.
 * Key functionalities include:
 *  - Viewing a list of joined contests in both individual and team modes.
 *  - Submitting a project file to an ongoing contest.
 *  - Validating allowed file types before submission based on contest settings.
 *  - Viewing existing submissions and their review status.
 *  - Switching between individual and team registration views.
 *  - Providing real-time feedback through Snackbar notifications.
 *  - Integrating with backend APIs to fetch registration, contest, and submission data.
 * 
 * Role: Participant
 * Developer: Zhaoyi Yang, Beiqi Dai
 */


import React, { useState, useEffect, useCallback } from 'react';
import TopBar from '../TopSide/TopBar';
import Sidebar from '../TopSide/Sidebar';
import {
  Typography,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Button,
  Pagination,
  Snackbar,
  Alert
} from '@mui/material';
import { SubmitDialog } from './Submitbottom';
import TeamRegistrations from '../team/TeamRegistrations';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import './Project.css';
import { useNavigate } from 'react-router-dom';

function Project() {
  const [userData, setUserData] = useState(null);
  const [registrationData, setRegistrationData] = useState([]);
  const [pagination, setPagination] = useState({ total: 0, page: 1, size: 10, pages: 0 });
  const [allowedTypes, setAllowedTypes] = useState([]);
  const [viewMode, setViewMode] = useState('personal'); // 'personal' or 'team'

  // Submit dialog state
  const [openSubmitDialog, setOpenSubmitDialog] = useState(false);
  const [selectedCompetitionId, setSelectedCompetitionId] = useState(null);

  // Snackbar
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState('');

  const navigate = useNavigate();

  // 1️⃣ Fetch user information
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

  /**
   * 2️⃣ Fetch personal registrations (wrapped in useCallback so the reference is stable)
   */
  const fetchRegistrations = useCallback(
    async (currentPage = 1) => {
      try {
        const url = new URL('http://localhost:8080/registrations/my');
        url.searchParams.append('page', currentPage);
        url.searchParams.append('size', pagination.size);

        const response = await fetch(url.toString(), {
          method: 'GET',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'User-ID': userData?.userId ?? '',
            'User-Role': userData?.role ?? '',
            Authorization: `Bearer ${localStorage.getItem('token')}`
          }
        });
        const data = await response.json();
        console.log('[Debug] Registration data:', data);
        setRegistrationData(Array.isArray(data.data) ? data.data : []);
        setPagination(prev => ({
          ...prev,
          total: data.total ?? 0,
          page: data.page ?? 1,
          pages: data.pages ?? 0
        }));
      } catch (error) {
        console.error('[Debug] Failed to fetch registration info:', error);
        setRegistrationData([]);
      }
    },
    [pagination.size, userData]
  );

  /**
   * 3️⃣ Fetch data whenever userData / page / mode changes
   */
  useEffect(() => {
    if (!userData) return;
    if (viewMode === 'personal') {
      fetchRegistrations(pagination.page);
    }
    // Team mode data is fetched inside TeamRegistrations component
  }, [userData, pagination.page, viewMode, fetchRegistrations]);

  // 4️⃣ Populate submission details for personal entries
  useEffect(() => {
    if (viewMode !== 'personal' || !userData || registrationData.length === 0) return;
    registrationData
      .filter(item => item.hasSubmitted && !item.fileName)
      .forEach(async item => {
        try {
          const res = await fetch(`http://localhost:8080/submissions/${item.competitionId}`, {
            method: 'GET',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
              'User-ID': userData.userId,
              'User-Role': userData.role || '',
              Authorization: `Bearer ${localStorage.getItem('token')}`
            }
          });
          if (!res.ok) {
            console.error('[Debug] Failed to fetch submission details:', res.status);
            return;
          }
          const submissionData = await res.json();
          console.log('[Debug] Submission details for', item.competitionId, submissionData);
          setRegistrationData(prev =>
            prev.map(reg =>
              reg.competitionId === item.competitionId
                ? { ...reg, fileName: submissionData.fileName, reviewStatus: submissionData.reviewStatus }
                : reg
            )
          );
        } catch (err) {
          console.error('[Debug] Error fetching submission details:', err);
        }
      });
  }, [registrationData, userData, viewMode]);

  // Open/close the submit dialog
  const handleOpenSubmitDialog = competitionId => {
    console.log('[Debug] Open submit dialog:', competitionId);
    setSelectedCompetitionId(competitionId);
    setOpenSubmitDialog(true);
  };
  const handleCloseSubmitDialog = () => {
    setOpenSubmitDialog(false);
    setSelectedCompetitionId(null);
  };

  // 5️⃣ Submission or edit logic (unchanged)
  const handleDialogSubmit = async ({ title, description, file }) => {
    if (!file) {
      setSnackbarMessage('Please select a file to upload!');
      setSnackbarOpen(true);
      handleCloseSubmitDialog();
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const userId = userData.userId;
      const userRole = userData.role || '';

      // Fetch competition details
      const detailResponse = await fetch(`http://localhost:8080/competitions/${selectedCompetitionId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          Authorization: `Bearer ${token}`
        }
      });
      if (!detailResponse.ok) {
        const errText = await detailResponse.text();
        console.error('[Debug] Failed to fetch competition details:', detailResponse.status, errText);
        throw new Error('Failed to get competition details');
      }
      const competitionDetail = await detailResponse.json();
      const allowedSubmissionTypes = competitionDetail.allowedSubmissionTypes || [];
      console.log('[Debug] Allowed types:', allowedSubmissionTypes);
      setAllowedTypes(allowedSubmissionTypes);

      // Validate file type
      const fileExtension = file.name.split('.').pop().toLowerCase();
      const allowedLower = allowedSubmissionTypes.map(t => t.toLowerCase());
      const extensionMap = {
        image: ['jpg', 'jpeg', 'png'],
        code: ['py', 'js', 'ts', 'java', 'cpp', 'c', 'cs', 'rb', 'go', 'rs', 'swift', 'kt'],
        text: ['txt', 'md', 'rtf', 'csv', 'log', 'doc', 'docx', 'pdf', 'odt']
      };
      const isMatched =
        allowedLower.includes(fileExtension) ||
        Object.entries(extensionMap).some(([key, list]) => allowedLower.includes(key) && list.includes(fileExtension));
      if (!isMatched) {
        const readable = allowedSubmissionTypes.flatMap(type => {
          const lower = type.toLowerCase();
          return extensionMap[lower] || [type];
        });
        setSnackbarMessage(`Invalid file type. Allowed: ${readable.join(', ')}`);
        setSnackbarOpen(true);
        handleCloseSubmitDialog();
        return;
      }

      // Build upload form
      const formData = new FormData();
      formData.append('competitionId', selectedCompetitionId);
      formData.append('title', title);
      formData.append('description', description);
      formData.append('file', file);

      // Upload
      const uploadResponse = await fetch('http://localhost:8080/submissions/upload', {
        method: 'POST',
        headers: {
          'User-ID': userId,
          'User-Role': userRole,
          Authorization: `Bearer ${token}`
        },
        body: formData
      });
      if (!uploadResponse.ok) {
        const errText = await uploadResponse.text();
        console.error('[Debug] Upload failed:', uploadResponse.status, errText);
        throw new Error('Failed to submit work');
      }
      console.log('[Debug] Upload successful!');
      // Locally update submission state
      setRegistrationData(prev =>
        prev.map(item =>
          item.competitionId === selectedCompetitionId
            ? { ...item, hasSubmitted: true, fileName: file.name }
            : item
        )
      );
    } catch (error) {
      console.error('[Debug] Submission failed:', error);
      setSnackbarMessage('Upload failed. Please try again.');
      setSnackbarOpen(true);
    } finally {
      handleCloseSubmitDialog();
    }
  };

  const handleViewSubmissionDetail = (competitionId, status) => {
    navigate(`/project-detail/${competitionId}`, {
      state: { competitionStatus: status }
    });
  };

  return (
    <>
      <TopBar />
      <div className="participant-project-container">
        <Sidebar />
        <div className="participant-project-content">
          {/* Header */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            marginBottom: 20,
            padding: '10px 20px',
            backgroundColor: '#FFF3E0',
            borderRadius: 12,
            boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
          }}>
            <EmojiEventsIcon sx={{ color: '#FF9800', fontSize: 36, mr: 2 }} />
            <Typography variant="h4" component="h2"
              sx={{ fontWeight: 'bold', color: '#FF9800', textShadow: '1px 1px 2px rgba(0,0,0,0.2)' }}>
              Joined Competitions
            </Typography>
          </div>

          {/* Toggle buttons */}
          <div style={{ marginBottom: 16, display: 'flex', gap: 8 }}>
            <Button
              variant={viewMode === 'personal' ? 'contained' : 'outlined'}
              onClick={() => setViewMode('personal')}
              sx={{
                backgroundColor: viewMode === 'personal' ? '#FF9800' : 'transparent',
                color: viewMode === 'personal' ? '#fff' : '#FF9800',
                borderColor: '#FF9800',
                '&:hover': {
                  backgroundColor: viewMode === 'personal'
                    ? '#FB8C00'
                    : 'rgba(255,152,0,0.1)',
                },
              }}
            >
              Individual
            </Button>
            <Button
              variant={viewMode === 'team' ? 'contained' : 'outlined'}
              onClick={() => setViewMode('team')}
              sx={{
                backgroundColor: viewMode === 'team' ? '#FF9800' : 'transparent',
                color: viewMode === 'team' ? '#fff' : '#FF9800',
                borderColor: '#FF9800',
                '&:hover': {
                  backgroundColor: viewMode === 'team'
                    ? '#FB8C00'
                    : 'rgba(255,152,0,0.1)',
                },
              }}
            >
              Team
            </Button>
          </div>

          {/* View */}
          {viewMode === 'personal' ? (
            <>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Contest Name</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell>Competition Status</TableCell>
                    <TableCell>Submission Name</TableCell>
                    <TableCell>Submission Status</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {registrationData.map((item, idx) => (
                    <TableRow key={`${item.competitionId}-${idx}`}>
                      <TableCell>{item.competitionName}</TableCell>
                      <TableCell>{item.category}</TableCell>
                      <TableCell>{item.status || 'Unknown'}</TableCell>
                      <TableCell>
                        {!item.hasSubmitted ? (
                          <Button
                            variant="outlined"
                            disabled={item.status !== 'ONGOING'}
                            onClick={() => handleOpenSubmitDialog(item.competitionId)}
                          >
                            Submit
                          </Button>
                        ) : (
                          <Button
                            variant="text"
                            sx={{ textDecoration: 'underline' }}
                            onClick={() => handleViewSubmissionDetail(item.competitionId, item.status)}
                          >
                            {item.fileName || 'No file'}
                          </Button>
                        )}
                      </TableCell>
                      <TableCell>
                        {item.hasSubmitted
                          ? (item.reviewStatus || 'PENDING').toUpperCase()
                          : 'Not Submitted'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {/* Pagination */}
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: 20 }}>
                <Pagination
                  count={pagination.pages}
                  page={pagination.page}
                  onChange={(e, v) => setPagination(prev => ({ ...prev, page: v }))}
                  sx={{
                    '& .MuiPaginationItem-root': { color: '#FF9800', borderColor: '#FF9800' },
                    '& .MuiPaginationItem-root.Mui-selected': {
                      backgroundColor: '#FF9800',
                      color: 'white',
                      borderColor: '#FF9800',
                      '&:hover': { backgroundColor: '#FB8C00' }
                    }
                  }}
                />
              </div>
            </>
          ) : (
            <TeamRegistrations userData={userData} />
          )}
        </div>
      </div>

      {/* Submit Dialog */}
      <SubmitDialog
        open={openSubmitDialog}
        onClose={handleCloseSubmitDialog}
        onSubmit={handleDialogSubmit}
        allowedTypes={allowedTypes}
      />

      {/* Snackbar */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={() => setSnackbarOpen(false)} severity="success" sx={{ width: '100%' }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </>
  );
}

export default Project;
