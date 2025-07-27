/**
 * @file ContestCard.js
 * @description 
 * This component displays a single contest in a card format for participants.
 * Participants can:
 *  - View basic contest information (title, date, category, description, status).
 *  - Join contests as an individual or as a team depending on the participation type.
 *  - Cancel their registration if already registered.
 *  - View approved submissions for a contest.
 * The component manages registration, cancellation, and team selection with backend API integration,
 * handles authentication, and provides user feedback via dialogs and snackbars.
 * It uses Material-UI components for styling and layout.
 * 
 * Role: Participant
 * Developer: Zhaoyi Yang, Beiqi Dai
 */

import React, { useState } from "react";
import "./ContestCard.css";
import {
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Button,
  Typography,
  Stack,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar,
  Alert,
  List,
  ListItem,
  ListItemText,
  CircularProgress,
  Box,
} from "@mui/material";
import { Flag, Category, Visibility, AccessTime, PlayArrow, Lock } from "@mui/icons-material";
import { useNavigate } from "react-router-dom";

function ContestCard({ contest, onLoginRequest }) {
  const navigate = useNavigate();
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });
  const [openRegDialog, setOpenRegDialog] = useState(false);
  const [openTeamDialog, setOpenTeamDialog] = useState(false);
  const [createdTeams, setCreatedTeams] = useState([]);
  const [teamsLoading, setTeamsLoading] = useState(false);

  const showSnackbar = (message, severity = "success") => {
    console.log(`Snackbar: [${severity}] ${message}`);
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = (event, reason) => {
    if (event) event.stopPropagation();
    setSnackbar({ ...snackbar, open: false });
  };

  const checkTeamStatus = async (teamId) => {
    const token = localStorage.getItem("token");
    try {
      const res = await fetch(
        `http://localhost:8080/registrations/teams/${contest.id}/${teamId}/status`,
        {
          method: "GET",
          headers: { "Content-Type": "application/x-www-form-urlencoded", Authorization: `Bearer ${token}` },
        }
      );
      console.log('Check team status response:', res.status);
      if (res.ok) {
        const text = await res.text();
        console.log('Team status:', text);
        return text === 'true';
      }
      console.error('Error checking team status:', res.status);
      return false;
    } catch (err) {
      console.error('Network error checking team status:', err);
      return false;
    }
  };

  const fetchCreatedTeams = async () => {
    const userId = localStorage.getItem("userId");
    console.log('Fetching teams for user:', userId);
    setTeamsLoading(true);
    try {
      const token = localStorage.getItem("token");
      const res = await fetch(
        `http://localhost:8080/teams/public/created?userId=${userId}&page=1&size=100`,
        { headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/x-www-form-urlencoded" } }
      );
      console.log('Fetch created teams status:', res.status);
      if (res.ok) {
        const result = await res.json();
        console.log('Created teams:', result.data);
        setCreatedTeams(result.data || []);
        return result.data || [];
      } else {
        const errText = await res.text();
        console.error('Teams API error:', errText);
        showSnackbar("Failed to load your teams.", "error");
      }
    } catch (err) {
      console.error('Network error fetching teams:', err);
      showSnackbar("Network error fetching your teams.", "error");
    } finally {
      setTeamsLoading(false);
    }
    return [];
  };

  const registerTeam = async (teamId) => {
    console.log('Register team', { competitionId: contest.id, teamId });
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");
    try {
      const res = await fetch(
        `http://localhost:8080/registrations/teams/${contest.id}/${teamId}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "User-ID": userId,
            "User-Role": userRole,
            Authorization: `Bearer ${token}`,
          },
          body: "",
        }
      );
      console.log('Team registration status:', res.status);
      if (res.ok) showSnackbar("Team registered successfully!", "success");
      else {
        const errText = await res.text();
        console.error('Team registration failed:', errText);
        showSnackbar("Team registration failed.", "error");
      }
    } catch (err) {
      console.error('Error in team registration:', err);
      showSnackbar("Network or server error.", "error");
    }
  };

  const cancelTeamRegistration = async (teamId) => {
    console.log('Cancel team registration', { competitionId: contest.id, teamId });
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");
    try {
      const res = await fetch(
        `http://localhost:8080/registrations/teams/${contest.id}/${teamId}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "User-ID": userId,
            "User-Role": userRole,
            Authorization: `Bearer ${token}`,
          },
        }
      );
      console.log('Cancel team status:', res.status);
      if (res.ok) showSnackbar("Team registration cancelled!", "success");
      else {
        const errText = await res.text();
        console.error('Cancel team registration failed:', errText);
        showSnackbar("Team cancellation failed.", "error");
      }
    } catch (err) {
      console.error('Error cancelling team registration:', err);
      showSnackbar("Network or server error.", "error");
    } finally {
      setOpenTeamDialog(false);
    }
  };

  const handleJoinClick = async (e) => {
    e.stopPropagation();
    console.log('Join click:', contest.id, contest.participationType);

    if (contest.status !== "ONGOING") {
      showSnackbar("You can only join ongoing competitions.", "warning");
      return;
    }

    const token = localStorage.getItem("token");
    if (!token) {
      console.log('User not logged in');
      if (onLoginRequest) onLoginRequest();
      else showSnackbar("Please log in first!", "warning");
      return;
    }

    if (contest.participationType === "TEAM") {
      await fetchCreatedTeams();
      setOpenTeamDialog(true);
      return;
    }

    console.log('Registering individual');
    try {
      const userId = localStorage.getItem("userId");
      const userRole = localStorage.getItem("userRole");
      const res = await fetch(
        `http://localhost:8080/registrations/${contest.id}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "User-ID": userId,
            "User-Role": userRole,
            Authorization: `Bearer ${token}`,
          },
          body: "",
        }
      );
      console.log('Individual registration status:', res.status);
      if (res.ok) showSnackbar("Registration successful!", "success");
      else {
        const text = await res.text();
        console.warn('Registration error:', text);
        if (text.includes("already registered")) setOpenRegDialog(true);
        else showSnackbar("Registration failed.", "error");
      }
    } catch (err) {
      console.error('Error registering individual:', err);
      showSnackbar("Network error during registration.", "error");
    }
  };

  const handleCancelRegistration = async () => {
    console.log('Cancel individual registration', contest.id);
    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");
      const userRole = localStorage.getItem("userRole");
      const res = await fetch(
        `http://localhost:8080/registrations/${contest.id}`,
        { method: "DELETE", headers: { "Content-Type": "application/x-www-form-urlencoded", "User-ID": userId, "User-Role": userRole, Authorization: `Bearer ${token}` } }
      );
      console.log('Cancel status:', res.status);
      if (res.ok) {
        showSnackbar("Cancelled successfully!", "success");
        setOpenRegDialog(false);
      } else showSnackbar("Cancellation failed.", "error");
    } catch (err) {
      console.error('Error cancelling:', err);
      showSnackbar("Network error.", "error");
    }
  };

  const handleViewSubmission = async (e) => {
    e.stopPropagation();

    if (!contest?.id) {
      console.warn("Contest ID is undefined, cannot fetch submissions.");
      showSnackbar("Invalid contest ID.", "error");
      return;
    }

    try {
      const res = await fetch(
        `http://localhost:8080/submissions/public/approved?competitionId=${contest.id}`
      );

      console.log('📥 Fetch submissions status:', res.status);

      if (!res.ok) {
        const text = await res.text();
        console.warn("Backend returned an error:", text);
        showSnackbar("Failed to fetch submissions.", "error");
        return;
      }

      const result = await res.json();
      const submissions = result.data || [];
      if (submissions.length === 0) {
        showSnackbar("No approved submissions yet.", "info");
        return;
      }
      const url = `/view-submission/${contest.id}`;
      navigate(url);
    } catch (err) {
      console.error('Error fetching submissions:', err);
      showSnackbar("Network error fetching submissions.", "error");
    }
  };
  

  return (
    <>
      <Card
        className="contest-card"
        onClick={() => navigate(`/contest-detail/${contest.id}`)}
        sx={{ maxWidth: 345, boxShadow: 3, cursor: "pointer" }}
      >
        <CardMedia component="img" height="200" image={contest.image} alt={contest.title} />
        <CardContent>
          <Box
            sx={{
              display: "inline-flex",
              alignItems: "center",
              backgroundColor:
                contest.status === "ONGOING"
                  ? "#FF9800"
                  : contest.status === "UPCOMING"
                    ? "#BDBDBD"
                    : "#9E9E9E",
              color: "white",
              padding: "4px 10px",
              borderRadius: "20px",
              fontSize: "0.8rem",
              fontWeight: "bold",
              mb: 1,
            }}
          >
            {contest.status === "ONGOING" && <PlayArrow sx={{ fontSize: 16, mr: 1 }} />}
            {contest.status === "UPCOMING" && <AccessTime sx={{ fontSize: 16, mr: 1 }} />}
            {contest.status === "COMPLETED" && <Lock sx={{ fontSize: 16, mr: 1 }} />}
            {contest.status}
          </Box>

          <Typography
            variant="h6"
            sx={{ mt: 1, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", width: "87%" }}
          >
            {contest.title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            <strong>Date:</strong> {contest.date}
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            <Category sx={{ fontSize: 18, marginRight: 1, color: "gray" }} /> {contest.category}
          </Typography>
          <Typography
            variant="body2"
            sx={{ mt: 1, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", width: "100%" }}
          >
            <span style={{ fontWeight: "bold", color: "#888" }}>Description:</span> {contest.description}
          </Typography>
        </CardContent>
        <CardActions>
          <Stack direction="row" spacing={1}>
            <Button
              variant="contained"
              startIcon={<Flag />}
              onClick={handleJoinClick}
              sx={{ bgcolor: "orange", "&:hover": { bgcolor: "darkorange" } }}
            >
              Join
            </Button>
            <Button
              variant="outlined"
              startIcon={<Visibility />}
              onClick={handleViewSubmission}
              sx={{
                color: "orange",
                borderColor: "orange",
                "&:hover": { bgcolor: "orange", color: "white" },
              }}
            >
              View Submission
            </Button>
          </Stack>
        </CardActions>
      </Card>

      <Dialog open={openRegDialog} onClose={() => setOpenRegDialog(false)} onClick={(e) => e.stopPropagation()}>
        <DialogTitle>Already Registered</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You have already registered for this competition. Cancel?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenRegDialog(false)} sx={{ color: "orange" }}>
            No
          </Button>
          <Button
            onClick={handleCancelRegistration}
            sx={{ bgcolor: "orange", "&:hover": { bgcolor: "darkorange" } }}
            autoFocus
          >
            Yes
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openTeamDialog} onClose={() => setOpenTeamDialog(false)} onClick={(e) => e.stopPropagation()}>
        <DialogTitle>Select Your Team</DialogTitle>
        <DialogContent>
          {teamsLoading ? (
            <CircularProgress />
          ) : createdTeams.length === 0 ? (
            <DialogContentText>You have no teams. Please create one first.</DialogContentText>
          ) : (
            <List>
              {createdTeams.map((team) => (
                <ListItem key={team.id} sx={{ flexDirection: "column", alignItems: "flex-start" }}>
                  <ListItemText primary={team.name} />
                  <Box sx={{ display: "flex", gap: 1, mt: 1 }}>
                    <Button
                      variant="outlined"
                      size="small"
                      sx={{
                        color: "orange",
                        borderColor: "orange",
                        "&:hover": { bgcolor: "orange", color: "white" },
                      }}
                      onClick={async () => {
                        const registered = await checkTeamStatus(team.id);
                        if (registered) {
                          showSnackbar("Team already registered.", "info");
                        } else {
                          registerTeam(team.id);
                        }
                      }}
                    >
                      Register
                    </Button>
                    <Button
                      variant="outlined"
                      size="small"
                      sx={{
                        color: "orange",
                        borderColor: "orange",
                        "&:hover": { bgcolor: "orange", color: "white" },
                      }}
                      onClick={async () => {
                        const registered = await checkTeamStatus(team.id);
                        if (!registered) {
                          showSnackbar("Team not registered yet.", "warning");
                        } else {
                          cancelTeamRegistration(team.id);
                        }
                      }}
                    >
                      Cancel Reg
                    </Button>
                  </Box>
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenTeamDialog(false)} sx={{ color: "orange" }}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert onClose={handleSnackbarClose} severity={snackbar.severity} sx={{ width: "100%" }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
}

export default ContestCard;