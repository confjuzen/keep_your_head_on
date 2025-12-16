![Keep Your Head On](core/assets/logo.png)

# Keep Your Head On

A desktop game built with LibGDX. This repository contains two Gradle modules:

- core — game logic, screens, assets loading
- lwjgl3 — desktop launcher and packaging

Versioning and artifact names are driven by gradle.properties and build.gradle. Note: the runnable JAR is named with the Gradle `appName` (currently “Abomination”).

---

## English

### Overview

Keep Your Head On is a LibGDX desktop game. The `lwjgl3` module launches the app via `io.github.abomination.lwjgl3.Lwjgl3Launcher`. Assets are loaded from `assets/` at the repo root and from `core/assets/`.

### Prerequisites

- Java JDK 17+ (recommended; project compiles for Java 8 compatibility)
- Git
- Optional: GraalVM 23+ for native image (advanced)

### Quick Start

- Clone the repo
- Open as a Gradle project in your IDE, or use the Gradle wrapper

Run from terminal:

```bash
./gradlew :lwjgl3:run
```

Run from IDE:

- Set main class to `io.github.abomination.lwjgl3.Lwjgl3Launcher` in the `lwjgl3` module
- macOS first-thread requirement is handled automatically by the Gradle run task

### Building and Packaging

- Fat runnable JAR (all platforms):

```bash
./gradlew :lwjgl3:jar
# Output: lwjgl3/build/libs/Abomination-<version>.jar
java -jar lwjgl3/build/libs/Abomination-<version>.jar
```

- OS-focused JARs (smaller, exclude other natives):

```bash
./gradlew :lwjgl3:jarLinux
./gradlew :lwjgl3:jarWin
./gradlew :lwjgl3:jarMac
```

- Application distribution with scripts (Application plugin):

```bash
./gradlew :lwjgl3:installDist   # creates build/install/lwjgl3 with bin/ and lib/
./gradlew :lwjgl3:distZip       # zip distribution in build/distributions/
```

#### Optional: Native Executable (GraalVM)

1) Install GraalVM (23+), ensure `native-image` is available
2) Enable native build in gradle.properties:

```properties
enableGraalNative=true
```

3) Build native image:

```bash
./gradlew :lwjgl3:nativeCompile
# Optional to run: ./gradlew :lwjgl3:nativeRun
```

Note: Native builds are experimental and may require platform-specific setup.

### Project Structure

```text
.
├─ assets/                # Shared assets for desktop runtime
├─ core/
│  ├─ assets/             # Core assets packaged as resources
│  └─ src/main/java/...   # Game logic, screens, entities
├─ lwjgl3/
│  └─ src/main/java/...   # Desktop launcher (Lwjgl3Launcher)
└─ gradle.properties      # gdxVersion, projectVersion, enableGraalNative, etc.
```

- The root `assets/` folder is added to the desktop runtime classpath.
- The `core/assets/` folder is packaged as resources by the `core` module.
- An `assets.txt` file is auto-generated during builds to aid resource packaging.

### UML Diagrams

- **Core Class Diagram (Mermaid)**
  - docs/uml/core-class-diagram.md
- **Enemy Spawning Sequence (Mermaid)**
  - docs/uml/enemy-spawn-sequence.md

Notes:
- GitHub renders Mermaid diagrams in Markdown. Open these files on GitHub to view the diagrams.
- Locally, use a Markdown viewer with Mermaid support (or VS Code with the “Markdown Preview Mermaid Support” extension).

### Versioning and Naming

- Set the release number in `gradle.properties` via `projectVersion`
- JAR names derive from `appName` (set to “Abomination” in the root `build.gradle` subprojects block)
- To rename artifacts, change `ext.appName` and bump `projectVersion`

### Contribution Guide

- Branching
  - Create feature branches from `main` (e.g., `feat/lane-balancing`)
  - Keep PRs focused and small when possible
