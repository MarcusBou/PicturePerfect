package com.example.pictureperfect;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;

import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.pictureperfect.Color.ColorValue;
import com.example.pictureperfect.Logic.PixelCounter;
import com.example.pictureperfect.Logic.TopColoListener;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView;
    private View[] topColorsView;
    private PixelCounter pixelCounter;
    private ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private TopColoListener listener = new TopColoListener() {
        @Override
        public void onUpdate(ColorValue[] cv) {
            for (int i = 0; i < topColorsView.length; i++) {
                topColorsView[i].setBackgroundColor(Color.rgb(0, 0, 0));
                if (cv.length > i) {
                    topColorsView[i].setBackgroundColor(Color.rgb(cv[i].getRed(), cv[i].getGreen(), cv[i].getBlue()));
                }
            }
        }
    };


        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_camera);
            this.pixelCounter = new PixelCounter(this);
            this.previewView = findViewById(R.id.previewView);
            this.imageCapture = new ImageCapture.Builder().build();
            this.pixelCounter.addListener(listener);
            this.topColorsView = new View[]{findViewById(R.id.FirstColour), findViewById(R.id.SecondColour), findViewById(R.id.ThirdColour), findViewById(R.id.FourthColour), findViewById(R.id.FifthColour)};
            this.cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
            this.addCameraProviderListener();
        }


        public void onClick(View view) {
            this.pixelCounter.getTopFiveColors(this.previewView.getBitmap());
        }

        /**Bind Image to the Preview*/
        private void bindImage(@NonNull ProcessCameraProvider cameraProvider){
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                @Override
                public void analyze(@NonNull ImageProxy image) {
                    image.close();
                }
            });
            Preview preview = new Preview.Builder().build();
            CameraSelector selector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
            preview.setSurfaceProvider(this.previewView.getSurfaceProvider());
            cameraProvider.bindToLifecycle((LifecycleOwner) this, selector,this.imageCapture, imageAnalysis, preview);
        }

        /**Method for adding a listener to camearaProviderListenableFuture*/
        private void addCameraProviderListener(){
            cameraProviderListenableFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                        bindImage(cameraProvider);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, ContextCompat.getMainExecutor(this));
        }
    }

