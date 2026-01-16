package com.example.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.ShoppingList;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListsAdapter extends RecyclerView.Adapter<ShoppingListsAdapter.VH> {

    public interface Listener {
        void onOpen(ShoppingList list);
        void onDelete(ShoppingList list);
    }

    private final List<ShoppingList> items = new ArrayList<>();
    private Listener listener;

    public void setListener(Listener l) { this.listener = l; }

    public void setItems(List<ShoppingList> lists) {
        items.clear();
        if (lists != null) items.addAll(lists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingList sl = items.get(position);

        h.tvName.setText("Tjedna lista");
        h.tvRange.setText(sl.date_from + " → " + sl.date_to);
        h.tvStatus.setText(sl.is_completed ? "Kupljeno ✅" : "Aktivna ⏳");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(sl);
        });

        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(sl);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvRange, tvStatus;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvListName);
            tvRange = itemView.findViewById(R.id.tvListRange);
            tvStatus = itemView.findViewById(R.id.tvListStatus);
        }
    }
}