- Commit Messages
  - Prefer Conventional Commits style: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`
- Code Style
  - Packages use `io.github.abomination.*`
  - Favor clear separation: screens/UI in `core`, desktop bootstrap in `lwjgl3`
  - Avoid global mutable state; prefer passing dependencies explicitly
- Assets
  - Put runtime assets under `assets/` (desktop) or `core/assets/` (packaged)
  - Keep filenames stable; the build generates an `assets.txt` index
- Submitting a PR
  - Run the game locally (`:lwjgl3:run`) and ensure no regressions
  - Update README/docs if behavior or setup changes
  - Add screenshots/GIFs for UI changes when relevant

### Testing and Coverage

- **What’s included**
- Unit tests with JUnit 5 (Jupiter)
- Code coverage via JaCoCo (HTML/XML reports)
- A local script to run tests and print a coverage summary
- A CI workflow that runs tests automatically on push/PR

- **One command (recommended):**
  
```bash
chmod +x scripts/run_tests.sh
./scripts/run_tests.sh
```

- This will:
- Clean the build
- Run all unit tests across modules
- Generate JaCoCo coverage reports
- Print a per-module line coverage summary and point you to HTML report paths

- **Manual Gradle commands:**
  
```bash
./gradlew test jacocoTestReport
```

- **Where to find results (core module):**
- Test report (HTML): `core/build/reports/tests/test/index.html`
- Coverage report (HTML): `core/build/reports/jacoco/test/html/index.html`
- Coverage (XML): `core/build/reports/jacoco/test/jacocoTestReport.xml`

- **Notes for IDE users:**
- Refresh/Reload the Gradle project to ensure tests appear under the `core` module
- The project uses JDK 17+; make sure your IDE Gradle JVM is set to 17+

- **Continuous Integration (GitHub Actions):**
- Workflow file: `.github/workflows/tests.yml`
- Triggers: on push to `main`/`master`, on pull requests, and manually via “Run workflow”
- Artifacts uploaded:
- `**/build/reports/tests/test` (test reports)
- `**/build/reports/jacoco/**` (coverage reports)
- View them in the Actions tab → the run → “Artifacts”

- **What’s being tested (examples):**
- `PlayerClassTest` verifies class strengths/relations and random selection constraints
- `GameStateTest` checks money updates, clamping at zero, level progression, and enemy rate multiplier defaults
- `UnitTest` validates core damage clamping and position setters via a tiny `DummyUnit`

- **Troubleshooting tests:**
- If tests aren’t discovered, try `./gradlew clean test --info`
- Ensure internet access for the Gradle first-time dependency download
- On CI failures, download artifacts to inspect HTML reports

### API Documentation (Javadoc)

- **One command (recommended):**
  
```bash
chmod +x scripts/gen_docs.sh
./scripts/gen_docs.sh
```

- **What it does**
- Runs Gradle `javadoc` for each module and an aggregated `javadocAll` task
- Disables doclint and does not fail the build on missing docs (so it always produces output)
- Prints where to open the generated documentation
  
- **Manual Gradle commands:**
  
```bash
./gradlew :core:javadoc :lwjgl3:javadoc javadocAll
```

- **Where to open the docs:**
- Aggregated site: `build/docs/javadoc-all/index.html`
- Core module: `core/build/docs/javadoc/index.html`
- Desktop module: `lwjgl3/build/docs/javadoc/index.html`
  
- **Continuous Integration (GitHub Actions):**
- The CI workflow also generates Javadoc and uploads it as the `javadoc` artifact
- Paths included: aggregated `build/docs/javadoc-all`, plus per-module Javadoc directories

### Troubleshooting

- macOS window doesn’t open: the run task sets `-XstartOnFirstThread` automatically
- JAR won’t run: ensure you’re using Java 17+ (`java -version`), then `java -jar ...`
- Missing assets at runtime: verify files live under `assets/` (root) and are tracked by Git
- Very large JAR: use OS-specific JAR tasks (`jarLinux`, `jarMac`, `jarWin`) to reduce size

---

## Français

### Présentation

Keep Your Head On est un jeu desktop basé sur LibGDX. Le module `lwjgl3` lance l’application via `io.github.abomination.lwjgl3.Lwjgl3Launcher`. Les ressources sont chargées depuis `assets/` (racine) et `core/assets/`.

### Prérequis

- Java JDK 17+ (recommandé ; compatibilité de compilation Java 8)
- Git
- Optionnel : GraalVM 23+ pour un exécutable natif

### Démarrage rapide

- Cloner le dépôt
- Ouvrir le projet avec Gradle dans l’IDE, ou utiliser le wrapper Gradle

Exécuter depuis le terminal :

```bash
./gradlew :lwjgl3:run
```

Exécuter depuis l’IDE :

- Définir la classe principale `io.github.abomination.lwjgl3.Lwjgl3Launcher` (module `lwjgl3`)
- Sur macOS, le premier thread est géré automatiquement par la tâche Gradle

### Construction et Packaging

- JAR exécutable (toutes plateformes) :

```bash
./gradlew :lwjgl3:jar
# Sortie : lwjgl3/build/libs/Abomination-<version>.jar
java -jar lwjgl3/build/libs/Abomination-<version>.jar
```

- JAR ciblé par OS (plus léger) :

```bash
./gradlew :lwjgl3:jarLinux
./gradlew :lwjgl3:jarWin
./gradlew :lwjgl3:jarMac
```

- Distribution avec scripts (plugin Application) :

```bash
./gradlew :lwjgl3:installDist   # crée build/install/lwjgl3 avec bin/ et lib/
./gradlew :lwjgl3:distZip       # archive zip dans build/distributions/
```

#### Optionnel : Binaire natif (GraalVM)

1) Installer GraalVM (23+), avec `native-image`
2) Activer dans `gradle.properties` :

```properties
enableGraalNative=true
```

3) Compiler :

```bash
./gradlew :lwjgl3:nativeCompile
# Exécution facultative : ./gradlew :lwjgl3:nativeRun
```

Attention : la compilation native est expérimentale et peut exiger une configuration spécifique à la plateforme.

### Structure du projet

```text
.
├─ assets/                # Ressources partagées pour l’exécution desktop
├─ core/
│  ├─ assets/             # Ressources packagées avec le module core
│  └─ src/main/java/...   # Logique de jeu, écrans, entités
├─ lwjgl3/
│  └─ src/main/java/...   # Lanceur desktop (Lwjgl3Launcher)
└─ gradle.properties      # gdxVersion, projectVersion, enableGraalNative, etc.
```

- Le dossier `assets/` (racine) est ajouté au classpath de l’application desktop
- Le dossier `core/assets/` est packagé comme ressources du module core
- Un fichier `assets.txt` est généré automatiquement pendant la build

### Versionning et Nom des Artifacts

- Définir la version dans `gradle.properties` via `projectVersion`
- Les noms des JAR dépendent de `appName` (actuellement “Abomination” dans `build.gradle`)
- Pour renommer, changer `ext.appName` et incrémenter `projectVersion`

### Guide de Contribution

- Branches
  - Créer des branches à partir de `main` (ex. `feat/systeme-ennemis`)
  - Favoriser des PRs petites et ciblées
- Messages de commit
  - Utiliser le style Conventional Commits : `feat:`, `fix:`, `refactor:`, `docs:`, `chore:`
- Style de code
  - Espace de nommage `io.github.abomination.*`
  - Séparer clairement : écrans/UX dans `core`, bootstrap desktop dans `lwjgl3`
  - Éviter l’état global mutable ; passer les dépendances explicitement
- Ressources
  - Mettre les assets dans `assets/` (desktop) ou `core/assets/` (packagé)
  - Garder des noms de fichiers stables ; la build génère un index `assets.txt`
- Ouvrir une PR
  - Tester en local (`:lwjgl3:run`) et éviter toute régression
  - Mettre à jour la documentation si besoin
  - Ajouter des captures/vidéos pour les changements d’UI si pertinent

### Dépannage

- Fenêtre macOS qui ne s’ouvre pas : `-XstartOnFirstThread` est géré par la tâche Gradle
- Impossible d’exécuter le JAR : vérifier Java 17+ (`java -version`), puis `java -jar ...`
- Ressources manquantes : vérifier que les fichiers sont bien dans `assets/` (racine)
- JAR volumineux : utiliser les tâches OS spécifiques (`jarLinux`, `jarMac`, `jarWin`)

---

Made with LibGDX. Contributions welcome!

