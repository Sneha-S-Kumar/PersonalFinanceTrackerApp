package com.example.e;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PIC_IMAGE_REQUEST = 1;

    DrawerLayout drawerLayout;
    CoordinatorLayout coordinatorLayout;
    MaterialToolbar materialToolbar;
    CardView addExpenseCard, viewExpensesCard, addCategoryCard,Budgetcardview;
    ImageView mainImageView;
    TextView nameTextView;
    NavigationView navigationView;
    AlertDialog expenseDialog, categoryDialog;
    ImageView uploadImageView;
    ExpenseRepository repository;
    Uri selectedImageUri;
    ArrayList<String> categoryList;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        addExpenseCard = findViewById(R.id.addcard);
        viewExpensesCard = findViewById(R.id.viewcard);
        mainImageView = findViewById(R.id.addbookimage);
        drawerLayout = findViewById(R.id.drawerlayout);
        addCategoryCard = findViewById(R.id.addcategory);
        Budgetcardview = findViewById(R.id.viewbudget);


        repository = new ExpenseRepository(getApplication());
        categoryList = repository.getAllCategories();


        AlertDialog.Builder expenseBuilder = new AlertDialog.Builder(this);
        View expenseView = getLayoutInflater().inflate(R.layout.dialogboxadd, null);

        uploadImageView = expenseView.findViewById(R.id.imageadd);
        Spinner categorySpinner = expenseView.findViewById(R.id.category);
        EditText amountEditText = expenseView.findViewById(R.id.amount);
        EditText notesEditText = expenseView.findViewById(R.id.notes);
        EditText dateEditText = expenseView.findViewById(R.id.date);
        Button addExpenseButton = expenseView.findViewById(R.id.addexpensebutton);


        dateEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                dateEditText.setText(sdf.format(new Date(selection)));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });


        AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(this);
        View categoryView = getLayoutInflater().inflate(R.layout.addcategory, null);

        EditText categoryNameEditText = categoryView.findViewById(R.id.catname);
        EditText budgetEditText = categoryView.findViewById(R.id.budget);
        EditText monthEditText = categoryView.findViewById(R.id.month);
        Button addCategoryButton = categoryView.findViewById(R.id.addcatbutton);


        monthEditText.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select month")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                monthEditText.setText(sdf.format(new Date(selection)));
            });

            datePicker.show(getSupportFragmentManager(), "MONTH_PICKER");
        });


        expenseBuilder.setView(expenseView);
        categoryBuilder.setView(categoryView);


        expenseDialog = expenseBuilder.create();
        categoryDialog = categoryBuilder.create();


        updateCategorySpinner(categorySpinner);


        mainImageView.setOnClickListener(v -> {

            updateCategorySpinner(categorySpinner);
            expenseDialog.show();
        });

        uploadImageView.setOnClickListener(v -> {
            Log.e("Main", "onClick: upload");
            openImagePicker();
        });

        addCategoryCard.setOnClickListener(v -> categoryDialog.show());

        viewExpensesCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewExpenses.class);
            startActivity(intent);
        });


        addExpenseButton.setOnClickListener(v -> {
            String category = "";
            if (categorySpinner.getSelectedItem() != null) {
                category = categorySpinner.getSelectedItem().toString();
            }
            String amount = amountEditText.getText().toString();
            String notes = notesEditText.getText().toString();
            String date = dateEditText.getText().toString();

            if (!category.isEmpty() && !amount.isEmpty() && !date.isEmpty()) {
                boolean success = repository.addExpense(category, amount, notes, date, selectedImageUri);
                if (success) {
                    amountEditText.setText("");
                    notesEditText.setText("");
                    dateEditText.setText("");
                    selectedImageUri = null;
                    expenseDialog.dismiss();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please enter all required data", Toast.LENGTH_SHORT).show();
            }
        });



        Budgetcardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, BudgetDashboardActivity.class);
                startActivity(intent);
            }
        });



        addCategoryButton.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString();
            String budget = budgetEditText.getText().toString();
            String month = monthEditText.getText().toString();

            if (!categoryName.isEmpty() && !budget.isEmpty() && !month.isEmpty()) {
                boolean success = repository.addCategory(categoryName, budget, month);
                if (success) {
                    categoryNameEditText.setText("");
                    budgetEditText.setText("");
                    monthEditText.setText("");
                    categoryDialog.dismiss();


                    categoryList = repository.getAllCategories();
                    updateCategorySpinner(categorySpinner);
                }
            } else {
                Toast.makeText(MainActivity.this, "Please enter all required data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCategorySpinner(Spinner spinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PIC_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Log.e("MainActivity", "onActivityResult: " + selectedImageUri);
            uploadImageView.setImageURI(selectedImageUri);
        }
    }
}