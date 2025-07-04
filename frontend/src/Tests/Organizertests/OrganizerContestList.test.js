import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import OrganizerContestList from "../../Organizer/ContestList";
import { BrowserRouter } from "react-router-dom";

const mockNavigate = jest.fn();
jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

global.fetch = jest.fn(() =>
  Promise.resolve({
    ok: true,
    json: () =>
      Promise.resolve({
        data: [
          {
            id: "1",
            name: "Mock Contest",
            category: "Art",
            status: "UPCOMING",
            startDate: "2025-05-01T00:00:00Z",
            endDate: "2025-05-10T00:00:00Z",
            participationType: "INDIVIDUAL",
          },
        ],
      }),
  })
);

const renderWithRouter = () => {
  render(
    <BrowserRouter>
      <OrganizerContestList />
    </BrowserRouter>
  );
};

describe("OrganizerContestList", () => {
  beforeEach(() => {
    fetch.mockClear();
    mockNavigate.mockClear();
  });

  it("renders contest list after fetch", async () => {
    renderWithRouter();
    await waitFor(() => {
      expect(screen.getByText("Mock Contest")).toBeInTheDocument();
    });
  });

  it("filters contests based on search input", async () => {
    renderWithRouter();
    await waitFor(() => screen.getByText("Mock Contest"));

    const searchInput = screen.getByPlaceholderText("Search by name...");
    fireEvent.change(searchInput, { target: { value: "Nonexistent" } });

    await waitFor(() => {
      expect(screen.queryByText("Mock Contest")).not.toBeInTheDocument();
    });
  });

  it("navigates to create new contest page", async () => {
    renderWithRouter();
    const createButton = await screen.findByText("Create New Competition");
    fireEvent.click(createButton);

    expect(mockNavigate).toHaveBeenCalledWith("/OrganizerContest/null");
  });
});
