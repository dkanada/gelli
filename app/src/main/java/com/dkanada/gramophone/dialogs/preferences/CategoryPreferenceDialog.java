package com.dkanada.gramophone.dialogs.preferences;

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
import com.dkanada.gramophone.model.Category;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryPreferenceDialog extends DialogFragment {
    public static final String TAG = CategoryPreferenceDialog.class.getSimpleName();

    public static CategoryPreferenceDialog create() {
        return new CategoryPreferenceDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.preference_dialog_category, null);
        List<Category> categories = PreferenceUtil.getInstance(getContext()).getCategories();
        CategoryAdapter adapter = new CategoryAdapter(categories);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        adapter.attachToRecyclerView(recyclerView);

        return new MaterialDialog.Builder(requireActivity())
                .customView(view, false)
                .title(R.string.pref_title_categories)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.reset_action)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onNeutral((dialog, action) -> {
                    adapter.setCategories(Arrays.stream(Category.values())
                        .peek(category -> category.select = true)
                        .collect(Collectors.toList()));
                })
                .onNegative((dialog, action) -> dismiss())
                .onPositive((dialog, action) -> {
                    PreferenceUtil.getInstance(getContext()).setCategories(adapter.getCategories());
                    dismiss();
                })
                .build();
    }
}
