package com.teixeira.subtitles.activities;

import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.teixeira.subtitles.App;
import com.teixeira.subtitles.BuildConfig;
import com.teixeira.subtitles.databinding.ActivityCrashBinding;
import java.util.Date;

public class CrashActivity extends BaseActivity {
  public static final String KEY_ERROR = "error";

  private ActivityCrashBinding binding;

  @Override
  protected View bindView() {
    binding = ActivityCrashBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setSupportActionBar(binding.toolbar);

    getOnBackPressedDispatcher()
        .addCallback(
            new OnBackPressedCallback(true) {

              @Override
              public void handleOnBackPressed() {
                finishAffinity();
              }
            });

    StringBuilder fullError = new StringBuilder();
    fullError.append(getSoftwareInfo());
    fullError.append(getAppInfo()).append("\n\n");
    fullError.append(getIntent().getExtras().getString(KEY_ERROR));

    binding.log.setText(fullError.toString());
    binding.copyAndReportIssue.setOnClickListener(
        v -> {
          App.getInstance().openUrl(App.APP_REPO_OPEN_ISSUE);
          ClipboardUtils.copyText(binding.log.getText().toString());
        });
    binding.copy.setOnClickListener(v -> ClipboardUtils.copyText(binding.log.getText().toString()));
    binding.closeApp.setOnClickListener(v -> finishAffinity());
  }

  private String getSoftwareInfo() {
    StringBuilder sb = new StringBuilder("ABIs: ");
    for (String abi : DeviceUtils.getABIs()) {
      sb.append(abi).append(", ");
    }
    sb.deleteCharAt(sb.length() - 2);
    return sb.append("\n")
        .append("Manufacturer: ")
        .append(DeviceUtils.getManufacturer())
        .append("\n")
        .append("Device: ")
        .append(DeviceUtils.getModel())
        .append("\n")
        .append("SDK: ")
        .append(Build.VERSION.SDK_INT)
        .append("\n")
        .append("Android: ")
        .append(Build.VERSION.RELEASE)
        .append("\n")
        .append("Model: ")
        .append(Build.VERSION.INCREMENTAL)
        .append("\n")
        .toString();
  }

  private String getAppInfo() {
    String buildVersion = BuildConfig.VERSION_NAME;
    String buildType = BuildConfig.BUILD_TYPE;

    return new StringBuilder("Version: ")
        .append(buildVersion)
        .append("\n")
        .append("Build: ")
        .append(buildType)
        .append("\n")
        .toString();
  }

  private Date getDate() {
    return Calendar.getInstance().getTime();
  }
}
