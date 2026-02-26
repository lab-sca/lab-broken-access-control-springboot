package org.fugerit.java.demo.lab.broken.access.control;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DocControllerSecurityTest - Test di sicurezza per il controller DocController.
 *
 * Conversione completa da Quarkus a Spring Boot:
 *
 * CONVERSIONI CHIAVE:
 *
 * Test Setup:
 * - @QuarkusTest                        → @SpringBootTest + @AutoConfigureMockMvc
 * - RestAssured given().when().get()    → mockMvc.perform(get())
 * - io.restassured.RestAssured          → org.springframework.test.web.servlet.MockMvc
 *
 * Security Testing:
 * - @TestSecurity(user="USER1", roles={"user"})  → @WithMockUser(username="USER1", authorities={"user"})
 * - given().header("Authorization", "Bearer " + token) → mockMvc.perform(get().header("Authorization", "Bearer " + token))
 *
 * Assertions:
 * - .then().statusCode(200)             → .andExpect(status().isOk())
 * - .then().statusCode(401)             → .andExpect(status().isUnauthorized())
 * - .then().statusCode(403)             → .andExpect(status().isForbidden())
 * - .extract().body().asString()        → .andReturn().getResponse().getContentAsString()
 *
 * Response Status:
 * - Response.Status.OK                  → HttpStatus.OK (200)
 * - Response.Status.CREATED             → HttpStatus.CREATED (201)
 * - Response.Status.UNAUTHORIZED        → HttpStatus.UNAUTHORIZED (401)
 * - Response.Status.FORBIDDEN           → HttpStatus.FORBIDDEN (403)
 *
 * JWT Generation:
 * - DemoJwtGeneratorRest.generateAdminToken()  → demoJwtController.generateAdminToken()
 *   (metodi statici in Quarkus → metodi di istanza in Spring Boot)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class DocControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoJwtGeneratorController demoJwtController;

    private static final String ID_NON_ESISTE = "955b6a27-3da5-421f-a380-a86944e0c769";

    private static final String ID_MARGHERITA_HACK = "46005e2d-4faa-4c5a-8ed2-6876d63622a7";

    private static final String ID_ALAN_TURING = "62472b90-14a5-45b5-891e-14f9e5659680";

    private static final String ID_RICHARD_FEYMAN = "3ad86124-765a-4104-a2dd-e99335ff1260";


    private static final String EXPIRED_JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL3VuaXR0ZXN0ZGVtb2FwcC5mdWdlcml0Lm9yZyIsInVwbiI6IkRFTU9VU0VSIiwiZ3JvdXBzIjpbImFkbWluIiwiZ3Vlc3QiLCJ1c2VyIl0sInN1YiI6IkRFTU9VU0VSIiwiaWF0IjoxNzcxMjQ2NzE3LCJleHAiOjE3NzEyNTAzMTcsImp0aSI6Ijc1MDA3YjBlLTBmYzktNDdkMS05OTY2LTEyMmIxODNkMDZlMyJ9.FblIqZcvhCpgJzlgulOBH0nWXkYwLJv9IpCuTAArvwTTZN2sAsFiGV7bH9tnalbINmgrVfSMAWoSVG1o4WtMY5Tg_ZtIGr1JJQY5zpH584CBWZIqDo9NJkVmTB1H1aK-ZiENGjghbdpVyxdy-JwS6YRdqfRtNWAG4jlzzXuEtsWKqCTeUt9cp1PVVOFyKVqOwG0tbPcjuEimCP3Z47XmFhe2TVll78BDY7AuRN-sWLRXAoSmOuTUY5I59Zqu_5PzqA_l2xDc8NtOlQDJXhFX1L1_WNYQMbNes8P4oS8_KDs_r5A_yxpjA8wPunfCOkJIsQ6QcWuvO7TB6pYfs_PeoxpSm2wMvKW2sRsmNqSHQ2oVKbLXp1Z4r2Wny0-CqkG7dTtBBhX9GRY79x67V9aoX_yH_gu2J0ujN6uPsrESSLDuBlOPpWGSn_OTES8fGhkLqalWmLAMQfE-oCphzmJ-4ktYwmpvOz4zczDBsbFZdGf6ARH3ahrvCbeiTM2SG_b4WBZBiNJ7kOSBoScRhIXaZT0ElfI6YhjyPn85P1qlVyxgzbmSKQWvYCVmZehGXHIA1Up4R9O39o7nsMhQjku3PMlTwfyQJ_x5OxeRs0ktmfrfm8Pzn0fMW3SMLUcDJPrmMT52mr5mscDBJVv0VBH_51o_bXTOFSjeVIvFXIk44mQ";

    // =========================================================================
    // TEST SUL PATH /doc/example.* (GENERAZIONE DOCUMENTI)
    // =========================================================================

    @Test
    @DisplayName("(200) generazione documento, formato HTML con utente autorizzato, ruolo utente 'user'")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"user"})
    void testHtmlOkNoAdminRole() throws Exception {
        mockMvc.perform(get("/doc/example.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("(200) generazione documento, formato PDF con utente autorizzato, ruolo utente 'admin', 'user' e 'guest'")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER2", authorities = {"guest", "user", "admin"})
    void testPdfOkNoAdminRole() throws Exception {
        mockMvc.perform(get("/doc/example.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    // VULNERABILITY: (5) risolvi questa vulnerabilità in modo che il caso di test funzioni.
    @Test
    @DisplayName("VULNERABILITY: (5) - (401) generazione documento, formato MarkDown con utente non valido, nessun ruolo associato.")
    @Tag("security")
    @Tag("unauthorized")
    @Tag("WithMockUser")
    void testMarkdown401NoAuthorizationBearer() throws Exception {
        mockMvc.perform(get("/doc/example.md"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("(403) generazione documento, formato PDF con utente senza ruolo necessario, ruolo utente 'user'.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"user"})
    void testMarkdown403NoAdminRole() throws Exception {
        mockMvc.perform(get("/doc/example.pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("(200) generazione documento, formato PDF con utente con i ruoli necessari (tramite bearer token), ruolo utente 'admin', 'user' e 'guest'.")
    @Tag("security")
    @Tag("success")
    @Tag("Bearer")
    void testOkWithJwt() throws Exception {
        String token = demoJwtController.generateAdminToken();
        mockMvc.perform(get("/doc/example.pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    @DisplayName("(200) generazione documento, formato MarkDown con utente che ha i ruoli per vedere tutti i risultati, ruolo utente 'admin', 'user' e 'guest'.")
    @Tag("security")
    @Tag("success")
    @Tag("Bearer")
    void testOkMarkDownConVerificaContenutoAdmin() throws Exception {
        String token = demoJwtController.generateAdminToken();
        String responseBody = mockMvc.perform(get("/doc/example.md")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        log.info("testOkMarkDownConVerificaContenutoAdmin : {}", responseBody);
        // il ruolo 'admin' ha accesso alla persona 'Richard Feynman'
        Assertions.assertTrue(responseBody.contains("Feynman"));
    }

    // VULNERABILITY: (2) risolvi questa vulnerabilità in modo che il caso di test funzioni.
    @Test
    @DisplayName("VULNERABILITY: (2) A - (200) generazione documento, formato MarkDown con utente che NON ha i ruoli per vedere tutti i risultati, ruolo utente 'user' e 'guest'.")
    @Tag("security")
    @Tag("success")
    @Tag("Bearer")
    void testOkMarkDownConVerificaContenutoUser() throws Exception {
        String token = demoJwtController.generateUserToken();
        String responseBody = mockMvc.perform(get("/doc/example.md")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        log.info("testOkMarkDownConVerificaContenutoUser : {}", responseBody);
        // il ruolo 'user' NON ha accesso alla persona 'Richard Feynman'
        Assertions.assertFalse(responseBody.contains("Feynman"));
    }

    @Test
    @DisplayName("(403) generazione documento, formato PDF con utente senza ruolo necessario (JWT), ruolo utente 'guest'.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("Bearer")
    void testForbiddenWithJwt() throws Exception {
        String token = demoJwtController.generateGuestToken();
        mockMvc.perform(get("/doc/example.pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("(401) generazione documento, formato PDF con utente senza autenticazione.")
    @Tag("security")
    @Tag("unauthorized")
    @Tag("Bearer")
    void testUnauthorizedWithoutJwt() throws Exception {
        mockMvc.perform(get("/doc/example.pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("(401) generazione documento, formato PDF con utente con JWT non valido.")
    @Tag("security")
    @Tag("unauthorized")
    @Tag("Bearer")
    void testUnauthorizedWithWrongJwt() throws Exception {
        mockMvc.perform(get("/doc/example.pdf")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJVU0VSMSIsIm5hbWUi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("(401) generazione documento, formato PDF con utente con JWT scaduto.")
    @Tag("security")
    @Tag("unauthorized")
    @Tag("Bearer")
    void testExpiredJWT() throws Exception {
        mockMvc.perform(get("/doc/example.pdf")
                        .header("Authorization", "Bearer " + EXPIRED_JWT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("(200) generazione documento, formato MarkDown con utente con ruolo necessario (JWT), ruolo utente 'guest'.")
    @Tag("security")
    @Tag("authorized")
    @Tag("Bearer")
    void testOkJwtMarkDown() throws Exception {
        String token = demoJwtController.generateGuestToken();
        mockMvc.perform(get("/doc/example.md")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("(200) generazione documento, formato AsciiDoc con utente con ruolo necessario (JWT), ruolo utente 'admin', 'user' e 'guest'.")
    @Tag("security")
    @Tag("authorized")
    @Tag("Bearer")
    void testOkJwtAsciiDoc() throws Exception {
        String token = demoJwtController.generateAdminToken();
        mockMvc.perform(get("/doc/example.adoc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("(403) generazione documento, formato AsciiDoc con utente senza ruolo necessario (JWT), ruolo utente 'guest'.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("Bearer")
    void testForbiddenJwtAsciiDoc() throws Exception {
        String token = demoJwtController.generateToken("DEMO", "guest");
        mockMvc.perform(get("/doc/example.pdf")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // TEST SUL PATH /doc/person/* (INTERROGA / INSERISCI / CANCELLA PERSONE)
    // =========================================================================

    @Test
    @DisplayName("(200) trova persona con ruolo autorizzato, ruolo utente 'admin', 'user' e 'guest'.")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER2", authorities = {"guest", "user", "admin"})
    void testFindPersonOkAdmin() throws Exception {
        String responseBody = mockMvc.perform(get("/doc/person/find/{uuid}", ID_RICHARD_FEYMAN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseBody.contains("Feynman"));
        log.info("testFindPersonOkAdmin : {}", responseBody);
    }

    @Test
    @DisplayName("(200) trova persona con ruolo autorizzato, ruolo utente 'user' e 'guest'.")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"guest", "user"})
    void testFindPersonOkUser() throws Exception {
        String responseBody = mockMvc.perform(get("/doc/person/find/{id}", ID_MARGHERITA_HACK))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(responseBody.contains("Hack"));
        log.info("responseBody : {}", responseBody);
    }

    // VULNERABILITY: (4) risolvi questa vulnerabilità in modo che il caso di test funzioni.
    @Test
    @DisplayName("VULNERABILITY: (4) - (403) trova persona con ruolo NON autorizzato, ruolo utente 'user' e 'guest'.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"guest", "user"})
    void testFindPersonKoForbidden() throws Exception {
        mockMvc.perform(get("/doc/person/find/{uuid}", ID_RICHARD_FEYMAN))
                .andExpect(status().isForbidden());
    }

    // VULNERABILITY: (1) risolvi questa vulnerabilità in modo che il caso di test funzioni.
    @Test
    @DisplayName("VULNERABILITY: (1) - (403) Un utente che non esiste, restituisce un forbidden per evitare object enumeration.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"guest", "user"})
    void testFindPersonKoNotFound() throws Exception {
        mockMvc.perform(get("/doc/person/find/{uuid}", ID_NON_ESISTE))
                .andExpect(status().isForbidden());
    }

    // VULNERABILITY: (2) risolvi questa vulnerabilità in modo che il caso di test funzioni.
    @Test
    @DisplayName("VULNERABILITY: (2) B - (200) Lista persona con ruolo 'user', non trova utenti per cui serve 'admin'.")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"guest", "user"})
    void testListPersonsResultKo() throws Exception {
        String responseBody = mockMvc.perform(get("/doc/person/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        log.info("responseBody testListPersonsResultKo : {}", responseBody);
        Assertions.assertFalse(responseBody.contains("Feynman"));
    }

    @Test
    @DisplayName("(200) Lista persona con ruolo 'admin', trova utenti per cui serve 'admin'.")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER2", authorities = {"admin", "user"})
    void testListPersonsResultOk() throws Exception {
        String responseBody = mockMvc.perform(get("/doc/person/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        log.info("responseBody testListPersonsResultOk : {}", responseBody);
        Assertions.assertTrue(responseBody.contains("Feynman"));
    }

    @Test
    @DisplayName("(201) Utente 'admin' inserisce una nuova persona.")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER2", authorities = {"admin", "user", "guest"})
    void testAddPersonAdminOk() throws Exception {
        String addMarieCurie = "{\"firstName\": \"MARIE\",\"lastName\": \"CURIE\",\"title\": \"Fisica\",\"minRole\": \"guest\"}";
        mockMvc.perform(post("/doc/person/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addMarieCurie))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.creationDate").exists());
    }

    @Test
    @DisplayName("(200) Utente 'admin' cancella una persona dopo averla inserita.")
    @Tag("security")
    @Tag("authorized")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER2", authorities = {"admin", "user"})
    void testAddDeletePersonAdminOk() throws Exception {
        // Inserisco un utente
        String addPierreCurie = "{\"firstName\": \"PIERRE\",\"lastName\": \"CURIE\",\"title\": \"Fisico\",\"minRole\": \"guest\"}";
        String responseBody = mockMvc.perform(post("/doc/person/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPierreCurie))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Estraggo l'ID dalla risposta JSON
        String uuid = com.jayway.jsonpath.JsonPath.read(responseBody, "$.uuid");
        log.info("testAddDeletePersonAdminOk added pierre curie uuid : {}", uuid);

        // Cancello l'utente
        mockMvc.perform(delete("/doc/person/delete/{uuid}", uuid))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("(403) Utente 'admin' impedita cancellazione di utente che non esiste con un forbidden.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER2", authorities = {"admin", "user"})
    void testDeletePersonAdminKoNonEsiste() throws Exception {
        mockMvc.perform(delete("/doc/person/delete/{id}", ID_NON_ESISTE))
                .andExpect(status().isForbidden());
    }

    // VULNERABILITY: (3) risolvi questa vulnerabilità in modo che il caso di test funzioni.
    @Test
    @DisplayName("VULNERABILITY: (3) - (403) Utente 'user' impedita cancellazione di un utente.")
    @Tag("security")
    @Tag("forbidden")
    @Tag("WithMockUser")
    @WithMockUser(username = "USER1", authorities = {"user"})
    void testDeletePersonUserKo() throws Exception {
        mockMvc.perform(delete("/doc/person/delete/{id}", ID_ALAN_TURING))
                .andExpect(status().isForbidden());
    }

}