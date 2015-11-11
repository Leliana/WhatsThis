package com.happen.it.make.whatisit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WhatsActivity extends AppCompatActivity {

    private TextView resultTextView;
    private ImageView inputImageView;
    private Bitmap bitmap;
    private Bitmap processedBitmap;
    private Button identifyButton;
    private SharedPreferences sharedPreferences;
    private String currentPhotoPath;
    private static final String PREF_USE_CAMERA_KEY = "USE_CAMERA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats);
        identifyButton = (Button)findViewById(R.id.identify_button);
        inputImageView = (ImageView)findViewById(R.id.tap_to_add_image);
        resultTextView = (TextView)findViewById(R.id.result_text);
        sharedPreferences = getSharedPreferences("Picture Pref", Context.MODE_PRIVATE);

        identifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != identifyButton) {
                    return;
                }
                if (processedBitmap == null) {
                    return;
                }

                new AsyncTask<Bitmap, Void, String>(){
                    @Override
                    protected void onPreExecute() {
                        resultTextView.setText("Calculating...");
                    }

                    @Override
                    protected String doInBackground(Bitmap... bitmaps) {
                        synchronized (identifyButton) {
                            String tag = MxNetUtils.identifyImage(bitmaps[0]);
                            return tag;
                        }
                    }
                    @Override
                    protected void onPostExecute(String tag) {
                        resultTextView.setText(tag);
                    }
                }.execute(processedBitmap);
            }
        });
        inputImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != inputImageView) {
                    return;
                }
                final boolean useCamera = sharedPreferences.getBoolean(PREF_USE_CAMERA_KEY, false);
                if (useCamera) {
                    dispatchTakePictureIntent();
                } else {
                    final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, Constants.SELECT_PHOTO_CODE);
                }
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, Constants.CAPTURE_PHOTO_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (processedBitmap != null) {
            inputImageView.setImageBitmap(processedBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_whats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_use_camera) {
            sharedPreferences.edit().putBoolean(PREF_USE_CAMERA_KEY, true).apply();
            return true;
        } else if (id == R.id.action_use_gallery) {
            sharedPreferences.edit().putBoolean(PREF_USE_CAMERA_KEY, false).apply();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case Constants.SELECT_PHOTO_CODE:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        bitmap = BitmapFactory.decodeStream(imageStream);
                        processedBitmap = processBitmap(bitmap);
                        inputImageView.setImageBitmap(processedBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case Constants.CAPTURE_PHOTO_CODE:
                if (resultCode == RESULT_OK) {
                    bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    processedBitmap = processBitmap(bitmap);
                    inputImageView.setImageBitmap(bitmap);
                }
                break;
        }
    }

    static final int SHORTER_SIDE = 256;
    static final int DESIRED_SIDE = 224;

    private static Bitmap processBitmap(final Bitmap origin) {
        //TODO: error handling
        final int originWidth = origin.getWidth();
        final int originHeight = origin.getHeight();
        int height = SHORTER_SIDE;
        int width = SHORTER_SIDE;
        if (originWidth < originHeight) {
            height = (int)((float)originHeight / originWidth * width);
        } else {
            width = (int)((float)originWidth / originHeight * height);
        }
        final Bitmap scaled = Bitmap.createScaledBitmap(origin, width, height, false);
        int y = (height - DESIRED_SIDE) / 2;
        int x = (width - DESIRED_SIDE) / 2;
        return Bitmap.createBitmap(scaled, x, y, DESIRED_SIDE, DESIRED_SIDE);
    }
}
