import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Sidebar from "../../Organizer/Sidebar";

const mockedNavigate = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useNavigate: () => mockedNavigate,
  useLocation: () => ({ pathname: "/OrganizerDashboard/test@example.com" }),
}));

beforeEach(() => {
  localStorage.setItem("email", "test@example.com");
  localStorage.setItem("token", "fake-token");
  jest.clearAllMocks();
  window.alert = jest.fn();
});

afterEach(() => {
  localStorage.clear();
});

describe("Sidebar", () => {
  it("renders sidebar links correctly", () => {
    render(
      <MemoryRouter>
        <Sidebar />
      </MemoryRouter>
    );

    expect(screen.getByText(/Dashboard/i)).toBeInTheDocument();
    expect(screen.getByText(/Profile/i)).toBeInTheDocument();
    expect(screen.getByText(/Contest/i)).toBeInTheDocument();
  });

  it("navigates to dashboard when clicking Dashboard link", () => {
    render(
      <MemoryRouter>
        <Sidebar />
      </MemoryRouter>
    );

    const dashboardLink = screen.getByText(/Dashboard/i);
    fireEvent.click(dashboardLink);

    expect(dashboardLink).toHaveClass("active-link");
  });

  it("navigates to profile page when clicking Profile link", () => {
    render(
      <MemoryRouter>
        <Sidebar />
      </MemoryRouter>
    );

    const profileLink = screen.getByText(/Profile/i);
    fireEvent.click(profileLink);

    expect(mockedNavigate).toHaveBeenCalledWith("/OrganizerProfile/test@example.com");
  });

  it("shows alert if not logged in when clicking links", () => {
    localStorage.removeItem("token");

    render(
      <MemoryRouter>
        <Sidebar />
      </MemoryRouter>
    );

    const dashboardLink = screen.getByText(/Dashboard/i);
    fireEvent.click(dashboardLink);

    expect(window.alert).toHaveBeenCalledWith("You are not authorized. Please log in first.");
  });
});
