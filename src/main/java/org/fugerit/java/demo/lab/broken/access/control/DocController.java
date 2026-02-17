package org.fugerit.java.demo.lab.broken.access.control;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.fugerit.java.demo.lab.broken.access.control.dto.AddPersonRequestDTO;
import org.fugerit.java.demo.lab.broken.access.control.dto.AddPersonResponseDTO;
import org.fugerit.java.demo.lab.broken.access.control.dto.PersonResponseDTO;
import org.fugerit.java.demo.lab.broken.access.control.entity.Person;
import org.fugerit.java.demo.lab.broken.access.control.repository.PersonRepository;
import org.fugerit.java.doc.base.config.DocConfig;
import org.fugerit.java.doc.base.process.DocProcessContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DocResource - REST controller per la generazione di documenti e gestione persone.
 *
 * Conversione completa da Quarkus JAX-RS a Spring Boot MVC.
 *
 * MAPPA DELLE CONVERSIONI:
 *
 * Annotation Level:
 * - @ApplicationScoped          → @RestController
 * - @Path("/doc")               → @RequestMapping("/doc")
 * - @GET                        → @GetMapping
 * - @POST                       → @PostMapping
 * - @DELETE                     → @DeleteMapping
 * - @Path("/example.html")      → value = "/example.html" in @GetMapping
 * - @PathParam("id")            → @PathVariable Long id
 * - @Produces("text/html")      → produces = "text/html"
 * - @Consumes(APPLICATION_JSON) → consumes = APPLICATION_JSON
 *
 * Response Level:
 * - Response.status(OK).entity(body).build()     → ResponseEntity.ok(body)
 * - Response.status(CREATED).entity(body).build() → ResponseEntity.status(CREATED).body(body)
 * - Response.status(FORBIDDEN).build()           → ResponseEntity.status(FORBIDDEN).build()
 * - Response.Status.OK                           → HttpStatus.OK
 *
 * Security Level:
 * - SecurityIdentity securityIdentity (field)    → Authentication auth (parameter)
 * - securityIdentity.getRoles()                  → getRolesFromAuth(auth)
 * - securityIdentity.getPrincipal().getName()    → auth.getName()
 *
 * Persistence Level:
 * - person.persistAndFlush()                     → personRepository.save(person)
 * - personRepository.findById(id)                → personRepository.findById(id).orElse(null)
 * - person.delete()                              → personRepository.delete(person)
 *
 * Transaction Level:
 * - @jakarta.transaction.Transactional           → @org.springframework...Transactional
 *
 * OpenAPI Level:
 * - @APIResponse (MicroProfile)                  → @ApiResponse (SpringDoc)
 * - @Schema MicroProfile                         → @Schema Swagger v3
 */
@Slf4j
@RestController
@RequestMapping("/doc")
@Tag(name = "document", description = "Servizio rest per la generazione di documento che contiene una lista di persone in vari formati, in aggiunta permette di manipolare l'elenco delle persone.")
@SecurityRequirement(name = "bearerAuth")
public class DocController {

    private final DocHelper docHelper;
    private final PersonRepository personRepository;

    /**
     * Costruttore - Spring Boot injection.
     *
     * In Quarkus: i campi vengono iniettati via CDI automaticamente.
     * In Spring: il costruttore con parametri è il meccanismo di injection.
     *
     * Nota: SecurityIdentity NON viene iniettato come campo in Spring.
     *       Viene invece passato come parametro Authentication nei metodi.
     */
    public DocController(DocHelper docHelper, PersonRepository personRepository) {
        this.docHelper = docHelper;
        this.personRepository = personRepository;
    }

