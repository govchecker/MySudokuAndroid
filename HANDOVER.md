# 🤖 AI Handover & Development Context

Dieses Dokument dient als Gedächtnisstütze für neue KI-Sitzungen (z. B. mit ChatGPT, Claude oder GitHub Copilot), damit die Entwicklung der Sudoku-App nahtlos fortgesetzt werden kann.

---

## 📌 Projekt-Status: MySudoku (Android)

**Kontext:**
Moderne Sudoku-App für Android mit garantierter logischer Lösbarkeit und modernem Material 3 Design ("MH Sudoku").

**Technische Eckdaten:**
*   **Stack:** Kotlin (2.3.10), Jetpack Compose (BOM 2026.02.00), Material 3.
*   **Gradle:** 9.3.1 mit Version Catalogs (`libs.versions.toml`).
*   **Architektur:** MVVM mit `AndroidViewModel` und `StateFlow`.
*   **Persistenz:** Automatisches Speichern in `SharedPreferences` (JSON-Serialisierung via `org.json`).

## 🧠 Kern-Logik (Generator & Solver)

1.  **SudokuLogicSolver:** Ein "Human-Style Solver", der logische Techniken anwendet und Tipps generiert:
    *   `NAKED_SINGLE`, `HIDDEN_SINGLE`
    *   `POINTING_PAIRS` (Block-Linien-Interaktion)
    *   `NAKED_PAIR`, `X_WING` (für höhere Schwierigkeitsgrade)
2.  **SudokuGenerator:** 
    *   Erstellt erst ein volles Grid.
    *   Nutzt `removeDigitsSmart`: Entfernt Zahlen und prüft mit dem `LogicSolver`, ob das Rätsel auf der gewählten Schwierigkeit noch eindeutig lösbar ist.

## 📊 Schwierigkeitsgrade
*   **EASY:** 35 leer, nur Naked Singles nötig.
*   **MEDIUM:** 45 leer, Hidden Singles erlaubt.
*   **HARD:** 55 leer, erfordert Pointing Pairs.
*   **EXPERT:** 60 leer, nutzt Naked Pairs und X-Wing.

## 🛠 Aktueller Funktionsumfang
*   **Logik-Check:** Garantiert lösbare Rätsel ohne Raten.
*   **Tipp-System:** Logische Hinweise mit technischer Erklärung und visueller Markierung betroffener Zellen.
*   **Modernes UI ("MH Sudoku"):** CenterAlignedTopAppBar, Card-basiertes Grid, optimiertes Padding für Notizen (keine Überdeckung durch dicke Linien).
*   **Smart Notes:** Auto-Fill Funktion und automatische Bereinigung bei korrekten Eingaben.
*   **Persistence:** Auto-Save & Resume inkl. Timer und Spielstand.
*   **Clean Code:** Keine Warnungen im Inspector, aktuelle APIs (Compose, Gradle).

## 🚀 Nächste Schritte
*   [ ] **Block-Animationen:** Visuelle Effekte, wenn ein 3x3-Block, eine Reihe oder Spalte vervollständigt wird.
*   [ ] **Haptisches Feedback:** Vibration bei Fehlern oder erfolgreichen Eingaben.
*   [ ] **Swordfish-Technik:** Implementierung weiterer Experten-Logik im Solver.
*   [ ] **Dark Mode Polish:** Überprüfung der Kontraste im dunklen Design.

---
*Kopiere diesen Text in eine neue Session, um sofort weiterzuarbeiten.*
