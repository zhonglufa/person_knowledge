import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './e2e-tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [
    ['html', { outputFolder: 'e2e-test-results/html' }],
    ['json', { outputFile: 'e2e-test-results/results.json' }],
    ['list']
  ],
  globalSetup: './e2e-tests/global-setup.js',
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5000',
    storageState: process.env.PLAYWRIGHT_STORAGE_STATE || 'e2e-test-results/storageState.json',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  // 暂时禁用自动启动，由用户手动启动前端服务
  // webServer: {
  //   command: 'npm run dev',
  //   url: 'http://localhost:5000',
  //   reuseExistingServer: !process.env.CI,
  //   timeout: 120000,
  // },
});
