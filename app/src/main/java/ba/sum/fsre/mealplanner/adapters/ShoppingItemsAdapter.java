package ba.sum.fsre.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.models.ShoppingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShoppingItemsAdapter extends RecyclerView.Adapter<ShoppingItemsAdapter.VH> {

    public interface Listener {
        void onToggle(ShoppingItem item, boolean checked);
    }

    private final List<ShoppingItem> items = new ArrayList<>();
    private Map<String, String> ingredientNameById;
    private Map<Integer, String> unitNameById;
    private Listener listener;

    public void setListener(Listener l) { this.listener = l; }

    public void setMaps(Map<String, String> ingMap, Map<Integer, String> unitMap) {
        this.ingredientNameById = ingMap;
        this.unitNameById = unitMap;
        notifyDataSetChanged();
    }

    public void setItems(List<ShoppingItem> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void setAllChecked(boolean checked) {
        for (ShoppingItem it : items) it.is_checked = checked;
        notifyDataSetChanged();
    }

    public boolean areAllChecked() {
        if (items.isEmpty()) return false;
        for (ShoppingItem it : items) if (!it.is_checked) return false;
        return true;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingItem it = items.get(position);

        String ingName = (ingredientNameById != null) ? ingredientNameById.get(it.ingredient_id) : null;
        if (ingName == null) ingName = "Ingredient";

        String unitName = (unitNameById != null) ? unitNameById.get(it.unit_id) : null;
        if (unitName == null) unitName = "";

        h.tvName.setText(ingName);
        h.tvQty.setText(String.valueOf(it.quantity) + " " + unitName);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(it.is_checked);
        h.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggle(it, isChecked);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty;
        CheckBox cb;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvQty = itemView.findViewById(R.id.tvIngredientQty);
            cb = itemView.findViewById(R.id.cbBought);
        }
    }
}
