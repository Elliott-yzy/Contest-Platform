import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import SubmissionRatings from "../../Organizer/SubmissionRatings";

const mockedNavigate = jest.fn();

jest.mock("react-router-dom", () => ({
  ...jest.requireActual("react-router-dom"),
  useParams: () => ({ competitionId: "test-competition-id" }),
  useNavigate: () => mockedNavigate,
}));

beforeAll(() => {
  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ data: [] }),
    })
  );
});

afterAll(() => {
  global.fetch.mockRestore();
});

beforeEach(() => {
  localStorage.setItem("token", "fake-token");
  localStorage.setItem("userId", "user-123");
  localStorage.setItem("role", "organizer");
  jest.clearAllMocks();
  window.alert = jest.fn();
});

afterEach(() => {
  localStorage.clear();
});

describe("SubmissionRatings", () => {
  it("renders rated submissions table after data loads", async () => {
    render(
      <MemoryRouter>
        <SubmissionRatings />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText("Rated Submissions Comparison")).toBeInTheDocument();
    });
  });

  it("triggers auto award when clicking the button", async () => {
    render(
      <MemoryRouter>
        <SubmissionRatings />
      </MemoryRouter>
    );

    const autoAwardButton = await screen.findByText(/Auto Award Winners/i);
    fireEvent.click(autoAwardButton);

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith(expect.stringContaining("Auto-award completed"));
    });
  });

  it("navigates back to submissions list when clicking back button", async () => {
    render(
      <MemoryRouter>
        <SubmissionRatings />
      </MemoryRouter>
    );

    const backButton = await screen.findByText(/Back to Submissions List/i);
    fireEvent.click(backButton);

    expect(mockedNavigate).toHaveBeenCalledWith("/OrganizerSubmissions/test-competition-id");
  });
});
