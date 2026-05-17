import { defineConfig, devices } from '@playwright/test'

const baseURL = process.env.PLAYWRIGHT_BASE_URL || 'http://127.0.0.1:3000'
const apiURL = process.env.PLAYWRIGHT_API_URL || 'http://127.0.0.1:8080'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 1 : undefined,
  timeout: 60_000,
  expect: { timeout: 15_000 },
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'playwright-report' }],
    ['junit', { outputFile: 'test-results/junit.xml' }]
  ],
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    locale: 'zh-CN'
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: process.env.PLAYWRIGHT_SKIP_SERVERS
    ? undefined
    : [
        {
          command: process.platform === 'win32'
            ? 'cd .. && mvnw.cmd -q spring-boot:run -Dspring-boot.run.profiles=dev'
            : 'cd .. && ./mvnw -q spring-boot:run -Dspring-boot.run.profiles=dev',
          url: `${apiURL}/api/c/home/`,
          reuseExistingServer: !process.env.CI,
          timeout: 180_000,
          stdout: 'pipe',
          stderr: 'pipe'
        },
        {
          command: 'npm run dev -- --host 127.0.0.1 --port 3000',
          url: baseURL,
          reuseExistingServer: !process.env.CI,
          timeout: 120_000,
          stdout: 'pipe',
          stderr: 'pipe'
        }
      ]
})
