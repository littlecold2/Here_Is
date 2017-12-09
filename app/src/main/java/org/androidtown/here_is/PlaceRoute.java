package org.androidtown.here_is;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by MIN on 2017-12-07.
 */

// 길찾기 관련 클래스
public class PlaceRoute {

    private List<Polyline> L_Poly; //경로 저장

    private String Dis; // 거리
    private String Dur; // 소요시간
    private String Bus; // 버스이름

    private Marker bus_marker; // 마커
    private GoogleMap map; //맵
    private LatLng loc1;
    private LatLng loc2;


    PlaceRoute()
    {
        L_Poly =new ArrayList<>();
    }
    PlaceRoute(GoogleMap map, LatLng loc1, LatLng loc2)
    {
        this.map = map;
        this.loc1 = loc1;
        this.loc2 = loc2;
        L_Poly =new ArrayList<>();
    }

    //길찾기 실행
    public void findRoute(GoogleMap map, LatLng loc1, LatLng loc2,Marker bus_marker)
    {
        this.map = map;
        this.loc1 = loc1;
        this.loc2 = loc2;
        this.bus_marker =bus_marker;
        String url = getUrl(loc1,loc2 ); // 마커 위치정보 넘겨줘서 맞는 url형식 만듬
        fetchUrl fUrl = new fetchUrl(); // fetch할 클래스 생성
        fUrl.execute(url); // url fetch
    }
    public void removePoly()
    {
        for (Polyline pl : L_Poly) {
            pl.remove();
        }
        L_Poly.clear();
    }

    private String getUrl(LatLng origin, LatLng dest) // 위치 두개 받아서 길찾기 URL 형식으로 바꿈  // 키 필요   Google Direction APi 이용
    {
        String url = "";
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        if()
        Log.d("l_d",Long.toString(System.currentTimeMillis()));
        url = "https://maps.googleapis.com/maps/api/directions/json?" +  str_origin +"&"+str_dest +"&mode=transit"+"&alternatives=true"+  "&key=AIzaSyC6tzB9C33kG_99yhC0L0jSKhK3KJHycSk";
        return url;
    }

    private class fetchUrl extends AsyncTask<String, Void, String> // AsyncTsk는 일종의 쓰레드 doInBackground 에서 PostExecute로 return값 넘겨줄수 있고, Post Execute는 ui컨트롤 부분 가능 Google Direction APi 이용
    {
        protected String doInBackground(String... url)
        {
            String data="";
            try {
                data = downloadUrl(url[0]); // URL 보내서 정보 받기
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;

        }
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }


    }// fetchUrl

    private String downloadUrl(String strUrl) throws IOException // 만든 URL 보내서 관련 정보 받아오기
    {
        String data = "";
        InputStream iStream = null;
        HttpsURLConnection urlConnection = null;
        Log.d("Url",strUrl);
        try{
            URL url = new URL(strUrl);

            // url 만들기
            urlConnection = (HttpsURLConnection) url.openConnection();

            // 연결
            urlConnection.connect();

            // 데이터 읽기
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//            BufferedReader br = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while((line = br.readLine()) != null) // 다 읽을 때 까지 버퍼에 계속 넣기
            {
                sb.append(line);
            }

            data = sb.toString(); // 버퍼에 쌓인 내용 저장
            Log.d("downloadUrl", data.toString());
            br.close();

        }
        catch (Exception e)
        {
            Log.d("Urlfail", "urldownloadfail");
        }
        finally
        {
            Log.d("Urlend", "end");
            iStream.close();;
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> // 맵에 길찾기 한 루트를 Polyline을 이용해 그려주고 소요시간, 거리 가져오는 함수 DataParser클래스를 이용해 JSON파싱한 내용을 이용한다. Google Direction APi 이용
    {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
//루트 관련 정보 저장
            JSONObject jObject_route;
            List<List<HashMap<String,String >>> routes = null;
            try {
                jObject_route = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject_route);

                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }// doinback

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);


                Log.d("d_parsing", "path size: " + Integer.toString(path.size()));
                // Fetching all the points in i-th route

                for (int j = 0; j < path.size(); j++) { // 패스 수 많금 포문
                    HashMap<String, String> point = path.get(j);

                    if(point.containsKey("Distance")||point.containsKey("Duration")) { // 거리나 소요시간 키를 가지고 있으면
                        Dis = point.get("Distance"); // 그 거리 정보 가져온다.
                        Dur = point.get("Duration"); // 그 소요시간 정보 가져온다.
                    }
                    else{
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                    //버스 노선 가져옴
                    if(point.containsKey("bus"))
                    {
                        Bus = point.get("bus");
                        bus_marker.setTitle("버스 노선: " +Bus);
                        bus_marker.setSnippet("거리: " +Dis+" , "+"소요 시간: "+Dur);
                        bus_marker.showInfoWindow();
                    }
                } // for
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.rgb(33, 142, 233));//8EC7fF

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                L_Poly.add(map.addPolyline(lineOptions));

            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }

    }// ParserTask


}
