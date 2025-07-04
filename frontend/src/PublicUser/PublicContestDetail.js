/**
 * PublicContestDetail.js
 * 
 * This component displays the details of a public contest, including:
 * - Contest title, description, category, status, and associated images.
 * - Navigates to related works when the "View related works" button is clicked.
 * - Allows users to navigate through images of the contest and view more details such as submission types, scoring criteria, and intro video.
 * 
 * Role: Public User
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, CircularProgress, IconButton, Button } from "@mui/material";
import { ArrowBack } from "@mui/icons-material";
import Navbar from "../Homepages/Navbar";
import Footer from "../Homepages/Footer";
import "./UserContestList.css";
import defaultImage from "./1.jpg";

function PublicContestDetail() {
  const { id: contestId } = useParams();
  const navigate = useNavigate();
  const [contestDetail, setContestDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    const fetchContestDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await fetch(`http://localhost:8080/competitions/${contestId}`);
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
    };

    fetchContestDetail();
  }, [contestId]);

  if (loading) {
    return (
      <>
        <Navbar />
        <div className="publicuser-main-wrapper">
          <div className="publicuser-content-wrapper">
            <CircularProgress sx={{ marginTop: "50px" }} />
          </div>
        </div>
        <Footer />
      </>
    );
  }

  if (error || !contestDetail) {
    return (
      <>
        <Navbar />
        <div className="publicuser-main-wrapper">
          <div className="publicuser-content-wrapper">
            <Typography variant="h6" color="error" align="center" sx={{ mt: 5 }}>
              {error || "No contest details available."}
            </Typography>
          </div>
        </div>
        <Footer />
      </>
    );
  }

  return (
    <>
      <Navbar />
      <div className="publicuser-main-wrapper">
        <div className="publicuser-content-wrapper">
          <div className="publicuser-contest-container">
            {/* return button */}
            <Button
              onClick={() => navigate(-1)}
              startIcon={<ArrowBack />}
              sx={{
                mb: 2,
                alignSelf: "flex-start",
                color: "#632713",
                border: "1px solid #632713",
                textTransform: "none",
                fontWeight: "bold",
                '&:hover': {
                  backgroundColor: "#f4e1d2",
                  borderColor: "#632713",
                }
              }}
            >
              Back to List
            </Button>

            {/* Layout of images and text */}
            <Box sx={{ display: "flex", flexDirection: ["column", "row"], gap: 3 }}>
              <Box
                sx={{
                  flex: 1,
                  position: "relative",
                  backgroundColor: "#f4a986",
                  height: 400,
                  borderRadius: 3,
                  overflow: "hidden",
                }}
              >
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
                  }}
                />

                {/* Switch arrow */}
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

              {/* Details on the right */}
              <Box sx={{ flex: 2, display: "flex", flexDirection: "column", gap: 3 }}>
                {/* Title, description and button */}
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: "bold", color: "#632713" }}>
                    {contestDetail.name}
                  </Typography>
                  <Typography variant="body1" sx={{ mt: 1 }}>
                    {contestDetail.description}
                  </Typography>
                  <Box sx={{ mt: 2 }}>
                    <Button
                      className="view-work-button"
                      onClick={() => navigate(`/work-list?competitionId=${contestId}`)}
                    >
                      View related works
                    </Button>
                  </Box>
                </Box>

                {/* Contest details */}
                <Box
                  sx={{
                    p: 2,
                    backgroundColor: "#d17f5c",
                    borderRadius: 2,
                    color: "#fff",
                    boxShadow: 2,
                  }}
                >
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Competition Details
                  </Typography>
                  <DetailRow label="Category" value={contestDetail.category} />
                  <DetailRow label="Public" value={contestDetail.isPublic ? "Yes" : "No"} />
                  <DetailRow label="Status" value={contestDetail.status} />
                  <DetailRow
                    label="Start Date"
                    value={new Date(contestDetail.startDate).toLocaleString()}
                  />
                  <DetailRow
                    label="End Date"
                    value={new Date(contestDetail.endDate).toLocaleString()}
                  />
                  <DetailRow
                    label="Allowed Submission Types"
                    value={contestDetail.allowedSubmissionTypes?.join(", ")}
                  />
                  <DetailRow label="Participation Type" value={contestDetail.participationType} />

                  <DetailRow
                    label="Scoring Criteria"
                    value={contestDetail.scoringCriteria?.join(", ")}
                  />
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
          </div>
        </div>
      </div>
      <Footer />
    </>
  );
}

// Unify the style of each line
function DetailRow({ label, value }) {
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "space-between",
        mb: 1,
        borderBottom: "1px solid rgba(255,255,255,0.4)",
        paddingBottom: "5px",
      }}
    >
      <Typography variant="subtitle2">{label}:</Typography>
      <Typography variant="body2">{value || "N/A"}</Typography>
    </Box>
  );
}

export default PublicContestDetail;
