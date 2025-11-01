package se233.adpro2.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet {
    private double x, y;
    private final double vx, vy;
    private final boolean playerOwned;
    private static final double W = 10, H = 4;

    private Bullet(double x, double y, double vx, double vy, boolean playerOwned) {
        this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.playerOwned = playerOwned;
    }

    public static Bullet player(double x, double y, double vx, double vy) {
        return new Bullet(x, y, vx, vy, true);
    }
    public static Bullet enemy(double x, double y, double vx, double vy) {
        return new Bullet(x, y, vx, vy, false);
    }

    public void step(double dt) {
        x += vx * dt;
        y += vy * dt;
    }

    public boolean outside(double Wc, double Hc) {
        return x < -50 || x > Wc + 50 || y < -50 || y > Hc + 50;
    }

    public Rectangle2D getRect() { return new Rectangle2D(x, y, W, H); }

    public void draw(GraphicsContext gc) {
        gc.setFill(playerOwned ? Color.YELLOW : Color.ORANGE);
        gc.fillRect(x, y, W, H);
    }
}
