# E2E 测试（Playwright）

对应用例见仓库根目录 `医药订货系统-实现版需求与测试.md` 第 5 节（`FE-AUTH` / `FE-ADM` / `FE-C`）。

## 本地运行

```bash
cd admin-web
npm ci
npx playwright install chromium   # 首次需下载浏览器
npm run test:e2e                  # 自动启动后端(8080) + 前端(3000)
```

若服务已启动：

```bash
# PowerShell
$env:PLAYWRIGHT_SKIP_SERVERS='1'
npm run test:e2e
```

## 报告

- HTML：`npm run test:e2e:report`
- JUnit：`test-results/junit.xml`（CI 写入 PR Checks）

## CI

`.github/workflows/ci.yml` 的 `e2e` job 会执行全部用例并上传 `playwright-report`。
