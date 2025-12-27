import { test, expect } from '@playwright/test';
import { AuthPage } from '../pages/AuthPage.js';
import { HomePage } from '../pages/HomePage.js';
import { PostPage } from '../pages/PostPage.js';
import { createTestUser, generateTestTitle } from '../helpers/test-helpers.js';

/**
 * 게시글 E2E 테스트
 * 
 * 테스트 시나리오:
 * - 게시글 목록 표시
 * - 게시글 작성 페이지 이동
 * - 게시글 생성
 * - 게시글 상세 보기
 * - 게시글 수정
 * - 게시글 삭제
 * - 다른 사용자의 게시글 수정/삭제 불가
 */
test.describe('Posts E2E Tests', () => {
  let authPage;
  let homePage;
  let postPage;
  let testUser;
  let testTitle;
  const testContent = '이것은 테스트 게시글 내용입니다.';

  test.beforeEach(async ({ page }) => {
    authPage = new AuthPage(page);
    homePage = new HomePage(page);
    postPage = new PostPage(page);
    testUser = createTestUser();
    testTitle = generateTestTitle();
    
    // 각 테스트 전에 회원가입 및 로그인 (격리 보장)
    await authPage.signup(testUser.email, testUser.password, testUser.nickname);
    await authPage.login(testUser.email, testUser.password);
  });

  test('게시글 목록이 표시되어야 함', async ({ page }) => {
    await homePage.goto();
    await page.getByRole('heading', { name: /게시글 목록/i }).waitFor({ state: 'visible' });
    await homePage.writeButton.waitFor({ state: 'visible' });
  });

  test('게시글 작성 페이지로 이동할 수 있어야 함', async ({ page }) => {
    await homePage.goto();
    await homePage.writeButton.click();
    await expect(page).toHaveURL(/.*\/posts\/new/);
    await page.getByRole('heading', { name: /게시글 작성/i }).waitFor({ state: 'visible' });
  });

  test('새 게시글을 생성할 수 있어야 함', async ({ page }) => {
    await postPage.createPost(testTitle, testContent);
    
    // 게시글 상세 페이지에서 내용 확인
    await postPage.expectPostContent(testTitle, testContent);
    await page.getByRole('main').getByText(testUser.nickname).first().waitFor({ state: 'visible' });
  });

  test('게시글 상세를 볼 수 있어야 함', async ({ page }) => {
    await postPage.createPost(testTitle, testContent);
    
    // 상세 페이지에서 모든 내용 확인
    await postPage.expectPostContent(testTitle, testContent);
    await page.getByRole('main').getByText(testUser.nickname).first().waitFor({ state: 'visible' });
    await page.getByRole('heading', { name: /댓글/i }).first().waitFor({ state: 'visible' });
  });

  test('게시글을 수정할 수 있어야 함', async ({ page }) => {
    await postPage.createPost(testTitle, testContent);
    
    const editTitle = `${testTitle} (수정됨)`;
    const editContent = `${testContent} 수정된 내용입니다.`;
    
    await postPage.editPost(editTitle, editContent);
    
    // 수정된 내용 확인
    await postPage.expectPostContent(editTitle, editContent);
  });

  test('게시글을 삭제할 수 있어야 함', async ({ page }) => {
    await postPage.createPost(testTitle, testContent);
    
    // 홈으로 이동
    await homePage.goto();
    await homePage.clickPostByTitle(testTitle);
    
    // 삭제
    await postPage.deletePost();
    
    // 홈으로 리다이렉트 확인
    await expect(page).toHaveURL(/.*\/$/);
    // 게시글이 목록에서 사라졌는지 확인
    await homePage.expectPostNotVisible(testTitle);
  });

  test('다른 사용자의 게시글은 수정/삭제할 수 없어야 함', async ({ page }) => {
    // 첫 번째 사용자로 게시글 생성
    await postPage.createPost(testTitle, testContent);
    
    // 로그아웃
    await authPage.logout();
    
    // 두 번째 사용자 생성 및 로그인
    const otherUser = createTestUser();
    await authPage.signup(otherUser.email, otherUser.password, otherUser.nickname);
    await authPage.login(otherUser.email, otherUser.password);
    
    // 게시글 상세 페이지로 이동
    await homePage.goto();
    await homePage.clickPostByTitle(testTitle);
    
    // 수정/삭제 버튼이 없어야 함
    await postPage.expectNoEditDeleteButtons();
  });
});

