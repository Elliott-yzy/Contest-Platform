/**
 * TeamPublicDetail.js
 * 
 * This component displays the details of a team and its submission for a specific competition. It includes:
 * - The team name and description (passed from navigation state).
 * - The submission details including title, description, file (if available), review status, and total score.
 * 
 * It fetches the submission data from the backend using the competition and team IDs.
 * Displays loading state while fetching data and shows error if no submission is found.
 * 
 * Role: Public User
 * Developer: Ziqi Yi
 */
import React, { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { Typography, CircularProgress, Button, Box, Paper } from "@mui/material";
import Navbar from "../Homepages/Navbar";
import Footer from "../Homepages/Footer";
import "./TeamPublicDetail.css";

function TeamPublicDetail() {
  const { competitionId, teamId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { teamName, teamDescription } = location.state || {};
  const [submission, setSubmission] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSubmission = async () => {
      try {
        const res = await fetch(`http://localhost:8080/submissions/public/teams/${competitionId}/${teamId}`);
        if (!res.ok) throw new Error("No submission found for this team.");
        const data = await res.json();
        setSubmission(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchSubmission();
  }, [competitionId, teamId]);

  return (
    <>
      <Navbar />
      <div className="pu-team-detail-container">
        <Button variant="text" onClick={() => navigate(-1)} className="pu-back-button">
          ← Back to Team List
        </Button>

        <Paper elevation={4} className="pu-submission-card">
          <Typography variant="h5" className="pu-title">
            {teamName || "Unnamed Team"}
          </Typography>
          <Typography className="pu-description">
            {teamDescription || "No description provided."}
          </Typography>

          {loading ? (
            <Box sx={{ display: "flex", justifyContent: "center", mt: 5 }}>
              <CircularProgress />
            </Box>
          ) : error ? (
            <Typography align="center" color="error" sx={{ mt: 5 }}>
              No submission found for this team.
            </Typography>
          ) : (
            <>
              <Typography className="pu-sub-label">📌 Submission Title:</Typography>
              <Typography>{submission.title || "No title"}</Typography>

              <Typography className="pu-sub-label">📝 Description:</Typography>
              <Typography>{submission.description || "No description"}</Typography>

              <Typography className="pu-sub-label">📎 File:</Typography>
              {submission.fileUrl ? (
                <a href={submission.fileUrl} target="_blank" rel="noopener noreferrer">
                  {submission.fileName || "Download File"}
                </a>
              ) : (
                <Typography>No file submitted.</Typography>
              )}

              <Typography sx={{ mt: 2 }}>
                <strong>Review Status:</strong> {submission.reviewStatus}
              </Typography>
              <Typography>
                <strong>Total Score:</strong> {submission.totalScore ?? "N/A"}
              </Typography>
            </>
          )}
        </Paper>
      </div>
      <Footer />
    </>
  );
}

export default TeamPublicDetail;
