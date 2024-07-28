package com.teixeira.subtitles.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.callbacks.UpdateProjectsCallback;
import com.teixeira.subtitles.databinding.ActivityMainBinding;
import com.teixeira.subtitles.fragments.ProjectsFragment;
import com.teixeira.subtitles.fragments.sheets.CreateProjectSheetFragment;

public class MainActivity extends BaseActivity implements UpdateProjectsCallback {

  private ActivityMainBinding binding;

  @Override
  protected View bindView() {
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setSupportActionBar(binding.toolbar);

    binding.fabNewProject.setOnClickListener(
        v -> CreateProjectSheetFragment.newInstance().show(getSupportFragmentManager(), null));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == R.id.menu_about) {
      startActivity(new Intent(this, AboutActivity.class));
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void updateProjects() {
    Fragment fragment = getSupportFragmentManager().findFragmentById(binding.projects.getId());

    if (fragment != null) {
      ((ProjectsFragment) fragment).loadProjects();
    }
  }
}
