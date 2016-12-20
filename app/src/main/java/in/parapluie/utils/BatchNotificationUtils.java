package in.parapluie.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class BatchNotificationUtils extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        String Tokens = FirebaseUtils.getInstance().getTokens();
        Log.d("tokens",Tokens.toString());
        String BATCH_API_KEY = "DEV5857E46E81E571DE1E199726489";
        String BATCH_REST_API_KEY = "722a39c52cddcd0a6bc66cb8bf58da71";
        String batchURL = "https://api.batch.com/1.1/" + BATCH_API_KEY + "/transactional/send";
        URL url = null;

        /*try {
            url = new URL(batchURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        try {
            url = new URL(batchURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("X-Authorization", BATCH_REST_API_KEY );
            String postJsonData = "{ \"group_id\": \"messages\", \"priority\":\"normal\", \"recipients\": { \"tokens\": ["+ Tokens +"] }, \"message\": { \"title\": \"Parapluie Stylist\", \"body\": \"You have new queries on the app!\" }, \"deeplink\": \"https://k2a92.app.goo.gl/Ubqx\", \"gcm_collapse_key\": { \"enabled\": true, \"key\": \"default\"}}";
            con.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(postJsonData);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.d("response","" + sb.toString());
            } else {
                Log.d("response",con.getResponseMessage());
            }
            Log.d("response code of post","" + responseCode);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
