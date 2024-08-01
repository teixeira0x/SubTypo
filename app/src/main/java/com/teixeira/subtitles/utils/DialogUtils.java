package com.teixeira.subtitles.utils;

import android.content.Context;
import android.view.LayoutInflater;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira.subtitles.databinding.LayoutProgressDialogBinding;

public class DialogUtils {

  public static MaterialAlertDialogBuilder createSimpleDialog(
      Context context, String title, String message) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(title);
    builder.setMessage(message);
    return builder;
  }

  public static MaterialAlertDialogBuilder createProgressDialog(
      Context context, String message, boolean cancelable) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);

    LayoutProgressDialogBinding binding =
        LayoutProgressDialogBinding.inflate(LayoutInflater.from(context));
    binding.message.setText(message);
    builder.setCancelable(cancelable);
    builder.setView(binding.getRoot());

    return builder;
  }
}
