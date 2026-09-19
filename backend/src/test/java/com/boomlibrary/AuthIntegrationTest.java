package com.boomlibrary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void register_validData_returns201WithToken() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "user1@test.com",
                                "username", "user1",
                                "password", "password123"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "dup@test.com", "username", "dupuser", "password", "password123"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "dup@test.com", "username", "dupuser2", "password", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        String email = "logintest@test.com";
        String password = "password123";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", email, "username", "logintest", "password", password))))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", "wrongpass@test.com", "username", "wrongpassuser", "password", "correctpass"))))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "wrongpass@test.com", "password", "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getBooks_withoutToken_returns403() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isForbidden());
    }

    @Test
    void getBooks_withValidToken_returns200EmptyList() throws Exception {
        String token = registerAndGetToken("booklist@test.com", "booklistuser", "password123");

        mvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createBook_withValidToken_returns201() throws Exception {
        String token = registerAndGetToken("createbook@test.com", "createbookuser", "password123");

        mvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "bookName", "Clean Code",
                                "author", "Robert C. Martin",
                                "read", false
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookName").value("Clean Code"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void getAdminUsers_withUserToken_returns403() throws Exception {
        String token = registerAndGetToken("nonadmin@test.com", "nonadminuser", "password123");

        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // Helper: register a user and return the JWT token
    private String registerAndGetToken(String email, String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "username", username, "password", password))))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
