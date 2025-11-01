package se233.adpro2;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se233.adpro2.model.Boss1DefenseWall;

import static org.junit.jupiter.api.Assertions.*;

public class BossDamageTest {

    @Test
    void hit_reduces_hp_until_dead() {
        Boss1DefenseWall boss = new Boss1DefenseWall(700, 420);
        assertTrue(boss.isAlive());

        for (int i = 0; i < 30; i++) boss.hit(1);
        assertFalse(boss.isAlive(), "should be dead after 30 hits");
        assertTrue(boss.hpText().startsWith("0 /"), "hp text should show zero");
    }
}
