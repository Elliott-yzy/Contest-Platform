/**
 * GoogleCallback.js
 * 
 * This component handles the OAuth callback after Google authentication.
 * It retrieves the authorization code and state from the URL,
 * exchanges them for a token by calling the backend API,
 * stores user information (email, token, role) in localStorage,
 * and redirects the user to their profile page.
 * 
 * Developer: Zhaoyi Yang
 */


import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';

function GoogleCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const code = searchParams.get('code');
  const state = searchParams.get('state');

  useEffect(() => {
    const fetchToken = async () => {
      try {
        const res = await fetch(`http://localhost:8080/users/oauth/callback/google?code=${code}&state=${state}`);
        const data = await res.json();
        if (res.ok) {
          localStorage.setItem('email', data.email);
          localStorage.setItem('token', data.accessToken);
          localStorage.setItem('role', data.role);
          navigate(`/profile/${data.email}`);
        } else {
          alert(data.message || 'Google login failed');
        }
      } catch (err) {
        console.error('Google login error:', err);
        alert('Server error');
      }
    };

    if (code && state) {
      fetchToken();
    }
  }, [code, state, navigate]);

  return <p>Logging you in with Google...</p>;
}

export default GoogleCallback;
