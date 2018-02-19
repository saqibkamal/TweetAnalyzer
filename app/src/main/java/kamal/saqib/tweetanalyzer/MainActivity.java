package kamal.saqib.tweetanalyzer;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText hastag;
    Button submit;
    ListView listView,listView2;
    String[] tweets;
    ArrayList<String> results;
    ArrayList<String> tweet,pos_tweet,neg_tweet;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hastag=findViewById(R.id.editText);
        submit = findViewById(R.id.button);
        listView = findViewById(R.id.listview);
        listView2=findViewById(R.id.listview2);

        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == submit){
            String tag=hastag.getText().toString();
            if(tag!=null && tag.length()>0){
                if((tag.charAt(0)!='#'))
                    tag='#'+tag;

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setTitle("Processing. It may take a bit...");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                GetTweet getTweet = new GetTweet();
                getTweet.execute(tag);
            }
        }
    }

    public void finelly(){
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, pos_tweet);
        listView.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, neg_tweet);
        listView2.setAdapter(adapter2);
        progressDialog.dismiss();
    }


    public  class GetTweet extends AsyncTask<String ,Void,Void> {
        String tag,result;


        @Override
        protected Void doInBackground(String... strings) {

            tag = strings[0];

            HttpClient httpclient;
                    HttpResponse response = null;
                    result = "";
                    try {

                        httpclient = new DefaultHttpClient();
                        HttpPost post = new HttpPost("http://10.0.2.2:5000/get_tweet");
                        JSONObject json = new JSONObject();
                        json.put("hashtag", tag);
                        StringEntity se;
                        se = new StringEntity(json.toString());
                        post.setEntity(se);

                        post.setHeader("Content-type", "application/json");
                        response = httpclient.execute(post);
                        Log.i("Sending", "Complete");

                    } catch (Exception e) {
                        result = "Sending Error";
                        Log.i("Error",result);
                    }



                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));
                        String line = "";
                        String temp="";
                        while ((line = rd.readLine()) != null) {
                            Log.i("MSf",line);
                            temp = temp + line;
                        }




                        temp=temp.replace("\\u"," ");
                        temp=temp.replace("\\n"," ");

                        tweets=temp.split(" ~~~~~~~~~~~~~~~ ");

                        tweet=new ArrayList<>();
                        for(int i=1;i<tweets.length;i++)
                            tweet.add(tweets[i]);
                        for(int i=0;i<tweet.size();i++)
                            Log.i("Tweet",tweet.get(i));


                        Log.i("Recieving Complete","Total tweets recieved = "+ tweet.size() );
                    } catch (Exception e) {
                        result = "Recieving error";
                        Log.i("Error", e.getMessage());
                    }

                    return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("All process","Completed");


                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, tweet);
                listView.setAdapter(adapter);

                GetPredictions getPredictions = new GetPredictions();
                getPredictions.execute();



        }


    }

    public  class GetPredictions extends AsyncTask<String ,Void,Void> {
        String result;
        @Override
        protected Void doInBackground(String... strings) {

            HttpClient httpclient;
            HttpResponse response = null;
            results=new ArrayList<>();
            pos_tweet=new ArrayList<>();
            neg_tweet=new ArrayList<>();
            result = "";
               for(int i=0;i<tweet.size();i++){
                    try {

                        httpclient = new DefaultHttpClient();
                        HttpPost post = new HttpPost("http://10.0.2.2:5000/predict");
                        JSONObject json = new JSONObject();
                        json.put("tweetjson", tweet.get(i));
                        StringEntity se;
                        se = new StringEntity(json.toString());
                        post.setEntity(se);

                        post.setHeader("Content-type", "application/json");
                        response = httpclient.execute(post);


                    } catch (Exception e) {
                        result = "Sending Error";
                        Log.i("Error",result);
                    }

                    try {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(
                                response.getEntity().getContent()));
                        String line = "";
                        while ((line = rd.readLine()) != null) {
                            Log.i("line read",line);
                            results.add(line);
                            if(line.contains("0"))
                                neg_tweet.add(tweet.get(i));
                            else if(line.contains("1"))
                                pos_tweet.add(tweet.get(i));

                        }
                    } catch (Exception e) {
                        result = "Recieving error";
                        Log.i("Error", result);
                    }
                }
            Log.i("Sending & Recieving", "Complete");

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Prediction Completed","Pos = "+pos_tweet.size() + " Neg = "+ neg_tweet.size());
            finelly();
        }


    }







}
