package com.teixeira.subtitles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.teixeira.subtitles.databinding.LayoutSubtitleItemBinding;
import com.teixeira.subtitles.models.Subtitle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubtitleListAdapter extends RecyclerView.Adapter<SubtitleListAdapter.VH> {

  private final List<Subtitle> subtitles;

  private final SubtitleListener listener;

  public SubtitleListAdapter(SubtitleListener listener) {
    this(new ArrayList<>(), listener);
  }

  public SubtitleListAdapter(@NonNull List<Subtitle> subtitles, SubtitleListener listener) {
    this.subtitles = subtitles;
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
    binding.startAndEnd.setText(subtitle.getStartTime() + " | " + subtitle.getEndTime());

    binding
        .getRoot()
        .setOnClickListener(v -> listener.onSubtitleClickListener(v, position, subtitle));
    binding
        .getRoot()
        .setOnLongClickListener(v -> listener.onSubtitleLongClickListener(v, position, subtitle));
  }

  @Override
  public int getItemCount() {
    return subtitles.size();
  }

  public void addSubtitle(Subtitle subtitle) {
    int index = subtitles.size();
    addSubtitle(index, subtitle);
  }

  public void addSubtitle(int index, Subtitle subtitle) {
    subtitles.add(index, subtitle);
    notifyItemChanged(index);

    if (listener != null) {
      listener.onUpdateSubtitles(subtitles);
    }
  }

  public void setSubtitle(int index, Subtitle subtitle) {
    if (index != -1) {
      subtitles.set(index, subtitle);
      notifyItemChanged(index, subtitle);

      if (listener != null) {
        listener.onUpdateSubtitles(subtitles);
      }
    }
  }

  public void removeSubtitle(int index) {
    removeSubtitle(subtitles.get(index));
  }

  public void removeSubtitle(Subtitle subtitle) {
    int index = subtitles.indexOf(subtitle);
    if (index != -1) {
      subtitles.remove(subtitle);
      notifyItemRemoved(index);

      if (listener != null) {
        listener.onUpdateSubtitles(subtitles);
      }
    }
  }

  public List<Subtitle> getSubtitles() {
    return subtitles;
  }

  public interface SubtitleListener {
    void onSubtitleClickListener(View view, int index, Subtitle subtitle);

    boolean onSubtitleLongClickListener(View view, int index, Subtitle subtitle);

    void onUpdateSubtitles(List<Subtitle> subtitles);
  }

  class VH extends RecyclerView.ViewHolder {
    private final LayoutSubtitleItemBinding binding;

    public VH(LayoutSubtitleItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }

  public static class SubtitleTouchHelper extends ItemTouchHelper.Callback {

    private SubtitleListAdapter adapter;

    public SubtitleTouchHelper(SubtitleListAdapter adapter) {
      this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
      return true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerVjew, ViewHolder holder, ViewHolder target) {
      Collections.swap(
          adapter.getSubtitles(), holder.getAdapterPosition(), target.getAdapterPosition());
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
        adapter.notifyDataSetChanged();
      }
    }
  }
}
