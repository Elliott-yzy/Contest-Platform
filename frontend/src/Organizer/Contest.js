/**
 * @file OrganizerContest.js
 * @description 
 * This component provides a form for organizers to create a new contest.
 * It allows organizers to:
 *  - Enter contest details such as name, description, category, dates, and participation type.
 *  - Add custom scoring criteria.
 *  - Select allowed submission formats (e.g., PDF, ZIP, CODE, Image, Text).
 *  - Choose between public or private visibility.
 *  - Automatically determine the contest status (Upcoming, Ongoing, Completed) based on dates.
 * 
 * Upon form submission, the contest information is sent to the backend API to create a new competition entry.
 * Material-UI components are used for form inputs, layout, and interactions.
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import {
  TextField,
  Select,
  MenuItem,
  FormControl,
  Radio,
  RadioGroup,
  FormControlLabel,
  Button,
  IconButton,
  Checkbox,
  FormGroup,
} from "@mui/material";
import { Delete } from "@mui/icons-material";
import "./Contest.css";

function OrganizerContest() {
  const [contestData, setContestData] = useState({
    contestName: "",
    contestDescription: "",
    category: "",
    startDate: "",
    endDate: "",
    isPublic: "Public",
    scoringCriteria: [],
    submissionFormats: [],
    participationType: "INDIVIDUAL",
  });

  const [newCriteria, setNewCriteria] = useState("");
  const navigate = useNavigate();
  const availableFormats = ["PDF", "ZIP", "CODE", "Image", "Text"];
  const { email } = useParams();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setContestData((prevData) => ({ ...prevData, [name]: value }));
  };

  const addCriteria = () => {
    if (newCriteria.trim() !== "") {
      setContestData((prevData) => ({
        ...prevData,
        scoringCriteria: [...prevData.scoringCriteria, newCriteria],
      }));
      setNewCriteria("");
    }
  };

  const removeCriteria = (index) => {
    setContestData((prevData) => ({
      ...prevData,
      scoringCriteria: prevData.scoringCriteria.filter((_, i) => i !== index),
    }));
  };

  const handleFormatChange = (format) => {
    setContestData((prevData) => ({
      ...prevData,
      submissionFormats: prevData.submissionFormats.includes(format)
        ? prevData.submissionFormats.filter((f) => f !== format)
        : [...prevData.submissionFormats, format],
    }));
  };

  const getAutoStatus = () => {
    const now = new Date();
    const start = new Date(contestData.startDate + "T00:00:00");
    const end = new Date(contestData.endDate + "T23:59:59");
    if (now < start) return "UPCOMING";
    if (now >= start && now <= end) return "ONGOING";
    return "COMPLETED";
  };

  const handleSave = async () => {
    try {
      const payload = {
        name: contestData.contestName,
        description: contestData.contestDescription,
        category: contestData.category,
        startDate: new Date(contestData.startDate + "T10:00:00Z").toISOString().replace(".000", ""),
        endDate: new Date(contestData.endDate + "T18:00:00Z").toISOString().replace(".000", ""),
        isPublic: contestData.isPublic === "Public",
        status: getAutoStatus(),
        allowedSubmissionTypes: contestData.submissionFormats,
        scoringCriteria: contestData.scoringCriteria,
        participationType: contestData.participationType,
        imageUrls: [],
        introVideoUrl: "",
      };
      console.log("Sending payload:", payload);

      const response = await fetch("http://localhost:8080/competitions", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(payload),
      });

      const data = await response.json();
      if (response.ok) {
        alert("🎉 Contest created successfully!");
        navigate(`/OrganizerContestList/${email}`);
      } else {
        alert("❌ Failed to create contest: " + (data.message || "Unknown error"));
      }
    } catch (error) {
      console.error("Error creating contest:", error);
      alert("❌ Server error. Please try again.");
    }
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <h2 className="contest-label">Contest Name</h2>
          <TextField
            fullWidth
            name="contestName"
            value={contestData.contestName}
            onChange={handleChange}
            placeholder="Enter contest name"
          />

          <h2 className="contest-label">Contest Description</h2>
          <TextField
            fullWidth
            multiline
            rows={3}
            name="contestDescription"
            value={contestData.contestDescription}
            onChange={handleChange}
            placeholder="Describe your contest..."
          />

          <h2 className="contest-label">Category</h2>
          <FormControl fullWidth>
            <Select
              name="category"
              value={contestData.category}
              onChange={handleChange}
              displayEmpty
            >
              <MenuItem value="" disabled>Select a category</MenuItem>
              <MenuItem value="Design & Creativity">Design & Creativity</MenuItem>
              <MenuItem value="Programming & Technology">Programming & Technology</MenuItem>
              <MenuItem value="Business & Entrepreneurship">Business & Entrepreneurship</MenuItem>
              <MenuItem value="Mathematics & Science">Mathematics & Science</MenuItem>
              <MenuItem value="Humanities & Social Sciences">Humanities & Social Sciences</MenuItem>
              <MenuItem value="Music & Performing Arts">Music & Performing Arts</MenuItem>
              <MenuItem value="Others">Others</MenuItem>
            </Select>
          </FormControl>

          <h2 className="contest-label">Start Date & Deadline</h2>
          <div className="date-picker">
            <TextField
              type="date"
              name="startDate"
              value={contestData.startDate}
              onChange={handleChange}
              inputProps={{ "data-testid": "start-date" }}
            />

            <TextField
              type="date"
              name="endDate"
              value={contestData.endDate}
              onChange={handleChange}
              inputProps={{ "data-testid": "end-date" }}
            />

          </div>

          <h2 className="contest-label">Scoring Criteria</h2>
          <div className="scoring-input">
            <TextField
              fullWidth
              value={newCriteria}
              onChange={(e) => setNewCriteria(e.target.value)}
              placeholder="Enter scoring criteria"
            />
            <Button variant="contained" color="primary" onClick={addCriteria}>Add</Button>
          </div>
          <ul className="scoring-list">
            {contestData.scoringCriteria.map((criteria, index) => (
              <li key={index} className="scoring-item">
                {criteria}
                <IconButton aria-label="delete" onClick={() => removeCriteria(index)} size="small">
                  <Delete />
                </IconButton>
              </li>
            ))}
          </ul>

          <h2 className="contest-label">Submission Formats</h2>
          <FormGroup>
            {availableFormats.map((format) => (
              <FormControlLabel
                key={format}
                control={
                  <Checkbox
                    checked={contestData.submissionFormats.includes(format)}
                    onChange={() => handleFormatChange(format)}
                  />
                }
                label={format}
              />
            ))}
          </FormGroup>

          <h2 className="contest-label">Participation Type</h2>
          <FormControl fullWidth>
            <Select
              name="participationType"
              value={contestData.participationType}
              onChange={handleChange}
              displayEmpty
            >
              <MenuItem value="" disabled>Select type</MenuItem>
              <MenuItem value="INDIVIDUAL">INDIVIDUAL</MenuItem>
              <MenuItem value="TEAM">TEAM</MenuItem>
            </Select>
          </FormControl>

          <h2 className="contest-label">Public / Private</h2>
          <RadioGroup
            row
            name="isPublic"
            value={contestData.isPublic}
            onChange={handleChange}
          >
            <FormControlLabel value="Public" control={<Radio />} label="Public" />
            <FormControlLabel value="Private" control={<Radio />} label="Private" />
          </RadioGroup>

          <h2 className="contest-label">Competition Status</h2>
          <TextField
            label="Competition Status"
            value={getAutoStatus()}
            fullWidth
            disabled
            variant="outlined"
            slotProps={{
              input: {
                style: {
                  backgroundColor: "#f5f5f5",
                },
              },
            }}
            style={{ marginBottom: "20px" }}
          />

          <Button
            variant="contained"
            color="primary"
            className="save-button"
            onClick={handleSave}
          >
            Create Contest
          </Button>

          <Button
            variant="outlined"
            color="secondary"
            style={{ marginTop: "10px", marginLeft: "10px" }}
            onClick={() => navigate(`/OrganizerContestList/${email}`)}
          >
            Cancel
          </Button>
        </div>
      </div>
    </>
  );
}

export default OrganizerContest;
