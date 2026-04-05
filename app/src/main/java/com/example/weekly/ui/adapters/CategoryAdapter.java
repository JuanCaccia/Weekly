package com.example.weekly.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weekly.R;
import com.example.weekly.domain.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private Long selectedCategoryId = null;
    private final OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(Category category);
    }

    public CategoryAdapter(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(Long id) {
        this.selectedCategoryId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_picker, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, category.getId().equals(selectedCategoryId));
        holder.itemView.setOnClickListener(v -> {
            selectedCategoryId = category.getId();
            notifyDataSetChanged();
            listener.onCategorySelected(category);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final View colorCircle;
        private final TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorCircle = itemView.findViewById(R.id.viewCategoryColor);
            categoryName = itemView.findViewById(R.id.textCategoryName);
        }

        public void bind(Category category, boolean isSelected) {
            categoryName.setText(category.getName());
            int color = Color.parseColor(category.getColorHex());
            colorCircle.setBackgroundTintList(ColorStateList.valueOf(color));
            
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_category_selected);
            } else {
                itemView.setBackground(null);
            }
        }
    }
}
