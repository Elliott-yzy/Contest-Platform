import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import OrganizerProfile from "../../Organizer/Profile";
import { MemoryRouter } from "react-router-dom";

beforeEach(() => {
  localStorage.setItem("token", "fake-token");
  localStorage.setItem("userId", "fake-user-id");
  localStorage.setItem("role", "organizer");

  global.fetch = jest.fn((url, options) => {
    if (url.endsWith("/users/profile") && (!options || options.method === "GET")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          name: "Test User",
          email: "test@example.com",
          description: "A test user",
          avatarUrl: "/test-avatar.png",
        }),
      });
    }
    if (url.endsWith("/users/profile") && options?.method === "PUT") {
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    }
    if (url.endsWith("/users/profile/avatar") && options?.method === "POST") {
      return Promise.resolve({ ok: true, json: () => Promise.resolve({ avatarUrl: "/new-avatar.png" }) });
    }
    if (url.includes("/users/") && options?.method === "DELETE") {
      return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
    }
    return Promise.resolve({ ok: true, json: () => Promise.resolve({}) });
  });

  window.alert = jest.fn();
  window.URL.createObjectURL = jest.fn(() => "blob:fake-url");
  window.URL.revokeObjectURL = jest.fn();
});

afterEach(() => {
  localStorage.clear();
  jest.clearAllMocks();
});

const renderProfile = () => {
  render(
    <MemoryRouter>
      <OrganizerProfile />
    </MemoryRouter>
  );
};

describe("OrganizerProfile", () => {
  it("updates profile successfully", async () => {
    renderProfile();

    fireEvent.change(await screen.findByPlaceholderText("Enter your name"), {
      target: { value: "New Name" },
    });
    fireEvent.change(await screen.findByPlaceholderText("Enter your email"), {
      target: { value: "newemail@example.com" },
    });

    fireEvent.click(screen.getByText("Save"));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith("Profile updated successfully");
    });
  });

  it("opens avatar upload dialog", async () => {
    renderProfile();

    const avatars = await screen.findAllByAltText("User Avatar");
    fireEvent.click(avatars[1]);
    await waitFor(() => {
      expect(screen.getByText("Upload Avatar")).toBeInTheDocument();
    });
  });

  it("deletes account when confirmed", async () => {
    renderProfile();

    fireEvent.click(screen.getByText("Delete Account"));
    fireEvent.click(screen.getByText("Delete"));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith("Your account has been deleted.");
    });
  });
});
