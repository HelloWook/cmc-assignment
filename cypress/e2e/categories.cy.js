describe("Categories E2E Tests", () => {
  const testEmail = `test${Date.now()}@example.com`;
  const testPassword = "test1234";
  const testNickname = "테스트유저";
  const categoryName = `테스트카테고리${Date.now()}`;

  beforeEach(() => {
    cy.signup(testEmail, testPassword, testNickname);
    cy.login(testEmail, testPassword);
  });

  it("should navigate to categories page", () => {
    cy.visit("/");
    cy.contains("카테고리 관리").click();
    cy.url().should("include", "/categories");
    cy.contains("카테고리 관리");
  });

  it("should create a category", () => {
    cy.visit("/categories");
    cy.get("#categoryName").type(categoryName);
    cy.get('button[type="submit"]').contains("추가하기").click();

    cy.contains(categoryName);
    cy.contains("카테고리가 생성되었습니다");
  });

  it("should edit a category", () => {
    cy.createCategory(categoryName);

    const editName = `${categoryName} (수정됨)`;
    cy.contains(categoryName)
      .parent()
      .parent()
      .parent()
      .contains("수정")
      .click();
    cy.get(".edit-form")
      .first()
      .within(() => {
        cy.get('input[type="text"]').clear().type(editName);
        cy.get('button[type="submit"]').contains("저장").click();
      });

    cy.contains(editName);
    cy.contains("카테고리가 수정되었습니다");
  });

  it("should delete a category", () => {
    cy.createCategory(categoryName);

    cy.contains(categoryName);
    cy.contains(categoryName)
      .parent()
      .parent()
      .parent()
      .within(() => {
        cy.contains("삭제").click();
      });
    cy.on("window:confirm", () => true);

    cy.wait(2000);
    cy.contains(categoryName).should("not.exist");
    cy.contains("카테고리가 삭제되었습니다");
  });

  it("should filter posts by category", () => {
    cy.createCategory(categoryName);

    const testTitle = `테스트 게시글 ${Date.now()}`;
    const testContent = "테스트 내용";

    cy.visit("/posts/new");
    cy.get('input[name="title"]').type(testTitle);
    cy.get('textarea[name="content"]').type(testContent);

    cy.get('input[type="checkbox"]').first().check();
    cy.get("form").contains("작성하기").click();

    cy.wait(1000);
    cy.visit("/");
    cy.wait(1000);
    cy.contains(categoryName).parent().click();

    cy.url().should("include", "categoryId=");
    cy.contains(testTitle);
  });

  it("should display empty state when no categories", () => {
    cy.visit("/categories");

    cy.get(".empty-state", { timeout: 10000 }).should("exist");
    cy.contains("등록된 카테고리가 없습니다").should("exist");
  });
});
