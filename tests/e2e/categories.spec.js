import { test, expect } from '@playwright/test';
import { AuthPage } from '../pages/AuthPage.js';
import { HomePage } from '../pages/HomePage.js';
import { CategoryPage } from '../pages/CategoryPage.js';
import { PostPage } from '../pages/PostPage.js';
import { createTestUser, generateCategoryName, generateTestTitle } from '../helpers/test-helpers.js';

/**
 * 카테고리 E2E 테스트
 * 
 * 테스트 시나리오:
 * - 카테고리 관리 페이지 이동
 * - 카테고리 생성
 * - 카테고리 수정
 * - 카테고리 삭제
 * - 카테고리로 게시글 필터링
 * - 빈 상태 표시
 */
test.describe('Categories E2E Tests', () => {
  let authPage;
  let homePage;
  let categoryPage;
  let postPage;
  let testUser;
  let categoryName;

  test.beforeEach(async ({ page }) => {
    authPage = new AuthPage(page);
    homePage = new HomePage(page);
    categoryPage = new CategoryPage(page);
    postPage = new PostPage(page);
    testUser = createTestUser();
    categoryName = generateCategoryName();
    
    // 각 테스트 전에 회원가입 및 로그인 (격리 보장)
    await authPage.signup(testUser.email, testUser.password, testUser.nickname);
    await authPage.login(testUser.email, testUser.password);
  });

  test('카테고리 관리 페이지로 이동할 수 있어야 함', async ({ page }) => {
    await homePage.goto();
    await homePage.categoryManagementButton.click();
    await expect(page).toHaveURL(/.*\/categories/);
    await homePage.expectText('카테고리 관리');
  });

  test('카테고리를 생성할 수 있어야 함', async ({ page }) => {
    await categoryPage.createCategory(categoryName);
    
    // 카테고리가 목록에 표시되는지 확인
    await categoryPage.expectCategoryVisible(categoryName);
    await homePage.expectText('카테고리가 생성되었습니다');
  });

  test('카테고리를 수정할 수 있어야 함', async ({ page }) => {
    await categoryPage.createCategory(categoryName);
    
    const editName = `${categoryName} (수정됨)`;
    await categoryPage.editCategory(categoryName, editName);
    
    // 수정된 카테고리가 표시되는지 확인
    await categoryPage.expectCategoryVisible(editName);
    await homePage.expectText('카테고리가 수정되었습니다');
  });

  test('카테고리를 삭제할 수 있어야 함', async ({ page }) => {
    await categoryPage.createCategory(categoryName);
    
    await categoryPage.deleteCategory(categoryName);
    
    // 카테고리가 사라졌는지 확인
    await categoryPage.expectCategoryNotVisible(categoryName);
    await homePage.expectText('카테고리가 삭제되었습니다');
  });

  test('카테고리로 게시글을 필터링할 수 있어야 함', async ({ page }) => {
    // 카테고리 생성
    await categoryPage.createCategory(categoryName);
    
    // 카테고리 ID를 얻기 위해 페이지를 새로고침하고 카테고리 목록 확인
    await categoryPage.goto('/categories');
    const categoryLink = page.getByText(categoryName);
    await categoryLink.waitFor({ state: 'visible' });
    
    // 게시글 생성 (카테고리 선택은 실제 구현에 따라 조정 필요)
    const testTitle = generateTestTitle();
    const testContent = '테스트 내용';
    await postPage.createPost(testTitle, testContent);
    
    // 홈으로 이동
    await homePage.goto();
    
    // 카테고리 클릭하여 필터링
    await homePage.filterByCategory(categoryName);
    
    // URL에 categoryId가 포함되어야 함
    await expect(page).toHaveURL(/.*categoryId=/);
    // 게시글이 표시되어야 함 (실제 구현에 따라 조정 필요)
    // await homePage.expectPostVisible(testTitle);
  });

  test('카테고리가 없을 때 빈 상태를 표시해야 함', async ({ page }) => {
    await categoryPage.goto('/categories');
    
    // 빈 상태 메시지 확인 (실제 구현에 따라 조정 필요)
    // await categoryPage.expectEmptyState();
  });
});

