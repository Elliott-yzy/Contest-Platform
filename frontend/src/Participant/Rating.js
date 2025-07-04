/**
 * Rating.js
 * 
 * Displays a list of competitions assigned to the current judge, allowing them to view the competition details and review submissions.
 * Provides functionality to navigate to review submission pages and view competition-related information, such as category, start/end dates, scoring criteria, and allowed submission types.
 * 
 * Role: Judge
 * Developer: Zhaoyi Yang
 */


import React, { useState, useEffect } from 'react';
import TopBar from './TopSide/TopBar';
import Sidebar from './TopSide/Sidebar';
import './Rating.css';
import {
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TableContainer,
  Paper,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
  Chip,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';

function Rating() {
  const [competitions, setCompetitions] = useState([]);
  const [selectedComp, setSelectedComp] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const navigate = useNavigate();

  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    if (!token || !userId) return;

    fetch(`http://localhost:8080/judges/my-competitions?page=1&size=100`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        'User-ID': userId,
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error(`Fetch error: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        setCompetitions(data.data || []);
      })
      .catch((err) => {
        console.error('Error fetching competitions:', err);
      });
  }, [token, userId]);

  const fetchCompetitionDetail = async (id) => {
    try {
      const res = await fetch(`http://localhost:8080/competitions/${id}`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
          'User-ID': userId,
        },
      });
      const data = await res.json();
      if (res.ok) {
        setSelectedComp(data);
        setDialogOpen(true);
      } else {
        alert(data.message || 'Failed to fetch competition detail');
      }
    } catch (error) {
      console.error('Error fetching competition detail:', error);
    }
  };

  return (
    <>
      <TopBar />
      <div className="rating-container">
        <Sidebar />
        <div className="rating-content">
          <Typography variant="h5" gutterBottom>
            Competitions Assigned to You as Judge
          </Typography>

          <TableContainer component={Paper} sx={{ mt: 2 }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {competitions.map((comp, index) => (
                  <TableRow key={comp.id}>
                    <TableCell>{index + 1}</TableCell>
                    <TableCell>
                      <Button variant="text" onClick={() => fetchCompetitionDetail(comp.id)}>
                        {comp.name}
                      </Button>
                    </TableCell>
                    <TableCell>{comp.description}</TableCell>
                    <TableCell>{comp.status}</TableCell>
                    <TableCell>
                      <Button
                        variant="contained"
                        size="small"
                        color={comp.status === 'COMPLETED' ? 'primary' : 'inherit'}
                        disabled={comp.status !== 'COMPLETED'}
                        onClick={() => navigate(`/JudgeSubmissions/${comp.id}`)}
                      >
                        Review
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </div>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          {selectedComp?.name}
          <Chip label={selectedComp?.status} sx={{ ml: 2 }} color="primary" size="small" />
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          {selectedComp && (
            <>
              <Typography sx={{ mb: 2 }}>{selectedComp.description}</Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography>
                <strong>Category:</strong> {selectedComp.category}
              </Typography>
              <Typography>
                <strong>Participation:</strong> {selectedComp.participationType}
              </Typography>
              <Typography>
                <strong>Start:</strong>{' '}
                {new Date(selectedComp.startDate).toLocaleString()}
              </Typography>
              <Typography>
                <strong>End:</strong> {new Date(selectedComp.endDate).toLocaleString()}
              </Typography>
              <Typography>
                <strong>Public:</strong> {selectedComp.isPublic ? 'Yes' : 'No'}
              </Typography>
              <Divider sx={{ my: 2 }} />
              <Typography>
                <strong>Scoring Criteria:</strong>
              </Typography>
              {selectedComp.scoringCriteria?.map((item, idx) => (
                <Chip key={idx} label={item} size="small" sx={{ m: 0.5 }} />
              ))}
              <Typography sx={{ mt: 2 }}>
                <strong>Allowed Submission Types:</strong>
              </Typography>
              {selectedComp.allowedSubmissionTypes?.map((item, idx) => (
                <Chip key={idx} label={item} size="small" color="secondary" sx={{ m: 0.5 }} />
              ))}
              {selectedComp.imageUrls?.length > 0 && (
                <div style={{ marginTop: 20 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Display Images:
                  </Typography>
                  <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                    {selectedComp.imageUrls.map((url, idx) => (
                      <img
                        key={idx}
                        src={url}
                        alt={`img-${idx}`}
                        style={{
                          width: 120,
                          height: 100,
                          objectFit: 'cover',
                          borderRadius: 6,
                          border: '1px solid #ddd',
                        }}
                      />
                    ))}
                  </div>
                </div>
              )}
              {selectedComp.introVideoUrl && (
                <Button
                  variant="outlined"
                  href={selectedComp.introVideoUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  sx={{ mt: 3 }}
                >
                  Watch Intro Video
                </Button>
              )}
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default Rating;
