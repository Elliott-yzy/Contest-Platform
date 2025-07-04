/**
 * TeamProjectDetail.js
 * 
 * View and manage team submissions.
 * Allows the team creator to edit or delete the team's submitted work.
 * 
 * Role: Participant (Team Leader)
 * Developer: Beiqi Dai
 */


// Enhanced TeamProjectDetail.js with edit/delete only visible to creator
import React, { useState, useEffect } from 'react';
import {
  Typography, Button, CircularProgress, Alert, Paper, Snackbar, TextField
} from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import TopBar from '../TopSide/TopBar';
import Sidebar from '../TopSide/Sidebar';
import '../project/Projectdetail.css';

function TeamProjectDetail() {
  const { competitionId, teamId } = useParams();
  const navigate = useNavigate();
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreator, setIsCreator] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [updatedTitle, setUpdatedTitle] = useState('');
  const [updatedDescription, setUpdatedDescription] = useState('');
  const [updatedFile, setUpdatedFile] = useState(null);
  const [snackbarMessage, setSnackbarMessage] = useState('');

  const userId = localStorage.getItem('userId');
  const userRole = localStorage.getItem('role');
  const token = localStorage.getItem('token');

  useEffect(() => {
    const fetchSubmission = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/submissions/public/teams/${competitionId}/${teamId}`,
          {
            method: 'GET',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
          }
        );
        if (!response.ok) {
          if (response.status === 404) setError('Submission not found.');
          else throw new Error(await response.text());
        } else {
          const data = await response.json();
          setSubmission(data);
          setUpdatedTitle(data.title || '');
          setUpdatedDescription(data.description || '');
        }
      } catch (err) {
        setError(err.message || 'Failed to load submission.');
      } finally {
        setLoading(false);
      }
    };

    const checkIfCreator = async () => {
      try {
        const url = new URL('http://localhost:8080/teams/public/created');
        url.searchParams.set('userId', userId);
        url.searchParams.set('page', 1);
        url.searchParams.set('size', 100);
        const res = await fetch(url.toString(), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
        const data = await res.json();
        const is = data.data?.some(team => team.id === teamId);
        setIsCreator(is);
      } catch (err) {
        console.error('Failed to verify creator:', err);
      }
    };

    fetchSubmission();
    checkIfCreator();
  }, [competitionId, teamId, userId]);

  const handleEdit = () => setEditMode(true);
  const handleCancelEdit = () => setEditMode(false);
  const handleFileChange = (e) => setUpdatedFile(e.target.files[0]);

  const handleSaveEdit = async () => {
    try {
      const formData = new FormData();
      formData.append('competitionId', competitionId);
      formData.append('teamId', teamId);
      formData.append('title', updatedTitle);
      formData.append('description', updatedDescription);
      if (updatedFile) formData.append('file', updatedFile);

      const res = await fetch('http://localhost:8080/submissions/teams/upload', {
        method: 'POST',
        headers: {
          'User-ID': userId,
          'User-Role': userRole,
          Authorization: `Bearer ${token}`
        },
        body: formData
      });

      if (!res.ok) throw new Error('Update failed');
      setSnackbarMessage('Submission updated successfully!');
      setEditMode(false);
    } catch (err) {
      setSnackbarMessage(err.message);
    }
  };

  const handleDelete = async () => {
    if (!submission?.submissionId || !window.confirm('Confirm delete submission?')) return;
    try {
      const res = await fetch(`http://localhost:8080/submissions/teams/${submission.submissionId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'User-ID': userId,
          'User-Role': userRole,
          Authorization: `Bearer ${token}`
        }
      });
      if (!res.ok) throw new Error('Delete failed');
      setSnackbarMessage('Submission deleted.');
      setTimeout(() => navigate(-1), 1500);
    } catch (err) {
      setSnackbarMessage(err.message);
    }
  };

  if (loading) return <CircularProgress />;
  if (error) return <Typography color="error">{error}</Typography>;

  return (
    <>
      <TopBar />
      <div className="participant-project-container">
        <Sidebar />
        <div className="participant-project-content">
          <div className="back-button" onClick={() => navigate(-1)}>
            <ArrowBackIcon className="back-icon" sx={{ color: '#FF9800' }} />
            <Typography variant="button" className="back-text" sx={{ color: '#FF9800' }}>
              Go Back
            </Typography>
          </div>

          <Paper className="detail-paper">
            <Typography variant="h4" gutterBottom sx={{ color: '#FF9800' }}>
              Team Submission Details
            </Typography>

            {editMode ? (
              <>
                <TextField fullWidth label="Title" margin="normal" value={updatedTitle} onChange={(e) => setUpdatedTitle(e.target.value)} />
                <TextField fullWidth label="Description" margin="normal" multiline minRows={3} value={updatedDescription} onChange={(e) => setUpdatedDescription(e.target.value)} />
                <input type="file" onChange={handleFileChange} style={{ margin: '10px 0' }} />
                <div style={{ marginTop: 20 }}>
                  <Button variant="contained" onClick={handleSaveEdit} sx={{ backgroundColor: '#FF9800', mr: 2 }}>Save</Button>
                  <Button variant="outlined" onClick={handleCancelEdit} sx={{ color: '#FF9800' }}>Cancel</Button>
                </div>
              </>
            ) : (
              <>
                <Typography variant="h6">Title: {submission.title}</Typography>
                <Typography variant="body1">Description: {submission.description}</Typography>
                <Typography variant="body2">File Name: {submission.fileName}</Typography>
                {submission.fileUrl && (
                  <Button variant="outlined" href={submission.fileUrl} target="_blank" sx={{ borderColor: '#FF9800', color: '#FF9800', mt: 2 }}>
                    View File
                  </Button>
                )}
                <Typography variant="body2">File Type: {submission.fileType}</Typography>
                <Typography variant="body2">Review Status: {submission.reviewStatus}</Typography>
                <Typography variant="body2">Review Comments: {submission.reviewComments}</Typography>
                <Typography variant="body2">Reviewed By: {submission.reviewedBy}</Typography>
                <Typography variant="body2">Reviewed At: {submission.reviewedAt ? new Date(submission.reviewedAt).toLocaleString() : 'N/A'}</Typography>
                <Typography variant="body2">Total Score: {submission.totalScore}</Typography>
                <Typography variant="body2">Submitted At: {submission.createdAt ? new Date(submission.createdAt).toLocaleString() : 'Unknown'}</Typography>
                {isCreator && (
                  <div style={{ marginTop: 20 }}>
                    <Button variant="contained" onClick={handleEdit} sx={{ backgroundColor: '#FF9800', mr: 2 }}>Edit</Button>
                    <Button variant="outlined" onClick={handleDelete} sx={{ color: '#FF9800' }}>Delete</Button>
                  </div>
                )}
              </>
            )}
          </Paper>
        </div>
      </div>

      <Snackbar
        open={!!snackbarMessage}
        autoHideDuration={3000}
        onClose={() => setSnackbarMessage('')}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert severity="success" sx={{ width: '100%', backgroundColor: '#FF9800', color: '#fff' }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </>
  );
}

export default TeamProjectDetail;
