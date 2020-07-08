package com.jon.cotgenerator.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jon.cotgenerator.R;
import com.jon.cotgenerator.presets.OutputPreset;
import com.jon.cotgenerator.utils.Protocol;

import java.util.List;

public class ListPresetsAdapter extends RecyclerView.Adapter<ListPresetsAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private Protocol protocol;
    private List<OutputPreset> presets;
    private ItemClickListener clickListener;

    ListPresetsAdapter(Context context, Protocol protocol, List<OutputPreset> presets) {
        this.inflater = LayoutInflater.from(context);
        this.protocol = protocol;
        this.presets = presets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.preset_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final OutputPreset preset = presets.get(position);
        holder.alias.setText(preset.alias);
        holder.address.setText(preset.address);
        holder.port.setText(String.valueOf(preset.port));
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    OutputPreset getPreset(int position) {
        return presets.get(position);
    }

    void updatePresets(List<OutputPreset> presets) {
        this.presets = presets;
        this.notifyDataSetChanged();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClickEditItem(View view, Protocol protocol, int position);
        void onClickDeleteItem(View view, Protocol protocol, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView alias, address, port;
        ImageButton edit, delete;

        ViewHolder(View itemView) {
            super(itemView);
            alias = itemView.findViewById(R.id.presetListItemAlias);
            address = itemView.findViewById(R.id.presetListItemAddress);
            port = itemView.findViewById(R.id.presetListItemPort);

            edit = itemView.findViewById(R.id.presetListItemEdit);
            edit.setOnClickListener(this);
            delete = itemView.findViewById(R.id.presetListItemDelete);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                switch (view.getId()) {
                    case R.id.presetListItemEdit:
                        clickListener.onClickEditItem(view, protocol, getAdapterPosition());
                        break;
                    case R.id.presetListItemDelete:
                        clickListener.onClickDeleteItem(view, protocol, getAdapterPosition());
                        break;
                }
            }
        }
    }
}
