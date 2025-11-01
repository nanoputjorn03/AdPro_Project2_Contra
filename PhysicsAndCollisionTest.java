package se233.adpro2;

import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;
import se233.adpro2.model.Bullet;
import se233.adpro2.model.Collision;

import static org.junit.jupiter.api.Assertions.*;

public class PhysicsAndCollisionTest {

    @Test
    void bullet_step_moves_position() {
        Bullet b = Bullet.player(10, 10, 100, -50);
        b.step(0.2); // 0.2 s
        // should move +20 on x and -10 on y
        assertTrue(b.getRect().getMinX() >= 30 - 10); // loose check using rect width 10
        assertTrue(b.getRect().getMinY() <= 0 + 4);   // y decreased (rect height 4)
    }

    @Test
    void aabb_detects_overlap_and_separation() {
        Rectangle2D r1 = new Rectangle2D(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D(9, 0, 10, 10);
        Rectangle2D r3 = new Rectangle2D(11, 0, 10, 10);

        assertTrue(Collision.aabb(r1, r2), "edges overlapping should be true");
        assertFalse(Collision.aabb(r1, r3), "separated rectangles should be false");
    }
}
