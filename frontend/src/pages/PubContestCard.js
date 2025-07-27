/**
 * ContestCard.js
 * 
 * This component represents a public contest card view.
 * It displays the contest's image, title, organizer, date, category, and description.
 * Users can vote for a contest or join it by clicking the corresponding buttons.
 * Voting and joining actions require user authentication and interact with the server API.
 * 
 * Developer: Beiqi Dai
 */


import React from "react";
import "./PubContestCard.css";
import {
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Button,
  Typography,
  IconButton,
} from "@mui/material";
import { Favorite, HowToVote, Flag, Category } from "@mui/icons-material";

function ContestCard({ contest, onCardClick, onLoginRequest }) {
  const handleCardClick = () => {
    console.log("Card clicked:", contest);
    onCardClick && onCardClick(contest);
  };

  const handleVoteClick = async (e) => {
    e.stopPropagation();
    console.log("Vote clicked:", contest);

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Please log in first!");
        return;
      }

      const response = await fetch(
        `http://localhost:5000/api/contest/${contest.id}/votes`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        console.log("Vote success:", data);
        alert("Vote success!");
      } else {
        const errData = await response.json();
        console.error("Failed to vote:", errData);

        if (errData.error === "Already voted") {
          alert("You have already voted!");
        } else {
          alert("Vote failed, please check the console.");
        }
      }
    } catch (error) {
      console.error("Error voting:", error);
      alert("Vote failed, network or server error.");
    }
  };

  const handleJoinClick = async (e) => {
    e.stopPropagation();
    console.log("Join clicked:", contest);

    const token = null;

    console.log("Token from localStorage:", token);

    if (!token) {
      if (onLoginRequest) {
        onLoginRequest();
      } else {
        alert("Please log in first!");
      }
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:5000/api/contest/${contest.id}/join`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        const data = await response.json();
        console.log("Join success:", data);
        alert("Join success!");
      } else {
        const errData = await response.json();
        console.error("Failed to join contest:", errData);

        if (errData.error === "Already JOIN!") {
          alert("You have already joined!");
        } else {
          alert("Join failed, please check the console.");
        }
      }
    } catch (error) {
      console.error("Error joining contest:", error);
      alert("Join failed, network or server error.");
    }
  };

  return (
    <Card
      className="contest-card"
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
        <Typography variant="subtitle2" color="text.secondary">
          <strong>Organizer:</strong> {contest.organizer}
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
        <Typography variant="body2" sx={{ mt: 1 }}>
          {contest.description}
        </Typography>
      </CardContent>

      <CardActions sx={{ justifyContent: "space-between" }}>
        <IconButton color="primary" onClick={handleVoteClick}>
          <HowToVote />
          <Typography variant="body2" sx={{ ml: 0.5 }}>
            {contest.votes}
          </Typography>
        </IconButton>
        <div>
          <Button
            className="vote-button"
            variant="outlined"
            startIcon={<Favorite />}
            onClick={handleVoteClick}
          >
            Vote
          </Button>

          <Button
            className="join-button"
            variant="contained"
            startIcon={<Flag />}
            sx={{ ml: 1 }}
            onClick={handleJoinClick}
          >
            Join
          </Button>
        </div>
      </CardActions>
    </Card>
  );
}

export default ContestCard;
