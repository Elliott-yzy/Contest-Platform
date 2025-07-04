import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import ResetPassword from './pages/ResetPassword';

/* Participant Pages */
import Profile from './Participant/profile/Profile.js';
import Contest from './Participant/contest/Contest.js';
import PContestDetail from './Participant/contest/ContestDetail.js';
import Project from './Participant/project/Project.js';
import Rating from './Participant/Rating';
import ViewSubmission from "./Participant/contest/ViewSubmission.js";
import CommentsPage from "./Participant/contest/CommentsPage.js";
import ProjectDetail from './Participant/project/Projectdetail.js';
import JudgeSubmissions from './Participant/JudgeSubmissions';
import RatingDetail from './Participant/RatingDetail';
import ReRating from './Participant/ReRating';
import TeamProjectDetail from './Participant/team/TeamProjectDetail.js';
import TeamPage from './Participant/team/TeamPage.js';

/* Organizer Pages */
import OrganizerProfile from './Organizer/Profile';
import OrganizerDashboard from './Organizer/Dashboard';
import OrganizerContestList from './Organizer/ContestList';
import OrganizerContest from './Organizer/Contest';
import OrganizerEditContest from './Organizer/EditContest';
import OrganizerUploadMedia from './Organizer/UploadMedia';
import OrganizerParticipantList from './Organizer/ParticipantList';
import OrganizerSubmissions from './Organizer/CheckSubmissions';
import SubmissionRatings from './Organizer/SubmissionRatings';
import OrganizerAddJudge from './Organizer/OrganizerAddJudge';

/* Admin Pages */
import AdminProfile from './Admin/AdminProfile';
import AdminAccountManage from './Admin/AdminAccountManage';
import AdminCompetitionsManage from './Admin/AdminCompetitionsManage';
import AdminDashboard from './Admin/AdminDashboard';

/* Public User Pages */
import ContestList from './PublicUser/UserContestList.js';
import PublicContestDetail from './PublicUser/PublicContestDetail';
import HowToUse from './PublicUser/HowToUse';
import WorkList from './PublicUser/WorkList';
import LoginForm from './Homepages/Login';
import OAuthCallback from './pages/OAuthCallback';
import PublicuserComents from './PublicUser/ComentsPage';
import TeamListPage from './PublicUser/TeamListPage.js';
import TeamPublicDetail from './PublicUser/TeamPublicDetail.js';

/* Homepage and Loading */
import HomePage from './Homepages/Homepage';
import Loading from './Homepages/Loading.js';

function App() {
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(false);
    }, 3000);
    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return <Loading />;
  }

  return (
    <Router>
      <Routes>
        {/* Homepage */}
        <Route path="/" element={<HomePage />} />

        {/* System-related pages */}
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/oauth/callback" element={<OAuthCallback />} />

        {/* Participant Pages */}
        <Route path="/profile/:email" element={<Profile />} />
        <Route path="/contest/:email" element={<Contest />} />
        <Route path="/teams/:email" element={<TeamPage />} />
        <Route path="/project/:email" element={<Project />} />
        <Route path="/rating/:email" element={<Rating />} />

        {/* Contests */}
        <Route path="/contest-detail/:id" element={<PContestDetail />} />
        <Route path="/JudgeSubmissions/:competitionId" element={<JudgeSubmissions />} />
        <Route path="/RatingDetail/:competitionId/:submissionId" element={<RatingDetail />} />
        <Route path="/ReRating/:competitionId/:submissionId" element={<ReRating />} />
        <Route path="/view-submission/:competitionId" element={<ViewSubmission />} />
        <Route path="/comments/:submissionId" element={<CommentsPage />} />

        {/* Projects */}
        <Route path="/team-project-detail/:competitionId/team/:teamId" element={<TeamProjectDetail />} />
        <Route path="/project-detail/:competitionId" element={<ProjectDetail />} />
        <Route path="/publicusercoments/:submissionId" element={<PublicuserComents />} />

        {/* Organizer Pages */}
        <Route path="/OrganizerProfile/:email" element={<OrganizerProfile />} />
        <Route path="/OrganizerDashboard/:email" element={<OrganizerDashboard />} />
        <Route path="/OrganizerContestList/:email" element={<OrganizerContestList />} />
        <Route path="/OrganizerContest/:email" element={<OrganizerContest />} />
        <Route path="/OrganizerEditContest/:email" element={<OrganizerEditContest />} />
        <Route path="/OrganizerUploadMedia/:id" element={<OrganizerUploadMedia />} />
        <Route path="/OrganizerParticipantList/:competitionId" element={<OrganizerParticipantList />} />
        <Route path="/OrganizerSubmissions/:competitionId" element={<OrganizerSubmissions />} />
        <Route path="/submissions/:competitionId/ratings" element={<SubmissionRatings />} />
        <Route path="/OrganizerAddJudge/:competitionId" element={<OrganizerAddJudge />} />

        {/* Admin Pages */}
        <Route path="/AdminProfile" element={<AdminProfile />} />
        <Route path="/AdminAccountManage" element={<AdminAccountManage />} />
        <Route path="/AllCompetitions" element={<AdminCompetitionsManage />} />
        <Route path="/AdminDashboard" element={<AdminDashboard />} />

        {/* Public User Pages */}
        <Route path="/contest-list" element={<ContestList />} />
        <Route path="/work-list" element={<WorkList />} />
        <Route path="/login" element={<LoginForm />} />
        <Route path="/how-to-use" element={<HowToUse />} />
        <Route path="/publiccontest-detail/:id" element={<PublicContestDetail />} />
        <Route path="/public-teams/:contestId" element={<TeamListPage />} />
        <Route path="/public-team-detail/:competitionId/:teamId" element={<TeamPublicDetail />} />
      </Routes>
    </Router>
  );
}

export default App;
