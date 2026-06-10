package com.yudi.asmara.expensereport.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yudi.asmara.expensereport.R;
import com.yudi.asmara.expensereport.databinding.ItemTransactionBinding;
import com.yudi.asmara.expensereport.models.Transaction;
import com.yudi.asmara.expensereport.utils.FormatUtils;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions;
    private OnTransactionActionListener listener;

    private static final int[] PASTEL_COLORS = {
        0xFFFFE0E0, 0xFFFFE8CC, 0xFFD3F9D8, 0xFFD0EBFF,
        0xFFE8E5FF, 0xFFFFE0F0, 0xFFE0F7FA, 0xFFF1F8E8,
        0xFFFFF3E0, 0xFFF3E5F5, 0xFFE8EAF6, 0xFFE0F2F1
    };

    private static final int[] PASTEL_BARS = {
        0xFFFF6B6B, 0xFFFF922B, 0xFF51CF66, 0xFF74B9FF,
        0xFFA29BFE, 0xFFF783AC, 0xFF4DD0E1, 0xFF8BC34A,
        0xFFFFB74D, 0xFFCE93D8, 0xFF7986CB, 0xFF4DB6AC
    };

    public interface OnTransactionActionListener {
        void onDelete(Transaction transaction);
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionActionListener listener) {
        this.transactions = transactions;
        this.listener = listener;
    }

    public void updateData(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);
        holder.binding.tvCategory.setText(t.getNamaKategori());
        String keterangan = t.getKeterangan();

        if (keterangan == null || keterangan.isEmpty()) {
            keterangan = "-";
        }

        holder.binding.tvDescription.setText(keterangan);
        holder.binding.tvNominal.setText(FormatUtils.rupiah(t.getNominal()));
        holder.binding.tvDate.setText(t.getTanggalTransaksi());

        int colorIndex = Math.abs(t.getNamaKategori().hashCode()) % PASTEL_BARS.length;
        int barColor = PASTEL_BARS[colorIndex];
        holder.binding.colorBar.setBackgroundColor(barColor);

        String tipe = t.getTipe();
        if ("income".equals(tipe)) {
            holder.binding.tvNominal.setTextColor(holder.itemView.getContext().getColor(R.color.green_title));
        } else {
            holder.binding.tvNominal.setTextColor(holder.itemView.getContext().getColor(R.color.red));
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) listener.onDelete(t);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (transactions == null) {
            return 0;
        }
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        ViewHolder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
