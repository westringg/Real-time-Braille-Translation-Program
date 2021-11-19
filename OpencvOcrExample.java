package example;

import chu.sample.ncloud.utils.NaverSmsUtilV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.opencv.core.CvType.CV_32FC2;

/**
 * 갱신된 이미지에서 텍스트 추출하는 클래스
 */
public class OpencvOcrExample implements Runnable {

	public static final String SAVE_DIR = "src/main/resources";
	public static final String FILE_NAME = "snapshot";
	public static final String FILE_EXT = "png";
	public static final String SAVE_PATH = SAVE_DIR + "/" + FILE_NAME + "." + FILE_EXT;
	public static final String OCR_KEY = "RGVQck1qYm1IZ29GZHJQWmpIdERNZlFqdFl6dHdrYXM=";
	public static final String REST_URI = "https://569cdd509a4c4e31bf80651af98c4b45.apigw.ntruss.com/custom/v1/2871/015a8ef25eb2f464f0bc251746946304749b818b0dda137b9041e56e4a965dbd/general";

	ThreadStatus status;

	OpencvOcrExample(ThreadStatus status) {
		this.status = status;
	}

	// 3. Main Thread
	@Override
	public void run() {
		Mat frame = new Mat();
		while (status.getRunning()) {
			System.out.println("ThreadName: " + Thread.currentThread().getName() + ", param.isUpdated: " + status.getUpdated());
			try {
				// 새로운 이미지가 쓰레드에서 갱신되었는지 check
				System.out.println("// check new image");
				if (!status.getUpdated()) {
					Thread.sleep(5000);	// 5s 대기 --> 새로운 이미지가 들어올때까지 sleep
					continue;
				}

				// 여기로 오면 새로운 이미지가 업데이트 된 것임.
				frame = status.getImgBuffer().clone();	// 새 이미지 copy
				status.setUpdated(false);	// 새로운 이미지 복사했으니, 다음번에 다시 사용할 수 있게끔 flag 설정

				// 3.1 Finger detect
				MatOfPoint fingerContour = detectFinger(frame);

				// 3.2 OCR
				ArrayList<OcrResField> ocrFields = textRecognition(frame);

				// 3.3 Select text
				String closestText = selectText(ocrFields, fingerContour);

				// 3.4 Send detected text to Dot Watch
				if (!closestText.isEmpty())
					sendMessage(closestText);

				System.out.println("Detected text: " + closestText);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}

	private MatOfPoint detectFinger(Mat frame) {
		// finger detection with hsv
        // convert source image to HSV
        Mat blurImage = new Mat();
        Mat hsvImage = new Mat();
        Imgproc.blur(frame, blurImage, new Size(7, 7));		// smooth the image
        Imgproc.cvtColor(blurImage, hsvImage, Imgproc.COLOR_BGR2HSV);	// convert to hsv image
        // mask hsv image
        Scalar scalarLower = new Scalar(0, 30, 0);
        Scalar scalarUpper = new Scalar(15, 255, 255);
        Mat maskedImage = new Mat();
        Core.inRange(hsvImage, scalarLower, scalarUpper, maskedImage);
		// refine image
        Mat refinedImage = refineImage(maskedImage);

		// find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(refinedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // find the finger contour
        int centerX = frame.width() / 2;
        int bottomY = frame.height();
        Point pt = new Point(centerX, bottomY - 1);
        MatOfPoint fingerCont = findFingerContour(contours, pt);

        return fingerCont;
	}

	private Mat refineImage(Mat maskedImage) {
		// apply morphological operators
		// dilate with large element, erode with small ones
		Mat morphImage = new Mat();
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));
		Imgproc.erode(maskedImage, morphImage, erodeElement);
		Imgproc.erode(maskedImage, morphImage, erodeElement);
		Imgproc.dilate(maskedImage, morphImage, dilateElement);
		Imgproc.dilate(maskedImage, morphImage, dilateElement);

		return morphImage;
	}

	/**
	 * Find a finger contour from a lot of contours <br/>
	 *  - determines whether the point is inside a contour
	 * @param contours
	 * @return
	 */
	private MatOfPoint findFingerContour(List<MatOfPoint> contours, Point pt) {
		MatOfPoint fingerCont = new MatOfPoint();
		if (contours != null) {
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
		}
		return fingerCont;
	}

	private ArrayList<OcrResField> textRecognition(Mat frame) throws JsonProcessingException {
		// Read and convert image mat to base64 string
		// Encoding the image
		MatOfByte matOfByte = new MatOfByte();
		Imgcodecs.imencode(".png", frame, matOfByte);
		// Storing the encoded Mat in a byte array
		byte[] byteArray = matOfByte.toArray();
		String encodedString = Base64.getEncoder().encodeToString(byteArray);
		System.out.println("Encoded String = " + encodedString);

		// Prepare for Naver OCR API call
		OcrReqImage ocrReqImg = new OcrReqImage("png", "CapturedImage");
		ocrReqImg.setData(encodedString);
		ArrayList<OcrReqImage> images = new ArrayList<>();
		images.add(ocrReqImg);
		OcrRequest ocrReq = new OcrRequest("V2", "string", 0, "ko");
		ocrReq.setImages(images);
		// Call Naver OCR API
		String ocrApiCallResult = callOcrApi(REST_URI, ocrReq, OCR_KEY);
		System.out.println("OCR API Call Result = " + ocrApiCallResult);
		// Extract OCR text
		String ocrText = extractOcrText(ocrApiCallResult, OcrResponse.class);
		System.out.println("Extracted OCR Text = " + ocrText);
		// Extract OCR fields
		ArrayList<OcrResField> ocrResFields = extractOcrFields(ocrApiCallResult, OcrResponse.class);
		// Draw bounding box
//		drawBoundingBox(SAVE_PATH, ocrResFields, new Scalar(255, 0, 0));

		return ocrResFields;
	}

