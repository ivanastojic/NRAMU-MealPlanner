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

    public interface OnMealPlanLongClick {
        void onEdit(MealPlan plan);
        void onDelete(MealPlan plan);
    }

    public interface OnMealPlanClick {
        void onClick(MealPlan plan);
    }

    private List<MealPlan> plans;
    private final HashMap<String, String> recipeIdToTitle;
    private final OnMealPlanLongClick longClickListener;
    private final OnMealPlanClick clickListener;

    public MealPlanAdapter(
            List<MealPlan> plans,
            HashMap<String, String> recipeIdToTitle,
            OnMealPlanLongClick longClickListener,
            OnMealPlanClick clickListener
    ) {
        this.plans = plans;
        this.recipeIdToTitle = recipeIdToTitle;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
    }

    public void setPlans(List<MealPlan> newPlans) {
        this.plans = newPlans;
        notifyDataSetChanged();
    }

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

        holder.tvDate.setText(p.plan_date);
        holder.tvMealType.setText(p.meal_type);

        String title = recipeIdToTitle.get(p.recipe_id);
        holder.tvRecipeTitle.setText(title != null ? title : p.recipe_id);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(p);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                // samo Delete ili možeš napraviti dialog u Activity-u
                longClickListener.onDelete(p);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvMealType, tvRecipeTitle;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
        }
    }
}
