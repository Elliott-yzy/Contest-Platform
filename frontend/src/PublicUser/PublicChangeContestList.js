/**
 * PublicChangeContestList.js
 * 
 * This component displays a list of contests for public users with the ability to view contest details, vote, and join.
 * It provides:
 * - Contest information: title, category, date, and status.
 * - Vote and Join buttons with a unified login prompt for unauthenticated users.
 * - Snackbar notifications to inform users about the login requirement.
 * 
 * Role: Public User
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React, { useState } from "react";
import {
  TableRow,
  TableCell,
  Button,
  Snackbar,
  Alert,
} from "@mui/material";
import "./PublicChangeContestList.css";

function PublicChangeContestList({ contest, onClick }) {
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

  // ✅ Add a unified pop-up prompt
  const handleLoginReminder = (e) => {
    e.stopPropagation();
    showSnackbar("Please log in first!", "warning");
  };

  if (!contest) return null;

  return (
    <>
      <TableRow hover className="publicuser-contest-table-row" onClick={handleRowClick}>
        <TableCell className="publicuser-table-cell">{contest.title}</TableCell>
        <TableCell className="publicuser-table-cell">{contest.category}</TableCell>
        <TableCell className="publicuser-table-cell">{contest.date}</TableCell>
        <TableCell className="publicuser-table-cell">
          <span className={`publicuser-status-dot ${contest.status.toLowerCase()}`}></span>
          {contest.status}
        </TableCell>
        <TableCell className="publicuser-table-cell">
          <div className="publicuser-button-group">
            <Button
              className="publicuser-vote-button"
              size="small"
              onClick={handleLoginReminder} // ✨ Change to a unified login prompt
            >
              Vote
            </Button>
            <Button
              className="publicuser-join-button"
              size="small"
              onClick={handleLoginReminder} // ✨ Change to a unified login prompt
            >
              Join
            </Button>
          </div>
        </TableCell>
      </TableRow>

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

export default PublicChangeContestList;
