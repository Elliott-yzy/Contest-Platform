import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import EditContest from "../../Organizer/EditContest";

if (!global.fetch) {
  global.fetch = jest.fn();
}

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");
  return {
    ...actual,
    useParams: () => ({ email: "test@example.com" }),
    useSearchParams: () => [
      new URLSearchParams({ competitionId: "test-id" }),
    ],
    useNavigate: () => jest.fn(),
  };
});

beforeEach(() => {
  global.fetch.mockImplementation((url) => {
    if (url.includes("/competitions/test-id")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          id: "test-id",
          name: "Test Contest",
          description: "This is a test description",
          category: "Design & Creativity",
          startDate: "2025-05-01T00:00:00",
          endDate: "2025-05-10T00:00:00",
          isPublic: true,
          scoringCriteria: ["Creativity", "Impact"],
          allowedSubmissionTypes: ["PDF", "Image"],
          participationType: "INDIVIDUAL",
        }),
      });
    }
    return Promise.resolve({
      ok: true,
      json: () => Promise.resolve({}),
    });
  });
});

afterEach(() => {
  jest.clearAllMocks();
});

describe("EditContest", () => {
  test("renders contest details from API", async () => {
    render(
      <MemoryRouter initialEntries={["/edit-contest?competitionId=test-id"]}>
        <EditContest />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByDisplayValue("Test Contest")).toBeInTheDocument();
      expect(screen.getByDisplayValue("This is a test description")).toBeInTheDocument();
    });
  });

  test("allows adding new scoring criteria", async () => {
    render(
      <MemoryRouter initialEntries={["/edit-contest?competitionId=test-id"]}>
        <EditContest />
      </MemoryRouter>
    );

    await waitFor(() => screen.getByText("Update Contest"));

    const input = screen.getByPlaceholderText("Enter scoring criteria");
    fireEvent.change(input, { target: { value: "Technical Skill" } });

    const addButton = screen.getByRole("button", { name: /add/i });
    fireEvent.click(addButton);

    expect(screen.getByText("Technical Skill")).toBeInTheDocument();
  });

  test("submits updated contest data", async () => {
    const mockNavigate = jest.fn();
    jest.spyOn(require("react-router-dom"), "useNavigate").mockReturnValue(mockNavigate);

    render(
      <MemoryRouter initialEntries={["/edit-contest?competitionId=test-id"]}>
        <EditContest />
      </MemoryRouter>
    );

    await waitFor(() => screen.getByText("Update Contest"));

    const startInput = screen.getByLabelText(/Start Date/i);
    const endInput = screen.getByLabelText(/End Date/i);
    fireEvent.change(startInput, { target: { value: "2025-05-01" } });
    fireEvent.change(endInput, { target: { value: "2025-05-10" } });

    const updateButton = screen.getByRole("button", { name: /update contest/i });
    fireEvent.click(updateButton);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining("/update/"),
        expect.anything()
      );
      expect(mockNavigate).toHaveBeenCalled();
    });
  });

  test("navigates back on cancel", async () => {
    const mockNavigate = jest.fn();
    jest.spyOn(require("react-router-dom"), "useNavigate").mockReturnValue(mockNavigate);

    render(
      <MemoryRouter initialEntries={["/edit-contest?competitionId=test-id"]}>
        <EditContest />
      </MemoryRouter>
    );

    await waitFor(() => screen.getByText("Cancel"));

    const cancelButton = screen.getByRole("button", { name: /cancel/i });
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalled();
    });
  });
});
