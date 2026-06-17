package com.example.app_quan_li_chi_tieu_ca_nhan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_quan_li_chi_tieu_ca_nhan.models.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etPhoneNumber, etDOB, etAddress, etOccupation, etBio;
    private EditText etIncomeGoal, etSavingGoal;
    private Spinner spinnerGender;
    private MaterialButton btnSave, btnCancel;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // Bind Views
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDOB = findViewById(R.id.etDOB);
        etAddress = findViewById(R.id.etAddress);
        etOccupation = findViewById(R.id.etOccupation);
        etBio = findViewById(R.id.etBio);
        etIncomeGoal = findViewById(R.id.etIncomeGoal);
        etSavingGoal = findViewById(R.id.etSavingGoal);
        spinnerGender = findViewById(R.id.spinnerGender);
        
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Set up Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        // Set up Gender Spinner
        String[] genders = {"Chưa thiết lập", "Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Set up Date of Birth Picker
        etDOB.setOnClickListener(v -> showDatePicker());

        // Load Current Data
        loadCurrentProfileData(genders);

        // Set up Save Button Click
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // If etDOB already has a valid date, parse it to show in picker
        String currentDob = etDOB.getText().toString();
        if (currentDob.contains("/")) {
            try {
                String[] parts = currentDob.split("/");
                int d = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1; // 0-indexed
                int y = Integer.parseInt(parts[2]);
                day = d;
                month = m;
                year = y;
            } catch (Exception ignored) {}
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, yearSelected, monthOfYear, dayOfMonth) -> {
            String dobFormatted = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, yearSelected);
            etDOB.setText(dobFormatted);
        }, year, month, day);
        
        datePickerDialog.show();
    }

    private void loadCurrentProfileData(String[] genders) {
        // 1. Fetch base user info (fullName)
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null) {
                            etFullName.setText(fullName);
                        }
                    }
                });

        // 2. Fetch extended profile info
        db.collection("profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            etPhoneNumber.setText(profile.getPhoneNumber());
                            etDOB.setText(profile.getDateOfBirth());
                            etAddress.setText(profile.getAddress());
                            etOccupation.setText(profile.getOccupation());
                            etBio.setText(profile.getBio());
                            
                            // Format goal inputs as plain integers/decimals for editing
                            if (profile.getMonthlyIncomeGoal() > 0) {
                                etIncomeGoal.setText(String.valueOf((long) profile.getMonthlyIncomeGoal()));
                            }
                            if (profile.getSavingGoal() > 0) {
                                etSavingGoal.setText(String.valueOf((long) profile.getSavingGoal()));
                            }

                            // Set Spinner Selection
                            String gender = profile.getGender();
                            if (gender != null) {
                                for (int i = 0; i < genders.length; i++) {
                                    if (gender.equalsIgnoreCase(genders[i])) {
                                        spinnerGender.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void saveProfileChanges() {
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String occupation = etOccupation.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String incomeGoalStr = etIncomeGoal.getText().toString().trim();
        String savingGoalStr = etSavingGoal.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (fullName.isEmpty()) {
            etFullName.setError("Họ và tên không được để trống");
            return;
        }

        double incomeGoal = 0.0;
        if (!incomeGoalStr.isEmpty()) {
            try {
                incomeGoal = Double.parseDouble(incomeGoalStr);
            } catch (NumberFormatException e) {
                etIncomeGoal.setError("Hạn mức thu nhập không hợp lệ");
                return;
            }
        }

        double savingGoal = 0.0;
        if (!savingGoalStr.isEmpty()) {
            try {
                savingGoal = Double.parseDouble(savingGoalStr);
            } catch (NumberFormatException e) {
                etSavingGoal.setError("Hạn mức tiết kiệm không hợp lệ");
                return;
            }
        }

        btnSave.setEnabled(false);
        Toast.makeText(this, "Đang lưu thông tin...", Toast.LENGTH_SHORT).show();

        // Use Firestore WriteBatch to update both collections atomically
        WriteBatch batch = db.batch();

        // 1. Update fullName in 'users/{userId}'
        batch.update(db.collection("users").document(userId), "fullName", fullName);

        // 2. Set/Update all details in 'profiles/{userId}'
        UserProfile profile = new UserProfile(
                userId,
                null, // avatarUrl (not implemented yet, default to null)
                phoneNumber,
                dob,
                gender,
                address,
                occupation,
                incomeGoal,
                savingGoal,
                "light", // default theme
                "vi",    // default language
                true,    // default notificationsEnabled
                bio
        );
        batch.set(db.collection("profiles").document(userId), profile);

        // Commit Batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
