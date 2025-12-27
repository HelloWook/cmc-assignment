describe("Comments E2E Tests", () => {
  const testEmail = `test${Date.now()}@example.com`;
  const testPassword = "test1234";
  const testNickname = "테스트유저";
  const testTitle = `테스트 게시글 ${Date.now()}`;
  const testContent = "이것은 테스트 게시글 내용입니다.";
  const testComment = "테스트 댓글입니다.";
  const testReply = "테스트 대댓글입니다.";

  beforeEach(() => {
    cy.signup(testEmail, testPassword, testNickname);
    cy.login(testEmail, testPassword);
    cy.createPost(testTitle, testContent);
  });

  it("should display comment section", () => {
    cy.contains("댓글");
    cy.contains("댓글 작성");
  });

  it("should create a comment", () => {
    cy.get(".comment-form-container")
      .first()
      .within(() => {
        cy.get('textarea[name="content"]').type(testComment);
        cy.get('button[type="submit"]').contains("댓글 작성").click();
      });

    cy.contains(testComment);
    cy.contains(testNickname);
  });

  it("should edit a comment", () => {
    cy.get(".comment-form-container")
      .first()
      .within(() => {
        cy.get('textarea[name="content"]').type(testComment);
        cy.get('button[type="submit"]').contains("댓글 작성").click();
      });

    const editComment = `${testComment} (수정됨)`;
    cy.contains("수정").first().click();

    cy.get("textarea").first().clear().type(editComment);
    cy.get('button[type="submit"]').first().click();

    cy.contains(editComment);
  });

  it("should delete a comment", () => {
    cy.get(".comment-form-container")
      .first()
      .within(() => {
        cy.get('textarea[name="content"]').type(testComment);
        cy.get('button[type="submit"]').contains("댓글 작성").click();
      });

    cy.contains(testComment);
    cy.contains("삭제").first().click();
    cy.on("window:confirm", () => true);

    cy.contains(testComment).should("not.exist");
  });

  it("should create a reply comment", () => {
    cy.get(".comment-form-container")
      .first()
      .within(() => {
        cy.get('textarea[name="content"]').type(testComment);
        cy.get('button[type="submit"]').contains("댓글 작성").click();
      });

    cy.contains("대댓글 작성").first().click();
    cy.get(".reply-form-container")
      .first()
      .within(() => {
        cy.get('textarea[name="content"]').type(testReply);
        cy.get('button[type="submit"]').contains("작성").click();
      });

    cy.contains(testReply);
    cy.contains(testNickname);
  });

  it("should not allow commenting without login", () => {
    cy.get('[data-bs-toggle="dropdown"]').first().click();
    cy.get('form[action*="/logout"]').submit();

    cy.visit("/");
    cy.contains(testTitle).click();

    cy.contains("로그인").should("exist");
    cy.get('textarea[name="content"]').should("not.exist");
  });
});
