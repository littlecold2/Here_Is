package org.androidtown.here_is;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebviewActivity extends AppCompatActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent intent = getIntent();
        String loc= intent.getExtras().get("loc").toString();

        mWebView = new WebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        final AppCompatActivity activity = this;

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });
        // 바로 네이버 검색 할 수 있게 함
        mWebView .loadUrl("http://search.naver.com/search.naver?where=nexearch&query="+loc);
        setContentView(mWebView );

    }

           private class WebViewClientClass extends WebViewClient {

        // 앱내부에서 바로 보여주게
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    // 웹뷰에서 뒤로가기 버튼 눌럿을경우 웹페이지에서 반응하게
           @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
