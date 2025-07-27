/**
 * @file ContestCard.js
 * @description 
 * This component renders a contest card on the homepage, displaying basic contest information.
 * It allows users to:
 *  - Click the card to view contest details.
 *  - Vote for a contest (requires login and backend API interaction).
 *  - Join a contest (requires login and backend API interaction).
 * 
 * The card displays the contest's title, organizer, date, category, description, image, and current vote count.
 * It handles user interactions such as voting and joining, including frontend feedback and backend communication.
 * Material-UI components are used for layout, styling, and interactive elements.
 * 
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React from "react";
import "./ContestCard.css";
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

function ContestCard({ contest, onCardClick }) {
  // Click event for the entire card
  const handleCardClick = () => {
    console.log("Card clicked:", contest);
    onCardClick && onCardClick(contest);
  };

  // Click event for Vote button: call backend API
  const handleVoteClick = async (e) => {
    e.stopPropagation(); // Prevent event bubbling
    console.log("Vote clicked:", contest);

    try {
      // Get token from localStorage (replace as needed)
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Please log in first!");
        return;
      }

      // Send POST request to /api/contest/:id/votes
      const response = await fetch(
        `http://localhost:8080/api/contest/${contest.id}/votes`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        // If the response is 2xx
        const data = await response.json();
        console.log("Vote success:", data);
        alert("Vote successful!");
      } else {
        // If not a 2xx response, parse error message
        const errData = await response.json();
        console.error("Failed to vote:", errData);

        // If backend returns { error: "Already voted" }
        if (errData.error === "Already voted") {
          alert("You have already voted!");
        } else {
          // Other errors
          alert("Voting failed, please check the console");
        }
      }
    } catch (error) {
      console.error("Error voting:", error);
      alert("Voting failed due to network or server error");
    }
  };

  // Click event for Join button: call backend API
  const handleJoinClick = async (e) => {
    e.stopPropagation(); // Prevent event bubbling
    console.log("Join clicked:", contest);

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        alert("Please log in first!");
        return;
      }

      const response = await fetch(
        `http://localhost:8080/api/contest/${contest.id}/join`,
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
        alert("Joined successfully!");
      } else {
        const errData = await response.json();
        console.error("Failed to join contest:", errData);

        // If backend returns { error: "Already JOIN!" }
        if (errData.error === "Already JOIN!") {
          alert("You have already joined!");
        } else {
          alert("Joining failed, please check the console");
        }
      }
    } catch (error) {
      console.error("Error joining contest:", error);
      alert("Joining failed due to network or server error");
    }
  };

  return (
    <Card
      className="homepage-contest-card"
      onClick={handleCardClick}
      sx={{ maxWidth: 345, boxShadow: 3, cursor: "pointer" }}
    >
      <CardMedia
        component="img"
        height="200"
        image={contest.image}
        alt={contest.title}
      />
  
      <CardContent className="homepage-card-body">
        <Typography variant="h6" component="div" className="homepage-title">
          {contest.title}
        </Typography>
        <Typography variant="subtitle2" color="text.secondary" className="homepage-organizer">
          <strong>Organizer:</strong> {contest.organizer}
        </Typography>
        <Typography variant="body2" color="text.secondary" className="homepage-date">
          <strong>Date:</strong> {contest.date}
        </Typography>
        <Typography
          variant="body2"
          sx={{ display: "flex", alignItems: "center", mt: 1 }}
        >
          <Category sx={{ fontSize: 18, marginRight: 1, color: "gray" }} />
          {contest.category}
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }} className="homepage-description">
          {contest.description}
        </Typography>
      </CardContent>
  
      <CardActions className="homepage-card-footer">
        {/* Voting icon + vote count */}
        <div className="homepage-vote-info">
          <IconButton color="primary" onClick={handleVoteClick}>
            <HowToVote />
            <Typography variant="body2" sx={{ ml: 0.5 }} className="homepage-vote-count">
              {contest.votes}
            </Typography>
          </IconButton>
        </div>
  
        {/* Button group: Vote + Join */}
        <div className="homepage-button-group">
          <Button
            className="homepage-vote-button"
            variant="outlined"
            startIcon={<Favorite />}
            onClick={handleVoteClick}
          >
            Vote
          </Button>
  
          <Button
            className="homepage-join-button"
            variant="contained"
            startIcon={<Flag />}
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
