package example;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class MyFrame {

    private final JFrame frame;
    private final MyPanel panel;

    public MyFrame() {
        // JFrame which holds JPanel
        frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // JPanel which is used for drawing image
        panel = new MyPanel();
        frame.getContentPane().add(panel);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void render(Mat image) {
        Image i = toBufferedImage(image);
        panel.setImage(i);
        panel.repaint();
        frame.pack();
    }

    private Image toBufferedImage(Mat m) {
        // check if image is grayscale or color
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        // transfer bytes from Mat to BufferedImage
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }
}
