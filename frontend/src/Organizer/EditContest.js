/**
 * @file EditContest.js
 * @description 
 * This component allows organizers to edit an existing competition's details.
 * It enables organizers to:
 *  - View and update competition information including name, description, category, dates, and participation type.
 *  - Add or remove scoring criteria.
 *  - Select allowed submission formats (e.g., PDF, ZIP, CODE, Image, Text).
 *  - Set competition visibility as public or private.
 *  - Automatically determine and update the competition's status (Upcoming, Ongoing, Completed) based on dates.
 * 
 * Upon submission, the updated contest information is sent to the backend API.
 * Material-UI components are used for building the form and user interactions.
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams, useParams } from "react-router-dom";
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

function EditContest() {
  const [contestData, setContestData] = useState({
    contestName: "",
    contestDescription: "",
    category: "",
    startDate: "",
    endDate: "",
    isPublic: "Public",
    scoringCriteria: [],
    submissionFormats: [],
    participationType: "",
  });

  const [newCriteria, setNewCriteria] = useState("");
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const competitionId = searchParams.get("competitionId");
  const availableFormats = ["PDF", "ZIP", "CODE", "Image", "Text"];
  const { email } = useParams();

  useEffect(() => {
    if (competitionId) {
      fetch(`http://localhost:8080/competitions/${competitionId}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      })
        .then((res) => res.json())
        .then((data) => {
          if (data && data.id) {
            setContestData({
              contestName: data.name,
              contestDescription: data.description,
              category: data.category,
              startDate: data.startDate.slice(0, 10),
              endDate: data.endDate.slice(0, 10),
              isPublic: data.isPublic ? "Public" : "Private",
              scoringCriteria: data.scoringCriteria || [],
              submissionFormats: data.allowedSubmissionTypes || [],
              participationType: data.participationType,
            });
          }
        })
        .catch((err) => console.error("Failed to load contest details:", err));
    }
  }, [competitionId]);

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

  const handleUpdate = async () => {
    try {
      const start = new Date(contestData.startDate + "T10:00:00Z");
      const end = new Date(contestData.endDate + "T18:00:00Z");

      const payload = {
        name: contestData.contestName,
        description: contestData.contestDescription,
        category: contestData.category,
        startDate: start.toISOString().replace(".000", ""),
        endDate: end.toISOString().replace(".000", ""),
        isPublic: contestData.isPublic === "Public",
        status: getAutoStatus(),
        allowedSubmissionTypes: contestData.submissionFormats,
        scoringCriteria: contestData.scoringCriteria,
        participationType: contestData.participationType,
        imageUrls: [],
        introVideoUrl: "",
      };

      const response = await fetch(`http://localhost:8080/competitions/update/${competitionId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "User-ID": localStorage.getItem("userId"),
          "User-Role": localStorage.getItem("role"),
        },
        body: JSON.stringify(payload),
      });

      const data = await response.json();
      if (response.ok) {
        alert("✅ Contest updated successfully!");
        navigate(`/OrganizerContestList/${email}`);
      } else {
        alert("❌ Failed to update contest: " + (data.message || "Unknown error"));
      }
    } catch (err) {
      console.error("Error updating contest:", err);
    }
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container">
        <Sidebar />
        <div className="dashboard-content">
          <h2 className="contest-label">Edit Contest</h2>

          <TextField
            fullWidth
            name="contestName"
            value={contestData.contestName}
            onChange={handleChange}
            placeholder="Enter contest name"
            label="Contest Name"
          />

          <TextField
            fullWidth
            multiline
            rows={3}
            name="contestDescription"
            value={contestData.contestDescription}
            onChange={handleChange}
            placeholder="Describe your contest"
            label="Description"
          />

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

          <div className="date-picker">
            <TextField type="date" name="startDate" value={contestData.startDate} onChange={handleChange} label="Start Date" />
            <TextField type="date" name="endDate" value={contestData.endDate} onChange={handleChange} label="End Date" />
          </div>

          <h2 className="contest-label">Scoring Criteria</h2>
          <div className="scoring-input">
            <TextField
              fullWidth
              value={newCriteria}
              onChange={(e) => setNewCriteria(e.target.value)}
              placeholder="Enter scoring criteria"
            />
            <Button variant="contained" onClick={addCriteria}>Add</Button>
          </div>
          <ul className="scoring-list">
            {contestData.scoringCriteria.map((c, i) => (
              <li key={i} className="scoring-item">
                {c}
                <IconButton onClick={() => removeCriteria(i)} size="small"><Delete /></IconButton>
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
              <MenuItem value="" disabled>Select participation type</MenuItem>
              <MenuItem value="INDIVIDUAL">INDIVIDUAL</MenuItem>
              <MenuItem value="TEAM">TEAM</MenuItem>
            </Select>
          </FormControl>

          <h2 className="contest-label">Public / Private</h2>
          <RadioGroup row name="isPublic" value={contestData.isPublic} onChange={handleChange}>
            <FormControlLabel value="Public" control={<Radio />} label="Public" />
            <FormControlLabel value="Private" control={<Radio />} label="Private" />
          </RadioGroup>

          <h2 className="contest-label">Competition Status</h2>
          <TextField
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
            onClick={handleUpdate}
          >
            Update Contest
          </Button>

          <Button
            variant="outlined"
            color="secondary"
            style={{ marginTop: "10px" }}
            onClick={() => navigate(`/OrganizerContestList/${email}`)}
          >
            Cancel
          </Button>

        </div>
      </div>
    </>
  );
}

export default EditContest;
