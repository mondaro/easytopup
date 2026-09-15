package com.mondaro.easytopup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mondaro.easytopup.R;
import com.mondaro.easytopup.model.NavDrawerItem;

import java.util.List;

/**
 * Navigation drawer list.
 *
 * <p>Replaces the old {@code BaseAdapter}, which re-ran three {@code findViewById}
 * calls for every row on every bind. The view holder does that once per row.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter<NavDrawerAdapter.ItemHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final List<NavDrawerItem> items;
    private final OnItemClickListener listener;
    private int selectedPosition = 0;

    public NavDrawerAdapter(@NonNull List<NavDrawerItem> items,
                            @NonNull OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setSelectedPosition(int position) {
        if (position == selectedPosition) {
            return;
        }
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_list_item, parent, false);
        return new ItemHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(items.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        private final TextView title;
        private final TextView counter;

        ItemHolder(@NonNull View itemView, @NonNull OnItemClickListener listener) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
            counter = itemView.findViewById(R.id.counter);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position);
                }
            });
        }

        void bind(@NonNull NavDrawerItem item, boolean selected) {
            icon.setImageResource(item.getIcon());
            title.setText(item.getTitle());
            itemView.setActivated(selected);
            if (item.getCounterVisibility()) {
                counter.setVisibility(View.VISIBLE);
                counter.setText(item.getCount());
            } else {
                counter.setVisibility(View.GONE);
            }
        }
    }
}
