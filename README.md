# Eksamensprojekt | Gruppe 2:
## Projektkalkulationsværktøj

Et webbaseret værktøj til at nedbryde projekter, estimere og registrere tidsforbrug, samt visualise projektdetaljer for Alpha Solutions.

## Om projektet
Dette værktøj er udviklet for at hjælpe Alpha Solutions med at effektivisere deres projektstyring. Løsningen tilbyder:
*   **Projektstyring:** Oprettelse og redigering af projekter, delprojekter, opgaver og delopgaver.
*   **Tidskalkulation:** Beregning af totalt tidsforbrug (estimeret og faktisk).
*   **Rapportering:** Visuelt overblik over deadlines, opgaver, og distribution af timeestimater.

## Teknologier
* Backend: Java 21, Spring Boot 3.5.7, Spring JDBC (JdbcTemplate)
* Frontend: Thymeleaf 3.1.3, HTML5, CSS3, JavaScript (Chart.js 4.5.1)
* Database: MySQL 8.0
* Build Tool: Maven 4.0.0
* CI/CD: GitHub Actions (2025 workflow)
* Hosting: Azure Web App + Azure MySQL
* IDE: IntelliJ IDEA 2025.2

## Installation & Setup

### Forudsætninger
*   Java 21 JDK installeret.
*   MySQL Server kørende lokalt.

### Trin-for-trin
1.  **Klon repository:**
    ```bash
    git clone https://github.com/KEA-BarHjaMadMik/Eksamensprojekt.git
    cd Eksamensprojekt
    ```

2.  **Konfigurer database:**
    *   Kør SQL-scriptet `src/main/resources/scripts/db_exam_project_create.sql` for at oprette database og tabeller.
    *   *(Valgfrit)* Kør SQL-scriptet fundet i `src/main/resources/scripts/db_exam_project_populate.sql` for at indsætte testdata i tabellerne.

3.  **Miljøvariabler:**
    For at køre projektet lokalt skal følgende miljøvariabler opsættes (f.eks. i IntelliJ Run Configuration eller Environment Variables):
    *   `DEV_DATABASE_URL`: `jdbc:mysql://localhost:3306/alpha_solutions_db`
    *   `DEV_USERNAME`: `din_bruger` (f.eks. root)
    *   `DEV_PASSWORD`: `dit_password`
## Kørsel
Kør EksamensprojektApplication.java i IntelliJ IDEA.

## Test
Testdækningen er opdelt i to primære niveauer for at sikre både applikationslogik og data-integritet:

*   **Web Layer Tests (`@WebMvcTest`):**
    Isoleret test af controllere. Her verificeres HTTP-statuskoder, routing, sessionshåndtering og redirects uden at starte hele applikations-konteksten. Service-laget mockes ved hjælp af `@MockitoBean`.
*   **Integration Tests:**
    Validering af database-interaktion og repositories. Disse tests afvikles mod en **H2 in-memory database** for at sikre et kontrolleret testmiljø.

### Teknologier
*   **JUnit 5:** Test framework.
*   **AssertJ:** Fluent assertions.
*   **Mockito:** Mocking af afhængigheder.
*   **MockMvc:** Simulering af HTTP requests.
*   **H2 Database:** In-memory database til integrationstests.

## Deployment

* **CI/CD:** Pipeline er opsat via GitHub Actions, som kører tests ved hvert push og pull request.
* **Hosting:** Automatisk deployment til Azure App Service ved push til main-branchen.
* **Database:** Azure Database for MySQL.
* **Kørende applikation:** [https://exam-project.azurewebsites.net/](https://exam-project.azurewebsites.net/)

## Team
- GitHub navne: @HeroMouse, @BardieJoensen, @MBroholm

## Contributing
[Se CONTRIBUTING.md](docs/CONTRIBUTING.md)

