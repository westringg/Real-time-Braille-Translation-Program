package example;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

/**
 * 최신 이미지 획득하는 클래스
 */
public class UpdateLatestImage implements Runnable {
    ThreadStatus status;

    UpdateLatestImage(ThreadStatus status) {
        this.status = status;
    }

    @Override
    public void run() {
        // Instantiating the VideoCapture class (camera:: 0)
        VideoCapture capture = new VideoCapture();
        capture.open(0);

        while (status.getRunning()) {
            System.out.println("ThreadName: " + Thread.currentThread().getName() + ", param.isUpdated: " + status.getUpdated());
            System.out.println("======= Image capture start ========");

            // Check if video capturing is enabled
            if (!capture.isOpened()) {
                System.out.println("Camera is not enabled!");
                System.exit(-1);
            }
            // Reading the next video frame from the camera
            Mat frame = new Mat();
            capture.read(frame);
            status.setImgBuffer(frame.clone());
            status.setUpdated(true);
            System.out.println("Image captured.");
            System.out.println("======= Image capture end ========");

            HighGui.imshow("Current image", status.getImgBuffer());
            int key = HighGui.waitKey(1);
            if (key == 27) {
                status.setRunning(false);
                break;
            }
        }
    }
}
