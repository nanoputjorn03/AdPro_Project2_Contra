package se233.adpro2;

import se233.adpro2.exceptions.GameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreManagerTest {

    @Test
    void addPoints_ok_doesNotThrow() {
        ScoreManager s = new ScoreManager();
        assertDoesNotThrow(() -> {
            s.add(2);
            s.add(3);
        });
        assertEquals(5, s.get());
    }

    @Test
    void addPoints_negative_throwsGameException() {
        ScoreManager s = new ScoreManager();
        assertThrows(GameException.class, () -> s.add(-1));
    }
}
