package io.github.team_sippo.tensorflow_lite_mnist.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TensorflowLiteRunner {
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 1;
    private static final int RESULT_DIM = 10;
    private Context context;
    private MappedByteBuffer modelFile;
    private Interpreter model;
    private ByteBuffer imgData = null;
    private float[][] probabilities = null;

    public TensorflowLiteRunner(Context context){
        this.context = context;
        String modelFilename="models/converted_keras_model.tflite";
        try {
            modelFile = readModel(modelFilename);
            model = new Interpreter(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runInference(final Bitmap bitmap){
        convertBitmapToByteBuffer(bitmap);
        probabilities = new float[1][RESULT_DIM];
        model.run(imgData, probabilities);

        float prob = 0;
        float prevFloat = 0;
        String resultString = "";
        int index=0;
        for(int i=0; i<RESULT_DIM; i++){
            prob=probabilities[0][i];
            if(prevFloat<prob){
                index=i;
                prevFloat=prob;
            }
            resultString = resultString + " " + String.valueOf(prob);
        }
        Toast.makeText(context,"the results is " + String.valueOf(index), Toast.LENGTH_LONG).show();

    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if(imgData == null){
            Log.d("a", String.valueOf(bitmap.getWidth()));
            imgData = ByteBuffer.allocateDirect(
                    DIM_BATCH_SIZE
                            * bitmap.getWidth()
                            * bitmap.getHeight()
                            * DIM_PIXEL_SIZE
                            * Float.SIZE/8);
            imgData.order(ByteOrder.nativeOrder());
        } else {
            imgData.clear();
            imgData.order(ByteOrder.nativeOrder());
        }


        imgData.rewind();
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < bitmap.getWidth(); ++i) {
            for (int j = 0; j < bitmap.getHeight(); ++j) {
                final int val = pixels[pixel++];
                // int is 32bits and RGBa is contained each 8bits. so shift git and filter bits to extract 0-255 value
                float red = ((val >> 16) & 0xFF) / 255.f ;
                float green = ((val >> 8) & 0xFF) / 255.f;
                float blue = (val & 0xFF) / 255.f;
                float grayValue = (float) (0.2126*red + 0.7152*green + 0.0722*blue);
                float inputValue = (float) (grayValue);
                // Log.d("a", String.valueOf(inputValue));
                imgData.putFloat(inputValue);
            }
        }
        return;
    }

    private MappedByteBuffer readModel(String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);    }
}
