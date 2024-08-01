package com.teixeira.subtitles.models;

public class ExportFormat {

  public static final int FORMAT_SUBRIP = 0;

  private int icon;
  private String name;
  private int format;

  public ExportFormat(int icon, String name, int format) {
    this.icon = icon;
    this.name = name;
    this.format = format;
  }

  public int getIcon() {
    return this.icon;
  }

  public void setIcon(int icon) {
    this.icon = icon;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getFormat() {
    return this.format;
  }

  public void setFormat(int format) {
    this.format = format;
  }
}
