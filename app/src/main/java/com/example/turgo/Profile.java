package com.example.turgo;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.widget.ImageView.ScaleType;
import android.text.InputType;
import android.text.TextUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Profile extends Fragment implements ProfileSubSettingActionListener {
    private static final String TAG = "ProfileEmailChange";

    private User user;
    private ArrayList<Setting> settings;
    private TextView tvProfileName;
    private ImageView ivProfileAvatar;
    private RecyclerView rvProfileSettings;
    private ActivityResultLauncher<Intent> profileImagePickerLauncher;

    public Profile(User user) {
        this.user = user;
    }

    public Profile() {
    }

    public static Profile newInstance(String param1, String param2) {
        return new Profile();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK
                            || result.getData() == null
                            || result.getData().getData() == null) {
                        return;
                    }
                    Uri selectedImage = result.getData().getData();
                    uploadProfileImage(selectedImage);
                }
        );
    }

    private void resolveUserFromActivity() {
        if (user != null) {
            return;
        }
        if (getActivity() instanceof StudentScreen) {
            user = ((StudentScreen) requireActivity()).getStudent();
            return;
        }
        if (getActivity() instanceof TeacherScreen) {
            user = ((TeacherScreen) requireActivity()).getTeacher();
        }
    }

    private void initializeContent() {
        settings = new ArrayList<>();
        if (user == null) {
            return;
        }
        Setting personalInfo = new Setting("Personal Info");
        settings.add(personalInfo);
        Setting contact = new Setting("Contact");
        settings.add(contact);
        Setting preferences = new Setting("Preferences");
        settings.add(preferences);
        Setting language = new Setting("Language");
        settings.add(language);

        personalInfo.addSubSettings("Full Name", user.getFullName(), SettingEditType.EDIT_TEXT);
        personalInfo.addSubSettings("Nickname", user.getNickname(), SettingEditType.EDIT_TEXT);
        personalInfo.addSubSettings("Date Of Birth", user.getBirthDate(), SettingEditType.EDIT_TEXT);
        personalInfo.addSubSettings("Gender", user.getGender(), SettingEditType.SPINNER);

        contact.addSubSettings("Email", user.getEmail(), SettingEditType.EDIT_TEXT);
        contact.addSubSettings("Phone Number", user.getPhoneNumber(), SettingEditType.EDIT_TEXT);

        preferences.addSubSettings("Theme", user.getTheme().getTheme(), SettingEditType.SPINNER);
        preferences.addSubSettings("Notifications", "Enabled", SettingEditType.SPINNER);
        if (user instanceof Student) {
            preferences.addSubSettings("Auto Schedule Meeting", Integer.toString(((Student) user).getAutoSchedule()), SettingEditType.SPINNER);
        }
        preferences.addSubSettings("Profile Picture", "Change profile image", SettingEditType.UPLOAD);

        language.addSubSettings("Language", user.getLanguage().getDisplayName(), SettingEditType.SPINNER);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        rvProfileSettings = view.findViewById(R.id.rv_profile_settings);

        resolveUserFromActivity();
        if (user == null) {
            Toast.makeText(requireContext(), "Unable to load profile.", Toast.LENGTH_SHORT).show();
            return view;
        }

        tvProfileName.setText(Tool.boolOf(user.getFullName()) ? user.getFullName() : "User");
        String profileImageUrl = resolveProfileImageUrl();
        if (Tool.boolOf(profileImageUrl)) {
            showProfileAvatar(profileImageUrl);
        }

        initializeContent();
        rvProfileSettings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProfileSettings.setAdapter(new ProfileSettingsAdapter(requireContext(), settings, this));
        syncAuthEmailToProfileIfChanged(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncAuthEmailToProfileIfChanged(true);
    }

    @Override
    public void onSaveSubSetting(Setting group, SubSetting subSetting, String newValue) {
        if (user == null || subSetting == null || !Tool.boolOf(newValue)) {
            return;
        }
        String key = subSetting.getSubSetting();
        Map<String, Object> updates = new HashMap<>();

        switch (key) {
            case "Full Name":
                user.setFullName(newValue);
                updates.put("fullName", newValue);
                tvProfileName.setText(newValue);
                break;
            case "Nickname":
                user.setNickname(newValue);
                updates.put("nickname", newValue);
                break;
            case "Date Of Birth":
                user.setBirthDate(newValue);
                updates.put("birthDate", newValue);
                break;
            case "Gender":
                user.setGender(newValue);
                updates.put("gender", newValue);
                break;
            case "Email":
                launchEmailChangeFlow(newValue);
                return;
            case "Phone Number":
                user.setPhoneNumber(newValue);
                updates.put("phoneNumber", newValue);
                break;
            case "Theme":
                Theme selectedTheme = parseTheme(newValue);
                if (selectedTheme != null) {
                    user.setTheme(selectedTheme);
                    updates.put("theme", selectedTheme.getTheme());
                }
                break;
            case "Language":
                Language selectedLanguage = parseLanguage(newValue);
                if (selectedLanguage != null) {
                    user.setLanguage(selectedLanguage);
                    updates.put("language", selectedLanguage.getDisplayName());
                }
                break;
            case "Auto Schedule Meeting":
                if (user instanceof Student) {
                    int autoSchedule = parsePositiveInt(newValue, ((Student) user).getAutoSchedule());
                    ((Student) user).setAutoSchedule(autoSchedule);
                    updates.put("autoSchedule", autoSchedule);
                }
                break;
            case "Notifications":
                Toast.makeText(requireContext(), "Notification preference save is not wired yet.", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        persistProfileUpdates(updates, true);
    }

    private void launchEmailChangeFlow(String initialEmailInput) {
        refreshSettingsList();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "You must be signed in to change email.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Tool.boolOf(firebaseUser.getUid()) || !firebaseUser.getUid().equals(user.getUid())) {
            Toast.makeText(requireContext(), "Session mismatch. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isGoogleOnly = AuthEmailChangeManager.isGoogleProvider(firebaseUser)
                && !AuthEmailChangeManager.hasPasswordProvider(firebaseUser);
        if (isGoogleOnly) {
            Toast.makeText(requireContext(), "Google-only accounts must change email from your Google account.", Toast.LENGTH_LONG).show();
            return;
        }

        showEmailChangeDialog(firebaseUser, initialEmailInput);
    }

    private void showEmailChangeDialog(FirebaseUser firebaseUser, String initialEmailInput) {
        if (!isAdded()) {
            return;
        }
        int spacing = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                requireContext().getResources().getDisplayMetrics()
        );
        int padding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4,
                requireContext().getResources().getDisplayMetrics()
        );

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, padding);

        EditText etNewEmail = new EditText(requireContext());
        etNewEmail.setHint("New email");
        etNewEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etNewEmail.setSingleLine(true);
        etNewEmail.setText(initialEmailInput);

        EditText etConfirmEmail = new EditText(requireContext());
        etConfirmEmail.setHint("Confirm new email");
        etConfirmEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etConfirmEmail.setSingleLine(true);

        container.addView(etNewEmail);
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        confirmParams.topMargin = spacing;
        etConfirmEmail.setLayoutParams(confirmParams);
        container.addView(etConfirmEmail);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Email Change")
                .setMessage("We will send a verification link to your new email.")
                .setView(container)
                .setNegativeButton("Cancel", (d, which) -> refreshSettingsList())
                .setPositiveButton("Send Verification", null)
                .create();

        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newEmail = etNewEmail.getText() != null ? etNewEmail.getText().toString().trim() : "";
            String confirmEmail = etConfirmEmail.getText() != null ? etConfirmEmail.getText().toString().trim() : "";
            String validationError = AuthEmailChangeManager.validateEmailInputs(newEmail, confirmEmail, firebaseUser.getEmail());
            if (!TextUtils.isEmpty(validationError)) {
                Toast.makeText(requireContext(), validationError, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            requestVerifyBeforeUpdate(firebaseUser, newEmail);
        }));

        dialog.show();
    }

    private void requestVerifyBeforeUpdate(FirebaseUser firebaseUser, String newEmail) {
        AuthEmailChangeManager.verifyBeforeUpdateEmail(firebaseUser, newEmail, new AuthEmailChangeManager.VerificationCallback() {
            @Override
            public void onVerificationSent() {
                if (!isAdded()) {
                    return;
                }
                showEmailChangeNoticeDialog();
                refreshSettingsList();
            }

            @Override
            public void onRecentLoginRequired() {
                if (!isAdded()) {
                    return;
                }
                promptPasswordReauth(firebaseUser, newEmail);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                Log.e(TAG, "verifyBeforeUpdateEmail failed: " + message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                refreshSettingsList();
            }
        });
    }

    private void promptPasswordReauth(FirebaseUser firebaseUser, String pendingEmail) {
        if (!isAdded()) {
            return;
        }
        if (!AuthEmailChangeManager.hasPasswordProvider(firebaseUser) || !Tool.boolOf(firebaseUser.getEmail())) {
            Toast.makeText(requireContext(), "Please sign in again before changing your email.", Toast.LENGTH_LONG).show();
            return;
        }

        EditText etPassword = new EditText(requireContext());
        etPassword.setHint("Current password");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etPassword.setSingleLine(true);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Reauthenticate")
                .setMessage("For security, enter your current password.")
                .setView(etPassword)
                .setNegativeButton("Cancel", (d, which) -> refreshSettingsList())
                .setPositiveButton("Continue", null)
                .create();

        dialog.setOnShowListener(unused -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            if (!Tool.boolOf(password)) {
                Toast.makeText(requireContext(), "Password is required.", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            AuthEmailChangeManager.reauthenticateWithPassword(
                    firebaseUser,
                    firebaseUser.getEmail(),
                    password,
                    new AuthEmailChangeManager.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            requestVerifyBeforeUpdate(firebaseUser, pendingEmail);
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded()) {
                                return;
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                            refreshSettingsList();
                        }
                    }
            );
        }));
        dialog.show();
    }

    private void showEmailChangeNoticeDialog() {
        if (!isAdded()) {
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_email_change_notice, null, false);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        View okButton = dialogView.findViewById(R.id.btn_email_change_notice_ok);
        okButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void refreshSettingsList() {
        if (!isAdded() || rvProfileSettings == null) {
            return;
        }
        initializeContent();
        rvProfileSettings.setAdapter(new ProfileSettingsAdapter(requireContext(), settings, this));
    }

    private void syncAuthEmailToProfileIfChanged(boolean showSuccessToast) {
        if (user == null || !Tool.boolOf(user.getUid()) || !isAdded()) {
            return;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || !Tool.boolOf(firebaseUser.getUid()) || !firebaseUser.getUid().equals(user.getUid())) {
            return;
        }

        firebaseUser.reload().addOnSuccessListener(unused -> {
            if (!isAdded()) {
                return;
            }
            String authEmail = firebaseUser.getEmail();
            if (!Tool.boolOf(authEmail)) {
                return;
            }
            String current = user.getEmail();
            if (Tool.boolOf(current) && authEmail.equalsIgnoreCase(current)) {
                return;
            }

            user.setEmail(authEmail);
            Map<String, Object> updates = new HashMap<>();
            updates.put("email", authEmail);
            persistProfileUpdates(updates, false);
            refreshSettingsList();
            if (showSuccessToast) {
                Toast.makeText(requireContext(), "Email updated successfully.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void persistProfileUpdates(Map<String, Object> updates) {
        persistProfileUpdates(updates, true);
    }

    private void persistProfileUpdates(Map<String, Object> updates, boolean showToast) {
        if (updates == null || updates.isEmpty() || user == null || !Tool.boolOf(user.getUid())) {
            return;
        }
        if (user instanceof Student) {
            StudentRepository repository = new StudentRepository(user.getUid());
            repository.updateMultipleFields(updates);
            if (showToast) {
                Toast.makeText(requireContext(), "Profile setting updated.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (user instanceof Teacher) {
            TeacherRepository repository = new TeacherRepository(user.getUid());
            repository.updateMultipleFields(updates);
            if (showToast) {
                Toast.makeText(requireContext(), "Profile setting updated.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(1, parsed);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Theme parseTheme(String value) {
        for (Theme t : Theme.values()) {
            if (t.getTheme().equalsIgnoreCase(value)) {
                return t;
            }
        }
        return null;
    }

    private Language parseLanguage(String value) {
        for (Language l : Language.values()) {
            if (l.getDisplayName().equalsIgnoreCase(value)) {
                return l;
            }
        }
        return null;
    }

    @Override
    public void onUploadRequested(Setting group, SubSetting subSetting) {
        if (!isAdded()) {
            return;
        }
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        profileImagePickerLauncher.launch(Intent.createChooser(pickIntent, "Select Profile Image"));
    }

    private void uploadProfileImage(Uri imageUri) {
        if (!isAdded() || imageUri == null) {
            return;
        }

        Bitmap sourceBitmap;
        try {
            sourceBitmap = decodeBitmapFromUri(imageUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to open selected image.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sourceBitmap == null) {
            Toast.makeText(requireContext(), "Invalid image file.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap croppedSquare = cropToSquare(sourceBitmap);
        File croppedFile;
        try {
            croppedFile = saveBitmapToCache(croppedSquare);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to process image.", Toast.LENGTH_SHORT).show();
            return;
        }

        Tool.uploadToCloudinary(croppedFile, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(String imageUrl) {
                if (!isAdded()) {
                    return;
                }
                user.setPfpCloudinary(imageUrl);
                if (user instanceof Teacher) {
                    ((Teacher) user).setProfileImageCloudinary(imageUrl);
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("pfpCloudinary", imageUrl);
                if (user instanceof Teacher) {
                    updates.put("profileImageCloudinary", imageUrl);
                }
                persistProfileUpdates(updates, false);
                showProfileAvatar(imageUrl);
                Toast.makeText(requireContext(), "Profile picture updated.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(DatabaseError error) {
                if (!isAdded()) {
                    return;
                }
                if (Tool.isConnectivityIssue(error)) {
                    Toast.makeText(requireContext(), "No internet connection. Upload failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to upload profile picture.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Bitmap decodeBitmapFromUri(Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), imageUri);
            return ImageDecoder.decodeBitmap(source);
        }
        // minSdk is 30, this is just a fallback path.
        return android.provider.MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
    }

    private Bitmap cropToSquare(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int side = Math.min(width, height);
        int x = (width - side) / 2;
        int y = (height - side) / 2;
        return Bitmap.createBitmap(source, x, y, side, side);
    }

    private File saveBitmapToCache(Bitmap bitmap) throws IOException {
        File outFile = File.createTempFile("profile_square_", ".jpg", requireContext().getCacheDir());
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out);
            out.flush();
        }
        return outFile;
    }

    private void showProfileAvatar(String imageUrl) {
        if (!isAdded() || ivProfileAvatar == null || !Tool.boolOf(imageUrl)) {
            return;
        }
        ivProfileAvatar.setImageTintList((ColorStateList) null);
        ivProfileAvatar.setColorFilter(null);
        ivProfileAvatar.setPadding(0, 0, 0, 0);
        ivProfileAvatar.setScaleType(ScaleType.CENTER_CROP);
        Tool.setImageCloudinary(requireContext(), imageUrl, ivProfileAvatar);
    }

    private String resolveProfileImageUrl() {
        if (user == null) {
            return "";
        }
        if (Tool.boolOf(user.getPfpCloudinary())) {
            return user.getPfpCloudinary();
        }
        if (user instanceof Teacher) {
            String teacherImage = ((Teacher) user).getProfileImageCloudinary();
            if (Tool.boolOf(teacherImage)) {
                return teacherImage;
            }
        }
        return "";
    }
}
