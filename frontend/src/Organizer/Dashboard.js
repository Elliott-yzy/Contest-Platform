/**
 * @file OrganizerDashboard.js
 * @description 
 * This component displays an analytical dashboard for organizers to monitor their competitions.
 * It allows organizers to:
 *  - View overall metrics such as number of participants, submissions, judges, and approval rate.
 *  - Visualize the distribution of competition statuses using a pie chart.
 *  - Select individual competitions to view participant or submission trend over time.
 * 
 * The component fetches detailed statistics from backend APIs,
 * processes aggregated metrics and trends,
 * and uses Material-UI and Recharts for visualization and interactive controls.
 * 
 * Role: Organizer
 * Developer: Zhaoyi Yang
 */


import React, { useEffect, useState, useMemo } from "react";
import TopBar from "./TopBar";
import Sidebar from "./Sidebar";
import {
  Box,
  Typography,
  CircularProgress,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tooltip,
} from "@mui/material";
import {
  PieChart,
  Pie,
  Cell,
  Tooltip as ReTooltip,
  Legend,
  ResponsiveContainer,
  CartesianGrid,
  XAxis,
  YAxis,
  LineChart,
  Line,
} from "recharts";

const COLORS = [
  "#0088FE",
  "#00C49F",
  "#FFBB28",
  "#FF8042",
  "#8884d8",
  "#82ca9d",
  "#a4de6c",
  "#d0ed57",
];

