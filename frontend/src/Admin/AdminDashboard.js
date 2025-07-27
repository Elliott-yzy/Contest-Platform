/**
 * @file AdminDashboard.jsx
 * @description 
 * This component provides the administrative dashboard for the platform.
 * It allows admin users to:
 *  - View key platform statistics including total competitions, participants, and submissions.
 *  - Visualize breakdowns of individual and team participants/submissions using bar charts.
 *  - Monitor submission trends over time.
 *  - Monitor participant registration trends (if available).
 * 
 * The component fetches public platform overview data from the backend,
 * processes the raw data into formats suitable for visual display,
 * and renders the information using Recharts and Material-UI components.
 * Interactive features such as tabs and tooltips are also supported for better data navigation.
 * 
 * Role: Admin
 * Developer: Zhaoyi Yang
 */


// src/pages/AdminDashboard.jsx
import React, { useEffect, useState, useMemo } from "react";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import {
  Box,
  Typography,
  CircularProgress,
  Paper,
  Tooltip,
  Tabs,
  Tab,
} from "@mui/material";
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as ReTooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const COLORS = ["#0088FE", "#FF8042", "#00C49F", "#8884d8"];

function AdminDashboard() {
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(0);

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch(
          "http://localhost:8080/dashboard/public/platform-overview"
        );
        const data = await res.json();
        setOverview(data || null);
      } catch (e) {
        console.error("Dashboard fetch error:", e);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const {
    cards,
    barData,
    submissionTrend,
    participantTrend,
    hasParticipantTrend,
  } = useMemo(() => {
    if (!overview)
      return {
        cards: [],
        barData: [],
        submissionTrend: [],
        participantTrend: [],
        hasParticipantTrend: false,
      };

    const approvePct =
      overview.totalSubmissions > 0
        ? Number(
          (
            (overview.approvedSubmissions / overview.totalSubmissions) *
            100
          ).toFixed(1)
        )
        : 0;

    const upcoming =
      overview.totalCompetitions -
      overview.activeCompetitions -
      overview.finishedCompetitions;

    const cardsArr = [
      {
        id: "competitions",
        title: "Competitions",
        value: overview.totalCompetitions,
        tooltip: [
          { k: "Active", v: overview.activeCompetitions },
          { k: "Finished", v: overview.finishedCompetitions },
          { k: "Upcoming", v: upcoming },
        ],
      },
      {
        id: "participants",
        title: "Participants",
        value: overview.totalParticipants,
        tooltip: [
          { k: "Individual", v: overview.individualParticipants },
          { k: "Team", v: overview.teamParticipants },
        ],
      },
      {
        id: "submissions",
        title: "Submissions",
        value: overview.totalSubmissions,
        tooltip: [
          { k: "Individual", v: overview.individualSubmissions },
          { k: "Team", v: overview.teamSubmissions },
          { k: "Approved", v: overview.approvedSubmissions },
        ],
      },
      {
        id: "approval",
        title: "Approval Rate",
        value: `${approvePct}%`,
      },
    ];

    const barArr = [
      {
        name: "Counts",
        "Ind Participants": overview.individualParticipants,
        "Team Participants": overview.teamParticipants,
        "Ind Submissions": overview.individualSubmissions,
        "Team Submissions": overview.teamSubmissions,
      },
    ];

    const subTrendMap = overview.submissionTrend || {};
    const submissionTrendArr = Object.keys(subTrendMap)
      .sort()
      .map((d) => ({ date: d, value: subTrendMap[d] }));

    const partTrend = overview.participantTrend || {};
    const indMap = partTrend.individual || {};
    const teamMap = partTrend.team || {};
    const mergedDates = Array.from(
      new Set([...Object.keys(indMap), ...Object.keys(teamMap)])
    ).sort();
    const participantTrendArr = mergedDates.map((d) => ({
      date: d,
      individual: indMap[d] || 0,
      team: teamMap[d] || 0,
    }));

    return {
      cards: cardsArr,
      barData: barArr,
      submissionTrend: submissionTrendArr,
      participantTrend: participantTrendArr,
      hasParticipantTrend: participantTrendArr.length > 0,
    };
  }, [overview]);

  const MetricCard = ({ title, value, tooltip = [] }) => {
    const content =
      tooltip.length === 0 ? (
        ""
      ) : (
        <Box>
          {tooltip.map((t) => (
            <Typography key={t.k} fontSize={13}>
              {t.k}: <strong>{t.v}</strong>
            </Typography>
          ))}
        </Box>
      );

    const card = (
      <Paper
        elevation={1}
        sx={{
          p: 2,
          width: 180,
          height: 100,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          borderRadius: 2,
          backgroundColor: "#f0f0f0",
        }}
      >
        <Typography variant="body2" color="text.secondary">
          {title}
        </Typography>
        <Typography variant="h5" fontWeight={700} color="#22507a">
          {value}
        </Typography>
      </Paper>
    );
    return tooltip.length > 0 ? (
      <Tooltip arrow title={content}>
        {card}
      </Tooltip>
    ) : (
      card
    );
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container" style={{ display: "flex" }}>
        <Sidebar />
        <Box flex={1} p={2}>
          <Typography variant="h5" gutterBottom>
            📊 Platform Dashboard
          </Typography>

          {loading ? (
            <Box display="flex" justifyContent="center" mt={6}>
              <CircularProgress />
            </Box>
          ) : !overview ? (
            <Typography color="text.secondary">(No data)</Typography>
          ) : (
            <>
              <Box mb={4} display="flex" gap={2} flexWrap="wrap">
                {cards.map(({ id, ...rest }) => (
                  <MetricCard key={id} {...rest} />
                ))}
              </Box>

              <Box mb={5}>
                <Typography variant="subtitle1" gutterBottom>
                  Participant / Submission Breakdown
                </Typography>
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={barData} barGap={4} maxBarSize={40}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" hide />
                    <YAxis allowDecimals={false} />
                    <Legend />
                    <ReTooltip />
                    <Bar
                      dataKey="Ind Participants"
                      stackId="a"
                      fill={COLORS[0]}
                    />
                    <Bar
                      dataKey="Team Participants"
                      stackId="a"
                      fill={COLORS[1]}
                    />
                    <Bar dataKey="Ind Submissions" stackId="b" fill={COLORS[2]} />
                    <Bar
                      dataKey="Team Submissions"
                      stackId="b"
                      fill={COLORS[3]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </Box>

              <Box>
                <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 1 }}>
                  <Tab label="Submission Trend" />
                  {hasParticipantTrend && <Tab label="Participant Trend" />}
                </Tabs>

                {tab === 0 && (
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={submissionTrend}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis allowDecimals={false} />
                      <ReTooltip />
                      <Line
                        type="monotone"
                        dataKey="value"
                        stroke={COLORS[0]}
                        dot={{ r: 2 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                )}

                {tab === 1 && hasParticipantTrend && (
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={participantTrend}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="date" />
                      <YAxis allowDecimals={false} />
                      <ReTooltip />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="individual"
                        stroke={COLORS[0]}
                        dot={{ r: 2 }}
                        name="Individual"
                      />
                      <Line
                        type="monotone"
                        dataKey="team"
                        stroke={COLORS[1]}
                        dot={{ r: 2 }}
                        name="Team"
                      />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </Box>
            </>
          )}
        </Box>
      </div>
    </>
  );
}

export default AdminDashboard;
