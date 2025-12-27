import { BasePage } from "./BasePage.js";

/**
 * CategoryPage - 카테고리 관리 페이지 객체
 */
export class CategoryPage extends BasePage {
  constructor(page) {
    super(page);
  }

  get categoryNameInput() {
    return this.page.locator("#categoryName");
  }

  get addCategoryButton() {
    return this.page.getByRole("button", { name: /추가하기/i });
  }

  /**
   * 카테고리 생성
   * @param {string} name - 카테고리 이름
   */
  async createCategory(name) {
    await this.goto("/categories");
    await this.categoryNameInput.fill(name);
    await this.addCategoryButton.click();
    await this.page.getByText(name).waitFor({ state: "visible" });
  }

  /**
   * 카테고리 수정
   * @param {string} oldName - 기존 카테고리 이름
   * @param {string} newName - 새 카테고리 이름
   */
  async editCategory(oldName, newName) {
    const categoryNameElement = this.page
      .locator('[id^="category-name-"]')
      .filter({ hasText: oldName })
      .first();
    const categoryIdAttr = await categoryNameElement.getAttribute("id");
    const idMatch = categoryIdAttr?.match(/category-name-(\d+)/);
    if (!idMatch) throw new Error("Category ID not found");

    await this.page
      .locator(`button[onclick*="editCategory(${idMatch[1]})"]`)
      .first()
      .click();

    const editForm = this.page.locator(`#category-edit-${idMatch[1]}`);
    await editForm.waitFor({ state: "visible" });

    const textbox = this.page.locator(`#edit-name-${idMatch[1]}`);
    await textbox.clear();
    await textbox.fill(newName);

    await this.page
      .locator(
        `form[action*="/categories/${idMatch[1]}/edit"] button[type="submit"]`
      )
      .click();

    await this.page
      .getByText(newName)
      .waitFor({ state: "visible", timeout: 10000 });
  }

  /**
   * 카테고리 삭제
   * @param {string} name - 삭제할 카테고리 이름
   */
  async deleteCategory(name) {
    const categoryNameElement = this.page
      .locator('[id^="category-name-"]')
      .filter({ hasText: name })
      .first();
    const categoryIdAttr = await categoryNameElement.getAttribute("id");
    const idMatch = categoryIdAttr?.match(/category-name-(\d+)/);
    if (!idMatch) throw new Error("Category ID not found");

    this.page.once("dialog", (dialog) => dialog.accept());

    await this.page
      .locator(
        `form[action*="/categories/${idMatch[1]}/delete"] button[type="submit"]`
      )
      .click();

    await this.page
      .getByText(name)
      .waitFor({ state: "hidden", timeout: 10000 });
  }

  /**
   * 카테고리가 표시되는지 확인
   * @param {string} name - 카테고리 이름
   */
  async expectCategoryVisible(name) {
    await this.page.getByText(name).waitFor({ state: "visible" });
  }

  /**
   * 카테고리가 표시되지 않는지 확인
   * @param {string} name - 카테고리 이름
   */
  async expectCategoryNotVisible(name) {
    const { expect } = await import("@playwright/test");
    await expect(this.page.getByText(name)).not.toBeVisible();
  }

  /**
   * 빈 상태 메시지 확인
   */
  async expectEmptyState() {
    await this.page
      .getByText("등록된 카테고리가 없습니다")
      .waitFor({ state: "visible" });
  }
}
