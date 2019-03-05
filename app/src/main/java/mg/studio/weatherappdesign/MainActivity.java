package mg.studio.weatherappdesign;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnClick(null);
    }

    //Get the current time,and set the tv_data
    public void currentTime() {
        SimpleDateFormat dff = new SimpleDateFormat("yyyy/MM/dd   HH:mm:ss");
        dff.setTimeZone(TimeZone.getTimeZone("GMT+08"));  //Change the timezone
        Date curDate = new Date(System.currentTimeMillis());
        String str = dff.format(curDate);
        TextView curtime = (TextView) this.findViewById(R.id.tv_date);
        curtime.setText(str);
    }

    //get current day of week
    public static String getWeekDay(int off) {
        String result = new String("");
        final Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(System.currentTimeMillis());
        int day = instance.get(Calendar.DAY_OF_WEEK);
        int offDay = day + off;
        switch (offDay % 7) {
            case 0:
                result = "Sun";
                break;
            case 1:
                result = "Mon";
                break;
            case 2:
                result = "Tue";
                break;
            case 3:
                result = "Wed";
                break;
            case 4:
                result = "Thu";
                break;
            case 5:
                result = "Fri";
                break;
            case 6:
                result = "Sta";
                break;
        }
        return result;
    }

    //set the next four day of the week
    public void nextDayOfWeek() {
        String week1string = getWeekDay(-1);
        Log.i("week1：", week1string);
        ((TextView) findViewById(R.id.tv_week_1)).setText(week1string);

        String week2string = getWeekDay(0);
        ((TextView) findViewById(R.id.tv_week_2)).setText(week2string);

        String week3string = getWeekDay(1);
        ((TextView) findViewById(R.id.tv_week_3)).setText(week3string);

        String week4string = getWeekDay(2);
        ((TextView) findViewById(R.id.tv_week_4)).setText(week4string);

        String week5string = getWeekDay(3);
        ((TextView) findViewById(R.id.tv_week_5)).setText(week5string);
    }

    //Get Json data and set the img_weather_condition
    protected void setImgView(JSONArray jsonArray, int index) {
        try {
            JSONObject thisDay = jsonArray.getJSONObject(index * 8);
            String weather = thisDay.optString("weather").toString();
            JSONArray weatherArray = new JSONArray(weather);
            JSONObject weatherMain = weatherArray.getJSONObject(0);
            String result = weatherMain.optString("main");
            ImageView thisView;
            switch (index) {
                case 0:
                    thisView = (ImageView) findViewById(R.id.img_weather_condition_1);
                    break;
                case 1:
                    thisView = (ImageView) findViewById(R.id.img_weather_condition_2);
                    break;
                case 2:
                    thisView = (ImageView) findViewById(R.id.img_weather_condition_3);
                    break;
                case 3:
                    thisView = (ImageView) findViewById(R.id.img_weather_condition_4);
                    break;
                case 4:
                    thisView = (ImageView) findViewById(R.id.img_weather_condition_5);
                    break;
                default:
                    thisView = (ImageView) findViewById(R.id.img_weather_condition_1);
            }

            switch (result) {
                case "Clear":
                    thisView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.sunny_small));
                    break;
                case "Wind":
                    thisView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.windy_small));
                    break;
                case "Rain":
                    thisView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.rainy_small));
                default:
            }
        } catch (JSONException e) {
            Log.i("Error:", e.toString());
        }
    }

    //set the location
    public void setCityName(String cityName) {
        ((TextView) findViewById(R.id.tv_location)).setText(cityName);
    }

    //set the btnClick
    public void btnClick(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            new DownloadUpdate().execute();
            Toast.makeText(this, "Weather Updated!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "No Network!", Toast.LENGTH_SHORT).show();
            }
    }

    private class DownloadUpdate extends AsyncTask<String, Void, String> {

       protected  JSONArray arrayList;
        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = "http://api.openweathermap.org/data/2.5/forecast?q=Chongqing,cn&mode=json&APPID=aa3d744dc145ef9d350be4a80b16ecab";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            try {
                URL url = new URL(stringUrl);

                // Create the request to get the information from the server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Mainly needed for debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                try {
                    JSONObject root = new JSONObject(buffer.toString());

                    String list = root.optString("list").toString();
                    arrayList = new JSONArray(list);
                    JSONObject today = arrayList.getJSONObject(0);
                    String main = today.optString("main").toString();
                    JSONObject temp = new JSONObject(main);
                    String result = temp.optString("temp").toString();
                    Double tempData = Double.parseDouble(result);
                    tempData = tempData - 273.15;
                    result = String.valueOf(tempData.intValue());
                    return result;

                } catch (JSONException e) {
                    Log.i("Error:", e.toString());
                }
            }catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String temperature) {
            //Update the temperature displayed
            ((TextView) findViewById(R.id.temperature_of_the_day_1)).setText(temperature);
            for(int i=0;i<=4;i++){
                setImgView(arrayList,i);
            }
            setCityName("Chongqing");
            currentTime();
            nextDayOfWeek();
        }
    }
}

