package com.teixeira.subtitles.utils;

import android.net.Uri;
import com.teixeira.subtitles.App;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

  public static final File DATA_DIR =
      new File(App.getInstance().getExternalFilesDir(null).getAbsolutePath());
  public static final File PROJECTS_DIR = new File(DATA_DIR, "projects");

  public static void writeFileContent(Uri uri, String content) {
    try {
      OutputStream outputStream = App.getInstance().getContentResolver().openOutputStream(uri);
      if (outputStream != null) {
        outputStream.write(content.getBytes());
        outputStream.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // public static String get
}
