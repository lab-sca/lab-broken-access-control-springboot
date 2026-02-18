package org.fugerit.java.demo.lab.broken.access.control.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Configurazione Spring Security con JWT (OAuth2 Resource Server).
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    @Value("${jwt.demo.public-key-path}")
    private String publicKeyPath;

    @Value("${jwt.demo.private-key-path}")
    private String privateKeyPath;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoint pubblici
                        .requestMatchers("/demo/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // SOLUTION: (5) rimosso l' endpoint /dco/example.md da quelli per cui era permesso l'accesso indiscriminato
                        // Tutti gli altri endpoint richiedono autenticazione
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                // Necessario per H2 console (usa iframe)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    /**
     * JwtDecoder: verifica i JWT in ingresso con la chiave pubblica RSA.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            RSAPublicKey publicKey = loadPublicKey(publicKeyPath);
            log.info("✅ JwtDecoder configurato con chiave pubblica RSA da: {}", publicKeyPath);
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception e) {
            log.error("❌ Impossibile caricare la chiave pubblica JWT da: {}", publicKeyPath, e);
            throw new RuntimeException("Impossibile caricare la chiave pubblica JWT da: " + publicKeyPath, e);
        }
    }

    /**
     * JwtEncoder: firma i JWT generati dall'endpoint /demo.
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        try {
            RSAPublicKey publicKey = loadPublicKey(publicKeyPath);
            RSAPrivateKey privateKey = loadPrivateKey(privateKeyPath);
            RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
            log.info("✅ JwtEncoder configurato con coppia di chiavi RSA");
            return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        } catch (Exception e) {
            log.error("❌ Impossibile caricare le chiavi RSA per JwtEncoder", e);
            throw new RuntimeException("Impossibile caricare le chiavi RSA per JwtEncoder", e);
        }
    }

    /**
     * Mappa il claim 'groups' del JWT ai ruoli Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("groups");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    // =========================================================================
    // Utility: caricamento chiavi RSA dal classpath
    // =========================================================================

    private RSAPublicKey loadPublicKey(String path) throws Exception {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            String pem = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(keyBytes));
        }
    }

    private RSAPrivateKey loadPrivateKey(String path) throws Exception {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            String pem = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        }
    }

}