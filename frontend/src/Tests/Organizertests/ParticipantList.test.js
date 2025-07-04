import React from "react";
import { render, screen, fireEvent, waitFor, within } from "@testing-library/react";
import ParticipantList from "../../Organizer/ParticipantList";
import { MemoryRouter, Route, Routes } from "react-router-dom";

const mockNavigate = jest.fn();

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => ({ competitionId: "test-competition" }),
    useLocation: () => ({ pathname: "/OrganizerDashboard/test@example.com", state: { participationType: "INDIVIDUAL" } }),
    MemoryRouter: actual.MemoryRouter,
    Routes: actual.Routes,
    Route: actual.Route,
  };
});

beforeEach(() => {
  jest.spyOn(Storage.prototype, "getItem").mockImplementation((key) => {
    if (key === "email") return "test@example.com";
    if (key === "token") return "mock-token";
    if (key === "userId") return "mock-user-id";
    if (key === "role") return "organizer";
    return null;
  });

  global.fetch = jest.fn((url) => {
    if (url.includes("/competitions/test-competition")) {
      return Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            name: "Test Competition",
            category: "Art",
            startDate: "2025-05-01",
            endDate: "2025-05-10",
            status: "Ongoing",
            selectedParticipationType: "INDIVIDUAL",
          }),
      });
    }
    if (url.includes("/registrations/test-competition/participants")) {
      return Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            data: [
              {
                userId: "user123",
                name: "Alice",
                email: "alice@example.com",
                description: "Participant",
                registeredAt: new Date().toISOString(),
              },
            ],
            pages: 1,
            total: 1,
          }),
      });
    }
    return Promise.resolve({
      ok: true,
      json: () => Promise.resolve({}),
    });
  });

  window.URL.createObjectURL = jest.fn();
  jest.mock("file-saver", () => ({ saveAs: jest.fn() }));

  mockNavigate.mockClear();
});

const renderWithRouter = () => {
  render(
    <MemoryRouter initialEntries={["/participant-list/test-competition"]}>
      <Routes>
        <Route path="/participant-list/:competitionId" element={<ParticipantList />} />
      </Routes>
    </MemoryRouter>
  );
};

describe("ParticipantList", () => {
  it("renders competition info and participant list", async () => {
    renderWithRouter();

    expect(await screen.findByText(/Participants for: Test Competition/i)).toBeInTheDocument();

    const table = screen.getByRole("table");
    const nameCell = within(table).getByText("Alice");
    expect(nameCell).toBeInTheDocument();

    const emailCell = within(table).getByText("alice@example.com");
    expect(emailCell).toBeInTheDocument();
  });

  it("allows searching participants", async () => {
    renderWithRouter();
    const searchInput = await screen.findByLabelText(/Search by name/i);
    fireEvent.change(searchInput, { target: { value: "Alice" } });

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining("/participants?page=1&size=10&keyword=Alice"),
        expect.anything()
      );
    });
  });

  it("clicks export to excel button", async () => {
    renderWithRouter();
    const exportButton = await screen.findByRole("button", { name: /Export to Excel/i });
    fireEvent.click(exportButton);

    expect(exportButton).toBeInTheDocument();
  });

  it("clicks back button to navigate", async () => {
    renderWithRouter();
    const backButton = await screen.findByRole("button", { name: /Back to Contest List/i });
    fireEvent.click(backButton);
    expect(mockNavigate).toHaveBeenCalledWith("/OrganizerContestList/test@example.com");
  });
});
