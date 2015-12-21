public class ExtendedCanvas extends Canvas {
    public ExtendedCanvas(final Drawer drawer) {
        super(drawer);
    }

    public void drawDoubleCircle(int cx, int cy, int r1, int r2) {
        drawCircle(cx, cy, r1);
        drawCircle(cx, cy, r2);
    }

    public void drawCircledSquare(int cx, int cy, int r) {
        final int a = Math.round(2.0f * (float) (r / Math.sqrt(2.0)));
        final int px = cx - a / 2;
        final int py = cy - a / 2;
        drawRectangle(px, py, a, a);
        drawCircle(cx, cy, r);
    }

    public void drawSquaredCircle(int cx, int cy, int r) {
        final int d = r + r;
        final int px = cx - r;
        final int py = cy - r;
        drawRectangle(px, py, d, d);
        drawCircle(cx, cy, r);
    }
}
