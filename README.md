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
