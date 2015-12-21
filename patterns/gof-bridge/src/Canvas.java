public class Canvas {
    private final Drawer drawer;

    public Canvas(final Drawer drawer) {
        this.drawer = drawer;
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        this.drawer.drawLine(x1, y1, x2, y2);
    }

    public void drawCircle(int cx, int cy, int r) {
        this.drawer.drawCircle(cx, cy, r);
    }

    public void drawRectangle(int x, int y, int w, int h) {
        this.drawer.drawLine(x, y, x + w, y); // top 
        this.drawer.drawLine(x, y, x, y + h); // left 
        this.drawer.drawLine(x, y + h, x + w, y + h); // bottom 
        this.drawer.drawLine(x + w, y, x + w, y + h); // right 
    }
}
