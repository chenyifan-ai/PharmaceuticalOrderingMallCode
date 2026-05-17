import { expect, type Page } from '@playwright/test'

export const ACCOUNTS = {
  admin: { phone: '13800000000', password: 'admin123' },
  merchant: { phone: '13800000001', password: 'admin123' },
  buyer: { phone: '13800000002', password: 'admin123' }
}

export async function loginAdminPortal(
  page: Page,
  role: keyof typeof ACCOUNTS = 'admin'
) {
  const { phone, password } = ACCOUNTS[role]
  await page.goto('/login')
  await page.getByPlaceholder('请输入手机号').fill(phone)
  await page.getByPlaceholder('请输入密码').fill(password)
  await page.getByRole('button', { name: /登\s*录/ }).click()
}

export async function loginViaAccountTip(
  page: Page,
  role: 'admin' | 'merchant' | 'user'
) {
  await page.goto('/login')
  const classMap = { admin: '.admin', merchant: '.merchant', user: '.user' }
  await page.locator(`.account-item${classMap[role]}`).click()
  await page.getByRole('button', { name: /登\s*录/ }).click()
}

export async function expectAdminMenu(page: Page, title: string) {
  await expect(page.locator('.admin-menu').getByText(title, { exact: true })).toBeVisible()
}

export async function gotoAdminPage(page: Page, menuTitle: string) {
  await page.locator('.admin-menu').getByText(menuTitle, { exact: true }).click()
}
