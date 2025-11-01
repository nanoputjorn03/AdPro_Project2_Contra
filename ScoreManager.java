package se233.adpro2;

import se233.adpro2.exceptions.GameException;

public class ScoreManager {
    private int score;

    /**
     * Add points to the score.
     * Automatically handles invalid inputs with fallback.
     */
    public void add(int pts) throws GameException {
        if (pts < 0) {
            throw new GameException("Negative score increment: " + pts);
        }
        score += pts;
    }

    /** Returns the current score. */
    public int get() {
        return score;
    }

    /** Resets score to zero. */
    public void reset() {
        score = 0;
    }
}
