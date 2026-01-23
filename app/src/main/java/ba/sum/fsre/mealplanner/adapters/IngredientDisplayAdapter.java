package ba.sum.fsre.mealplanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.mealplanner.R;
import ba.sum.fsre.mealplanner.models.IngredientDisplay;

import java.util.ArrayList;
import java.util.List;

public class IngredientDisplayAdapter
        extends RecyclerView.Adapter<IngredientDisplayAdapter.VH> {

    public interface OnIngredientActions {
        void onEdit(IngredientDisplay ingredient);
        void onDelete(IngredientDisplay ingredient);
    }

    private final List<IngredientDisplay> items = new ArrayList<>();
    private final OnIngredientActions actionsListener;
    private final boolean canEdit;

    public IngredientDisplayAdapter(OnIngredientActions listener, boolean canEdit) {
        this.actionsListener = listener;
        this.canEdit = canEdit;
    }

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

        holder.btnMenu.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        if (canEdit) {
            holder.btnMenu.setOnClickListener(v -> showMenu(holder, d));


            holder.itemView.setOnLongClickListener(v -> {
                showMenu(holder, d);
                return true;
            });
        } else {
            holder.btnMenu.setOnClickListener(null);
            holder.itemView.setOnLongClickListener(null);
        }
    }

    private void showMenu(@NonNull VH holder, @NonNull IngredientDisplay d) {
        PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnMenu);
        popup.getMenuInflater().inflate(R.menu.menu_item_actions, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_edit) {
                if (actionsListener != null) actionsListener.onEdit(d);
                return true;
            } else if (menuItem.getItemId() == R.id.action_delete) {
                if (actionsListener != null) actionsListener.onDelete(d);
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLine;
        ImageButton btnMenu;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvLine = itemView.findViewById(R.id.tvIngredientQty);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
