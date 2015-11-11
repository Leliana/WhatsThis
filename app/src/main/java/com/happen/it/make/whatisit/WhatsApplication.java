package com.happen.it.make.whatisit;

import android.app.Application;
import android.content.Context;

import org.dmlc.mxnet.Predictor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leliana on 8/5/15.
 */
public class WhatsApplication extends Application{
    private static Predictor predictor;
    public static Predictor getPredictor() {return predictor;}
    private static List<String> dict;
    private static Map<String, Float> mean;
    public static String getName(int i) {
        if (i >= dict.size()) {
            return "Shit";
        }
        return dict.get(i);
    }
    public static Map<String, Float> getMean() {
        return mean;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        final byte[] symbol = readRawFile(this, R.raw.symbol);
        final byte[] params = readRawFile(this, R.raw.params);
        final Predictor.Device device = new Predictor.Device(Predictor.Device.Type.CPU, 0);
        final int[] shape = {1, 3, 224, 224};
        final String key = "data";
        final Predictor.InputNode node = new Predictor.InputNode(key, shape);

        predictor = new Predictor(symbol, params, device, new Predictor.InputNode[]{node});
        dict = readRawTextFile(this, R.raw.synset);
        try {
            final StringBuilder sb = new StringBuilder();
            final List<String> lines = readRawTextFile(this, R.raw.mean);
            for (final String line : lines) {
                sb.append(line);
            }
            final JSONObject meanJson = new JSONObject(sb.toString());
            mean = new HashMap<>();
            mean.put("b", (float) meanJson.optDouble("b"));
            mean.put("g", (float) meanJson.optDouble("g"));
            mean.put("r", (float) meanJson.optDouble("r"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readRawFile(Context ctx, int resId)
    {
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        int size = 0;
        byte[] buffer = new byte[1024];
        try (InputStream ins = ctx.getResources().openRawResource(resId)) {
            while((size=ins.read(buffer,0,1024))>=0){
                outputStream.write(buffer,0,size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public static List<String> readRawTextFile(Context ctx, int resId)
    {
        List<String> result = new ArrayList<>();
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;

        try {
            while (( line = buffreader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            return null;
        }
        return result;
    }
}
