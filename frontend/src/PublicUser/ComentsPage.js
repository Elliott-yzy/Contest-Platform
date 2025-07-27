/**
 * CommentsPage.js
 * 
 * This component allows users to view and manage comments for a specific submission. 
 * Users can load the comments, view replies, and load more comments when available.
 * It includes functionalities like:
 * - Viewing comments and replies with pagination.
 * - Expanding and collapsing replies to comments.
 * - Displaying the creation time of comments and replies.
 * - Displaying snackbar notifications for errors or success messages.
 * 
 * Role: Public User
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Button,
  Typography,
  Snackbar,
  Alert,
  Paper,
  IconButton,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import axios from "axios";
import Navbar from "../Homepages/Navbar";
import Footer from "../Homepages/Footer";
import "./ComentsPage.css";

function CommentsPage() {
  const { submissionId } = useParams();
  const navigate = useNavigate();

  const [comments, setComments] = useState([]);
  const [expandedReplies, setExpandedReplies] = useState({});
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");
  const currentUserId = localStorage.getItem("userId");

  const fetchComments = useCallback(async (pageToFetch = 1, reset = false) => {
    try {
      const res = await axios.get(`http://localhost:8080/interactions/comments/list`, {
        params: {
          submissionId,
          page: pageToFetch,
          size: 10,
          sortBy: "createdAt",
          order: "desc",
        },
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      });
      const fetchedComments = res.data.data || [];
      const pages = res.data.pages || 1;

      setTotalPages(pages);

      if (reset) {
        setComments(fetchedComments);
        setPage(1);
      } else {
        setComments((prev) => [...prev, ...fetchedComments]);
        setPage(pageToFetch);
      }
    } catch (error) {
      console.error("Failed to fetch comments:", error);
      showSnackbar("Failed to fetch comments.", "error");
    }
  }, [submissionId]);

  useEffect(() => {
    if (submissionId) {
      fetchComments(1, true);
    }
  }, [submissionId, fetchComments]);

  const handleLoadMore = () => {
    fetchComments(page + 1, false);
  };

  const toggleExpandedReplies = (commentId) => {
    setExpandedReplies((prev) => ({
      ...prev,
      [commentId]: !prev[commentId],
    }));
  };

  const showSnackbar = (message, severity = "success") => {
    setSnackbarOpen(false); // Reset first
    setTimeout(() => {
      setSnackbarMessage(message);
      setSnackbarSeverity(severity);
      setSnackbarOpen(true);
    }, 100); // Give a tiny delay
  };

  const handleSnackbarClose = () => {
    setSnackbarOpen(false);
  };

  const handleBack = () => navigate(-1);

  return (
    <div className="publicuser-comments-page-container">
      <Navbar />
      <div className="publicuser-comments-page-content">
        <div className="publicuser-comments-section">
          <div className="publicuser-Right-comment-place">
            <div className="publicuser-comments-container">
              <div className="publicuser-wrapper">

                {/* Return button */}
                <div style={{ display: "flex", alignItems: "center", marginBottom: "20px" }}>
                  <IconButton onClick={handleBack}>
                    <ArrowBackIcon style={{ fontSize: "30px" }} />
                  </IconButton>
                  <Typography variant="body1" sx={{ color: "brown", marginLeft: "8px", fontWeight: "bold" }}>
                    Go Back
                  </Typography>
                </div>

                {/* Comment Title */}
                <Typography variant="h4" sx={{ mb: 2, fontWeight: "bold" }}>
                  Comments
                </Typography>

                {/* Comment list */}
                {comments.length === 0 ? (
                  <Typography>No comments yet.</Typography>
                ) : (
                  comments.map((comment) => {
                    const commentId = comment._id || comment.id;
                    const isExpanded = expandedReplies[commentId];
                    const repliesToShow = isExpanded
                      ? (comment.replies || [])
                      : (comment.replies?.slice(0, 1) || []);

                    return (
                      <Paper key={commentId} sx={{ marginBottom: 2, padding: 2, position: "relative" }}>
                        {/* Main comment content */}
                        <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
                          {comment.userId === currentUserId ? "My comment" : "Anonymous"}
                        </Typography>
                        <Typography variant="body1" sx={{ marginTop: "5px", marginBottom: "24px" }}>
                          {comment.content}
                        </Typography>
                        <Typography variant="body2" sx={{ position: "absolute", bottom: 8, left: 16, color: "gray", fontSize: "12px" }}>
                          {new Date(comment.createdAt).toLocaleString()}
                        </Typography>

                        {/* Sub-comment section */}
                        {comment.replies && comment.replies.length > 0 && (
                          <div style={{ marginLeft: "20px", marginTop: "20px" }}>
                            {repliesToShow.map((reply) => (
                              <Paper key={reply.id} sx={{ marginBottom: 1, padding: 1, backgroundColor: "#f9f9f9", position: "relative" }}>
                                <Typography variant="subtitle2" sx={{ fontWeight: "bold" }}>
                                  {reply.userId === currentUserId ? "My reply" : "Anonymous"}
                                </Typography>
                                <Typography variant="body2" sx={{ marginTop: "3px", marginBottom: "24px" }}>
                                  {reply.content}
                                </Typography>
                                <Typography variant="body2" sx={{ position: "absolute", bottom: 8, left: 16, color: "gray", fontSize: "11px" }}>
                                  {new Date(reply.createdAt).toLocaleString()}
                                </Typography>
                              </Paper>
                            ))}

                            {/* Expand/retract the comment button */}
                            {comment.replies.length > 1 && (
                              <Button
                                variant="text"
                                size="small"
                                onClick={() => toggleExpandedReplies(commentId)}
                                sx={{ color: "#FF9800", mt: 1 }}
                              >
                                {isExpanded ? "Hide replies" : "View more replies"}
                              </Button>
                            )}
                          </div>
                        )}
                      </Paper>
                    );
                  })
                )}

                {/* Load more comments */}
                {page < totalPages && (
                  <Button
                    variant="contained"
                    onClick={handleLoadMore}
                    sx={{
                      mt: 2,
                      backgroundColor: "#FF9800",
                      "&:hover": { backgroundColor: "#e68900" },
                    }}
                  >
                    Load More Comments
                  </Button>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Pop-up window prompt */}
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

      {/* Footer */}
      <Footer />
    </div>
  );
}

export default CommentsPage;
