package com.teixeira.subtitles.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.teixeira.subtitles.R;
import com.teixeira.subtitles.databinding.LayoutExportFormatItemBinding;
import com.teixeira.subtitles.models.ExportFormat;
import java.util.ArrayList;
import java.util.List;

public class ExportFormatListAdapter
    extends RecyclerView.Adapter<ExportFormatListAdapter.ExportFormatViewHolder> {

  private final List<ExportFormat> formats = new ArrayList<>();
  private int selectedFormat = 0;

  public ExportFormatListAdapter(Context context) {
    formats.add(
        new ExportFormat(
            R.drawable.ic_file_code,
            context.getString(R.string.export_format_subrip),
            ExportFormat.FORMAT_SUBRIP));
  }

  @Override
  public ExportFormatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ExportFormatViewHolder(
        LayoutExportFormatItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(ExportFormatViewHolder holder, int position) {
    LayoutExportFormatItemBinding binding = holder.binding;
    ExportFormat format = formats.get(position);

    binding.readioButton.setChecked(selectedFormat == format.getFormat());
    binding.icon.setImageResource(format.getIcon());
    binding.name.setText(format.getName());

    binding.getRoot().setOnClickListener(v -> {});
  }

  @Override
  public int getItemCount() {
    return formats.size();
  }

  public int getSelectedExportFormat() {
    return selectedFormat;
  }

  class ExportFormatViewHolder extends RecyclerView.ViewHolder {
    private LayoutExportFormatItemBinding binding;

    public ExportFormatViewHolder(LayoutExportFormatItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
