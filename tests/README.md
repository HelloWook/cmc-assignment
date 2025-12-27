# E2E 테스트 가이드

이 프로젝트는 **Playwright**를 사용하여 E2E(End-to-End) 테스트를 구현합니다.

## 좋은 E2E 테스트의 5가지 조건

이 테스트 스위트는 다음 5가지 원칙을 준수합니다:

### ① 신뢰성 (No Flakiness)
- Playwright의 **Auto-waiting** 기능 활용
- 하드 코딩된 `sleep()` 사용 지양
- 네트워크 지연을 고려한 타임아웃 설정

### ② 유지보수성 (POM 패턴)
- **Page Object Model (POM)** 패턴 적용
- 페이지 요소와 동작을 객체화하여 캡슐화
- UI 변경 시 해당 페이지 객체만 수정하면 됨

### ③ 비즈니스 중심 시나리오
- "버튼 클릭"보다는 "사용자가 상품을 장바구니에 담는다" 같은 시나리오 중심
- `data-testid` 같은 전용 셀렉터 사용 권장 (구현 상세와 테스트 분리)

### ④ 격리된 테스트
- 각 테스트가 독립적으로 실행
- `beforeEach`에서 테스트 데이터 생성
- 이전 테스트의 데이터가 다음 테스트에 영향 주지 않음

### ⑤ CI 연동
- CI 환경에서 자동 실행 가능
- 실패 시 스크린샷, 비디오, 트레이스 자동 수집
- GitHub Actions 등과 연동 가능

## 프로젝트 구조

```
tests/
├── e2e/              # E2E 테스트 파일
│   ├── auth.spec.js      # 인증 테스트
│   ├── posts.spec.js     # 게시글 테스트
│   ├── comments.spec.js  # 댓글 테스트
│   └── categories.spec.js # 카테고리 테스트
├── pages/            # Page Object Model (POM)
│   ├── BasePage.js       # 기본 페이지 클래스
│   ├── HomePage.js       # 홈 페이지
│   ├── AuthPage.js       # 인증 페이지
│   ├── PostPage.js       # 게시글 페이지
│   ├── CommentPage.js    # 댓글 페이지
│   └── CategoryPage.js   # 카테고리 페이지
└── helpers/          # 테스트 헬퍼 함수
    └── test-helpers.js   # 공통 유틸리티
```

## 설치 및 실행

### 1. 의존성 설치

```bash
npm install
```

### 2. Playwright 브라우저 설치

```bash
npx playwright install
```

### 3. 애플리케이션 실행

Docker를 사용하여 애플리케이션을 실행합니다:

```bash
docker-compose up -d
```

애플리케이션이 `http://localhost:8081`에서 실행 중이어야 합니다.

### 4. 테스트 실행

#### 모든 테스트 실행 (헤드리스 모드)
```bash
npm run test:e2e
```

#### UI 모드로 실행 (대화형)
```bash
npm run test:e2e:ui
```

#### 헤드 모드로 실행 (브라우저 표시)
```bash
npm run test:e2e:headed
```

#### 디버그 모드로 실행
```bash
npm run test:e2e:debug
```

#### 테스트 리포트 보기
```bash
npm run test:e2e:report
```

## 테스트 작성 가이드

### Page Object Model (POM) 패턴

각 페이지는 별도의 클래스로 분리되어 있습니다:

```javascript
// tests/pages/HomePage.js
export class HomePage extends BasePage {
  get writeButton() {
    return this.page.getByRole('link', { name: '글쓰기' });
  }
  
  async clickPostByTitle(title) {
    await this.page.getByRole('link', { name: title }).click();
  }
}
```

### 테스트 작성 예시

```javascript
import { test, expect } from '@playwright/test';
import { HomePage } from '../pages/HomePage.js';
import { AuthPage } from '../pages/AuthPage.js';

test.describe('게시글 테스트', () => {
  test.beforeEach(async ({ page }) => {
    // 각 테스트 전에 초기화
    const authPage = new AuthPage(page);
    await authPage.login('test@example.com', 'password');
  });

  test('게시글을 생성할 수 있어야 함', async ({ page }) => {
    const homePage = new HomePage(page);
    const postPage = new PostPage(page);
    
    await homePage.goto();
    await homePage.writeButton.click();
    await postPage.createPost('제목', '내용');
    
    await postPage.expectPostContent('제목', '내용');
  });
});
```

## CI/CD 연동

### GitHub Actions 예시

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: npm install
      - run: npx playwright install --with-deps
      - run: npm run test:e2e
      - uses: actions/upload-artifact@v3
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
```

## 주의사항

1. **테스트 격리**: 각 테스트는 독립적으로 실행되어야 합니다.
2. **데이터 정리**: 테스트 후 생성된 데이터는 자동으로 정리됩니다 (각 테스트마다 새로운 사용자 생성).
3. **타임아웃**: 네트워크 지연을 고려하여 적절한 타임아웃을 설정했습니다.
4. **셀렉터**: 가능하면 `data-testid`를 사용하거나, 의미 있는 텍스트/역할 기반 셀렉터를 사용합니다.

## 문제 해결

### 테스트가 실패하는 경우

1. **애플리케이션이 실행 중인지 확인**
   ```bash
   curl http://localhost:8081
   ```

2. **브라우저가 설치되어 있는지 확인**
   ```bash
   npx playwright install
   ```

3. **테스트 리포트 확인**
   ```bash
   npm run test:e2e:report
   ```

### Flaky 테스트

테스트가 가끔 실패하는 경우:
- 타임아웃을 늘려보세요
- `waitForLoadState('networkidle')` 사용
- 더 안정적인 셀렉터 사용

## 참고 자료

- [Playwright 공식 문서](https://playwright.dev/)
- [Page Object Model 패턴](https://playwright.dev/docs/pom)
- [Best Practices](https://playwright.dev/docs/best-practices)

