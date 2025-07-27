import { test, expect } from '@playwright/test';

test.use({ browserName: 'chromium' });

test.describe('Team Management Page', () => {
  test.beforeEach(async ({ page }) => {
   
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('userId', 'mock-user-id');
      localStorage.setItem('role', 'participant');
    });

  
    await page.route('**/users/profile', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          userId: 'mock-user-id',
          role: 'participant',
          name: 'Mock User',
          email: 'mockuser@example.com'
        }),
      });
    });

  
    await page.goto('http://localhost:3000/teams/mockuser@example.com', { waitUntil: 'networkidle' });
  });

  test('should display Team Management', async ({ page }) => {
    await expect(page.getByText('Team Management')).toBeVisible({ timeout: 10000 });
  });


  test('Create or Join team ', async ({ page }) => {
    await expect(page.locator('button:has-text("Create Team")')).toBeVisible();;
  });


  test('should have My Teams', async ({ page }) => {
    
    await expect(page.locator('button', { hasText: /My Teams/i })).toBeVisible();
  });


  test('Snackbar component loaded', async ({ page }) => {
    await expect(page.locator('div[role="alert"]')).toBeHidden(); 
  });
});
