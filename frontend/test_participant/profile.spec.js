import { test, expect } from '@playwright/test';


test.use({ browserName: 'chromium' });

test.describe('Profile Page', () => {
  test.beforeEach(async ({ page }) => {

    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('userId', 'mock-user-id');
      localStorage.setItem('role', 'participant');
    });

    await page.route('http://localhost:8080/users/profile', async (route, request) => {
      if (request.method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            name: 'Mock User',
            email: 'mockuser@example.com',
            description: 'This is a mock description.',
            avatarUrl: '/mock-avatar.png'
          }),
        });
      } else if (request.method() === 'PUT') {
        const postData = await request.postData();
        const updated = JSON.parse(postData);
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            message: 'Profile updated successfully!'
          }),
        });
      } else {
        await route.continue();
      }
    });


    await page.goto('http://localhost:3000/profile/mockuser@example.com', { waitUntil: 'networkidle' });


    await expect(page.locator('button.save-button')).toBeVisible();
  });


  test('should allow editing profile information and save', async ({ page }) => {
    await page.fill('input[name="name"]', 'New Name');
    await page.fill('input[name="email"]', 'newemail@example.com');
    await page.fill('input[name="password"]', 'NewPassword123');
    await page.fill('textarea[name="description"]', 'I love frontend development.');

    await page.click('button.save-button', { force: true });


    const alert = page.locator('div[role="alert"]');
    await expect(alert).toHaveText(/profile updated successfully/i, { timeout: 20000 });
  });

  test('should delete account successfully', async ({ page }) => {
    await page.click('button.delete-button', { force: true });
    const confirmDialog = page.getByRole('dialog', { name: /delete account/i });
    await expect(confirmDialog).toBeVisible();
  });
});
