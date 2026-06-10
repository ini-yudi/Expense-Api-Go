package com.yudi.asmara.expensereport.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yudi.asmara.expensereport.databinding.ItemCategoryBinding;
import com.yudi.asmara.expensereport.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;
    private OnCategoryActionListener listener;

    private static final int[] COLORS = {
        0xFFFF6B6B, 0xFFFF922B, 0xFF51CF66, 0xFF74B9FF,
        0xFFA29BFE, 0xFFF783AC, 0xFF4DD0E1, 0xFF8BC34A,
        0xFFFFB74D, 0xFFCE93D8, 0xFF7986CB, 0xFF4DB6AC
    };

    private static final int[] PASTEL = {
        0xFFFFE0E0, 0xFFFFE8CC, 0xFFD3F9D8, 0xFFD0EBFF,
        0xFFE8E5FF, 0xFFFFE0F0, 0xFFE0F7FA, 0xFFF1F8E8,
        0xFFFFF3E0, 0xFFF3E5F5, 0xFFE8EAF6, 0xFFE0F2F1
    };

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    public CategoryAdapter(List<Category> categories, OnCategoryActionListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void updateData(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category c = categories.get(position);
        holder.binding.tvName.setText(c.getNamaKategori());

        if (c.getIcon() != null && !c.getIcon().isEmpty() && c.getIcon().startsWith("#")) {
            try {
                holder.binding.colorBar.setBackgroundColor(Color.parseColor(c.getIcon()));
            } catch (Exception e) {
                setColorByHash(holder, c);
            }
        } else {
            setColorByHash(holder, c);
        }

        holder.binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onEdit(c);
            }
        });
        holder.binding.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onDelete(c);
            }
        });
    }

    private void setColorByHash(ViewHolder holder, Category c) {
        int colorIndex = Math.abs(c.getNamaKategori().hashCode()) % COLORS.length;
        holder.binding.colorBar.setBackgroundColor(COLORS[colorIndex]);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCategoryBinding binding;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
