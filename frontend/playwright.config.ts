import { defineConfig, FullConfig, FullResult, Reporter } from '@playwright/test';
import fs from 'fs';
import path from 'path';

export default defineConfig({
  testDir: './test_participant',
  timeout: 30 * 1000,
  expect: {
    timeout: 5000,
  },
  fullyParallel: true,
  retries: 1,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report' }],
  ],
  use: {
    actionTimeout: 0,
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    viewport: { width: 1280, height: 720 },
    ignoreHTTPSErrors: true,
    headless: true,
  },
  projects: [
    {
      name: 'Chromium',
      use: { browserName: 'chromium' },
    },
  ],
  webServer: {
    command: 'npm run start',
    port: 3000,
    reuseExistingServer: !process.env.CI,
  },
  // 👇 add test results summary generation
  async reporterCallback(config: FullConfig, result: FullResult) {
    const passed = result.status.passed;
    const failed = result.status.failed;
    const total = passed + failed;
    const passRate = total > 0 ? ((passed / total) * 100).toFixed(2) : '0.00';

    const reportFolder = path.resolve('playwright-report');
    const passRateFile = path.join(reportFolder, 'pass-rate.txt');

    const summary = `🎯 Test Summary\n✅ Passed: ${passed}\n❌ Failed: ${failed}\n📊 Pass Rate: ${passRate}%\n`;

    try {
      fs.mkdirSync(reportFolder, { recursive: true });
      fs.writeFileSync(passRateFile, summary, { encoding: 'utf-8' });
      console.log('\n📄 pass-rate.txt written to report folder.\n');
    } catch (err) {
      console.error('Failed to write pass-rate.txt:', err);
    }
  }
});
