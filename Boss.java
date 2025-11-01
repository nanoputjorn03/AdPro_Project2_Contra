package se233.adpro2.model;

import se233.adpro2.core.Entity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import java.util.List;

/**
 * Base class for all bosses.
 * Provides common Entity fields (x, y, width, height, scale)
 * and standardizes boss interface methods.
 */
public abstract class Boss implements Entity {

    // Core position (x = left, y = feet/baseline)
    protected double x, y;

    // Natural sprite dimensions (1x scale = 100%)
    protected double spriteW = 100;
    protected double spriteH = 100;

    // Scale factor for rendering (1.0 = 100% of sprite size)
    protected double scale = 1.0;

    // HP tracking for all bosses
    protected int hp;
    protected int maxHp;

    public Boss(double x, double y, double spriteW, double spriteH, int maxHp) {
        this.x = x;
        this.y = y;
        this.spriteW = spriteW;
        this.spriteH = spriteH;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    /** Called every frame to update boss behavior. */
    public abstract void updateAI(double dt, Player player, List<Bullet> enemyBullets, double W, double H);

    /** Called every frame to render the boss. */
    public abstract void draw(GraphicsContext gc);

    /** Returns the weak hitbox of the boss. */
    public abstract Rectangle2D getWeakRect();

    // --- Shared boss mechanics ---
    @Override
    public double getX() { return x; }

    @Override
    public double getY() { return y; }

    @Override
    public double getW() { return spriteW * scale; }

    @Override
    public double getH() { return spriteH * scale; }

    /** Returns true if HP > 0 */
    public boolean isAlive() { return hp > 0; }

    /** Apply damage to the boss */
    public void hit(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    /** HP string for HUD */
    public String hpText() {
        return hp + " / " + maxHp;
    }

    /** Set new render scale */
    public void setScale(double s) {
        this.scale = Math.max(0.05, s);
    }

    /** Get current render scale */
    public double getScale() {
        return scale;
    }
}
