package com.teixeira0x.subtypo.utils;

import android.net.Uri;
import com.teixeira0x.subtypo.App;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtil {

  public static final File APP_DATA_DIR =
      new File(App.getInstance().getExternalFilesDir(null).getAbsolutePath());
  public static final File PROJECTS_DIR = new File(APP_DATA_DIR, "projects");

  public static String getFileName(String path) {
    return path.substring(path.lastIndexOf("/") + 1);
  }

  public static String readFile(String path) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      reader.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    return sb.toString();
  }

  public static String readFileFromUri(Uri uri) throws IOException {
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

  public static void writeFileFromUri(Uri uri, String content) throws IOException {
    OutputStream outputStream = App.getInstance().getContentResolver().openOutputStream(uri);
    if (outputStream != null) {
      outputStream.write(content.getBytes());
      outputStream.close();
    }
  }
}
