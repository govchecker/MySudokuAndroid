# My Sudoku App 🧩

Eine moderne Sudoku-App für Android, entwickelt mit **Jetpack Compose** und nach der **MVVM-Architektur**. Das Design und die Funktionalität sind an das klassische Microsoft Sudoku angelehnt, ergänzt um einen intelligenten Logik-Generator.

## 🚀 Features

*   **MH Sudoku Branding:** Modernes UI mit zentrierter Top-App-Bar und "MH Sudoku" Titel.
*   **Logik-Garantie:** Jedes Rätsel ist ohne Raten (Brute-Force) und nur mit logischen Strategien lösbar.
*   **Experten-Modus:** Neue Schwierigkeitsstufe *Expert* für Profis, die Techniken wie *Naked Pairs* oder *X-Wing* erfordert.
*   **Intelligentes Hinweis-System:** Erhalte logische Tipps, die nicht nur die Lösung verraten, sondern die dahinterliegende Technik erklären (z.B. Pointing Pairs, Hidden Singles).
*   **Visuelle Hilfe:** Tipps heben die betroffenen Zellen farblich hervor, um das Lernen neuer Strategien zu erleichtern.
*   **Automatisches Speichern & Fortsetzen:** Der Spielstand (Grid, Timer, Schwierigkeit) wird im Hintergrund gesichert und beim nächsten App-Start nahtlos geladen.
*   **Schwierigkeitsgrade:** Wähle zwischen *Easy*, *Medium*, *Hard* und *Expert*.
*   **Smart Notes:** Beim Eintragen einer Zahl werden Notizen in derselben Zeile, Spalte und im 3x3-Block automatisch gelöscht.
*   **Fehler-Validierung:** Zahlen, die gegen Sudoku-Regeln verstoßen (Duplikate oder falsche Lösung), werden sofort markiert.
*   **Modernes Material 3 Design:** Nutzt Card-Layouts, sanfte Elevation-Effekte und ein harmonisches Farbschema.
*   **Verbesserte Usability:** Notiz-Modus mit automatischer Bereinigung und optimiertem Padding für bessere Lesbarkeit auf allen Geräten.
*   **Zahlen-Counter:** Zeigt an, wie oft jede Zahl noch platziert werden muss.
*   **Zwei Eingabe-Modi:** Unterstützt sowohl "Zelle zuerst" als auch "Zahl zuerst" (Fast Input).
*   **Undo-Funktion:** Mache deine letzten Spielzüge unbegrenzt rückgängig.

## 🧠 Logik & Generierung

Das Herzstück der App ist der **Human-Style Solver**. Er prüft bei der Erstellung, ob das Rätsel mit menschlichen Techniken lösbar bleibt:

*   **Naked & Hidden Singles:** Die Basis jeder Lösung.
*   **Pointing Pairs & Triples:** Block-Linien-Interaktionen.
*   **Naked Pairs/Subsets:** Fortgeschrittene Eliminierung von Kandidaten.
*   **X-Wing:** Musterbasierte Reduktion für hohe Schwierigkeitsgrade.

## 🛠 Tech Stack

*   **Sprache:** Kotlin
*   **UI-Framework:** Jetpack Compose (Material 3)
*   **Architektur:** MVVM + AndroidViewModel
*   **State Management:** StateFlow & Unidirectional Data Flow (UDF)
*   **Persistenz:** SharedPreferences mit JSON-Serialisierung

---
*Entwickelt als Lernprojekt für sauberen Kotlin-Code und moderne Android-Entwicklung.*
