/**
 * ContestDetail.js
 * 
 * This component displays the detailed view of a contest. It includes:
 * - Contest information such as title, description, organizer, date, and category.
 * - A modal for login when the user is not authenticated and tries to perform certain actions.
 * - Buttons to view the contest details and to log in.
 * - A close button to exit the contest detail view.
 * 
 * Role: Public User
 * Developer: Beiqi Dai
 */


// ContestDetail.js
import React, { useState } from "react";
import { Typography, Card, CardMedia, CardContent, IconButton, Button, Box } from "@mui/material";
import { Close } from "@mui/icons-material";
import { useNavigate } from "react-router-dom"; // ✅ Ensure correct import of useNavigate
import LoginModal from "../Homepages/Login";

function ContestDetail({ contest, onClose }) {
  const navigate = useNavigate(); // ✅ Define navigate inside the component
  const [isLoginOpen, setIsLoginOpen] = useState(false); // ✅ Fix useState

  // Open login modal
  const handleOpenLogin = () => {
    setIsLoginOpen(true);
  };

  // Close login modal, reopen Contest Detail
  const handleCloseLogin = () => {
    setIsLoginOpen(false);
  };

  return (
    <div style={{ position: "relative" }}>
      {/* Close button with gray rectangular background */}
      <IconButton
        onClick={onClose}
        sx={{
          position: "absolute",
          top: 10,
          right: 10,
          zIndex: 1,
          backgroundColor: "blue.300", // use Material UI's built-in gray
          color: "white",
          borderRadius: 0, // rectangular shape, no border radius
          padding: "4px",
          "&:hover": {
            backgroundColor: "grey.500",
          },
        }}
      >
        <Close />
      </IconButton>
      <Card sx={{ maxWidth: "100%", boxShadow: 3, marginTop: "10px" }}>
        <CardMedia
          component="img"
          height="300"
          image={contest.image}
          alt={contest.title}
        />
        <CardContent>
          <Typography variant="h4" component="div" gutterBottom>
            {contest.title}
          </Typography>
          <Typography variant="subtitle1" color="text.secondary" gutterBottom>
            Organizer: {contest.organizer}
          </Typography>
          <Typography variant="body1" color="text.secondary" gutterBottom>
            Date: {contest.date}
          </Typography>
          <Typography variant="body1" color="text.secondary" gutterBottom>
            Category: {contest.category}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {contest.description}
          </Typography>
          {/* Buttons at the bottom right */}
          <Box sx={{ display: "flex", justifyContent: "flex-end", gap: 2, mt: 3 }}>
            <Button variant="contained" color="primary" onClick={() => navigate("/work-list")}>
              View Details
            </Button>
            <Button variant="outlined" color="primary" onClick={handleOpenLogin}>
              Login
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Login modal - ensure it is not nested inside CardContent to avoid z-index issues */}
      {isLoginOpen && <LoginModal onClose={handleCloseLogin} />}
    </div>
  );
}

export default ContestDetail;
