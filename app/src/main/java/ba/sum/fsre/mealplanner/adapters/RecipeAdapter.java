package com.example.mealplanner.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    public interface OnRecipeClick {
        void onClick(Recipe recipe);
    }

    public interface OnRecipeMenuAction {
        void onEdit(Recipe recipe);
        void onDelete(Recipe recipe);
    }

    private final List<Recipe> items = new ArrayList<>();
    private final OnRecipeClick clickListener;
    private final OnRecipeMenuAction menuActionListener;

    public RecipeAdapter(
            OnRecipeClick clickListener,
            OnRecipeMenuAction menuActionListener
    ) {
        this.clickListener = clickListener;
        this.menuActionListener = menuActionListener;
    }

    public void setItems(List<Recipe> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Recipe recipe = items.get(position);
        holder.tvTitle.setText(recipe.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(recipe);
            }
        });

        holder.btnMenu.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(
                    v.getContext(),
                    holder.btnMenu,
                    Gravity.END,
                    0,
                    R.style.AppPopupMenu
            );

            popup.getMenuInflater().inflate(R.menu.menu_item_actions, popup.getMenu());

            popup.setOnMenuItemClickListener(menuItem -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return true;

                Recipe r = items.get(pos);

                if (menuItem.getItemId() == R.id.action_edit) {
                    if (menuActionListener != null) menuActionListener.onEdit(r);
                    return true;
                }

                if (menuItem.getItemId() == R.id.action_delete) {
                    if (menuActionListener != null) menuActionListener.onDelete(r);
                    return true;
                }

                return false;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageButton btnMenu;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
