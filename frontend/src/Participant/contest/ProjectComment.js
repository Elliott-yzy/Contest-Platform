/**
 * @file ProjectComment.js
 * @description 
 * This component allows participants to view and post comments under a project submission.
 * Key functionalities include:
 *  - Displaying a paginated list of existing comments and their replies.
 *  - Allowing logged-in users to submit a new comment on a project.
 *  - Real-time feedback through Snackbar notifications for success and error cases.
 *  - Automatic reloading of the comment list after a new comment is posted.
 * The component integrates with a backend API, using authentication tokens
 * and supports both comment creation and comment display inside a modal dialog.
 * It uses Material-UI for layout, dialogs, and notifications.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect } from "react";
import { Button, TextField, Dialog, DialogTitle, DialogContent, DialogActions, Typography, Snackbar, Alert } from "@mui/material";
import axios from "axios";

function ProjectComment({ submissionId }) {
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentsOpen, setCommentsOpen] = useState(false);
  const [newComment, setNewComment] = useState("");
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");

  // Get the comment list
  useEffect(() => {
    if (submissionId) {
      setCommentsLoading(true);
      axios
        .get(`http://localhost:8080/interactions/comments/list`, {
          params: {
            submissionId: submissionId,
            page: 1,
            size: 10,
            sortBy: "createdAt",
            order: "desc",
          },
        })
        .then((res) => {
          setComments(res.data.data || []);
          setCommentsLoading(false);
        })
        .catch((err) => {
          console.error("Failed to fetch comments:", err);
          setComments([]);
          setCommentsLoading(false);
        });
    }
  }, [submissionId]);

  // Open the comment pop-up window
  const handleOpenComments = () => {
    setCommentsOpen(true);
  };

  // Close the comment pop-up window
  const handleCloseComments = () => {
    setCommentsOpen(false);
  };

  // Handle the submission of a new comment
  const handleAddComment = () => {
    const userId = localStorage.getItem("userId");
    const token = localStorage.getItem("token");

    if (!userId || !token) {
      setSnackbarMessage("User not logged in. Cannot post comment.");
      setSnackbarSeverity("error");
      setSnackbarOpen(true);
      return;
    }

    const commentData = {
      submissionId: submissionId,
      content: newComment,
    };

    axios
      .post(`http://localhost:8080/interactions/comments`, commentData, {
        headers: {
          "User-ID": userId,
          "Authorization": `Bearer ${token}`,
        },
      })
      .then(() => {
        setSnackbarMessage("Comment posted successfully!");
        setSnackbarSeverity("success");
        setSnackbarOpen(true);
        setNewComment("");  // Clear the input field
        handleOpenComments(); // refresh the comment list
      })
      .catch((error) => {
        setSnackbarMessage("Failed to post comment.");
        setSnackbarSeverity("error");
        setSnackbarOpen(true);
      });
  };

  const handleSnackbarClose = () => {
    setSnackbarOpen(false);
  };

  return (
    <>
      <div style={{ marginTop: "20px", textAlign: "center" }}>
        <TextField
          label="Add a Comment"
          fullWidth
          multiline
          rows={4}
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          variant="outlined"
        />
        <Button
          variant="contained"
          color="primary"
          onClick={handleAddComment}
          sx={{ mt: 2 }}
        >
          Post Comment
        </Button>
      </div>

      {/* Comment */}
      <Dialog open={commentsOpen} onClose={handleCloseComments} fullWidth maxWidth="md">
        <DialogTitle>Comment list</DialogTitle>
        <DialogContent dividers>
          {commentsLoading ? (
            <Typography>Loading...</Typography>
          ) : comments.length === 0 ? (
            <Typography>There are no comments for the moment.</Typography>
          ) : (
            comments.map((comment) => (
              <div key={comment._id} style={{ marginBottom: "10px" }}>
                <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
                  {comment.authorName || "Anonymous"}
                </Typography>
                <Typography variant="body2" sx={{ color: "gray" }}>
                  {new Date(comment.createdAt).toLocaleString()}
                </Typography>
                <Typography variant="body1" sx={{ marginTop: "5px" }}>
                  {comment.content}
                </Typography>
                {/* 子评论展示 */}
                {comment.replies && comment.replies.length > 0 && (
                  <div style={{ marginLeft: "20px" }}>
                    {comment.replies.map((reply) => (
                      <div key={reply._id}>
                        <Typography variant="body2" sx={{ fontWeight: "bold" }}>
                          {reply.authorName || "Anonymous"}
                        </Typography>
                        <Typography variant="body2" sx={{ color: "gray" }}>
                          {new Date(reply.createdAt).toLocaleString()}
                        </Typography>
                        <Typography variant="body2" sx={{ marginTop: "5px" }}>
                          {reply.content}
                        </Typography>
                      </div>
                    ))}
                  </div>
                )}
                <hr />
              </div>
            ))
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseComments} color="primary">
            Close
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar Notification */}
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
    </>
  );
}

export default ProjectComment;
