package org.androidtown.here_is;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by syseng on 2017-10-30.
 */

public class ServerConn extends AsyncTask<String, Void, String> {
        Context context;
        //AlertDialog alertDialog;

        ServerConn (Context ctx) {
        context = ctx;
        }

@Override
protected String doInBackground(String... params) {
        String type = params[0];
        if(type.equals("login")) {
        String login_url = "http://52.78.20.5/here/login.php";
        try {
        String user_id = params[1];
        String user_pw = params[2];
        URL url = new URL(login_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        String post_data = URLEncoder.encode("user_id", "UTF-8")+"="+URLEncoder.encode(user_id, "UTF-8")+"&"+URLEncoder.encode("user_pw", "UTF-8")+"="+URLEncoder.encode(user_pw, "UTF-8");
        bufferedWriter.write(post_data);
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStream.close();
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
        String result="";
        String line;
        while((line = bufferedReader.readLine()) != null) {
        result += line;
        }
        bufferedReader.close();
        inputStream.close();
        httpURLConnection.disconnect();
        return result;
        } catch (MalformedURLException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }

        if(type.equals("signup")) {
        String signup_url = "http://52.78.20.5/here/signup.php";
        try {
        String id = params[1];
        String pw = params[2];
        String name = params[3];
        String info = params[4];
        String index = params[5];
        URL url = new URL(signup_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        OutputStream outputStream = httpURLConnection.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8") + "&" + URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(pw, "UTF-8") + "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8") + "&" + URLEncoder.encode("info", "UTF-8") + "=" + URLEncoder.encode(info, "UTF-8") + "&" + URLEncoder.encode("index", "UTF-8") + "=" + URLEncoder.encode(index, "UTF-8");
        bufferedWriter.write(post_data);
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStream.close();
        InputStream inputStream = httpURLConnection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
        String result = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
        result += line;
        }
        bufferedReader.close();
        inputStream.close();
        httpURLConnection.disconnect();
        return result;
        } catch (MalformedURLException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        }
        return null;
        }

@Override
protected void onPreExecute() {
        //alertDialog = new AlertDialog.Builder(context).create();
        //alertDialog.setTitle("Login Status");
        //super.onPreExecute();
        }

@Override
protected void onPostExecute(String result) {
        //alertDialog.setMessage(result);
        //alertDialog.show();
        super.onPostExecute(result);
        }

@Override
protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        }
        }
