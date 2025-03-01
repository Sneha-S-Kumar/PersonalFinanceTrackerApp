package com.example.e;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EditExpenseActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText categoryEditText;
    private EditText amountEditText;
    private EditText dateEditText;
    private ImageView expenseImageView;
    private Button saveButton;
    private Button cancelButton;
    private Button selectImageButton;
    private Button removeImageButton;

    private ExpenseRepository repository;
    private int expenseId;
    private String currentImagePath;
    private int adapterPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);


        repository = new ExpenseRepository(getApplication());


        categoryEditText = findViewById(R.id.edit_category);
        amountEditText = findViewById(R.id.edit_amount);
        dateEditText = findViewById(R.id.edit_date);
        expenseImageView = findViewById(R.id.edit_expense_image);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        selectImageButton = findViewById(R.id.select_image_button);
        removeImageButton = findViewById(R.id.remove_image_button);


        Intent intent = getIntent();
        expenseId = intent.getIntExtra("expense_id", -1);
        adapterPosition = intent.getIntExtra("position", -1);

        if (expenseId != -1) {

            categoryEditText.setText(intent.getStringExtra("expense_category"));
            amountEditText.setText(String.valueOf(intent.getDoubleExtra("expense_amount", 0.0)));
            dateEditText.setText(intent.getStringExtra("expense_date"));
            currentImagePath = intent.getStringExtra("expense_image_path");


            if (currentImagePath != null && !currentImagePath.isEmpty()) {
                expenseImageView.setVisibility(View.VISIBLE);
                removeImageButton.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(currentImagePath)
                        .error(R.drawable.placeholder_image)
                        .into(expenseImageView);
            } else {
                expenseImageView.setVisibility(View.GONE);
                removeImageButton.setVisibility(View.GONE);
            }
        } else {

            Toast.makeText(this, "Error: Expense not found", Toast.LENGTH_SHORT).show();
            finish();
        }


        dateEditText.setOnClickListener(v -> showDatePicker());


        selectImageButton.setOnClickListener(v -> openImageChooser());


        removeImageButton.setOnClickListener(v -> {
            currentImagePath = null;
            expenseImageView.setVisibility(View.GONE);
            removeImageButton.setVisibility(View.GONE);
        });


        saveButton.setOnClickListener(v -> saveExpense());


        cancelButton.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateEditText.setText(dateFormat.format(new Date(selection)));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();


            currentImagePath = saveImageToInternalStorage(imageUri);


            expenseImageView.setVisibility(View.VISIBLE);
            removeImageButton.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUri)
                    .into(expenseImageView);
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {

            File storageDir = new File(getFilesDir(), "expense_images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File imageFile = new File(storageDir, "expense_" + UUID.randomUUID().toString() + ".jpg");


            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            OutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void saveExpense() {

        String category = categoryEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();

        if (category.isEmpty()) {
            categoryEditText.setError("Category cannot be empty");
            return;
        }

        if (amountStr.isEmpty()) {
            amountEditText.setError("Amount cannot be empty");
            return;
        }

        if (date.isEmpty()) {
            dateEditText.setError("Date cannot be empty");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            amountEditText.setError("Invalid amount");
            return;
        }


        Uri imagePath = null;
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            imagePath = Uri.parse(currentImagePath);
        }
        boolean success = repository.updateExpense(expenseId, category, amountStr, "", date, imagePath);



        ExpenseEntity updatedExpense = new ExpenseEntity();
        updatedExpense.setId(expenseId);
        updatedExpense.setCategory(category);
        updatedExpense.setAmount(String.valueOf(amount));
        updatedExpense.setDate(date);
        updatedExpense.setImagePath(Uri.parse(currentImagePath));


        repository.updateExpense(updatedExpense);
if(success) {

    Intent resultIntent = new Intent();
    resultIntent.putExtra("position", adapterPosition);
    setResult(RESULT_OK, resultIntent);

    Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
    finish();
}
    }
}