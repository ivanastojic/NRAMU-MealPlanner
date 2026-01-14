package com.example.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.IngredientDisplay;

import java.util.ArrayList;
import java.util.List;

public class IngredientDisplayAdapter
        extends RecyclerView.Adapter<IngredientDisplayAdapter.VH> {

    // ===== INTERFACE ZA LONG PRESS =====
    public interface OnIngredientLongClick {
        void onEdit(IngredientDisplay ingredient);
        void onDelete(IngredientDisplay ingredient);
    }

    private final List<IngredientDisplay> items = new ArrayList<>();
    private OnIngredientLongClick longClickListener;

    // ===== KONSTRUKTORI =====
    public IngredientDisplayAdapter() {}

    public IngredientDisplayAdapter(OnIngredientLongClick listener) {
        this.longClickListener = listener;
    }

    // ===== DATA =====
    public void setItems(List<IngredientDisplay> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    // ===== ADAPTER =====
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        IngredientDisplay d = items.get(position);

        holder.tvName.setText(d.name);
        holder.tvLine.setText(d.line);

        // LONG PRESS → EDIT / DELETE
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onDelete(d);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ===== VIEW HOLDER =====
    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLine;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvLine = itemView.findViewById(R.id.tvIngredientQty);
        }
    }
}
