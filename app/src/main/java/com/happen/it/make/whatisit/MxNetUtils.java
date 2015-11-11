package com.happen.it.make.whatisit;

import android.graphics.Bitmap;

import org.dmlc.mxnet.Predictor;

import java.nio.ByteBuffer;

/**
 * Created by leliana on 11/6/15.
 */
public class MxNetUtils {
    private static boolean libLoaded = false;
    private MxNetUtils() {}

    public static String identifyImage(final Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] bytes = byteBuffer.array();
        float[] colors = new float[bytes.length / 4 * 3];

        float mean_b = WhatsApplication.getMean().get("b");
        float mean_g = WhatsApplication.getMean().get("g");
        float mean_r = WhatsApplication.getMean().get("r");
        for (int i = 0; i < bytes.length; i += 4) {
            int j = i / 4;
            colors[0 * 224 * 224 + j] = (float)(((int)(bytes[i + 0])) & 0xFF) - mean_r;
            colors[1 * 224 * 224 + j] = (float)(((int)(bytes[i + 1])) & 0xFF) - mean_g;
            colors[2 * 224 * 224 + j] = (float)(((int)(bytes[i + 2])) & 0xFF) - mean_b;
        }
        Predictor predictor = WhatsApplication.getPredictor();
        predictor.forward("data", colors);
        final float[] result = predictor.getOutput(0);

        int index = 0;
        for (int i = 0; i < result.length; ++i) {
            if (result[index] < result[i]) index = i;
        }
        String tag = WhatsApplication.getName(index);
        String [] arr = tag.split(" ", 2);
        return arr[1];
    }
}
