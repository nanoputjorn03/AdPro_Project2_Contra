package se233.adpro2.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import se233.adpro2.Main; // ✅ added for getResource()

import java.util.Random;

public class EnemyMinion {

    private double x, y;
    private double vx, vy;
    private final double speed = 80;
    private final double w = 32;
    private final double h = 40;

    private boolean alive = true;
    private final Random rand = new Random();

    // ✅ added sprite image
    private final Image sprite = new Image(
            Main.class.getResource("/assets/sprites/EnemyMinion.png").toExternalForm()
    );

    public EnemyMinion(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.vx = 0;
        this.vy = 0;
    }

    public void update(double dt, Player player) {
        if (!alive) return;
        double px = player.getX() + player.getW() / 2;
        double py = player.getY() - player.getH() / 2;
        double dx = px - (x + w / 2);
        double dy = py - (y - h / 2);
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.01) {
            vx = (dx / dist) * speed;
            vy = (dy / dist) * speed;
        }

        x += vx * dt;
        y += vy * dt;
    }

    public void draw(GraphicsContext gc) {
        if (!alive) return;

        // ✅ Draw image first
        gc.drawImage(sprite, x, y - h, w, h);

    }

    public Rectangle2D getRect() {
        return new Rectangle2D(x, y - h, w, h);
    }

    public boolean isAlive() { return alive; }
    public void kill() { alive = false; }
}
