package se233.adpro2.util;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Grid-based sprite sheet helper with safe bounds handling.
 */
public class SpriteSheet {
    private final Image image;
    private final int frameWidth;
    private final int frameHeight;
    private final int cols;
    private final int rows;

    /**
     * Loads a spritesheet from a file path or URL.
     *
     * @param uri          path or URI (e.g. "file:assets/sprites/sheet.png")
     * @param frameWidth   width of each frame in pixels
     * @param frameHeight  height of each frame in pixels
     */
    public SpriteSheet(String uri, int frameWidth, int frameHeight) {
        this.image = new Image(uri, false);
        if (image.isError()) {
            System.err.println("[SpriteSheet] Failed to load: " + uri + " :: " + image.getException());
        }
        this.frameWidth = Math.max(1, frameWidth);
        this.frameHeight = Math.max(1, frameHeight);

        int iw = (int) Math.max(1, Math.floor(image.getWidth()));
        int ih = (int) Math.max(1, Math.floor(image.getHeight()));
        int computedCols = iw / this.frameWidth;
        int computedRows = ih / this.frameHeight;

        if (computedCols <= 0 || computedRows <= 0) {
            System.err.printf(
                    "[SpriteSheet] Warning: invalid sheet grid for %s (img=%dx%d, frame=%dx%d). " +
                            "Forcing 1x1 grid. Check your frame size or image path.%n",
                    uri, iw, ih, this.frameWidth, this.frameHeight
            );
            computedCols = 1;
            computedRows = 1;
        }

        this.cols = computedCols;
        this.rows = computedRows;
    }

    /**
     * Returns a single frame from the spritesheet at (col,row).
     * Out-of-range indices are clamped to the valid range to avoid crashes.
     *
     * @param col  column index (0-based)
     * @param row  row index (0-based)
     * @return     cropped frame image (never null)
     */
    public Image frame(int col, int row) {
        int c = clamp(col, 0, cols - 1);
        int r = clamp(row, 0, rows - 1);

        if (c != col || r != row) {
            System.err.printf("[SpriteSheet] frame() clamped out-of-range request col=%d row=%d -> col=%d row=%d (cols=%d, rows=%d)%n",
                    col, row, c, r, cols, rows);
        }

        return new WritableImage(
                image.getPixelReader(),
                c * frameWidth,
                r * frameHeight,
                frameWidth,
                frameHeight
        );
    }

    /**
     * Draw a specific frame to the canvas at top-left (x,y) with the given scale.
     * Out-of-range indices are clamped safely.
     */
    public void drawFrame(GraphicsContext gc, int row, int col, double x, double y, double scale) {
        int r = clamp(row, 0, rows - 1);
        int c = clamp(col, 0, cols - 1);

        double sx = c * frameWidth;
        double sy = r * frameHeight;
        double dw = frameWidth * scale;
        double dh = frameHeight * scale;

        gc.drawImage(image, sx, sy, frameWidth, frameHeight, x, y, dw, dh);
    }

    private static int clamp(int v, int lo, int hi) {
        return (v < lo) ? lo : (v > hi ? hi : v);
    }

    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public Image getImage() { return image; }
}
