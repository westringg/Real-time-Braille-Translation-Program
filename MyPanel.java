package example;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {

    private Image img;

    public void setImage(Image img) {
        this.img = img;

        // set JPanel size to image size
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }
}
