import { test, expect } from '@playwright/test';
import { AuthPage } from '../pages/AuthPage.js';
import { HomePage } from '../pages/HomePage.js';
import { PostPage } from '../pages/PostPage.js';
import { CommentPage } from '../pages/CommentPage.js';
import { createTestUser, generateTestTitle } from '../helpers/test-helpers.js';

/**
 * 댓글 E2E 테스트
 * 
 * 테스트 시나리오:
 * - 댓글 섹션 표시
 * - 댓글 생성
 * - 댓글 수정
 * - 댓글 삭제
 * - 대댓글 생성
 * - 로그인하지 않은 사용자는 댓글 작성 불가
 */
test.describe('Comments E2E Tests', () => {
  let authPage;
  let homePage;
  let postPage;
  let commentPage;
  let testUser;
  let testTitle;
  const testContent = '이것은 테스트 게시글 내용입니다.';
  const testComment = '테스트 댓글입니다.';
  const testReply = '테스트 대댓글입니다.';

  test.beforeEach(async ({ page }) => {
    authPage = new AuthPage(page);
    homePage = new HomePage(page);
    postPage = new PostPage(page);
    commentPage = new CommentPage(page);
    testUser = createTestUser();
    testTitle = generateTestTitle();
    
    // 각 테스트 전에 회원가입, 로그인, 게시글 생성 (격리 보장)
    await authPage.signup(testUser.email, testUser.password, testUser.nickname);
    await authPage.login(testUser.email, testUser.password);
    await postPage.createPost(testTitle, testContent);
  });

  test('댓글 섹션이 표시되어야 함', async ({ page }) => {
    await page.getByRole('heading', { name: /댓글/i }).first().waitFor({ state: 'visible' });
    await page.getByRole('heading', { name: /댓글 작성/i }).first().waitFor({ state: 'visible' });
  });

  test('댓글을 생성할 수 있어야 함', async ({ page }) => {
    await commentPage.createComment(testComment);
    
    // 댓글이 표시되는지 확인
    await commentPage.expectCommentVisible(testComment);
    await page.getByRole('main').getByText(testUser.nickname).first().waitFor({ state: 'visible' });
  });

  test('댓글을 수정할 수 있어야 함', async ({ page }) => {
    await commentPage.createComment(testComment);
    
    const editComment = `${testComment} (수정됨)`;
    await commentPage.editComment(testComment, editComment);
    
    // 수정된 댓글이 표시되는지 확인
    await commentPage.expectCommentVisible(editComment);
  });

  test('댓글을 삭제할 수 있어야 함', async ({ page }) => {
    await commentPage.createComment(testComment);
    
    await commentPage.deleteComment(testComment);
    
    // 댓글이 사라졌는지 확인
    await commentPage.expectCommentNotVisible(testComment);
  });

  test('대댓글을 생성할 수 있어야 함', async ({ page }) => {
    await commentPage.createComment(testComment);
    
    await commentPage.createReply(testComment, testReply);
    
    // 대댓글이 표시되는지 확인
    await commentPage.expectCommentVisible(testReply);
    await page.getByRole('main').getByText(testUser.nickname).first().waitFor({ state: 'visible' });
  });

  test('로그인하지 않은 사용자는 댓글을 작성할 수 없어야 함', async ({ page }) => {
    // 로그아웃
    await authPage.logout();
    
    // 게시글 상세 페이지로 이동
    await homePage.goto();
    await homePage.clickPostByTitle(testTitle);
    
    // 로그인 링크가 표시되어야 함
    await homePage.loginButton.waitFor({ state: 'visible' });
    // 댓글 입력창이 없어야 함
    await expect(commentPage.commentTextarea).not.toBeVisible();
  });
});

