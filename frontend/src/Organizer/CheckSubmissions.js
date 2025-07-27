/**
 * @file OrganizerSubmissions.js
 * @description 
 * This component allows organizers to manage and review contest submissions.
 * It provides functionality to:
 *  - View all submissions for a specific competition with pagination and search.
 *  - Sort submissions by creation time.
 *  - Open a detailed view of each submission, including title, description, file information, and time.
 *  - Approve or reject submissions with review comments.
 *  - Navigate to the scoring page for individual submissions.
 * 
 * The component interacts with backend APIs for fetching submissions and submitting reviews,
 * uses Material-UI components for layout and modals, and maintains state for loading, pagination, and review actions.
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useEffect, useState, useCallback } from "react";
import {
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TableContainer,
  Paper,
  Typography,
  Pagination,
  TextField,
  CircularProgress,
  Box,
  Button,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  MenuItem,
  Select,
  InputLabel,
  FormControl,
  Link,
} from "@mui/material";
import { Visibility } from "@mui/icons-material";
import { useParams, useNavigate } from "react-router-dom";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import "./ContestList.css";

function OrganizerSubmissions() {
  const { competitionId } = useParams();
  const navigate = useNavigate();
  const email = localStorage.getItem("email");
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const role = localStorage.getItem("role");

  const [submissions, setSubmissions] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [, setTotalCount] = useState(0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState("");
  const [sortBy] = useState("createdAt");
  const [order, setOrder] = useState("desc");
  const [loading, setLoading] = useState(false);
  const [competitionName, setCompetitionName] = useState("");

  const [selectedSubmission, setSelectedSubmission] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [reviewStatus, setReviewStatus] = useState("");
  const [reviewComments, setReviewComments] = useState("");

  const fetchSubmissions = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(
        `http://localhost:8080/submissions/public?competitionId=${competitionId}&page=${page}&size=10&keyword=${keyword}&sortBy=${sortBy}&order=${order}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
        }
      );
      const data = await res.json();
      if (res.ok) {
        setSubmissions(data.data || []);
        setTotalPages(data.pages || 1);
        setTotalCount(data.total || 0);
      } else {
        console.error("Failed to fetch submissions:", data);
      }
    } catch (err) {
      console.error("Error fetching submissions:", err);
    } finally {
      setLoading(false);
    }
  }, [competitionId, page, keyword, sortBy, order, token, userId, role]);

  useEffect(() => {
    const fetchCompetitionName = async () => {
      try {
        const res = await fetch(`http://localhost:8080/competitions/${competitionId}`);
        const data = await res.json();
        if (res.ok) {
          setCompetitionName(data.name || "Unnamed Competition");
        } else {
          console.error("Failed to fetch competition name:", data);
        }
      } catch (err) {
        console.error("Error fetching competition name:", err);
      }
    };
    fetchCompetitionName();
  }, [competitionId]);

  useEffect(() => {
    fetchSubmissions();
  }, [fetchSubmissions]);

  const handleSearch = (e) => {
    setKeyword(e.target.value);
    setPage(1);
  };

  const handleSortToggle = () => {
    setOrder((prev) => (prev === "asc" ? "desc" : "asc"));
    setPage(1);
  };

  const handleViewDetail = (submission) => {
    setSelectedSubmission(submission);
    setReviewStatus(["APPROVED", "REJECTED"].includes(submission.reviewStatus) ? submission.reviewStatus : "");
    setReviewComments(submission.reviewComments || "");
    setDialogOpen(true);
  };

  const handleReviewSubmit = async () => {
    if (!selectedSubmission) return;
    try {
      const res = await fetch("http://localhost:8080/submissions/review", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
          "User-ID": userId,
          "User-Role": role.toUpperCase(),
        },
        body: JSON.stringify({
          submissionId: selectedSubmission.id,
          reviewStatus,
          reviewComments,
        }),
      });
      if (res.ok) {
        alert("✅ Review submitted successfully");
        setDialogOpen(false);
        fetchSubmissions();
      } else {
        const err = await res.json();
        alert("❌ Review failed: " + err.message);
      }
    } catch (err) {
      alert("❌ Error submitting review");
      console.error(err);
    }
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <Typography variant="h5" gutterBottom>
            Submissions for: {competitionName}
          </Typography>

          <Box sx={{ display: "flex", gap: 2, mb: 2 }}>
            <TextField
              label="Search by title or description"
              value={keyword}
              onChange={handleSearch}
              variant="outlined"
              fullWidth
            />
            <Button variant="outlined" onClick={handleSortToggle}>
              Sort by: {sortBy} ({order.toUpperCase()})
            </Button>
          </Box>

          {loading ? (
            <div style={{ textAlign: "center", marginTop: 50 }}>
              <CircularProgress />
            </div>
          ) : (
            <>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Title</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Submitted At</TableCell>
                      <TableCell>Score</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Action</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {submissions.map((s) => (
                      <TableRow key={s.id}>
                        <TableCell>{s.title}</TableCell>
                        <TableCell>{s.description}</TableCell>
                        <TableCell>{new Date(s.createdAt).toLocaleString()}</TableCell>
                        <TableCell>
                          {s.totalScore != null ? (
                            <Button
                              variant="text"
                              color="primary"
                              onClick={() => navigate(`/submissions/${competitionId}/ratings`)}
                            >
                              {s.totalScore}
                            </Button>
                          ) : (
                            "N/A"
                          )}
                        </TableCell>
                        <TableCell>
                          <Typography
                            variant="body2"
                            color={
                              s.reviewStatus === "APPROVED"
                                ? "green"
                                : s.reviewStatus === "REJECTED"
                                  ? "red"
                                  : "orange"
                            }
                            fontWeight="bold"
                          >
                            {s.reviewStatus ?? "PENDING"}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <IconButton color="primary" onClick={() => handleViewDetail(s)}>
                            <Visibility />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Pagination
                count={totalPages}
                page={page}
                onChange={(e, val) => setPage(val)}
                color="primary"
                sx={{ mt: 2 }}
              />

              <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Submission Review</DialogTitle>
                <DialogContent dividers>
                  {selectedSubmission && (
                    <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                      <Typography><strong>Title:</strong> {selectedSubmission.title}</Typography>
                      <Typography><strong>Description:</strong> {selectedSubmission.description}</Typography>
                      <Typography>
                        <strong>File:</strong>{" "}
                        <Link href={selectedSubmission.fileUrl} target="_blank" rel="noopener">
                          {selectedSubmission.fileName}
                        </Link>
                      </Typography>
                      <Typography><strong>Type:</strong> {selectedSubmission.fileType}</Typography>
                      <Typography><strong>Time:</strong> {new Date(selectedSubmission.createdAt).toLocaleString()}</Typography>
                      {selectedSubmission.fileType?.startsWith("image") && (
                        <img src={selectedSubmission.fileUrl} alt="preview" style={{ maxWidth: "100%" }} />
                      )}
                      <FormControl fullWidth>
                        <InputLabel>Review Status</InputLabel>
                        <Select
                          value={["APPROVED", "REJECTED"].includes(reviewStatus) ? reviewStatus : ""}
                          onChange={(e) => setReviewStatus(e.target.value)}
                          label="Review Status"
                        >
                          <MenuItem value="APPROVED">APPROVED</MenuItem>
                          <MenuItem value="REJECTED">REJECTED</MenuItem>
                        </Select>
                      </FormControl>
                      <TextField
                        label="Review Comments"
                        multiline
                        rows={4}
                        value={reviewComments}
                        onChange={(e) => setReviewComments(e.target.value)}
                        fullWidth
                      />
                    </Box>
                  )}
                </DialogContent>
                <DialogActions>
                  <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
                  <Button variant="contained" onClick={handleReviewSubmit}>Submit Review</Button>
                </DialogActions>
              </Dialog>

              <Button variant="outlined" sx={{ mt: 2 }} onClick={() => navigate(`/OrganizerContestList/${email}`)}>
                🔙 Back to Contest List
              </Button>
            </>
          )}
        </div>
      </div>
    </>
  );
}

export default OrganizerSubmissions;