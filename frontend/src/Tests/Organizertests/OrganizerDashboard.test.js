import React from "react";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import OrganizerDashboard from "../../Organizer/Dashboard";

global.ResizeObserver = class {
  observe() { }
  unobserve() { }
  disconnect() { }
};

beforeEach(() => {
  global.fetch = jest.fn((url) => {
    if (url.includes("/user/avatar")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ avatarUrl: "https://example.com/avatar.jpg" }),
      });
    }
    if (url.includes("/competitions/achieve/my")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ data: [{ id: "comp1", name: "Test Competition" }] }),
      });
    }
    if (url.includes("/dashboard/public/statistics")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          submissionCount: 10,
          approvedSubmissionCount: 7,
          individualParticipantCount: 5,
          teamParticipantCount: 0,
          judgeCount: 3,
          participationType: "INDIVIDUAL",
          individualParticipantTrend: {
            "2025-01-01": 1,
            "2025-01-02": 2,
          },
        }),
      });
    }
    if (url.includes("/competitions/") || url.includes("/api/competitions/")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ status: "open" }),
      });
    }
    if (url.includes("/organizer/upload-media/")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ mediaList: [] }),
      });
    }

    console.error("Unknown API called:", url);
    return Promise.resolve({
      ok: false,
      json: () => Promise.reject(new Error("Unknown API")),
    });
  });
});

afterEach(() => {
  jest.resetAllMocks();
});

describe("OrganizerDashboard", () => {
  it("renders loading spinner initially", () => {
    render(
      <BrowserRouter>
        <OrganizerDashboard />
      </BrowserRouter>
    );
    expect(screen.getByRole("progressbar")).toBeInTheDocument();
  });

  it("renders dashboard metrics after loading", async () => {
    render(
      <BrowserRouter>
        <OrganizerDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Participants/i)).toBeInTheDocument();
      expect(screen.getByText(/Submissions/i)).toBeInTheDocument();
      expect(screen.getByText(/Judges/i)).toBeInTheDocument();
      expect(screen.getByText(/Approval/i)).toBeInTheDocument();
    });
  });

  it("shows status distribution and trend viewer", async () => {
    render(
      <BrowserRouter>
        <OrganizerDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Status Distribution/i)).toBeInTheDocument();
      expect(screen.getByText(/Trend Viewer/i)).toBeInTheDocument();
    });
  });

  it("can select a competition for trend viewing", async () => {
    render(
      <BrowserRouter>
        <OrganizerDashboard />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByLabelText(/Select competition/i)).toBeInTheDocument();
    });

    fireEvent.mouseDown(screen.getByLabelText(/Select competition/i));

    await waitFor(() => {
      expect(screen.getByText(/Test Competition/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText(/Test Competition/i));
  });
});
