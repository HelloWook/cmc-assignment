/**
 * BasePage - 모든 페이지 객체의 기본 클래스
 * 
 * 공통 기능을 제공하여 코드 중복을 줄이고 유지보수성을 향상시킵니다.
 */
export class BasePage {
  constructor(page) {
    this.page = page;
  }

  /**
   * 페이지로 이동
   * @param {string} path - 이동할 경로
   */
  async goto(path = '/') {
    await this.page.goto(path);
  }

  /**
   * 현재 URL 확인
   * @param {string} expectedUrl - 예상 URL
   */
  async expectUrl(expectedUrl) {
    await this.page.waitForURL(expectedUrl);
  }

  /**
   * 텍스트가 표시되는지 확인
   * @param {string} text - 확인할 텍스트
   */
  async expectText(text) {
    await this.page.getByText(text).first().waitFor({ state: 'visible' });
  }

  /**
   * data-testid로 요소 찾기
   * @param {string} testId - data-testid 값
   */
  getByTestId(testId) {
    return this.page.getByTestId(testId);
  }

  /**
   * 로딩 완료 대기 (네트워크 유휴 상태)
   */
  async waitForLoad() {
    await this.page.waitForLoadState('networkidle');
  }
}

