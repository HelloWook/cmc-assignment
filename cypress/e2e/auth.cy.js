describe('Authentication E2E Tests', () => {
  const testEmail = `test${Date.now()}@example.com`
  const testPassword = 'test1234'
  const testNickname = '테스트유저'

  beforeEach(() => {
    cy.visit('/')
  })

  it('should display home page', () => {
    cy.contains('게시글 목록')
    cy.contains('게시판')
  })

  it('should navigate to signup page', () => {
    cy.contains('회원가입').click()
    cy.url().should('include', '/signup')
    cy.contains('회원가입')
  })

  it('should navigate to login page', () => {
    cy.contains('로그인').click()
    cy.url().should('include', '/login')
    cy.contains('로그인')
  })

  it('should sign up a new user', () => {
    cy.visit('/signup')
    cy.get('input[name="email"]').type(testEmail)
    cy.get('input[name="password"]').type(testPassword)
    cy.get('input[name="nickname"]').type(testNickname)
    cy.get('form').submit()
    
    cy.url().should('include', '/login')
    cy.contains('회원가입이 완료되었습니다')
  })

  it('should login with valid credentials', () => {
    cy.signup(testEmail, testPassword, testNickname)
    cy.login(testEmail, testPassword)
    
    cy.url().should('eq', Cypress.config().baseUrl + '/')
    cy.contains(testNickname)
  })

  it('should show error on invalid login', () => {
    cy.visit('/login')
    cy.get('input[name="email"]').type('invalid@example.com')
    cy.get('input[name="password"]').type('wrongpassword')
    cy.get('form').submit()
    
    cy.url().should('include', '/login')
    cy.contains('이메일 또는 비밀번호')
  })

  it('should logout successfully', () => {
    cy.signup(testEmail, testPassword, testNickname)
    cy.login(testEmail, testPassword)
    
    cy.contains(testNickname).click()
    cy.contains('로그아웃').click()
    
    cy.url().should('eq', Cypress.config().baseUrl + '/')
    cy.contains('로그인')
  })
})

