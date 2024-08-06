package com.teixeira.subtitles.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.teixeira.subtitles.databinding.LayoutSubtitleItemBinding;
import com.teixeira.subtitles.models.Subtitle;
import com.teixeira.subtitles.viewmodels.SubtitlesViewModel;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SubtitleListAdapter extends RecyclerView.Adapter<SubtitleListAdapter.VH> {

  private final SubtitleListener listener;

  private List<Subtitle> subtitles;
  private ItemTouchHelper touchHelper;

  private boolean isVideoPlaying;

  public SubtitleListAdapter(@NonNull SubtitleListener listener) {
    Objects.requireNonNull(listener);
    this.listener = listener;
  }

  @Override
  public VH onCreateViewHolder(ViewGroup parent, int viewType) {
    return new VH(
        LayoutSubtitleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(VH holder, int position) {
    LayoutSubtitleItemBinding binding = holder.binding;
    Subtitle subtitle = subtitles.get(position);

    binding.text.setText(subtitle.getText());
    binding.inScreen.setVisibility(subtitle.isInScreen() ? View.VISIBLE : View.INVISIBLE);
    binding.startAndEnd.setText(subtitle.getStartTime() + " | " + subtitle.getEndTime());

    binding.dragHandler.setOnTouchListener(
        (v, event) -> {
          if (!isVideoPlaying
              && touchHelper != null
              && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            touchHelper.startDrag(holder);
          }
          return false;
        });
    binding
        .getRoot()
        .setOnClickListener(v -> listener.onSubtitleClickListener(v, position, subtitle));
    binding
        .getRoot()
        .setOnLongClickListener(v -> listener.onSubtitleLongClickListener(v, position, subtitle));
  }

  @Override
  public int getItemCount() {
    return subtitles != null ? subtitles.size() : 0;
  }

  public void setTouchHelper(ItemTouchHelper touchHelper) {
    this.touchHelper = touchHelper;
  }

  public void setSubtitles(List<Subtitle> subtitles) {
    this.subtitles = subtitles;
    notifyDataSetChanged();
  }

  public void setVideoPlaying(boolean isVideoPlaying) {
    this.isVideoPlaying = isVideoPlaying;
  }

  public interface SubtitleListener {
    void onSubtitleClickListener(View view, int index, Subtitle subtitle);

    boolean onSubtitleLongClickListener(View view, int index, Subtitle subtitle);
  }

  class VH extends RecyclerView.ViewHolder {
    private final LayoutSubtitleItemBinding binding;

    public VH(LayoutSubtitleItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public static class SubtitleTouchHelper extends ItemTouchHelper.Callback {

    private SubtitlesViewModel viewModel;
    private SubtitleListAdapter adapter;

    public SubtitleTouchHelper(SubtitlesViewModel viewModel, SubtitleListAdapter adapter) {
      this.viewModel = viewModel;
      this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
      return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerVjew, ViewHolder holder, ViewHolder target) {
      Collections.swap(adapter.subtitles, holder.getAdapterPosition(), target.getAdapterPosition());
      adapter.notifyItemMoved(holder.getAdapterPosition(), target.getAdapterPosition());
      return true;
    }

    @Override
    public void onSwiped(ViewHolder arg0, int arg1) {}

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
      int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
      return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
      super.onSelectedChanged(viewHolder, actionState);
      if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
        viewModel.pushStackToUndoManager(adapter.subtitles);
        viewModel.saveSubtitles();
      }
    }
  }
}
