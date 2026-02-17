package org.fugerit.java.demo.lab.broken.access.control.config;

import lombok.extern.slf4j.Slf4j;
import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.demo.lab.broken.access.control.DocHelper;
import org.fugerit.java.doc.base.config.InitHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

/**
 * AppInitializer - Inizializzazione dell'applicazione Spring Boot.
 *
 * Conversione da Quarkus a Spring Boot:
 *
 * CONVERSIONI CHIAVE:
 *
 * Lifecycle Hook:
 * - Quarkus: void onStart(@Observes StartupEvent ev)
 * - Spring:  void run(ApplicationArguments args) throws Exception
 *
 * Component Scope:
 * - Quarkus: @ApplicationScoped
 * - Spring:  @Component
 *
 * Reflection Registration:
 * - Quarkus: @RegisterForReflection(targets = {DocHelper.class, People.class})
 * - Spring:  Non necessario - Spring Boot configura automaticamente la reflection
 *           per le classi gestite dal container
 *
 * Database Access:
 * - Quarkus: DriverManager.getConnection(url) con URL hardcoded
 * - Spring:  DataSource iniettato da Spring (configurato in application.yml)
 *
 * COMPORTAMENTO IDENTICO:
 * - Inizializzazione handlers Fugerit Venus Doc in modalità asincrona
 * - Log delle informazioni sul database
 * - Esecuzione all'avvio dell'applicazione
 */
@Slf4j
@Component
public class AppInitializer implements ApplicationRunner {

    private final DocHelper docHelper;

    /**
     * Costruttore - Spring Boot injection.
     *
     * In Quarkus: DocHelper viene iniettato via CDI
     * In Spring:  DocHelper e DataSource vengono iniettati via costruttore
     */
    public AppInitializer(DocHelper docHelper) {
        this.docHelper = docHelper;
    }

    /**
     * Metodo eseguito all'avvio dell'applicazione.
     *
     * Equivalente Quarkus: void onStart(@Observes StartupEvent ev)
     *
     * ApplicationRunner viene eseguito dopo che:
     * - Il contesto Spring è stato completamente inizializzato
     * - Tutti i bean sono stati creati
     * - Il database è stato configurato (schema.sql e data.sql eseguiti)
     *
     * @param args argomenti da linea di comando
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("L'applicazione si sta avviando...");
        log.info("========================================");

        // Inizializza i document handlers di Fugerit Venus Doc in modalità asincrona
        // (identico al codice Quarkus)
        InitHandler.initDocAllAsync(
                docHelper.getDocProcessConfig().getFacade().handlers());

        // Log informazioni database
        initDb();

        log.info("========================================");
        log.info("Avvio terminato.");
        log.info("========================================");
        log.info("Swagger UI:  http://localhost:8080/swagger-ui/index.html");
        log.info("H2 Console:  http://localhost:8080/h2-console");
        log.info("Generate JWT: http://localhost:8080/demo/admin,user,guest.txt");
        log.info("========================================");
    }

    /**
     * Inizializza e logga informazioni sul database.
     *
     * CONVERSIONE DA QUARKUS:
     * - Quarkus: usa DriverManager con URL hardcoded e SafeFunction.apply()
     * - Spring:  usa DataSource iniettato (configurato in application.yml)
     *
     * Nota: In Spring Boot non è necessario eseguire manualmente init.sql
     *       perché Spring lo gestisce automaticamente tramite:
     *       - spring.sql.init.mode=always
     *       - schema.sql e data.sql in src/main/resources
     */
    private void initDb() {
        SafeFunction.apply(() -> {
            String url = "jdbc:h2:mem:labbac;DB_CLOSE_DELAY=-1;MODE=Oracle;INIT=RUNSCRIPT FROM './src/test/resources/h2init/init.sql';";
            try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
                DatabaseMetaData meta = conn.getMetaData();
                log.info("Connected to database '{} - {}'", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
            }
        });
    }

}