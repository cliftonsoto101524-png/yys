package com.yys.root;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * RecyclerView adapter for script grid.
 */
public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ViewHolder> {

    private List<MainActivity.ScriptItem> mItems;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(MainActivity.ScriptItem item);
    }

    public ScriptAdapter(List<MainActivity.ScriptItem> items) {
        mItems = items;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void updateItems(List<MainActivity.ScriptItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_script, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.ScriptItem item = mItems.get(position);
        holder.tvName.setText(item.displayName);
        holder.tvDesc.setText(item.description);

        boolean enabled = ConfigManager.getInstance().isScriptEnabled(item.id);
        holder.card.setChecked(enabled);

        holder.card.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvName;
        TextView tvDesc;

        ViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_script);
            tvName = itemView.findViewById(R.id.tv_script_name);
            tvDesc = itemView.findViewById(R.id.tv_script_desc);
        }
    }
}
