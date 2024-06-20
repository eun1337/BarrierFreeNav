package com.example.capstone_kotlin;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FaceDetection implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "FaceDetection";

    private Net net;
    private Mat frame;
    private CameraBridgeViewBase mOpenCvCameraView;

    public FaceDetection(CameraBridgeViewBase openCvCameraView) {
        mOpenCvCameraView = openCvCameraView;
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void initializeOpenCVDependencies() {
        try {
            InputStream is = getClass().getResourceAsStream("res_ssd_300Dim.caffemodel");
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "res_ssd_300Dim.caffemodel");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            net = Dnn.readNetFromCaffe(getClass().getResource("weights-prototxt.txt").getPath(),
                    mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error loading cascade", e);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        frame = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();

        // Convert frame to a blob
        Mat blob = Dnn.blobFromImage(frame, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0), false, false);

        // Pass the blob into DNN
        net.setInput(blob);
        Mat detections = net.forward();
        int boxCount = 0;

        // Loop over the detections to extract specific confidence
        for (int i = 0; i < detections.size(3); i++) {
            double confidence = detections.get(0, 0, i, 2)[0];
            // Greater than the minimum confidence
            if (confidence < 0.5) {
                continue;
            }

            boxCount++;

            // Compute the boxes (x, y)-coordinates
            int x1 = (int) (detections.get(0, 0, i, 3)[0] * frame.cols());
            int y1 = (int) (detections.get(0, 0, i, 4)[0] * frame.rows());
            int x2 = (int) (detections.get(0, 0, i, 5)[0] * frame.cols());
            int y2 = (int) (detections.get(0, 0, i, 6)[0] * frame.rows());

            // Draw the bounding box of the face along with the associated probability
            Imgproc.rectangle(frame, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 0, 255), 2);
            String text = String.format("%.2f%%", confidence * 100) + " (" + (y2 - y1) + ", " + (x2 - x1) + ")";
            int y = y1 - 10 > 10 ? y1 - 10 : y1 + 10;
            Imgproc.putText(frame, text, new Point(x1, y), Core.FONT_HERSHEY_SIMPLEX, 0.45, new Scalar(0, 0, 255), 2);
        }

        Log.d(TAG, "Box Count: " + boxCount);

        return frame;
    }

    public void onDestroy() {
        mOpenCvCameraView.disableView();
        frame.release();
        net.close();
    }

    public void onPause() {
        mOpenCvCameraView.disableView();
    }

    public void onResume() {
        mOpenCvCameraView.enableView();
    }
}
