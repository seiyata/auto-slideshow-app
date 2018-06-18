package jp.techacademy.tajikara.seiya.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Timer timer;
    Handler handler = new Handler();

    ImageView imageView;

    Button prevButton;
    Button playButton;
    Button nextButton;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        prevButton = findViewById(R.id.prevButton);
        playButton = findViewById(R.id.playButton);
        nextButton = findViewById(R.id.nextButton);
        imageView = findViewById(R.id.imageView);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showNextImage();
                                }
                            });
                        }
                    }, 2000, 2000);
                    playButton.setText("停止");
                    prevButton.setEnabled(false);
                    nextButton.setEnabled(false);
                } else {
                    timer.cancel();
                    timer = null;
                    playButton.setText("再生");
                    prevButton.setEnabled(true);
                    nextButton.setEnabled(true);
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrevImage();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextImage();
            }
        });

        cursor.moveToFirst();
        showImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        cursor.close();
    }

    private void showNextImage() {
        if (cursor.isLast()) {
            cursor.moveToFirst();
        } else {
            cursor.moveToNext();
        }
        showImage();
    }

    private void showPrevImage() {
        if (cursor.isFirst()) {
            cursor.moveToLast();
        } else {
            cursor.moveToPrevious();
        }
        showImage();
    }

    private void showImage() {
        imageView.setImageURI(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))));
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null,     // 項目(null = 全項目)
                null,      // フィルタ条件(null = フィルタなし)
                null,   // フィルタ用パラメータ
                null       // ソート (null ソートなし)
        );
    }
}