function OrganizerDashboard() {
  const [stats, setStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedComp, setSelectedComp] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem("token");
        const userId = localStorage.getItem("userId");
        const role = localStorage.getItem("role");

        const res = await fetch(
          "http://localhost:8080/competitions/achieve/my?page=1&size=100",
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "User-ID": userId,
              "User-Role": role,
            },
          }
        );
        const { data: competitions = [] } = await res.json();

        const list = await Promise.all(
          competitions.map(async (c) => {
            const [statRes, detailRes] = await Promise.all([
              fetch(
                `http://localhost:8080/dashboard/public/statistics?competitionId=${c.id}`,
                { headers: { Authorization: `Bearer ${token}` } }
              ),
              fetch(`http://localhost:8080/competitions/${c.id}`, {
                headers: { Authorization: `Bearer ${token}` },
              }),
            ]);
            const stat = await statRes.json();
            const detail = await detailRes.json();

            const totalSubs = stat.submissionCount || 0;
            const approved = stat.approvedSubmissionCount || 0;

            return {
              id: c.id,
              name: c.name,
              status: (detail.status || detail.competitionStatus || "UNKNOWN").toUpperCase(),
              regs:
                stat.participationType === "INDIVIDUAL"
                  ? stat.individualParticipantCount || 0
                  : stat.teamParticipantCount || 0,
              subs: totalSubs,
              judges: stat.judgeCount || 0,
              approvePct: totalSubs > 0 ? (approved / totalSubs) * 100 : 0,
              trend:
                Object.keys(stat.individualParticipantTrend || {}).length > 0
                  ? stat.individualParticipantTrend
                  : Object.keys(stat.teamParticipantTrend || {}).length > 0
                    ? stat.teamParticipantTrend
                    : stat.submissionTrend,
            };
          })
        );

        setStats(list);
      } catch (e) {
        console.error("Dashboard fetch error:", e);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const { totals, tooltipMap } = useMemo(() => {
    const sum = { regs: 0, subs: 0, judges: 0 };
    const listRegs = [];
    const listSubs = [];
    const listJudges = [];
    const listApprove = [];

    stats.forEach((s) => {
      sum.regs += s.regs;
      sum.subs += s.subs;
      sum.judges += s.judges;

      listRegs.push({ name: s.name, value: s.regs });
      listSubs.push({ name: s.name, value: s.subs });
      listJudges.push({ name: s.name, value: s.judges });
      listApprove.push({ name: s.name, value: `${s.approvePct.toFixed(1)}%` });
    });

    const approvePct =
      sum.subs > 0
        ? Number(
          (
            (stats.reduce((acc, s) => acc + s.subs * (s.approvePct / 100), 0) /
              sum.subs) *
            100
          ).toFixed(1)
        )
        : 0;

    return {
      totals: { ...sum, approvePct },
      tooltipMap: {
        regs: listRegs,
        subs: listSubs,
        judges: listJudges,
        approvePct: listApprove,
      },
    };
  }, [stats]);

  const statusPie = useMemo(() => {
    const count = {};
    stats.forEach((s) => {
      count[s.status] = (count[s.status] || 0) + 1;
    });
    return Object.entries(count).map(([name, value], i) => ({
      name,
      value,
      fill: COLORS[i % COLORS.length],
    }));
  }, [stats]);

  const hasTrendData = useMemo(
    () => stats.some((s) => Object.keys(s.trend || {}).length > 0),
    [stats]
  );

  const currentTrend = useMemo(() => {
    const t = stats.find((s) => s.id === selectedComp);
    if (!t) return [];
    return Object.entries(t.trend || {}).map(([date, count]) => ({ date, count }));
  }, [selectedComp, stats]);

  const MetricCard = ({ label1, label2, value, unit = "", tooltipKey }) => {
    const rows = tooltipMap[tooltipKey] || [];
    const title = (
      <Box>
        {rows.map((r) => (
          <Typography key={r.name} fontSize={13}>
            {r.name}: <strong>{r.value}</strong>
          </Typography>
        ))}
      </Box>
    );

    return (
      <Tooltip arrow placement="top" title={title}>
        <Paper
          elevation={1}
          sx={{
            p: 2,
            width: 180,
            height: 110,
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            borderRadius: 2,
            backgroundColor: "#f0f0f0",
            cursor: "default",
          }}
        >
          <Typography variant="body2" color="text.secondary">
            {label1}
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={1}>
            {label2}
          </Typography>
          <Typography variant="h5" fontWeight={700} color="#22507a">
            {value}
            {unit}
          </Typography>
        </Paper>
      </Tooltip>
    );
  };

  return (
    <>
      <TopBar />
      <div className="dashboard-container" style={{ display: "flex" }}>
        <Sidebar />
        <Box flex={1} p={2} className="dashboard-content">
          <Typography variant="h5" gutterBottom>
            📊 Competition Dashboard
          </Typography>

          {loading ? (
            <Box display="flex" justifyContent="center" mt={6}>
              <CircularProgress />
            </Box>
          ) : stats.length === 0 ? (
            <Typography color="text.secondary">(No competitions yet)</Typography>
          ) : (
            <>
              <Box mb={4}>
                <Typography variant="subtitle1" gutterBottom>
                  Overall Metrics
                </Typography>
                <Box
                  display="flex"
                  gap={2}
                  flexWrap="wrap"
                  justifyContent="center"
                >
                  <MetricCard
                    label1="Participants"
                    label2="Number"
                    value={totals.regs}
                    tooltipKey="regs"
                  />
                  <MetricCard
                    label1="Submissions"
                    label2="Number"
                    value={totals.subs}
                    tooltipKey="subs"
                  />
                  <MetricCard
                    label1="Judges"
                    label2="Number"
                    value={totals.judges}
                    tooltipKey="judges"
                  />
                  <MetricCard
                    label1="Approval"
                    label2="Rate"
                    value={totals.approvePct}
                    unit="%"
                    tooltipKey="approvePct"
                  />
                </Box>
              </Box>

              <Box mb={2}>
                <Box
                  sx={{
                    display: "flex",
                    flexWrap: "wrap",
                    gap: 4,
                    width: "100%",
                  }}
                >
                  <Box flex="1 1 0" minWidth={0}>
                    <Typography variant="subtitle1" gutterBottom>
                      Status Distribution
                    </Typography>
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={statusPie}
                          dataKey="value"
                          nameKey="name"
                          cx="50%"
                          cy="50%"
                          outerRadius="75%"
                          label
                        >
                          {statusPie.map((d, i) => (
                            <Cell key={i} fill={d.fill} />
                          ))}
                        </Pie>
                        <ReTooltip />
                        <Legend wrapperStyle={{ fontSize: 12 }} />
                      </PieChart>
                    </ResponsiveContainer>
                  </Box>

                  {hasTrendData && (
                    <Box flex="1 1 0" minWidth={0}>
                      <Typography variant="subtitle1" gutterBottom>
                        Trend Viewer
                      </Typography>
                      <FormControl size="small" sx={{ minWidth: 240, mb: 1 }}>
                        <InputLabel id="comp-select-label">Select competition</InputLabel>
                        <Select
                          labelId="comp-select-label"
                          value={selectedComp}
                          label="Select competition"
                          onChange={(e) => setSelectedComp(e.target.value)}
                        >
                          {stats
                            .filter((s) => Object.keys(s.trend || {}).length > 0)
                            .map((s) => (
                              <MenuItem key={s.id} value={s.id}>
                                {s.name}
                              </MenuItem>
                            ))}
                        </Select>
                      </FormControl>

                      {currentTrend.length > 0 ? (
                        <ResponsiveContainer width="100%" height={260}>
                          <LineChart data={currentTrend}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" fontSize={11} />
                            <YAxis allowDecimals={false} fontSize={11} />
                            <ReTooltip />
                            <Line
                              type="monotone"
                              dataKey="count"
                              stroke="#8884d8"
                              dot={{ r: 2 }}
                            />
                          </LineChart>
                        </ResponsiveContainer>
                      ) : (
                        <Typography color="text.secondary" fontSize={12}>
                          (No trend data)
                        </Typography>
                      )}
                    </Box>
                  )}
                </Box>
              </Box>
            </>
          )}
        </Box>
      </div>
    </>
  );
}

export default OrganizerDashboard;
