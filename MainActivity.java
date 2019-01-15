package com.doyen.fans.myfacedetector;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.doyen.fans.myfacedetector.helper.GraphicOverlay;
import com.doyen.fans.myfacedetector.helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    GraphicOverlay graphicOverlay;
    Button btnDetect;
    AlertDialog waitingDialog;
    //  FirebaseApp firebaseApp;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }


    @Override
    protected void onStop() {
        super.onStop();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //     firebaseApp = new FirebaseApp();
        cameraView = findViewById(R.id.camera_view);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        btnDetect = findViewById(R.id.btn_detect);

        waitingDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("Please wait")
                .setCancelable(false)
                .build();


        btnDetect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                waitingDialog.show();

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                runFaceDector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void runFaceDector(Bitmap bitmap) {



        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder().build();

        FirebaseVisionFaceDetector detector =  FirebaseVision.getInstance().getVisionFaceDetector(options);
        //FirebaseVisionFaceDetector detector = firebaseVision.getVisionFaceDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        processFaceResult(firebaseVisionFaces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count = 0;
        for (FirebaseVisionFace face:firebaseVisionFaces)
        {
            Rect bounds = face.getBoundingBox();
            RectOverlay rect = new RectOverlay(graphicOverlay, bounds);
            graphicOverlay.add(rect);
            count++;

        }

        waitingDialog.dismiss();
        Toast.makeText(this, String.format("Detected %d faces in image", count), Toast.LENGTH_SHORT).show();
    }
}
