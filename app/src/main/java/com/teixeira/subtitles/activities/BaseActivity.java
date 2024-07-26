package com.teixeira.subtitles.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.color.MaterialColors;

abstract class BaseActivity extends AppCompatActivity {

  protected abstract View bindView();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    getWindow().setStatusBarColor(getStatusBarColor());
    getWindow().setNavigationBarColor(getNavigationBarColor());
    getWindow().setNavigationBarDividerColor(getNavigationBarDividerColor());
    super.onCreate(savedInstanceState);
    setContentView(bindView());
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
}
