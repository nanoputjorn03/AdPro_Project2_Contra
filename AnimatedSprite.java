package se233.adpro2.util;

import javafx.scene.canvas.GraphicsContext;

/**
 * Simple row-based animation (all frames come from the same spritesheet row).
 * - Clamps invalid row indices
 * - Filters out-of-range columns
 * - Uses SpriteSheet.drawFrame (no per-frame allocations)
 * - Draws using bottom-baseline coordinates (x, y is sprite's bottom-left)
 */
public class AnimatedSprite {

    private final SpriteSheet sheet;
    private int row;            // clamped to [0, sheet.rows)
    private int[] cols;         // filtered to valid [0, sheet.cols)
    private final double fps;   // frames per second (>= 1)
    private final double frameTime;

    private double t = 0.0;
    private int idx = 0;
    private boolean flipX = false;

    /**
     * @param sheet spritesheet
     * @param row   row index in spritesheet (0-based)
     * @param cols  list of column indices (frames) for this animation
     * @param fps   frames per second (if <= 0, defaults to 1)
     */
    public AnimatedSprite(SpriteSheet sheet, int row, int[] cols, double fps) {
        this.sheet = sheet;

        // Clamp row
        int maxRows = Math.max(1, sheet.getRows());
        if (row < 0 || row >= maxRows) {
            int clamped = Math.max(0, Math.min(row, maxRows - 1));
            System.err.printf("[AnimatedSprite] Row %d out of range (rows=%d). Clamping to %d%n",
                    row, maxRows, clamped);
            this.row = clamped;
        } else {
            this.row = row;
        }

        // Filter columns
        this.cols = filterCols(cols, Math.max(1, sheet.getCols()));
        if (this.cols.length == 0) {
            // Fallback to column 0 if all invalid
            System.err.printf("[AnimatedSprite] All columns out of range for row=%d (cols=%d). Using [0].%n",
                    this.row, sheet.getCols());
            this.cols = new int[]{0};
        }

        this.fps = (fps <= 0) ? 1.0 : fps;
        this.frameTime = 1.0 / this.fps;
    }

    /** Advance the animation clock. */
    public void update(double dt) {
        t += dt;
        while (t >= frameTime) {
            t -= frameTime;
            idx = (idx + 1) % cols.length;
        }
    }

    /** Restart animation at first frame. */
    public void reset() {
        t = 0;
        idx = 0;
    }

    /** Set horizontal flip. */
    public void setFlipX(boolean flip) {
        this.flipX = flip;
    }

    /**
     * Draw current frame.
     * @param gc    graphics
     * @param x     bottom-left x (baseline)
     * @param y     bottom baseline y (feet)
     * @param scale scale factor
     */
    public void draw(GraphicsContext gc, double x, double y, double scale) {
        if (cols.length == 0) return;

        final int col = cols[idx];
        final double w = sheet.getFrameWidth() * scale;
        final double h = sheet.getFrameHeight() * scale;

        // Convert to top-left for drawing
        final double drawX = x;
        final double drawY = y - h;

        if (!flipX) {
            sheet.drawFrame(gc, row, col, drawX, drawY, scale);
        } else {
            gc.save();
            // Mirror horizontally around the vertical line at x
            // After scaling by -1 on X, the new X for top-left becomes -(x + w)
            gc.scale(-1, 1);
            sheet.drawFrame(gc, row, col, -drawX - w, drawY, scale);
            gc.restore();
        }
    }

    // --- helpers ---

    private static int[] filterCols(int[] in, int maxCols) {
        if (in == null || in.length == 0) return new int[]{0};
        return java.util.Arrays.stream(in)
                .filter(c -> c >= 0 && c < maxCols)
                .toArray();
    }

    // Optional runtime tuning if you need it later:

    /** Change row at runtime (clamped). Resets animation to first frame. */
    public void setRow(int newRow) {
        int maxRows = Math.max(1, sheet.getRows());
        if (newRow < 0 || newRow >= maxRows) {
            System.err.printf("[AnimatedSprite] setRow: %d out of range (rows=%d). Ignored.%n", newRow, maxRows);
            return;
        }
        this.row = newRow;
        reset();
    }

    /** Change columns at runtime (filtered). Resets animation to first frame. */
    public void setCols(int[] newCols) {
        int[] f = filterCols(newCols, Math.max(1, sheet.getCols()));
        if (f.length == 0) {
            System.err.println("[AnimatedSprite] setCols: all out of range. Keeping previous.");
            return;
        }
        this.cols = f;
        reset();
    }
    public int getFrameWidth()  { return sheet.getFrameWidth(); }
    public int getFrameHeight() { return sheet.getFrameHeight(); }
}
