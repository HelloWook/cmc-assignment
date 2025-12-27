import { BasePage } from "./BasePage.js";

/**
 * AuthPage - 인증 관련 페이지 객체 (로그인, 회원가입)
 */
export class AuthPage extends BasePage {
  constructor(page) {
    super(page);
  }

  get emailInput() {
    return this.page.locator("#email").or(this.page.getByLabel("이메일"));
  }

  get passwordInput() {
    return this.page.locator("#password").or(this.page.getByLabel("비밀번호"));
  }

  get loginSubmitButton() {
    return this.page.getByRole("button", { name: /로그인/i });
  }

  get nicknameInput() {
    return this.page.locator("#nickname").or(this.page.getByLabel("닉네임"));
  }

  get signupSubmitButton() {
    return this.page.getByRole("button", { name: /회원가입/i });
  }

  /**
   * 로그인 수행
   * @param {string} email - 이메일
   * @param {string} password - 비밀번호
   */
  async login(email, password) {
    await this.goto("/login");
    await this.page.waitForLoadState("networkidle");
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginSubmitButton.click();
    await this.page.waitForURL("**/", { timeout: 10000 });
  }

  /**
   * 회원가입 수행
   * @param {string} email - 이메일
   * @param {string} password - 비밀번호
   * @param {string} nickname - 닉네임
   */
  async signup(email, password, nickname) {
    await this.goto("/signup");
    await this.page.waitForLoadState("networkidle");
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.nicknameInput.fill(nickname);
    await this.signupSubmitButton.click();
    await this.page.waitForURL("**/login", { timeout: 10000 });
  }

  /**
   * 로그아웃 수행
   */
  async logout() {
    const userDropdown = this.page.locator("#userDropdown");
    if (await userDropdown.isVisible({ timeout: 2000 })) {
      await userDropdown.click();
      await this.page.waitForTimeout(500);
    }

    const logoutButton = this.page
      .locator('form[action*="/logout"] button[type="submit"]')
      .first();
    await logoutButton.click();

    await this.page.waitForURL("**/", { timeout: 10000 });
  }

  /**
   * 에러 메시지 확인
   * @param {string} expectedMessage - 예상 에러 메시지
   */
  async expectErrorMessage(expectedMessage) {
    await this.page.getByText(expectedMessage).waitFor({ state: "visible" });
  }

  /**
   * 성공 메시지 확인
   * @param {string} expectedMessage - 예상 성공 메시지
   */
  async expectSuccessMessage(expectedMessage) {
    await this.page.getByText(expectedMessage).waitFor({ state: "visible" });
  }
}
