# PharmaceuticalOrderingMallCode · 医药订货商城

医药 B2B 订货平台：Spring Boot 后端 + Vue3 管理端/采购端，含资质门禁、对公凭证、退款审核、结算对账、Playwright E2E 与 GitHub Actions CI。

仓库地址：<https://github.com/chenyifan-ai/PharmaceuticalOrderingMallCode>

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

## 测试

```powershell
.\mvnw.cmd test
powershell -File scripts\api-smoke-test.ps1
cd admin-web; npm ci; npx playwright install chromium; npm run test:e2e
```
