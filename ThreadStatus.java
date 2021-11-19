package example;

import org.opencv.core.Mat;

/**
 * 전역 변수 클래스
 */
public class ThreadStatus {
    // 1. Global 변수 선언
    private Boolean isUpdated = false;
    private Boolean isRunning = true;
    private Mat imgBuffer = null;

    public synchronized Boolean getUpdated() {
        return isUpdated;
    }

    public synchronized void setUpdated(Boolean updated) {
        isUpdated = updated;
    }

    public synchronized Boolean getRunning() {
        return isRunning;
    }

    public synchronized void setRunning(Boolean running) {
        isRunning = running;
    }

    public synchronized Mat getImgBuffer() {
        return imgBuffer;
    }

    public synchronized void setImgBuffer(Mat imgBuffer) {
        this.imgBuffer = imgBuffer;
    }
}
