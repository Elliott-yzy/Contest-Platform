import { test, expect } from '@playwright/test';

test.use({ browserName: 'chromium' });

test.describe('Rating Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('userId', 'mock-user-id');
      localStorage.setItem('role', 'participant');
    });

    await page.route('**/judges/my-competitions**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: [
            {
              id: 'competition-1',
              name: 'Mock Competition 1',
              description: 'Test Competition for Judges',
              status: 'COMPLETED',
            },
          ],
        }),
      });
    });

    await page.route('**/competitions/competition-1', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 'competition-1',
          name: 'Mock Competition 1',
          description: 'Detailed description for Competition 1.',
          category: 'Tech',
          participationType: 'INDIVIDUAL',
          startDate: new Date().toISOString(),
          endDate: new Date().toISOString(),
          isPublic: true,
          scoringCriteria: ['Creativity', 'Technical Skill'],
          allowedSubmissionTypes: ['PDF', 'Image'],
          imageUrls: [],
          introVideoUrl: '',
          status: 'COMPLETED',
        }),
      });
    });

    await page.goto('http://localhost:3000/rating/mockuser@example.com', { waitUntil: 'networkidle' });
  });

  test('should display Competitions Assigned to You title', async ({ page }) => {
    await expect(page.getByText('Competitions Assigned to You as Judge')).toBeVisible();
  });

  test('should display competition table', async ({ page }) => {
    await expect(page.locator('table')).toBeVisible();
  });

  test('should open competition detail dialog when clicking competition name', async ({ page }) => {
    
    await page.getByRole('button', { name: 'Mock Competition 1' }).click();

   
    const dialog = page.locator('.MuiDialog-container');
    await expect(dialog).toBeVisible();

   
    await expect(dialog.getByRole('heading', { name: /Mock Competition 1/i })).toBeVisible();


    await expect(dialog.getByText('Detailed description for Competition 1.')).toBeVisible();
  });
});
