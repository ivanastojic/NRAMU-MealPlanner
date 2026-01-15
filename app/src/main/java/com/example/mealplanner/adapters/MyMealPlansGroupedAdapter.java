package com.example.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.DayPlanGroup;
import com.example.mealplanner.models.MealPlanRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMealPlansGroupedAdapter extends RecyclerView.Adapter<MyMealPlansGroupedAdapter.VH> {

    public interface OnDayClickListener {
        void onDayClick(DayPlanGroup group);
    }

    private final List<DayPlanGroup> items = new ArrayList<>();
    private Map<String, String> recipeTitleById = new HashMap<>();
    private OnDayClickListener dayClickListener;

    public void setItems(List<DayPlanGroup> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setRecipeTitleMap(Map<String, String> map) {
        recipeTitleById = (map != null) ? map : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.dayClickListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_plan_group, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DayPlanGroup g = items.get(position);

        h.tvDate.setText(g.date);

        // Napravi 3 linije: Breakfast/Lunch/Dinner
        h.tvBreakfast.setText("Breakfast: " + getTitleForMeal(g, "Breakfast"));
        h.tvLunch.setText("Lunch: " + getTitleForMeal(g, "Lunch"));
        h.tvDinner.setText("Dinner: " + getTitleForMeal(g, "Dinner"));

        h.itemView.setOnClickListener(v -> {
            if (dayClickListener != null) dayClickListener.onDayClick(g);
        });
    }

    private String getTitleForMeal(DayPlanGroup g, String mealType) {
        if (g.plans == null) return "-";
        for (MealPlanRow p : g.plans) {
            if (p.meal_type != null && p.meal_type.equalsIgnoreCase(mealType)) {
                String t = recipeTitleById.get(p.recipe_id);
                return (t != null && !t.isEmpty()) ? t : "(recept)";
            }
        }
        return "-";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvBreakfast, tvLunch, tvDinner;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvBreakfast = itemView.findViewById(R.id.tvBreakfast);
            tvLunch = itemView.findViewById(R.id.tvLunch);
            tvDinner = itemView.findViewById(R.id.tvDinner);
        }
    }
}
