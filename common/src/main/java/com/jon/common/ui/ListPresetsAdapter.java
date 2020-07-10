package com.jon.common.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jon.common.AppSpecific;
import com.jon.common.CotApplication;
import com.jon.common.R;
import com.jon.common.presets.OutputPreset;

import java.util.List;

public class ListPresetsAdapter extends RecyclerView.Adapter<ListPresetsAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<OutputPreset> presets;
    private ItemClickListener clickListener;

    ListPresetsAdapter(Context context, List<OutputPreset> presets) {
        this.inflater = LayoutInflater.from(context);
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

    void updatePresets(List<OutputPreset> presets) {
        this.presets = presets;
        this.notifyDataSetChanged();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClickEditItem(OutputPreset preset);
        void onClickDeleteItem(OutputPreset preset);
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
            delete = itemView.findViewById(R.id.presetListItemDelete);
            edit.setOnClickListener(this);
            delete.setOnClickListener(this);
            setImageButtonTint(edit);
            setImageButtonTint(delete);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                if (view.getId() == R.id.presetListItemEdit) {
                    clickListener.onClickEditItem(presets.get(getAdapterPosition()));
                } else if (view.getId() == R.id.presetListItemDelete) {
                    clickListener.onClickDeleteItem(presets.get(getAdapterPosition()));
                }
            }
        }

        private void setImageButtonTint(ImageButton button) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Drawable icon = button.getDrawable();
                @ColorInt int colour = CotApplication.getContext().getColor(AppSpecific.getIconColourId());
                icon.setTint(colour);
                button.setImageDrawable(icon);
            }
        }
    }
}
