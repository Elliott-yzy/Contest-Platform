/**
 * @file DeleteComment.js
 * @description 
 * This component allows a user to delete their comment from a submission's comment section.
 * Participants can:
 *  - Click the delete button next to their own comments or replies.
 *  - Receive real-time feedback via a Snackbar after deletion succeeds or fails.
 *  - Trigger a parent refresh by invoking the `onDeleted` callback after successful deletion.
 * The component communicates with a backend API, sending proper authentication headers
 * and handles user session validations (token and userId presence).
 * It uses Material-UI components for the delete button and snackbar feedback.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState } from "react";
import PropTypes from "prop-types";
import axios from "axios";
import { IconButton, Snackbar, Alert } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";

function DeleteComment({ commentId, onDeleted }) {
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");

  const currentUserId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");
  const userRole = localStorage.getItem("role");

  const handleDelete = () => {
    if (!currentUserId || !token) {
      setSnackbarMessage("Please log in.");
      setSnackbarSeverity("error");
      return setSnackbarOpen(true);
    }

    axios
      .delete(
        `http://localhost:8080/interactions/comments/${commentId}`,
        {
          headers: {
            "User-ID": currentUserId,
            "User-Role": userRole,
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/x-www-form-urlencoded",
          },
        }
      )
      .then(() => {
        setSnackbarMessage("Comment deleted successfully!");
        setSnackbarSeverity("success");
        setSnackbarOpen(true);
        onDeleted();
      })
      .catch((err) => {
        console.error(err);
        setSnackbarMessage("Failed to delete comment.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
      });
  };

  const handleClose = () => setSnackbarOpen(false);

  return (
    <>
      <IconButton onClick={handleDelete} size="small">
        <DeleteIcon fontSize="small" />
      </IconButton>
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={3000}
        onClose={handleClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert onClose={handleClose} severity={snackbarSeverity} sx={{ width: "100%" }}>
          {snackbarMessage}
        </Alert>
      </Snackbar>
    </>
  );
}

DeleteComment.propTypes = {
  commentId: PropTypes.string.isRequired,
  onDeleted: PropTypes.func.isRequired,
};

export default DeleteComment;
