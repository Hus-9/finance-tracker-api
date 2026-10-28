package com.husnain.financetracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The base class every integration test extends.
// It starts the real app with a real PostgreSQL in Docker so the tests check everything working together.
// The database container starts once and every test class shares it, which keeps the tests quick.
// Every test registers its  own users with random emails so the tests never get in each other's way.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

public abstract class AbstractIntegrationTest {

    // A throwaway PostgreSQL 16 in Docker, @ServiceConnection points the app's database settings at it automatically.
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    // Starts the container before Spring starts so the app has a database to connect to.
    static {

        POSTGRES.start();

    }

    // MockMvc sends requests to the app without needing a real server running.
    @Autowired
    protected MockMvc mockMvc;

    // ObjectMapper turns Java objects into JSON and back, it's the same one the app uses.
    @Autowired
    protected ObjectMapper objectMapper;

    // Registers a brand new user with a random email, logs in and gives back the token ready for the Authorization header.
    protected String newUser() throws Exception {

        String body = json(Map.of("email", "user-" + UUID.randomUUID() + "@example.com", "password", "password123"));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(login).get("accessToken").asText();

    }

    // Small helpers so the tests can send requests as a user  in one line.
    protected ResultActions getAs(String token, String url) throws Exception {

        return mockMvc.perform(withToken(MockMvcRequestBuilders.get(url), token));

    }

    protected ResultActions postAs(String token, String url, Object body) throws Exception {

        return mockMvc.perform(withToken(MockMvcRequestBuilders.post(url), token)
                .contentType(MediaType.APPLICATION_JSON).content(json(body)));

    }

    protected ResultActions putAs(String token, String url, Object body) throws Exception {

        return mockMvc.perform(withToken(MockMvcRequestBuilders.put(url), token)
                .contentType(MediaType.APPLICATION_JSON).content(json(body)));

    }

    protected ResultActions deleteAs(String token, String url) throws Exception {

        return mockMvc.perform(withToken(MockMvcRequestBuilders.delete(url), token));

    }

    private MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {

        return request.header("Authorization", token);

    }

    protected String json(Object value) throws Exception {

        return objectMapper.writeValueAsString(value);

    }

    // Reads the JSON that came back from a request.
    protected JsonNode body(ResultActions result) throws Exception {

        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());

    }

    // Finds the id of one of the user's categories by its name like one of the defaults made at sign up.
    protected long categoryId(String token, String name) throws Exception {

        for (JsonNode category : body(getAs(token, "/api/categories"))) {

            if (category.get("name").asText().equals(name)) {

                return category.get("id").asLong();

            }
        }

        throw new AssertionError("No category called " + name);

    }

    protected long createAccount(String token) throws Exception {

        return body(postAs(token, "/api/accounts", Map.of("name", "Monzo", "type", "CURRENT", "currency", "GBP"))
                .andExpect(status().isCreated())).get("id").asLong();

    }

    protected long createTransaction(String token, long accountId, long categoryId, String amount, String type, String date) throws Exception {

        Map<String, Object> request = Map.of("accountId", accountId, "categoryId", categoryId, "amount", new BigDecimal(amount), "type", type, "date", date);
        return body(postAs(token, "/api/transactions", request)
                .andExpect(status().isCreated())).get("id").asLong();

    }
}
