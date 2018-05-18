package org.androidtown.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;

/*
 * 카메라 사용하기
 * 1. 파일의 저장경로를 xml로 생성
 * 2. Android Manifest
 */

public class MainActivity extends AppCompatActivity {

  ImageView imageView;
  Button btnCamera;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // 1. 버젼 체크 - 마시멜로 이상이면 Camera 권한체크
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      checkPermission();
    } else {
      init();
    }
  }

  public static final int PERM_CAMERA = 99;

  // 마시멜로 이상일 경우 Dangerous 권한 체크
  @TargetApi(Build.VERSION_CODES.M)
  private void checkPermission() {
    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
      && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      init();
    } else {
      String permissions[] = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
      requestPermissions(permissions, PERM_CAMERA);
    }
  }

  private void init() {
    imageView = findViewById(R.id.imageView);
    btnCamera = findViewById(R.id.btnCamera);
    btnCamera.setEnabled(true);
  }

  public void openGallery(View v) {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(intent, Const.REQ_GALLERY);
  }

  Uri fileUri = null; // 이미지가 저장될 Uri

  public void openCamera(View v) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    // 롤리팝 미만버젼 처리
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      startActivityForResult(intent, Const.REQ_CAMERA);
    } else { // 롤리팝 이상
      // 실제 이미지가 저장될 경로
      File photoFile = null;
      try {
        photoFile = createTempFile();
        if (photoFile.exists()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 마시멜로만..
            fileUri = FileProvider.getUriForFile(
              this,
              BuildConfig.APPLICATION_ID + ".provider",
              photoFile);
          } else {
            fileUri = Uri.fromFile(photoFile);
          }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, Const.REQ_CAMERA);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private File createTempFile() throws Exception {
    // 파일명
    String tempFilename = "Temp_" + System.currentTimeMillis();
    // 파일 저장하기 위한 디렉토리
    File tempDir = new File(Environment.getExternalStorageDirectory() + "/CameraDir/");
    if (!tempDir.exists()) {
      tempDir.mkdirs();
    }

    // 파일 생성
    File file = File.createTempFile(tempFilename, ".jpg", tempDir);

    return file;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Uri imageUri = null;
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case Const.REQ_GALLERY:
          imageUri = data.getData();
          imageView.setImageURI(imageUri);
          break;
        case Const.REQ_CAMERA:
          if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
          } else {
            if (fileUri != null) {
              imageView.setImageURI(fileUri);
            }
          }
          break;
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERM_CAMERA:
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
          && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          init();
        } else {
          Toast.makeText(getBaseContext(), "앱을 사용하기 위해 카메라 권한이 필요합니다.", Toast.LENGTH_LONG)
            .show();
          finish();
        }
        break;
    }
  }
}