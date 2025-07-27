/**
 * @file RoleSelectModal.js
 * @description 
 * This component renders a modal for users to select their role before logging in or registering.
 * It allows users to:
 *  - Choose between "Participant" and "Organizer" roles.
 *  - Trigger the next step in the authentication flow based on the selected role.
 * 
 * Clicking outside the modal (on the backdrop) will close the modal.
 * Layout and styling are handled via RoleSelectModal.css.
 * 
 * Developer: Zhaoyi Yang, Ziqi Yi
 */


import React from "react";
import "./RoleSelectModal.css";

function RoleSelectModal({ onSelectRole, onClose }) {
  const handleBackdropClick = (e) => {
    if (e.target.classList.contains("modal-overlay")) {
      onClose();
    }
  };

  return (
    <div className="modal-overlay" onClick={handleBackdropClick}>
      <div className="role-modal">
        <h3>Select Your Role</h3>
        <div className="role-buttons">
          <button onClick={() => onSelectRole("Participant")}>Participant</button>
          <button onClick={() => onSelectRole("Organizer")}>Organizer</button>
        </div>
      </div>
    </div>
  );
}

export default RoleSelectModal;
