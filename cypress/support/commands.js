// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

Cypress.Commands.add("login", (email, password) => {
  cy.visit("/login");
  cy.get('input[name="email"]').type(email);
  cy.get('input[name="password"]').type(password);
  cy.get("form").submit();
  cy.url().should("not.include", "/login");
});

Cypress.Commands.add("logout", () => {
  cy.get('[data-bs-toggle="dropdown"]')
    .contains(Cypress.env("currentUser") || "사용자")
    .click();
  cy.get('form[action*="/logout"]').submit();
});

Cypress.Commands.add("signup", (email, password, nickname) => {
  cy.visit("/signup");
  cy.get('input[name="email"]').type(email);
  cy.get('input[name="password"]').type(password);
  cy.get('input[name="nickname"]').type(nickname);
  cy.get("form").submit();
});

Cypress.Commands.add("createPost", (title, content, categoryIds = []) => {
  cy.visit("/posts/new");
  cy.get('input[name="title"]').type(title);
  cy.get('textarea[name="content"]').type(content);

  if (categoryIds && categoryIds.length > 0) {
    categoryIds.forEach((categoryId) => {
      cy.get(`input[type="checkbox"][value="${categoryId}"]`).check();
    });
  }

  cy.get("form").contains("작성하기").click();
  cy.url().should("include", "/posts/");
});

Cypress.Commands.add("createCategory", (name) => {
  cy.visit("/categories");
  cy.get("#categoryName").type(name);
  cy.get('button[type="submit"]').contains("추가하기").click();
});
