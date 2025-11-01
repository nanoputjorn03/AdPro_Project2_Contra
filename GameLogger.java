package se233.adpro2.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file logger for game events (movement, hits, deaths)
 * Logs are saved under logs/game_log.txt
 */
public class GameLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_PATH = LOG_DIR + "/game_log.txt";
    private static final String HIGH_SCORE_PATH = LOG_DIR + "/highscore.txt";
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String event) {
        try {
            Files.createDirectories(Paths.get(LOG_DIR)); // ✅ ensures /logs exists
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_PATH, true))) {
                String time = LocalDateTime.now().format(fmt);
                bw.write("[" + time + "] " + event);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    public static synchronized void saveHighScore(int score) {
        try {
            Files.createDirectories(Paths.get(LOG_DIR)); // ✅ ensures /logs exists
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(HIGH_SCORE_PATH, false))) {
                String time = LocalDateTime.now().format(fmt);
                bw.write("High Score: " + score + " | Date: " + time);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to write high score: " + e.getMessage());
        }
    }
}
