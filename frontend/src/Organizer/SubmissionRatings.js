/**
 * @file SubmissionRatings.js
 * @description
 * Display submission scores in a sortable table for a specific competition.
 * Allows Organizer to:
 *  - View and compare total and individual criterion scores
 *  - Sort submissions by total or specific criterion
 *  - Trigger automatic winner award based on scores
 * 
 * Features:
 *  - Dynamic sortable table
 *  - Auto Award function
 *  - Loading state management
 *  - Full use of MUI (Material-UI) components
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useEffect, useState, useMemo, useCallback } from "react";
import {
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TableContainer,
  Paper,
  CircularProgress,
  Box,
  Button,
  TableSortLabel,
} from "@mui/material";
import { useNavigate, useParams } from "react-router-dom";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";

function SubmissionRatings() {
  const { competitionId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [submissions, setSubmissions] = useState([]);
  const [sortConfig, setSortConfig] = useState({ key: "totalScore", direction: "desc" });
  const userId = localStorage.getItem("userId");
  const role = localStorage.getItem("role");
  const token = localStorage.getItem("token");

  const fetchRatings = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetch(
        `http://localhost:8080/winners/scored-list?competitionId=${competitionId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
        }
      );
      const data = await res.json();
      if (res.ok && Array.isArray(data.data)) {
        setSubmissions(data.data);
      } else {
        console.error("Failed to fetch ratings:", data);
      }
    } catch (err) {
      console.error("Error fetching ratings:", err);
    } finally {
      setLoading(false);
    }
  }, [competitionId, token, userId, role]);

  useEffect(() => {
    fetchRatings();
  }, [fetchRatings]);

  const allCriteria = useMemo(() => {
    return Array.from(
      new Set(
        submissions.flatMap((sub) =>
          sub.criterionScores ? Object.keys(sub.criterionScores) : []
        )
      )
    );
  }, [submissions]);

  const sortedSubmissions = useMemo(() => {
    const sorted = [...submissions];
    const { key, direction } = sortConfig;

    sorted.sort((a, b) => {
      let aValue, bValue;

      if (key === "totalScore") {
        aValue = a.totalScore ?? 0;
        bValue = b.totalScore ?? 0;
      } else {
        aValue = a.criterionScores?.[key] ?? 0;
        bValue = b.criterionScores?.[key] ?? 0;
      }

      if (aValue < bValue) return direction === "asc" ? -1 : 1;
      if (aValue > bValue) return direction === "asc" ? 1 : -1;
      return 0;
    });

    return sorted;
  }, [submissions, sortConfig]);

  const handleSort = (key) => {
    setSortConfig((prev) => ({
      key,
      direction: prev.key === key && prev.direction === "asc" ? "desc" : "asc",
    }));
  };

  const handleAutoAward = async () => {
    try {
      const res = await fetch(
        `http://localhost:8080/winners/auto-award?competitionId=${competitionId}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
        }
      );

      if (res.status === 200) {
        alert("🎉 Auto-award completed successfully!");
        fetchRatings(); // refresh the data
      } else if (res.status === 400) {
        alert("⚠️ No scored submissions found.");
      } else if (res.status === 403) {
        alert("❌ You are not authorized to award.");
      } else {
        alert("❌ Unexpected error during auto-award.");
      }
    } catch (err) {
      console.error("Auto award failed:", err);
      alert("❌ Failed to connect to server.");
    }
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <Typography variant="h5" gutterBottom>
            Rated Submissions Comparison
          </Typography>

          {loading ? (
            <Box sx={{ textAlign: "center", mt: 5 }}>
              <CircularProgress />
            </Box>
          ) : sortedSubmissions.length > 0 ? (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell><strong>Title</strong></TableCell>
                    <TableCell sortDirection={sortConfig.key === "totalScore" ? sortConfig.direction : false}>
                      <TableSortLabel
                        active={sortConfig.key === "totalScore"}
                        direction={sortConfig.key === "totalScore" ? sortConfig.direction : "asc"}
                        onClick={() => handleSort("totalScore")}
                      >
                        <strong>Total Score</strong>
                      </TableSortLabel>
                    </TableCell>
                    {allCriteria.map((criterion) => (
                      <TableCell
                        key={criterion}
                        sortDirection={sortConfig.key === criterion ? sortConfig.direction : false}
                      >
                        <TableSortLabel
                          active={sortConfig.key === criterion}
                          direction={sortConfig.key === criterion ? sortConfig.direction : "asc"}
                          onClick={() => handleSort(criterion)}
                        >
                          <strong>{criterion}</strong>
                        </TableSortLabel>
                      </TableCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sortedSubmissions.map((sub) => (
                    <TableRow key={sub.submissionId}>
                      <TableCell>{sub.title}</TableCell>
                      <TableCell>{sub.totalScore}</TableCell>
                      {allCriteria.map((criterion) => (
                        <TableCell key={criterion}>
                          {sub.criterionScores?.[criterion] ?? "-"}
                        </TableCell>
                      ))}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Typography variant="body1" sx={{ mt: 4 }}>
              No scored submissions found for this competition.
            </Typography>
          )}

          <Button
            variant="contained"
            color="success"
            onClick={handleAutoAward}
          >
            🏆 Auto Award Winners
          </Button>

          <Button
            variant="outlined"
            onClick={() => navigate(`/OrganizerSubmissions/${competitionId}`)}
          >
            🔙 Back to Submissions List
          </Button>

        </div>
      </div>
    </>
  );
}

export default SubmissionRatings;
