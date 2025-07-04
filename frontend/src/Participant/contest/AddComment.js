/**
 * @file AddComment.js
 * @description 
 * This component allows participants to add comments on a specific submission.
 * It provides a text input area for users to write and submit their comments.
 * Core functionalities include:
 *  - Input validation (preventing empty comments).
 *  - Posting comments to the backend with appropriate authentication headers.
 *  - Displaying success or error feedback via Material-UI Snackbar and Alert components.
 *  - Invoking a callback to refresh the comment list after successful posting.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState } from "react";
import { Button, TextField, Typography, Snackbar, Alert } from "@mui/material";
import axios from "axios";

function AddComment({ submissionId, onCommentPosted }) {
  const [newComment, setNewComment] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");

  const currentUserId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");

  const showSnackbar = (message, severity = "success") => {
    setSnackbarMessage(message);
    setSnackbarSeverity(severity);
    setSnackbarOpen(true);
  };

  const handlePostComment = () => {
    if (!newComment.trim()) {
      return showSnackbar("Comment cannot be empty.", "error");
    }
    if (!currentUserId || !token) {
      return showSnackbar("Please log in.", "error");
    }

    axios
      .post(
        `http://localhost:8080/interactions/comments`,
        { submissionId, content: newComment },
        {
          headers: {
            "User-ID": currentUserId,
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      )
      .then(() => {
        setNewComment("");
        onCommentPosted();
        showSnackbar("Comment added successfully!");
      })
      .catch((err) => {
        console.error(err);
        showSnackbar("Failed to add comment.", "error");
      });
  };

  const handleSnackbarClose = () => {
    setSnackbarOpen(false);
  };

  return (
    <div className="add-comment-section" style={{ marginTop: "30px" }}>
      <Typography variant="h5" sx={{ mb: 2, color: "black" }}>
        Add a Comment
      </Typography>
      <TextField
        label="Write your comment"
        placeholder="Type your thoughts here..."
        fullWidth
        multiline
        rows={3}
        value={newComment}
        onChange={(e) => setNewComment(e.target.value)}
        variant="outlined"
        sx={{
          mb: 3,
          bgcolor: "#fff8e1",         // pale yellow background
          borderRadius: "8px",        // rounded corners
          "& .MuiOutlinedInput-root": {
            "& fieldset": {
              borderColor: "#FFB74D", // light orange border
            },
            "&:hover fieldset": {
              borderColor: "#FFA726",
            },
            "&.Mui-focused fieldset": {
              borderColor: "#FB8C00", // darker orange when focused
              borderWidth: "2px",
            },
          },
          "& .MuiInputLabel-root": {
            color: "#FB8C00",
            fontWeight: "bold",       // bold label
          },
          "& .MuiInputLabel-root.Mui-focused": {
            color: "#E65100",
          }
        }}
      />

      <Button
        variant="contained"
        onClick={handlePostComment}
        sx={{
          mt: 2,
          backgroundColor: "#FF9800",
          "&:hover": { backgroundColor: "#e68900" },
        }}
      >
        Post Comment
      </Button>

      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert onClose={handleSnackbarClose} severity={snackbarSeverity} sx={{ width: "100%" }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </div>
  );
}

export default AddComment;
