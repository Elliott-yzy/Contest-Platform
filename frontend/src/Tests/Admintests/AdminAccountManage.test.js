import React from "react";
import { render, screen, fireEvent, within } from "@testing-library/react";
import AdminAccountManage from "../../Admin/AdminAccountManage";
import { BrowserRouter } from "react-router-dom";

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
  global.fetch = jest.fn((url, options) => {
    if (url.includes("/users/admin/list")) {
      return Promise.resolve({
        ok: true,
        json: () =>
          Promise.resolve({
            data: [
              {
                id: "user-1",
                name: "Test User",
                email: "testuser@example.com",
                description: "A test user",
                role: "PARTICIPANT",
              },
            ],
            pages: 1,
            total: 1,
          }),
      });
    }

    if (url.includes("/users/") && options?.method === "DELETE") {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ message: "User deleted successfully." }),
      });
    }

    return Promise.resolve({ ok: false });
  });
});

afterEach(() => {
  jest.clearAllMocks();
});

describe("AdminAccountManage", () => {
  it("renders and shows loading initially", async () => {
    render(
      <BrowserRouter>
        <AdminAccountManage />
      </BrowserRouter>
    );

    expect(screen.getByText(/Loading users.../i)).toBeInTheDocument();

    await screen.findByText(/All Users/i);
  });

  it("renders user data after fetch", async () => {
    render(
      <BrowserRouter>
        <AdminAccountManage />
      </BrowserRouter>
    );

    const rows = await screen.findAllByRole("row");
    const userRow = rows.find((row) => within(row).queryByText("Test User"));

    expect(userRow).toBeTruthy();
    expect(within(userRow).getByText("Test User")).toBeInTheDocument();
    expect(within(userRow).getByText("testuser@example.com")).toBeInTheDocument();
    expect(within(userRow).getByText("A test user")).toBeInTheDocument();
    expect(within(userRow).getByText("PARTICIPANT")).toBeInTheDocument();
  });

  it("deletes a user after confirmation", async () => {
    window.confirm = jest.fn(() => true);

    render(
      <BrowserRouter>
        <AdminAccountManage />
      </BrowserRouter>
    );

    const deleteButton = await screen.findByRole("button", { name: /Delete/i });
    fireEvent.click(deleteButton);

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/users/user-1"),
      expect.objectContaining({
        method: "DELETE",
      })
    );
  });
});
