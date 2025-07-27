/**
 * TeamListPage.js
 * 
 * This component displays a list of registered teams for a specific contest.
 * - It fetches the list of teams registered in the contest using the contest ID.
 * - Displays loading spinner while data is being fetched.
 * - Shows an error message if there was an issue with fetching the teams.
 * - If no teams are found, a message is shown indicating that no teams have registered yet.
 * 
 * Each team name is clickable, and upon clicking, the user is navigated to the team detail page where they can view more information about that team.
 * 
 * Role: Public User
 * Developer: Ziqi Yi
 */


import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { CircularProgress, Typography, Button, Box } from "@mui/material";
import Navbar from "../Homepages/Navbar";
import Footer from "../Homepages/Footer";
import "./TeamListPage.css";

function TeamListPage() {
  const { contestId } = useParams();
  const navigate = useNavigate();
  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTeams = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/registrations/public/${contestId}/teams?page=1&size=100`
        );
        if (!response.ok) {
          throw new Error("Failed to fetch team data");
        }
        const result = await response.json();
        setTeams(result.data || []);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchTeams();
  }, [contestId]);

  return (
    <>
      <Navbar />
      <div className="public-team-list-container">
        <Button
          onClick={() => navigate(-1)}
          className="public-back-button"
        >
          ← Back to Contest
        </Button>

        <Typography variant="h4" className="public-page-title">
          Registered Teams
        </Typography>

        {loading ? (
          <Box sx={{ display: "flex", justifyContent: "center", mt: 5 }}>
            <CircularProgress />
          </Box>
        ) : error ? (
          <Typography color="error" align="center" sx={{ mt: 5 }}>
            {error}
          </Typography>
        ) : teams.length === 0 ? (
          <Typography align="center" sx={{ mt: 5 }}>
            No teams registered yet.
          </Typography>
        ) : (
          <div className="public-team-list">
            {teams.map((team, index) => (
              <div
                className="public-team-card"
                key={index}
                onClick={() =>
                  navigate(`/public-team-detail/${contestId}/${team.id}`, {
                    state: {
                      teamName: team.name,
                      teamDescription: team.description,
                    },
                  })
                }
                style={{ cursor: "pointer" }}
              >
                <Typography variant="h6" className="public-team-name">
                  {team.name || "Unnamed Team"}
                </Typography>
              </div>

            ))}
          </div>
        )}
      </div>
      <Footer />
    </>
  );

}

export default TeamListPage;
