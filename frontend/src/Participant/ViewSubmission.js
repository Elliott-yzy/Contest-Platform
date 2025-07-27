/**
 * ViewSubmission.js
 * 
 * Participant view for submission details.
 * Displays submission info, file download, voting, and comments access.
 * Includes loading and error handling for fetching submission data.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Snackbar,
  Alert,
  IconButton,
  Box,
  CircularProgress,
} from "@mui/material";
import ArrowBackIosNewIcon from "@mui/icons-material/ArrowBackIosNew";
import TopBar from "../Participant/TopBar";
import Sidebar from "../Participant/Sidebar";
import "./ViewSubmission.css";
import ViewVote from "./ViewVote";
import axios from "axios";

function ViewSubmission() {
  const navigate = useNavigate();
  const { competitionId, submissionId } = useParams();

  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");

  // Get details of submission
  useEffect(() => {
    axios
      .get(`http://localhost:8080/submissions/public/${submissionId}`)
      .then((res) => {
        setSubmission(res.data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("❌ Failed to obtain submission", err);
        setSnackbarMessage("Failed to obtain the work information");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
        setLoading(false);
      });
  }, [submissionId]);

  const handleOpenCommentsPage = () => {
    navigate(`/comments/${submissionId}`);
  };

  const handleGoBack = () => {
    navigate(-1);
  };

  if (loading) {
    return (
      <>
        <TopBar />
        <Sidebar />
        <div className="view-submission-content" style={{ textAlign: "center", padding: "40px" }}>
          <CircularProgress color="warning" />
          <Typography mt={2}>Load the work information...</Typography>
        </div>
      </>
    );
  }

  if (!submission) {
    return (
      <div style={{ padding: "20px" }}>
        <Typography variant="h5" color="error">
          The work was not found.
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={handleGoBack}
          sx={{ mt: 2 }}
        >
          Go Back
        </Button>
      </div>
    );
  }

  return (
    <>
      <TopBar />
      <div className="view-submission-container">
        <Sidebar />
        <div className="view-submission-content">
          <Box display="flex" alignItems="center" sx={{ mb: 2 }}>
            <IconButton onClick={handleGoBack} sx={{ color: "#FF9800" }}>
              <ArrowBackIosNewIcon />
            </IconButton>
            <Typography
              variant="body1"
              sx={{ ml: 1, fontWeight: "bold", color: "#FF9800" }}
            >
              Go back
            </Typography>
          </Box>

          <Typography
            variant="h4"
            sx={{ mb: 4, fontWeight: "bold", textAlign: "center" }}
          >
            Submission Details
          </Typography>

          <TableContainer component={Paper} sx={{ boxShadow: 3 }}>
            <Table>
              <TableHead>
                <TableRow sx={{ backgroundColor: "#FF9800" }}>
                  <TableCell align="center">Title</TableCell>
                  <TableCell align="center">Description</TableCell>
                  <TableCell align="center">File Name</TableCell>
                  <TableCell align="center">File Type</TableCell>
                  <TableCell align="center">File</TableCell>
                  <TableCell align="center">Review Status</TableCell>
                  <TableCell align="center">Vote</TableCell>
                  <TableCell align="center">Comment</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow>
                  <TableCell align="center">{submission.title}</TableCell>
                  <TableCell align="center">{submission.description}</TableCell>
                  <TableCell align="center">{submission.fileName}</TableCell>
                  <TableCell align="center">{submission.fileType}</TableCell>
                  <TableCell align="center">
                    <Button
                      variant="contained"
                      href={submission.fileUrl}
                      target="_blank"
                      sx={{
                        backgroundColor: "#FF9800",
                        "&:hover": { backgroundColor: "#e68900" },
                      }}
                    >
                      View File
                    </Button>
                  </TableCell>
                  <TableCell align="center">{submission.reviewStatus}</TableCell>
                  <TableCell align="center">
                    <ViewVote submissionId={submissionId} />
                  </TableCell>
                  <TableCell align="center">
                    <Button
                      variant="outlined"
                      onClick={handleOpenCommentsPage}
                      sx={{
                        color: "#FF9800",
                        borderColor: "#FF9800",
                        "&:hover": {
                          backgroundColor: "rgba(255, 152, 0, 0.08)",
                          borderColor: "#e68900",
                          color: "#e68900",
                        },
                      }}
                    >
                      View comment
                    </Button>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </div>

      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={() => setSnackbarOpen(false)}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          onClose={() => setSnackbarOpen(false)}
          severity={snackbarSeverity}
          sx={{ width: "100%" }}
        >
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </>
  );
}

export default ViewSubmission;
