package com.teixeira0x.subtypo.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.teixeira0x.subtypo.App;
import com.teixeira0x.subtypo.R;

public class UIModePreference extends Preference {

  public UIModePreference(Context context) {
    super(context);
  }

  public UIModePreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public UIModePreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public UIModePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onClick() {
    super.onClick();

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
    builder.setTitle(getTitle());

    String[] items =
        new String[] {
          getString(R.string.pref_aparence_ui_mode_value_follow),
          getString(R.string.pref_aparence_ui_mode_value_light),
          getString(R.string.pref_aparence_ui_mode_value_dark),
        };

    SharedPreferences.Editor prefEdit = getSharedPreferences().edit();
    int selectedItemIndex = getSharedPreferences().getInt(getKey(), 0);

    builder.setSingleChoiceItems(items, selectedItemIndex, (d, w) -> prefEdit.putInt(getKey(), w));
    builder.setNegativeButton(R.string.cancel, null);
    builder.setPositiveButton(
        R.string.save,
        (d, w) -> {
          prefEdit.apply();
          App.getInstance().updateUIMode();
        });
    builder.show();
  }

  private String getString(int resId) {
    return getContext().getString(resId);
  }
}
