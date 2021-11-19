package example;

import nu.pattern.OpenCV;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * 쓰레드 실행 프로세스
 */
public class OpencvOcrMain {

    public static void main(String[] args) throws IOException {
        // load opencv library
        OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 전역 변수 클래스
        ThreadStatus status = new ThreadStatus();

        UpdateLatestImage updateLatestImage = new UpdateLatestImage(status);
        Thread threadUpdateLatestImage = new Thread(updateLatestImage, "Update latest image Thread");
        OpencvOcrExample opencvOcrExample = new OpencvOcrExample(status);
        Thread threadMain = new Thread(opencvOcrExample, "Main Thread");

        // 최신 이미지 획득하는 쓰레드 실행
        threadUpdateLatestImage.start();
        // 메인 쓰레드 실행
        threadMain.start();
    }
}
