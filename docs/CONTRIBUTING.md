# Contributing

For at sikre kvalitet og stabilitet i `main`-branchen, følges nedenstående guidelines ved bidrag til projektet.

## Workflow

1.  **Opret Branch:**
    Arbejd aldrig direkte på `main`. Opret en ny branch for hver opgave.

2.  **Commits:**
    Hold commits små. Brug beskrivende commit messages (f.eks. "Tilføjer oprettelse af projekter" frem for "Update").

3.  **Test:**
    Inden der pushes eller oprettes Pull Request, skal alle tests passere lokalt:
    ```bash
    ./mvnw test
    ```

4.  **Pull Request (PR):**
    *   Merge til `main` sker udelukkende via Pull Requests.
    *   GitHub Actions CI-pipeline skal være grøn før merge.

## Kodestil
*   Følg standard Java naming conventions (camelCase til variabler/metoder, PascalCase til klasser).
*   Sørg for at koden er formateret (Brug IntelliJ's `Ctrl+Alt+L` / `Cmd+Option+L`).
