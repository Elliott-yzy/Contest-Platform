/**
 * @file ContestDetail.js
 * @description 
 * This component displays detailed information for a selected contest.
 * Participants can:
 *  - View a full overview of the contest including description, category, participation type, and status.
 *  - Browse through multiple contest images with navigation controls.
 *  - Watch the introductory video if available.
 *  - See detailed competition settings such as allowed submission types and scoring criteria.
 * The component fetches contest details from the backend API and provides a structured UI layout
 * using Material-UI components for styling.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


import React, { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, CircularProgress, IconButton } from "@mui/material";
import { ArrowBack } from "@mui/icons-material";
import TopBar from "../TopSide/TopBar";
import Sidebar from "../TopSide/Sidebar";
import "./Contest.css";

// Default image
import defaultImage from "./1.jpg";

// Dark line component
const DarkDetailRow = ({ label, value }) => (
  <Box
    sx={{
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      mb: 2,
      borderBottom: "1px solid rgba(255,255,255,0.3)",
      pb: 1,
    }}
  >
    <Typography variant="subtitle1" sx={{ color: "#fff" }}>
      {label}
    </Typography>
    <Typography variant="body1" sx={{ color: "#fff" }}>
      {value}
    </Typography>
  </Box>
);

function ContestDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [contestDetail, setContestDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Which picture is displayed currently
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  // Get details of the competition
  const fetchContestDetail = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/competitions/${id}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setContestDetail(data);
    } catch (err) {
      console.error("Error fetching contest details:", err);
      setError("Failed to load contest details.");
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchContestDetail();
  }, [fetchContestDetail]);

  return (
    <>
      <TopBar />
      <div
        className="participant-contest-container"
        style={{
          display: "flex",
          backgroundColor: "#ffffff",
          minHeight: "100vh",
        }}
      >
        <Sidebar />
        <div className="participant-contest-content" style={{ flex: 1, padding: "20px" }}>
          {/* return button */}
          <Box sx={{ display: "flex", alignItems: "center", mb: 2 }}>
            <IconButton onClick={() => navigate(-1)} sx={{ marginRight: 2 }}>
              <ArrowBack />
            </IconButton>
            <Typography variant="h6">Back To Contest List</Typography>
          </Box>

          {error && (
            <Typography variant="body2" color="error" gutterBottom>
              {error}
            </Typography>
          )}
          {loading ? (
            <CircularProgress />
          ) : contestDetail ? (
            <Box sx={{ display: "flex", flexDirection: "row", gap: 3 }}>
              {/* The image area on the left */}
              <Box
                sx={{
                  position: "relative",
                  flex: 1,
                  backgroundColor: "#EC6426",
                  height: 400,
                  borderRadius: 3,
                  overflow: "hidden",
                }}
              >
                {/* Background image */}
                <Box
                  sx={{
                    width: "100%",
                    height: "100%",
                    backgroundImage: `url(${contestDetail.imageUrls && contestDetail.imageUrls.length > 0
                      ? contestDetail.imageUrls[currentImageIndex]
                      : defaultImage
                      })`,
                    backgroundSize: "cover",
                    backgroundPosition: "center",
                    borderRadius: 3,
                  }}
                />

                {/* Switch between the left and right arrows */}
                {contestDetail.imageUrls && contestDetail.imageUrls.length > 1 && (
                  <>
                    <IconButton
                      sx={{
                        position: "absolute",
                        top: "50%",
                        left: 10,
                        transform: "translateY(-50%)",
                        backgroundColor: "rgba(255, 255, 255, 0.6)",
                        ":hover": { backgroundColor: "rgba(255, 152, 0, 0.8)" },
                      }}
                      onClick={() =>
                        setCurrentImageIndex((prev) =>
                          prev === 0 ? contestDetail.imageUrls.length - 1 : prev - 1
                        )
                      }
                    >
                      {"<"}
                    </IconButton>

                    <IconButton
                      sx={{
                        position: "absolute",
                        top: "50%",
                        right: 10,
                        transform: "translateY(-50%)",
                        backgroundColor: "rgba(255, 255, 255, 0.6)",
                        ":hover": { backgroundColor: "rgba(255, 152, 0, 0.8)" },
                      }}
                      onClick={() =>
                        setCurrentImageIndex((prev) =>
                          prev === contestDetail.imageUrls.length - 1 ? 0 : prev + 1
                        )
                      }
                    >
                      {">"}
                    </IconButton>
                  </>
                )}
              </Box>

              {/* The content area on the right side of the mold */}
              <Box sx={{ flex: 2, display: "flex", flexDirection: "column", gap: 3 }}>
                {/* Module 1: Overview of the Competition */}
                <Box
                  sx={{
                    p: 2,
                    backgroundColor: "#FFF",
                    borderRadius: 2,
                  }}
                >
                  <Typography variant="h4" sx={{ color: "#632713", fontWeight: "bold" }}>
                    {contestDetail.name}
                  </Typography>

                  <Typography variant="body1" sx={{ mt: 1, color: "#632713" }}>
                    {contestDetail.description}
                  </Typography>
                </Box>

                {/* Module 2: Competition Details */}
                <Box
                  sx={{
                    p: 2,
                    backgroundColor: "#d17f5c",
                    borderRadius: 2,
                    color: "#fff",
                  }}
                >
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Competition Details
                  </Typography>


                  <DarkDetailRow label="Category:" value={contestDetail.category} />
                  <DarkDetailRow label="Public:" value={contestDetail.isPublic ? "Yes" : "No"} />
                  <DarkDetailRow label="Status:" value={contestDetail.status} />
                  <DarkDetailRow label="Participation Type" value={contestDetail.participationType} />
                  <DarkDetailRow
                    label="Start Date:"
                    value={new Date(contestDetail.startDate).toLocaleString()}
                  />
                  <DarkDetailRow
                    label="End Date:"
                    value={new Date(contestDetail.endDate).toLocaleString()}
                  />
                  <DarkDetailRow
                    label="Allowed Submission Types:"
                    value={contestDetail.allowedSubmissionTypes?.join(", ")}
                  />
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle1" sx={{ color: "#fff" }}>
                      Scoring Criteria:
                    </Typography>
                    <Box sx={{ pl: 2, mt: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
                      {contestDetail.scoringCriteria?.length > 0 ? (
                        contestDetail.scoringCriteria.map((criteria, idx) => (
                          <Typography key={idx} variant="body2" sx={{ color: "#fff" }}>
                            • {criteria}
                          </Typography>
                        ))
                      ) : (
                        <Typography variant="body2" sx={{ color: "#fff" }}>
                          No scoring criteria available.
                        </Typography>
                      )}
                    </Box>
                  </Box>

                  {contestDetail.introVideoUrl && (
                    <Box sx={{ mt: 3 }}>
                      <Typography variant="subtitle1" sx={{ color: "#fff", mb: 1 }}>
                        Intro Video:
                      </Typography>
                      <video
                        src={contestDetail.introVideoUrl}
                        controls
                        style={{ width: "100%", borderRadius: "10px", backgroundColor: "#000" }}
                      >
                        Your browser does not support the video tag.
                      </video>
                    </Box>
                  )}
                </Box>
              </Box>
            </Box>
          ) : (
            <Typography variant="body1">No contest details available.</Typography>
          )}
        </div>
      </div>
    </>
  );

}

export default ContestDetail;