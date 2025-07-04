/**
 * Contest.js
 * 
 * This component displays a list of contests with search, filter, and pagination functionality.
 * It allows users to:
 * - Search contests by keyword.
 * - Filter contests by status, participation type, and category.
 * - Toggle between card and list views.
 * - Navigate to detailed contest views.
 * 
 * Role: Public User
 * Developer: Beiqi Dai, Zhaoyi Yang
 */

import React, { useState, useEffect } from "react";
import Navbar from "../Homepages/Navbar";
import Footer from "../Homepages/Footer";
import ContestCard from "./UserContestCard";
import { IconButton, Typography, Pagination } from "@mui/material";
import { Close, FilterAlt, ViewList } from "@mui/icons-material";
import SearchIcon from "@mui/icons-material/Search";
import { useNavigate } from "react-router-dom";
import "./UserContestList.css";
import defaultImage from "./1.jpg";
import ChangeContestTable from "./PublicChangeContestTable";

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
  const size = 6;
  const navigate = useNavigate();

  const toggleFilter = () => {
    setIsFilterVisible(!isFilterVisible);
  };

  const handleCategoryChange = (category) => {
    setSelectedCategories((prev) =>
      prev.includes(category) ? prev.filter((c) => c !== category) : [...prev, category]
    );
  };

  useEffect(() => {
    const fetchContests = async () => {
      try {
        const params = new URLSearchParams();
        if (searchTerm) params.append("keyword", searchTerm);
        if (selectedCategories.length > 0)
          params.append("category", selectedCategories.join(","));
        if (selectedStatus) params.append("status", selectedStatus);

        const url = `http://localhost:8080/competitions/list?${params.toString()}`;
        console.log("🔍 Fetching contests from:", url);

        const response = await fetch(url);
        const result = await response.json();
        console.log("📦 Raw contests:", result.data?.length, result.data?.[0]);
        setContests(result.data || []);
      } catch (error) {
        console.error("Error fetching contests:", error);
      }
    };

    fetchContests();
  }, [searchTerm, selectedCategories, selectedStatus]);

  const formatDateRange = (start, end) => {
    if (!start || !end) return "N/A";
    return `${new Date(start).toLocaleDateString()} ~ ${new Date(end).toLocaleDateString()}`;
  };

  const handleCardClick = (contest) => {
    navigate(`/publiccontest-detail/${contest.id}`);
  };

  const handleSearchClick = () => {
    setSearchTerm(searchInput);
    setPage(1);
  };

  const getStatusText = (status) => {
    switch (status) {
      case "UPCOMING":
        return "not started";
      case "ONGOING":
        return "in progress";
      case "COMPLETED":
        return "completed";
      default:
        return "unknown";
    }
  };

  const pages = Math.ceil(contests.length / size);
  const paginatedContests = contests.slice((page - 1) * size, page * size);

  const filteredContests = paginatedContests.filter((item) => {
    if (selectedParticipationType && item.participationType !== selectedParticipationType) {
      return false;
    }
    return true;
  });

  console.log("🧮 Filter: participationType =", selectedParticipationType);
  console.log("📋 Filtered contests:", filteredContests.length);

  return (
    <>
      <Navbar />
      <div className="publicuser-main-wrapper">
        <div className="publicuser-content-wrapper">
          <div className="publicuser-contest-container">
            <Typography
              variant="h4"
              align="center"
              fontWeight="bold"
              sx={{ marginTop: 0, marginBottom: 3 }}
            >
              Contest List
            </Typography>

            {/* Search bar */}
            <div className="publicuser-contest-header">
              <input
                className="publicuser-search-bar"
                type="text"
                placeholder="Search..."
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
              />
              <IconButton
                className="publicuser-search-button"
                title="Search"
                onClick={handleSearchClick}
              >
                <SearchIcon />
              </IconButton>
              <IconButton
                className="publicuser-filter-button"
                title="Filter"
                onClick={toggleFilter}
              >
                <FilterAlt />
              </IconButton>
              <IconButton
                className="publicuser-filter-button"
                title="Toggle List / Card View"
                onClick={() => setIsListView((prev) => !prev)}
              >
                <ViewList />
              </IconButton>
            </div>

            {/* Filter bar */}
            <div className={`publicuser-filter-sidebar ${isFilterVisible ? "visible" : ""}`}>
              <IconButton className="publicuser-close-filter" onClick={toggleFilter}>
                <Close />
              </IconButton>

              <Typography variant="h6" className="publicuser-filter-status" sx={{ mt: 2, backgroundColor: "white", color: "white", padding: "5px", marginBottom: "10px" }}>
                Filter by Status
              </Typography>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="publicuser-filter-select"
              >
                <option value="">All</option>
                <option value="UPCOMING">UPCOMING</option>
                <option value="ONGOING">ONGOING</option>
                <option value="COMPLETED">COMPLETED</option>
                <option value="AWARDED">AWARDED</option>
              </select>

              <Typography variant="h6" className="publicuser-filter-status" sx={{ mt: 2, backgroundColor: "white", color: "white", padding: "5px", marginBottom: "10px" }}>
                Filter by Participation Type
              </Typography>
              <div style={{ display: "flex", flexDirection: "column", gap: "8px", marginTop: "8px" }}>
                <select
                  value={selectedParticipationType}
                  onChange={(e) => {
                    setSelectedParticipationType(e.target.value);
                    setPage(1);
                  }}
                  className="publicuser-filter-select"
                >
                  <option value="">All</option>
                  <option value="INDIVIDUAL">INDIVIDUAL</option>
                  <option value="TEAM">TEAM</option>
                </select>

                <Typography variant="h6" className="publicuser-filter-category" >
                  Filter by Category
                </Typography>
                {contests.length > 0 &&
                  Array.from(new Set(contests.map((item) => item.category)))
                    .sort()
                    .map((category) => (
                      <label key={category} className="publicuser-filter-option">
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

            {/* Show the cards or forms */}
            <div className="publicuser-contest-cards">
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
                    participationType: item.participationType || "INDIVIDUAL",
                  }))}
                  onRowClick={handleCardClick}
                />
              ) : (
                filteredContests.map((item) => {
                  const mappedContest = {
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
                  };
                  return (
                    <ContestCard
                      key={mappedContest.id}
                      contest={mappedContest}
                      onCardClick={handleCardClick}
                    />
                  );
                })
              )}
            </div>

            {/* 分页器 */}
            <div style={{ display: "flex", justifyContent: "center", marginTop: "20px" }}>
              <Pagination
                count={pages}
                page={page}
                onChange={(event, value) => setPage(value)}
                sx={{
                  '& .MuiPaginationItem-root': {
                    color: '#FF9800',
                    borderColor: '#FF9800',
                  },
                  '& .Mui-selected': {
                    backgroundColor: '#FF9800',
                    color: '#fff',
                    borderColor: '#FF9800',
                    '&:hover': {
                      backgroundColor: '#FB8C00',
                    },
                  },
                }}
              />
            </div>
          </div>
        </div>
      </div>
      <Footer />
    </>
  );
}

export default Contest;
