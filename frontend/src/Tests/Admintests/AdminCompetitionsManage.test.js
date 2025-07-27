import React from "react";
import { render, screen, fireEvent, within } from "@testing-library/react";
import AdminCompetitionsManage from "../../Admin/AdminCompetitionsManage";
import { BrowserRouter } from "react-router-dom";

beforeAll(() => {
  window.alert = jest.fn();
  Storage.prototype.getItem = jest.fn((key) => {
    if (key === "email") return "admin@example.com";
    if (key === "token") return "admin-token";
    if (key === "userId") return "admin-id";
    if (key === "role") return "admin";
    return null;
  });
});

beforeEach(() => {
  global.fetch = jest.fn((url, options) => {
    if (url.includes("/competitions/list")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          data: [
            {
              id: "comp-1",
              name: "Awesome Competition",
              category: "Design & Creativity",
              status: "ONGOING",
              startDate: new Date().toISOString(),
              endDate: new Date().toISOString(),
            },
          ],
          pages: 1,
        }),
      });
    }
    if (url.includes("/competitions/delete/")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ message: "Competition deleted" }),
      });
    }
    if (url.match(/\/competitions\/[^/]+$/)) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          id: "comp-1",
          name: "Awesome Competition",
          description: "A great competition",
          status: "ONGOING",
          category: "Design & Creativity",
          participationType: "INDIVIDUAL",
          startDate: new Date().toISOString(),
          endDate: new Date().toISOString(),
          isPublic: true,
          scoringCriteria: ["Creativity", "Impact"],
          allowedSubmissionTypes: ["PDF", "Video"],
          imageUrls: [],
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
  jest.restoreAllMocks();
});

describe("AdminCompetitionsManage", () => {
  it("renders and shows loading initially", async () => {
    render(
      <BrowserRouter>
        <AdminCompetitionsManage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Loading competitions.../i)).toBeInTheDocument();
    await screen.findByText(/All Competitions/i);
  });

  it("renders competition data after fetch", async () => {
    render(
      <BrowserRouter>
        <AdminCompetitionsManage />
      </BrowserRouter>
    );

    await screen.findByText("Awesome Competition");
    expect(screen.getByText("Design & Creativity")).toBeInTheDocument();
    expect(screen.getByText("ONGOING")).toBeInTheDocument();
  });

  it("filters competitions by search input", async () => {
    render(
      <BrowserRouter>
        <AdminCompetitionsManage />
      </BrowserRouter>
    );

    await screen.findByText("Awesome Competition");

    const searchInput = screen.getByLabelText(/Search/i);
    fireEvent.change(searchInput, { target: { value: "new competition" } });

    await screen.findByLabelText(/Search/i);
    expect(global.fetch).toHaveBeenCalled();
  });

  it("opens competition detail dialog on click", async () => {
    render(
      <BrowserRouter>
        <AdminCompetitionsManage />
      </BrowserRouter>
    );

    await screen.findByText("Awesome Competition");

    const compButton = screen.getByRole("button", { name: /Awesome Competition/i });
    fireEvent.click(compButton);

    await screen.findByText(/A great competition/i);
    expect(screen.getByText(/Category:/i)).toBeInTheDocument();
  });

  it("deletes a competition after confirmation", async () => {
    window.confirm = jest.fn(() => true);

    render(
      <BrowserRouter>
        <AdminCompetitionsManage />
      </BrowserRouter>
    );

    await screen.findByText("Awesome Competition");

    const deleteButton = screen.getByRole("button", { name: /Delete/i });
    fireEvent.click(deleteButton);

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/competitions/delete/comp-1"),
      expect.objectContaining({
        method: "DELETE",
      })
    );
  });
});
