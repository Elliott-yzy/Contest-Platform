/**
 * JudgeSubmissions.js
 * 
 * Allows judges to view, search, and review pending submissions for a competition.
 * Provides functionality to open detailed judge comments and scores for each submission.
 * 
 * Role: Judge
 * Developer: Zhaoyi Yang
 */

import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import TopBar from './TopSide/TopBar';
import Sidebar from './TopSide/Sidebar';
import './Rating.css';
import {
  Typography,
  Table,
  TableContainer,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  Link,
  TextField,
  Pagination,
  CircularProgress,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Box,
} from '@mui/material';

function JudgeSubmissions() {
  const { competitionId } = useParams();
  const [submissions, setSubmissions] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [judgeDetail, setJudgeDetail] = useState(null);
  const navigate = useNavigate();

  const fetchSubmissions = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) return;

    setLoading(true);
    try {
      const res = await fetch(
        `http://localhost:8080/judges/pending-submissions?competitionId=${competitionId}&keyword=${keyword}&page=${page}&size=10&sortOrder=desc`,
        {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const data = await res.json();
      if (res.ok) {
        setSubmissions(data.data || []);
        setTotalPages(data.pages || 1);
      } else {
        console.error('Failed to fetch submissions:', data);
      }
    } catch (err) {
      console.error('Error fetching pending submissions:', err);
    } finally {
      setLoading(false);
    }
  }, [competitionId, keyword, page]);

  useEffect(() => {
    fetchSubmissions();
  }, [fetchSubmissions]);

  const handleViewJudgingDetail = async (submissionId) => {
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
        setJudgeDetail(data);
        setOpenDialog(true);
      } else {
        console.error('Failed to fetch judging detail:', data);
      }
    } catch (err) {
      console.error('Error fetching judging detail:', err);
    }
  };

  return (
    <>
      <TopBar />
      <div className="rating-container">
        <Sidebar />
        <div className="rating-content">
          <Typography variant="h5" gutterBottom>
            Pending Submissions for Review
          </Typography>

          <TextField
            label="Search by Title"
            variant="outlined"
            value={keyword}
            onChange={(e) => {
              setPage(1);
              setKeyword(e.target.value);
            }}
            fullWidth
            sx={{ mt: 2, mb: 2 }}
          />

          {loading ? (
            <div style={{ textAlign: 'center', marginTop: '2rem' }}>
              <CircularProgress />
            </div>
          ) : (
            <div className="table-wrapper">
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>#</TableCell>
                      <TableCell>Title</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Last Updated</TableCell>
                      <TableCell>File</TableCell>
                      <TableCell>Scored</TableCell>
                      <TableCell>Action</TableCell>
                      <TableCell>Rejudge</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {submissions.map((submission, index) => (
                      <TableRow key={submission.id}>
                        <TableCell>{(page - 1) * 10 + index + 1}</TableCell>
                        <TableCell>{submission.title}</TableCell>
                        <TableCell>{submission.description}</TableCell>
                        <TableCell>
                          {new Date(submission.lastUpdatedAt).toLocaleString()}
                        </TableCell>
                        <TableCell>
                          <Link href={submission.fileUrl} target="_blank" rel="noopener">
                            {submission.fileName || 'Download'}
                          </Link>
                        </TableCell>
                        <TableCell>
                          {submission.hasScored ? (
                            <Button
                              variant="text"
                              sx={{ color: '#1976d2', textTransform: 'none' }}
                              onClick={() => handleViewJudgingDetail(submission.id)}
                            >
                              Yes
                            </Button>
                          ) : (
                            'No'
                          )}
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="contained"
                            size="small"
                            disabled={submission.hasScored}
                            onClick={() =>
                              navigate(`/RatingDetail/${competitionId}/${submission.id}`)
                            }
                          >
                            Review
                          </Button>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="outlined"
                            size="small"
                            disabled={!submission.hasScored}
                            onClick={() =>
                              navigate(`/ReRating/${competitionId}/${submission.id}`)
                            }
                          >
                            Rejudge
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </div>
          )}

          <Pagination
            sx={{ mt: 2 }}
            count={totalPages}
            page={page}
            onChange={(e, val) => setPage(val)}
            color="primary"
          />
        </div>
      </div>

      {/* 美观弹窗 */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 'bold', fontSize: '1.5rem', textAlign: 'center' }}>
          🧑‍⚖️ Judging Details
        </DialogTitle>
        <DialogContent dividers>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            <strong>Judge Comments:</strong> {judgeDetail?.judgeComments || 'No comments'}
          </Typography>
          <Typography variant="subtitle1" sx={{ mb: 1 }}>
            <strong>Total Score:</strong> {judgeDetail?.totalScore}
          </Typography>
          <Typography variant="subtitle1" sx={{ fontWeight: 'bold', mt: 2, mb: 1 }}>
            Criteria Scores:
          </Typography>
          {judgeDetail?.scores?.map((s, idx) => (
            <Box
              key={idx}
              sx={{
                backgroundColor: '#f0f4f8',
                padding: '8px 12px',
                borderRadius: '8px',
                mb: 1,
              }}
            >
              <Typography>
                <strong>{s.criterion}</strong>: {s.score}{' '}
                <span style={{ color: '#888' }}>(weight: {s.weight})</span>
              </Typography>
            </Box>
          ))}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setOpenDialog(false)}
            sx={{
              borderRadius: '20px',
              px: 4,
              backgroundColor: '#90caf9',
              color: '#fff',
              textTransform: 'none',
              ':hover': {
                backgroundColor: '#64b5f6',
              },
            }}
          >
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default JudgeSubmissions;
