package com.example.user.okhttp;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.FormBody;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Cookie;
import okhttp3.Request;
import okhttp3.RequestBody;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.HashMap;
import  java.security.NoSuchAlgorithmException;
import java.security.DigestException;
import java.lang.CloneNotSupportedException;


public enum NetworkService implements NetworkInterface {
    INSTANCE;
    private static final long CONNECT_TIMEOUT = 2000;   // 2 seconds
    private static final long READ_TIMEOUT = 2000;      // 2 seconds
    private static OkHttpClient okHttpClient = null;
    //private static final String SEARCH_URL = "https://api.myjson.com/bins/hoh4j/";
    private static final String SEARCH_URL = "https://projects.co.id";
    private static final String RECEIVE_URL ="https://pubsub.pubnub.com/subscribe";
    private static final String SEND_URL ="https://pubsub.pubnub.com/publish";
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    /**
     * Method to build and return an OkHttpClient so we can set/get
     * headers quickly and efficiently.
     * @return OkHttpClient
     */
    private OkHttpClient buildClient() {

        CookieJar cookieJar = new CookieJar() {

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                // here you get the cookies from Response
                if (cookieStore.isEmpty())
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        };

        if (okHttpClient != null) return okHttpClient;
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);

        // Logging interceptor
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor);

        // custom interceptor for adding header and NetworkMonitor sliding window
        okHttpClientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Add whatever we want to our request headers.
                Request request = chain.request().newBuilder().addHeader("Accept", "application/json").build();
                Response response;
                try {
                    response = chain.proceed(request);
                } catch (SocketTimeoutException | UnknownHostException e) {
                    e.printStackTrace();
                    throw new IOException(e);
                }
                return response;
            }
        });

        return  okHttpClientBuilder.build();
    }

    private Request.Builder buildRequest(URL url) {
        return new Request.Builder()
                .url(url);
    }

    private Request.Builder buildRequest(URL url, String credential) {
        return buildRequest(url).header("Authorization", credential);
    }

    private URL buildURL(Uri builtUrl) {
        if (builtUrl == null) return null;
        try {
            String urlStr = builtUrl.toString();
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private URL buildURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getData(Request request) {
        String result;
        OkHttpClient client = buildClient();
        try {
            Response response = client.newCall(request).execute();
            result = response.body().string();
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getString(String endpoint, String username, String password) {
        Log.d("NetworkService", "getString by username and password from " + endpoint);
        String credentials = username + ":" + password;
        final String basicAuth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Request request = buildRequest(buildURL(endpoint), basicAuth).build();
        return getData(request);
    }

    @Override
    public String getString(String endpoint, String token) {
        Log.d("NetworkService", "getString by Bearer token from " + endpoint);
        String credentials = "Bearer " + token;
        Request request = buildRequest(buildURL(endpoint), credentials).build();
        return getData(request);
    }

    @Override
    public String search(String query) {
        String dataParser = "";
       // Uri uri = Uri.parse(SEARCH_URL  + "/public/home/login?ajax=1")
        Uri uri = Uri.parse(SEARCH_URL  + "/public/home/login")
                .buildUpon()
                .appendQueryParameter("ajax", "1")
                .build();
        URL url = buildURL(uri);
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("LoginActivity[_trigger_]", "1");

     // dynamically add more parameter like this:
        formBuilder.add("LoginActivity[user_name]", "AhmadRiki");
        formBuilder.add("LoginActivity[password]", "Najla2012");
        formBuilder.add("LoginActivity[remember]", "1");
        RequestBody formBody = formBuilder.build();
        Log.d("NetworkService","built search url: " + url.toString());
        Request request = buildRequest(url)
                .post(formBody)
                .build();
        String jsonStr = getData(request);
        Log.e("", "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {

                JSONObject jsonObj = new JSONObject(jsonStr);
                String result = jsonObj.getString("result");
                String id = jsonObj.getString("user_id");
                String userName = jsonObj.getString("user_name");
                String userDisplay = jsonObj.getString("user_display");
                String userPhoto = jsonObj.getString("user_photo");
                // Getting JSON Array node
                // JSONArray contacts = jsonObj.getJSONArray("contacts");
                dataParser = result + id + userName + userDisplay + userPhoto;
            } catch (final JSONException e) {
                Log.e("", "Json parsing error: " + e.getMessage());
            }
        }
        return dataParser;
    }

    //@Override
    public String search1(String query) {

      //  String dataParser = "";
        // Uri uri = Uri.parse(SEARCH_URL  + "/public/home/login?ajax=1")
        Uri uri = Uri.parse(SEARCH_URL  + "/chatz/cometchat_receive.php")
                .buildUpon()
               // .appendQueryParameter("buddylist", "1")
               // .appendQueryParameter("initialize", "1")
                .build();
        URL url = buildURL(uri);
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("buddylist", "1");

        // dynamically add more parameter like this:
        formBuilder.add("initialize", "1");;
        RequestBody formBody = formBuilder.build();

        Log.d("NetworkService","built search url: " + url.toString());

        Request request = buildRequest(url)
                .post(formBody)
                .build();
        // return dataParser;
        return getData(request);
    }


    public String search2(String query) {

        String dataParser = "";
        final String SubKey = "sub-c-68f95436-1bca-11e5-ac22-0619f8945a4f" ;
        final String PubKey = "pub-c-8dca3ed2-5e08-4860-9b2d-1a4a5c3a9468";
        String Channel = "0" ;
        String UrlAdd = "";
        String uuid;
        boolean isJSON = true;
        String Message = "";
        uuid =  UUID.randomUUID().toString();
        long unixTime = System.currentTimeMillis() / 1000L;
        String SendTime = String.valueOf(unixTime);
        try {
            Channel = getChannel(101);
        }catch (NoSuchAlgorithmException e ) {
            e.fillInStackTrace();
        }
        JSONObject item = new JSONObject();
        try {
            item.put("from", "119");
            item.put("message", "Hallo, pubnub publish here!");
            item.put("sent", SendTime);
            item.put("self", 0);

        }
        catch (JSONException e) { isJSON = false;}

        if(! isJSON)
        {
            Message = "{}";

        } else
        {
            Message = item.toString();
        }

       // String message = "{"from":"101","message":"halooo","sent":"1523847717492","self":0}";

        UrlAdd = SEND_URL + "/" + PubKey + "/" + SubKey + "/" +"0" +"/"+ Channel +"/" + "0" + "/" + Message;
        // Uri uri = Uri.parse(SEARCH_URL  + "/public/home/login?ajax=1")
     //   Uri uri = Uri.parse(SEARCH_URL  + "/chatz/cometchat_send.php")
        Uri uri = Uri.parse(UrlAdd)
                .buildUpon()
               // .appendQueryParameter("uuid", uuid)
               // .appendQueryParameter("store", "0")
               // .appendQueryParameter("message", "Hallo world1")
                //.appendQueryParameter("basedata", "")
                .build();
        URL url = buildURL(uri);
      //  FormBody.Builder formBuilder = new FormBody.Builder()
               // .add("callback", "");
      //         .add("uuid", uuid);
      //  formBuilder.add("store", 0);
      //  formBuilder.add("pnsdk", "PubNub-JS-Web%2F3.5.43");
        // dynamically add more parameter like this:
      //  formBuilder.add("to", "101");;
      //  formBuilder.add("message", "Hallo world1");
      //  formBuilder.add("basedata", "");
       // RequestBody formBody = formBuilder.build();

        Log.d("NetworkService","sent message to pubnub url: " + url.toString());

        Request request = buildRequest(url)
                .addHeader("Content-Type", "application/json")
                .build();
        // return dataParser;
        return getData(request);
    }


    public List<String> search3(String token) {
        List<String> dataTemp = new ArrayList<String>();
        final String SubKey = "sub-c-68f95436-1bca-11e5-ac22-0619f8945a4f" ;
        String Channel = "0" ;
        String TimeToken ;
        String uuid;
        String UrlAdd = "";
        String Sent = "" ;
        uuid =  UUID.randomUUID().toString();
        try {
            Channel = getChannel(119);
        }catch (NoSuchAlgorithmException e ) {
            e.fillInStackTrace();
        }

        TimeToken = token;
        if (TimeToken != "")
        {
            UrlAdd = RECEIVE_URL + "/" + SubKey + "/" + Channel +"/" + "0" + "/" + TimeToken;
        }else
        {
            UrlAdd = RECEIVE_URL + "/" + SubKey + "/" + Channel +"/" + "0" + "/" + "0";
        }
        Uri uri = Uri.parse( UrlAdd)
                .buildUpon()
                .build();
        URL url = buildURL(uri);
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("uuid", uuid);


        RequestBody formBody = formBuilder.build();

        Log.d("Receive message","Receive message url: " + url.toString());

        Request request = buildRequest(url)
                .post(formBody)
                .build();

        String jsonStr = getData(request);

        Log.e("", "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {

                JSONArray jsonArr = new JSONArray(jsonStr);
                TimeToken = jsonArr.get(1).toString();
                if(jsonArr.get(0) != null)
                {
                    JSONArray jsonArr1 =  new JSONArray(jsonArr.get(0).toString());
                    if(jsonArr1.get(0) != null)
                    {
                        JSONObject jsonObject = jsonArr1.getJSONObject(0);
                        Sent = jsonObject.getString("sent");
                    }
                }
               // String result = jsonObj.getString("result");
              //  String id = jsonObj.getString("user_id");
              //  String userName = jsonObj.getString("user_name");
              //  String userDisplay = jsonObj.getString("user_display");
              //  String userPhoto = jsonObj.getString("user_photo");
                // Getting JSON Array node
                // JSONArray contacts = jsonObj.getJSONArray("contacts");
              //  dataParser = result + id + userName + userDisplay + userPhoto;
            } catch (final JSONException e) {
                Log.e("", "Json parsing error: " + e.getMessage());

            }
        }
        else
        {
            jsonStr = "";
        }
        String SendDate = "";
        dataTemp.add(TimeToken);
        if (Sent!="")
        {
             long sentdate = Long.parseLong(Sent);
             java.util.Date time=new java.util.Date((long)sentdate);
             SendDate = time.toString();
        }

        dataTemp.add(SendDate);
        return dataTemp;
    }


    public String getChannel (int ID) throws NoSuchAlgorithmException{
        final String KeyA = "pub-c-8dca3ed2-5e08-4860-9b2d-1a4a5c3a9468";
        final String KeyB = "sub-c-68f95436-1bca-11e5-ac22-0619f8945a4f";
        final String KeyC = "306ae8b65cae7c87e40a9d511deaed00";
        String Channel = (Integer.toString(ID) + KeyA + KeyB + KeyC);
        try {
              MessageDigest md = MessageDigest.getInstance("MD5");
              md.update(Channel.getBytes());
            byte byteData[] = md.digest();
            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            Log.e("","Digest(in hex format):: " + sb.toString());
            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<byteData.length;i++) {
                String hex=Integer.toHexString(0xff & byteData[i]);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }
            Channel = hexString.toString().toLowerCase();
         }  catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
       // md.update(Channel.getBytes());
       // byte byteData[] = md.digest();
        return Channel ;
    }

}
