package ba.sum.fsre.mealplanner.adapters;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

    public interface OnPosSelected {
        void onSelected(int pos);
    }

    private final OnPosSelected cb;

    public SimpleItemSelectedListener(OnPosSelected cb) {
        this.cb = cb;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (cb != null) cb.onSelected(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
