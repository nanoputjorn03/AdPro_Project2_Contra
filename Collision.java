package se233.adpro2.model;

import javafx.geometry.Rectangle2D;

public final class Collision {
    private Collision() {}
    public static boolean aabb(Rectangle2D a, Rectangle2D b) {
        return a.getMinX() < b.getMaxX() &&
                a.getMaxX() > b.getMinX() &&
                a.getMinY() < b.getMaxY() &&
                a.getMaxY() > b.getMinY();
    }
}
