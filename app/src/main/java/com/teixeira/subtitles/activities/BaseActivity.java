package com.teixeira.subtitles.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.R;

abstract class BaseActivity extends AppCompatActivity {

  protected abstract View bindView();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    getWindow().setStatusBarColor(getStatusBarColor());
    getWindow().setNavigationBarColor(getNavigationBarColor());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      getWindow().setNavigationBarDividerColor(getNavigationBarDividerColor());
    }
    super.onCreate(savedInstanceState);
    setContentView(bindView());

    requestPermissions();
  }

  protected int getStatusBarColor() {
    return MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0);
  }

  protected int getNavigationBarColor() {
    return MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0);
  }

  protected int getNavigationBarDividerColor() {
    return MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant, 0);
  }

  private void requestPermissions() {
    if (!isPermissionsGranted()) {
      new MaterialAlertDialogBuilder(this)
          .setTitle(R.string.permission_request)
          .setMessage(R.string.permission_request_message)
          .setPositiveButton(
              R.string.grant,
              (d, w) -> {
                String[] permissions;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  permissions = new String[] {Manifest.permission.READ_MEDIA_VIDEO};
                } else {
                  permissions =
                      new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                      };
                }
                ActivityCompat.requestPermissions(this, permissions, 1000);
              })
          .setNegativeButton(R.string.no, null)
          .show();
    }
  }

  protected boolean isPermissionsGranted() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
          == PackageManager.PERMISSION_GRANTED;
    } else {
      return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
          == PackageManager.PERMISSION_GRANTED;
    }
  }
}
