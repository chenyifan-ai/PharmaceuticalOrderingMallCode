import { test, expect } from '@playwright/test'
import { loginViaAccountTip } from './helpers/auth'

test.describe('FE-C 采购端', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaAccountTip(page, 'user')
    await expect(page).toHaveURL(/\/c\/home/)
  })

  async function openConsumerNav(page: import('@playwright/test').Page, label: string | RegExp) {
    const link = page.locator('.consumer-header a, .consumer-nav a, .el-menu a').filter({ hasText: label })
    if (await link.count()) {
      await link.first().click()
      return
    }
    await page.goto(`/c/${typeof label === 'string' ? label.replace(/^\//, '') : 'products'}`)
  }

  test('FE-C-01 采购首页', async ({ page }) => {
    await expect(page.locator('.home-page')).toBeVisible()
    const sections = page.locator('.home-sections, .banner-placeholder, .home-banner-section')
    await expect(sections.first()).toBeVisible()
  })

  test('FE-C-02 商品列表', async ({ page }) => {
    await page.goto('/c/products')
    await expect(page.locator('.consumer-page, .product-list')).toBeVisible()
    await expect(page.locator('.el-card, .product-card, .el-table, .el-empty').first()).toBeVisible()
  })

  test('FE-C-03 购物车', async ({ page }) => {
    await page.goto('/c/cart')
    await expect(page.locator('.consumer-page')).toBeVisible()
    await expect(page.locator('.el-table, .el-empty, .cart')).toBeVisible()
  })

  test('FE-C-04 我的订单', async ({ page }) => {
    await page.goto('/c/orders')
    await expect(page.locator('.consumer-page')).toBeVisible()
    await expect(page.locator('.el-table, .el-empty, .order-list')).toBeVisible()
  })

  test('FE-C-05 企业资质', async ({ page }) => {
    await page.goto('/c/qualification')
    await expect(page.locator('.consumer-page')).toBeVisible()
    await expect(page.locator('.el-form, .el-descriptions, .el-empty').first()).toBeVisible()
  })

  test('FE-C-06 我的处方', async ({ page }) => {
    await page.goto('/c/prescriptions')
    await expect(page).toHaveURL(/\/c\/prescriptions/)
    await expect(page.locator('.el-table, .el-empty')).toBeVisible()
  })

  test('FE-C-07 采购统计', async ({ page }) => {
    await page.goto('/c/purchase-stats')
    await expect(page).toHaveURL(/\/c\/purchase-stats/)
    await expect(page.locator('.page-card, .el-card, .stat').first()).toBeVisible()
  })

  test('FE-C-08 个人中心', async ({ page }) => {
    await page.goto('/c/profile')
    await expect(page).toHaveURL(/\/c\/profile/)
    await expect(page.locator('.el-form, .el-card').first()).toBeVisible()
  })
})
