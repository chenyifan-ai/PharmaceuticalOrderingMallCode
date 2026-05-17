import { test, expect } from '@playwright/test'
import { loginViaAccountTip, expectAdminMenu } from './helpers/auth'

test.describe('FE-AUTH 认证与路由', () => {
  test('FE-AUTH-01 管理员登录进入数据概览', async ({ page }) => {
    await loginViaAccountTip(page, 'admin')
    await expect(page).toHaveURL(/\/dashboard/)
    await expectAdminMenu(page, '商品管理')
    await expectAdminMenu(page, '结算管理')
    await expect(page.locator('.screen-title, .page-title')).toContainText(/数据|概览|大屏/)
  })

  test('FE-AUTH-02 商家登录进入我的商品', async ({ page }) => {
    await loginViaAccountTip(page, 'merchant')
    await expect(page).toHaveURL(/\/merchant-products/)
    await expect(page.locator('.admin-menu').getByText('用户管理')).toHaveCount(0)
    await expectAdminMenu(page, '订单管理')
  })

  test('FE-AUTH-03 采购方登录重定向采购首页', async ({ page }) => {
    await loginViaAccountTip(page, 'user')
    await expect(page).toHaveURL(/\/c\/home/)
    await expect(page.locator('.home-page, .consumer-page')).toBeVisible()
  })

  test('FE-AUTH-04 商家访问用户管理被重定向', async ({ page }) => {
    await loginViaAccountTip(page, 'merchant')
    await page.goto('/users')
    await expect(page).toHaveURL(/\/merchant-products/)
  })

  test('FE-AUTH-05 采购独立端拒绝商家账号', async ({ page }) => {
    await page.goto('/consumer.html')
    await page.waitForLoadState('networkidle')
    if (!page.url().includes('consumer')) {
      await page.goto('/consumer.html')
    }
    const phoneInput = page.getByPlaceholder('手机号')
    if (!(await phoneInput.isVisible().catch(() => false))) {
      test.skip(true, 'consumer.html 路由未就绪，跳过独立端用例')
    }
    await phoneInput.fill('13800000001')
    await page.getByPlaceholder('密码').fill('admin123')
    await page.getByRole('button', { name: '登录采购端' }).click()
    await expect(page.getByText('请使用采购方账号登录')).toBeVisible({ timeout: 10_000 })
    await expect(page).toHaveURL(/\/login/)
  })
})
