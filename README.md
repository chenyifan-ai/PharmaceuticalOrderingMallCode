# 医药订货系统 (Pharmacy Mall)

医药 B2B 订货平台：Spring Boot 后端 + Vue3 管理端/采购端，含资质门禁、对公凭证、退款审核、结算对账、Playwright E2E 与 GitHub Actions CI。

## 快速启动

- Windows：双击 `一键启动.bat`（或见 `启动说明.md`）
- 管理端：http://localhost:3000  
- 后端 API：http://localhost:8080  

**测试账号**（密码均为 `admin123`）：

| 手机号 | 角色 |
|--------|------|
| 13800000000 | 管理员 |
| 13800000001 | 供应商 |
| 13800000002 | 采购用户 |

## 文档

- [实现版需求与测试](医药订货系统-实现版需求与测试.md)
- [原需求文档](医药订货系统需求文档.md)

## 推送到 GitHub

```powershell
# 1. 安装并登录 GitHub CLI: https://cli.github.com/
gh auth login

# 2. 创建私有仓库并推送（仓库名可改）
powershell -ExecutionPolicy Bypass -File scripts\push-to-github.ps1 -RepoName pharmacy-mall -Visibility private
```

或手动在 GitHub 新建空仓库后：

```powershell
git remote add origin https://github.com/<你的用户名>/pharmacy-mall.git
git branch -M main
git push -u origin main
```

## 测试

```powershell
.\mvnw.cmd test
powershell -File scripts\api-smoke-test.ps1
cd admin-web; npm ci; npx playwright install chromium; npm run test:e2e
```
