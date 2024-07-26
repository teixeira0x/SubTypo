package com.teixeira.subtitles.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.adapters.SubtitleListAdapter;
import com.teixeira.subtitles.callbacks.SubtitleEditorCallbacks;
import com.teixeira.subtitles.databinding.ActivityProjectBinding;
import com.teixeira.subtitles.fragments.sheets.SubtitleEditorSheetFragment;
import com.teixeira.subtitles.models.Project;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.project.ProjectManager;
import com.teixeira.subtitles.utils.VideoUtils;
import java.util.List;

public class ProjectActivity extends BaseActivity
    implements SubtitleEditorCallbacks, SubtitleListAdapter.SubtitleListener {

  private static final Handler mainHandler = ThreadUtils.getMainHandler();

  private ActivityProjectBinding binding;
  private ProjectManager projectManager;
  private Project project;
  private SubtitleListAdapter adapter;
  private Runnable onEverySecond;

  @Override
  protected View bindView() {
    binding = ActivityProjectBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

    projectManager = ProjectManager.getInstance();

    binding.videoControllerContent.addSubtitle.setOnClickListener(
        v -> {
          stopVideo();
          SubtitleEditorSheetFragment.newInstance(
                  binding.videoContent.videoView.getCurrentPosition())
              .show(getSupportFragmentManager(), null);
        });

    configureProject();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_project_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == R.id.menu_save) {}

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    mainHandler.removeCallbacks(onEverySecond);
    projectManager.destroy();
    super.onDestroy();
    onEverySecond = null;
    binding = null;
  }

  @Override
  public void addSubtitle(Subtitle subtitle) {
    adapter.addSubtitle(subtitle);
  }

  @Override
  public void updateSubtitle(int index, Subtitle subtitle) {
    adapter.updateSubtitle(index, subtitle);
  }

  @Override
  public void onUpdateSubtitles(List<Subtitle> subtitles) {
    String subtitlesJson =
        new Gson().toJson(subtitles, new TypeToken<List<Subtitle>>() {}.getType());
    FileIOUtils.writeFileFromString(project.getProjectPath() + "/subtitles.json", subtitlesJson);

    if (onEverySecond != null) {
      mainHandler.post(onEverySecond);
    }
  }

  @Override
  public void onSubtitleClickListener(View view, int index, Subtitle subtitle) {
    SubtitleEditorSheetFragment.newInstance(subtitle, index)
        .show(getSupportFragmentManager(), null);
  }

  @Override
  public boolean onSubtitleLongClickListener(View view, int index, Subtitle subtitle) {
    new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.delete)
        .setMessage(getString(R.string.delete_message, "\"" + subtitle.getText() + "\""))
        .setPositiveButton(R.string.yes, (d, w) -> adapter.removeSubtitle(subtitle))
        .setNegativeButton(R.string.no, null)
        .show();
    return true;
  }

  private void configureProject() {
    project =
        projectManager.setupProject(
            getIntent().getExtras().getParcelable("project", Project.class));
    getSupportActionBar().setTitle(project.getName());
    adapter = new SubtitleListAdapter(project.getSubtitles(), this);

    binding.subtitles.setLayoutManager(new LinearLayoutManager(this));
    binding.subtitles.setAdapter(adapter);

    configureVideoView();
  }

  private void configureVideoView() {
    onEverySecond = () -> onEverySecond();

    binding.videoContent.videoView.setVideoPath(project.getVideoPath());
    binding.videoContent.videoView.setOnPreparedListener(
        new MediaPlayer.OnPreparedListener() {

          @Override
          public void onPrepared(MediaPlayer player) {
            binding.videoControllerContent.seekBar.setMax(binding.videoContent.videoView.getDuration());
            binding.videoControllerContent.allTime.setText(
                VideoUtils.getTime(binding.videoContent.videoView.getDuration()));
            mainHandler.post(onEverySecond);
          }
        });

    binding.videoContent.videoView.setOnCompletionListener(
        new MediaPlayer.OnCompletionListener() {

          @Override
          public void onCompletion(MediaPlayer player) {
            binding.videoControllerContent.play.setImageResource(R.drawable.ic_play);
            mainHandler.removeCallbacks(onEverySecond);
          }
        });

    binding.videoControllerContent.seekBar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {

          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
              binding.videoControllerContent.currentTime.setText(VideoUtils.getTime(progress));
              binding.videoContent.videoView.seekTo(progress);
            }
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    binding.videoControllerContent.play.setOnClickListener(
        v -> {
          if (binding.videoContent.videoView.isPlaying()) {
            stopVideo();
          } else playVideo();
        });

    binding.videoControllerContent.skipBackward.setOnClickListener(v -> back5sec());
    binding.videoControllerContent.skipFoward.setOnClickListener(v -> skip5sec());
  }

  public void back5sec() {
    int seek = binding.videoContent.videoView.getCurrentPosition() - 5000;
    if (binding.videoContent.videoView.getCurrentPosition() <= 5000) {
      seek = binding.videoContent.videoView.getCurrentPosition();
    }

    binding.videoContent.videoView.seekTo(seek);
  }

  public void skip5sec() {
    int seek = binding.videoContent.videoView.getCurrentPosition() + 5000;
    if (seek > binding.videoContent.videoView.getDuration()) {
      seek = binding.videoContent.videoView.getDuration();
    }

    binding.videoContent.videoView.seekTo(seek);
  }

  private void playVideo() {
    binding.videoControllerContent.play.setImageResource(R.drawable.ic_pause);
    binding.videoContent.videoView.start();
    mainHandler.post(onEverySecond);
  }

  private void stopVideo() {
    binding.videoControllerContent.play.setImageResource(R.drawable.ic_play);
    binding.videoContent.videoView.pause();
    mainHandler.removeCallbacks(onEverySecond);
  }

  private void onEverySecond() {
    int currentTime = binding.videoContent.videoView.getCurrentPosition();
    binding.videoControllerContent.currentTime.setText(VideoUtils.getTime(currentTime));
    binding.videoControllerContent.seekBar.setProgress(binding.videoContent.videoView.getCurrentPosition());

    List<Subtitle> subtitles = adapter.getSubtitles();

    boolean subtitleFound = false;
    for (Subtitle subtitle : subtitles) {
      try {
        long startTime = VideoUtils.getMilliSeconds(subtitle.getStartTime());
        long endTime = VideoUtils.getMilliSeconds(subtitle.getEndTime());

        if (currentTime >= startTime && currentTime <= endTime) {
          binding.videoContent.tvSubtitle.setText(subtitle.getText());
          binding.videoContent.tvSubtitle.setVisibility(View.VISIBLE);
          subtitleFound = true;
          break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (!subtitleFound) {
      binding.videoContent.tvSubtitle.setVisibility(View.GONE);
    }

    if (binding.videoContent.videoView.isPlaying()) {
      mainHandler.postDelayed(onEverySecond, 1);
    }
  }
}
