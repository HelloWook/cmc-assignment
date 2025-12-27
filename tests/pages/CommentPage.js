import { expect } from "@playwright/test";
import { BasePage } from "./BasePage.js";

/**
 * CommentPage - 댓글 관련 페이지 객체
 */
export class CommentPage extends BasePage {
  constructor(page) {
    super(page);
  }

  get commentTextarea() {
    return this.page.locator('textarea[name="content"]').first();
  }

  get commentSubmitButton() {
    return this.page.getByRole("button", { name: /댓글 작성/i }).first();
  }

  /**
   * 댓글 작성
   * @param {string} content - 댓글 내용
   */
  async createComment(content) {
    await this.commentTextarea.fill(content);
    await this.commentSubmitButton.click();
    await this.page
      .locator('[id^="comment-content-"]')
      .filter({ hasText: content })
      .first()
      .waitFor({ state: "visible", timeout: 10000 });
  }

  /**
   * 댓글 수정
   * @param {string} oldContent - 기존 댓글 내용
   * @param {string} newContent - 새 댓글 내용
   */
  async editComment(oldContent, newContent) {
    const commentText = this.page.getByText(oldContent).first();
    const commentContainer = commentText.locator("..").locator("..");
    const commentContent = commentContainer
      .locator('[id^="comment-content-"]')
      .first();
    const commentId = await commentContent.getAttribute("id");
    const idMatch = commentId?.match(/comment-content-(\d+)/);
    if (!idMatch) throw new Error("Comment ID not found");

    await this.page
      .locator(`button[onclick*="editComment(${idMatch[1]})"]`)
      .first()
      .click();

    await this.page
      .locator(`#comment-edit-${idMatch[1]}`)
      .waitFor({ state: "visible" });

    const editTextarea = this.page.locator(`#edit-content-${idMatch[1]}`);
    await editTextarea.clear();
    await editTextarea.fill(newContent);

    await this.page
      .locator(
        `form[action*="/comments/${idMatch[1]}/edit"] button[type="submit"]`
      )
      .click();

    await this.page
      .locator('[id^="comment-content-"]')
      .filter({ hasText: newContent })
      .first()
      .waitFor({ state: "visible", timeout: 10000 });
  }

  /**
   * 댓글 삭제
   * @param {string} content - 삭제할 댓글 내용
   */
  async deleteComment(content) {
    const commentText = this.page.getByText(content).first();
    const commentContainer = commentText.locator("..").locator("..");
    const commentContent = commentContainer
      .locator('[id^="comment-content-"]')
      .first();
    const commentId = await commentContent.getAttribute("id");
    const idMatch = commentId?.match(/comment-content-(\d+)/);
    if (!idMatch) throw new Error("Comment ID not found");

    this.page.once("dialog", (dialog) => dialog.accept());

    await this.page
      .locator(
        `form[action*="/comments/${idMatch[1]}/delete"] button[type="submit"]`
      )
      .click();

    await this.page
      .getByText(content)
      .waitFor({ state: "hidden", timeout: 10000 });
  }

  /**
   * 대댓글 작성
   * @param {string} parentCommentContent - 부모 댓글 내용
   * @param {string} replyContent - 대댓글 내용
   */
  async createReply(parentCommentContent, replyContent) {
    const commentText = this.page.getByText(parentCommentContent).first();
    const commentContainer = commentText.locator("..").locator("..");
    const commentContent = commentContainer
      .locator('[id^="comment-content-"]')
      .first();
    const commentId = await commentContent.getAttribute("id");
    const idMatch = commentId?.match(/comment-content-(\d+)/);
    if (!idMatch) throw new Error("Comment ID not found");

    await this.page
      .locator(`button[onclick*="showReplyForm(${idMatch[1]})"]`)
      .first()
      .click();

    const replyForm = this.page.locator(`#reply-form-${idMatch[1]}`);
    await replyForm.waitFor({ state: "visible" });

    await replyForm.locator('textarea[name="content"]').fill(replyContent);
    await replyForm.getByRole("button", { name: /작성/i }).click();

    await this.page
      .locator('[id^="comment-content-"]')
      .filter({ hasText: replyContent })
      .first()
      .waitFor({ state: "visible", timeout: 10000 });
  }

  /**
   * 댓글이 표시되는지 확인
   * @param {string} content - 댓글 내용
   */
  async expectCommentVisible(content) {
    await this.page
      .locator('[id^="comment-content-"]')
      .filter({ hasText: content })
      .first()
      .waitFor({ state: "visible" });
  }

  /**
   * 댓글이 표시되지 않는지 확인
   * @param {string} content - 댓글 내용
   */
  async expectCommentNotVisible(content) {
    await expect(
      this.page
        .locator('[id^="comment-content-"]')
        .filter({ hasText: content })
        .first()
    ).not.toBeVisible();
  }
}
