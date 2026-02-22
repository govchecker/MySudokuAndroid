# My Sudoku App 🧩

Eine moderne Sudoku-App für Android, entwickelt mit **Jetpack Compose** und nach der **MVVM-Architektur**. Das Design und die Funktionalität sind an das klassische Microsoft Sudoku angelehnt, ergänzt um einen intelligenten Logik-Generator.

## 🚀 Features

*   **Logik-Garantie:** Jedes Rätsel ist ohne Raten (Brute-Force) und nur mit logischen Strategien lösbar.
*   **Automatisches Speichern & Fortsetzen:** Der Spielstand (Grid, Timer, Schwierigkeit) wird im Hintergrund gesichert und beim nächsten App-Start nahtlos geladen.
*   **Schwierigkeitsgrade:** Wähle zwischen *Easy*, *Medium* und *Hard*. Die App merkt sich deine letzte Wahl.
*   **Smart Notes:** Beim Eintragen einer Zahl werden Notizen in derselben Zeile, Spalte und im 3x3-Block automatisch gelöscht.
*   **Fehler-Validierung:** Zahlen, die gegen Sudoku-Regeln verstoßen (Duplikate oder falsche Lösung), werden sofort markiert.
*   **Highlighting:** Beim Auswählen einer Zelle oder einer Zahl werden alle identischen Werte auf dem Board hervorgehoben.
*   **Zahlen-Counter:** Zeigt an, wie oft jede Zahl noch platziert werden muss, inkl. Erfolgs-Check.
*   **Zwei Eingabe-Modi:** Unterstützt sowohl "Zelle zuerst" als auch "Zahl zuerst" (Fast Input).
*   **Notiz-Modus:** Setze kleine Hinweiszahlen manuell oder nutze die **Auto-Fill** Funktion.
*   **Undo-Funktion:** Mache deine letzten Spielzüge unbegrenzt rückgängig.

## 🧠 Logik & Generierung

Das Herzstück der App ist der **Human-Style Solver**. Im Gegensatz zu einfachen Generatoren prüft dieser bei der Erstellung, ob das Rätsel mit menschlichen Techniken lösbar bleibt:

*   **Naked Singles:** Die einfachste Form der Herleitung.
*   **Hidden Singles:** Findet Zahlen, die in einer Einheit nur an einem Ort stehen können.
*   **Pointing Pairs:** Erkennt Ausschlusskriterien durch Interaktion von Blöcken und Linien.

Der Algorithmus entfernt eine Zahl nur dann endgültig, wenn der Solver das Rätsel weiterhin ohne Sackgassen lösen kann. Dies verhindert, dass der Spieler jemals raten muss.

## 🛠 Tech Stack

*   **Sprache:** Kotlin
*   **UI-Framework:** Jetpack Compose
*   **Architektur:** MVVM + AndroidViewModel
*   **State Management:** StateFlow & Unidirectional Data Flow (UDF)
*   **Persistenz:** SharedPreferences mit JSON-Serialisierung
*   **Icons:** Material Symbols (Extended Pack)

## 📂 Projektstruktur

*   `model/`: 
    *   `SudokuGenerator.kt`: Erstellt valide Grids und dünnt sie logisch aus.
    *   `SudokuLogicSolver.kt`: Simuliert menschliche Lösungswege.
    *   `SudokuCell.kt`: Datenmodell für eine einzelne Zelle.
*   `ui/`:
    *   `SudokuViewModel.kt`: Verwaltet den State, Timer und die Persistenz.
    *   `SudokuGameScreen.kt`: Die gesamte UI-Logik in Compose.
*   `MainActivity.kt`: Einstiegspunkt der App.

## ⚙️ Installation

1.  Klone das Repository.
2.  Öffne das Projekt in **Android Studio (Ladybug oder neuer)**.
3.  Führe einen **Gradle Sync** aus.
4.  Starte die App auf einem Emulator oder einem physischen Android-Gerät.

---
*Entwickelt als Lernprojekt für sauberen Kotlin-Code und moderne Android-Entwicklung.*
