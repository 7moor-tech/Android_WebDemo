package com.moor.webdemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.moor.webdemo.permission.OnRequestCallback;
import com.moor.webdemo.permission.PermissionConstants;
import com.moor.webdemo.permission.PermissionXUtil;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = findViewById(R.id.et_url);
        findViewById(R.id.tv_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //权限部分根据自己的需要进行选择
                //拍照,相册,文件等
                PermissionXUtil.checkPermission(MainActivity.this, new OnRequestCallback() {
                            @Override
                            public void requestSuccess() {
                                openWeb();
                            }
                        }, PermissionConstants.STORE,
                        PermissionConstants.CAMERA,
                        PermissionConstants.RECORD_AUDIO,
                        PermissionConstants.MODIFY_AUDIO);

            }
        });
    }

    public void openWeb() {
        String targetUrl = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(targetUrl)) {
            Toast.makeText(this.getApplicationContext(), "请填写地址", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, WebActivity.class);
            intent.putExtra("targetUrl", targetUrl);
            startActivity(intent);
        }
    }
}