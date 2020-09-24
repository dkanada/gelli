package com.dkanada.gramophone.preferences;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.CategoryAdapter;
import com.dkanada.gramophone.model.CategoryInfo;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class CategoryPreferenceDialog extends DialogFragment {
    public static CategoryPreferenceDialog newInstance() {
        return new CategoryPreferenceDialog();
    }

    private CategoryAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.preference_dialog_category, null);

        List<CategoryInfo> categories;
        if (savedInstanceState != null) {
            categories = savedInstanceState.getParcelableArrayList(PreferenceUtil.CATEGORIES);
        } else {
            categories = PreferenceUtil.getInstance(getContext()).getCategories();
        }

        adapter = new CategoryAdapter(categories);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        adapter.attachToRecyclerView(recyclerView);

        return new MaterialDialog.Builder(getContext())
                .title(R.string.library_categories)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.reset_action)
                .autoDismiss(false)
                .onNeutral((dialog, action) -> adapter.setCategories(PreferenceUtil.getInstance(getContext()).getDefaultCategories()))
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    updateCategories(adapter.getCategories());
                    dismiss();
                })
                .build();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PreferenceUtil.CATEGORIES, new ArrayList<>(adapter.getCategories()));
    }

    private void updateCategories(List<CategoryInfo> categories) {
        if (getSelected(categories) == 0) return;

        PreferenceUtil.getInstance(getContext()).setCategories(categories);
    }

    private int getSelected(List<CategoryInfo> categories) {
        int selected = 0;
        for (CategoryInfo category : categories) {
            if (category.visible) selected++;
        }

        return selected;
    }
}
