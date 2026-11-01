package com.example.ideaformaayanzohar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ideaformaayanzohar.adapter.ReminderAdapter;
import com.example.ideaformaayanzohar.ai.AiEngine;
import com.example.ideaformaayanzohar.db.AppDatabase;
import com.example.ideaformaayanzohar.location.LocationHelper;
import com.example.ideaformaayanzohar.model.Reminder;
import com.example.ideaformaayanzohar.voice.VoiceSpeaker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ReminderAdapter.OnReminderActionListener {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static final int REQUEST_CODE_LOCATION_PERM = 2000;

    private AppDatabase db;
    private LocationHelper locationHelper;
    private VoiceSpeaker voiceSpeaker;

    private RecyclerView rvReminders;
    private ReminderAdapter adapter;
    private LinearLayout layoutEmptyState;
    private TextView tvSummary;
    private ChipGroup chipGroupCategories;
    private Chip chipAiStatus;
    private ImageButton btnSettings;

    private FloatingActionButton fabAddReminder;
    private ExtendedFloatingActionButton fabAiVoice;

    private String currentSelectedCategory = "הכל";
    private TextInputEditText activeAiInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Helpers & Database
        db = AppDatabase.getInstance(this);
        locationHelper = new LocationHelper(this);
        voiceSpeaker = new VoiceSpeaker(this);

        // Request Location Permission
        checkLocationPermission();

        // Initialize Views
        rvReminders = findViewById(R.id.rvReminders);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvSummary = findViewById(R.id.tvSummary);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        chipAiStatus = findViewById(R.id.chipAiStatus);
        btnSettings = findViewById(R.id.btnSettings);

        fabAddReminder = findViewById(R.id.fabAddReminder);
        fabAiVoice = findViewById(R.id.fabAiVoice);

        // Setup RecyclerView
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(this);
        rvReminders.setAdapter(adapter);

        // Category Filter Listener
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentSelectedCategory = "הכל";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chipShopping) currentSelectedCategory = "קניות";
                else if (id == R.id.chipWork) currentSelectedCategory = "עבודה";
                else if (id == R.id.chipStudy) currentSelectedCategory = "לימודים";
                else if (id == R.id.chipHome) currentSelectedCategory = "בית";
                else currentSelectedCategory = "הכל";
            }
            loadReminders();
        });

        // Listeners
        fabAddReminder.setOnClickListener(v -> showAddReminderDialog(null));
        fabAiVoice.setOnClickListener(v -> showAiAssistantBottomSheet());
        chipAiStatus.setOnClickListener(v -> showAiAssistantBottomSheet());
        btnSettings.setOnClickListener(v -> showSettingsDialog());

        // Populate Sample Data if Empty
        populateSampleDataIfEmpty();

        // Initial Data Load
        loadReminders();
    }

    private void checkLocationPermission() {
        if (!locationHelper.hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERM
            );
        }
    }

    private void populateSampleDataIfEmpty() {
        List<Reminder> existing = db.reminderDao().getAllReminders();
        if (existing == null || existing.isEmpty()) {
            db.reminderDao().insert(new Reminder("לקנות חלב וירקות 🥛", "לזכור לקנות גם קרטון חלב 3% ועגבניות", "קניות", System.currentTimeMillis(), 32.0853, 34.7818, "סופרמרקט", false, 3));
            db.reminderDao().insert(new Reminder("להגיש הצעת פרויקט גמר 📚", "למלא את 9 הסעיפים בהצעת פרויקט גמר באנדרואיד ולשלוק למורה יוסף", "לימודים", System.currentTimeMillis(), 32.1818, 34.8718, "בית ספר AMIT", false, 3));
            db.reminderDao().insert(new Reminder("סידור וניקוי החדר 🏠", "לסדר את שולחן העבודה והמחשב", "בית", System.currentTimeMillis(), 32.0, 34.0, "בית", false, 1));
        }
    }

    private void loadReminders() {
        List<Reminder> list;
        if ("הכל".equals(currentSelectedCategory)) {
            list = db.reminderDao().getAllReminders();
        } else {
            list = db.reminderDao().getRemindersByCategory(currentSelectedCategory);
        }

        adapter.setReminderList(list);

        if (list.isEmpty()) {
            rvReminders.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvReminders.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }

        // Update AI Summary
        List<Reminder> activeList = db.reminderDao().getActiveReminders();
        String summary = AiEngine.generateDailySummary(activeList);
        tvSummary.setText(summary);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageButton btnCloseSettings = dialogView.findViewById(R.id.btnCloseSettings);
        ChipGroup chipGroupTheme = dialogView.findViewById(R.id.chipGroupTheme);
        SwitchMaterial switchVoiceSpeaker = dialogView.findViewById(R.id.switchVoiceSpeaker);
        Button btnShareReminders = dialogView.findViewById(R.id.btnShareReminders);
        Button btnClearCompleted = dialogView.findViewById(R.id.btnClearCompleted);
        Button btnResetSampleData = dialogView.findViewById(R.id.btnResetSampleData);

        if (btnCloseSettings != null) btnCloseSettings.setOnClickListener(v -> dialog.dismiss());

        if (switchVoiceSpeaker != null) {
            switchVoiceSpeaker.setChecked(voiceSpeaker.isSpeechEnabled());
            switchVoiceSpeaker.setOnCheckedChangeListener((buttonView, isChecked) -> voiceSpeaker.setSpeechEnabled(isChecked));
        }

        if (chipGroupTheme != null) {
            chipGroupTheme.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int id = checkedIds.get(0);
                    if (id == R.id.chipThemeLight) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (id == R.id.chipThemeDark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                }
            });
        }

        if (btnShareReminders != null) {
            btnShareReminders.setOnClickListener(v -> {
                shareRemindersList();
                dialog.dismiss();
            });
        }

        if (btnClearCompleted != null) {
            btnClearCompleted.setOnClickListener(v -> {
                List<Reminder> all = db.reminderDao().getAllReminders();
                for (Reminder r : all) {
                    if (r.isCompleted()) {
                        db.reminderDao().delete(r);
                    }
                }
                Toast.makeText(this, "התזכורות שהושלמו נוקו", Toast.LENGTH_SHORT).show();
                loadReminders();
                dialog.dismiss();
            });
        }

        if (btnResetSampleData != null) {
            btnResetSampleData.setOnClickListener(v -> {
                List<Reminder> all = db.reminderDao().getAllReminders();
                for (Reminder r : all) {
                    db.reminderDao().delete(r);
                }
                populateSampleDataIfEmpty();
                Toast.makeText(this, "נתוני ההדגמה אופסו בהצלחה", Toast.LENGTH_SHORT).show();
                loadReminders();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void shareRemindersList() {
        List<Reminder> activeList = db.reminderDao().getActiveReminders();
        if (activeList.isEmpty()) {
            Toast.makeText(this, "אין משימות אקטיביות לשיתוף", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📌 רשימת המשימות שלי מ-GeoRemind AI:\n\n");
        for (int i = 0; i < activeList.size(); i++) {
            Reminder r = activeList.get(i);
            sb.append(i + 1).append(". ").append(r.getTitle());
            if (r.getLocationName() != null && !r.getLocationName().isEmpty()) {
                sb.append(" (📍 ").append(r.getLocationName()).append(")");
            }
            sb.append("\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "רשימת משימות - GeoRemind AI");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "שתף משימות באמצעות..."));
    }

    private void showAiAssistantBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ai_assistant, null);
        bottomSheet.setContentView(dialogView);

        TextInputEditText etAiInput = dialogView.findViewById(R.id.etAiInput);
        this.activeAiInputEditText = etAiInput;

        Button btnProcessAi = dialogView.findViewById(R.id.btnProcessAi);
        Button btnVoiceMic = dialogView.findViewById(R.id.btnVoiceMic);
        ImageButton btnCloseAi = dialogView.findViewById(R.id.btnCloseAi);

        MaterialCardView cardAiResponse = dialogView.findViewById(R.id.cardAiResponse);
        TextView tvAiResponseText = dialogView.findViewById(R.id.tvAiResponseText);

        Chip chipPrompt1 = dialogView.findViewById(R.id.chipPrompt1);
        Chip chipPrompt2 = dialogView.findViewById(R.id.chipPrompt2);
        Chip chipPrompt3 = dialogView.findViewById(R.id.chipPrompt3);

        if (chipPrompt1 != null) chipPrompt1.setOnClickListener(v -> etAiInput.setText("תזכיר לי לקנות חלב בסופרמרקט"));
        if (chipPrompt2 != null) chipPrompt2.setOnClickListener(v -> etAiInput.setText("מה המשימות הדחופות שלי להיום?"));
        if (chipPrompt3 != null) chipPrompt3.setOnClickListener(v -> etAiInput.setText("תזכיר לי להגיש עבודה במחשבים מחר"));

        if (btnCloseAi != null) btnCloseAi.setOnClickListener(v -> bottomSheet.dismiss());

        if (btnVoiceMic != null) btnVoiceMic.setOnClickListener(v -> startVoiceRecognition());

        if (btnProcessAi != null) {
            btnProcessAi.setOnClickListener(v -> {
                String text = etAiInput.getText() != null ? etAiInput.getText().toString().trim() : "";
                if (text.isEmpty()) {
                    etAiInput.setError("נא להזין בקשה או תזכורת ל-AI");
                    return;
                }

                List<Reminder> activeList = db.reminderDao().getActiveReminders();
                String aiResponse = AiEngine.processPersonalAssistantQuery(text, activeList);

                tvAiResponseText.setText(aiResponse);
                cardAiResponse.setVisibility(View.VISIBLE);

                // Speak out loud via TextToSpeech
                voiceSpeaker.speak(aiResponse);

                // Refresh reminders if new item created
                loadReminders();
            });
        }

        bottomSheet.show();
    }

    private void showAddReminderDialog(@Nullable Reminder existingReminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etTitle = dialogView.findViewById(R.id.etTitle);
        TextInputEditText etContent = dialogView.findViewById(R.id.etContent);
        TextInputEditText etLocation = dialogView.findViewById(R.id.etLocation);

        ChipGroup chipGroupCategorySelect = dialogView.findViewById(R.id.chipGroupCategorySelect);
        ChipGroup chipGroupPrioritySelect = dialogView.findViewById(R.id.chipGroupPrioritySelect);

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        View btnAiAutoFill = dialogView.findViewById(R.id.btnAiAutoFill);

        if (existingReminder != null) {
            if (tvDialogTitle != null) tvDialogTitle.setText("עריכת תזכורת ✏️");
            etTitle.setText(existingReminder.getTitle());
            etContent.setText(existingReminder.getContent());
            etLocation.setText(existingReminder.getLocationName());
        }

        if (btnAiAutoFill != null) {
            btnAiAutoFill.setOnClickListener(v -> {
                String titleText = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                if (titleText.isEmpty()) {
                    etTitle.setError("הזן משהו בכותרת למילוי ב-AI");
                    return;
                }
                Reminder aiParsed = AiEngine.parseFreeTextToReminder(titleText);
                etLocation.setText(aiParsed.getLocationName());
                etContent.setText(aiParsed.getContent());
                Toast.makeText(this, "מילוי AI הושלם בהצלחה!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
                String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";
                String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

                if (title.isEmpty()) {
                    etTitle.setError("נא להזין כותרת לתזכורת");
                    return;
                }

                // Category Selection
                String category = "כללי";
                int selectedCatId = chipGroupCategorySelect.getCheckedChipId();
                if (selectedCatId == R.id.chipCatShopping) category = "קניות";
                else if (selectedCatId == R.id.chipCatWork) category = "עבודה";
                else if (selectedCatId == R.id.chipCatStudy) category = "לימודים";
                else if (selectedCatId == R.id.chipCatHome) category = "בית";

                // Priority Selection
                int priority = 1;
                int selectedPrioId = chipGroupPrioritySelect.getCheckedChipId();
                if (selectedPrioId == R.id.chipPriorityMedium) priority = 2;
                else if (selectedPrioId == R.id.chipPriorityHigh) priority = 3;

                if (existingReminder != null) {
                    existingReminder.setTitle(title);
                    existingReminder.setContent(content);
                    existingReminder.setLocationName(location);
                    existingReminder.setCategory(category);
                    existingReminder.setPriority(priority);
                    db.reminderDao().update(existingReminder);
                    Toast.makeText(this, "התזכורת עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                } else {
                    Reminder newReminder = new Reminder(title, content, category, System.currentTimeMillis(), 0.0, 0.0, location, false, priority);
                    db.reminderDao().insert(newReminder);
                    Toast.makeText(this, "התזכורת נוצרה בהצלחה", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
                loadReminders();
            });
        }

        dialog.show();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL"); // עברית
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "דבר עכשיו ליצירת תזכורת AI...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "הכתבה קולית לא נתמכת במכשיר זה, מזין טקסט AI לדוגמה", Toast.LENGTH_LONG).show();
            if (activeAiInputEditText != null) {
                activeAiInputEditText.setText("לקנות חלב וירקות בסופרמרקט היום בערב");
            } else {
                createAiSimulatedReminder("לקנות חלב וירקות בסופרמרקט היום בערב");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                if (activeAiInputEditText != null) {
                    activeAiInputEditText.setText(spokenText);
                } else {
                    createAiSimulatedReminder(spokenText);
                }
            }
        }
    }

    private void createAiSimulatedReminder(String text) {
        Reminder aiParsedReminder = AiEngine.parseFreeTextToReminder(text);
        db.reminderDao().insert(aiParsedReminder);
        Toast.makeText(this, "AI פענח ונוצרה תזכורת: " + aiParsedReminder.getTitle(), Toast.LENGTH_LONG).show();
        loadReminders();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "הרשאת מיקום אושרה!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceSpeaker != null) {
            voiceSpeaker.shutdown();
        }
    }

    // ReminderAdapter Callbacks
    @Override
    public void onToggleCompleted(Reminder reminder, boolean isChecked) {
        reminder.setCompleted(isChecked);
        db.reminderDao().update(reminder);
        loadReminders();
    }

    @Override
    public void onDeleteReminder(Reminder reminder) {
        db.reminderDao().delete(reminder);
        Toast.makeText(this, "התזכורת נמחקה", Toast.LENGTH_SHORT).show();
        loadReminders();
    }

    @Override
    public void onItemClick(Reminder reminder) {
        showAddReminderDialog(reminder);
    }
}
