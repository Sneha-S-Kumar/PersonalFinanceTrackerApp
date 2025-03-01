package com.example.e;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {
    private ExpenseDao expenseDao;
    private CategoryDao categoryDao;
    private BudgetAlertDao budgetAlertDao;
    private Application application;
    private ExecutorService executorService;


    public ExpenseRepository(Application application) {
        ExpenseRoomDatabase database = ExpenseRoomDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        categoryDao = database.categoryDao();
        budgetAlertDao = database.budgetAlertDao();
        this.application = application;
        executorService = Executors.newSingleThreadExecutor();

    }




    public boolean addExpense(String category, String amount, String notes, String date, Uri imagePath) {
        ExpenseEntity expense = new ExpenseEntity(category, amount, notes, date, imagePath);

        try {
            long result = new InsertExpenseAsyncTask(expenseDao).execute(expense).get();
            if (result == -1) {
                Toast.makeText(application, "Error adding expense", Toast.LENGTH_SHORT).show();
                return false;
            }

            Toast.makeText(application, "Expense Added Successfully", Toast.LENGTH_SHORT).show();


            executorService.execute(() -> {
                Log.d("BudgetUpdate", "Updating budget for category: " + category + " | Date: " + date);
              //  String month = date.substring(0, 7); // Extract YYYY-MM
                SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                String month;
                try {
                    Date parsedDate = inputFormat.parse(date);
                    month = outputFormat.format(parsedDate);
                    Log.d("BudgetUpdate", "Extracted month: " + month);
                } catch (ParseException e) {
                    Log.e("BudgetUpdate", "Error parsing date: " + e.getMessage());
                    month = date;
                }

                CategoryEntity categoryEntity = categoryDao.getCategoryByNameAndMonth(category, month);
                if (categoryEntity != null) {
                    double newBudget = Double.parseDouble(categoryEntity.getBudget()) - Double.parseDouble(amount);
                    categoryDao.updateBudget(category, String.valueOf(newBudget), month);
                }


                checkBudgetStatus(category, month);
            });

            return true;
        } catch (ExecutionException | InterruptedException e) {
            Log.e("ExpenseRepository", "Error adding expense: " + e.getMessage());
            Toast.makeText(application, "Error adding expense", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public void updateExpense(ExpenseEntity expense) {
        executorService.execute(() -> {
            expenseDao.update(expense);
        });
    }


    private void checkBudgetStatus(String category, String month) {
        try {
            Log.d("BudgetCheck", "Checking budget for: " + category + " in " + month);

            CategoryEntity categoryEntity = new GetCategoryBudgetAsyncTask(categoryDao).execute(category, month).get();
            if (categoryEntity == null) return;

            double budget = Double.parseDouble(categoryEntity.getBudget());
            double totalSpent = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                totalSpent = new GetTotalSpendingAsyncTask(expenseDao).execute(category, month).get();
            }

            double percentUsed = (totalSpent / budget) * 100;
            Log.d("BudgetCheck", "Total Spent: " + totalSpent + " / Budget: " + budget);

            if (percentUsed >= 100) {
                showToast("Budget exceeded for " + category + "!");
                saveBudgetAlert(category, month, "Budget exceeded! Spent: " + String.format("%.2f", totalSpent) + " of " + budget);
            } else if (percentUsed >= 80) {
                showToast("Approaching budget limit for " + category);
                saveBudgetAlert(category, month, "Budget alert! Used " + String.format("%.0f", percentUsed) + "% of " + category + " budget");
            }
        } catch (Exception e) {
            Log.e("BudgetCheck", "Error checking budget: " + e.getMessage());
        }
    }

    private void saveBudgetAlert(String category, String month, String message) {
        BudgetAlertEntity alert = new BudgetAlertEntity(category, month, message, System.currentTimeMillis());
        new InsertBudgetAlertAsyncTask(budgetAlertDao).execute(alert);
    }

    public boolean updateExpense(int id, String category, String amount, String notes, String date, Uri imagePath) {
        ExpenseEntity expense = new ExpenseEntity(category, amount, notes, date, imagePath);
        expense.setId(id);

        try {
            int rowsAffected = new UpdateExpenseAsyncTask(expenseDao).execute(expense).get();
            if (rowsAffected > 0) {
                checkBudgetStatus(category, date.substring(0, 7));
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e("ExpenseRepository", "Error updating expense: " + e.getMessage());
        }
        return false;
    }

    private void showToast(String message) {
        Toast.makeText(application, message, Toast.LENGTH_LONG).show();
    }

    public void deleteExpense(int id) {
        new DeleteExpenseAsyncTask(expenseDao).execute(id);
    }

    public ArrayList<ExpenseEntity> getAllExpenses() {
        ArrayList<ExpenseEntity> expensesList = new ArrayList<>();
        try {
            List<ExpenseEntity> entities = new GetAllExpensesAsyncTask(expenseDao).execute().get();
            expensesList.addAll(entities);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return expensesList;
    }


    public ArrayList<ExpenseEntity> searchExpenses(String category) {
        ArrayList<ExpenseEntity> expensesList = new ArrayList<>();
        try {
            List<ExpenseEntity> entities = new SearchExpensesAsyncTask(expenseDao).execute(category).get();
            expensesList.addAll(entities);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return expensesList;
    }


    public boolean addCategory(String category, String budget, String month) {
        CategoryEntity categoryEntity = new CategoryEntity(category, budget, month);
        try {
            long result = new InsertCategoryAsyncTask(categoryDao).execute(categoryEntity).get();
            if (result != -1) {
                Toast.makeText(application, "Category Added Successfully", Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            Toast.makeText(application, "Error adding category", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean updateCategoryBudget(String category, String budget, String month) {
        try {
            int result = new UpdateCategoryBudgetAsyncTask(categoryDao).execute(category, budget, month).get();
            return result > 0;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String> getAllCategories() {
        ArrayList<String> categories = new ArrayList<>();
        try {
            List<String> categoryNames = new GetAllCategoriesAsyncTask(categoryDao).execute().get();
            categories.addAll(categoryNames);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Map<String, Double> getBudgetSummary(String month) {
        Map<String, Double> budgetSummary = new HashMap<>();
        try {

            List<CategoryEntity> categories = new GetCategoriesByMonthAsyncTask(categoryDao).execute(month).get();

            for (CategoryEntity category : categories) {

                double spent = new GetTotalSpendingAsyncTask(expenseDao).execute(category.getCategory(), month).get();
                double budget = Double.parseDouble(category.getBudget());


                double percentSpent = (spent / budget) * 100;
                budgetSummary.put(category.getCategory(), percentSpent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return budgetSummary;
    }

    public double getTotalBudget(String month) {
        try {
            return new GetTotalBudgetAsyncTask(categoryDao).execute(month).get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getTotalSpending(String month) {
        try {
            return new GetMonthTotalSpendingAsyncTask(expenseDao).execute(month).get();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<BudgetAlertEntity> getBudgetAlerts() {
        try {
            return new GetBudgetAlertsAsyncTask(budgetAlertDao).execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    private static class GetCategoryBudgetAsyncTask extends AsyncTask<String, Void, CategoryEntity> {
        private CategoryDao categoryDao;

        private GetCategoryBudgetAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected CategoryEntity doInBackground(String... params) {
            return categoryDao.getCategoryByNameAndMonth(params[0], params[1]);
        }
    }

    private static class GetTotalSpendingAsyncTask extends AsyncTask<String, Void, Double> {
        private ExpenseDao expenseDao;

        private GetTotalSpendingAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected Double doInBackground(String... params) {
            String category = params[0];
            String month = params[1];
            Double total = expenseDao.getTotalSpendingByCategory(category, month + "%");
            return total != null ? total : 0.0;
        }
    }

    private static class GetMonthTotalSpendingAsyncTask extends AsyncTask<String, Void, Double> {
        private ExpenseDao expenseDao;

        private GetMonthTotalSpendingAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected Double doInBackground(String... params) {
            String month = params[0];
            Double total = expenseDao.getTotalSpendingForMonth(month + "%");
            return total != null ? total : 0.0;
        }
    }

    private static class UpdateCategoryBudgetAsyncTask extends AsyncTask<String, Void, Integer> {
        private CategoryDao categoryDao;

        private UpdateCategoryBudgetAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String category = params[0];
            String budget = params[1];
            String month = params[2];
            return categoryDao.updateBudget(category, budget, month);
        }
    }

    private static class GetCategoriesByMonthAsyncTask extends AsyncTask<String, Void, List<CategoryEntity>> {
        private CategoryDao categoryDao;

        private GetCategoriesByMonthAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected List<CategoryEntity> doInBackground(String... params) {
            return categoryDao.getCategoriesByMonth(params[0]);
        }
    }

    private static class GetTotalBudgetAsyncTask extends AsyncTask<String, Void, Double> {
        private CategoryDao categoryDao;

        private GetTotalBudgetAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected Double doInBackground(String... params) {
            Double total = categoryDao.getTotalBudgetForMonth(params[0]);
            return total != null ? total : 0.0;
        }
    }

    private static class InsertBudgetAlertAsyncTask extends AsyncTask<BudgetAlertEntity, Void, Void> {
        private BudgetAlertDao budgetAlertDao;

        private InsertBudgetAlertAsyncTask(BudgetAlertDao budgetAlertDao) {
            this.budgetAlertDao = budgetAlertDao;
        }

        @Override
        protected Void doInBackground(BudgetAlertEntity... alerts) {
            budgetAlertDao.insert(alerts[0]);
            return null;
        }
    }

    private static class GetBudgetAlertsAsyncTask extends AsyncTask<Void, Void, List<BudgetAlertEntity>> {
        private BudgetAlertDao budgetAlertDao;

        private GetBudgetAlertsAsyncTask(BudgetAlertDao budgetAlertDao) {
            this.budgetAlertDao = budgetAlertDao;
        }

        @Override
        protected List<BudgetAlertEntity> doInBackground(Void... voids) {
            return budgetAlertDao.getAllAlerts();
        }
    }

    private static class InsertExpenseAsyncTask extends AsyncTask<ExpenseEntity, Void, Long> {
        private ExpenseDao expenseDao;

        private InsertExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected Long doInBackground(ExpenseEntity... expenseEntities) {
            return expenseDao.insert(expenseEntities[0]);
        }
    }

    private static class UpdateExpenseAsyncTask extends AsyncTask<ExpenseEntity, Void, Integer> {
        private ExpenseDao expenseDao;

        private UpdateExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected Integer doInBackground(ExpenseEntity... expenseEntities) {
            return expenseDao.update(expenseEntities[0]);
        }
    }

    private static class DeleteExpenseAsyncTask extends AsyncTask<Integer, Void, Void> {
        private ExpenseDao expenseDao;

        private DeleteExpenseAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected Void doInBackground(Integer... ids) {
            expenseDao.deleteById(ids[0]);
            return null;
        }
    }

    private static class GetAllExpensesAsyncTask extends AsyncTask<Void, Void, List<ExpenseEntity>> {
        private ExpenseDao expenseDao;

        private GetAllExpensesAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected List<ExpenseEntity> doInBackground(Void... voids) {
            return expenseDao.getAllExpenses();
        }
    }

    private static class SearchExpensesAsyncTask extends AsyncTask<String, Void, List<ExpenseEntity>> {
        private ExpenseDao expenseDao;

        private SearchExpensesAsyncTask(ExpenseDao expenseDao) {
            this.expenseDao = expenseDao;
        }

        @Override
        protected List<ExpenseEntity> doInBackground(String... strings) {
            return expenseDao.searchExpensesByCategory(strings[0]);
        }
    }

    private static class InsertCategoryAsyncTask extends AsyncTask<CategoryEntity, Void, Long> {
        private CategoryDao categoryDao;

        private InsertCategoryAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected Long doInBackground(CategoryEntity... categoryEntities) {
            return categoryDao.insert(categoryEntities[0]);
        }
    }

    private static class GetAllCategoriesAsyncTask extends AsyncTask<Void, Void, List<String>> {
        private CategoryDao categoryDao;

        private GetAllCategoriesAsyncTask(CategoryDao categoryDao) {
            this.categoryDao = categoryDao;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return categoryDao.getAllCategoryNames();
        }
    }


}