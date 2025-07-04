/**
 * ReRating.js
 * 
 * This component allows judges to update their previous rating for a submission. It fetches the initial judging details and provides sliders to adjust scores and a text field for feedback. Once the rating is updated, the data is submitted to the backend.
 * 
 * Role: Judge
 * Developer: Zhaoyi Yang
 */



import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import TopBar from './TopSide/TopBar';
import Sidebar from './TopSide/Sidebar';
import './Rating.css';
import {
  Typography,
  Slider,
  Paper,
  Box,
  CircularProgress,
  Button,
  Snackbar,
  Alert,
  TextField,
  Grid,
} from '@mui/material';

function ReRating() {
  const { competitionId, submissionId } = useParams();
  const navigate = useNavigate();

  const [scoringCriteria, setScoringCriteria] = useState([]);
  const [scores, setScores] = useState({});
  const [weights, setWeights] = useState({});
  const [feedback, setFeedback] = useState('');
  const [loading, setLoading] = useState(true);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  useEffect(() => {
    const fetchJudgingDetails = async () => {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      if (!token || !userId) return;

      try {
        const res = await fetch(`http://localhost:8080/judges/${submissionId}/detail`, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
            'User-ID': userId,
          },
        });

        const data = await res.json();
        if (res.ok) {
          const newScores = {};
          const newWeights = {};
          data.scores.forEach(s => {
            newScores[s.criterion] = s.score;
            newWeights[s.criterion] = s.weight;
          });
          setScoringCriteria(data.scores.map(s => s.criterion));
          setScores(newScores);
          setWeights(newWeights);
          setFeedback(data.judgeComments || '');
        } else {
          console.error('Failed to fetch judging detail:', data);
        }
      } catch (err) {
        console.error('Error fetching judging detail:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchJudgingDetails();
  }, [submissionId]);

  const handleSliderChange = (criterion, value) => {
    setScores(prev => ({ ...prev, [criterion]: value }));
  };

  const handleSubmit = async () => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    if (!token || !userId) return;

    const scoreArray = scoringCriteria.map(criterion => ({
      criterion,
      score: scores[criterion],
      weight: weights[criterion],
    }));

    try {
      const res = await fetch(`http://localhost:8080/judges/${submissionId}`, {
        method: 'PUT',
        headers: {
          Authorization: `Bearer ${token}`,
          'User-ID': userId,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          competitionId,
          submissionId,
          judgeComments: feedback,
          scores: scoreArray,
        }),
      });

      if (res.ok) {
        setSnackbar({ open: true, message: 'Rating updated successfully ✅', severity: 'success' });
        setTimeout(() => navigate(`/JudgeSubmissions/${competitionId}`), 1500);
      } else {
        const data = await res.json();
        setSnackbar({ open: true, message: `Failed to update: ${data.error}`, severity: 'error' });
      }
    } catch (err) {
      console.error('Error updating rating:', err);
      setSnackbar({ open: true, message: 'Error updating rating ❌', severity: 'error' });
    }
  };

  return (
    <>
      <TopBar />
      <div className="rating-container">
        <Sidebar />
        <div className="rating-content">
          <Typography variant="h5" gutterBottom>
            Update Your Rating
          </Typography>

          {loading ? (
            <div style={{ textAlign: 'center', marginTop: '2rem' }}>
              <CircularProgress />
            </div>
          ) : (
            <Paper sx={{ padding: 3, borderRadius: '20px' }}>
              <Grid container spacing={4}>
                <Grid item xs={12} md={7}>
                  {scoringCriteria.map((criterion, index) => (
                    <Box key={index} mb={4}>
                      <Typography gutterBottom>{criterion}</Typography>
                      <Slider
                        value={scores[criterion]}
                        min={0}
                        max={10}
                        step={0.1}
                        onChange={(e, val) => handleSliderChange(criterion, val)}
                        valueLabelDisplay="auto"
                        marks={[{ value: 0, label: '0.0' }, { value: 10, label: '10.0' }]}
                      />
                    </Box>
                  ))}
                </Grid>

                <Grid item xs={12} md={5} display="flex" flexDirection="column" alignItems="center">
                  <Typography gutterBottom>Feedback</Typography>
                  <TextField
                    multiline
                    rows={8}
                    placeholder="Write your feedback here..."
                    value={feedback}
                    onChange={(e) => setFeedback(e.target.value)}
                    fullWidth
                    sx={{ borderRadius: '16px', backgroundColor: '#eaf4f8', mb: 2 }}
                  />
                  <Button
                    onClick={handleSubmit}
                    variant="contained"
                    sx={{
                      backgroundColor: '#ffa726',
                      color: '#fff',
                      borderRadius: '20px',
                      textTransform: 'none',
                      px: 4,
                      ':hover': {
                        backgroundColor: '#fb8c00',
                      },
                    }}
                  >
                    Update Rating
                  </Button>
                </Grid>
              </Grid>
            </Paper>
          )}

          <Snackbar
            open={snackbar.open}
            autoHideDuration={4000}
            onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
          >
            <Alert severity={snackbar.severity} sx={{ width: '100%' }}>
              {snackbar.message}
            </Alert>
          </Snackbar>
        </div>
      </div>
    </>
  );
}

export default ReRating;
