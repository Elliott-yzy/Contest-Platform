/**
 * @file ProjectDetail.js
 * @description 
 * This component provides participants with the ability to view, edit, and delete their competition submissions.
 * Key functionalities include:
 *  - Fetching and displaying submission details for a specific competition.
 *  - Allowing participants to update the title, description, and file associated with a submission.
 *  - Deleting an existing submission after confirmation.
 *  - Displaying operation feedback through Snackbar notifications.
 *  - Handling both loading and error states during data fetching.
 *  - Seamlessly integrating with backend APIs for submission management.
 * 
 * Role: Participant
 * Developer: Zhaoyi Yang, Beiqi Dai
 */


import React, { useState, useEffect } from 'react';
import {
  Typography,
  Button,
  CircularProgress,
  TextField,
  Snackbar,
  Alert,
  Paper
} from '@mui/material';
// Use useParams and useNavigate to get route params and navigation methods
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
// Import top navigation bar and sidebar components
import TopBar from '../TopSide/TopBar';
import Sidebar from '../TopSide/Sidebar';
// Import page styles
import './Projectdetail.css';

function ProjectDetail() {
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userData, setUserData] = useState(null);

  // States related to edit mode
  const [editMode, setEditMode] = useState(false);
  const [updatedTitle, setUpdatedTitle] = useState("");
  const [updatedDescription, setUpdatedDescription] = useState("");
  const [updatedFile, setUpdatedFile] = useState(null);

  // Snackbar state
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");

  const location = useLocation();
  const competitionStatus = location.state?.competitionStatus || 'UNKNOWN';
  const isEditable = competitionStatus === 'ONGOING';

  // Get competitionId parameter from route using useParams
  const { competitionId } = useParams();
  const navigate = useNavigate();

  // Fetch user information
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
        setUserData(data);
      } catch (err) {
        console.error("获取用户信息失败:", err);
      }
    };
    fetchUserData();
  }, []);

  // After userData is loaded and competitionId exists, fetch submission details by competitionId
  useEffect(() => {
    if (!userData || !competitionId) return;

    const fetchSubmissionDetail = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await fetch(`http://localhost:8080/submissions/${competitionId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'User-ID': userData.userId,
            'User-Role': userData.role || '',
            Authorization: `Bearer ${token}`
          }
        });
        if (!response.ok) {
          const errText = await response.text();
          throw new Error(`Failed to fetch submission details: ${errText}`);
        }
        const data = await response.json();
        setSubmission(data);
        setLoading(false);
      } catch (err) {
        setError(err.message || "Error fetching submission details");
        setLoading(false);
      }
    };
    fetchSubmissionDetail();
  }, [userData, competitionId]);

  const handleDeleteSubmission = async () => {
    if (!submission || !submission.id) {
      alert("No submission to delete.");
      return;
    }
    if (!window.confirm("Are you sure you want to delete this submission?")) {
      return;
    }
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/submissions/${submission.id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'User-ID': userData.userId,
          'User-Role': userData.role || '',
          Authorization: `Bearer ${token}`
        }
      });
      if (!response.ok) {
        const errText = await response.text();
        throw new Error(errText);
      }
      setSnackbarMessage("Submission deleted successfully!");
      setSnackbarOpen(true);
      // After deletion, navigate back to the list page (assuming the list page route is /project)
      setTimeout(() => navigate(-1), 1500);
    } catch (err) {
      setSnackbarMessage("Failed to delete submission");
      setSnackbarOpen(true);
    }
  };

  const handleEditSubmission = () => {
    setEditMode(true);
    setUpdatedTitle(submission.title || "");
    setUpdatedDescription(submission.description || "");
  };

  const handleFileChange = (e) => {
    setUpdatedFile(e.target.files[0]);
  };

  const handleCancelEdit = () => {
    setEditMode(false);
  };

  // Submit edits
  const handleSaveEdit = async () => {
    if (!updatedFile) {
      alert("Please select a file to upload!");
      return;
    }
    try {
      const token = localStorage.getItem('token');
      const userId = userData.userId;
      const userRole = userData.role || '';

      const formData = new FormData();
      formData.append('competitionId', competitionId);
      formData.append('title', updatedTitle);
      formData.append('description', updatedDescription);
      formData.append('file', updatedFile);

      const response = await fetch('http://localhost:8080/submissions/upload', {
        method: 'POST',
        headers: {
          'User-ID': userId,
          'User-Role': userRole,
          Authorization: `Bearer ${token}`
        },
        body: formData
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
      }
      const updatedSubmission = await response.json();
      setSubmission(updatedSubmission);
      setSnackbarMessage("Submission updated successfully!");
      setSnackbarOpen(true);
      setEditMode(false);
    } catch (err) {
      alert("Update failed: " + err.message);
    }
  };

  const handleSnackbarClose = (event, reason) => {
    if (reason === 'clickaway') return;
    setSnackbarOpen(false);
  };

  if (loading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Typography color="error">{error}</Typography>;
  }

  return (
    <>
      <TopBar />
      <div className="participant-project-container">
        <Sidebar />
        <div className="participant-project-content">
          {/* Back button fixed at top-left of content area */}
          <div className="back-button" onClick={() => navigate(-1)}>
            <ArrowBackIcon className="back-icon" sx={{ color: '#FF9800' }} />
            <Typography variant="button" className="back-text" sx={{ color: '#FF9800' }}>
              Go Back
            </Typography>
          </div>
          <Paper className="detail-paper">
            <Typography variant="h4" gutterBottom sx={{ color: '#FF9800' }}>
              Submission Details
            </Typography>
            {editMode ? (
              <>
                <TextField
                  label="Title"
                  fullWidth
                  margin="normal"
                  value={updatedTitle}
                  onChange={(e) => setUpdatedTitle(e.target.value)}
                  InputLabelProps={{ style: { color: '#FF9800' } }}
                  sx={{ '& .MuiInputBase-root': { color: '#FF9800' } }}
                />
                <TextField
                  label="Description"
                  fullWidth
                  margin="normal"
                  multiline
                  minRows={3}
                  value={updatedDescription}
                  onChange={(e) => setUpdatedDescription(e.target.value)}
                  InputLabelProps={{ style: { color: '#FF9800' } }}
                  sx={{ '& .MuiInputBase-root': { color: '#FF9800' } }}
                />
                <input type="file" onChange={handleFileChange} style={{ margin: '10px 0' }} />
                <div style={{ marginTop: '20px' }}>
                  <Button
                    variant="contained"
                    onClick={handleSaveEdit}
                    sx={{
                      backgroundColor: '#FF9800',
                      ':hover': { backgroundColor: '#FB8C00' },
                      mr: 2
                    }}
                  >
                    Save
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={handleCancelEdit}
                    sx={{
                      borderColor: '#FF9800',
                      color: '#FF9800',
                      ':hover': { backgroundColor: '#FFE0B2' }
                    }}
                  >
                    Cancel
                  </Button>
                </div>
              </>
            ) : (
              <>
                <Typography variant="h6" >
                  Title: {submission.title || "No Title"}
                </Typography>
                <Typography variant="body1" >
                  Description: {submission.description || "No description available"}
                </Typography>
                <Typography variant="body2" >
                  File Name: {submission.fileName || "No file"}
                </Typography>
                {submission.fileUrl && (
                  <Button
                    variant="outlined"
                    href={submission.fileUrl}
                    target="_blank"
                    sx={{
                      borderColor: '#FF9800',
                      color: '#FF9800',
                      ':hover': { backgroundColor: '#FFE0B2' },
                      mt: 2
                    }}
                  >
                    View File
                  </Button>
                )}
                <Typography variant="body2" >
                  Review Status: {submission.reviewStatus || "Pending"}
                </Typography>
                <Typography variant="body2" >
                  Score: {submission.totalScore ?? "Not scored yet"}
                </Typography>
                <Typography variant="body2" >
                  Submission Time:{" "}
                  {submission.createdAt
                    ? new Date(submission.createdAt).toLocaleString()
                    : "Unknown"}
                </Typography>
                <div style={{ marginTop: '20px' }}>
                  <Button
                    variant="contained"
                    onClick={handleEditSubmission}
                    disabled={!isEditable}
                    sx={{
                      backgroundColor: isEditable ? '#FF9800' : '#E0E0E0',
                      color: isEditable ? '#fff' : '#9E9E9E',
                      ':hover': {
                        backgroundColor: isEditable ? '#FB8C00' : '#E0E0E0'
                      },
                      mr: 2
                    }}
                  >
                    Edit Submission
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={handleDeleteSubmission}
                    disabled={!isEditable}
                    sx={{
                      borderColor: isEditable ? '#FF9800' : '#E0E0E0',
                      color: isEditable ? '#FF9800' : '#9E9E9E',
                      ':hover': {
                        backgroundColor: isEditable ? '#FFE0B2' : 'inherit'
                      }
                    }}
                  >
                    Delete Submission
                  </Button>
                </div>
              </>
            )}
            <Snackbar
              open={snackbarOpen}
              autoHideDuration={3000}
              onClose={handleSnackbarClose}
              anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            >
              <Alert
                onClose={handleSnackbarClose}
                severity="success"
                sx={{ width: '100%', backgroundColor: '#FF9800', color: '#fff' }}
              >
                {snackbarMessage}
              </Alert>
            </Snackbar>
          </Paper>
        </div>
      </div>
    </>
  );
}

export default ProjectDetail;
