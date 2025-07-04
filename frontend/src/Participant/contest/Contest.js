/**
 * @file Contest.js
 * @description 
 * This component provides a participant-facing view to explore available competitions.
 * Participants can:
 *  - Search competitions by keywords.
 *  - Filter competitions by status (Upcoming, Ongoing, Completed, Awarded).
 *  - Filter by participation type (Individual or Team).
 *  - Filter by competition categories.
 *  - Switch between card view and table list view.
 *  - Join competitions directly if allowed.
 * The component integrates with a backend API for fetching competition data,
 * manages pagination and client-side filtering, and uses Material-UI for UI design.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect } from "react";
import TopBar from "../TopSide/TopBar";
import Sidebar from "../TopSide/Sidebar";
import ContestCard from "./ContestCard";
import ChangeContestTable from "./ChangeContestTable";
import { IconButton, Typography, Pagination } from "@mui/material";
import { Close, FilterAlt, ViewList } from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import "./Contest.css";
import SearchIcon from "@mui/icons-material/Search";
import defaultImage from "./1.jpg";
import RefreshIcon from "@mui/icons-material/Refresh";

function Contest() {
  const [isFilterVisible, setIsFilterVisible] = useState(false);
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [selectedStatus, setSelectedStatus] = useState("");
  const [selectedParticipationType, setSelectedParticipationType] = useState("");
  const [contests, setContests] = useState([]);
  const [searchInput, setSearchInput] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [isListView, setIsListView] = useState(false);
  const [page, setPage] = useState(1);
  const [size] = useState(6);
  const navigate = useNavigate();

  const toggleFilter = () => setIsFilterVisible(!isFilterVisible);

  const handleCategoryChange = (category) => {
    setSelectedCategories((prev) =>
      prev.includes(category)
        ? prev.filter((c) => c !== category)
        : [...prev, category]
    );
  };

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      console.error("No token found. Please login first.");
      return;
    }

    const fetchContests = async () => {
      try {
        const params = new URLSearchParams();
        if (searchTerm) params.append("keyword", searchTerm);
        if (selectedCategories.length > 0) params.append("category", selectedCategories.join(","));
        if (selectedStatus) params.append("status", selectedStatus);

        const url = `http://localhost:8080/competitions/list?${params.toString()}`;

        const response = await fetch(url, {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        setContests(result.data || []);
      } catch (error) {
        console.error("Error fetching contests:", error);
      }
    };

    fetchContests();
  }, [searchTerm, selectedCategories, selectedStatus]);

  const formatDateRange = (start, end) => {
    if (!start || !end) return "N/A";
    const startStr = new Date(start).toLocaleDateString();
    const endStr = new Date(end).toLocaleDateString();
    return `${startStr} ~ ${endStr}`;
  };

  const handleCardClick = (contest) => {
    navigate(`/contest-detail/${contest.id}`);
  };

  const handleSearchClick = () => {
    setSearchTerm(searchInput);
    setPage(1);
  };

  const getStatusText = (status) => {
    switch (status) {
      case "UPCOMING":
        return "Not started";
      case "ONGOING":
        return "Ongoing";
      case "COMPLETED":
        return "Completed";
      default:
        return "Unknown status";
    }
  };

  const pages = Math.ceil(contests.length / size);
  const paginatedContests = contests.slice((page - 1) * size, page * size);

  const filteredContests = paginatedContests.filter((item) => {
    if (selectedParticipationType && item.participationType !== selectedParticipationType) return false;
    return true;
  });

  return (
    <>
      <TopBar />
      <div className="participant-contest-container">
        <Sidebar />
        <div className="participant-contest-content">
          {/* Top search bar */}
          <div className="participant-contest-header">
            <IconButton onClick={() => {
              setSearchInput("");
              setSearchTerm("");
              setPage(1);
            }} sx={{
              color: "#FF9800", border: "1px solid #FF9800", borderRadius: "8px", marginRight: "8px",
              ":hover": { backgroundColor: "#FFE0B2" },
            }}><RefreshIcon /></IconButton>

            <input
              className="participant-search-bar"
              type="text"
              placeholder="Search..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
            />
            <IconButton onClick={handleSearchClick} sx={{
              color: "#FF9800", border: "1px solid #FF9800", borderRadius: "8px", marginLeft: "8px",
              ":hover": { backgroundColor: "#FFE0B2" },
            }}><SearchIcon /></IconButton>

            <IconButton onClick={toggleFilter} sx={{
              color: "#FF9800", border: "1px solid #FF9800", borderRadius: "8px", marginLeft: "8px",
              ":hover": { backgroundColor: "#FFE0B2" },
            }}><FilterAlt /></IconButton>

            <IconButton onClick={() => setIsListView((prev) => !prev)} sx={{
              color: "#FF9800", border: "1px solid #FF9800", borderRadius: "8px", marginLeft: "8px",
              ":hover": { backgroundColor: "#FFE0B2" },
            }}><ViewList /></IconButton>
          </div>

          {/* Filter panel */}
          <div className={`participant-filter-sidebar ${isFilterVisible ? "visible" : ""}`}>
            <IconButton className="participant-close-filter" onClick={toggleFilter}><Close /></IconButton>

            <Typography variant="h6" sx={{ mt: 2, backgroundColor: "white", color: "white", padding: "5px" }}>
              Filter by Status
            </Typography>
            <select value={selectedStatus} onChange={(e) => setSelectedStatus(e.target.value)} className="participant-filter-select">
              <option value="">All</option>
              <option value="UPCOMING">UPCOMING</option>
              <option value="ONGOING">ONGOING</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="AWARDED">AWARDED</option>
            </select>

            <Typography variant="h6" sx={{ mt: 2, backgroundColor: "white", color: "white", padding: "5px" }}>
              Filter by Participation Type
            </Typography>
            <select value={selectedParticipationType} onChange={(e) => setSelectedParticipationType(e.target.value)} className="participant-filter-select">
              <option value="">All</option>
              <option value="INDIVIDUAL">Individual</option>
              <option value="TEAM">Team</option>
            </select>

            <Typography variant="h6" sx={{ mt: 2, backgroundColor: "white", color: "white", padding: "5px", marginBottom: "10px" }}>
              Filter by Category
            </Typography>
            {contests.length > 0 &&
              Array.from(new Set(contests.map((item) => item.category))).sort().map((category) => (
                <label key={category} className="participant-filter-option">
                  <input type="checkbox" checked={selectedCategories.includes(category)} onChange={() => handleCategoryChange(category)} />
                  {category}
                </label>
              ))}
          </div>

          {/* Display area */}
          <div className="participant-contest-cards">
            {isListView ? (
              <ChangeContestTable
                contests={filteredContests.map((item) => ({
                  id: item.id,
                  title: item.name,
                  description: item.description,
                  category: item.category,
                  date: formatDateRange(item.startDate, item.endDate),
                  isPublic: item.isPublic,
                  status: item.status,
                  statusText: getStatusText(item.status),
                  allowedSubmissionTypes: item.allowedSubmissionTypes,
                  scoringCriteria: item.scoringCriteria,
                  introVideoUrl: item.introVideoUrl,
                  image: item.imageUrls?.[0] || defaultImage,
                  createdAt: item.createdAt,
                }))}
                onRowClick={handleCardClick}
              />
            ) : (
              filteredContests.map((item) => (
                <ContestCard
                  key={item.id}
                  contest={{
                    id: item.id,
                    title: item.name,
                    description: item.description,
                    category: item.category,
                    date: formatDateRange(item.startDate, item.endDate),
                    isPublic: item.isPublic,
                    status: item.status,
                    allowedSubmissionTypes: item.allowedSubmissionTypes,
                    scoringCriteria: item.scoringCriteria,
                    introVideoUrl: item.introVideoUrl,
                    image: item.imageUrls?.[0] || defaultImage,
                    createdAt: item.createdAt,
                    participationType: item.participationType || "INDIVIDUAL",
                  }}
                  onCardClick={handleCardClick}
                />
              ))
            )}
          </div>

          {/* Paginator */}
          <div style={{ display: "flex", justifyContent: "center", marginTop: "20px" }}>
            <Pagination
              count={pages}
              page={page}
              onChange={(event, value) => setPage(value)}
              sx={{
                '& .MuiPaginationItem-root': { color: '#FF9800', borderColor: '#FF9800' },
                '& .MuiPaginationItem-root.Mui-selected': {
                  backgroundColor: '#FF9800',
                  color: '#fff',
                  borderColor: '#FF9800',
                  '&:hover': { backgroundColor: '#FB8C00' },
                }
              }}
            />
          </div>
        </div>
      </div>
    </>
  );
}

export default Contest;
