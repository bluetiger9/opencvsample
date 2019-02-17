package com.example.opencvsample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // load native library
    static {
        System.loadLibrary("opencvsample");
    }

    private TextView mTextView;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // OpenCV version
        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText("OpenCV version: " + version());

        // load image from THETA storage


        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
        options.inSampleSize = 4;

        String photoPath = Environment.getExternalStorageDirectory() + "/DCIM/100RICOH/" + "R0010275.JPG";

        Bitmap imgTheta = BitmapFactory.decodeFile(photoPath, options);


        ByteBuffer byteBufferTheta = ByteBuffer.allocate(imgTheta.getByteCount());
        imgTheta.copyPixelsToBuffer(byteBufferTheta);

        // OpenCV
        byte[] dstTheta = rgba2bgra(imgTheta.getWidth(), imgTheta.getHeight(), byteBufferTheta.array());

        Bitmap bmpTheta = Bitmap.createBitmap(imgTheta.getWidth(), imgTheta.getHeight(), Bitmap.Config.ARGB_8888);
        bmpTheta.copyPixelsFromBuffer(ByteBuffer.wrap(dstTheta));



        // ******  end load image from THETA

        // load the picture from the drawable resource
        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.sculpture);

        // get the byte array from the Bitmap instance
        ByteBuffer byteBuffer = ByteBuffer.allocate(img.getByteCount());
        img.copyPixelsToBuffer(byteBuffer);

        // call the process from the native library
        byte[] dst = rgba2bgra(img.getWidth(), img.getHeight(), byteBuffer.array());

        // set the output image on an ImageView
        Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageBitmap(bmpTheta);


        // Output
        File photo = new File(Environment.getExternalStorageDirectory() + "/DCIM/" + "CV0010275.JPG");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmpTheta.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

        try {
            FileOutputStream fos = new FileOutputStream(photo.getPath());
            fos.write(byteArrayOutputStream.toByteArray());
            fos.flush();
            fos.close();

        }
        catch (java.io.IOException e) {
            Log.e("OPENCV", "export file problem", e);
        }
    }

    // native functions
    public native String version();
    public native byte[] rgba2bgra(int width, int height, byte[] src);
}