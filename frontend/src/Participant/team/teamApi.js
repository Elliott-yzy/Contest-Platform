/**
 * @file teamApi.js
 * @description
 * This module provides a set of API utility functions for managing team operations 
 * such as fetching, creating, updating, joining, leaving, and deleting teams.
 * 
 * Main Functions:
 *  - fetchJoinedTeams: Fetch all teams the user has joined.
 *  - fetchTeams: Fetch paginated public teams with optional sorting and keyword filtering.
 *  - fetchMyTeams: Fetch the user's own created/joined teams.
 *  - createTeam: Create a new team.
 *  - joinTeam: Join a public team.
 *  - leaveTeam: Leave a team.
 *  - deleteTeam: Delete a team (only allowed for the creator).
 *  - updateTeam: Update a team's name and description (only allowed for the creator).
 *  - getTeamCreator: Retrieve the creator's information of a specific team.
 *  - removeTeamMember: Remove a member from a team (only allowed for the creator).
 *  - getTeamDetail: Fetch detailed team information (public endpoint).
 * 
 * Helper:
 *  - buildHeaders: Build standard headers for requests with User-ID and Authorization token.
 * 
 * Base URL:
 *  - All endpoints are prefixed with 'http://localhost:8080'
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


// ParticipantTeam/teamApi.js
const BASE_URL = 'http://localhost:8080';

export const fetchJoinedTeams = async (userData, setState) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/my-joined?page=1&size=1000`, {
      headers: buildHeaders(userData, token)
    });
    const json = await res.json();
    const ids = (json.data || []).map(t => t.id);
    setState(new Set(ids));
  } catch (e) {
    console.error('[fetchJoinedTeams]', e);
  }
};

export const fetchTeams = async ({ page, keyword, sortBy, order }, setTeams, setPages) => {
  try {
    const url = `${BASE_URL}/teams/public/all?page=${page}&size=5&sortBy=${sortBy}&order=${order}&keyword=${keyword}`;
    console.log('[Debug] fetchTeams request URL:', url);
    const res = await fetch(url, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } });
    const json = await res.json();
    console.log('[Debug] fetchTeams response:', json);
    setTeams(json.data || []);
    setPages(json.pages || 1);
  } catch (e) {
    console.error('[fetchTeams]', e);
  }
};

export const fetchMyTeams = async (userData, setState) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/my-joined?page=1&size=1000`, {
      headers: buildHeaders(userData, token)
    });
    const json = await res.json();
    setState(json.data || []);
  } catch (e) {
    console.error('[fetchMyTeams]', e);
  }
};

export const createTeam = async (name, description, userData, { onSuccess, onError }) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/create`, {
      method: 'POST',
      headers: {
        ...buildHeaders(userData, token),
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ name, description })
    });
    if (res.status === 201) onSuccess();
    else onError(`Error ${res.status}: ${await res.text()}`);
  } catch (e) {
    onError(e.message);
  }
};

export const joinTeam = async (team, userData, setState, setMsg, setSuccess, setOpen) => {
  try {
    if (team.createdBy === userData.userId) throw new Error('You are the creator');
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/${team.id}/join`, {
      method: 'POST',
      headers: buildHeaders(userData, token)
    });
    const msg = res.status === 409 ? 'Already a member' : 'Successfully joined';
    if (res.ok) {
      setState(prev => new Set(prev).add(team.id));
      setMsg(msg);
      setSuccess(true);
      setOpen(true);
    } else throw new Error(`${res.status}: ${await res.text()}`);
  } catch (e) {
    setMsg(`Join failed: ${e.message}`);
    setSuccess(false);
    setOpen(true);
  }
};

export const leaveTeam = async (id, userData, setState, setMsg, setSuccess, setOpen) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/${id}/leave`, {
      method: 'POST',
      headers: buildHeaders(userData, token)
    });
    if (res.ok) {
      setState(prev => {
        const copy = new Set(prev);
        copy.delete(id);
        return copy;
      });
      setMsg('Left successfully');
      setSuccess(true);
      setOpen(true);
    } else {
      const errMsg = res.status === 403 ? 'Team leader cannot leave' : await res.text();
      throw new Error(`${res.status}: ${errMsg}`);
    }
  } catch (e) {
    setMsg(`Leave failed: ${e.message}`);
    setSuccess(false);
    setOpen(true);
  }
};

export const deleteTeam = async (id, userData, { onSuccess, onError }) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/${id}`, {
      method: 'DELETE',
      headers: buildHeaders(userData, token)
    });

    if (res.ok) {
      onSuccess && onSuccess();
    } else if (res.status === 403) {
      onError && onError('You are not authorized to delete this team.');
    } else if (res.status === 404) {
      onError && onError('Team not found. It may have already been deleted.');
    } else {
      const text = await res.text();
      onError && onError(`Error ${res.status}: ${text}`);
    }
  } catch (e) {
    onError && onError(`Request failed: ${e.message}`);
  }
};


const buildHeaders = (userData, token) => ({
  'User-ID': userData?.userId || '',
  'User-Role': userData?.role || '',
  Authorization: `Bearer ${token}`
});

export const updateTeam = async (teamId, { name, description }, userData, { onSuccess, onError }) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`http://localhost:8080/teams/${teamId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'User-ID': userData?.userId || '',
        'User-Role': userData?.role || '',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify({ name, description })
    });
    if (res.status === 200) {
      onSuccess && onSuccess();
    } else if (res.status === 403) {
      onError && onError('You are not authorized to update this team.');
    } else if (res.status === 404) {
      onError && onError('Team not found.');
    } else {
      const text = await res.text();
      onError && onError(`Error ${res.status}: ${text}`);
    }
  } catch (err) {
    onError && onError(`Request failed: ${err.message}`);
  }
};

/**
 * Obtain the information of the team creator (for determining permissions)
 * @param {string} teamId
 * @param {object} userData
 * @returns {Promise<{ id, name, email, avatarUrl, description, createdAt }>}
 */
export const getTeamCreator = async (teamId, userData) => {
  try {
    const token = localStorage.getItem('token');
    const res = await fetch(`${BASE_URL}/teams/${teamId}/creator`, {
      method: 'GET',
      headers: buildHeaders(userData, token)
    });

    if (res.status === 200) {
      const data = await res.json();
      return data;
    } else if (res.status === 404) {
      throw new Error(`Team ${teamId} not found.`);
    } else {
      const text = await res.text();
      throw new Error(`Error ${res.status}: ${text}`);
    }
  } catch (err) {
    console.error(`[getTeamCreator] ${err.message}`);
    throw err;
  }
};

export const removeTeamMember = async (teamId, memberId, userData) => {
  const token = localStorage.getItem('token');
  const res = await fetch(`http://localhost:8080/teams/${teamId}/members/${memberId}`, {
    method: 'DELETE',
    headers: {
      'User-ID': userData?.userId || '',
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/x-www-form-urlencoded',
    }
  });

  if (!res.ok) {
    const msg = await res.text();
    throw new Error(`${res.status}: ${msg}`);
  }
};

export const getTeamDetail = async (teamId) => {
  const res = await fetch(`http://localhost:8080/teams/public/${teamId}`);
  if (!res.ok) throw new Error(`Failed to fetch team detail: ${res.status}`);
  return await res.json();
};
