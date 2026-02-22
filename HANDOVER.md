# 🤖 AI Handover & Development Context

Dieses Dokument dient als Gedächtnisstütze für neue KI-Sitzungen (z. B. mit ChatGPT, Claude oder GitHub Copilot), damit die Entwicklung der Sudoku-App nahtlos fortgesetzt werden kann.

---

## 📌 Projekt-Status: MySudoku (Android)

**Kontext:**
Moderne Sudoku-App für Android, die auf eine garantierte logische Lösbarkeit setzt (kein Raten erforderlich).

**Technische Eckdaten:**
*   **Stack:** Kotlin, Jetpack Compose, Material 3.
*   **Architektur:** MVVM mit `AndroidViewModel` und `StateFlow`.
*   **Persistenz:** Automatisches Speichern in `SharedPreferences` (JSON-Serialisierung via `org.json`).

## 🧠 Kern-Logik (Generator & Solver)

1.  **SudokuLogicSolver:** Ein "Human-Style Solver", der nur logische Techniken anwendet:
    *   `NAKED_SINGLE`: Nur ein Kandidat pro Zelle.
    *   `HIDDEN_SINGLE`: Kandidat kann in einer Einheit nur an einem Ort stehen.
    *   `POINTING_PAIRS`: Eliminierung durch Block-Linien-Interaktion.
2.  **SudokuGenerator:** 
    *   Erstellt erst ein volles Grid.
    *   Nutzt `removeDigitsSmart`: Entfernt Zahlen und prüft mit dem `LogicSolver`, ob das Rätsel auf der gewählten Schwierigkeit noch eindeutig lösbar ist. Falls nicht -> Rollback.

## 📊 Schwierigkeitsgrade (Difficulty.kt)
*   **EASY:** 35 leer, nur Naked Singles nötig.
*   **MEDIUM:** 45 leer, Hidden Singles erlaubt.
*   **HARD:** 55 leer, erfordert Pointing Pairs.

## 🛠 Aktueller Funktionsumfang
*   Logik-Check beim Generieren (keine Sackgassen).
*   Auto-Save & Resume beim App-Start.
*   Schwierigkeits-Dialog mit Highlighting der letzten Wahl.
*   Notiz-Modus inkl. Auto-Fill & automatischer Bereinigung bei korrekten Eingaben.
*   Undo-Funktion und Fehler-Validierung.
*   Timer-System und persistenter Spielstand.

## 🚀 Nächste Schritte
*   [ ] **Expert-Modus:** Implementierung von X-Wing oder Swordfish Techniken.
*   [ ] **Hint-System:** Tipps geben, die die genutzte Logik-Technik erklären.
*   [ ] **UI-Polish:** Animationen für ausgefüllte Blöcke und verbessertes haptisches Feedback.

---
*Kopiere diesen Text in eine neue Session, um sofort weiterzuarbeiten.*
