package com.teixeira.subtitles.utils;

import android.net.Uri;
import com.teixeira.subtitles.App;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtil {

  public static final File DATA_DIR =
      new File(App.getInstance().getExternalFilesDir(null).getAbsolutePath());
  public static final File PROJECTS_DIR = new File(DATA_DIR, "projects");

  public static String getFileName(String path) {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  public static String readFileContent(Uri uri) throws IOException {
    InputStream inputStream = App.getInstance().getContentResolver().openInputStream(uri);
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line).append("\n");
    }
    inputStream.close();
    return sb.toString();
  }

  public static void writeFileContent(Uri uri, String content) throws IOException {
    OutputStream outputStream = App.getInstance().getContentResolver().openOutputStream(uri);
    if (outputStream != null) {
      outputStream.write(content.getBytes());
      outputStream.close();
    }
  }
}
