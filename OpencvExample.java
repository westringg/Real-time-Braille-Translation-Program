package example;

import static org.opencv.core.CvType.CV_32FC2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nu.pattern.OpenCV;

public class OpencvExample implements Runnable {

    private static Boolean isRunning = true;
    private static Boolean isUpdated = false;
    private static Mat imgBuffer = null;

    public static final String SAVE_DIR = "src/main/resources";
    public static final String FILE_NAME = "snapshot";
    public static final String FILE_EXT = "png";
    public static final String SAVE_PATH = SAVE_DIR + "/" + FILE_NAME + "." + FILE_EXT;
    public static final String OCR_KEY = "RGVQck1qYm1IZ29GZHJQWmpIdERNZlFqdFl6dHdrYXM=";
    public static final String REST_URI = "https://569cdd509a4c4e31bf80651af98c4b45.apigw.ntruss.com/custom/v1/2871/015a8ef25eb2f464f0bc251746946304749b818b0dda137b9041e56e4a965dbd/general";

    public static class UpdateLatestImage implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                System.out.println("ThreadName: "+Thread.currentThread().getName() + ", param.isUpdated: "+isUpdated);
                try {
                    System.out.println("======= image capture start ========");
                    System.out.println("// load opencv library");
                    System.out.println("// Instantiating the VideoCapture class (camera:: 0)");
                    System.out.println("// Reading the next video frame from the camera");
                    Thread.sleep(2000);
                    isUpdated = true;
                    System.out.println("======= image capture end ========");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("// image captured");
            }
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            System.out.println("ThreadName: " + Thread.currentThread().getName() + ", param.isUpdated: "+isUpdated);
            try {
                System.out.println("// check new image");
                if (!isUpdated) {
                    Thread.sleep(1000);
                    continue;
                }
                System.out.println("// call Naver OCR API");
                isUpdated = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        UpdateLatestImage updateLatestImage = new UpdateLatestImage();
        Thread threadUpdateLatestImage = new Thread(updateLatestImage, "update latest image thread");
        OpencvExample opencvExample = new OpencvExample();
        Thread threadMain = new Thread(opencvExample, "main thread");
        threadUpdateLatestImage.start();
        threadMain.start();

        Scanner userInput = new Scanner(System.in);
        String inputString = userInput.nextLine();
//        System.out.println(inputString);
        while (inputString.equals("c")) {
            isRunning = false;
            System.exit(0);
        }

//        // load opencv library
//        OpenCV.loadShared();
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//
//        // Instantiating the VideoCapture class (camera:: 0)
//        VideoCapture capture = new VideoCapture();
//        capture.open(0);
//        // Check if video capturing is enabled
//        if (!capture.isOpened()) {
//            System.out.println("camera is not enabled!");
//            System.exit(-1);
//        }
//        // Reading the next video frame from the camera
//        Mat mat = new Mat();
//        capture.read(mat);
//        saveImage(mat, SAVE_PATH);
//
//        File imageFile = new File(SAVE_PATH);
//        String imagePath = imageFile.getAbsolutePath();
//
//        // read and convert image file to base64 string
//        byte[] fileContent = FileUtils.readFileToByteArray(imageFile);
//        String encodedString = Base64.getEncoder().encodeToString(fileContent);
//        System.out.println("Encoded String = " + encodedString);
//
//        // prepare for Naver OCR API call
//        OcrReqImage ocrReqImg = new OcrReqImage("png", "CapturedImage");
//        ocrReqImg.setData(encodedString);
//        ArrayList<OcrReqImage> images = new ArrayList<>();
//        images.add(ocrReqImg);
//        OcrRequest ocrReq = new OcrRequest("V2", "string", 0, "ko");
//        ocrReq.setImages(images);
//        // call Naver OCR API
//        String ocrApiCallResult = callOcrApi(REST_URI, ocrReq, OCR_KEY);
//        System.out.println("OCR API Call Result = " + ocrApiCallResult);
//        // extract OCR text
//        String ocrText = extractOcrText(ocrApiCallResult, OcrResponse.class);
//        System.out.println("Extracted OCR Text = " + ocrText);
//        // extract OCR fields
//        ArrayList<OcrResField> ocrResFields = extractOcrFields(ocrApiCallResult, OcrResponse.class);
//        // draw bounding box
//        drawBoundingBox(SAVE_PATH, ocrResFields, new Scalar(255, 0, 0));
//
//        // finger detection with hsv
//        // convert source image to HSV
//        Mat srcImage = loadImage(imagePath);
//        Mat blurImage = new Mat();
//        Mat hsvImage = new Mat();
//        Imgproc.blur(srcImage, blurImage, new Size(7, 7));  // remove some noise
//        Imgproc.cvtColor(blurImage, hsvImage, Imgproc.COLOR_BGR2HSV);
//        // mask hsv image
//        Scalar scalarLower = new Scalar(0, 30, 0);
//        Scalar scalarUpper = new Scalar(15, 255, 255);
//        Mat maskedImage = new Mat();
//        Core.inRange(hsvImage, scalarLower, scalarUpper, maskedImage);
//        // save masked image
//        saveImage(maskedImage, SAVE_DIR + "/" + FILE_NAME + "_detected.png");
//
//        // morphological operators
//        // dilate with large element, erode with small ones
//        Mat morphImage = new Mat();
//        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
//        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
//        Imgproc.erode(maskedImage, morphImage, erodeElement);
//        Imgproc.erode(maskedImage, morphImage, erodeElement);
//        Imgproc.dilate(maskedImage, morphImage, dilateElement);
//        Imgproc.dilate(maskedImage, morphImage, dilateElement);
//
//        // find contours
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(morphImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        // find the finger contour
//        int centerX = srcImage.width() / 2;
//        int bottomY = srcImage.height();
//        Point pt = new Point(centerX, bottomY - 1);
//        MatOfPoint fingerCont = findFingerContour(contours, pt);
//        if (fingerCont.empty()) {
//            System.out.println("finger contour not found!");
//            System.exit(-1);
//        }
//
//        // draw contours
//        Mat contourImage = srcImage.clone();
//        List<MatOfPoint> contList = new ArrayList<>();
//        contList.add(fingerCont);
//        Imgproc.drawContours(contourImage, contList, -1, new Scalar(0, 255, 0), 2);
//        saveImage(contourImage, SAVE_DIR + "/" + FILE_NAME + "_contour.png");
//
////        // get convex hull
////        List<MatOfPoint> convexHull = getConvexHull(contours);
////        // draw convex hull
////        Mat convexImage = contourImage.clone();
////        Imgproc.drawContours(convexImage, convexHull, -1, new Scalar(0, 0, 255), 2);
////        saveImage(convexImage, savePath + "/gongcha_menu_convex_hull.png");
//
//        // find a point of the fingertip
//        Point fingertip = findFingertip(fingerCont);
//        // find the text closest to fingertip
//        double minDist = 0;
//        String closestText = "";
//        for (int i=0; i<ocrResFields.size(); i++) {
//            OcrResField field = ocrResFields.get(i);
//            // find a center of the rectangle from the ocr text
//            ArrayList<Vertex> vertices = field.getBoundingPoly().getVertices();
//            Point centerOfText = findCenterOfRect(vertices);
//
//            // get distance between two points
//            double distance = getDistance(fingertip, centerOfText);
//            if (i == 0) {
//                minDist = distance;
//                closestText = field.getInferText();
//            }
//            if (distance < minDist) {
//                minDist = distance;
//                closestText = field.getInferText();
//            }
//        }
//        System.out.println("The text the finger is pointing to is: " + closestText);
    }

    // draw bounding box and save image
    private static void drawBoundingBox(String imgPath, ArrayList<OcrResField> ocrResFields, Scalar scalar) {
        Mat boundingBoxImg = Imgcodecs.imread(imgPath).clone();
        for(OcrResField field: ocrResFields) {
            Vertex v1 = field.getBoundingPoly().getVertices().get(0);
            Vertex v2 = field.getBoundingPoly().getVertices().get(2);
            Point pt1 = new Point(v1.getX(), v1.getY());
            Point pt2 = new Point(v2.getX(), v2.getY());
            Imgproc.rectangle(boundingBoxImg, pt1, pt2, scalar);
        }
        saveImage(boundingBoxImg, SAVE_DIR + "/" + FILE_NAME + "_bounding_box.png");
        System.out.println("The bounding box has drawn at: "+ FILE_NAME + "_bounding_box.png");
    }

    // get distance between fingertip and center point of the ocr text
    private static double getDistance(Point fingertip, Point centerOfText) {
        return Math.sqrt(Math.pow(Math.abs(fingertip.x - centerOfText.x), 2) + Math.pow(Math.abs(fingertip.y - centerOfText.y), 2));
    }

    // calculate a center point of the ocr text rectangle
    private static Point findCenterOfRect(ArrayList<Vertex> vertices) {
        Vertex ltPt = vertices.get(0);
        Vertex brPt = vertices.get(2);
        float centerX = ltPt.getX() + (brPt.getX() - ltPt.getX()) / 2;
        float centerY = ltPt.getY() + (brPt.getY() - ltPt.getY()) / 2;

        return new Point(centerX, centerY);
    }

    // find a fingertip
    // detect a point with minimum y value
    private static Point findFingertip(MatOfPoint fingerCont) {
        List<Point> fingerPoints = fingerCont.toList();
        Point minPt = new Point();
        for (int i=0; i<fingerPoints.size(); i++) {
            Point pt = fingerPoints.get(i);
            if (i == 0) minPt = pt;
            if (pt.y < minPt.y) minPt = pt;
        }
        return minPt;
    }

    // call Naver OCR API
    private static String callOcrApi(String REST_URI, OcrRequest ocrReq, String OCR_KEY) {
        Client client = ClientBuilder.newClient();
        Response res = client.target(REST_URI)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header("X-OCR-SECRET", OCR_KEY)
                .post(Entity.entity(ocrReq, MediaType.APPLICATION_JSON));
        return res.readEntity(String.class);
    }

    // extract OCR fields
    private static ArrayList<OcrResField> extractOcrFields(String ocrApiCallResult, Class<OcrResponse> ocrResponseClass) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        OcrResponse ocrRes = mapper.readValue(ocrApiCallResult, ocrResponseClass);
        ArrayList<OcrResImage> ocrResImages = ocrRes.getImages();
        ArrayList<OcrResField> ocrResFields = new ArrayList<>();
        for (OcrResImage ocrResImage : ocrResImages) {
            ocrResFields = ocrResImage.getFields();
        }
        return ocrResFields;
    }

