/**
 * PublicChangeContestTable.js
 * 
 * This component renders a table displaying a list of contests for public users. 
 * It shows:
 * - Contest name, category, date, status, and action buttons (Vote, Join).
 * - Each contest row links to its respective actions (Vote/Join) and displays a unified login prompt for unauthenticated users.
 * 
 * Role: Public User
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React from "react";
import "./PublicChangeContestList.css";
import { Table, TableHead, TableBody, TableRow, TableCell } from "@mui/material";
import ChangeContestList from "./PublicChangeContestList";

function PublicChangeContestTable({ contests, onRowClick }) {
  return (
    <div className="table-container">
      <Table>
        <TableHead>
          <TableRow
            sx={{
              backgroundColor: "#ffa680",
            }}
          >
            <TableCell
              sx={{
                fontSize: "18px",
                fontWeight: "700",
                color: "white",
              }}
            >
              Competition Name
            </TableCell>
            <TableCell
              sx={{
                fontSize: "18px",
                fontWeight: "700",
                color: "white",
              }}
            >
              Category
            </TableCell>
            <TableCell
              sx={{
                fontSize: "18px",
                fontWeight: "700",
                color: "white",
              }}
            >
              Date
            </TableCell>
            <TableCell
              sx={{
                fontSize: "18px",
                fontWeight: "700",
                color: "white",
              }}
            >
              Status
            </TableCell>
            <TableCell
              sx={{
                fontSize: "18px",
                fontWeight: "700",
                color: "white",
              }}
            >
              Actions
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {contests.map((contest, index) => (
            <ChangeContestList
              key={index}
              contest={contest}
              onClick={() => onRowClick && onRowClick(contest)}
            />
          ))}
        </TableBody>
      </Table>
    </div>
  );
}

export default PublicChangeContestTable;
