import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import TopBar from "../../Organizer/TopBar";

const mockNavigate = jest.fn();
const mockLocationReload = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockNavigate,
}));

beforeAll(() => {
  global.fetch = jest.fn((url) => {
    if (url.includes("/users/profile")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ avatarUrl: "/mock-avatar.jpg" }),
      });
    } else if (url.includes("/users/logout")) {
      return Promise.resolve({ ok: true });
    }
    return Promise.reject(new Error("Unknown endpoint"));
  });

  Object.defineProperty(window, "location", {
    writable: true,
    value: { href: "", reload: mockLocationReload },
  });
});

afterAll(() => {
  global.fetch.mockRestore();
});

beforeEach(() => {
  localStorage.setItem("email", "test@example.com");
  localStorage.setItem("token", "fake-token");
  jest.clearAllMocks();
});

afterEach(() => {
  localStorage.clear();
});

describe("TopBar", () => {
  it("renders email from localStorage", async () => {
    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );
    expect(await screen.findByText("test@example.com")).toBeInTheDocument();
  });

  it("loads and displays avatar from server", async () => {
    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );

    const avatar = await screen.findByAltText("User Avatar");
    expect(avatar.src).toContain("/mock-avatar.jpg");
  });

  it("navigates to profile page when clicking avatar", async () => {
    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );

    const avatar = await screen.findByAltText("User Avatar");
    fireEvent.click(avatar);

    expect(mockNavigate).toHaveBeenCalledWith("/OrganizerProfile/test@example.com");
  });

  it("shows logout confirmation dialog when clicking logout icon", async () => {
    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );

    const logoutIcon = document.querySelector(".logout-icon");
    fireEvent.click(logoutIcon);

    await waitFor(() => {
      expect(screen.getByText("Log out reminder")).toBeInTheDocument();
      expect(screen.getByText("Are you sure you want to log out?")).toBeInTheDocument();
    });
  });

  it("logs out and redirects when confirmed", async () => {
    render(
      <MemoryRouter>
        <TopBar />
      </MemoryRouter>
    );

    const logoutIcon = document.querySelector(".logout-icon");
    fireEvent.click(logoutIcon);

    const logoutButton = await screen.findByText("Log out");
    fireEvent.click(logoutButton);

    await waitFor(() => {
      expect(window.location.href).toBe("/");
    });

    expect(localStorage.getItem("token")).toBeNull();
  });
});
