# My Sudoku App 🧩

Eine moderne Sudoku-App für Android, entwickelt mit **Jetpack Compose** und nach der **MVVM-Architektur**. Das Design und die Funktionalität sind an das klassische Microsoft Sudoku angelehnt.

## 🚀 Features

*   **Smart Notes:** Beim Eintragen einer Zahl werden Notizen in derselben Zeile, Spalte und im 3x3-Block automatisch gelöscht.
*   **Fehler-Validierung:** Zahlen, die gegen Sudoku-Regeln verstoßen (Duplikate in Zeile, Spalte oder Block), werden sofort rot markiert.
*   **Highlighting:** Beim Auswählen einer Zelle oder einer Zahl werden alle identischen Werte auf dem Board hervorgehoben.
*   **Zahlen-Counter:** Über den Eingabe-Buttons wird angezeigt, wie oft jede Zahl (1-9) noch platziert werden muss. Ein grünes Häkchen erscheint, wenn eine Zahl vollständig ist.
*   **Zwei Eingabe-Modi:**
    *   *Zelle zuerst:* Wähle eine leere Zelle und dann die Zahl.
    *   *Zahl zuerst (Fast Input):* Wähle eine Zahl (wird orange markiert) und tippe dann nacheinander auf verschiedene Zellen.
*   **Notiz-Modus:** Schalte mit dem Bleistift-Symbol um, um kleine Hinweiszahlen (Notizen) in Zellen zu setzen.
*   **Undo-Funktion:** Mache deine letzten Spielzüge mit dem Rückgängig-Button einfach rückgängig.

## 🛠 Tech Stack

*   **Sprache:** Kotlin
*   **UI-Framework:** Jetpack Compose
*   **Architektur:** MVVM (Model-View-ViewModel)
*   **State Management:** StateFlow & Unidirectional Data Flow (UDF)
*   **Icons:** Material Symbols (Extended Pack)

## 📂 Projektstruktur

*   `model/`: Datenklassen wie `SudokuCell`.
*   `ui/`: UI-Komponenten (`SudokuGameScreen`) und das `SudokuViewModel`.
*   `MainActivity.kt`: Einstiegspunkt der App.

## ⚙️ Installation

1.  Klone das Repository.
2.  Öffne das Projekt in **Android Studio (Ladybug oder neuer)**.
3.  Führe einen **Gradle Sync** aus.
4.  Starte die App auf einem Emulator oder einem physischen Android-Gerät.

---
*Entwickelt als Lernprojekt für sauberen Kotlin-Code und moderne Android-Entwicklung.*
