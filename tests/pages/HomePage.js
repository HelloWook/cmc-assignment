import { expect } from '@playwright/test';
import { BasePage } from './BasePage.js';

/**
 * HomePage - 메인 페이지 객체
 */
export class HomePage extends BasePage {
  constructor(page) {
    super(page);
  }

  get postsList() {
    return this.page.getByText('게시글 목록');
  }

  get writeButton() {
    return this.page.getByRole('link', { name: /글쓰기/i }).first();
  }

  get loginButton() {
    return this.page.getByRole('link', { name: /로그인/i }).first();
  }

  get signupButton() {
    return this.page.getByRole('link', { name: /회원가입/i }).first();
  }

  get categoryManagementButton() {
    return this.page.getByRole('link', { name: /카테고리 관리/i }).first();
  }

  /**
   * 게시글 제목으로 클릭
   * @param {string} title - 게시글 제목
   */
  async clickPostByTitle(title) {
    const postLink = this.page.locator(`a[href*="/posts/"]`).filter({ hasText: title }).first();
    await postLink.click();
    await this.page.waitForURL(/.*\/posts\/\d+/, { timeout: 10000 });
  }

  /**
   * 게시글이 표시되는지 확인
   * @param {string} title - 게시글 제목
   */
  async expectPostVisible(title) {
    await this.page.getByText(title).waitFor({ state: 'visible' });
  }

  /**
   * 게시글이 표시되지 않는지 확인
   * @param {string} title - 게시글 제목
   */
  async expectPostNotVisible(title) {
    await expect(this.page.getByText(title)).not.toBeVisible();
  }

  /**
   * 카테고리로 필터링
   * @param {string} categoryName - 카테고리 이름
   */
  async filterByCategory(categoryName) {
    const categoryLink = this.page.locator(`a[href*="categoryId="]`).filter({ hasText: categoryName }).first();
    await categoryLink.click();
    await this.page.waitForURL(/.*categoryId=/, { timeout: 5000 });
  }
}

