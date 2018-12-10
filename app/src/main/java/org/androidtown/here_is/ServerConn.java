package org.androidtown.here_is;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.ContactsContract;

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

// 서버와 데이터 통신을 하는 클래스
// 구글링을 통해 가져다 쓴 클래스로써 정확하게 모든 구조를 알지 못하고 변경한 부분만 주석처리 했습니다.
public class ServerConn extends AsyncTask<String, Void, String> {
    Context context;
    //AlertDialog alertDialog;




    ServerConn(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        //params를 통해서 type과 그에 맞는 정보들을 받음
        //받은 항목중 인덱스 0번이 type이므로 type 변수에 저장
        String type = params[0];

        //만약 로그인 시
        if (type.equals("login")) {
            String login_url = "http://littlecold2.iptime.org/here_is/login_json.php";
//            String login_url = "http://52.78.20.5/here/login_json.php";
            //서버에 있는 로그인 php와 통신하기 위해서 경로 지정
            try {
                //로그인 시 id, pw만 입력값으로 필요하므로 각 값을 변수에 저장
                String user_id = params[1];
                String user_pw = params[2];
                URL url = new URL(login_url);

                //데이터 통신을 하기위한 연결
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                //url을 통해서 보낼 데이터를 id = 'id' & pw = 'pw' 식으로 만들어서 인코딩
                String post_data = URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8") + "&" + URLEncoder.encode("user_pw", "UTF-8") + "=" + URLEncoder.encode(user_pw, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String result = "";
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result +=  line;
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

        //회원가입 시
        if (type.equals("signup")) {
            String signup_url = "http://littlecold2.iptime.org/here_is/signup_json.php";
            try {
                String id = params[1];
                String pw = params[2];
                String name = params[3];
                String info = params[4];
                String youtube = params[5];
                String index = params[6];
                URL url = new URL(signup_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8") + "&"
                        + URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(pw, "UTF-8") + "&"
                        + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8") + "&"
                        + URLEncoder.encode("info", "UTF-8") + "=" + URLEncoder.encode(info, "UTF-8") + "&"
                        + URLEncoder.encode("youtube", "UTF-8") + "=" + URLEncoder.encode(youtube, "UTF-8") + "&"
                        + URLEncoder.encode("index", "UTF-8") + "=" + URLEncoder.encode(index, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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

        //회원정보 수정 시
        if (type.equals("edit_profile")) {
            String signup_url = "http://littlecold2.iptime.org/here_is/edit_profile_json.php";
            try {
                String id = params[1];
                String pw = params[2];
                String name = params[3];
                String info = params[4];
                String youtube = params[5];
                String index = params[6];
                URL url = new URL(signup_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                String post_data = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8") + "&"
                        + URLEncoder.encode("pw", "UTF-8") + "=" + URLEncoder.encode(pw, "UTF-8") + "&"
                        + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8") + "&"
                        + URLEncoder.encode("info", "UTF-8") + "=" + URLEncoder.encode(info, "UTF-8") + "&"
                        + URLEncoder.encode("youtube", "UTF-8") + "=" + URLEncoder.encode(youtube, "UTF-8") + "&"
                        + URLEncoder.encode("index", "UTF-8") + "=" + URLEncoder.encode(index, "UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
