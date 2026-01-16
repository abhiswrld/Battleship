# Battleship (Java Swing Edition)

A fully interactive, GUI-based implementation of the classic naval strategy game Battleship, built in Java. This project demonstrates core object-oriented programming concepts, event-driven user interfaces, and multidimensional array logic.

## Features

* **Interactive GUI:** Built using Java Swing, replacing console input with a clickable grid interface (JButtons) for a modern user experience.
* **Two-Phase Gameplay:** Distinct logic for the **Setup Phase** (ship placement with rotation) and **Battle Phase** (turn-based combat).
* **Fog of War Mechanics:** Implements a "Pass-and-Play" system where the opponent's ships are hidden, only revealing hits (Red) and misses (White).
* **Smart Validation:** Prevents illegal moves such as overlapping ships, out-of-bounds placement, or firing on the same coordinate twice.

## How to Run

1.  Ensure you have Java installed.
2.  Clone the repository:
    ```bash
    git clone [https://github.com/abhiswrld/Battleship.git](https://github.com/abhiswrld/Battleship.git)
    cd Battleship
    ```
3.  Compile the source code:
    ```bash
    javac Battleship.java
    ```
4.  Run the game:
    ```bash
    java Battleship
    ```

## Technical Highlights

* **Language:** Java (Swing & AWT libraries)
* **Data Structures:** Uses **2D char arrays** to track internal game state ('S' for Ship, 'X' for Hit, 'M' for Miss) separate from the visual layer.
* **Event Handling:** Utilizes `ActionListener` interfaces to handle grid clicks and dynamic state changes (switching between players and phases).
* **Algorithm Design:** Implements validation logic with $O(N)$ complexity to check for ship overlaps and boundary constraints before placement.
