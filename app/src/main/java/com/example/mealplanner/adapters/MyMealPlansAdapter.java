package com.example.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.MealPlanRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMealPlansAdapter extends RecyclerView.Adapter<MyMealPlansAdapter.VH> {

    public interface OnPlanLongClickListener {
        void onPlanLongClick(MealPlanRow plan);
    }

    private final List<MealPlanRow> items = new ArrayList<>();
    private Map<String, String> recipeTitleById = new HashMap<>();
    private OnPlanLongClickListener longClickListener;

    public void setItems(List<MealPlanRow> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setRecipeTitleMap(Map<String, String> map) {
        recipeTitleById = (map != null) ? map : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setOnPlanLongClickListener(OnPlanLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void removeById(String planId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id != null && items.get(i).id.equals(planId)) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
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
    public void onBindViewHolder(@NonNull VH h, int position) {
        MealPlanRow row = items.get(position);

        // ✅ DATUM (iz baze)
        String date = row.plan_date;
        if (date != null && date.length() >= 10) {
            date = date.substring(0, 10); // ako dođe timestamp, uzmi samo YYYY-MM-DD
        }
        h.tvDate.setText(date != null ? date : "");

        // ✅ MEAL TYPE
        h.tvMealType.setText(row.meal_type);

        // ✅ TITLE recepta (iz mape)
        String title = recipeTitleById.get(row.recipe_id);
        if (title == null || title.isEmpty()) {
            title = "Recipe ID: " + row.recipe_id;
        }
        h.tvRecipeTitle.setText(title);

        // ✅ LONG PRESS
        h.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onPlanLongClick(row);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
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
