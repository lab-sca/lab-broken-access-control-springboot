# Broken Access Control Lab - Spring Boot

Un laboratorio educativo completo per testare e comprendere le vulnerabilità [Broken Access Control](https://owasp.org/Top10/2025/A01_2025-Broken_Access_Control/) nelle applicazioni Java con Spring Boot.

> ⚠️ **ATTENZIONE**: Questo progetto contiene **intenzionalmente vulnerabilità di sicurezza** a scopo educativo. **NON utilizzare in produzione** e **NON esporre pubblicamente** senza aver rimosso tutte le vulnerabilità dimostrative.

> 🔴 Versione vulnerabile

Le vulnerabilità di tipo [Broken Access Control](https://owasp.org/Top10/2025/A01_2025-Broken_Access_Control/) sono attualmente le più diffuse secondo il progetto [OWASP](https://owasp.org/). Sono al primo posto sia nella [OWASP Top 10](https://owasp.org/Top10/) del [2021](https://owasp.org/Top10/2021/) che [2025](https://owasp.org/Top10/2025/).

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](CHANGELOG.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-teal.svg)](https://opensource.org/licenses/MIT)

## 🆕 Spring Boot Version

Questo è il **port per Spring Boot** del laboratorio originale Quarkus. Tutte le funzionalità e vulnerabilità dimostrative sono state convertite mantenendo la stessa architettura di sicurezza e gli stessi test.

**Differenze principali rispetto a Quarkus**:
- JAX-RS (`@Path`, `@GET`) → Spring MVC (`@RequestMapping`, `@GetMapping`)
- SmallRye JWT → Spring Security OAuth2 Resource Server
- Panache (Active Record) → Spring Data JPA (Repository pattern)
- `@ApplicationScoped` → `@RestController`
- `@TestSecurity` → `@WithMockUser`
- RestAssured → MockMvc per i test
- `@RolesAllowed` → `@PreAuthorize`

Per la versione Quarkus originale, visita: [lab-broken-access-control-quarkus](https://github.com/fugerit79/lab-broken-access-control-quarkus)

## Indice

### Il laboratorio

- [Quickstart](#quickstart)
- [Obiettivi del laboratorio](#obiettivi-del-laboratorio)
- [Cosa imparerai](#cosa-imparerai)
- [Il progetto](#il-progetto)
- [Lo scenario](#lo-scenario)
- [Vulnerabilità dimostrative](#vulnerabilità-dimostrative)
- [Architettura della sicurezza](#architettura-della-sicurezza)
- [Workflow del laboratorio](#workflow-del-laboratorio)
- [Riferimenti rapidi](#-riferimenti-rapidi)
- [FAQ / Problemi comuni](#-faq--problemi-comuni)
- [Licenza](#licenza)

### Contenuti extra

- [Note sugli unit test](JUNIT-TEST.md)
- [Security JUnit con tagging](JUNIT-TAG.md)
- [Troubleshooting](TROUBLESHOOTING.md)
- [Contribuire](CONTRIBUITING.md)

## Quickstart

### Requisiti

* Maven 3.9.x
* Java 21+

### Verifica dell'applicazione

Per eseguire i test standard:
```shell
mvn verify
```

Per attivare anche la verifica dei tag di sicurezza con il plugin `junit5-tag-check-maven-plugin`:
```shell
mvn verify -P security
```

### Avvio dell'applicazione
```shell
mvn spring-boot:run
```

L'applicazione sarà disponibile su: http://localhost:8080

### Utilizzo dell'applicazione

1. Apri la [Swagger UI](http://localhost:8080/swagger-ui/index.html)
2. Genera un JWT token (vedi sezione successiva)
3. Autorizza le richieste con il token
4. Testa gli endpoint disponibili

### Generazione e utilizzo dei JWT token

#### Generazione del token

Usa l'endpoint `/demo/{roles}.txt` per generare un JWT con i ruoli desiderati.

> ⏱️ **Durata token**: 1 ora (3600 secondi)  
> 🔑 **Algoritmo**: RS256 (RSA Signature con SHA-256)  
> 📝 **Issuer**: `https://unittestdemoapp.fugerit.org`

**Ruoli disponibili:**

| Ruolo   | Permessi                           | Esempio di utilizzo        |
|---------|------------------------------------|----------------------------|
| `admin` | Accesso completo a tutti i formati | Operazioni di gestione     |
| `user`  | Accesso a MarkDown e HTML          | Lettura documenti standard |
| `guest` | Accesso solo a MarkDown            | Visualizzazione base       |

**Esempi di generazione da console:**
```bash
# Token con singolo ruolo
curl http://localhost:8080/demo/guest.txt
```
```bash
# Token con ruoli multipli (separati da virgola)
curl http://localhost:8080/demo/admin,user,guest.txt
```

> ⚠️ **Nota importante**: L'endpoint `/demo/{roles}.txt` è fornito **solo per scopi dimostrativi**.
> In produzione, l'autenticazione deve avvenire tramite un Identity Provider (IDP) esterno.

**Esempio di payload JWT generato:**
```json
{
  "iss": "https://unittestdemoapp.fugerit.org",
  "sub": "DEMOUSER",
  "upn": "DEMOUSER",
  "groups": [
    "admin",
    "user",
    "guest"
  ],
  "iat": 1739795000,
  "exp": 1739798600,
  "jti": "a2b3c4d5-e6f7-48a9-9b0c-1d2e3f4g5h6i"
}
```

Puoi usare strumenti online come [jwt.io](https://www.jwt.io/) per verificare il contenuto del tuo JWT.

#### Autorizzazione nella Swagger UI

1. Clicca sul pulsante **"Authorize"** (lucchetto verde in alto a destra) nella Swagger UI
2. Inserisci il JWT ottenuto in precedenza (senza "Bearer", solo il token)
3. Clicca su "Authorize"
4. Clicca su "Close"

> **Nota Spring Boot**: A differenza di Quarkus, in Spring Boot Swagger UI aggiunge automaticamente il prefisso "Bearer" al token, quindi devi incollare **solo il token** senza "Bearer".

### Test: Accesso negato (403 Forbidden)

Se tenti di accedere a un endpoint senza i ruoli necessari, riceverai un errore 403.

**Esempio**: Tentativo di accesso a `/doc/example.adoc` senza ruolo `admin`

```bash
curl -H "Authorization: Bearer <user-token>" http://localhost:8080/doc/example.adoc
# Risposta: 403 Forbidden
```

### Test: Accesso consentito (200 OK)

Con i ruoli appropriati, puoi accedere agli endpoint autorizzati.

**Esempio**: Accesso a `/doc/example.md` con ruoli `guest` o `user`

```bash
curl -H "Authorization: Bearer <guest-token>" http://localhost:8080/doc/example.md
# Risposta: 200 OK + documento Markdown
```

Vedi la [mappatura di ruoli e path](#mappatura-ruoli--permessi--metodo-http) per maggiori dettagli.

## Workflow del laboratorio

### Passo 1: Setup iniziale
```bash
git clone https://github.com/fugerit79/lab-broken-access-control-springboot.git
cd lab-broken-access-control-springboot
mvn spring-boot:run
```

### Passo 2: Esplora le vulnerabilità

- Apri `DocController.java`
- Cerca i commenti `// VULNERABILITY: (n)`
- Analizza il codice vulnerabile
- Identifica il tipo di vulnerabilità (IDOR, BOLA, etc.)

### Passo 3: Esegui i test
```bash
mvn verify -P security
```

I test falliranno dove ci sono vulnerabilità. Osserva gli errori per capire cosa non funziona.

### Passo 4: Correggi le vulnerabilità

- Implementa le correzioni seguendo le best practices OWASP
- Verifica con i test che le modifiche funzionino
- Confronta con le soluzioni (`// SOLUTION: (n)`)

### Passo 5: Verifica la copertura
```bash
mvn verify -P security
```

Tutti i test devono passare ✅

### Passo 6: Trova la vulnerabilità BONUS

Cerca la vulnerabilità (X) che non è coperta dai test. Suggerimenti:
- Esamina tutti gli endpoint
- Cerca metodi HTTP non documentati
- Controlla le annotation mancanti

## Obiettivi del laboratorio

Questo laboratorio ti permetterà di:

- 🎯 Comprendere le vulnerabilità Broken Access Control in pratica
- 🔍 Identificare pattern di codice vulnerabile
- 🛡️ Imparare tecniche di mitigazione e best practices
- ✅ Implementare test di sicurezza efficaci con JUnit tags
- 📊 Misurare la copertura dei requisiti di sicurezza
- 🔄 Comprendere le differenze tra Quarkus e Spring Boot Security

## Cosa imparerai

Completando questo laboratorio, acquisirai competenze pratiche su:

- 🔐 **Autenticazione JWT**: Implementazione e configurazione con Spring Security OAuth2
- 🛡️ **RBAC**: Design e implementazione di Role-Based Access Control
- 🐛 **Vulnerability Detection**: Identificazione di BOLA, IDOR e privilege escalation
- ✅ **Security Testing**: Strategia di test con JUnit tags e MockMvc
- 📊 **Security Metrics**: Misurazione della copertura dei requisiti di sicurezza
- 🔒 **Defense in Depth**: Approccio a più livelli per la sicurezza applicativa
- ⚙️ **Spring Security**: Configurazione OAuth2 Resource Server e JWT Decoder/Encoder

## Il progetto

Questo progetto dimostra come implementare una strategia di testing basata su tag JUnit per garantire la copertura dei requisiti di sicurezza in un'applicazione Spring Boot con autenticazione JWT e RBAC (Role-Based Access Control).

### Stack tecnologico

I principali componenti usati per questo progetto sono:

- [Spring Boot 3.4.x - Framework applicativo Java enterprise](https://spring.io/projects/spring-boot)
- [Spring Security - Framework di sicurezza e autenticazione](https://spring.io/projects/spring-security)
- [Spring Data JPA - Astrazione per la persistenza dati con pattern Repository](https://spring.io/projects/spring-data-jpa)
- [SpringDoc OpenAPI - Generazione automatica documentazione API (Swagger UI)](https://springdoc.org/)
- [H2 Database - Database in-memory per sviluppo e test](https://www.h2database.com/)
- [junit5-tag-check-maven-plugin - Plugin Maven che permette di verificare che dei test con tag specifici siano stati eseguiti](https://github.com/fugerit-org/junit5-tag-check-maven-plugin)
- [Fugerit Venus Doc - Framework per la generazione di documenti in vari formati (usato solo per le funzionalità dimostrative)](https://github.com/fugerit-org/fj-doc)

## Lo scenario

Nel nostro scenario, abbiamo una base dati popolata e alcuni path disponibili.

### Base dati

Esiste una base dati di persone (sono entità di dominio, non utenti). La tabella PEOPLE è pre-popolata con 3 soggetti, che hanno 4 proprietà principali:

- Nome, Cognome, Titolo descrivono la persona
- Ruolo minimo: rappresenta il ruolo minimo richiesto per poter accedere a quella persona

| Nome       | Cognome | Titolo      | Ruolo minimo |
|------------|---------|-------------|--------------|
| Richard    | Feynman | Fisico      | admin        |
| Margherita | Hack    | Astrofisica | -            |
| Alan       | Turing  | Matematico  | -            |

> **NOTA**: Nel nostro DB pre-popolato tutti possono vedere i dati di Margherita Hack e Alan Turing, ma per vedere i dati di Richard Feynman (che sta lavorando al progetto Manhattan), serve il ruolo 'admin'.

### Mappatura ruoli / permessi / metodo http

L'applicazione è configurata per gestire 3 ruoli e 4 path, che generano lo stesso documento in formati diversi. Non tutti i ruoli sono autorizzati a generare ogni path. Ecco la mappa dei permessi:

| Path                        | Output      | Ruoli autorizzati  | Metodo http |
|-----------------------------|-------------|--------------------|-------------|
| `/doc/example.md` (*)       | 📝 MarkDown | admin, user, guest | GET         |
| `/doc/example.adoc`         | 📄 AsciiDoc | admin              | GET         |
| `/doc/example.html` (*)     | 🌐 HTML     | admin, user        | GET         |
| `/doc/example.pdf`          | 📑 PDF      | admin              | GET         |
| `/doc/person/list` (*)      | 📋 JSON     | admin, user        | GET         |
| `/doc/person/find/{id}` (*) | 📋 JSON     | admin, user        | GET         |
| `/doc/person/add`           | 📋 JSON     | admin              | POST        |
| `/doc/person/delete/{id}`   | 📋 JSON     | admin              | DELETE      |

> (*) Eccetto gli utenti con ruolo 'admin', su questi path potrebbe esserci una limitazione ai dati mostrati in base al ruolo minimo richiesto.

**Ruoli e permessi dettagliati:**

| Ruolo   | Permessi                           | Esempio di utilizzo                         |
|---------|------------------------------------|---------------------------------------------|
| `admin` | Accesso completo a tutti i formati | Vedere Richard Feynman, gestire persone     |
| `user`  | Accesso a MarkDown e HTML          | Vedere Hack e Turing, documenti base        |
| `guest` | Accesso solo a MarkDown            | Visualizzazione read-only limitata          |

## Vulnerabilità dimostrative

Questo laboratorio include 6 vulnerabilità reali di tipo Broken Access Control:

| #   | Vulnerabilità                      | Classificazione | Endpoint                                                       | Status      |
|-----|------------------------------------|-----------------|----------------------------------------------------------------|-------------|
| (1) | ID Enumeration                     | ?               | `/person/find/{id}`                                            | 🔴 To Fix   |
| (2) | Privilege Escalation (Data)        | ?               | `/doc/example.md`, `/doc/example.html`, `/doc/person/list`     | 🔴 To Fix   |
| (3) | Privilege Escalation (Action)      | ?               | `/doc/person/delete/{id}`                                      | 🔴 To Fix   |
| (4) | Broken Object Authorization        | ?               | `/doc/person/find/{id}`                                        | 🔴 To Fix   |
| (5) | Missing Authentication             | ?               | `/doc/example.md`                                              | 🔴 To Fix   |
| (X) | Hidden Vulnerability (BONUS)       | ?               | `???`                   

---

Le vulnerabilità da risolvere saranno presenti a partire dal controller REST:

- [DocController](src/main/java/org/fugerit/java/demo/lab/broken/access/control/controller/DocController.java)

Visto che questo progetto segue l'approccio del *Test-driven development* abbiamo scritto prima i test della nostra applicazione, ovvero:

- [DocControllerSecurityTest](src/test/java/org/fugerit/java/demo/lab/broken/access/control/DocControllerSecurityTest.java) - Test di sicurezza, in particolare gli accessi non autorizzati

I casi di test dove sono presenti vulnerabilità falliranno, per quelli sarà presente il commento:
```java
// VULNERABILITY: (n) risolvi questa vulnerabilità in modo che il caso di test funzioni.
```

Una volta pubblicate le soluzioni, le potrai trovare cercando il commento:
```java
// SOLUTION: (n) 
```

Dove (n) è l'id del comportamento vulnerabile introdotto, ad esempio (1).

In totale saranno presenti 5 vulnerabilità. Ognuna farà fallire uno dei casi di test. Solo la numero (2) farà fallire 2 casi di test.

> **BONUS**: C'è un path che contiene una vulnerabilità non censita negli unit test, nella soluzione sarà censita come SOLUTION: (X)

Buon lavoro!

## Architettura della sicurezza

L'applicazione implementa un sistema di sicurezza a più livelli:

1. **Autenticazione JWT**: Verifica dell'identità tramite token firmati con RS256
2. **OAuth2 Resource Server**: Spring Security gestisce la validazione JWT
3. **RBAC**: Controllo accessi basato su ruoli con `@PreAuthorize`
4. **Object-Level Authorization**: Verifica permessi su singoli oggetti nel codice
5. **Test automatizzati**: Garanzia della copertura dei requisiti di sicurezza tramite tag JUnit

### Flusso di autenticazione (Spring Boot)
```
User → JWT Token → SecurityFilterChain → JwtDecoder → JwtAuthenticationConverter 
     → GrantedAuthority (roles) → @PreAuthorize Check → Object Authorization → Resource Access
```

### Componenti di sicurezza Spring Boot

| Componente | Ruolo | File di configurazione |
|------------|-------|------------------------|
| `SecurityConfig` | Configurazione centrale sicurezza | Bean `SecurityFilterChain`, `JwtDecoder`, `JwtEncoder` |
| `JwtDecoder` | Decodifica e valida JWT con chiave pubblica RSA | `SecurityConfig.java` |
| `JwtEncoder` | Genera JWT firmati per demo | `SecurityConfig.java` |
| `JwtAuthenticationConverter` | Mappa claim 'groups' → GrantedAuthority | `SecurityConfig.java` |
| `@PreAuthorize` | Controllo accessi a livello metodo | Sui metodi dei controller |
| `@WithMockUser` | Mock user per test di sicurezza | Sui metodi di test |
| `MockMvc` | Client HTTP per test integration | Nei test `DocControllerSecurityTest` |

## 📚 Riferimenti rapidi

| Risorsa              | Link                                       |
|----------------------|--------------------------------------------|
| Swagger UI           | http://localhost:8080/swagger-ui/index.html|
| H2 Console           | http://localhost:8080/h2-console           |
| Actuator Health      | http://localhost:8080/actuator/health      |
| OpenAPI JSON         | http://localhost:8080/v3/api-docs          |
| OWASP Top 10 (2025)  | https://owasp.org/Top10/2025/              |
| OWASP API Security   | https://owasp.org/API-Security/            |
| JWT Debugger         | https://jwt.io/                            |
| Spring Security Docs | https://docs.spring.io/spring-security/    |
| Spring Boot Docs     | https://docs.spring.io/spring-boot/        |

## ❓ FAQ / Problemi comuni

<details>
<summary><b>Il token JWT scade troppo velocemente</b></summary>

I token hanno validità di 1 ora. Genera un nuovo token con:
```bash
curl http://localhost:8080/demo/admin,user,guest.txt
```

Oppure usa la Swagger UI per rigenerarlo rapidamente.
</details>

<details>
<summary><b>Errore 403 anche con il token corretto</b></summary>

Verifica:
1. ✅ Token non scaduto (controlla `exp` su jwt.io)
2. ✅ Ruolo appropriato per l'endpoint (vedi tabella permessi)
3. ✅ Header Authorization corretto: `Bearer <token>` (con lo spazio)
4. ✅ Token copiato completamente senza spazi extra
5. ✅ In Swagger UI, incolla SOLO il token (senza "Bearer")
</details>

<details>
<summary><b>I test di sicurezza non vengono eseguiti</b></summary>

Usa il profilo security:
```bash
mvn verify -P security
```

Il profilo `security` attiva il plugin `junit5-tag-check-maven-plugin` che verifica la copertura dei test taggati.
</details>

<details>
<summary><b>Spring Boot non si avvia - porta 8080 occupata</b></summary>

Cambia la porta in `application.yml`:
```yaml
server:
  port: 8081
```

Oppure termina il processo che occupa la porta 8080:
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```
</details>

<details>
<summary><b>Errore 401 Unauthorized su tutti gli endpoint</b></summary>

Hai dimenticato di autorizzare nella Swagger UI. Clicca sul pulsante "Authorize" (lucchetto verde) in alto a destra e inserisci il token (senza "Bearer"):
```
eyJ0eXAiOiJKV1QiLCJhbGc...
```
</details>

<details>
<summary><b>Come faccio a vedere Richard Feynman?</b></summary>

Richard Feynman ha `minRole=admin`, quindi serve un token con ruolo `admin`:
```bash
curl http://localhost:8080/demo/admin.txt
```

Poi usa questo token per chiamare `/doc/person/list` o `/doc/example.md`.
</details>

<details>
<summary><b>I test passano ma la vulnerabilità è ancora presente</b></summary>

Ricorda che ci sono 6 vulnerabilità:
- 5 coperte dai test (che devono passare)
- 1 BONUS non coperta dai test (devi trovarla manualmente)

Cerca `// SOLUTION: (X)` nel codice per vedere la vulnerabilità nascosta.
</details>

<details>
<summary><b>Errore: Unable to load RSA keys</b></summary>

Verifica che i file delle chiavi RSA siano presenti:
```
src/main/resources/security/publicKey.pem
src/main/resources/security/privateKey.pem
```

Se mancano, copiali dal progetto Quarkus originale o rigenerane di nuovi.
</details>

<details>
<summary><b>Differenze tra versione Quarkus e Spring Boot?</b></summary>

Le principali differenze tecniche:

| Aspetto | Quarkus | Spring Boot |
|---------|---------|-------------|
| **REST Framework** | JAX-RS (`@Path`, `@GET`) | Spring MVC (`@RequestMapping`, `@GetMapping`) |
| **JWT** | SmallRye JWT | Spring Security OAuth2 Resource Server |
| **ORM** | Panache (Active Record) | Spring Data JPA (Repository pattern) |
| **Scope** | `@ApplicationScoped` | `@RestController`, `@Component` |
| **Security Annotation** | `@RolesAllowed` | `@PreAuthorize` |
| **Test Security** | `@TestSecurity` | `@WithMockUser` |
| **HTTP Client Test** | RestAssured | MockMvc |
| **Response** | JAX-RS `Response` | Spring `ResponseEntity` |
| **Path Parameters** | `@PathParam` | `@PathVariable` |
| **Persistence** | `person.persist()` | `repository.save(person)` |
| **Transactions** | `@jakarta.transaction.Transactional` | `@org.springframework...Transactional` |

**Funzionalmente** sono identiche:
- ✅ Stesse 6 vulnerabilità dimostrative
- ✅ Stessa architettura di sicurezza RBAC
- ✅ Stessi test (convertiti da RestAssured a MockMvc)
- ✅ Stessi JWT (RS256, compatibili tra versioni)
- ✅ Stesso database H2 e schema
</details>

## Licenza

Questo progetto è rilasciato sotto licenza MIT - vedi il file [LICENSE](LICENSE) per i dettagli.

---

## 🎓 Per ulteriori informazioni

- 📖 [Note sugli unit test](JUNIT-TEST.md)
- 🏷️ [Security JUnit con tagging](JUNIT-TAG.md)
- 🔧 [Troubleshooting avanzato](TROUBLESHOOTING.md)
- 🤝 [Come contribuire](CONTRIBUITING.md)
- 🔄 [Versione Quarkus originale](https://github.com/fugerit79/lab-broken-access-control-quarkus)

---

**Sviluppato con ❤️ per la community della sicurezza applicativa**

**Spring Boot port**: Community contribution | **Original Quarkus version**: [fugerit79](https://github.com/fugerit79)