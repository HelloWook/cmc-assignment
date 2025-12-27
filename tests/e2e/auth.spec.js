import { test, expect } from '@playwright/test';
import { AuthPage } from '../pages/AuthPage.js';
import { HomePage } from '../pages/HomePage.js';
import { createTestUser } from '../helpers/test-helpers.js';

/**
 * 인증 E2E 테스트
 * 
 * 테스트 시나리오:
 * - 홈 페이지 표시
 * - 회원가입 페이지 이동
 * - 로그인 페이지 이동
 * - 회원가입
 * - 로그인
 * - 잘못된 로그인 정보 처리
 * - 로그아웃
 */
test.describe('Authentication E2E Tests', () => {
  let authPage;
  let homePage;
  let testUser;

  test.beforeEach(async ({ page }) => {
    authPage = new AuthPage(page);
    homePage = new HomePage(page);
    testUser = createTestUser();
  });

  test('홈 페이지가 정상적으로 표시되어야 함', async ({ page }) => {
    await homePage.goto();
    await homePage.expectText('게시글 목록');
    await page.getByRole('heading', { name: /게시글 목록/i }).waitFor({ state: 'visible' });
  });

  test('회원가입 페이지로 이동할 수 있어야 함', async ({ page }) => {
    await homePage.goto();
    await homePage.signupButton.click();
    await expect(page).toHaveURL(/.*\/signup/);
    await page.getByRole('heading', { name: /회원가입/i }).waitFor({ state: 'visible' });
  });

  test('로그인 페이지로 이동할 수 있어야 함', async ({ page }) => {
    await homePage.goto();
    await homePage.loginButton.click();
    await expect(page).toHaveURL(/.*\/login/);
    await page.getByRole('heading', { name: /로그인/i }).waitFor({ state: 'visible' });
  });

  test('새 사용자를 회원가입시킬 수 있어야 함', async ({ page }) => {
    await authPage.signup(testUser.email, testUser.password, testUser.nickname);
    await expect(page).toHaveURL(/.*\/login/);
    await authPage.expectSuccessMessage('회원가입이 완료되었습니다');
  });

  test('유효한 자격증명으로 로그인할 수 있어야 함', async ({ page }) => {
    // 먼저 회원가입
    await authPage.signup(testUser.email, testUser.password, testUser.nickname);
    
    // 로그인
    await authPage.login(testUser.email, testUser.password);
    
    // 홈으로 리다이렉트 확인
    await expect(page).toHaveURL(/.*\/$/);
    await page.getByRole('button', { name: new RegExp(testUser.nickname) }).waitFor({ state: 'visible' });
  });

  test('잘못된 로그인 정보로 로그인 시 에러를 표시해야 함', async ({ page }) => {
    await authPage.goto('/login');
    await authPage.emailInput.fill('invalid@example.com');
    await authPage.passwordInput.fill('wrongpassword');
    await authPage.loginSubmitButton.click();
    
    // 로그인 페이지에 머물러 있어야 함
    await expect(page).toHaveURL(/.*\/login/);
    await authPage.expectErrorMessage(/이메일 또는 비밀번호/);
  });

  test('로그아웃이 정상적으로 동작해야 함', async ({ page }) => {
    // 회원가입 및 로그인
    await authPage.signup(testUser.email, testUser.password, testUser.nickname);
    await authPage.login(testUser.email, testUser.password);
    
    // 로그아웃
    await authPage.logout();
    
    // 홈으로 리다이렉트 확인
    await expect(page).toHaveURL(/.*\/$/);
    await homePage.loginButton.waitFor({ state: 'visible' });
  });
});

