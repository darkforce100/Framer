package edu.lonestar.framer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.service.dreams.DreamService;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import edu.lonestar.framer.util.RemoteImage;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class FramerDaydreamService extends DreamService {
    SharedPreferences sharedPref;
    static int color = Color.rgb(255,255,255);
    static boolean wantsAdaptive = false;
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        new DownloadDaemon().refresh();

        // Exit dream upon user touch?
        setInteractive(false);

        // Hide system UI?
        setFullscreen(true);

        // Keep screen at full brightness?
        setScreenBright(true);

        // Set the content view, just like you would with an Activity.
        setContentView(R.layout.framer_daydream);
        sharedPref = getApplication().getSharedPreferences("framer", Context.MODE_PRIVATE);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        //displayNewImage();
        //TODO set matting size due to overscan
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = null;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        }
        Point size = new Point();
        if (display != null) {
            display.getSize(size);
        }
        assert display != null;
        findViewById(R.id.bufferLeft).setMinimumWidth(findViewById(R.id.bufferLeft).getWidth()+(size.x*sharedPref.getInt("overscan", 0)/200));
        findViewById(R.id.bufferRight).setMinimumWidth(findViewById(R.id.bufferRight).getWidth()+(size.x*sharedPref.getInt("overscan", 0)/200));
        findViewById(R.id.bufferTop).setMinimumHeight(findViewById(R.id.bufferTop).getHeight()+(size.y*sharedPref.getInt("overscan", 0)/200));
        findViewById(R.id.bufferBottom).setMinimumHeight(findViewById(R.id.bufferBottom).getHeight()+(size.y*sharedPref.getInt("overscan", 0)/200));
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayNewImage();
            }
        }, 0);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void displayNewImage(){
        if (DownloadDaemon.obunfiltered.size() > 0) {
            int ran = new Random().nextInt(DownloadDaemon.obunfiltered.size()-2);
            RemoteImage imageToDisplay = DownloadDaemon.obunfiltered.elementAt(ran);
            new FramerDaydreamService.ImageDownloadTask().execute(imageToDisplay.getUrl());
            new ImageDownloadTask().execute(imageToDisplay.getUrl());
            ((TextView) findViewById(R.id.artistText)).setText(imageToDisplay.getArtist());
            ((TextView) findViewById(R.id.titleText)).setText(String.format(new Locale("en"), "%s (%d)", imageToDisplay.getTitle(), imageToDisplay.getYear()));
        } else {
            ((ImageView)findViewById(R.id.imageView)).setImageResource(R.drawable.framer_banner);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayNewImage();
                }
            }, 2000);
        }
        if (sharedPref.getBoolean("display_nameplate", false)) {
            findViewById(R.id.nameplateLayout).setVisibility(View.VISIBLE);
        } else findViewById(R.id.nameplateLayout).setVisibility(View.INVISIBLE);
        wantsAdaptive = sharedPref.getBoolean("adaptive_matting", false);
    }
    class ImageDownloadTask extends AsyncTask<String,Object,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0].trim());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                if (wantsAdaptive) {
                    color = calculateAverageColor(myBitmap, 1);
                } else {
                    color = Color.rgb(255,255,255);
                }
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            ((ImageView)findViewById(R.id.imageView)).setImageBitmap(result);
            findViewById(R.id.bufferBottom).setBackgroundColor(color);
            findViewById(R.id.bufferTop).setBackgroundColor(color);
            findViewById(R.id.bufferLeft).setBackgroundColor(color);
            findViewById(R.id.bufferRight).setBackgroundColor(color);
        }
    }

    public int calculateAverageColor(android.graphics.Bitmap bitmap, int pixelSpacing) {
        int R = 0; int G = 0; int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += pixelSpacing) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return Color.rgb(R / n, G / n, B / n);
    }

}
