package se233.adpro2.core;

public interface Movable {
    void moveHorizontal(double dx);
    void applyPhysics(double dt, double floorY);
}