    // =========================================================================
    // ENDPOINT GENERAZIONE DOCUMENTI
    // =========================================================================

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The HTML document content"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "document")
    @Operation(operationId = "HTMLExample",
            summary = "Versione HTML del documento (ruoli: admin, user)",
            description = "Generato con Fugerit Venus Doc https://venusdocs.fugerit.org/")
    @GetMapping(value = "/example.html", produces = "text/html")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    public ResponseEntity<byte[]> htmlExample(Authentication auth) throws IOException {
        // Quarkus: Response.status(Response.Status.OK).entity(processDocument(...)).build()
        // Spring:  ResponseEntity.ok(processDocument(...))
        return ResponseEntity.ok(processDocument(DocConfig.TYPE_HTML, auth));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The Markdown document content"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "document")
    @Operation(operationId = "MarkdownExample",
            summary = "Versione MarkDown del documento (ruoli: admin, user, guest)",
            description = "Generato con Fugerit Venus Doc https://venusdocs.fugerit.org/")
    @GetMapping(value = "/example.md", produces = "text/markdown")
    public ResponseEntity<byte[]> markdownExample(Authentication auth) throws IOException {
        return ResponseEntity.ok(processDocument(DocConfig.TYPE_MD, auth));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The AsciiDoc document content"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "document")
    @Operation(operationId = "AsciiDocExample",
            summary = "Versione AsciiDoc del documento (ruoli: admin)",
            description = "Generato con Fugerit Venus Doc https://venusdocs.fugerit.org/")
    @GetMapping(value = "/example.adoc", produces = "text/asciidoc")
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<byte[]> asciidocExample(Authentication auth) throws IOException {
        return ResponseEntity.ok(processDocument(DocConfig.TYPE_ADOC, auth));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The PDF document content"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "document")
    @Operation(operationId = "PDFExample",
            summary = "Versione PDF del documento (ruoli: admin)",
            description = "Generato con Fugerit Venus Doc https://venusdocs.fugerit.org/")
    @GetMapping(value = "/example.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyAuthority('admin')")
    public ResponseEntity<byte[]> pdfExample(Authentication auth) throws IOException {
        return ResponseEntity.ok(processDocument(DocConfig.TYPE_PDF, auth));
    }

    // =========================================================================
    // ENDPOINT GESTIONE PERSONE
    // =========================================================================

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "La persona è stata creata",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AddPersonResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dati non validi (validazione fallita)"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "person")
    @Operation(operationId = "addPerson",
            summary = "Aggiunge una persona al database (ruoli: admin)",
            description = "Vanno forniti i parametri, nome, cognome, titolo e ruolo minimo.")
    @PostMapping(value = "/person/add",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('admin')")
    @Transactional
    public ResponseEntity<AddPersonResponseDTO> addPerson(@Valid @RequestBody AddPersonRequestDTO request) {
        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setTitle(request.getTitle());
        person.setMinRole(request.getMinRole());
        person = personRepository.save(person);

        AddPersonResponseDTO response = new AddPersonResponseDTO();
        response.setId(person.getId());
        response.setCreationDate(person.getCreationDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "La persona è stata creata",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AddPersonResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dati non validi (validazione fallita)"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "person")
    @Operation(operationId = "addPerson",
            summary = "Aggiunge una persona al database (ruoli: admin)",
            description = "Vanno forniti i parametri, nome, cognome, titolo e ruolo minimo.")
    @PutMapping(value = "/person/add",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<AddPersonResponseDTO> addPersonPut(@Valid @RequestBody AddPersonRequestDTO request) {
        return this.addPerson( request );
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dati della persona trovata",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersonResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "person")
    @Operation(operationId = "findPerson",
            summary = "Interroga i dati di una persona per ID (ruoli: admin, user)",
            description = "Sul risultato viene verificato che sia presente il ruolo minimo.")
    @GetMapping(value = "/person/find/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Transactional(readOnly = true)
    public ResponseEntity<PersonResponseDTO> findPerson(@PathVariable Long id, Authentication auth) {
        Person person = personRepository.findById(id).orElse(null);

        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(person.toDTO());
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Persona cancellata con successo"),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "person")
    @Operation(operationId = "deletePerson",
            summary = "Cancella una persona per ID (ruoli: admin)",
            description = "Cancella un utente")
    @DeleteMapping("/person/delete/{id}")
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Transactional
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        Person person = personRepository.findById(id).orElse(null);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        personRepository.delete(person);
        return ResponseEntity.ok().build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista delle persone",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PersonResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Se l'autenticazione non è presente"),
            @ApiResponse(responseCode = "403", description = "Se l'utente non è autorizzato per la risorsa"),
            @ApiResponse(responseCode = "500", description = "In caso di errori non gestiti")
    })
    @Tag(name = "person")
    @Operation(operationId = "listPersons",
            summary = "Elenca le persone attualmente presenti (ruoli: admin, user)",
            description = "Il risultato viene filtrato in base al ruolo minimo")
    @GetMapping(value = "/person/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('admin', 'user')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PersonResponseDTO>> listPersons(Authentication auth) {
        return ResponseEntity.ok(
                listAllPersons(auth).stream()
                        .map(Person::toDTO)
                        .toList()
        );
    }

    // =========================================================================
    // METODI PRIVATI
    // =========================================================================

    /**
     * Metodo worker che genera effettivamente i documenti tramite il framework:
     * https://github.com/fugerit-org/fj-doc ( documentazione: https://venusdocs.fugerit.org/ )
     */
    private byte[] processDocument(String handlerId, Authentication auth) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            List<Person> personsFromDb = this.listAllPersons(auth);

            // Converti le entità Person in oggetti People per il template
            List<People> listPeople = personsFromDb.stream()
                    .map(person -> new People(
                            person.getFirstName(),
                            person.getLastName(),
                            person.getTitle()))
                    .toList();

            log.info("processDocument handlerId : {}", handlerId);
            String chainId = "document";

            this.docHelper.getDocProcessConfig().fullProcess(
                    chainId,
                    DocProcessContext.newContext("listPeople", listPeople),
                    handlerId,
                    baos);

            return baos.toByteArray();
        }
    }

    /**
     * Metodo che carica tutte le persone cui l'utente corrente ha accesso.
     *
     */
    private List<Person> listAllPersons(Authentication auth) {
        Set<String> userRoles = getRolesFromAuthentication(auth);

        log.info("user : {}, roles : {}", auth != null ? auth.getName() : "-", userRoles);

        List<Person> personsFromDb = this.personRepository.findByRolesOrderedByName(userRoles);
        log.info("Caricate {} persone database", personsFromDb.size());
        return personsFromDb;
    }

    /**
     * Utility: estrae i ruoli dall'Authentication di Spring Security.
     *
     * In Spring Security, dobbiamo estrarre manualmente da Authentication.getAuthorities()
     * che contiene oggetti GrantedAuthority. La mappatura dal claim 'groups' ai ruoli
     * avviene tramite JwtAuthenticationConverter configurato in SecurityConfig.
     *
     * @param auth l'oggetto Authentication corrente
     * @return Set di ruoli come stringhe (es. {"admin", "user"})
     */
    private Set<String> getRolesFromAuthentication(Authentication auth) {
        return auth == null ? new HashSet<>() : auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

}