/**
 * @file ViewSubmission.js
 * @description 
 * This component displays detailed information about a single submission.
 * Features include:
 *  - Displaying submission metadata such as title, description, file information, and review status.
 *  - Providing a button to view the submitted file in a new tab.
 *  - Enabling users to view votes and comments associated with the submission.
 *  - Navigation controls to return to the previous page or to open the comments page.
 * The component integrates with the ViewVote and comment management functionalities.
 * It uses Material-UI for table layout, buttons, and notifications.
 * 
 * Role: Participant
 * Developer: Zhaoyi Yang
 */

import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
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
import TopBar from "../TopSide/TopBar";
import Sidebar from "../TopSide/Sidebar";
import "./ViewSubmission.css";
import ViewVote from "../ViewVote";

function ViewSubmission() {
  const { competitionId } = useParams();
  const navigate = useNavigate();

  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        const res = await fetch(`http://localhost:8080/submissions/public/approved?competitionId=${competitionId}`);
        if (!res.ok) {
          const text = await res.text();
          throw new Error(text || "Failed to fetch submissions");
        }
        const result = await res.json();
        setSubmissions(result.data || []);
      } catch (error) {
        console.error("Error fetching submissions:", error);
        setErrorMessage(error.message || "Network error fetching submissions");
      } finally {
        setLoading(false);
      }
    };

    fetchSubmissions();
  }, [competitionId]);

  const handleOpenCommentsPage = (submissionId) => {
    navigate(`/comments/${submissionId}`);
  };

  const handleGoBack = () => {
    navigate(-1);
  };

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
            Approved Submissions
          </Typography>

          {loading ? (
            <Box display="flex" justifyContent="center" alignItems="center" height="50vh">
              <CircularProgress />
            </Box>
          ) : errorMessage ? (
            <Typography variant="h6" color="error" align="center">
              {errorMessage}
            </Typography>
          ) : submissions.length === 0 ? (
            <Typography variant="h6" align="center">
              No approved submissions found.
            </Typography>
          ) : (
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
                  {submissions.map((submission) => (
                    <TableRow key={submission.id || submission._id}>
                      <TableCell align="center">{submission.title}</TableCell>
                      <TableCell align="center">{submission.description}</TableCell>
                      <TableCell align="center">{submission.fileName}</TableCell>
                      <TableCell align="center">{submission.fileType}</TableCell>
                      <TableCell align="center">
                        <Button
                          variant="contained"
                          color="primary"
                          href={submission.fileUrl}
                          target="_blank"
                          sx={{
                            backgroundColor: "#FF9800",
                            "&:hover": {
                              backgroundColor: "#e68900",
                            },
                          }}
                        >
                          View File
                        </Button>
                      </TableCell>
                      <TableCell align="center">{submission.reviewStatus}</TableCell>
                      <TableCell align="center">
                        <ViewVote submissionId={submission.id || submission._id} />
                      </TableCell>
                      <TableCell align="center">
                        <Button
                          variant="outlined"
                          onClick={() => handleOpenCommentsPage(submission.id || submission._id)}
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
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </div>
      </div>

      <Snackbar
        open={Boolean(errorMessage)}
        autoHideDuration={3000}
        onClose={() => setErrorMessage("")}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          onClose={() => setErrorMessage("")}
          severity="error"
          sx={{ width: "100%" }}
        >
          {errorMessage}
        </Alert>
      </Snackbar>
    </>
  );
}

export default ViewSubmission;