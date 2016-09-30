package com.example.akash.wynkassignment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public ImageView icon;
    public Button prevButton;
    public Button nextButton;
    public ProgressBar progressBar;
    public static List<ImageData> imageDataList;
    public ArrayList<String> imageList = new ArrayList<>();
    public int counter = 0;
    public static final Handler messageHandler = new Handler() {

        public void handleMessage(Message msg) {
            final int what = msg.what;
            switch (what) {
                case 1:
                    ImageData result = (ImageData) msg.obj;
                    imageDataList.add(result);

                case 2:
                    ImageData resulthome = (ImageData) msg.obj;
                    imageDataList.add(resulthome);

                    /*doUpdate(result);*/
                    break;
            }
        }
    };


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.bt_prev:
                goTopreviouspage();
                break;
            case R.id.bt_next:
                goTonextPage();
                break;
        }
    }

    private void goTonextPage() {
        counter++;
        if (counter < imageList.size() && counter > 0) {
            ImageData imageData = imageDataList.get(counter);
            if (imageData.isDownladed) {
                hideLoading();
                icon.setImageBitmap(imageData.getBitmapResource());
            } else {
                showLoading();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Invalid page", Toast.LENGTH_LONG).show();
        }


    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void goTopreviouspage() {
        counter--;
        if (counter < imageList.size() && counter >= 0) {
            ImageData imageData = imageDataList.get(counter);
            if (imageData.isDownladed) {
                hideLoading();
                icon.setImageBitmap(imageData.getBitmapResource());
            } else {
                showLoading();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Invalid page", Toast.LENGTH_LONG).show();
        }

    }

    static class ImageData {
        private String requestID;
        private Bitmap bitmapResource;
        boolean isDownladed;
        public boolean isDownloadStarted;

        public ImageData(String requestID, Bitmap bitmapResource, boolean isDownladed) {
            this.requestID = requestID;
            this.bitmapResource = bitmapResource;
            this.isDownladed = isDownladed;
        }

        public ImageData() {

        }


        public String getRequestID() {
            return requestID;
        }

        public void setRequestID(String requestID) {
            this.requestID = requestID;
        }

        public Bitmap getBitmapResource() {
            return bitmapResource;
        }

        public void setBitmapResource(Bitmap bitmapResource) {
            this.bitmapResource = bitmapResource;
        }

        public boolean isDownladed() {
            return isDownladed;
        }

        public void setDownladed(boolean downladed) {
            isDownladed = downladed;
        }
    }


    class GetJsonData extends AsyncTask<String, Void, String> {
        private MainActivity mainActivity;
        private String url;


        public ArrayList<String> getImageList() {
            return imageList;
        }


        public GetJsonData(MainActivity mainActivity, String s) {
            this.mainActivity = this.mainActivity;
            this.url = url;
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        public String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            URL url = null;
            String object = null;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL("http://api.flickr.com/services/feeds/photos_public.gne?format=json");
          /*  Log.e(NetworkConst.TAG_REST + getClass().getSimpleName(), "doInBackground() URL :> " + url);*/

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                urlConnection.connect();

                InputStream inStream = null;
                inStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return (result.toString());
        }


        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
       /* result = "{" + result;
        result = result + "}";*/
            result = result.substring(16);
            result = "{" + result;
            result = result + "}";
            JSONObject baseObject = null;
            JSONArray jArray = null;
            JSONObject mediaobject = null;
            ArrayList<JSONObject> medialist = new ArrayList<>();

            try {
                baseObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (baseObject != null)
                try {
                    jArray = baseObject.getJSONArray("items");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject oneObject = jArray.getJSONObject(i);
                    // Pulling items from the array
                    medialist.add(oneObject.getJSONObject("media"));
                } catch (JSONException e) {
                    // Oops
                }
            }

            for (int i = 0; i < medialist.size(); i++) {
                JSONObject oneObject = medialist.get(i);
                try {
                    imageList.add(oneObject.getString("m"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (imageList != null && !imageList.isEmpty()) {
                downloadImages(imageList);
            }
            //Do something with the JSON string

        }
    }

    private void downloadImages(ArrayList<String> imageList) {
        imageDataList = new ArrayList<>();
        for (int i = 0; i < imageList.size(); i++) {
            getBitmapFromURL(imageList.get(i));
        }
    }


    public static void getBitmapFromURL(final String src) {
        final ImageData[] imageData = new ImageData[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    imageData[0] = new ImageData(src, bitmap, true);
                    Message message = new Message();
                    message.what = 1;
                    message.obj = imageData[0];
                    messageHandler.sendMessage(message);


                } catch (IOException e) {
                    Message message = new Message();
                    message.what = 1;
                    // Log exception
                    message.obj = null;

                }
            }
        });
        t1.start();
    }

    public static void getHomePageImage(final String src) {
        final ImageData[] imageData = new ImageData[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    imageData[0] = new ImageData(src, bitmap, true);
                    Message message = new Message();
                    message.what = 2;
                    message.obj = imageData[0];
                    messageHandler.sendMessage(message);


                } catch (IOException e) {
                    Message message = new Message();
                    message.what = 1;
                    // Log exception
                    message.obj = null;

                }
            }
        });
        t1.start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        icon = (ImageView) findViewById(R.id.iv_icon);
        prevButton = (Button) findViewById(R.id.bt_prev);
        nextButton = (Button) findViewById(R.id.bt_next);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading);
        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        GetJsonData myTask = new GetJsonData(this, "");
        myTask.execute();

    }
}
