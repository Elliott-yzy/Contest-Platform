/**
 * WorkList.js
 * 
 * This component displays the approved works for a given competition.
 * It includes:
 * - Search functionality for filtering works by title/description
 * - A table with work title, description, type, file link, votes count, vote button, comment button
 * - Loading spinner while fetching
 * 
 * Role: Public User
 * Developer: Zhaoyi Yang
 */

import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  IconButton,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  CircularProgress,
  Snackbar,
} from "@mui/material";
import { Search, PictureAsPdf, Image, Code, InsertDriveFile } from "@mui/icons-material";
import Navbar from "../Homepages/Navbar";
import Footer from "../Homepages/Footer";
import { useLocation, useNavigate } from "react-router-dom";
import "./WorkList.css";

function WorkList() {
  const location = useLocation();
  const navigate = useNavigate();
  const [competitionId, setCompetitionId] = useState("");
  const [works, setWorks] = useState([]);
  const [filteredWorks, setFilteredWorks] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [loading, setLoading] = useState(true);
  const [snackbarOpen, setSnackbarOpen] = useState(false);

  useEffect(() => {
    const queryParams = new URLSearchParams(location.search);
    const id = queryParams.get("competitionId");
    if (id) {
      setCompetitionId(id);
    }
  }, [location.search]);

  useEffect(() => {
    async function fetchWorks() {
      if (!competitionId) return;
      setLoading(true);
      try {
        const res = await fetch(`http://localhost:8080/submissions/public/approved?competitionId=${competitionId}`);
        const result = await res.json();
        const mappedWorks = await Promise.all(result.data.map(async (item, idx) => {
          const voteRes = await fetch(`http://localhost:8080/interactions/votes/count?submissionId=${item.id}`);
          const voteCount = voteRes.ok ? await voteRes.text() : "0";
          return {
            id: item.id || String(idx),
            title: item.title || `Work ${idx}`,
            description: item.description || "No description.",
            fileType: item.fileType || "",
            fileUrl: item.fileUrl || "",
            voteCount: parseInt(voteCount, 10) || 0
          };
        }));
        setWorks(mappedWorks);
        setFilteredWorks(mappedWorks); // 初始显示全部
      } catch (err) {
        console.error("Error fetching works:", err);
      } finally {
        setLoading(false);
      }
    }
    fetchWorks();
  }, [competitionId]);

  const handleSearchClick = () => {
    console.log("[Search] searchInput:", searchInput);
    const term = searchInput.trim().toLowerCase();
    if (!term) {
      setFilteredWorks(works); // 输入为空，恢复全量
    } else {
      const filtered = works.filter(w =>
        w.title.toLowerCase().includes(term) || w.description.toLowerCase().includes(term)
      );
      setFilteredWorks(filtered);
    }
  };

  const handleVoteClick = (work) => {
    console.log("Vote clicked:", work);
    setSnackbarOpen(true); // 这里可以接入真正的投票逻辑
  };

  const handleSnackbarClose = () => setSnackbarOpen(false);

  const handleViewCommentClick = (id) => {
    navigate(`/publicusercoments/${id}`);
  };

  const getFileTypeIcon = (type) => {
    if (type.startsWith("image/")) return <Image />;
    if (type === "application/pdf") return <PictureAsPdf />;
    if (type.includes("code") || type.includes("json") || type.includes("python")) return <Code />;
    return <InsertDriveFile />;
  };

  if (!competitionId) return null;

  return (
    <>
      <Navbar />
      <div className="work-wrapper">
        <div className="work-container">
          <Typography
            variant="h4"
            align="center"
            gutterBottom
            fontWeight="bold"
            sx={{ marginTop: "20px", marginBottom: "30px" }}
          >
            Approved Submissions
          </Typography>

          {/* Search bar */}
          <Box display="flex" justifyContent="center" alignItems="center" sx={{ mb: 3 }}>
            <TextField
              variant="outlined"
              placeholder="Search by title or description..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              size="small"
              sx={{ width: "300px", mr: 2 }}
            />
            <IconButton onClick={handleSearchClick}>
              <Search />
            </IconButton>
          </Box>

          {/* Table */}
          {loading ? (
            <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <TableContainer component={Paper} sx={{ maxWidth: "90%", margin: "auto", mt: 4, backgroundColor: "#fff", boxShadow: 3, borderRadius: 2 }}>
              <Table>
                <TableHead>
                  <TableRow sx={{ backgroundColor: "#FF9800" }}>
                    <TableCell align="center" sx={{ color: "white" }}>Title</TableCell>
                    <TableCell align="center" sx={{ color: "white" }}>Description</TableCell>
                    <TableCell align="center" sx={{ color: "white" }}>Type</TableCell>
                    <TableCell align="center" sx={{ color: "white" }}>View File</TableCell>
                    <TableCell align="center" sx={{ color: "white" }}>Votes count</TableCell>
                    <TableCell align="center" sx={{ color: "white" }}>Vote</TableCell>
                    <TableCell align="center" sx={{ color: "white" }}>Comment</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredWorks.map((w) => (
                    <TableRow key={w.id}>
                      <TableCell align="center">{w.title}</TableCell>
                      <TableCell align="center">{w.description}</TableCell>
                      <TableCell align="center">{getFileTypeIcon(w.fileType)}</TableCell>
                      <TableCell align="center">
                        <Button
                          variant="contained"
                          color="warning"
                          size="small"
                          href={w.fileUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          View
                        </Button>
                      </TableCell>
                      <TableCell align="center">{w.voteCount}</TableCell>
                      <TableCell align="center">
                        <Button
                          variant="contained"
                          color="warning"
                          size="small"
                          onClick={() => handleVoteClick(w)}
                        >
                          Vote
                        </Button>
                      </TableCell>
                      <TableCell align="center">
                        <Button
                          variant="outlined"
                          size="small"
                          onClick={() => handleViewCommentClick(w.id)}
                          sx={{
                            color: "#FF9800",
                            borderColor: "#FF9800",
                            "&:hover": {
                              backgroundColor: "rgba(255,152,0,0.08)",
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

      {/* Snackbar for Vote clicked */}
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={1000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Box sx={{ bgcolor: "orange", color: "white", px: 2, py: 1, borderRadius: 1, fontWeight: "bold" }}>
          Please login first.
        </Box>
      </Snackbar>

      <Footer />
    </>
  );
}

export default WorkList;
