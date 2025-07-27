/**
 * WorksDetail.js
 * 
 * This component displays the detailed information of a selected work.
 * It includes:
 * - A close button to exit the modal view.
 * - A card showing the work's image, title, producer, and description.
 * 
 * Role: Public User
 * Developer: Beiqi Dai
 */


import React from "react";
import { Typography, Card, CardMedia, CardContent, IconButton } from "@mui/material";
import { Close } from "@mui/icons-material";

function WorksDetail({ work, onClose }) {
  return (
    <div style={{ position: "relative" }}>
      {/* The close button with a gray rectangular background */}
      <IconButton
        onClick={onClose}
        sx={{
          position: "absolute",
          top: 10,
          right: 10,
          zIndex: 1,
          backgroundColor: "blue.300", // Rectangular effect, no rounded corners. Use the built-in gray of Material UI
          color: "white",
          borderRadius: 0, // Rectangular effect, no rounded corners
          padding: "4px",
          "&:hover": {
            backgroundColor: "grey.500",
          },
        }}
      >
        <Close />
      </IconButton>
      <Card sx={{ maxWidth: "100%", boxShadow: 3, marginTop: "20px" }}>
        <CardMedia
          component="img"
          height="300"
          image={work.image}
          alt={work.title}
        />
        <CardContent>
          <Typography variant="h4" component="div" gutterBottom>
            {work.title}
          </Typography>
          <Typography variant="subtitle1" color="text.secondary" gutterBottom>
            Producer: {work.producer}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Work description: {work.description}
          </Typography>
        </CardContent>
      </Card>
    </div>
  );
}

export default WorksDetail;