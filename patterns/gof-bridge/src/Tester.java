import javax.swing.*;
import java.awt.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class Tester {
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
        f.setSize(500, 500);
        f.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(final Graphics g) {
                super.paintComponent(g);
                Drawer d = new HighQualityDrawer((Graphics2D) g);
                ExtendedCanvas c = new ExtendedCanvas(d);
                c.drawSquaredCircle(getWidth() / 2, getHeight() / 2, getWidth() / 3);
                c.drawSquaredCircle(getWidth() / 3, getHeight() / 3, getWidth() / 5);
            }
        });
        f.repaint();
        f.setVisible(true);
    }
}
