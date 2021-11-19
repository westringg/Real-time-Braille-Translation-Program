package example;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class WebcamCapture {

    public static void main(String[] args) {
        // load the native opencv library
        OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // register the default camera
        VideoCapture cap = new VideoCapture();

        // check if video capturing is enabled
        if (!cap.isOpened()) {
            System.out.println("camera is not enabled.");
            System.exit(-1);
        }

        // matrix for storing image
        Mat image = new Mat();
        // Frame for displaying image
        MyFrame frame = new MyFrame();
        frame.setVisible(true);

        // main loop
        while (true) {
            // read current camera frame into matrix
            cap.read(image);
            // render frame if the camera is still acquiring images
            if (!image.empty()) {
                frame.render(image);
            } else {
                System.out.println("No captured frame -- camera disconnected");
                break;
            }
        }
    }
}
