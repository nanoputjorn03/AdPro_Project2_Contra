package se233.adpro2.model;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Player {

    // --- Core physics ---
    private double x, y;
    private double vx = 0, vy = 0;
    private final double w = 40, h = 60;
    public static final double MOVE_SPEED = 240;
    private static final double GRAVITY = 800;
    private static final double JUMP_STRENGTH = -400;

    // --- State ---
    private boolean onGround = true;
    private boolean prone = false;
    private boolean facingLeft = false;
    private boolean jumping = false;
    private boolean shooting = false;

    // --- HP ---
    private int hp = 100;
    private final int maxHP = 100;
    private boolean invulnerable = false;

    // --- Visual (animation system) ---
    private final PlayableCharacter character;
    private se233.adpro2.util.AnimatedSprite currentAnim;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;

        this.character = PlayableCharacter.BILL();
        this.currentAnim = character.idle;
    }

    // --- Movement ---
    public void moveHorizontal(double move) {
        vx = move;
        if (move < 0) facingLeft = true;
        else if (move > 0) facingLeft = false;
    }

    public void setProne(boolean p) {
        prone = p;
    }

    public boolean isOnGround() { return onGround; }

    public void jump() {
        if (onGround) {
            vy = JUMP_STRENGTH;
            onGround = false;
            jumping = true;
        }
    }

    public void setShooting(boolean s) {
        shooting = s;
    }

    public void respawn(double rx, double ry) {
        x = rx; y = ry;
        vx = vy = 0;
        onGround = true;
        hp = maxHP;
    }

    // --- HP logic ---
    public int getHP() { return hp; }
    public void setHP(int value) { hp = Math.max(0, Math.min(value, maxHP)); }
    public void takeDamage(int dmg) { if (!invulnerable) hp = Math.max(0, hp - dmg); }
    public boolean isDead() { return hp <= 0; }
    public boolean isInvulnerable() { return invulnerable; }
    public void setInvulnerable(boolean v) { invulnerable = v; }

    // --- Physics ---
    public void applyPhysics(double dt, double groundY, List<Rectangle2D> solids, boolean downPressed) {
        vy += GRAVITY * dt;

        if (downPressed && onGround) {
            onGround = false;
        }

        double newY = y + vy * dt;
        boolean landed = false;

        if (newY > groundY) {
            newY = groundY;
            vy = 0;
            landed = true;
        }

        Rectangle2D nextV = new Rectangle2D(x, newY - h, w, h);
        for (Rectangle2D r : solids) {
            if (downPressed && vy > 0) continue;
            if (nextV.intersects(r)) {
                if (vy > 0) {
                    newY = r.getMinY();
                    vy = 0;
                    landed = true;
                } else if (vy < 0) {
                    newY = r.getMaxY();
                    vy = 0;
                }
                nextV = new Rectangle2D(x, newY - h, w, h);
            }
        }

        y = newY;
        onGround = landed;

        if (onGround) jumping = false;

        double newX = x + vx * dt;
        Rectangle2D nextH = new Rectangle2D(newX, y - h, w, h);
        for (Rectangle2D r : solids) {
            if (nextH.intersects(r)) {
                if (vx > 0 && (x + w) <= r.getMinX() + 1) {
                    newX = r.getMinX() - w;
                    vx = 0;
                } else if (vx < 0 && x >= r.getMaxX() - 1) {
                    newX = r.getMaxX();
                    vx = 0;
                }
                nextH = new Rectangle2D(newX, y - h, w, h);
            }
        }

        x = newX;

        if (x < 0) x = 0;
        if (x + w > 960) x = 960 - w;
    }

    // --- Animation Update ---
    private void updateAnimation(double dt) {
        boolean airborne = !onGround || jumping;
        boolean moving   = Math.abs(vx) > 1;

        // Priority: prone(on ground) > airborne(jump/fall) > shooting(stand) > run > idle
        if (prone && onGround) {
            switchAnim(character.prone);
        } else if (airborne) {
            switchAnim(character.jump);
        } else if (shooting) {
            switchAnim(character.shoot);
        } else if (moving && onGround) {
            switchAnim(character.run);
        } else {
            switchAnim(character.idle);
        }

        currentAnim.setFlipX(facingLeft);
        currentAnim.update(dt);
    }

    private void switchAnim(se233.adpro2.util.AnimatedSprite next) {
        if (currentAnim != next) {
            currentAnim = next;
            currentAnim.reset();
        }
    }

    // --- Draw ---
    public void draw(GraphicsContext gc) {
        updateAnimation(1 / 60.0); // assume 60fps

        double frameW = currentAnim.getFrameWidth()  * character.scale; // 40 * scale
        double frameH = currentAnim.getFrameHeight() * character.scale; // 60 * scale

        // Center on X, and lift by (frameH - hitboxH) so feet stay on the floor
        double drawX = x + (getW() - frameW) * 0.5;
        double offsetY = 130;
        double drawY   = y - (frameH - getH()) + offsetY;

        currentAnim.draw(gc, drawX, drawY, character.scale);

        // HP bar + debug
        gc.setFill(Color.DARKRED);
        gc.fillRect(x, y - getH() - 12, getW(), 4);
        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(x, y - getH() - 12, getW() * (getHP() / 100.0), 4);

        if (isInvulnerable()) {
            gc.setStroke(Color.CYAN);
            gc.strokeRect(x, y - getH(), getW(), getH());
        }
    }

    // --- Collision box ---
    public Rectangle2D getRect() { return new Rectangle2D(x, y - h, w, h); }

    // --- Accessors ---
    public double getX() { return x; }
    public double getY() { return y; }
    public double getW() { return w; }
    public double getH() { return h; }
    public boolean isFacingLeft() { return facingLeft; }
}
