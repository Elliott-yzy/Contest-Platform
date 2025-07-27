/**
 * @file OrganizerAddJudge.js
 * @description
 * This component allows organizers to manage the judges of a specific competition.
 * Organizers can:
 *  - Add one or multiple judges by email (comma-separated).
 *  - Prevent participants of the competition from being assigned as judges.
 *  - View a paginated list of assigned judges.
 *  - Delete a judge assignment.
 * 
 * It fetches competition details, current judges, and participant lists to handle conflict checks.
 * Material-UI components are used for the interface and feedback (Snackbar/Alert).
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import { useEffect, useState, useCallback } from "react";
import {
  Typography,
  TextField,
  Button,
  Paper,
  TableContainer,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Snackbar,
  Alert,
  Pagination,
} from "@mui/material";
import { useParams, useNavigate } from "react-router-dom";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import "./ContestList.css";

function OrganizerAddJudge() {
  const { competitionId } = useParams();
  const navigate = useNavigate();
  const email = localStorage.getItem("email");
  const token = localStorage.getItem("token");
  const userId = localStorage.getItem("userId");
  const role = localStorage.getItem("role");

  const [judgeEmail, setJudgeEmail] = useState("");
  const [judges, setJudges] = useState([]);
  const [competitionName, setCompetitionName] = useState("");
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });

  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const [participantsEmails, setParticipantsEmails] = useState(new Set());

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch(`http://localhost:8080/competitions/${competitionId}`);
        const data = await res.json();
        if (res.ok) setCompetitionName(data.name || "Unnamed Competition");
      } catch (err) {
        console.error("Error fetching competition name:", err);
      }
    })();
  }, [competitionId]);

  const fetchJudges = useCallback(async (currentPage = 1) => {
    try {
      const res = await fetch(
        `http://localhost:8080/competitions/${competitionId}/judges?page=${currentPage}&size=10`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
        }
      );
      const data = await res.json();
      if (res.ok) {
        setJudges(data.data || []);
        setTotalPages(data.pages || 1);
      } else {
        console.error("Failed to fetch judges:", data);
      }
    } catch (err) {
      console.error("Error fetching judges:", err);
    }
  }, [competitionId, token, userId, role]);

  useEffect(() => {
    fetchJudges(page);
  }, [competitionId, page, fetchJudges]);

  const fetchParticipants = useCallback(async () => {
    try {
      const res = await fetch(
        `http://localhost:8080/registrations/${competitionId}/participants?page=1&size=10000`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
        }
      );
      const data = await res.json();
      if (res.ok) {
        const emailsSet = new Set((data.data || []).map((p) => p.email));
        setParticipantsEmails(emailsSet);
      } else {
        console.error("Failed to fetch participants:", data);
      }
    } catch (err) {
      console.error("Error fetching participants:", err);
    }
  }, [competitionId, token, userId, role]);

  useEffect(() => {
    fetchParticipants();
  }, [competitionId, fetchParticipants]);

  const handleAddJudge = async () => {
    const trimmedEmails = judgeEmail
      .split(",")
      .map((e) => e.trim())
      .filter((e) => e);

    if (trimmedEmails.length === 0) {
      setSnackbar({ open: true, message: "Judge email(s) cannot be empty", severity: "warning" });
      return;
    }

    const conflict = trimmedEmails.find((e) => participantsEmails.has(e));
    if (conflict) {
      setSnackbar({
        open: true,
        message: `❌ ${conflict} is already a participant in this competition and cannot be assigned as a judge.`,
        severity: "error",
      });
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/competitions/${competitionId}/assign-judges`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
          body: JSON.stringify({ judgeEmails: trimmedEmails }),
        }
      );

      const text = await res.text();
      if (res.ok) {
        setSnackbar({ open: true, message: "✅ " + text, severity: "success" });
        setJudgeEmail("");
        fetchJudges(page);
      } else {
        let errorMessage = text;
        try {
          const parsed = JSON.parse(text);
          if (parsed.error) errorMessage = "❌ " + parsed.error;
        } catch { }
        setSnackbar({ open: true, message: errorMessage, severity: "error" });
      }
    } catch (err) {
      console.error("Error assigning judge:", err);
      setSnackbar({ open: true, message: "❌ Error assigning judge", severity: "error" });
    }
  };

  const handleDeleteJudge = async (judgeId) => {
    if (!window.confirm("Are you sure you want to remove this judge?")) return;

    try {
      const res = await fetch(
        `http://localhost:8080/competitions/${competitionId}/judges/${judgeId}`,
        {
          method: "DELETE",
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": role.toUpperCase(),
          },
        }
      );
      const msg = await res.text();
      if (res.ok) {
        setSnackbar({ open: true, message: "✅ " + msg, severity: "success" });
        fetchJudges(page);
      } else {
        setSnackbar({ open: true, message: "❌ Failed to delete: " + msg, severity: "error" });
      }
    } catch (err) {
      console.error("Error deleting judge:", err);
      setSnackbar({ open: true, message: "❌ Error deleting judge", severity: "error" });
    }
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <Typography variant="h5" gutterBottom>
            Judges for: {competitionName}
          </Typography>

          <div style={{ display: "flex", gap: "1rem", marginBottom: "1rem" }}>
            <TextField
              label="Judge Email(s)"
              variant="outlined"
              value={judgeEmail}
              onChange={(e) => setJudgeEmail(e.target.value)}
              fullWidth
              helperText="You can enter multiple emails separated by commas"
            />
            <Button variant="contained" color="primary" onClick={handleAddJudge}>
              Add Judge
            </Button>
          </div>

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Delete</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {judges.map((judge, index) => (
                  <TableRow key={judge.id || index}>
                    <TableCell>{(page - 1) * 10 + index + 1}</TableCell>
                    <TableCell>{judge.email}</TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        color="error"
                        variant="outlined"
                        onClick={() => handleDeleteJudge(judge.id)}
                      >
                        Delete
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <Pagination
            sx={{ mt: 2 }}
            count={totalPages}
            page={page}
            onChange={(e, val) => setPage(val)}
            color="primary"
          />

          <Button
            variant="outlined"
            sx={{ mt: 2 }}
            onClick={() => navigate(`/OrganizerContestList/${email}`)}
          >
            🔙 Back to Contest List
          </Button>

          <Snackbar
            open={snackbar.open}
            autoHideDuration={4000}
            onClose={() => setSnackbar({ ...snackbar, open: false })}
          >
            <Alert severity={snackbar.severity} sx={{ width: "100%" }}>
              {snackbar.message}
            </Alert>
          </Snackbar>
        </div>
      </div>
    </>
  );
}

export default OrganizerAddJudge;
