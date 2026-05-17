import { test, expect } from '@playwright/test'
import { loginViaAccountTip, gotoAdminPage } from './helpers/auth'

test.describe('FE-ADM 管理端', () => {
  test.beforeEach(async ({ page }) => {
    await loginViaAccountTip(page, 'admin')
  })

  test('FE-ADM-01 Dashboard 数据概览加载', async ({ page }) => {
    await expect(page).toHaveURL(/\/dashboard/)
    await expect(page.locator('.main-content')).toBeVisible()
    const chartOrEmpty = page.locator('canvas, .dashboard, .page-card, .el-empty')
    await expect(chartOrEmpty.first()).toBeVisible()
  })

  test('FE-ADM-02 商品管理列表', async ({ page }) => {
    await gotoAdminPage(page, '商品管理')
    await expect(page).toHaveURL(/\/products/)
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.getByRole('button', { name: /添加|新增/ }).first()).toBeVisible()
  })

  test('FE-ADM-03 供应商管理', async ({ page }) => {
    await gotoAdminPage(page, '供应商管理')
    await expect(page).toHaveURL(/\/merchants/)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('FE-ADM-04 资质审核', async ({ page }) => {
    await gotoAdminPage(page, '资质审核')
    await expect(page).toHaveURL(/\/qualifications/)
    await expect(page.locator('.el-table')).toBeVisible()
  })

  test('FE-ADM-05 付款凭证审核', async ({ page }) => {
    await gotoAdminPage(page, '付款凭证审核')
    await expect(page).toHaveURL(/\/payment-vouchers/)
    await expect(page.locator('.el-table, .el-empty')).toBeVisible()
  })

  test('FE-ADM-06 退款审核', async ({ page }) => {
    await gotoAdminPage(page, '退款审核')
    await expect(page).toHaveURL(/\/refund-audit/)
    await expect(page.getByRole('tab', { name: '待审核' })).toBeVisible()
    await expect(page.getByRole('tab', { name: '已退款' })).toBeVisible()
  })

  test('FE-ADM-07 结算管理', async ({ page }) => {
    await gotoAdminPage(page, '结算管理')
    await expect(page).toHaveURL(/\/settlements/)
    await expect(page.getByRole('button', { name: '生成结算单' })).toBeVisible()
    await expect(page.locator('.el-table, .el-empty')).toBeVisible()
  })

  test('FE-ADM-08 订单管理', async ({ page }) => {
    await gotoAdminPage(page, '订单管理')
    await expect(page).toHaveURL(/\/orders/)
    await expect(page.locator('.el-table')).toBeVisible()
    await expect(page.getByRole('button', { name: '搜索' })).toBeVisible()
  })

  test('FE-ADM-09 库存管理', async ({ page }) => {
    await gotoAdminPage(page, '库存管理')
    await expect(page).toHaveURL(/\/stock/)
    await expect(page.locator('.page-card, .el-table')).toBeVisible()
  })

  test('FE-ADM-10 用户管理', async ({ page }) => {
    await gotoAdminPage(page, '用户管理')
    await expect(page).toHaveURL(/\/users/)
    await expect(page.locator('.el-table')).toBeVisible()
  })
})
