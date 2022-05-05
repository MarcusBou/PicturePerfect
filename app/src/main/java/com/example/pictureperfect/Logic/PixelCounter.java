package com.example.pictureperfect.Logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.Size;
import android.view.View;

import androidx.annotation.NonNull;
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
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

public class PixelCounter {

    private Map<ColorValue, Double> colorCounter;
    private Context ctx;
    private ColorValue[] colorValues;

    private List<TopColoListener> listeners;

    public PixelCounter(Context context){
        this.ctx = context;
        this.listeners = new ArrayList<TopColoListener>();
    }


    /**Takes a bitmap, then Counts every pixel and take color value
     * @param previewBitmap the bitmap from the preview*/
    public void getTopFiveColors(Bitmap previewBitmap) {
        this.colorCounter = new HashMap<>();
        Bitmap bitmap = Bitmap.createScaledBitmap(previewBitmap, previewBitmap.getWidth() / 10, previewBitmap.getHeight() / 10, true);
        findPixelColor(bitmap);
        this.colorCounter = this.sortByValue(this.colorCounter);
        colorValues = this.colorCounter.keySet().toArray(new ColorValue[0]);
        notifyListener();
    }

    /**Finds Finds Color in every pixel, then finds Color Resemblance Value
     * @param bitmap the bitmap that shall be found colors in*/
    private void findPixelColor(Bitmap bitmap){
        for (int y = 1; y < bitmap.getHeight(); y++) {
            for (int x = 1; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);
                int redValue = Color.red(pixel);
                int greenValue = Color.green(pixel);
                int blueValue = Color.blue(pixel);
                this.findColorResemblance(new ColorValue(redValue, greenValue, blueValue));
            }
        }
    }

    /**Cheks if there is a resemblance in already existing colors
     * then adds to the HASHMAP
     * @param colorValue  which value that shall be checked*/
    private void findColorResemblance(ColorValue colorValue){
        if (this.colorCounter.isEmpty()){
            this.colorCounter.put(colorValue, 1.0);
        }else{
            boolean result = false;
            for (Map.Entry<ColorValue, Double> colorIndicator: this.colorCounter.entrySet()){
                double difference = this.calculateColorEuclidean(colorIndicator.getKey(), colorValue);
                if (difference <= 40){
                    try {
                        this.colorCounter.put(colorIndicator.getKey(), colorIndicator.getValue() + 1);
                        result = true;
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            if(!result){
                this.colorCounter.put(colorValue, 1.0);
            }
        }
    }

    /**Calculates the differences in color*/
    private double calculateColorEuclidean(ColorValue color1, ColorValue color2){
        return Math.sqrt(Math.pow((color2.getRed() - color1.getRed()), 2)+
                Math.pow((color2.getGreen() - color1.getGreen()), 2)+
                Math.pow((color2.getBlue() - color1.getBlue()), 2));
    }

    /**Sorts out Hashmap by value*/
    private HashMap<ColorValue, Double> sortByValue(Map<ColorValue, Double> colorMapping){
        List<Map.Entry<ColorValue, Double>> list = new LinkedList<Map.Entry<ColorValue, Double>>(colorMapping.entrySet());
        Collections.sort(list, (i1 , i2 ) -> i2.getValue().compareTo(i1.getValue()));
        HashMap<ColorValue, Double> temp = new LinkedHashMap<ColorValue, Double>();
        for (Map.Entry<ColorValue, Double> item : list){ temp.put(item.getKey(), item.getValue()); }
        return temp;
    }

    /**Notify listners on gui level*/
    private void notifyListener(){
        for (TopColoListener listener : listeners){
            listener.onUpdate(colorValues);
        }
    }

    public void addListener(TopColoListener listener){
        this.listeners.add(listener);
    }

    public void deleteListener(TopColoListener listener){
        this.listeners.remove(listener);
    }
}
