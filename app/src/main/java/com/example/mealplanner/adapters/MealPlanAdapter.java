package com.example.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.MealPlan;

import java.util.HashMap;
import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.VH> {

    public interface OnMealPlanClick {
        void onClick(MealPlan plan);
    }


    public interface OnMealPlanMenuAction {
        void onEdit(MealPlan plan);
        void onDelete(MealPlan plan);
    }

    private List<MealPlan> plans;
    private final HashMap<String, String> recipeIdToTitle;
    private final OnMealPlanClick clickListener;
    private final OnMealPlanMenuAction menuActionListener;

    public MealPlanAdapter(
            List<MealPlan> plans,
            HashMap<String, String> recipeIdToTitle,
            OnMealPlanClick clickListener,
            OnMealPlanMenuAction menuActionListener
    ) {
        this.plans = plans;
        this.recipeIdToTitle = recipeIdToTitle;
        this.clickListener = clickListener;
        this.menuActionListener = menuActionListener;
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

        holder.btnMenu.setOnClickListener(v -> showMenu(holder));

        // (opcionalno) long press → isto otvori menu
        holder.itemView.setOnLongClickListener(v -> {
            showMenu(holder);
            return true;
        });
    }

    private void showMenu(@NonNull VH holder) {
        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;

        MealPlan plan = plans.get(pos);

        PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnMenu);
        popup.getMenuInflater().inflate(R.menu.menu_item_actions, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_edit) {
                if (menuActionListener != null) menuActionListener.onEdit(plan);
                return true;
            } else if (menuItem.getItemId() == R.id.action_delete) {
                if (menuActionListener != null) menuActionListener.onDelete(plan);
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvMealType, tvRecipeTitle;
        ImageButton btnMenu;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
