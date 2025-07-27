/**
 * TeamList.js
 * 
 * A list and filter component to browse, join, or leave public teams.
 * Used in the Participant Team Management module.
 * 
 * Role: Participant
 * Developer: Beiqi Dai
 */


// ParticipantTeam/TeamList.js
import React from 'react';
import { TextField, Box, FormControl, InputLabel, Select, MenuItem, List, ListItem, ListItemText, Typography, Button, Pagination } from '@mui/material';

function TeamList({
  teams, joinedTeams, page, pages,
  keyword, sortBy, order,
  setPage, setKeyword, setSortBy, setOrder,
  onJoin, onLeave
}) {
  return (
    <>
      <Box mb={2}>
        <TextField
          label="Search Teams"
          value={keyword}
          onChange={e => { setKeyword(e.target.value); setPage(1); }}
          size="small"
          fullWidth
        />
      </Box>
      <Box display="flex" gap={1} mb={2}>
        <FormControl size="small">
          <InputLabel>Sort By</InputLabel>
          <Select value={sortBy} onChange={e => { setSortBy(e.target.value); setPage(1); }} label="Sort By">
            <MenuItem value="name">Name</MenuItem>
            <MenuItem value="createdAt">Created At</MenuItem>
          </Select>
        </FormControl>
        <FormControl size="small">
          <InputLabel>Order</InputLabel>
          <Select value={order} onChange={e => { setOrder(e.target.value); setPage(1); }} label="Order">
            <MenuItem value="asc">Asc</MenuItem>
            <MenuItem value="desc">Desc</MenuItem>
          </Select>
        </FormControl>
      </Box>
      <List dense>
        {teams.map(t => {
          const isMember = joinedTeams.has(t.id);
          return (
            <ListItem key={t.id} divider secondaryAction={
              isMember
                ? <Button size="small" color="warning" onClick={() => onLeave(t.id)}>Leave</Button>
                : <Button size="small" onClick={() => onJoin(t)}>Join</Button>
            }>
              <ListItemText primary={t.name} secondary={t.description || 'No description'} />
            </ListItem>
          );
        })}
        {teams.length === 0 && <Typography variant="body2" color="text.secondary">No teams found.</Typography>}
      </List>
      <Box display="flex" justifyContent="center" mt={2}>
        <Pagination count={pages} page={page} onChange={(e, v) => setPage(v)} size="small" />
      </Box>
    </>
  );
}

export default TeamList;

