import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import AdminDashboard from "../../Admin/AdminDashboard";
import { BrowserRouter } from "react-router-dom";

beforeAll(() => {
  global.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
});

beforeAll(() => {
  Storage.prototype.getItem = jest.fn((key) => {
    if (key === "email") return "admin@example.com";
    if (key === "token") return "admin-token";
    if (key === "userId") return "admin-id";
    if (key === "role") return "admin";
    return null;
  });
});

beforeEach(() => {
  global.fetch = jest.fn(() => Promise.resolve({
    ok: true,
    json: () => Promise.resolve({
      totalCompetitions: 10,
      activeCompetitions: 3,
      finishedCompetitions: 5,
      totalParticipants: 100,
      individualParticipants: 60,
      teamParticipants: 40,
      totalSubmissions: 80,
      individualSubmissions: 50,
      teamSubmissions: 30,
      approvedSubmissions: 70,
      submissionTrend: { "2025-04-01": 10, "2025-04-02": 20 },
      participantTrend: {
        individual: { "2025-04-01": 5, "2025-04-02": 10 },
        team: { "2025-04-01": 2, "2025-04-02": 3 },
      },
    }),
  }));
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe("AdminDashboard", () => {
  it("renders and shows loading initially", async () => {
    render(
      <BrowserRouter>
        <AdminDashboard />
      </BrowserRouter>
    );

    expect(screen.getByText(/Platform Dashboard/i)).toBeInTheDocument();
    expect(screen.getByRole("progressbar")).toBeInTheDocument();

    await screen.findByText(/Competitions/i);
  });

  it("renders overview cards after fetch", async () => {
    render(
      <BrowserRouter>
        <AdminDashboard />
      </BrowserRouter>
    );

    await screen.findByText("Competitions");
    expect(screen.getByText("Participants")).toBeInTheDocument();
    expect(screen.getByText("Submissions")).toBeInTheDocument();
    expect(screen.getByText(/Approval Rate/i)).toBeInTheDocument();
  });

  it("shows (No data) if overview fetch fails", async () => {
    global.fetch = jest.fn(() => Promise.resolve({
      ok: false,
      json: () => Promise.resolve(null),
    }));

    render(
      <BrowserRouter>
        <AdminDashboard />
      </BrowserRouter>
    );

    await screen.findByText("(No data)");
  });
});
