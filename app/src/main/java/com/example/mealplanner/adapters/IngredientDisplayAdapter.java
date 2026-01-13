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

public class IngredientDisplayAdapter extends RecyclerView.Adapter<IngredientDisplayAdapter.VH> {

    private final List<IngredientDisplay> items = new ArrayList<>();

    public void setItems(List<IngredientDisplay> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

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
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLine;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvLine = itemView.findViewById(R.id.tvIngredientQty);
        }
    }
}
