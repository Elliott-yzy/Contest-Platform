/**
 * ViewVote.js
 * 
 * Displays vote count for a submission and allows participants to vote or cancel their vote.
 * Fetches initial vote data and handles vote actions with backend API.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect } from "react";
import { Button, Typography, Box, Snackbar, Alert } from "@mui/material";
import axios from "axios";

function ViewVote({ submissionId }) {
  const [votes, setVotes] = useState(0);
  const [hasVoted, setHasVoted] = useState(false); // Whether the user has already voted
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("info");

  // Fetch initial vote count and voting status
  useEffect(() => {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");
    
    if (!submissionId || !userId || !token) return;

    // Get vote count
    axios
      .get(`http://localhost:8080/interactions/votes/count`, {
        params: { submissionId },
      })
      .then((res) => {
        setVotes(res.data || 0);
      })
      .catch((err) => {
        console.error("Failed to fetch vote count:", err);
      });

    // Check if already voted
    axios
      .get(`http://localhost:8080/interactions/votes/status`, {
        params: { submissionId },
        headers: {
          "User-ID": userId,
          "Authorization": `Bearer ${token}`,
        },
      })
      .then((res) => {
        setHasVoted(res.data === true);
      })
      .catch((err) => {
        console.error("Failed to check vote status:", err);
      });
  }, [submissionId]);

  // Handle vote or cancel vote
  const handleVote = () => {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");
    console.log("提交投票参数：", { submissionId, userId, token });

    if (!userId || !token) {
      setSnackbarMessage("User not logged in.");
      setSnackbarSeverity("error");
      setSnackbarOpen(true);
      return;
    }

    if (!hasVoted) {
      // Cast vote
      axios
        .post(
          `http://localhost:8080/interactions/votes?submissionId=${submissionId}`,
           null,
          {
            headers: {
              "User-ID": userId,
              "Authorization": `Bearer ${token}`,
              "Content-Type": "application/x-www-form-urlencoded",
            },
          }
        )
        .then(() => {
          setVotes((prev) => prev + 1);
          setHasVoted(true);
          setSnackbarMessage("Vote successful! Thank you!");
          setSnackbarSeverity("success");
          setSnackbarOpen(true);
        })
        .catch((err) => {
          console.error("Vote failed:", err);
          setSnackbarMessage("Vote failed.");
          setSnackbarSeverity("error");
          setSnackbarOpen(true);
        });
    } else {
      // Cancel vote
      axios
        .delete(`http://localhost:8080/interactions/votes`, {
          params: { submissionId },
          headers: {
            "User-ID": userId,
            "Authorization": `Bearer ${token}`,
          },
        })
        .then(() => {
          setVotes((prev) => Math.max(prev - 1, 0));
          setHasVoted(false);
          setSnackbarMessage("Vote canceled.");
          setSnackbarSeverity("info");
          setSnackbarOpen(true);
        })
        .catch((err) => {
          console.error("Cancel vote failed:", err);
          setSnackbarMessage("Cancel vote failed.");
          setSnackbarSeverity("error");
          setSnackbarOpen(true);
        });
    }
  };

  const handleSnackbarClose = () => {
    setSnackbarOpen(false);
  };

  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1" style={{ marginRight: "8px" }}>
        {votes}
      </Typography>
      <Button
        variant="contained"
        onClick={handleVote}
        sx={{
          backgroundColor: "#FF9800",
          color: "white",
          ":hover": {
            backgroundColor: "#FB8C00", // Darker orange for hover effect
          },
        }}
      >
        {hasVoted ? "Cancel Vote" : "Vote"}
      </Button>

      {/* Snackbar notification */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          onClose={handleSnackbarClose}
          severity={snackbarSeverity}
          sx={{ width: "100%" }}
        >
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </Box>
  );
}

export default ViewVote;