    // extract OCR text
    private static String extractOcrText(String ocrApiCallResult, Class<OcrResponse> ocrResponseClass) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        OcrResponse ocrRes = mapper.readValue(ocrApiCallResult, ocrResponseClass);
        ArrayList<OcrResImage> ocrResImages = ocrRes.getImages();
        ArrayList<String> ocrTextList = new ArrayList<>();
        for (OcrResImage ocrResImage : ocrResImages) {
            ArrayList<OcrResField> ocrResFields = ocrResImage.getFields();
            for (OcrResField ocrResField : ocrResFields) {
                ocrTextList.add(ocrResField.getInferText());
            }
        }
        return String.join(" ", ocrTextList);
    }

    // load image
    private static Mat loadImage(String imagePath) {
        return Imgcodecs.imread(imagePath);
    }
    // save image
    private static void saveImage(Mat imageMatrix, String targetPath) {
        Imgcodecs.imwrite(targetPath, imageMatrix);
    }

    /**
     * find a finger contour from a lot of contours <br>
     *  - determines whether the point is inside a contour
     * @param contours
     * @return
     */
    private static MatOfPoint findFingerContour(List<MatOfPoint> contours, Point pt) {
        MatOfPoint fingerCont = new MatOfPoint();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint cont = contours.get(i);
            MatOfPoint2f con2 = new MatOfPoint2f();
            cont.convertTo(con2, CV_32FC2);
            double pointInContour = Imgproc.pointPolygonTest(con2, pt, false);
//            System.out.println("src image point(center,bottom-1): (" + pt.x + "," + pt.y + ")");
//            System.out.println("point is inside or on edge?: " + (pointInContour > -1.0 ? "Yes" : "No"));
            if (pointInContour > -1) {
                System.out.println("pointInCountour index: " + i);
                fingerCont = cont;
                break;
            }
        }
        return fingerCont;
    }

    // find max contour
    private static MatOfPoint findMaxContour(List<MatOfPoint> contours) {
        MatOfPoint maxContour = new MatOfPoint();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);

            Rect rect = Imgproc.boundingRect(contour);
            if(rect.width > rect.height) continue;

            if(i == 0) maxContour = contour;
            double contourArea = Imgproc.contourArea(contour);
            double maxContourArea = Imgproc.contourArea(maxContour);
            if(contourArea > maxContourArea) maxContour = contour;
        }
        return maxContour;
    }

    // get Convex hull
    private static List<MatOfPoint> getConvexHull(List<MatOfPoint> contours) {
        // draw convex hull
        List<MatOfPoint> hullList = new ArrayList<>();
        for (int i=0; i<contours.size(); i++) {
            MatOfPoint points = contours.get(i);
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(points, hull);
            Point[] contourArray = points.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int j = 0; j < hullContourIdxList.size(); j++) {
                hullPoints[j] = contourArray[hullContourIdxList.get(j)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }
        return hullList;
    }
}
