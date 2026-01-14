package com.example.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.MealPlan;

import java.util.HashMap;
import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.VH> {

    // ===== INTERFACE ZA LONG PRESS =====
    public interface OnMealPlanLongClick {
        void onEdit(MealPlan plan);
        void onDelete(MealPlan plan);
    }

    private List<MealPlan> plans;
    private final HashMap<String, String> recipeIdToTitle;
    private final OnMealPlanLongClick longClickListener;

    // ===== KONSTRUKTOR =====
    public MealPlanAdapter(
            List<MealPlan> plans,
            HashMap<String, String> recipeIdToTitle,
            OnMealPlanLongClick longClickListener
    ) {
        this.plans = plans;
        this.recipeIdToTitle = recipeIdToTitle;
        this.longClickListener = longClickListener;
    }

    public void setPlans(List<MealPlan> newPlans) {
        this.plans = newPlans;
        notifyDataSetChanged();
    }

    // ===== ADAPTER =====
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_plan, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MealPlan p = plans.get(position);

        holder.tvMealType.setText(p.meal_type);

        String title = recipeIdToTitle.get(p.recipe_id);
        holder.tvRecipeTitle.setText(title != null ? title : p.recipe_id);

        // LONG PRESS → EDIT / DELETE
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onDelete(p);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    // ===== VIEW HOLDER =====
    static class VH extends RecyclerView.ViewHolder {
        TextView tvMealType, tvRecipeTitle;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
        }
    }
}
