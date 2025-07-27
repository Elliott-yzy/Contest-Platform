/**
 * ContestCard.js
 * 
 * This component displays a card representing a contest with details such as title, date, category, and description.
 * It allows users to:
 * - View contest details and navigate to the contest page on click.
 * - Vote or join the contest with a snackbar reminder if not logged in.
 * 
 * Role: Public User
 * Developer: Beiqi Dai
 */


import React, { useState } from "react";
import "./UserContestCard.css";
import {
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Button,
  Typography,
  Snackbar,
  Alert,
} from "@mui/material";
import { Favorite, Flag, Category } from "@mui/icons-material";

function ContestCard({ contest, onCardClick }) {
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "warning" });

  const handleCardClick = () => {
    console.log("Card clicked:", contest);
    onCardClick && onCardClick(contest);
  };

  const handleOpenSnackbar = (e) => {
    e.stopPropagation(); // Prevent bubbles from triggering card jumps
    setSnackbar({
      open: true,
      message: "Please log in before voting or joining the contest!",
      severity: "warning"
    });
  };

  const handleSnackbarClose = (event, reason) => {
    if (event) {
      event.stopPropagation();
    }
    setSnackbar({ ...snackbar, open: false });
  };

  return (
    <>
      <Card
        className="publicuser-contest-card"
        onClick={handleCardClick}
        sx={{ maxWidth: 345, boxShadow: 3, cursor: "pointer" }}
      >
        <CardMedia
          component="img"
          height="200"
          image={contest.image}
          alt={contest.title}
        />

        <CardContent>
          <Typography variant="h6" component="div">
            {contest.title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            <strong>Date:</strong> {contest.date}
          </Typography>
          <Typography
            variant="body2"
            sx={{ display: "flex", alignItems: "center", mt: 1 }}
          >
            <Category sx={{ fontSize: 18, marginRight: 1, color: "gray" }} />
            {contest.category}
          </Typography>
          <Typography
            variant="body2"
            sx={{
              mt: 1,
              overflow: "hidden",
              textOverflow: "ellipsis",
              whiteSpace: "nowrap",
              width: "100%",
            }}
          >
            <span style={{ fontWeight: 'bold', color: '#888' }}>Description:</span> {contest.description}
          </Typography>
        </CardContent>

        <CardActions sx={{ justifyContent: "flex-end" }}>
          <Button
            className="publicuser-vote-button"
            variant="outlined"
            startIcon={<Favorite />}
            onClick={handleOpenSnackbar}
          >
            Vote
          </Button>

          <Button
            className="publicuser-join-button"
            variant="contained"
            startIcon={<Flag />}
            sx={{ ml: 1 }}
            onClick={handleOpenSnackbar}
          >
            Join
          </Button>
        </CardActions>
      </Card>

      {/* ✨ Snackbar for feedback */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      >
        <Alert
          onClose={handleSnackbarClose}
          severity={snackbar.severity}
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </>
  );
}

export default ContestCard;
