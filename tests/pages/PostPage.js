import { expect } from '@playwright/test';
import { BasePage } from './BasePage.js';

/**
 * PostPage - 게시글 관련 페이지 객체
 */
export class PostPage extends BasePage {
  constructor(page) {
    super(page);
  }

  get titleInput() {
    return this.page.locator('#title').or(this.page.getByLabel('제목'));
  }

  get contentTextarea() {
    return this.page.locator('#content').or(this.page.getByLabel('내용'));
  }

  get submitButton() {
    return this.page.getByRole('button', { name: /작성하기|수정하기/i });
  }

  get editButton() {
    return this.page.getByRole('link', { name: /수정/i }).first();
  }

  get deleteButton() {
    return this.page.locator('form[action*="/delete"] button[type="submit"]').first();
  }

  /**
   * 게시글 작성
   * @param {string} title - 제목
   * @param {string} content - 내용
   * @param {number[]} categoryIds - 카테고리 ID 배열 (선택)
   */
  async createPost(title, content, categoryIds = []) {
    await this.goto('/posts/new');
    await this.page.waitForLoadState('networkidle');
    await this.titleInput.fill(title);
    await this.contentTextarea.fill(content);
    
    if (categoryIds.length > 0) {
      for (const categoryId of categoryIds) {
        await this.page.locator(`input[type="checkbox"][value="${categoryId}"]`).check();
      }
    }
    
    await this.submitButton.click();
    await this.page.waitForURL('**/posts/**', { timeout: 10000 });
  }

  /**
   * 게시글 수정
   * @param {string} title - 새 제목
   * @param {string} content - 새 내용
   */
  async editPost(title, content) {
    await this.editButton.click();
    await this.page.waitForURL('**/edit', { timeout: 10000 });
    await this.page.waitForLoadState('networkidle');
    await this.titleInput.clear();
    await this.titleInput.fill(title);
    await this.contentTextarea.clear();
    await this.contentTextarea.fill(content);
    await this.submitButton.click();
    await this.page.waitForURL('**/posts/**', { timeout: 10000 });
  }

  /**
   * 게시글 삭제
   */
  async deletePost() {
    this.page.once('dialog', dialog => dialog.accept());
    await this.deleteButton.click();
    await this.page.waitForURL('**/', { timeout: 10000 });
  }

  /**
   * 게시글 내용 확인
   * @param {string} title - 제목
   * @param {string} content - 내용
   */
  async expectPostContent(title, content) {
    await this.page.getByRole('heading', { name: title }).waitFor({ state: 'visible' });
    await this.page.locator('.post-content').filter({ hasText: content }).first().waitFor({ state: 'visible' });
  }

  /**
   * 수정/삭제 버튼이 없는지 확인 (다른 사용자의 게시글)
   */
  async expectNoEditDeleteButtons() {
    await expect(this.editButton).not.toBeVisible();
    await expect(this.deleteButton).not.toBeVisible();
  }
}

