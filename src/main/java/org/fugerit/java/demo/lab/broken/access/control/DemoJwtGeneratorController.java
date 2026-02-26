package org.fugerit.java.demo.lab.broken.access.control;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.fugerit.java.demo.lab.broken.access.control.security.EnumRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

/**
 * DemoJwtGeneratorController - REST controller per la generazione di JWT demo.
 *
 * Conversione da Quarkus SmallRye JWT a Spring Boot OAuth2 JwtEncoder.
 *
 * CONVERSIONI CHIAVE:
 *
 * Annotation Level:
 * - @ApplicationScoped            → @RestController
 * - @Path("/demo")                → @RequestMapping("/demo")
 * - @GET                          → @GetMapping
 * - @Path("/{roles}.txt")         → value = "/{roles}.txt"
 * - @PathParam("roles")           → @PathVariable String roles
 * - @Produces("text/plain")       → produces = MediaType.TEXT_PLAIN_VALUE
 * - @APIResponse (MicroProfile)   → @ApiResponse (SpringDoc)
 *
 * JWT Generation:
 * - Jwt.issuer(ISSUER)            → JwtClaimsSet.builder().issuer(ISSUER)
 *   .upn(username)                  .claim("upn", username)
 *   .groups(roles)                  .claim("groups", Arrays.asList(roles))
 *   .claim(sub, username)           .subject(username)
 *   .expiresIn(Duration)            .expiresAt(Instant)
 *   .sign()                         jwtEncoder.encode(JwtEncoderParameters.from(claims))
 *
 * Dependency Injection:
 * - Quarkus: SmallRye JWT firma automaticamente con chiave da application.yml
 * - Spring:  JwtEncoder iniettato via costruttore (configurato in SecurityConfig)
 */
@Slf4j
@RestController
@RequestMapping("/demo")
@Tag(name = "jwt authorization demo")
public class DemoJwtGeneratorController {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.issuer:https://unittestdemoapp.fugerit.org}")
    private String issuer;

    @Value("${app.jwt.duration-minutes:60}")
    private long durationMinutes;

    /**
     * Costruttore - Spring Boot injection.
     *
     * In Quarkus: SmallRye JWT usa Jwt.sign() che carica automaticamente
     *             le chiavi da application.yml (smallrye.jwt.sign.key.location)
     *
     * In Spring:  Dobbiamo iniettare esplicitamente il JwtEncoder configurato
     *             in SecurityConfig con le chiavi RSA
     */
    public DemoJwtGeneratorController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @ApiResponse(responseCode = "200", description = "Generazione del JWT")
    @Operation(
            operationId = "demoToken",
            summary = "Genera un nuovo JWT, i ruoli vanno passati come path param separati da virgola (es. 'admin,user,guest')",
            description = "Attenzione: da utilizzare solo per motivi dimostrativi! (la durata del JWT sarà di un'ora)"
    )
    @GetMapping(value = "/{roles}.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String newToken(@PathVariable String roles) {
        return generateToken("DEMOUSER", roles.split(","));
    }

    /**
     * Genera un JWT per un utente con ruolo guest.
     *
     * @return il token JWT generato
     */
    public String generateGuestToken() {
        return generateToken("USER3", EnumRoles.GUEST.getCode());
    }

    /**
     * Genera un JWT per un utente con ruoli user e guest.
     *
     * @return il token JWT generato
     */
    public String generateUserToken() {
        return generateToken("USER1", EnumRoles.USER.getCode(), EnumRoles.GUEST.getCode());
    }

    /**
     * Genera un JWT per un utente con ruoli admin, user e guest.
     *
     * @return il token JWT generato
     */
    public String generateAdminToken() {
        return generateToken("USER2", EnumRoles.ADMIN.getCode(), EnumRoles.USER.getCode(), EnumRoles.GUEST.getCode());
    }

    /**
     * Genera un JWT personalizzato.
     *
     * CONVERSIONE DA QUARKUS:
     *
     * Quarkus (SmallRye JWT):
     * ```
     * return Jwt.issuer(ISSUER)
     *     .upn(username)
     *     .groups(new HashSet<>(Arrays.asList(roles)))
     *     .claim(Claims.sub.name(), username)
     *     .expiresIn(Duration.ofMinutes(JWT_DURATION_IN_MINUTES))
     *     .sign();
     * ```
     *
     * Spring Boot (OAuth2 JwtEncoder):
     * ```
     * JwtClaimsSet claims = JwtClaimsSet.builder()
     *     .issuer(ISSUER)
     *     .subject(username)
     *     .claim("upn", username)
     *     .claim("groups", Arrays.asList(roles))
     *     .expiresAt(now.plus(duration))
     *     .build();
     * return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
     * ```
     *
     * STRUTTURA DEL JWT PRODOTTO:
     * Identica tra Quarkus e Spring Boot - stesso issuer, stessi claim, stessa firma RSA.
     * I token sono quindi completamente compatibili e intercambiabili.
     *
     * @param username lo username da usare per il JWT (verrà inserito come upn e claim sub)
     * @param roles    l'elenco dei ruoli da associare all'utente (inseriti nel claim 'groups')
     * @return il token JWT firmato con RS256
     */
    public String generateToken(String username, String... roles) {
        Instant now = Instant.now();
        Instant expiry = now.plus(durationMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .claim("upn", username)
                .claim("groups", Arrays.asList(roles))
                .issuedAt(now)
                .expiresAt(expiry)
                .id(UUID.randomUUID().toString())
                .build();

        log.debug("Generating JWT for user '{}' with roles {}", username, Arrays.toString(roles));

        // Quarkus: Jwt.sign() firma automaticamente
        // Spring:  jwtEncoder.encode() usa le chiavi configurate in SecurityConfig
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}