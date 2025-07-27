# Test info

- Name: Project Page >> display Team view toggle button
- Location: C:\Users\beiqi\Desktop\participant\my-app\test_participant\project.spec.js:56:7

# Error details

```
Error: Timed out 5000ms waiting for expect(locator).toBeVisible()

Locator: getByRole('button', { name: 'Team' })
Expected: visible
Received: <element(s) not found>
Call log:
  - expect.toBeVisible with timeout 5000ms
  - waiting for getByRole('button', { name: 'Team' })

    at C:\Users\beiqi\Desktop\participant\my-app\test_participant\project.spec.js:57:62
```

# Page snapshot

```yaml
- banner:
  - img "Logo"
  - heading "Questora" [level=1]
  - heading "👋 Hi, Participant" [level=1]
  - img "User Avatar"
  - img
- complementary:
  - navigation:
    - list:
      - listitem:
        - link "profile Profile":
          - /url: /profile/null
          - img "profile": 👤
          - text: Profile
      - listitem:
        - link "contest Contest":
          - /url: /contest/null
          - img "contest": 🏆
          - text: Contest
      - listitem:
        - link "team Team":
          - /url: /teams/null
          - img "team": 👥
          - text: Team
      - listitem:
        - link "▶ project Project":
          - /url: /project/null
          - text: ▶
          - img "project": 📁
          - text: Project
      - listitem:
        - link "rating Rating":
          - /url: /rating/null
          - img "rating": ⭐
          - text: Rating
- heading "Joined Competitions" [level=2]
- button "Individual"
- button "Team"
- table:
  - rowgroup:
    - row "Contest Name Category Competition Status Submission Name Submission Status":
      - columnheader "Contest Name"
      - columnheader "Category"
      - columnheader "Competition Status"
      - columnheader "Submission Name"
      - columnheader "Submission Status"
  - rowgroup:
    - row "Mock Competition 1 Technology ONGOING Submit Not Submitted":
      - cell "Mock Competition 1"
      - cell "Technology"
      - cell "ONGOING"
      - cell "Submit":
        - button "Submit"
      - cell "Not Submitted"
- navigation "pagination navigation":
  - list:
    - listitem:
      - button "Go to previous page" [disabled]
    - listitem:
      - button "page 1": "1"
    - listitem:
      - button "Go to next page" [disabled]
```

# Test source

```ts
   1 | import { test, expect } from '@playwright/test';
   2 |
   3 | test.use({ browserName: 'chromium' });
   4 |
   5 | test.describe('Project Page', () => {
   6 |   test.beforeEach(async ({ page }) => {
   7 |     await page.addInitScript(() => {
   8 |       localStorage.setItem('token', 'mock-token');
   9 |       localStorage.setItem('userId', 'mock-user-id');
  10 |       localStorage.setItem('role', 'participant');
  11 |     });
  12 |
  13 |     await page.route('**/users/profile', async (route) => {
  14 |       await route.fulfill({
  15 |         status: 200,
  16 |         contentType: 'application/json',
  17 |         body: JSON.stringify({
  18 |           userId: 'mock-user-id',
  19 |           role: 'participant',
  20 |           name: 'Mock User',
  21 |           email: 'mock@example.com'
  22 |         }),
  23 |       });
  24 |     });
  25 |
  26 |     // Mock personal registration data
  27 |     await page.route('**/registrations/my**', async (route) => {
  28 |       await route.fulfill({
  29 |         status: 200,
  30 |         contentType: 'application/json',
  31 |         body: JSON.stringify({
  32 |           data: [
  33 |             {
  34 |               competitionId: 'comp-1',
  35 |               competitionName: 'Mock Competition 1',
  36 |               category: 'Technology',
  37 |               status: 'ONGOING',
  38 |               hasSubmitted: false
  39 |             }
  40 |           ],
  41 |           total: 1,
  42 |           page: 1,
  43 |           size: 10,
  44 |           pages: 1
  45 |         }),
  46 |       });
  47 |     });
  48 |
  49 |     await page.goto('http://localhost:3000/project/mock@example.com', { waitUntil: 'networkidle' });
  50 |   });
  51 |
  52 |   test('display Joined Competitions header', async ({ page }) => {
  53 |     await expect(page.getByText('Joined Competitions')).toBeVisible({ timeout: 10000 });
  54 |   });
  55 |
  56 |   test('display Team view toggle button', async ({ page }) => {
> 57 |     await expect(page.getByRole('button', { name: 'Team' })).toBeVisible();
     |                                                              ^ Error: Timed out 5000ms waiting for expect(locator).toBeVisible()
  58 |   });
  59 |
  60 |   test('display personal competition table', async ({ page }) => {
  61 |     await expect(page.locator('table')).toBeVisible();
  62 |   });
  63 |
  64 |
  65 |   test('pagination control', async ({ page }) => {
  66 |     await expect(page.locator('.MuiPagination-root')).toBeVisible();
  67 |   });
  68 |
  69 |
  70 |   test('Submit for personal registration', async ({ page }) => {
  71 |     await expect(page.getByRole('button', { name: 'Submit' })).toBeVisible();
  72 |   });
  73 | });
  74 |
```