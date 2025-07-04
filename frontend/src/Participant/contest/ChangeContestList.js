/**
 * @file ChangeContestList.js
 * @description 
 * This component renders a table row for a contest and provides functionality for users to register or cancel registration.
 * Core functionalities include:
 *  - Displaying contest details such as title, category, date, and status.
 *  - Allowing participants to join a competition by sending a registration request.
 *  - Handling cases where the user has already registered, with the option to cancel registration.
 *  - Providing feedback through confirmation dialogs and success/error snackbars.
 *  - Managing API interactions with appropriate authentication headers.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState } from "react";
import {
  TableRow,
  TableCell,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Snackbar,
  Alert,
} from "@mui/material";
import "./ChangeContestList.css";

function ChangeContestList({ contest, onClick }) {
  const [openDialog, setOpenDialog] = useState(false);
  const [snackbar, setSnackbar] = useState({ open: false, message: "", severity: "success" });

  const handleRowClick = (e) => {
    if (onClick) onClick(e);
  };

  const showSnackbar = (message, severity = "success") => {
    setSnackbar({ open: true, message, severity });
  };

  const handleSnackbarClose = (event, reason) => {
    if (event) {
      event.stopPropagation();
    }
    setSnackbar({ ...snackbar, open: false });
  };

  const handleJoinClick = async (e) => {
    e.stopPropagation();
    console.log("Join clicked:", contest);

    const token = localStorage.getItem("token");
    if (!token) {
      showSnackbar("Please log in first!", "warning");
      return;
    }

    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");

    try {
      const response = await fetch(
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

      if (response.ok) {
        const data = await response.text();
        console.log("Registration successful:", data);
        showSnackbar("Registration successful!", "success");
      } else {
        const errText = await response.text();
        console.error("Failed to register:", errText);
        try {
          const errData = JSON.parse(errText);
          if (errData.error === "You have already registered for this competition") {
            console.log("You have already registered.");
            setOpenDialog(true); // Open confirmation dialog
          } else {
            showSnackbar("Registration failed. Please check the console.", "error");
          }
        } catch (parseError) {
          showSnackbar("Registration failed. Please check the console.", "error");
        }
      }
    } catch (error) {
      console.error("Error registering:", error);
      showSnackbar("Registration failed due to network or server error.", "error");
    }
  };

  const handleCancelRegistration = async (e) => {
    if (e) e.stopPropagation();

    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");

    try {
      const response = await fetch(
        `http://localhost:8080/registrations/${contest.id}`,
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

      if (response.ok) {
        const data = await response.text();
        console.log("Cancellation successful:", data);
        showSnackbar("Registration cancelled successfully!", "success");
        setOpenDialog(false);
      } else {
        const errorText = await response.text();
        console.error("Cancellation failed:", errorText);
        showSnackbar("Cancellation failed!", "error");
      }
    } catch (error) {
      console.error("Error cancelling registration:", error);
      showSnackbar("Cancellation failed due to network or server error.", "error");
    }
  };

  if (!contest) return null;

  return (
    <>
      <TableRow hover className="contest-table-row" onClick={handleRowClick}>
        <TableCell className="table-cell">{contest.title}</TableCell>
        <TableCell className="table-cell">{contest.category}</TableCell>
        <TableCell className="table-cell">{contest.date}</TableCell>
        <TableCell className="table-cell">
          <span className={`status-dot ${contest.status}`}></span>
          {contest.status}
        </TableCell>
        <TableCell className="table-cell">
          <div className="button-group">
            <Button
              className="join-button"
              size="small"
              onClick={handleJoinClick}
            >
              Join
            </Button>
          </div>
        </TableCell>
      </TableRow>

      {/* Dialog for confirming cancellation */}
      <Dialog open={openDialog} onClose={(e) => { if (e) e.stopPropagation(); setOpenDialog(false); }}>
        <DialogTitle>Already Registered</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You have already registered for this competition. Do you want to cancel your registration?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={(e) => { if (e) e.stopPropagation(); setOpenDialog(false); }} color="primary">
            No
          </Button>
          <Button onClick={handleCancelRegistration} color="error" autoFocus>
            Yes
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar for feedback */}
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

export default ChangeContestList;
