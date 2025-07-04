import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import OrganizerContest from "../../Organizer/Contest";
import { BrowserRouter } from "react-router-dom";

const mockNavigate = jest.fn();

jest.mock("react-router-dom", () => {
  const actual = jest.requireActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useParams: () => ({ email: "test@example.com" }),
  };
});

beforeEach(() => {
  jest.spyOn(Storage.prototype, "getItem").mockImplementation((key) => {
    if (key === "email") return "test@example.com";
    if (key === "token") return "mock-token";
    return null;
  });

  global.fetch = jest.fn(() =>
    Promise.resolve({
      ok: true,
      json: () => Promise.resolve({ message: "Contest created" }),
    })
  );

  mockNavigate.mockClear();
});

const renderWithRouter = () => {
  render(
    <BrowserRouter>
      <OrganizerContest />
    </BrowserRouter>
  );
};

describe("OrganizerContest", () => {
  it("renders form fields", () => {
    renderWithRouter();
    expect(screen.getByPlaceholderText("Enter contest name")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Describe your contest...")).toBeInTheDocument();
    expect(screen.getByText("Create Contest")).toBeInTheDocument();
  });

  it("can add and remove scoring criteria", () => {
    renderWithRouter();
    const input = screen.getByPlaceholderText("Enter scoring criteria");
    fireEvent.change(input, { target: { value: "Creativity" } });
    fireEvent.click(screen.getByText("Add"));
    expect(screen.getByText("Creativity")).toBeInTheDocument();

    const deleteButton = screen.getByRole("button", { name: "delete" });
    fireEvent.click(deleteButton);
    expect(screen.queryByText("Creativity")).not.toBeInTheDocument();
  });

  it("submits form and navigates on success", async () => {
    renderWithRouter();

    fireEvent.change(screen.getByPlaceholderText("Enter contest name"), {
      target: { value: "My Contest" },
    });

    fireEvent.change(screen.getByPlaceholderText("Describe your contest..."), {
      target: { value: "A test contest" },
    });

    fireEvent.change(screen.getByTestId("start-date"), {
      target: { value: "2025-05-01" },
    });

    fireEvent.change(screen.getByTestId("end-date"), {
      target: { value: "2025-05-10" },
    });

    fireEvent.click(screen.getByText("Create Contest"));

    await waitFor(() => {
      expect(fetch).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith("/OrganizerContestList/test@example.com");
    });
  });
});
