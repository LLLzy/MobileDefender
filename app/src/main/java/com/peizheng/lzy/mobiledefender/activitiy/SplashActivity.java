package com.peizheng.lzy.mobiledefender.activitiy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.FileCallback;
import com.peizheng.lzy.mobiledefender.R;
import com.peizheng.lzy.mobiledefender.utils.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {
    private static final int CODE_UPDATE_DIALOG = 0;
    private static final int CODE_URL_ERROR = 1;
    private static final int CODE_CONN_ERROR = 2;
    private static final int CODE_JSON_ERROR = 3;
    private static final int CODE_ENTER_HOME = 4;
    private int mVersionCode;
    private String mVersionName;
    private String mDescription;
    private String mDownloadUrl;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_UPDATE_DIALOG:
                    showUpdateDialog();
                    break;
                case CODE_JSON_ERROR:
                    Toast.makeText(SplashActivity.this, "数据分析失败", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case CODE_CONN_ERROR:
                    Toast.makeText(SplashActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case CODE_URL_ERROR:
                    Toast.makeText(SplashActivity.this, "网络地址错误", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case CODE_ENTER_HOME:
                    enterHome();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        tv_version.setText(getVersionName());
        checkVersion();

    }

    private void checkVersion() {
        new Thread() {
            Message msg = Message.obtain();
            final long startTime = System.currentTimeMillis();

            @Override
            public void run() {
                try {

                    URL url = new URL("http://10.18.57.8:8080/update.json");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inputstream = conn.getInputStream();
                        String result = StreamUtils.readFromStream(inputstream);
                        JSONObject jsonObject = new JSONObject(result);

                        mVersionCode = jsonObject.getInt("versionCode");
                        mVersionName = jsonObject.getString("versionName");
                        mDescription = jsonObject.getString("description");
                        mDownloadUrl = jsonObject.getString("downloadUrl");

                        if (getVersionCode() < mVersionCode) {

                            msg.what = CODE_UPDATE_DIALOG;
                        } else {
                            msg.what = CODE_ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    msg.what = CODE_URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    msg.what = CODE_CONN_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    msg.what = CODE_JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    long endTime = System.currentTimeMillis();
                    long usedTime = endTime - startTime;
                    if (usedTime < 2000) {
                        try {
                            sleep(2000 - usedTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                }

            }
        }.start();

    }

    private String getVersionName() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本更新:" + mVersionName);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(mDescription);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                download();
            }
        });

        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHome();
            }
        });
        builder.show();

    }

    protected void download() {
        final TextView tv_progress = (TextView) findViewById(R.id.tv_progress);
        tv_progress.setVisibility(View.VISIBLE);
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                OkHttpUtils.get(mDownloadUrl).execute(new FileCallback(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "MobileDefender.apk") {
                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        Toast.makeText(SplashActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess(File file, Call call, Response response) {
                        Toast.makeText(SplashActivity.this, "下载成功", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void downloadProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                        tv_progress.setText(progress * 100 + "%" + "  " + networkSpeed / 1000 + "K/s");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(SplashActivity.this, "sd卡不存在", Toast.LENGTH_SHORT).show();
        }
    }

    void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

}
