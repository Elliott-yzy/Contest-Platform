import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import UploadMedia from "../../Organizer/UploadMedia";

jest.mock("../../Organizer/TopBar", () => () => <div>TopBar</div>);
jest.mock("../../Organizer/Sidebar", () => () => <div>Sidebar</div>);

beforeAll(() => {
  global.fetch = jest.fn((url, options) => {
    if (options?.method === "GET" && url.includes("/competitions/") && url.includes("/media")) {
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({
          introVideoUrl: "test-intro.mp4",
          imageUrls: ["test-image-1.jpg", "test-image-2.jpg"],
        }),
      });
    } else if (options?.method === "POST" && url.includes("/upload")) {
      return Promise.resolve({ ok: true });
    } else if (options?.method === "DELETE" && url.includes("/delete")) {
      return Promise.resolve({ ok: true });
    }
    return Promise.reject(new Error("Unknown API"));
  });

  global.URL.createObjectURL = jest.fn(() => "blob:mock-url");
});

afterAll(() => {
  jest.resetAllMocks();
});

beforeEach(() => {
  jest.spyOn(window, "alert").mockImplementation(() => {});
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe("UploadMedia Component", () => {
  test("previews selected files", async () => {
    render(
      <MemoryRouter>
        <UploadMedia />
      </MemoryRouter>
    );
  
    const fileInput = screen.getByTestId('file-input');
    const file = new File(["dummy content"], "test.jpg", { type: "image/jpeg" });
  
    fireEvent.change(fileInput, { target: { files: [file] } });
  
    await waitFor(() => {
      expect(screen.getByAltText("preview-0")).toBeInTheDocument();
    });
  });  

  test("cancels upload and navigates back", async () => {
    render(
      <MemoryRouter>
        <UploadMedia />
      </MemoryRouter>
    );

    const cancelButton = await screen.findByRole("button", { name: /cancel/i });
    fireEvent.click(cancelButton);

    expect(cancelButton).toBeInTheDocument();
  });
});
