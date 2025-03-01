package com.example.e;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetDashboardActivity extends AppCompatActivity {

    private TextView monthYearText;
    private TextView totalBudgetText;
    private TextView totalSpendingText;
    private LinearProgressIndicator overallProgress;
    private RecyclerView categoryRecyclerView;
    private RecyclerView alertsRecyclerView;
    private ExpenseRepository repository;
    private String currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_dashboard);

        monthYearText = findViewById(R.id.month_year_text);
        totalBudgetText = findViewById(R.id.total_budget_text);
        totalSpendingText = findViewById(R.id.total_spending_text);
        overallProgress = findViewById(R.id.overall_progress);
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        alertsRecyclerView = findViewById(R.id.alerts_recycler_view);


        repository = new ExpenseRepository(getApplication());


        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        currentMonth = sdf.format(new Date());
        monthYearText.setText(currentMonth);


        loadBudgetData();
    }

    private void loadBudgetData() {

        double totalBudget = repository.getTotalBudget(currentMonth);
        double totalSpending = repository.getTotalSpending(currentMonth);


        totalBudgetText.setText(String.format("Budget: $%.2f", totalBudget));
        totalSpendingText.setText(String.format("Spent: $%.2f", totalSpending));


        int progressPercentage = totalBudget > 0 ? (int)((totalSpending / totalBudget) * 100) : 0;
        overallProgress.setProgress(progressPercentage);


        if (progressPercentage >= 100) {
            overallProgress.setIndicatorColor(Color.RED);
        } else if (progressPercentage >= 80) {
            overallProgress.setIndicatorColor(Color.YELLOW);
        } else {
            overallProgress.setIndicatorColor(Color.GREEN);
        }


        Map<String, Double> categoryBudgets = repository.getBudgetSummary(currentMonth);
        List<CategoryBudgetItem> categoryItems = new ArrayList<>();

        for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
            categoryItems.add(new CategoryBudgetItem(entry.getKey(), entry.getValue()));
        }

        CategoryBudgetAdapter categoryAdapter = new CategoryBudgetAdapter(this, categoryItems);
        categoryRecyclerView.setAdapter(categoryAdapter);


        List<BudgetAlertEntity> alerts = repository.getBudgetAlerts();
        BudgetAlertAdapter alertAdapter = new BudgetAlertAdapter(this, alerts);
        alertsRecyclerView.setAdapter(alertAdapter);
    }




    private static class CategoryBudgetItem {
        String category;
        double percentSpent;

        CategoryBudgetItem(String category, double percentSpent) {
            this.category = category;
            this.percentSpent = percentSpent;
        }
    }

    private static class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder> {
        private Context context;
        private List<CategoryBudgetItem> items;

        CategoryBudgetAdapter(Context context, List<CategoryBudgetItem> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_budget, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryBudgetItem item = items.get(position);

            holder.categoryName.setText(item.category);
            holder.percentText.setText(String.format("%.1f%%", item.percentSpent));
            holder.progressBar.setProgress((int)item.percentSpent);


            if (item.percentSpent >= 100) {
                holder.progressBar.setIndicatorColor(Color.RED);
                holder.categoryCard.setCardBackgroundColor(Color.parseColor("#FFDDDD"));
            } else if (item.percentSpent >= 80) {
                holder.progressBar.setIndicatorColor(Color.YELLOW);
                holder.categoryCard.setCardBackgroundColor(Color.parseColor("#FFFFDD"));
            } else {
                holder.progressBar.setIndicatorColor(Color.GREEN);
                holder.categoryCard.setCardBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView categoryName;
            TextView percentText;
            LinearProgressIndicator progressBar;
            CardView categoryCard;

            ViewHolder(View itemView) {
                super(itemView);
                categoryName = itemView.findViewById(R.id.category_name);
                percentText = itemView.findViewById(R.id.percent_text);
                progressBar = itemView.findViewById(R.id.category_progress);
                categoryCard = itemView.findViewById(R.id.category_card);
            }
        }
    }

    private static class BudgetAlertAdapter extends RecyclerView.Adapter<BudgetAlertAdapter.ViewHolder> {
        private Context context;
        private List<BudgetAlertEntity> alerts;

        BudgetAlertAdapter(Context context, List<BudgetAlertEntity> alerts) {
            this.context = context;
            this.alerts = alerts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_budget_alert, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BudgetAlertEntity alert = alerts.get(position);

            holder.alertMessage.setText(alert.getMessage());
            holder.categoryMonth.setText(alert.getCategory() + " - " + alert.getMonth());

            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedTime = sdf.format(new Date(alert.getTimestamp()));
            holder.timestamp.setText(formattedTime);
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView alertMessage;
            TextView categoryMonth;
            TextView timestamp;

            ViewHolder(View itemView) {
                super(itemView);
                alertMessage = itemView.findViewById(R.id.alert_message);
                categoryMonth = itemView.findViewById(R.id.category_month);
                timestamp = itemView.findViewById(R.id.timestamp);
            }
        }
    }
}