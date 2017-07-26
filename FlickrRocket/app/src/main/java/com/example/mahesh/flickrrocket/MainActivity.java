package com.example.mahesh.flickrrocket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by MAHESH on 2/3/2016.
 */


public class MainActivity extends Activity {
    //ImageView variable to loads an image
    ImageView imageView;
    //Int Variable used to keep track of jsonarray value
    public static int count = 0;
    //EditText to get search bar value
    EditText searchBarText;
    //String variable to store search bar value
    public static String searchValue = null;
    //Button to direct search engine
    Button go_button;
    //Progress dialog to show loading time
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_load);
        //Initialize Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //Initialize button , imageview and searchbar text
        go_button = (Button) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.imageView);
        searchBarText = (EditText) findViewById(R.id.editText);
        //Set "rocket" as default value to search bar
        searchBarText.setText("rocket");
        //Set Properties for Edittext and ImageView
        searchBarText.setEnabled(true);
        imageView.setClickable(true);
        searchBarText.bringToFront();
        //Initially set image to ImageView
        imageView.setImageResource(R.drawable.searchimage);
        //Detect click on GO button
        go_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start display progress dialog untill image is ready to be loaded
                mProgressDialog.show();
                downloadfromSearchBarTask task = new downloadfromSearchBarTask();
                task.execute();
            }
        });
    }

    // Download Data from flickr site as a AsyncTask
    private class downloadfromSearchBarTask extends AsyncTask<String, String, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            //Initialize JSONOBJECT to save json data from flickr
            JSONObject jsonData;
            JSONObject jsonObject;
            JSONArray jsonArray = new JSONArray();
            try {
                //Get search engine value for url
                String val = getSearchhValue();
                //Set url
                URL url = new URL("https://api.flickr.com/services/rest/?format=json&sort=random&method=flickr.photos.search&tags=" + val + "&tag_mode=all&api_key=0e2b6aaf8a6901c264acb91f151a3350&nojsoncallback=1");
                //Download json data from flickr by creating HttpURLConnection
                HttpURLConnection c = null;
                c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.connect();
                int status = c.getResponseCode();
                //Receive data as inputstream
                InputStream inputStream = c.getInputStream();
                //Convert inputstream data to string
                String result = convertInputStreamToString(inputStream);
                //parse data to JSONOBJECT
                jsonObject = new JSONObject(result);
                //Extract "photos" object from jsonobject
                jsonData = jsonObject.getJSONObject("photos");
                //Extract "photo" array from jsondata
                jsonArray = jsonData.getJSONArray("photo");
                return jsonArray;
            } catch (MalformedURLException m) {
                m.printStackTrace();
            } catch (IOException io) {
                io.printStackTrace();
            } catch (JSONException je) {
                je.printStackTrace();
            }
            return jsonArray;
        }

        protected void onPostExecute(JSONArray jsonArray) {
            //Check if data is null
            if(jsonArray.length() == 0){
                //if null then display message
                Toast.makeText(getApplicationContext(), "No Data Available!!"+"/n"+"Give a New Search Item!!", Toast.LENGTH_SHORT).show();
                //stop the progess dialog
                mProgressDialog.dismiss();
            }else
            //Load image from json array
            load_Image(jsonArray);
        }

        //Method to convert the InputStream to String
        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;
            inputStream.close();
            return result;
        }

        //Method to load image
        private void load_Image(final JSONArray jsonArray) {
            //Load image as backGround thread ASYNCTASK
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    try {
                        int i = 0;
                        i = getCount();
                        if (i == jsonArray.length()) {
                            i = 0;
                            //Update keepcount function to keep track of iteration
                            keepCount(i);
                        } else {
                            //Update keepcount function to keep track of iteration
                            keepCount(i);
                        }
                        //Extract values from json array
                        String FARM_VALUE = jsonArray.getJSONObject(i).get("farm").toString();
                        String SERVER_VALUE = jsonArray.getJSONObject(i).get("server").toString();
                        String PHOTO_ID_VALUE = jsonArray.getJSONObject(i).get("id").toString();
                        String PHOTO_SECRET_VALUE = jsonArray.getJSONObject(i).get("secret").toString();

                        //Load values to image url
                        URL url = new URL("https://farm" + FARM_VALUE + ".static.flickr.com/" + SERVER_VALUE + "/" + PHOTO_ID_VALUE + "_" + PHOTO_SECRET_VALUE + "_m.jpg");
                        //Download image using HttpURLConnection
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        //Get response as inputstream
                        InputStream input = connection.getInputStream();
                        //Convert input values to bitmap values
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        return myBitmap;

                    } catch (JSONException js) {
                        js.printStackTrace();
                    } catch (IOException io) {
                        io.printStackTrace();
                    }

                    return null;
                }

                protected void onPostExecute(Bitmap myBitmap) {
                    //Dismiss progress dialog once the image is ready to be loaded
                    mProgressDialog.dismiss();
                    imageView.setClickable(true);
                    //Set image to imageview
                    imageView.setImageBitmap(myBitmap);

                    //Detect touch on imageview to load next image to imageview
                    imageView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            go_button.setClickable(true);
                            searchBarText.setClickable(true);
                            searchBarText.setEnabled(true);
                            int action = event.getActionMasked();
                            if (action == MotionEvent.ACTION_DOWN) {
                                load_Image(jsonArray);
                            }
                            return false;
                        }
                    });
                }
            }.execute();
        }

        //Method to return  the iteration value of jsonarray
        public int getCount() {
            return count;
        }

        //Method to keep updated iteration value of jsonarray
        public void keepCount(int value) {
            count = value;
            count++;
        }
    }

    //Method to retuen the search engine edittext value
    public String getSearchhValue() {
        searchValue = searchBarText.getText().toString();
        return searchValue;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


