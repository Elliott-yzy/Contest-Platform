/**
 * @file AdminAccountManage.js
 * @description 
 * This component provides an administrative interface for managing user accounts.
 * It allows admin users to view, search, filter, and delete participant and organizer accounts.
 * Admins can:
 *  - View a paginated list of users (excluding other admins).
 *  - Search users by keyword (name or email).
 *  - Filter users by role (Participant or Organizer).
 *  - Delete a selected user after confirmation.
 * The component integrates with a backend API, handles authentication headers, 
 * and updates the displayed data dynamically based on user interaction.
 * It uses Material-UI components for styling and layout.
 * 
 * Role: Admin
 * Developer: Zhaoyi Yang
 */

import React, { useEffect, useState } from "react";
import {
  Pagination,
  Table,
  TableHead,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Paper,
  TextField,
  FormControl,
  Select,
  MenuItem,
  InputLabel,
  Button,
} from "@mui/material";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import "./AdminAccountManage.css";

function AdminAccountManage() {
  const [users, setUsers] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [page, setPage] = useState(1);
  const [roleFilter, setRoleFilter] = useState("");
  const [keyword, setKeyword] = useState("");
  const [loading, setLoading] = useState(true);

  const fetchUsers = async (currentPage = 1, role = "", keyword = "") => {
    setLoading(true);
    try {
      const params = new URLSearchParams({
        page: currentPage,
        size: 10,
        ...(role && { role }),
        ...(keyword && { keyword }),
        sortBy: "createdAt",
        order: "desc",
      });

      const response = await fetch(
        `http://localhost:8080/users/admin/list?${params.toString()}`,
        {
          method: "GET",
          headers: {
            "User-ID": localStorage.getItem("userId"),
            "User-Role": localStorage.getItem("role"),
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      const data = await response.json();
      if (response.ok) {
        setUsers(data.data);
        setTotalPages(data.pages);
      } else {
        alert(data.message || "Failed to fetch users");
      }
    } catch (error) {
      console.error("Error fetching users:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers(page, roleFilter, keyword);
  }, [page, roleFilter, keyword]);  

  useEffect(() => {
    setPage(1);
    fetchUsers(1, roleFilter, keyword);
  }, [roleFilter, keyword]);

  const handlePageChange = (_, value) => {
    setPage(value);
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm("Are you sure you want to delete this user?")) return;

    try {
      const response = await fetch(`http://localhost:8080/users/${userId}`, {
        method: "DELETE",
        headers: {
          "User-ID": localStorage.getItem("userId"),
          "User-Role": localStorage.getItem("role"),
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      if (response.ok) {
        alert("User deleted successfully.");
        fetchUsers(page, roleFilter, keyword);
      } else {
        const err = await response.json();
        alert(err.message || "Failed to delete user.");
      }
    } catch (error) {
      console.error("Error deleting user:", error);
      alert("An error occurred while deleting user.");
    }
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <h2 className="admin-label">All Users</h2>

          <div className="admin-header">
            <TextField
              className="search-bar"
              label="Search"
              variant="outlined"
              size="small"
              value={keyword}
              onChange={(e) => {
                setPage(1);
                setKeyword(e.target.value);
              }}
            />

            <FormControl size="small" style={{ minWidth: 150, marginLeft: "10px" }}>
              <InputLabel>Role</InputLabel>
              <Select
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
                label="Role"
              >
                <MenuItem value="">All</MenuItem>
                <MenuItem value="ORGANIZER">ORGANIZER</MenuItem>
                <MenuItem value="PARTICIPANT">PARTICIPANT</MenuItem>
              </Select>
            </FormControl>
          </div>

          {loading ? (
            <p>Loading users...</p>
          ) : users.filter((user) => user.role !== "ADMIN").length === 0 ? (
            <p>No users found.</p>
          ) : (
            <div className="admin-table">
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>#</TableCell>
                      <TableCell>Name</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Role</TableCell>
                      <TableCell>Delete</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {users
                      .filter((user) => user.role !== "Admin")
                      .map((user, index) => (
                        <TableRow key={user.id}>
                          <TableCell>{index + 1 + (page - 1) * 10}</TableCell>
                          <TableCell>{user.name}</TableCell>
                          <TableCell>{user.email}</TableCell>
                          <TableCell>{user.description || "-"}</TableCell>
                          <TableCell>{user.role}</TableCell>
                          <TableCell>
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              onClick={() => handleDeleteUser(user.id)}
                            >
                              Delete
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </div>
          )}

          <div style={{ marginTop: "20px", display: "flex", justifyContent: "center" }}>
            <Pagination
              count={totalPages}
              page={page}
              onChange={handlePageChange}
              color="primary"
            />
          </div>
        </div>
      </div>
    </>
  );
}

export default AdminAccountManage;