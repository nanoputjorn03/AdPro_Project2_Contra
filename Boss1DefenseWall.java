package se233.adpro2.model;

import se233.adpro2.core.Damageable;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.Random;

public class Boss1DefenseWall extends Boss implements Damageable {

    private final double scale = 2.5;
    private int hp = 30;
    private final int maxHP = 30;
    private double shootTimer = 0;
    private final Random rand = new Random();

    public Boss1DefenseWall(double x, double y) {
        // keep the same logical size/hitbox, even if we don't render a sprite
        super(x, y, 48, 64, 30);
        setScale(scale);
    }

    @Override
    public void updateAI(double dt, Player player, List<Bullet> enemyBullets, double W, double H) {
        shootTimer -= dt;

        if (shootTimer <= 0) {
            shootTimer = Math.max(0.8, 1.8 * hp / (double) maxHP);

            double px = player.getX() + player.getW() * 0.5;
            double py = player.getY() - player.getH() * 0.5;
            double bx = x + getW() * 0.15;
            double by = y - getH() * 0.55;

            double dx = px - bx;
            double dy = py - by;
            double dist = Math.sqrt(dx * dx + dy * dy);
            double spd = 220;

            enemyBullets.add(Bullet.enemy(bx, by, (dx / dist) * spd, (dy / dist) * spd));
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        double barW = getW() * 0.9, barH = 6;
        double barX = x + (getW() - barW) / 2;
        double barY = (y - getH()) - 10;

        gc.setFill(Color.DARKRED);
        gc.fillRect(barX, barY, barW, barH);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(barX, barY, barW * (hp / (double) maxHP), barH);
    }

    @Override
    public Rectangle2D getWeakRect() {
        // keep the same weak spot logic
        double ww = getW() * 0.25, wh = getH() * 0.25;
        double wx = x + getW() * 0.38;
        double wy = (y - getH()) + getH() * 0.50;
        return new Rectangle2D(wx, wy, ww, wh);
    }

    @Override public boolean isAlive() { return hp > 0; }
    @Override public void hit(int dmg) { hp = Math.max(0, hp - dmg); }
    @Override public String hpText() { return hp + " / " + maxHP; }
}
