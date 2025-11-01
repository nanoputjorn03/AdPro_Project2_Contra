package se233.adpro2.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Explosion {
    private double x, y;          // center
    private double t = 0;         // seconds elapsed
    private final double life = 0.28; // total lifetime

    public Explosion(double cx, double cy) {
        this.x = cx;
        this.y = cy;
    }

    public void update(double dt) { t += dt; }

    public boolean done() { return t >= life; }

    public void draw(GraphicsContext gc) {
        double a = 1.0 - (t / life);            // fade
        double r = 6 + 60 * (t / life);         // expand
        gc.setGlobalAlpha(a);
        gc.setFill(Color.rgb(255, 210, 80));
        gc.fillOval(x - r*0.5, y - r*0.5, r, r);
        gc.setGlobalAlpha(1.0);
    }
}
