package com.teixeira.subtitles.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.teixeira.subtitles.databinding.LayoutSubtitleItemBinding;
import com.teixeira.subtitles.models.Subtitle;
import java.util.ArrayList;
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
    subtitles.add(subtitle);
    notifyItemChanged(index);

    if (listener != null) {
      listener.onUpdateSubtitles(subtitles);
    }
  }

  public void updateSubtitle(int index, Subtitle subtitle) {
    if (index != -1) {
      subtitles.set(index, subtitle);
      notifyItemChanged(index, subtitle);

      if (listener != null) {
        listener.onUpdateSubtitles(subtitles);
      }
    }
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
}
