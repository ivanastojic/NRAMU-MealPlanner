package com.example.mealplanner.models;

import java.util.ArrayList;
import java.util.List;

public class DayPlanGroup {
    public String date;                 // "YYYY-MM-DD"
    public List<MealPlanRow> plans = new ArrayList<>();

    public DayPlanGroup(String date) {
        this.date = date;
    }
}