	// Call Naver OCR API
	private static String callOcrApi(String REST_URI, OcrRequest ocrReq, String OCR_KEY) {
		Client client = ClientBuilder.newClient();
		Response res = client.target(REST_URI).request(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).header("X-OCR-SECRET", OCR_KEY)
				.post(Entity.entity(ocrReq, MediaType.APPLICATION_JSON));
		return res.readEntity(String.class);
	}

	// Extract OCR text
	private static String extractOcrText(String ocrApiCallResult, Class<OcrResponse> ocrResponseClass)
			throws JsonProcessingException {
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

	// Extract OCR fields
	private static ArrayList<OcrResField> extractOcrFields(String ocrApiCallResult, Class<OcrResponse> ocrResponseClass)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		OcrResponse ocrRes = mapper.readValue(ocrApiCallResult, ocrResponseClass);
		ArrayList<OcrResImage> ocrResImages = ocrRes.getImages();
		ArrayList<OcrResField> ocrResFields = new ArrayList<>();
		for (OcrResImage ocrResImage : ocrResImages) {
			ocrResFields = ocrResImage.getFields();
		}
		return ocrResFields;
	}

	private String selectText(ArrayList<OcrResField> ocrFields, MatOfPoint fingerContour) {
		// Find a point of the fingertip
        Point fingertip = findFingertip(fingerContour);
        // Find the text closest to fingertip
        double minDist = 0;
        String closestText = "";
        if (ocrFields != null) {
			for (int i = 0; i < ocrFields.size(); i++) {
				OcrResField field = ocrFields.get(i);
				// Find a center of the rectangle from the ocr text
				ArrayList<Vertex> vertices = field.getBoundingPoly().getVertices();
				Point centerOfText = findCenterOfRect(vertices);

				// Get distance between two points
				double distance = getDistance(fingertip, centerOfText);
				if (i == 0) {
					minDist = distance;
					closestText = field.getInferText();
				}
				if (distance < minDist) {
					minDist = distance;
					closestText = field.getInferText();
				}
			}
		}
        System.out.println("The text the finger is pointing to is: " + closestText);

        return closestText;
	}

	// Find a fingertip
	// Detect a point with minimum y value
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

	// Calculate a center point of the ocr text rectangle
	private static Point findCenterOfRect(ArrayList<Vertex> vertices) {
		Vertex ltPt = vertices.get(0);
		Vertex brPt = vertices.get(2);
		float centerX = ltPt.getX() + (brPt.getX() - ltPt.getX()) / 2;
		float centerY = ltPt.getY() + (brPt.getY() - ltPt.getY()) / 2;

		return new Point(centerX, centerY);
	}

	// Get distance between fingertip and center point of the ocr text
	private static double getDistance(Point fingertip, Point centerOfText) {
		return Math.sqrt(Math.pow(Math.abs(fingertip.x - centerOfText.x), 2) + Math.pow(Math.abs(fingertip.y - centerOfText.y), 2));
	}

	// Send SMS
	private static void sendMessage(String closestText) {
		// 네이버 클라우드 SENS 가입이후 서비스 ID 확인 가능
		// 계정의 인증키 관리 페이지에서 accessKey 키 확인가능
		// 계정의 인증키 관리 페이지에서 securityKey 키 확인가능
		String serviceID = "ncp:sms:kr:259838061021:soco_sms";
		String accessKey = "8fflSIzXdVviHbDEOgNy";
		String securityKey = "BjJP36h1U0738ghX6EWtbNG2eyezjVlpmHsprUfy";

		String sender = "01045769783";
		ArrayList<String> receiverList = new ArrayList<String>();
		receiverList.add("01045769783");

		String sentResult = NaverSmsUtilV2.getInstance(serviceID, accessKey, securityKey).sendMessage(sender, closestText, receiverList);
		System.out.println("SMS sent result: " + sentResult);
	}

	// draw bounding box and save image
	private static void drawBoundingBox(String imgPath, ArrayList<OcrResField> ocrResFields, Scalar scalar) {
		Mat boundingBoxImg = Imgcodecs.imread(imgPath).clone();
		for (OcrResField field : ocrResFields) {
			Vertex v1 = field.getBoundingPoly().getVertices().get(0);
			Vertex v2 = field.getBoundingPoly().getVertices().get(2);
			Point pt1 = new Point(v1.getX(), v1.getY());
			Point pt2 = new Point(v2.getX(), v2.getY());
			Imgproc.rectangle(boundingBoxImg, pt1, pt2, scalar);
		}
//		saveImage(boundingBoxImg, SAVE_DIR + "/" + FILE_NAME + "_bounding_box.png");
//		System.out.println("The bounding box has drawn at: " + FILE_NAME + "_bounding_box.png");
	}

}
