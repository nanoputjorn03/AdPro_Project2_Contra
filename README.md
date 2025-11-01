# AdPro_Project2_Contra
Contra game (Boss fight ONLY)

How to Run:

Option 1 – IntelliJ IDEA

Open the project in IntelliJ.

Make sure your JavaFX SDK path is correct (e.g. C:\Java\javafx-sdk-25.0.1\lib).

Run Main.java directly.

Option 2 – Terminal
mvn clean compile
java --module-path "C:\Java\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml -jar target/AdPro2-1.0-SNAPSHOT.jar

How to Test:

Run all JUnit tests with:

mvn test

Or right-click any test class (e.g. ScoreManagerTest.java) in IntelliJ → Run Test.

PROJECT STRUCTURE:

AdPro2/
│
├── src/
│   ├── main/java/se233/adpro2/
│   │   ├── core/               # Interfaces: Entity, Movable, Damageable
│   │   ├── exceptions/         # Custom exception classes
│   │   ├── model/              # Player, Bosses, Bullets, Explosions, etc.
│   │   ├── util/               # Animation, SpriteSheet, and Logging tools
│   │   ├── Main.java           # Game entry point (Application class)
│   │
│   └── test/java/se233/adpro2/ # JUnit test classes
│       ├── ScoreManagerTest.java
│       ├── PlayerMovementTest.java
│       ├── BossDamageTest.java
│       └── PhysicsAndCollisionTest.java
│
├── logs/
│   ├── game_log.txt            # Records runtime events
│   └── highscore.txt           # Stores best score + date
│
├── resources/ (optional)
│   ├── images/                 # Sprite assets (Player, Bosses, Bullets)
│   ├── sounds/                 # Sound effects and background music
│
├── pom.xml                     # Maven dependencies (JavaFX, JUnit)
└── README.md

