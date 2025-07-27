/**
 * @file CommentsPage.js
 * @description 
 * This component manages the comments and replies section for a submission.
 * It allows participants to:
 *  - View a paginated list of comments and their replies.
 *  - Add new comments and reply to existing comments.
 *  - Edit their own comments and replies.
 *  - Delete their own comments and replies.
 *  - Expand and collapse reply lists for better readability.
 * It integrates with a backend API for CRUD operations, handles pagination, 
 * provides success/error feedback via Snackbars, and uses Material-UI for layout and styling.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Button,
  TextField,
  Typography,
  Snackbar,
  Alert,
  Paper,
  IconButton,
  Pagination,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import EditIcon from "@mui/icons-material/Edit";
import axios from "axios";
import TopBar from "../TopSide/TopBar";
import Sidebar from "../TopSide/Sidebar";
import AddComment from "./AddComment";
import DeleteComment from "./DeleteComment";
import "./CommentsPage.css";

function CommentsPage() {
  const { submissionId } = useParams();
  const navigate = useNavigate();

  const [comments, setComments] = useState([]);
  const [expandedReplies, setExpandedReplies] = useState({});
  const [replyingCommentId, setReplyingCommentId] = useState(null);
  const [replyContent, setReplyContent] = useState("");
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingContent, setEditingContent] = useState("");

  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [snackbarMessage, setSnackbarMessage] = useState("");
  const [snackbarSeverity, setSnackbarSeverity] = useState("success");

  const currentUserId = localStorage.getItem("userId");

  const fetchComments = useCallback(async (pageToFetch = 1, reset = false) => {
    try {
      const res = await axios.get(
        `http://localhost:8080/interactions/comments/list`,
        {
          params: {
            submissionId,
            page: pageToFetch,
            size: 5,
            sortBy: "createdAt",
            order: "desc",
          },
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
        }
      );
      const fetched = res.data.data || [];
      const pages = res.data.pages || 1;
      setTotalPages(pages);
      if (reset) {
        setComments(fetched);
        setPage(pageToFetch);
      } else {
        setComments((prev) => [...prev, ...fetched]);
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

  const handlePageChange = (_event, value) => {
    fetchComments(value, true);
  };

  const handleReplySubmit = (parentCommentId) => {
    if (!replyContent.trim())
      return showSnackbar("Reply cannot be empty.", "error");
    const userId = currentUserId;
    const token = localStorage.getItem("token");
    if (!userId || !token) return showSnackbar("Please log in.", "error");

    axios
      .post(
        `http://localhost:8080/interactions/comments`,
        { submissionId, content: replyContent, parentId: parentCommentId },
        {
          headers: {
            "User-ID": userId,
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      )
      .then(() => {
        setReplyContent("");
        setReplyingCommentId(null);
        fetchComments(1, true);
        showSnackbar("Reply added successfully!");
      })
      .catch((err) => {
        console.error(err);
        showSnackbar("Failed to add reply.", "error");
      });
  };

  const handleStartEdit = (commentId, content) => {
    setEditingCommentId(commentId);
    setEditingContent(content);
  };

  const handleSaveEdit = (commentId) => {
    if (!editingContent.trim())
      return showSnackbar("Content cannot be empty.", "error");
    const userId = currentUserId;
    const token = localStorage.getItem("token");

    axios
      .put(
        `http://localhost:8080/interactions/comments/${commentId}`,
        { submissionId, content: editingContent },
        {
          headers: {
            "User-ID": userId,
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      )
      .then(() => {
        setEditingCommentId(null);
        setEditingContent("");
        fetchComments(1, true);
        showSnackbar("Comment updated successfully!");
      })
      .catch((err) => {
        console.error(err);
        showSnackbar("Failed to update comment.", "error");
      });
  };

  const toggleExpandedReplies = (commentId) =>
    setExpandedReplies((prev) => ({
      ...prev,
      [commentId]: !prev[commentId],
    }));

  const showSnackbar = (message, severity = "success") => {
    setSnackbarMessage(message);
    setSnackbarSeverity(severity);
    setSnackbarOpen(true);
  };

  const handleSnackbarClose = () => setSnackbarOpen(false);
  const handleBack = () => navigate(-1);

  return (
    <div className="comments-page-container">
      <TopBar />
      <div className="comments-page-content">
        <Sidebar />
        <div className="comments-section">
          <div className="Right-comment-place">
            <div className="comments-container">
              <div className="wrapper">
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    marginBottom: "20px",
                  }}
                >
                  <IconButton onClick={handleBack}>
                    <ArrowBackIcon style={{ fontSize: "30px" }} />
                  </IconButton>
                  <Typography
                    variant="body1"
                    sx={{
                      color: "brown",
                      marginLeft: "8px",
                      fontWeight: "bold",
                    }}
                  >
                    Go Back
                  </Typography>
                </div>

                <Typography variant="h4" sx={{ mb: 2, fontWeight: "bold" }}>
                  Comments
                </Typography>

                {comments.length === 0 ? (
                  <Typography>No comments yet.</Typography>
                ) : (
                  comments.map((comment) => {
                    const id = comment._id || comment.id;
                    const isExpanded = expandedReplies[id];
                    const repliesToShow = isExpanded
                      ? comment.replies
                      : comment.replies?.slice(0, 1);

                    return (
                      <Paper
                        key={id}
                        sx={{ marginBottom: 2, padding: 2, position: "relative" }}
                      >
                        {editingCommentId === id ? (
                          <>
                            <TextField
                              fullWidth
                              multiline
                              rows={3}
                              value={editingContent}
                              onChange={(e) =>
                                setEditingContent(e.target.value)
                              }
                              variant="outlined"
                              sx={{ mb: 1 }}
                            />
                            <Button
                              variant="contained"
                              size="small"
                              sx={{
                                mr: 1,
                                backgroundColor: "#FF9800",
                                "&:hover": { backgroundColor: "#e68900" },
                              }}
                              onClick={() => handleSaveEdit(id)}
                            >
                              Save
                            </Button>
                            <Button
                              variant="outlined"
                              size="small"
                              sx={{
                                color: "#FF9800",
                                borderColor: "#FF9800",
                                "&:hover": {
                                  backgroundColor: "rgba(255,152,0,0.1)",
                                  borderColor: "#e68900",
                                  color: "#e68900",
                                },
                              }}
                              onClick={() => setEditingCommentId(null)}
                            >
                              Cancel
                            </Button>
                          </>
                        ) : (
                          <>
                            <Typography
                              variant="subtitle1"
                              sx={{ fontWeight: "bold" }}
                            >
                              {comment.userId === currentUserId
                                ? "My comment"
                                : "Anonymous"}
                            </Typography>
                            <Typography
                              variant="body1"
                              sx={{ marginTop: "5px", marginBottom: "24px" }}
                            >
                              {comment.content}
                            </Typography>
                            <Typography
                              variant="body2"
                              sx={{
                                position: "absolute",
                                bottom: 8,
                                left: 16,
                                color: "gray",
                                fontSize: "12px",
                              }}
                            >
                              {new Date(comment.createdAt).toLocaleString()}
                            </Typography>

                            <Button
                              size="small"
                              variant="text"
                              onClick={() => setReplyingCommentId(id)}
                              sx={{
                                mt: 1,
                                color: "#FF9800",
                                "&:hover": {
                                  backgroundColor: "rgba(255,152,0,0.1)",
                                },
                              }}
                            >
                              Reply
                            </Button>

                            {replyingCommentId === id && (
                              <div style={{ marginTop: "10px" }}>
                                <TextField
                                  fullWidth
                                  multiline
                                  rows={2}
                                  label="Write a reply"
                                  value={replyContent}
                                  onChange={(e) =>
                                    setReplyContent(e.target.value)
                                  }
                                  variant="outlined"
                                />
                                <Button
                                  variant="contained"
                                  sx={{
                                    mt: 1,
                                    backgroundColor: "#FF9800",
                                    "&:hover": { backgroundColor: "#e68900" },
                                  }}
                                  onClick={() => handleReplySubmit(id)}
                                >
                                  Submit Reply
                                </Button>
                              </div>
                            )}

                            {comment.replies && comment.replies.length > 0 && (
                              <div
                                style={{ marginLeft: "12px", marginTop: "4px" }}
                              >
                                {repliesToShow.map((reply) => {
                                  const rid = reply.id;
                                  return (
                                    <Paper
                                      key={rid}
                                      sx={{
                                        marginBottom: 1,
                                        padding: 1,
                                        backgroundColor: "#f9f9f9",
                                        position: "relative",
                                      }}
                                    >
                                      {editingCommentId === rid ? (
                                        <>
                                          <TextField
                                            fullWidth
                                            multiline
                                            rows={2}
                                            value={editingContent}
                                            onChange={(e) =>
                                              setEditingContent(e.target.value)
                                            }
                                            variant="outlined"
                                            sx={{ mb: 1 }}
                                          />
                                          <Button
                                            variant="contained"
                                            size="small"
                                            sx={{
                                              mr: 1,
                                              backgroundColor: "#FF9800",
                                              "&:hover": {
                                                backgroundColor: "#e68900",
                                              },
                                            }}
                                            onClick={() => handleSaveEdit(rid)}
                                          >
                                            Save
                                          </Button>
                                          <Button
                                            variant="outlined"
                                            size="small"
                                            sx={{
                                              color: "#FF9800",
                                              borderColor: "#FF9800",
                                              "&:hover": {
                                                backgroundColor: "rgba(255,152,0,0.1)",
                                                borderColor: "#e68900",
                                                color: "#e68900",
                                              },
                                            }}
                                            onClick={() =>
                                              setEditingCommentId(null)
                                            }
                                          >
                                            Cancel
                                          </Button>
                                        </>
                                      ) : (
                                        <>
                                          <Typography
                                            variant="subtitle2"
                                            sx={{ fontWeight: "bold" }}
                                          >
                                            {reply.userId === currentUserId
                                              ? "My reply"
                                              : "Anonymous"}
                                          </Typography>
                                          <Typography
                                            variant="body2"
                                            sx={{
                                              marginTop: "3px",
                                              marginBottom: "24px",
                                            }}
                                          >
                                            {reply.content}
                                          </Typography>
                                          <Typography
                                            variant="body2"
                                            sx={{
                                              position: "absolute",
                                              bottom: 8,
                                              left: 16,
                                              color: "gray",
                                              fontSize: "11px",
                                            }}
                                          >
                                            {new Date(
                                              reply.createdAt
                                            ).toLocaleString()}
                                          </Typography>
                                          {reply.userId === currentUserId && (
                                            <>
                                              <IconButton
                                                sx={{
                                                  position: "absolute",
                                                  right: 40,
                                                  bottom: 8,
                                                }}
                                                onClick={() =>
                                                  handleStartEdit(
                                                    rid,
                                                    reply.content
                                                  )
                                                }
                                                size="small"
                                              >
                                                <EditIcon fontSize="small" />
                                              </IconButton>
                                              <div
                                                style={{
                                                  position: "absolute",
                                                  right: 16,
                                                  bottom: 8,
                                                }}
                                              >
                                                <DeleteComment
                                                  commentId={rid}
                                                  onDeleted={() =>
                                                    fetchComments(1, true)
                                                  }
                                                />
                                              </div>
                                            </>
                                          )}
                                        </>
                                      )}
                                    </Paper>
                                  );
                                })}
                                {comment.replies.length > 1 && (
                                  <Button
                                    variant="text"
                                    size="small"
                                    onClick={() => toggleExpandedReplies(id)}
                                    sx={{ color: "#FF9800", mt: 1 }}
                                  >
                                    {isExpanded
                                      ? "Hide replies"
                                      : "View more replies"}
                                  </Button>
                                )}
                              </div>
                            )}

                            {comment.userId === currentUserId && (
                              <>
                                <IconButton
                                  sx={{
                                    position: "absolute",
                                    right: 40,
                                    bottom: 8,
                                  }}
                                  onClick={() =>
                                    handleStartEdit(id, comment.content)
                                  }
                                  size="small"
                                >
                                  <EditIcon fontSize="small" />
                                </IconButton>
                                <div
                                  style={{
                                    position: "absolute",
                                    right: 16,
                                    bottom: 8,
                                  }}
                                >
                                  <DeleteComment
                                    commentId={id}
                                    onDeleted={() => fetchComments(1, true)}
                                  />
                                </div>
                              </>
                            )}
                          </>
                        )}
                      </Paper>
                    );
                  })
                )}

                <Pagination
                  count={totalPages}
                  page={page}
                  onChange={handlePageChange}
                  sx={{
                    mt: 2,
                    "& .MuiPaginationItem-root": { color: "#FF9800" },
                    "& .Mui-selected": {
                      backgroundColor: "#FF9800",
                      color: "#fff",
                    },
                  }}
                />

                <AddComment
                  submissionId={submissionId}
                  onCommentPosted={() => fetchComments(1, true)}
                />
              </div>
            </div>
          </div>
        </div>
      </div>

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
    </div>
  );
}

export default CommentsPage;
