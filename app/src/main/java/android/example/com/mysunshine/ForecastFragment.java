package android.example.com.mysunshine;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mail on 10/9/2016.
 */
public class ForecastFragment extends Fragment implements IFetchWeatherTaskCallBack {

    public static final String[] listitems = {
            "Today - Sunny - 88/63"
            ,"Tommorow - Cloudy - 38/13"
            ,"Teusday - Rainy - 36/11"
    };
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> mForecastAdapter;
    private List<String> weekForecast;
    private ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//         String[] data = {
//                "Mon 6/23 - Sunny - 31/17",
//                "Tue 6/24 - Foggy - 21/8",
//                "Wed 6/25 - Cloudy - 22/17",
//                "Thurs 6/26 - Rainy - 18/11",
//                "Fri 6/27 - Foggy - 21/10",
//                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
//                "Sun 6/29 - Sunny - 20/7"
//        };
//        weekForecast = new ArrayList<String>(Arrays.asList(data));
//        mForecastAdapter = new ArrayAdapter<String>(
//               getActivity(), // The current context (this activity)
//               R.layout.list_item_forcast, // The name of the layout ID.
//               R.id.list_item_forecast_textview, // The ID of the textview to populate.
//                weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container);

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
//        listView.setAdapter(mForecastAdapter);


        refeshData();

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_menu_item:
               refeshData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void refeshData() {
        FetchWeatherTask getJsonDataTask = new FetchWeatherTask();
        getJsonDataTask.execute("91210");
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecasefragment, menu);
    }

    public ForecastFragment() {
    }

    @Override
    public void FetchWeatherTaskCallBack(String[] data) {
        weekForecast = new ArrayList<String>(Arrays.asList(data));
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(), // The current context (this activity)
                R.layout.list_item_forcast, // The name of the layout ID.
                R.id.list_item_forecast_textview, // The ID of the textview to populate.
                weekForecast);
        listView.setAdapter(mForecastAdapter);
        mForecastAdapter.notifyDataSetChanged();
    }

    private class FetchWeatherTask extends AsyncTask<String,String,String[]>{
        private static final String QUERY_PARAM = "q";
        private static final String QUERY_POSTAL_VALUE = "91210";
        private static final String QUERY_COUNTRYCODE_VALUE = "FR";
        private static final String FORMAT_PARAM = "mode";
        private static final String FORMAT_VALUE = "json";
        private static final String UNITS_PARAM = "units";
        private static final String UNITS_VALUE = "metric";
        private static final String DAYS_PARAM = "cnt";
        private static final String DAYS_VALUE = "7";
        private static final String API_ID_PARAM = "appid";
        private static final String API_ID_VALUE = "59acb2f132f8716049682079bd97cc28";

        private static final String BASE_DATA_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();


        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            ForecastFragment.this.FetchWeatherTaskCallBack(strings);
        }

        @Override
        protected String[] doInBackground(String... params) {

         return getJsonData(params[0]);
        }

        private String[] getJsonData(String postalCode){

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                Uri dataUri = Uri.parse(BASE_DATA_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, postalCode + "," + QUERY_COUNTRYCODE_VALUE)
                        .appendQueryParameter(FORMAT_PARAM, FORMAT_VALUE)
                        .appendQueryParameter(UNITS_PARAM, UNITS_VALUE)
                        .appendQueryParameter(DAYS_PARAM, DAYS_VALUE)
                        .appendQueryParameter(API_ID_PARAM, API_ID_VALUE)
                        .build();

                URL url = new URL(dataUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
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
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "forecastJsonStr: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, Integer.valueOf(DAYS_VALUE));
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }


        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

    }

}
