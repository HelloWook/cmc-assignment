import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2E 테스트 설정
 * 
 * 좋은 E2E 테스트의 5가지 조건을 반영:
 * 1. 신뢰성: Auto-waiting 활용 (기본 제공)
 * 2. 유지보수성: POM 패턴 적용
 * 3. 비즈니스 중심: data-testid 사용
 * 4. 격리: 각 테스트 독립 실행
 * 5. CI 연동: CI 환경 지원
 */
export default defineConfig({
  // 테스트 파일 위치
  testDir: './tests/e2e',
  
  // 테스트 실행 설정
  fullyParallel: true, // 테스트 병렬 실행
  forbidOnly: !!process.env.CI, // CI 환경에서만 .only 허용
  retries: process.env.CI ? 2 : 0, // CI에서 실패 시 재시도
  workers: process.env.CI ? 1 : undefined, // CI에서는 순차 실행
  
  // 리포트 설정
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list'],
    ...(process.env.CI ? [['github']] : [])
  ],
  
  // 공유 설정
  use: {
    baseURL: 'http://localhost:8081',
    trace: 'on-first-retry', // 실패 시 트레이스 수집
    screenshot: 'only-on-failure', // 실패 시 스크린샷
    video: 'retain-on-failure', // 실패 시 비디오 저장
    actionTimeout: 10000, // 액션 타임아웃 (10초)
    navigationTimeout: 30000, // 네비게이션 타임아웃 (30초)
  },

  // 프로젝트별 브라우저 설정
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // 필요시 다른 브라우저 추가
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],

  // 웹 서버 설정 (선택사항 - Docker 사용 시 불필요)
  // webServer: {
  //   command: 'npm run start',
  //   url: 'http://localhost:8081',
  //   reuseExistingServer: !process.env.CI,
  // },
});

