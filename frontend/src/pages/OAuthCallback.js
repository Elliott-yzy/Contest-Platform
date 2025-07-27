/**
 * OAuthCallback.js
 * 
 * This component handles the generic OAuth callback flow.
 * It extracts the token, email, role, and userId from the URL parameters,
 * stores them in localStorage, and redirects the user based on their role:
 * Participant → Profile page, Organizer → Organizer Dashboard, otherwise → Home page.
 * If required parameters are missing, it shows an alert and redirects to home.
 * 
 * Developer: Zhaoyi Yang
 */


import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

function OAuthCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  const email = searchParams.get('email');
  const role = searchParams.get('role');
  const userId = searchParams.get('userId');

  useEffect(() => {
    if (token && email && role) {
      localStorage.setItem("userId", userId);
      localStorage.setItem('token', token);
      localStorage.setItem('email', email);
      localStorage.setItem('role', role);

      if (role === 'Participant') {
        navigate(`/profile/${email}`);
      } else if (role === 'Organizer') {
        navigate(`/OrganizerDashboard/${email}`);
      } else {
        navigate('/');
      }
    } else {
      alert('Missing required info from OAuth redirect.');
      navigate('/');
    }
  }, [token, email, role, userId, navigate]);

  return <p>Logging you in via OAuth...</p>;
}

export default OAuthCallback;