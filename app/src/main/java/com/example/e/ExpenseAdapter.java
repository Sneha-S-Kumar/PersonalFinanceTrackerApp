package com.example.e;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ExpenseEntity> expensesList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public ExpenseAdapter(Context context, ArrayList<ExpenseEntity> expensesList, OnItemClickListener listener) {
        this.context = context;
        this.expensesList = expensesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseEntity expense = expensesList.get(position);

        holder.categoryTextView.setText(expense.getCategory());
        holder.amountTextView.setText("₹" + expense.getAmount());
        holder.dateTextView.setText(expense.getDate());

        // Set visibility based on if there's an image
        if (expense.getImagePath() != null) {
            holder.expenseImageView.setVisibility(View.VISIBLE);


            try {
                Glide.with(context)
                        .load(expense.getImagePath())
                        .error(R.drawable.placeholder_image)
                        .into(holder.expenseImageView);
            } catch (Exception e) {
                holder.expenseImageView.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            holder.expenseImageView.setVisibility(View.GONE);
        }


        holder.editImageView.setOnClickListener(v -> listener.onEditClick(position));
        holder.deleteImageView.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView expenseCard;
        TextView categoryTextView, amountTextView, dateTextView;
        TextView editImageView, deleteImageView;
        ImageView expenseImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            expenseCard = itemView.findViewById(R.id.expense_card);
            categoryTextView = itemView.findViewById(R.id.expense_category);
            amountTextView = itemView.findViewById(R.id.expense_amount);
            dateTextView = itemView.findViewById(R.id.expense_date);
            expenseImageView = itemView.findViewById(R.id.expense_image);
            editImageView = itemView.findViewById(R.id.edit_expense);
            deleteImageView = itemView.findViewById(R.id.delete_expense);
        }
    }
}