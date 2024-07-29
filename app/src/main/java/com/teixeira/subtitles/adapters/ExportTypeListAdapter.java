package com.teixeira.subtitles.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.teixeira.subtitles.databinding.LayoutExportTypeItemBinding;
import com.teixeira.subtitles.models.ExportType;
import java.util.ArrayList;
import java.util.List;
import com.teixeira.subtitles.R;

public class ExportTypeListAdapter
    extends RecyclerView.Adapter<ExportTypeListAdapter.ExportTypeViewHolder> {

  private final List<ExportType> exportTypes = new ArrayList<>();

  private int selectedType = 0;

  public ExportTypeListAdapter(Context context) {
    exportTypes.add(
        new ExportType(
            R.drawable.ic_file_code,
            context.getString(R.string.export_in_subrip),
            ExportType.TYPE_SUBRIP));
  }

  @Override
  public ExportTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ExportTypeViewHolder(
        LayoutExportTypeItemBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
  }

  @Override
  public void onBindViewHolder(ExportTypeViewHolder holder, int position) {
    LayoutExportTypeItemBinding binding = holder.binding;
    ExportType type = exportTypes.get(position);

    binding.readioButton.setChecked(selectedType == type.getType());
    binding.icon.setImageResource(type.getIcon());
    binding.name.setText(type.getName());

    binding.getRoot().setOnClickListener(v -> {});
  }

  @Override
  public int getItemCount() {
    return exportTypes.size();
  }

  public int getSelectedExportType() {
    return selectedType;
  }

  class ExportTypeViewHolder extends RecyclerView.ViewHolder {
    private LayoutExportTypeItemBinding binding;

    public ExportTypeViewHolder(LayoutExportTypeItemBinding binding) {
      super(binding.getRoot());
      this.binding = binding;
    }
  }
}
