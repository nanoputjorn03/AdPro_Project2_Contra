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

public class Boss2Java extends Boss implements Damageable {

    private final double scale = 2.2;
    private int hp = 45;
    private final int maxHP = 45;
    private double shootTimer = 0, jumpTimer = 0;
    private final Random rand = new Random();

    private double vx = 100, vy = 0;
    private boolean onGround = true;
    private final double gravity = 600;
    private final double leftBound = 420, rightBound = 900, groundY = 420;

    private final SpriteSheet sheet;
    private final AnimatedSprite idleAnim, moveAnim, attackAnim, damageAnim;
    private AnimatedSprite currentAnim;

    public Boss2Java(double x, double y) {
        super(x, y, 64, 64, 45);
        setScale(scale);

        sheet = new SpriteSheet(Main.class.getResource("/assets/sprites/BossJava.png").toExternalForm(), 81, 84);
        idleAnim   = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        moveAnim   = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        attackAnim = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        damageAnim = new AnimatedSprite(sheet, 0, new int[]{0}, 1);
        currentAnim = idleAnim;
    }

        @Override
    public void updateAI(double dt, Player player, List<Bullet> enemyBullets, double W, double H) {
        x += vx * dt;
        if (x < leftBound) { x = leftBound; vx = Math.abs(vx); }
        if (x + getW() > rightBound) { x = rightBound - getW(); vx = -Math.abs(vx); }

        jumpTimer -= dt;
        if (jumpTimer <= 0 && onGround && rand.nextDouble() < 0.02) {
            vy = -380;
            onGround = false;
            jumpTimer = 1.5 + rand.nextDouble();
        }
        if (!onGround) {
            vy += gravity * dt;
            y += vy * dt;
            if (y >= groundY) { y = groundY; vy = 0; onGround = true; }
        }

        shootTimer -= dt;
        if (shootTimer <= 0) {
            shootTimer = Math.max(0.7, 1.4 * (hp / (double) maxHP));
            performAttack(player, enemyBullets);
            currentAnim = attackAnim;
        } else currentAnim = onGround ? moveAnim : idleAnim;

        currentAnim.setFlipX(vx < 0);
        currentAnim.update(dt);
    }

    private void performAttack(Player player, List<Bullet> enemyBullets) {
        double bx = x + getW() * 0.5;
        double by = (y - getH()) + getH() * 0.45;
        double px = player.getX() + player.getW() * 0.5;
        double py = player.getY() - player.getH() * 0.5;
        double dx = px - bx, dy = py - by;
        double base = Math.atan2(dy, dx);
        double spd = 280;
        for (int i = -2; i <= 2; i++) {
            double ang = base + Math.toRadians(i * 6);
            enemyBullets.add(Bullet.enemy(bx, by, Math.cos(ang) * spd, Math.sin(ang) * spd));
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
        double ww = getW() * 0.3, wh = getH() * 0.35;
        double wx = x + getW() * 0.35;
        double wy = (y - getH()) + getH() * 0.35;
        return new Rectangle2D(wx, wy, ww, wh);
    }

    @Override public boolean isAlive() { return hp > 0; }
    @Override public void hit(int dmg) { hp = Math.max(0, hp - dmg); currentAnim = damageAnim; }
    @Override public String hpText() { return hp + " / " + maxHP; }
}
