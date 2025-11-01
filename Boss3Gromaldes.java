package se233.adpro2.model;

import se233.adpro2.Main;
import se233.adpro2.core.Damageable;
import se233.adpro2.util.AnimatedSprite;
import se233.adpro2.util.SpriteSheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.Random;

public class Boss3Gromaldes extends Boss implements Damageable {

    private final double scale = 2.4;
    private int hp = 60;
    private final int maxHP = 60;
    private double shootTimer = 0;
    private final Random rand = new Random();

    private double vx = 120;
    private final double leftBound = 420, rightBound = 900;

    private final SpriteSheet sheet;
    private final AnimatedSprite idleAnim, attackAnim, damageAnim;
    private AnimatedSprite currentAnim;

    public Boss3Gromaldes(double x, double y) {
        super(x, y, 96, 96, 60);
        setScale(scale);

        sheet = new SpriteSheet(Main.class.getResource("/assets/sprites/BossGromaldes.png").toExternalForm(), 188, 99);
        idleAnim   = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        attackAnim = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        damageAnim = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        currentAnim = idleAnim;

    }

    @Override
    public void updateAI(double dt, Player player, List<Bullet> enemyBullets, double W, double H) {
        currentAnim.update(dt);
        x += vx * dt;
        if (x < leftBound) { x = leftBound; vx = Math.abs(vx); }
        if (x + getW() > rightBound) { x = rightBound - getW(); vx = -Math.abs(vx); }

        shootTimer -= dt;
        if (shootTimer <= 0) {
            shootTimer = 1.2;
            double bx = x + getW() * 0.5;
            double by = y - getH() * 0.6;
            double px = player.getX() + player.getW() * 0.5;
            double py = player.getY() - player.getH() * 0.5;
            double dx = px - bx, dy = py - by;
            double base = Math.atan2(dy, dx);
            spawnSpread(enemyBullets, bx, by, base, 240, 18);
            currentAnim = attackAnim;
        } else currentAnim = idleAnim;
    }

    private void spawnSpread(List<Bullet> enemyBullets, double bx, double by, double base, double spd, double spreadDeg) {
        for (int i = -2; i <= 2; i++) {
            double a = base + Math.toRadians(i * spreadDeg / 5.0);
            enemyBullets.add(Bullet.enemy(bx, by, Math.cos(a) * spd, Math.sin(a) * spd));
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        currentAnim.draw(gc, x, y, getScale());
        double barW = getW() * 0.9, barH = 6;
        double barX = x + (getW() - barW) / 2, barY = (y - getH()) - 10;
        gc.setFill(Color.DARKRED);
        gc.fillRect(barX, barY, barW, barH);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(barX, barY, barW * (hp / (double) maxHP), barH);
    }

    @Override
    public Rectangle2D getWeakRect() {
        double ww = getW() * 0.4, wh = getH() * 0.4;
        double wx = x + getW() * 0.3;
        double wy = (y - getH()) + getH() * 0.4;
        return new Rectangle2D(wx, wy, ww, wh);
    }

    @Override public boolean isAlive() { return hp > 0; }
    @Override public void hit(int dmg) { hp = Math.max(0, hp - dmg); currentAnim = damageAnim; }
    @Override public String hpText() { return hp + " / " + maxHP; }
}
