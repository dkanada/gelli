package com.dkanada.gramophone.preferences;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.CategoryInfoAdapter;
import com.dkanada.gramophone.model.CategoryInfo;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class CategoryPreferenceDialog extends DialogFragment {
    public static CategoryPreferenceDialog newInstance() {
        return new CategoryPreferenceDialog();
    }

    private CategoryInfoAdapter adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.preference_dialog_category, null);

        List<CategoryInfo> categoryInfos;
        if (savedInstanceState != null) {
            categoryInfos = savedInstanceState.getParcelableArrayList(PreferenceUtil.CATEGORIES);
        } else {
            categoryInfos = PreferenceUtil.getInstance(getContext()).getCategories();
        }

        adapter = new CategoryInfoAdapter(categoryInfos);

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
                .onNeutral((dialog, action) -> adapter.setCategoryInfos(PreferenceUtil.getInstance(getContext()).getDefaultCategories()))
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    updateCategories(adapter.getCategoryInfos());
                    dismiss();
                })
                .build();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PreferenceUtil.CATEGORIES, new ArrayList<>(adapter.getCategoryInfos()));
    }

    private void updateCategories(List<CategoryInfo> categories) {
        if (getSelected(categories) == 0) return;

        PreferenceUtil.getInstance(getContext()).setCategories(categories);
    }

    private int getSelected(List<CategoryInfo> categories) {
        int selected = 0;
        for (CategoryInfo categoryInfo : categories) {
            if (categoryInfo.visible)
                selected++;
        }

        return selected;
    }
}
