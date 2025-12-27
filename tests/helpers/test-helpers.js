/**
 * 테스트 헬퍼 함수들
 * 
 * 테스트 간 격리를 보장하고 공통 기능을 제공합니다.
 */

/**
 * 고유한 테스트 이메일 생성
 */
export function generateTestEmail() {
  return `test${Date.now()}${Math.random().toString(36).substring(7)}@example.com`;
}

/**
 * 고유한 테스트 닉네임 생성
 */
export function generateTestNickname() {
  return `테스트유저${Date.now()}`;
}

/**
 * 고유한 테스트 제목 생성
 */
export function generateTestTitle() {
  return `테스트 게시글 ${Date.now()}`;
}

/**
 * 고유한 카테고리 이름 생성
 */
export function generateCategoryName() {
  return `테스트카테고리${Date.now()}`;
}

/**
 * 테스트 사용자 정보 생성
 */
export function createTestUser() {
  return {
    email: generateTestEmail(),
    password: 'test1234',
    nickname: generateTestNickname(),
  };
}

/**
 * 페이지 로딩 대기 (네트워크 유휴 상태)
 */
export async function waitForPageLoad(page) {
  await page.waitForLoadState('networkidle');
}

/**
 * 알림 메시지 확인 및 대기
 */
export async function waitForNotification(page, message) {
  await page.getByText(message).waitFor({ state: 'visible', timeout: 5000 });
}

