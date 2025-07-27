/**
 * @file OrganizerContestList.js
 * @description 
 * This component displays the list of contests created by the organizer.
 * It allows organizers to:
 *  - Search contests by name.
 *  - Filter contests by status, category, and participation type.
 *  - Edit, upload media for, or delete contests.
 *  - View participants and submissions of each contest.
 *  - Add judges to contests.
 *  - Create new contests.
 * 
 * The component fetches organizer-specific contests from the backend,
 * manages multiple filters and actions per competition,
 * and uses Material-UI components for layout and interaction.
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  IconButton,
} from "@mui/material";
import { FilterAlt, Close } from "@mui/icons-material";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import "./ContestList.css";

function OrganizerContestList() {
  const [competitions, setCompetitions] = useState([]);
  const [filteredCompetitions, setFilteredCompetitions] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("");
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [isFilterVisible, setIsFilterVisible] = useState(false);
  const [selectedParticipationType, setSelectedParticipationType] = useState("");

  const navigate = useNavigate();
  const userId = localStorage.getItem("userId");
  const token = localStorage.getItem("token");
  const email = localStorage.getItem("email");

  useEffect(() => {
    const fetchCompetitions = async () => {
      try {
        const response = await fetch(`http://localhost:8080/competitions/achieve/my`, {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "User-ID": userId,
            "User-Role": "ORGANIZER",
          },
        });

        const data = await response.json();
        if (response.ok && Array.isArray(data.data)) {
          setCompetitions(data.data);
          setFilteredCompetitions(data.data);
        } else {
          console.error("Failed to fetch competitions", data);
        }
      } catch (err) {
        console.error("Error fetching competitions:", err);
      }
    };

    fetchCompetitions();
  }, [userId, token]);

  useEffect(() => {
    const filtered = competitions.filter((comp) => {
      const matchesSearch = comp.name.toLowerCase().includes(searchInput.toLowerCase());
      const matchesStatus = selectedStatus ? comp.status === selectedStatus : true;
      const matchesCategory =
        selectedCategories.length > 0 ? selectedCategories.includes(comp.category) : true;
      const matchesParticipation = selectedParticipationType
        ? comp.participationType === selectedParticipationType
        : true;
      return matchesSearch && matchesStatus && matchesCategory && matchesParticipation;
    });
    setFilteredCompetitions(filtered);
  }, [
    searchInput,
    selectedStatus,
    selectedCategories,
    selectedParticipationType,
    competitions,
  ]);

  const handleCreate = () => {
    navigate(`/OrganizerContest/${email}`);
  };

  const handleEdit = (competitionId) => {
    navigate(`/OrganizerEditContest/${email}?competitionId=${competitionId}`);
  };

  const handleDelete = async (competitionId) => {
    if (!window.confirm("Are you sure you want to delete this competition?")) return;

    try {
      const response = await fetch(`http://localhost:8080/competitions/delete/${competitionId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/x-www-form-urlencoded",
          "User-ID": userId,
          "User-Role": "ORGANIZER",
        },
      });

      if (response.ok) {
        setCompetitions((prev) => prev.filter((comp) => comp.id !== competitionId));
      } else {
        const errorData = await response.json();
        console.error("Failed to delete competition:", errorData);
        alert("Failed to delete competition.");
      }
    } catch (err) {
      console.error("Error deleting competition:", err);
      alert("An error occurred while deleting the competition.");
    }
  };

  const handleCategoryChange = (category) => {
    setSelectedCategories((prev) =>
      prev.includes(category)
        ? prev.filter((c) => c !== category)
        : [...prev, category]
    );
  };

  const toggleFilter = () => setIsFilterVisible((prev) => !prev);

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <div className="contest-header">
            <input
              className="search-bar"
              type="text"
              placeholder="Search by name..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
            />

            <IconButton
              title="Filter"
              onClick={toggleFilter}
              sx={{
                color: "#FF9800",
                border: "1px solid #FF9800",
                borderRadius: "8px",
                marginLeft: "8px",
                ":hover": { backgroundColor: "#FFE0B2" },
              }}
            >
              <FilterAlt />
            </IconButton>
          </div>

          <div className={`filter-sidebar ${isFilterVisible ? "visible" : ""}`}>
            <IconButton className="close-filter" onClick={toggleFilter}>
              <Close />
            </IconButton>

            <div className="filter-section">
              <h4 className="filter-heading">Filter by Status</h4>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="filter-select"
              >
                <option value="">All</option>
                <option value="UPCOMING">UPCOMING</option>
                <option value="ONGOING">ONGOING</option>
                <option value="COMPLETED">COMPLETED</option>
              </select>
            </div>

            <div className="filter-section">
              <h4 className="filter-heading">Filter by Participation Type</h4>
              <select
                value={selectedParticipationType}
                onChange={(e) => setSelectedParticipationType(e.target.value)}
                className="filter-select"
              >
                <option value="">All</option>
                <option value="INDIVIDUAL">INDIVIDUAL</option>
                <option value="TEAM">TEAM</option>
              </select>
            </div>

            <div className="filter-section">
              <h4 className="filter-heading">Filter by Category</h4>
              <div className="filter-checkbox-group">
                {competitions.length > 0 &&
                  Array.from(new Set(competitions.map((item) => item.category)))
                    .sort((a, b) => a.localeCompare(b))
                    .map((category) => (
                      <label key={category} className="filter-option">
                        <input
                          type="checkbox"
                          checked={selectedCategories.includes(category)}
                          onChange={() => handleCategoryChange(category)}
                        />
                        {category}
                      </label>
                    ))}
              </div>
            </div>
          </div>

          {filteredCompetitions.length === 0 ? (
            <Typography variant="body1" sx={{ mt: 3 }}>
              No competitions found.
            </Typography>
          ) : (
            <TableContainer component={Paper} className="contest-table">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Start Date</TableCell>
                    <TableCell>End Date</TableCell>
                    <TableCell>Actions</TableCell>
                    <TableCell>Media</TableCell>
                    <TableCell>Delete</TableCell>
                    <TableCell>Participants</TableCell>
                    <TableCell>Submissions</TableCell>
                    <TableCell>Add Judge</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredCompetitions.map((comp, index) => (
                    <TableRow key={comp.id}>
                      <TableCell>{index + 1}</TableCell>
                      <TableCell>{comp.name}</TableCell>
                      <TableCell>{comp.category}</TableCell>
                      <TableCell>{comp.status}</TableCell>
                      <TableCell>{new Date(comp.startDate).toLocaleDateString()}</TableCell>
                      <TableCell>{new Date(comp.endDate).toLocaleDateString()}</TableCell>
                      <TableCell>
                        <Button size="small" variant="outlined" onClick={() => handleEdit(comp.id)}>
                          Edit
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          color="secondary"
                          variant="contained"
                          onClick={() => navigate(`/OrganizerUploadMedia/${comp.id}`)}
                        >
                          Upload
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          color="error"
                          variant="outlined"
                          onClick={() => handleDelete(comp.id)}
                        >
                          Delete
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() =>
                            navigate(`/OrganizerParticipantList/${comp.id}`, {
                              state: { participationType: comp.participationType },
                            })
                          }
                        >
                          View
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => navigate(`/OrganizerSubmissions/${comp.id}`)}
                        >
                          Check
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="contained"
                          color="success"
                          onClick={() => navigate(`/OrganizerAddJudge/${comp.id}`)}
                        >
                          Add
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
          <div className="create-button-wrapper">
            <Button variant="contained" color="primary" onClick={handleCreate}>
              Create New Competition
            </Button>
          </div>
        </div>
      </div>
    </>
  );
}

export default OrganizerContestList;
