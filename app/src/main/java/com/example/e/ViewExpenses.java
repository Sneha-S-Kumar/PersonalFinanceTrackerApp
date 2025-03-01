package com.example.e;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ViewExpenses extends AppCompatActivity implements ExpenseAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private ArrayList<ExpenseEntity> expensesList;
    private ExpenseRepository repository;
    private TextView noExpensesText;
    private static final int EDIT_EXPENSE_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expenses);


        recyclerView = findViewById(R.id.expenses_recycler_view);
        noExpensesText = findViewById(R.id.no_expenses_text);


        repository = new ExpenseRepository(getApplication());


        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        loadExpenses();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadExpenses();
    }

    private void loadExpenses() {
        expensesList = repository.getAllExpenses();

        if (expensesList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noExpensesText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noExpensesText.setVisibility(View.GONE);

            adapter = new ExpenseAdapter(this, expensesList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onEditClick(int position) {
        ExpenseEntity expense = expensesList.get(position);




        Intent intent = new Intent(ViewExpenses.this, EditExpenseActivity.class);
        intent.putExtra("expense_id", expense.getId());
        intent.putExtra("expense_category", expense.getCategory());
        intent.putExtra("expense_amount", expense.getAmount());
        intent.putExtra("expense_date", expense.getDate());
        intent.putExtra("expense_image_path", expense.getImagePath());
        intent.putExtra("position", position);
        startActivityForResult(intent, EDIT_EXPENSE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_EXPENSE_REQUEST_CODE && resultCode == RESULT_OK) {

            int position = data.getIntExtra("position", -1);

            if (position != -1) {

                loadExpenses();



            }
        }
    }


    @Override
    public void onDeleteClick(int position) {
        ExpenseEntity expense = expensesList.get(position);


        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    repository.deleteExpense(expense.getId());


                    expensesList.remove(position);
                    adapter.notifyItemRemoved(position);

                
                    if (expensesList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        noExpensesText.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}