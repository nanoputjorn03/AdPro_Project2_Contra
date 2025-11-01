package se233.adpro2;

import se233.adpro2.core.Damageable;
import se233.adpro2.model.*;
import se233.adpro2.util.GameLogger;
import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main extends Application {

    private static final int W = 960, H = 540;
    private Canvas canvas;
    private GraphicsContext gc;

    private Image bgStage1, bgStage3, bgStage8, currentBackground;

    private final List<Rectangle2D> solids = new ArrayList<>();
    private boolean debugSolids = false;
    private boolean debugHit = false;

    private Player player;
    private final List<Bullet> playerBullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();

    private final List<Boss> bossOrder = new ArrayList<>();
    private int bossIndex = 0;
    private Boss boss;
    private boolean bossImmortal = false;

    private final List<EnemyMinion> minions = new ArrayList<>();
    private double spawnTimer = 0;

    private final ScoreManager score = new ScoreManager();
    private int lives = 3;

    private boolean left, right, up, down, shoot;
    private long lastShootNanos = 0;

    private enum State { START, FIGHT, CLEAR_ALL, GAME_OVER }
    private State state = State.START;

    private boolean specialShoot = false;
    private long lastSpecialNanos = 0;
    private static final long SPECIAL_COOLDOWN_NS = 1_200_000_000L; // 1.2s

    private final List<Explosion> explosions = new ArrayList<>();


    @Override
    public void start(Stage stage) {
        canvas = new Canvas(W, H);
        gc = canvas.getGraphicsContext2D();
        var urlbg1 = Main.class.getResource("/assets/backgrounds/stage1_jungle.png");
        var urlbg2 = Main.class.getResource("/assets/backgrounds/stage8_hangar.png");
        var urlbg3 = Main.class.getResource("/assets/backgrounds/stage3_waterfall.png");

        try {
            //boss 1
            bgStage1 = new Image(urlbg1.toExternalForm());
            //boss 2
            bgStage8 = new Image(urlbg2.toExternalForm());
            //boss 3
            bgStage3 = new Image(urlbg3.toExternalForm());
        } catch (Exception ignored) {}


        Scene scene = new Scene(new StackPane(canvas));
        setupInput(scene);

        stage.setTitle("Contra-CMU (Boss Rush + Minions + Logs)");
        stage.setScene(scene);
        stage.show();

        new AnimationTimer() {
            long prev = 0;
            @Override public void handle(long now) {
                if (prev == 0) prev = now;
                double dt = (now - prev) / 1_000_000_000.0;
                prev = now;
                try {
                    update(dt, now);
                } catch (Exception e) {
                    e.printStackTrace();
                    state = State.GAME_OVER;
                }
                render();
            }
        }.start();
    }

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT) left = true;
            else if (c == KeyCode.RIGHT) right = true;
            else if (c == KeyCode.UP) up = true;
            else if (c == KeyCode.DOWN) down = true;
            else if (c == KeyCode.SPACE) shoot = true;
            else if (c == KeyCode.E) specialShoot = true;     // <-- special
            else if (c == KeyCode.D) debugSolids = !debugSolids;
            else if (c == KeyCode.H) debugHit = !debugHit;
            else if (c == KeyCode.I) {
                player.setInvulnerable(!player.isInvulnerable());
                String playerState = player.isInvulnerable() ? "PLAYER GOD MODE ON" : "PLAYER GOD MODE OFF";
                String bossState = bossImmortal ? "BOSS IMMORTAL ON" : "BOSS IMMORTAL OFF";
                GameLogger.log(playerState + " | " + bossState);
                System.out.println(playerState + " | " + bossState);
            }
            else if (c == KeyCode.B) bossImmortal = !bossImmortal;
            else if (c == KeyCode.ENTER) {
                if (state == State.START) beginGame();
                else if (state == State.GAME_OVER || state == State.CLEAR_ALL) state = State.START;
            }
        });
        scene.setOnKeyReleased(e -> {
            KeyCode c = e.getCode();
            if (c == KeyCode.LEFT) left = false;
            else if (c == KeyCode.RIGHT) right = false;
            else if (c == KeyCode.UP) up = false;
            else if (c == KeyCode.DOWN) down = false;
            else if (c == KeyCode.SPACE) shoot = false;
            else if (c == KeyCode.E) specialShoot = false;    // <-- special
        });
    }

    private void beginGame() {
        player = new Player(100, 400);
        player.setHP(100);
        player.setInvulnerable(false);
        playerBullets.clear();
        enemyBullets.clear();
        minions.clear();
        score.reset();
        lives = 3;

        bossOrder.clear();
        bossOrder.add(new Boss1DefenseWall(740, 420));
        bossOrder.add(new Boss2Java(720, 420));
        bossOrder.add(new Boss3Gromaldes(740, 340));
        bossIndex = 0;
        boss = bossOrder.get(0);

        setBackgroundForBoss(boss);
        setSolidsForBoss(boss);
        state = State.FIGHT;

        GameLogger.log("=== GAME STARTED ===");
    }

    private void nextBossOrClear() {
        bossIndex++;
        if (bossIndex >= bossOrder.size()) {
            GameLogger.log("All bosses defeated. Mission clear!");
            state = State.CLEAR_ALL;
            return;
        }
        boss = bossOrder.get(bossIndex);
        setBackgroundForBoss(boss);
        setSolidsForBoss(boss);
        player.respawn(100, 400);
        player.setHP(100);
        playerBullets.clear();
        enemyBullets.clear();
        minions.clear();

        GameLogger.log("Next Boss: " + boss.getClass().getSimpleName());
    }

    private void setBackgroundForBoss(Boss b) {
        if (b instanceof Boss1DefenseWall) currentBackground = bgStage1;
        else if (b instanceof Boss2Java) currentBackground = bgStage8;
        else if (b instanceof Boss3Gromaldes) currentBackground = bgStage3;
        else currentBackground = null;
    }

    private void setSolidsForBoss(Boss b) {
        solids.clear();
        if (b instanceof Boss1DefenseWall) {
            // Stage 1: simple crates/ledges
            solids.add(new Rectangle2D(0, 230, 380, 5));
            solids.add(new Rectangle2D(100, 335, 280, 5));
            solids.add(new Rectangle2D(380, 298, 90, 5));
            solids.add(new Rectangle2D(480, 388, 90, 5));
        } else if (b instanceof Boss2Java) {
            // Stage 8: hangar platforms
            solids.add(new Rectangle2D(0, 360, 10, 100));
            solids.add(new Rectangle2D(320, 280, 160, 200));
        } else if (b instanceof Boss3Gromaldes) {
            // Stage 3: waterfall steps
            solids.add(new Rectangle2D(0, 370, 1000, 5));
            solids.add(new Rectangle2D(0, 470, 1000, 5));
        }
    }


    private void update(double dt, long now) {
        if (state != State.FIGHT) return;

        // Player move
        double move = 0;
        if (left) { move -= Player.MOVE_SPEED; GameLogger.log("Player moved LEFT"); }
        if (right) { move += Player.MOVE_SPEED; GameLogger.log("Player moved RIGHT"); }
        player.moveHorizontal(move);

        if (up && !shoot && player.isOnGround()) {
            player.jump();
            GameLogger.log("Player JUMPED");
        }
        player.setProne(down);
        player.applyPhysics(dt, 460, solids, down);

        // Shooting
        if (shoot && now - lastShootNanos > 180_000_000) {
            player.setShooting(shoot);
            GameLogger.log("Player FIRED bullet");

            double speed = 520;
            double vx = player.isFacingLeft() ? -speed : speed;
            double vy = 0;

            // Special attack (spread shot)
            if (specialShoot) {
                firePlayerSpread(now);
            }

            // adjust spawn position based on player direction
            double muzzleOffsetX = player.isFacingLeft() ? 10 : player.getW() - 10;
            double bx = player.getX() + muzzleOffsetX;
            double by = player.getY() - player.getH() * 0.73;  // higher (shoulder height)

            if (up) {
                vx = 0;
                vy = -speed;
                bx = player.getX() + player.getW() * 0.5;  // center when shooting up
                by = player.getY() - player.getH() + 10;   // top of sprite
            }
            else if (down && !player.isOnGround()) {
                vx = 0;
                vy = speed;
                bx = player.getX() + player.getW() * 0.5;
                by = player.getY() - 5;                    // bottom center
            }

            playerBullets.add(Bullet.player(bx, by, vx, vy));
            lastShootNanos = now;
        }


        if (boss != null && boss.isAlive()) boss.updateAI(dt, player, enemyBullets, W, H);

        stepBullets(playerBullets, dt);
        stepBullets(enemyBullets, dt);

        // Minions
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnTimer = 4 + Math.random() * 3;
            double sx = player.isFacingLeft() ? -50 : W + 50;
            minions.add(new EnemyMinion(sx, 420));
            GameLogger.log("Minion spawned at " + sx);
        }

        for (EnemyMinion e : minions) e.update(dt, player);

        // Collisions: minions
        for (Iterator<EnemyMinion> it = minions.iterator(); it.hasNext();) {
            EnemyMinion e = it.next();
            if (!e.isAlive()) { it.remove(); continue; }

            for (Iterator<Bullet> bt = playerBullets.iterator(); bt.hasNext();) {
                Bullet b = bt.next();
                if (Collision.aabb(e.getRect(), b.getRect())) {
                    var br = b.getRect();
                    explosions.add(new Explosion(br.getMinX() + br.getWidth()/2, br.getMinY() + br.getHeight()/2));
                    e.kill();
                    bt.remove();
                    score.add(2);
                    GameLogger.log("Minion killed by player bullet");
                    break;
                }
            }

            if (Collision.aabb(e.getRect(), player.getRect()) && !player.isInvulnerable()) {
                lives--;
                e.kill();
                GameLogger.log("Player hit by Minion (Lives left: " + lives + ")");
                if (lives <= 0) {
                    GameLogger.log("Player DIED - GAME OVER");
                    state = State.GAME_OVER;
                } else {
                    player.respawn(100, 400);
                    playerBullets.clear();
                    enemyBullets.clear();
                    minions.clear();
                }
                break;
            }
        }

        // Boss damage
        boolean advanceBoss = false;
        if (boss != null && boss.isAlive()) {
            Rectangle2D bossBounds = new Rectangle2D(boss.getX(), boss.getY() - boss.getH(), boss.getW(), boss.getH());
            for (Iterator<Bullet> it = playerBullets.iterator(); it.hasNext();) {
                Bullet b = it.next();
                if (Collision.aabb(b.getRect(), bossBounds)) {
                    var br = b.getRect();
                    explosions.add(new Explosion(br.getMinX() + br.getWidth()/2, br.getMinY() + br.getHeight()/2));
                    if (!bossImmortal && boss instanceof Damageable d) d.hit(1);
                    GameLogger.log("Boss hit by player bullet");
                    it.remove();
                    if (!boss.isAlive() && !bossImmortal) {
                        GameLogger.log("Boss defeated: " + boss.getClass().getSimpleName());
                        score.add(10);
                        advanceBoss = true;
                        break;
                    }
                }
            }
        }
        if (advanceBoss) nextBossOrClear();

        // Enemy bullets hit player
        for (Iterator<Bullet> it = enemyBullets.iterator(); it.hasNext();) {
            Bullet b = it.next();
            if (Collision.aabb(b.getRect(), player.getRect())) {
                if (!player.isInvulnerable()) {
                    player.takeDamage(100);
                    lives--;
                    GameLogger.log("Player hit by enemy bullet (-100 HP). Lives=" + lives);
                    if (lives <= 0) {
                        GameLogger.log("Player DIED - GAME OVER");
                        state = State.GAME_OVER;
                    } else player.respawn(100, 400);
                }
                it.remove();
                break;
            }
        }
        stepExplosions(dt);
    }

    private void stepBullets(List<Bullet> list, double dt) {
        for (Bullet b : list) b.step(dt);
        list.removeIf(b -> b.outside(W, H));
    }

    private void render() {
        if (state == State.START || state == State.GAME_OVER || state == State.CLEAR_ALL) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, W, H);
        } else if (currentBackground != null) {
            gc.drawImage(currentBackground, 0, 0, W, H);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, W, H);
        }

        switch (state) {
            case START -> {
                gc.setFill(Color.WHITE);
                gc.fillText("Contra-Adpro_Project2 (Boss Rush + Minions + Logs)", 360, 220);
                gc.fillText("Press ENTER to Start", 390, 260);
                gc.fillText("Controls: ←/→ move, ↑ jump, ↓ prone, SPACE shoot, E special attack", 260, 290);
                gc.fillText("Debug: D solids, H hitboxes, I player god, B boss god", 250, 320);
            }
            case GAME_OVER -> {
                gc.setFill(Color.WHITE);
                gc.fillText("GAME OVER", 430, 240);
                gc.fillText("Score: " + score.get(), 450, 270);

                // Save high score to logs/highscore.txt
                GameLogger.saveHighScore(score.get());
                GameLogger.log("High score saved after GAME OVER: " + score.get());
            }
            case CLEAR_ALL -> {
                gc.setFill(Color.LIGHTGREEN);
                gc.fillText("MISSION CLEAR!", 420, 240);
                gc.setFill(Color.WHITE);
                gc.fillText("Score: " + score.get(), 450, 270);

                // Save high score when all bosses are defeated
                GameLogger.saveHighScore(score.get());
                GameLogger.log("High score saved after MISSION CLEAR: " + score.get());
            }
            case FIGHT -> {
                player.draw(gc);
                for (EnemyMinion e : minions) e.draw(gc);
                for (Bullet b : playerBullets) b.draw(gc);
                for (Bullet b : enemyBullets) b.draw(gc);
                if (boss != null) boss.draw(gc);
                for (Explosion ex : explosions) ex.draw(gc);

                if (debugSolids) {
                    gc.setStroke(Color.web("#00FFAA80"));
                    for (Rectangle2D r : solids)
                        gc.strokeRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
                }
                if (debugHit && boss != null) {
                    Rectangle2D bb = new Rectangle2D(boss.getX(), boss.getY() - boss.getH(), boss.getW(), boss.getH());
                    gc.setStroke(Color.web("#00FF00AA"));
                    gc.strokeRect(bb.getMinX(), bb.getMinY(), bb.getWidth(), bb.getHeight());
                }

                gc.setFill(Color.WHITE);
                gc.fillText("Score: " + score.get(), 20, 26);
                gc.fillText("Lives: " + lives, 20, 46);
                gc.fillText("HP: " + player.getHP(), 20, 66);
                String playerMode = player.isInvulnerable() ? "GOD MODE: ON" : "GOD MODE: OFF";
                String bossMode = bossImmortal ? "BOSS IMMORTAL: ON" : "BOSS IMMORTAL: OFF";
                gc.fillText(playerMode, 20, 86);
                gc.fillText(bossMode, 20, 106);

                // Boss HUD (top-right)
                if (boss != null) {
                    String bossName = boss.getClass().getSimpleName();
                    gc.fillText("Boss: " + bossName, W - 260, 26);
                    gc.fillText("HP: " + boss.hpText(), W - 260, 46);
                }
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
    /** Returns (bx, by) muzzle position for forward shooting (shoulder height). */
    private double[] playerMuzzle() {
        double muzzleOffsetX = player.isFacingLeft() ? 10 : player.getW() - 10;
        double bx = player.getX() + muzzleOffsetX;
        double by = player.getY() - player.getH() * 0.73;
        return new double[]{bx, by};
    }

    private void firePlayerBullet(double bx, double by, double vx, double vy) {
        playerBullets.add(Bullet.player(bx, by, vx, vy));
    }

    private void firePlayerSpread(long now) {
        if (now - lastSpecialNanos < SPECIAL_COOLDOWN_NS) return;

        double[] p = playerMuzzle();
        double bx = p[0], by = p[1];

        // base angle: left or right
        double base = Math.atan2(0, player.isFacingLeft() ? -1 : 1);
        double speed = 540;

        // fan: -12°, -6°, 0°, +6°, +12°
        for (int i = -2; i <= 2; i++) {
            double ang = base + Math.toRadians(i * 6);
            double vx = Math.cos(ang) * speed;
            double vy = Math.sin(ang) * speed;
            firePlayerBullet(bx, by, vx, vy);
        }
        lastSpecialNanos = now;
    }
    private void stepExplosions(double dt) {
        for (var ex : explosions) ex.update(dt);
        explosions.removeIf(Explosion::done);
    }

}
