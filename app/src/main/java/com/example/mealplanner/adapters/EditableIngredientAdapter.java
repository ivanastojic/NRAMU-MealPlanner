package com.example.mealplanner.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealplanner.R;
import com.example.mealplanner.models.EditableRecipeIngredient;
import com.example.mealplanner.models.Unit;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class EditableIngredientAdapter extends RecyclerView.Adapter<EditableIngredientAdapter.VH> {

    public interface OnDeleteClick {
        void onDelete(EditableRecipeIngredient item, int position);
    }

    private final List<EditableRecipeIngredient> items = new ArrayList<>();
    private final List<Unit> units = new ArrayList<>();
    private final List<String> unitNames = new ArrayList<>();

    private final OnDeleteClick deleteClick;

    public EditableIngredientAdapter(OnDeleteClick deleteClick) {
        this.deleteClick = deleteClick;
        setHasStableIds(true);
    }

    public void setUnits(List<Unit> newUnits) {
        units.clear();
        unitNames.clear();
        if (newUnits != null) {
            units.addAll(newUnits);
            for (Unit u : newUnits) unitNames.add(u.getName());
        }
        notifyDataSetChanged();
    }

    public void setItems(List<EditableRecipeIngredient> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<EditableRecipeIngredient> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_edit, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        EditableRecipeIngredient item = items.get(position);

        h.tvName.setText(item.ingredientName);

        // ---- QTY ----
        h.bindQty(item);

        // ---- NOTE ----
        h.bindNote(item);

        // ---- UNITS SPINNER ----
        ArrayAdapter<String> a = new ArrayAdapter<>(
                h.itemView.getContext(),
                R.layout.spinner_item_black,
                unitNames
        );
        a.setDropDownViewResource(R.layout.spinner_dropdown_item_black);
        h.spUnit.setAdapter(a);

        int unitPos = findUnitPosition(item.unitId);
        if (unitPos >= 0) h.spUnit.setSelection(unitPos);

        h.spUnit.setOnItemSelectedListener(new SimpleItemSelectedListener(pos -> {
            if (pos >= 0 && pos < units.size()) {
                item.unitId = units.get(pos).getId();
            }
        }));

        // ---- DELETE ----
        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && deleteClick != null) {
                deleteClick.onDelete(items.get(pos), pos);
            }
        });
    }

    private int findUnitPosition(int unitId) {
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getId() == unitId) return i;
        }
        return 0; // fallback
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).recipeIngredientId.hashCode();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        EditText etQty, etNote;
        Spinner spUnit;
        MaterialButton btnDelete;

        TextWatcher qtyWatcher;
        TextWatcher noteWatcher;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            etQty = itemView.findViewById(R.id.etQty);
            spUnit = itemView.findViewById(R.id.spUnit);
            etNote = itemView.findViewById(R.id.etNote);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bindQty(EditableRecipeIngredient item) {
            if (qtyWatcher != null) etQty.removeTextChangedListener(qtyWatcher);

            etQty.setText(String.valueOf(item.quantity));

            qtyWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    try {
                        String v = s.toString().trim();
                        if (v.isEmpty()) return;
                        item.quantity = Double.parseDouble(v);
                    } catch (Exception ignored) {}
                }
            };
            etQty.addTextChangedListener(qtyWatcher);
        }

        void bindNote(EditableRecipeIngredient item) {
            if (noteWatcher != null) etNote.removeTextChangedListener(noteWatcher);

            etNote.setText(item.note == null ? "" : item.note);

            noteWatcher = new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    item.note = s.toString();
                }
            };
            etNote.addTextChangedListener(noteWatcher);
        }
    }
    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
    }

}
