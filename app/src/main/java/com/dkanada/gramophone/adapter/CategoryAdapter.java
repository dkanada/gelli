package com.dkanada.gramophone.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.CategoryInfo;
import com.dkanada.gramophone.helper.SwipeAndDragHelper;

import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {
    private List<CategoryInfo> categories;
    private ItemTouchHelper touchHelper;

    public CategoryAdapter(List<CategoryInfo> categories) {
        this.categories = categories;
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(this);
        touchHelper = new ItemTouchHelper(swipeAndDragHelper);
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_category_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        CategoryInfo category = categories.get(position);

        holder.checkBox.setChecked(category.visible);
        holder.title.setText(holder.title.getResources().getString(category.category.stringRes));

        holder.itemView.setOnClickListener(v -> {
            if (!(category.visible && isLastCheckedCategory(category))) {
                category.visible = !category.visible;
                holder.checkBox.setChecked(category.visible);
            } else {
                Toast.makeText(holder.itemView.getContext(), R.string.you_have_to_select_at_least_one_category, Toast.LENGTH_SHORT).show();
            }
        });

        holder.dragView.setOnTouchListener((view, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(holder);
                    }

                    return false;
                });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<CategoryInfo> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        CategoryInfo categoryInfo = categories.get(oldPosition);
        categories.remove(oldPosition);
        categories.add(newPosition, categoryInfo);
        notifyItemMoved(oldPosition, newPosition);
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public List<CategoryInfo> getCategories() {
        return categories;
    }

    private boolean isLastCheckedCategory(CategoryInfo category) {
        if (category.visible) {
            for (CategoryInfo c : categories) {
                if (c != category && c.visible) return false;
            }
        }

        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView title;
        public View dragView;

        public ViewHolder(View view) {
            super(view);

            checkBox = view.findViewById(R.id.checkbox);
            title = view.findViewById(R.id.title);
            dragView = view.findViewById(R.id.drag_view);
        }
    }
}

