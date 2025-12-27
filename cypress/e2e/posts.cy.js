describe('Posts E2E Tests', () => {
  const testEmail = `test${Date.now()}@example.com`
  const testPassword = 'test1234'
  const testNickname = '테스트유저'
  const testTitle = `테스트 게시글 ${Date.now()}`
  const testContent = '이것은 테스트 게시글 내용입니다.'

  beforeEach(() => {
    cy.signup(testEmail, testPassword, testNickname)
    cy.login(testEmail, testPassword)
  })

  it('should display posts list', () => {
    cy.visit('/')
    cy.contains('게시글 목록')
    cy.contains('글쓰기')
  })

  it('should navigate to post creation page', () => {
    cy.visit('/')
    cy.contains('글쓰기').click()
    cy.url().should('include', '/posts/new')
    cy.contains('게시글 작성')
  })

  it('should create a new post', () => {
    cy.visit('/posts/new')
    cy.get('input[name="title"]').type(testTitle)
    cy.get('textarea[name="content"]').type(testContent)
    cy.get('form').contains('작성하기').click()
    
    cy.url().should('include', '/posts/')
    cy.contains(testTitle)
    cy.contains(testContent)
    cy.contains(testNickname)
  })

  it('should display post detail', () => {
    cy.createPost(testTitle, testContent)
    
    cy.contains(testTitle)
    cy.contains(testContent)
    cy.contains(testNickname)
    cy.contains('댓글')
  })

  it('should edit a post', () => {
    cy.createPost(testTitle, testContent)
    
    const editTitle = `${testTitle} (수정됨)`
    const editContent = `${testContent} 수정된 내용입니다.`
    
    cy.contains('수정').click()
    cy.url().should('include', '/edit')
    
    cy.get('input[name="title"]').clear().type(editTitle)
    cy.get('textarea[name="content"]').clear().type(editContent)
    cy.get('form').contains('수정하기').click()
    
    cy.url().should('include', '/posts/')
    cy.contains(editTitle)
    cy.contains(editContent)
  })

  it('should delete a post', () => {
    cy.createPost(testTitle, testContent)
    
    cy.contains('삭제').click()
    cy.on('window:confirm', () => true)
    
    cy.wait(2000)
    cy.url().should('eq', Cypress.config().baseUrl + '/')
    cy.contains(testTitle).should('not.exist')
  })

  it('should not allow editing other users posts', () => {
    cy.createPost(testTitle, testContent)
    
    cy.get('[data-bs-toggle="dropdown"]').first().click()
    cy.get('form[action*="/logout"]').submit()
    
    const otherEmail = `other${Date.now()}@example.com`
    cy.signup(otherEmail, testPassword, '다른유저')
    cy.login(otherEmail, testPassword)
    
    cy.visit('/')
    cy.contains(testTitle).click()
    
    cy.contains('수정').should('not.exist')
    cy.contains('삭제').should('not.exist')
  })
})

