package se233.adpro2;

import org.junit.jupiter.api.BeforeAll;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;
import se233.adpro2.model.Player;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerMovementTest {

    @BeforeAll
    static void bootFx() { FxTestSupport.initFx(); }

    private static final double EPS = 1e-6;

    @Test
    void jump_then_fall_then_land_on_ground() {
        Player p = new Player(100, 400);
        double groundY = 460;
        List<Rectangle2D> solids = new ArrayList<>();

        assertTrue(p.isOnGround());

        // jump
        p.jump();
        assertFalse(p.isOnGround());

        // simulate ~0.2s of airtime in small steps
        double yBefore = p.getY();
        double t = 0.0;
        while (t < 0.2) {
            p.applyPhysics(1/60.0, groundY, solids, /*downPressed*/ false);
            t += 1/60.0;
        }
        assertTrue(p.getY() < yBefore, "should move upward first");

        // keep stepping until we land
        int guard = 0;
        while (!p.isOnGround() && guard++ < 2000) {
            p.applyPhysics(1/60.0, groundY, solids, false);
        }
        assertTrue(p.isOnGround());
        assertEquals(groundY, p.getY(), EPS, "feet should sit on ground");
    }

    @Test
    void horizontal_move_and_screen_clamp() {
        Player p = new Player(10, 460);
        List<Rectangle2D> solids = new ArrayList<>();

        // move right
        p.moveHorizontal(Player.MOVE_SPEED);
        p.applyPhysics(0.5, 460, solids, false);
        assertTrue(p.getX() > 10);

        // move far left and clamp at 0
        p.moveHorizontal(-10_000);
        p.applyPhysics(1.0, 460, solids, false);
        assertEquals(0.0, p.getX(), EPS);
    }

    @Test
    void down_pressed_on_ground_causes_drop_through_platform() {
        Player p = new Player(100, 320);
        double groundY = 460;

        // a platform under the player (thin solid)
        List<Rectangle2D> solids = new ArrayList<>();
        solids.add(new Rectangle2D(80, 320, 200, 5));

        // place player on top of the platform first
        int guard = 0;
        while (!p.isOnGround() && guard++ < 2000) {
            p.applyPhysics(1/60.0, groundY, solids, false);
        }

        double yOnPlatform = p.getY();
        assertEquals(320, yOnPlatform, EPS, "should be standing on the platform top");

        // now press down to drop
        guard = 0;
        while (p.getY() < groundY && guard++ < 2000) {
            p.applyPhysics(1/60.0, groundY, solids, /*downPressed*/ true);
        }
        assertEquals(groundY, p.getY(), EPS, "should fall through to main ground");
    }
}
