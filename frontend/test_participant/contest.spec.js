import { test, expect } from '@playwright/test';

// 只在 Chromium 浏览器运行
test.use({ browserName: 'chromium' });

test.describe('Contest Page', () => {
  test.beforeEach(async ({ page }) => {
    // 1. 设置 localStorage，模拟登录状态
    await page.addInitScript(() => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('userId', 'mock-user-id');
      localStorage.setItem('role', 'participant');
    });

    // 2. Mock competitions/list 接口
    await page.route('**/competitions/list**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          data: [
            {
              id: 'contest-1',
              name: 'Mock Contest 1',
              description: 'Test contest description',
              category: 'Technology',
              startDate: '2025-01-01',
              endDate: '2025-12-31',
              isPublic: true,
              status: 'ONGOING',
              allowedSubmissionTypes: ['INDIVIDUAL'],
              scoringCriteria: 'Creativity',
              introVideoUrl: '',
              imageUrls: [],
              createdAt: '2025-01-01',
              participationType: 'INDIVIDUAL'
            }
          ]
        }),
      });
    });

    // 3. 打开Contest页面 —— 注意这里补上了mock email
    await page.goto('http://localhost:3000/contest/mockuser@example.com', { waitUntil: 'networkidle' });
  });

  // 测试比赛卡片是否正常显示
  test('should display contests successfully', async ({ page }) => {
    await expect(page.getByText('Mock Contest 1')).toBeVisible({ timeout: 10000 });
  });

  // 测试打开筛选面板
  test('should filter contests', async ({ page }) => {
    await page.click('button:has(svg[data-testid="FilterAltIcon"])');
    await expect(page.locator('.participant-filter-sidebar')).toHaveClass(/visible/);
  });

  // 测试切换到列表视图
  test('should toggle list view', async ({ page }) => {
    await page.click('button:has(svg[data-testid="ViewListIcon"])');
    await expect(page.locator('table')).toBeVisible({ timeout: 10000 });
  });

  // 测试搜索框输入并点击搜索
  test('should search contests', async ({ page }) => {
    await page.fill('input[type="text"]', 'Mock Contest 1');
    await page.click('button:has(svg[data-testid="SearchIcon"])');
    await expect(page.locator('input[type="text"]')).toHaveValue('Mock Contest 1');
  });


});
