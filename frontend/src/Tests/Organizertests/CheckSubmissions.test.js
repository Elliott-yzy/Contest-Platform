import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import OrganizerSubmissions from "../../Organizer/CheckSubmissions";
import { BrowserRouter } from "react-router-dom";

beforeAll(() => {
  Storage.prototype.getItem = jest.fn((key) => {
    if (key === "email") return "test@example.com";
    if (key === "token") return "fake-token";
    if (key === "userId") return "fake-user-id";
    if (key === "role") return "organizer";
    return null;
  });
});

beforeEach(() => {
  global.fetch = jest.fn((url) => {
    if (url.includes("/competitions/")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ name: "Test Competition" }),
      });
    }
    if (url.includes("/submissions/public")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          data: [
            {
              id: "submission-1",
              title: "Awesome Project",
              description: "This is a great project",
              createdAt: new Date().toISOString(),
              totalScore: 90,
              reviewStatus: "PENDING",
              fileUrl: "http://example.com/file.png",
              fileName: "file.png",
              fileType: "image/png",
            },
          ],
          pages: 1,
          total: 1,
        }),
      });
    }
    return Promise.resolve({ ok: false });
  });
});

afterEach(() => {
  jest.clearAllMocks();
});

describe("OrganizerSubmissions", () => {
  it("renders and shows loading spinner", async () => {
    render(
      <BrowserRouter>
        <OrganizerSubmissions />
      </BrowserRouter>
    );

    expect(screen.getByRole("progressbar")).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText(/Submissions for:/i)).toBeInTheDocument();
    });
  });

  it("renders fetched submission data", async () => {
    render(
      <BrowserRouter>
        <OrganizerSubmissions />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Awesome Project/i)).toBeInTheDocument();
      expect(screen.getByText(/This is a great project/i)).toBeInTheDocument();
    });
  });

  it("opens and closes the review dialog", async () => {
    render(
      <BrowserRouter>
        <OrganizerSubmissions />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Awesome Project/i)).toBeInTheDocument();
    });

    const viewButtons = screen.getAllByRole("button").filter(btn => btn.innerHTML.includes('svg'));
    fireEvent.click(viewButtons[0]);

    await waitFor(() => {
      expect(screen.getByText(/Submission Review/i)).toBeInTheDocument();
    });

    const cancelButton = screen.getByText(/Cancel/i);
    fireEvent.click(cancelButton);

    await waitFor(() => {
      expect(screen.queryByText(/Submission Review/i)).not.toBeInTheDocument();
    });
  });
});
