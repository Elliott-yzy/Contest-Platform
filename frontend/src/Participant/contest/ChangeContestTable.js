/**
 * @file ChangeContestTable.js
 * @description 
 * This component renders a table displaying a list of competitions available for participants to join.
 * Core functionalities include:
 *  - Displaying competition attributes such as name, category, date, and status.
 *  - Delegating each contest row rendering and interaction handling to the ChangeContestList component.
 *  - Providing a callback when a row is clicked, enabling navigation or additional actions.
 *  - Using Material-UI Table components for layout and customized header styling.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React from "react";
import "./ChangeContestList.css";
import { Table, TableHead, TableBody, TableRow, TableCell } from "@mui/material";
import ChangeContestList from "./ChangeContestList";

function ChangeContestTable({ contests, onRowClick }) {
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
              Join
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

export default ChangeContestTable;
