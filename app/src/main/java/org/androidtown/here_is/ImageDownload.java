package org.androidtown.here_is;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by MIN on 2018-05-03.
 */

public class ImageDownload extends AsyncTask<String, Void, Void> {


    private SharedPreferences userinfo;
    private String ID;
    private String fileName;
    String savePath = "";
    String fileUrl ="";
    String localPath = "";

    Context context;

    ImageDownload(Context context, String ID)
    {
        this.context = context;
        userinfo =  context.getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        this.ID = ID;
    }
    ImageDownload(Context context)
    {
        this.context = context;
        userinfo =  context.getSharedPreferences("userinfo", Activity.MODE_PRIVATE);
        this.ID ="";
    }

    @Override
    protected Void doInBackground(String... params) {

        //다운로드 경로를 지정
//        String savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;

        File dir = new File(savePath);
        //상위 디렉토리가 존재하지 않을 경우 생성
        if (!dir.exists()) {

            dir.mkdirs();

        }
        //파일 이름 :날짜_시간
        Date day = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
        fileName = ID;
//        fileName = String.valueOf(sdf.format(day));

        switch (params[1]) {
            case "my":
                savePath= context.getFilesDir().getAbsolutePath();
                break;
            case "other":
                savePath= context.getCacheDir().getAbsolutePath();
                break;
        }
        Log.d("img",savePath);

        //웹 서버 쪽 파일이 있는 경로
        fileUrl = params[0]+ "/" + fileName + ".jpg";;
        Log.d("img file path",fileUrl);

        // 로컬 경로
        localPath = savePath + "/" + fileName + ".jpg";
        Log.d("img local ",localPath);


        //다운로드 폴더에 동일한 파일명이 존재하는지 확인
        if (new File(savePath + "/" + fileName).exists()) {}

        else {

            try {
                URL imgUrl = new URL(fileUrl);
                //서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                int len = conn.getContentLength();
                byte[] tmpByte = new byte[len];
                //입력 스트림을 구한다
                InputStream is = conn.getInputStream();
                File file = new File(localPath);
                //파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(file); //
//            FileOutputStream fos = null;
//            fos = context.openFileOutput(fileName+".jpg", Context.MODE_PRIVATE);
                Log.d("fos", localPath);
                int read;

                //입력 스트림을 파일로 저장
                for (; ; ) {
                    read = is.read(tmpByte);
                    if (read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, read); //file 생성
                }
                Log.d("fos", localPath + "download fin");

                is.close();
                fos.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Bitmap img;
        try {
            File file = new File(savePath + "/" + fileName + "_s.jpg");
            FileOutputStream out=new FileOutputStream(file);


             img = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(localPath) ,100,100,true);
             img.compress(Bitmap.CompressFormat.JPEG,100,out);
             out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        출처: http://lueseypid.tistory.com/15 [감성 개발자!]



//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
//        Bitmap src = BitmapFactory.decodeFile(savePath + "/" + fileName + ".jpg", options);
//        출처: http://it77.tistory.com/99 [시원한물냉의 사람사는 이야기]

//        //저장한 이미지 열기
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        String targetDir = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;
//        File file = new File(targetDir + "/" + fileName + ".jpg");
//        //type 지정 (이미지)
//        i.setDataAndType(Uri.fromFile(file), "image/*");
//        getApplicationContext().startActivity(i);
//        //이미지 스캔해서 갤러리 업데이트
//        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
    }


    public void setID(String ID) {
        this.ID = ID;
    }
}

