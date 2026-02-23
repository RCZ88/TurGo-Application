package com.example.turgo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityComposeMailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ComposeMail extends AppCompatActivity {
    private EditText et_subject;
    private EditText et_body;
    private AutoCompleteTextView actv_to;
    private Button btn_send;
    private Button btn_draft;
    private ImageButton ib_bold;
    private ImageButton ib_italic;
    private ImageButton ib_underline;
    private TextView btn_addAttachment;
    private RecyclerView rvRecipientResults;
    private RecyclerView rvSelectedRecipients;
    private RecyclerView rvUploadedFiles;
    private LinearLayout llNoRecipientsSelected;

    private User user;

    private final ArrayList<User> cachedUsers = new ArrayList<>();
    private final HashSet<String> cachedUserIds = new HashSet<>();
    private final ArrayList<User> selectedRecipients = new ArrayList<>();
    private final ArrayList<Uri> filesUploaded = new ArrayList<>();
    private final ArrayList<SubmissionDisplay> attachmentPreview = new ArrayList<>();
    private final Map<String, file> uploadedAttachmentByUri = new HashMap<>();
    private final HashSet<String> uploadingAttachmentUris = new HashSet<>();
    private final HashSet<String> failedAttachmentUris = new HashSet<>();


    private ComposeRecipientListAdapter searchAdapter;
    private ComposeRecipientListAdapter selectedAdapter;
    private SubmissionAdapter attachmentAdapter;
    private Mail editingMail;
    private boolean pendingBold;
    private boolean pendingItalic;
    private boolean pendingUnderline;
    private int lastBodyEditStart = -1;
    private int lastBodyEditBefore;
    private int lastBodyEditCount;
    ActivityComposeMailBinding binding;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityComposeMailBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomInset);
            return insets;
        });


        actv_to = findViewById(R.id.actv_CM_To);
        et_subject = findViewById(R.id.et_CM_Subject);
        et_body = findViewById(R.id.etml_CM_Body);
        btn_addAttachment = findViewById(R.id.btn_CM_AddAttachment);
        btn_send = findViewById(R.id.btn_CM_Send);
        btn_draft = findViewById(R.id.btn_CM_Draft);
        ib_bold = findViewById(R.id.ib_CM_Bold);
        ib_italic = findViewById(R.id.ib_CM_Italic);
        ib_underline = findViewById(R.id.ib_CM_Underline);
        rvRecipientResults = findViewById(R.id.rv_CM_RecipientResults);
        rvSelectedRecipients = findViewById(R.id.rv_CM_SelectedRecipients);
        rvUploadedFiles = findViewById(R.id.rv_CM_UploadedFiles);
        llNoRecipientsSelected = findViewById(R.id.ll_CM_NoRecipientsSelected);

        Intent intent = getIntent();
        if (intent.getSerializableExtra(User.SERIALIZE_KEY_CODE) != null) {
            user = (User) intent.getSerializableExtra(User.SERIALIZE_KEY_CODE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            editingMail = intent.getSerializableExtra(Mail.SERIALIZE_KEY_CODE, Mail.class);
        } else {
            Object extra = intent.getSerializableExtra(Mail.SERIALIZE_KEY_CODE);
            if (extra instanceof Mail) {
                editingMail = (Mail) extra;
            }
        }

        setupRecipientLists();
        preloadUsers();
        setupRecipientSearchInput();
        setupFormattingButtons();
        setupAttachmentRecycler();
        setupAttachmentPicker();
        setupSendButtons();
        populateComposeFromEditingMail();
    }

    private void setupAttachmentRecycler() {
        attachmentAdapter = new SubmissionAdapter(
                attachmentPreview,
                SubmissionItemMode.FILE_PICKER,
                this::removeAttachmentAt
        );
        rvUploadedFiles.setLayoutManager(new LinearLayoutManager(this));
        rvUploadedFiles.setAdapter(attachmentAdapter);
    }

    private void removeAttachmentAt(int position) {
        if (position < 0 || position >= filesUploaded.size()) {
            return;
        }
        Uri removedUri = filesUploaded.remove(position);
        if (removedUri != null) {
            String key = removedUri.toString();
            uploadedAttachmentByUri.remove(key);
            uploadingAttachmentUris.remove(key);
            failedAttachmentUris.remove(key);
        }
        attachmentPreview.remove(position);
        attachmentAdapter.notifyItemRemoved(position);
        attachmentAdapter.notifyItemRangeChanged(position, attachmentPreview.size() - position);
        updateAttachmentUploadUiState();
    }

    private void setupRecipientLists() {
        searchAdapter = new ComposeRecipientListAdapter(false, this::addRecipient);
        selectedAdapter = new ComposeRecipientListAdapter(true, this::removeRecipient);

        rvRecipientResults.setLayoutManager(new LinearLayoutManager(this));
        rvRecipientResults.setAdapter(searchAdapter);

        rvSelectedRecipients.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedRecipients.setAdapter(selectedAdapter);
        selectedAdapter.submitList(selectedRecipients);
        updateSelectedRecipientsVisibility();
    }

    private void setupRecipientSearchInput() {
        actv_to.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                updateRecipientSuggestions(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFormattingButtons() {
        ib_bold.setFocusable(false);
        ib_italic.setFocusable(false);
        ib_underline.setFocusable(false);
        ib_bold.setFocusableInTouchMode(false);
        ib_italic.setFocusableInTouchMode(false);
        ib_underline.setFocusableInTouchMode(false);

        ib_bold.setOnClickListener(v -> onStyleButtonClicked(Typeface.BOLD));
        ib_italic.setOnClickListener(v -> onStyleButtonClicked(Typeface.ITALIC));
        ib_underline.setOnClickListener(v -> onUnderlineButtonClicked());

        et_body.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                syncPendingStylesFromCursor();
            }
        });
        et_body.setOnClickListener(v -> syncPendingStylesFromCursor());
        et_body.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastBodyEditStart = start;
                lastBodyEditBefore = count;
                lastBodyEditCount = after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                applyPendingStylesToInsertedText(editable);
            }
        });

        updateFormattingButtonStates();
    }

    private void onStyleButtonClicked(int targetStyle) {
        Editable editable = et_body.getText();
        int start = Selection.getSelectionStart(editable);
        int end = Selection.getSelectionEnd(editable);
        if (start < 0 || end < 0) {
            et_body.requestFocus();
            int cursor = editable.length();
            et_body.setSelection(cursor);
            start = cursor;
            end = cursor;
        }

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        if (start == end) {
            togglePendingStyle(targetStyle);
            updateFormattingButtonStates();
            return;
        }

        if (isStyleFullyApplied(editable, start, end, targetStyle)) {
            removeStyleFromRange(editable, start, end, targetStyle);
        } else {
            addStyleToRange(editable, start, end, targetStyle);
        }

        et_body.setSelection(end);
        syncPendingStylesFromCursor();
        updateFormattingButtonStates();
    }

    private void onUnderlineButtonClicked() {
        Editable editable = et_body.getText();
        int start = Selection.getSelectionStart(editable);
        int end = Selection.getSelectionEnd(editable);
        if (start < 0 || end < 0) {
            et_body.requestFocus();
            int cursor = editable.length();
            et_body.setSelection(cursor);
            start = cursor;
            end = cursor;
        }

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        if (start == end) {
            pendingUnderline = !pendingUnderline;
            updateFormattingButtonStates();
            return;
        }

        if (isUnderlineFullyApplied(editable, start, end)) {
            removeUnderlineFromRange(editable, start, end);
        } else {
            editable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        et_body.setSelection(end);
        syncPendingStylesFromCursor();
        updateFormattingButtonStates();
    }

    private void togglePendingStyle(int targetStyle) {
        if (targetStyle == Typeface.BOLD) {
            pendingBold = !pendingBold;
            return;
        }
        if (targetStyle == Typeface.ITALIC) {
            pendingItalic = !pendingItalic;
        }
    }

    private void applyPendingStylesToInsertedText(Editable editable) {
        int inserted = lastBodyEditCount - lastBodyEditBefore;
        if (inserted <= 0) {
            return;
        }
        int start = Math.max(0, lastBodyEditStart);
        int end = Math.min(editable.length(), start + inserted);
        if (start >= end) {
            return;
        }

        int style = Typeface.NORMAL;
        if (pendingBold && pendingItalic) {
            style = Typeface.BOLD_ITALIC;
        } else if (pendingBold) {
            style = Typeface.BOLD;
        } else if (pendingItalic) {
            style = Typeface.ITALIC;
        }

        if (style != Typeface.NORMAL) {
            editable.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (pendingUnderline) {
            editable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void syncPendingStylesFromCursor() {
        Editable editable = et_body.getText();
        if (editable.length() == 0) {
            pendingBold = false;
            pendingItalic = false;
            pendingUnderline = false;
            updateFormattingButtonStates();
            return;
        }

        int cursor = et_body.getSelectionStart();
        if (cursor < 0) {
            updateFormattingButtonStates();
            return;
        }

        int index = Math.min(cursor, editable.length() - 1);
        pendingBold = hasStyleAt(editable, index, Typeface.BOLD);
        pendingItalic = hasStyleAt(editable, index, Typeface.ITALIC);
        pendingUnderline = hasUnderlineAt(editable, index);
        updateFormattingButtonStates();
    }

    private void updateFormattingButtonStates() {
        setButtonActive(ib_bold, pendingBold);
        setButtonActive(ib_italic, pendingItalic);
        setButtonActive(ib_underline, pendingUnderline);
    }

    private void setButtonActive(ImageButton button, boolean active) {
        button.setSelected(active);
        button.setAlpha(active ? 1f : 0.65f);
    }

    private boolean isStyleFullyApplied(Editable editable, int start, int end, int targetStyle) {
        int safeEnd = Math.min(end, editable.length());
        for (int i = start; i < safeEnd; i++) {
            if (!hasStyleAt(editable, i, targetStyle)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasStyleAt(Editable editable, int index, int targetStyle) {
        StyleSpan[] spans = editable.getSpans(index, Math.min(index + 1, editable.length()), StyleSpan.class);
        for (StyleSpan span : spans) {
            int s = editable.getSpanStart(span);
            int e = editable.getSpanEnd(span);
            if (s <= index && e > index) {
                int style = span.getStyle();
                if (targetStyle == Typeface.BOLD && (style == Typeface.BOLD || style == Typeface.BOLD_ITALIC)) {
                    return true;
                }
                if (targetStyle == Typeface.ITALIC && (style == Typeface.ITALIC || style == Typeface.BOLD_ITALIC)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeStyleFromRange(Editable editable, int start, int end, int targetStyle) {
        StyleSpan[] spans = editable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans) {
            int spanStyle = span.getStyle();
            if (!styleContains(spanStyle, targetStyle)) {
                continue;
            }
            int s = editable.getSpanStart(span);
            int e = editable.getSpanEnd(span);
            int overlapStart = Math.max(s, start);
            int overlapEnd = Math.min(e, end);
            editable.removeSpan(span);

            if (s < overlapStart) {
                editable.setSpan(new StyleSpan(spanStyle), s, overlapStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int reducedStyle = reduceStyle(spanStyle, targetStyle);
            if (reducedStyle != Typeface.NORMAL && overlapStart < overlapEnd) {
                editable.setSpan(new StyleSpan(reducedStyle), overlapStart, overlapEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (overlapEnd < e) {
                editable.setSpan(new StyleSpan(spanStyle), overlapEnd, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void addStyleToRange(Editable editable, int start, int end, int targetStyle) {
        boolean[] covered = new boolean[Math.max(0, end - start)];
        StyleSpan[] spans = editable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans) {
            int spanStyle = span.getStyle();
            int s = editable.getSpanStart(span);
            int e = editable.getSpanEnd(span);
            int overlapStart = Math.max(s, start);
            int overlapEnd = Math.min(e, end);
            if (overlapStart >= overlapEnd) {
                continue;
            }

            editable.removeSpan(span);
            if (s < overlapStart) {
                editable.setSpan(new StyleSpan(spanStyle), s, overlapStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int mergedStyle = mergeStyle(spanStyle, targetStyle);
            editable.setSpan(new StyleSpan(mergedStyle), overlapStart, overlapEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (overlapEnd < e) {
                editable.setSpan(new StyleSpan(spanStyle), overlapEnd, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            for (int i = overlapStart; i < overlapEnd; i++) {
                covered[i - start] = true;
            }
        }

        int runStart = -1;
        for (int i = 0; i <= covered.length; i++) {
            boolean isCovered = i < covered.length && covered[i];
            if (!isCovered && runStart == -1) {
                runStart = i;
            }
            if ((isCovered || i == covered.length) && runStart != -1) {
                int segStart = start + runStart;
                int segEnd = start + i;
                if (segStart < segEnd) {
                    editable.setSpan(new StyleSpan(targetStyle), segStart, segEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                runStart = -1;
            }
        }
    }

    private boolean styleContains(int current, int target) {
        if (target == Typeface.BOLD) {
            return current == Typeface.BOLD || current == Typeface.BOLD_ITALIC;
        }
        if (target == Typeface.ITALIC) {
            return current == Typeface.ITALIC || current == Typeface.BOLD_ITALIC;
        }
        return false;
    }

    private int reduceStyle(int current, int removeTarget) {
        if (current == Typeface.BOLD_ITALIC) {
            return removeTarget == Typeface.BOLD ? Typeface.ITALIC : Typeface.BOLD;
        }
        if (current == removeTarget) {
            return Typeface.NORMAL;
        }
        return current;
    }

    private int mergeStyle(int current, int addTarget) {
        boolean bold = current == Typeface.BOLD || current == Typeface.BOLD_ITALIC;
        boolean italic = current == Typeface.ITALIC || current == Typeface.BOLD_ITALIC;

        if (addTarget == Typeface.BOLD) {
            bold = true;
        } else if (addTarget == Typeface.ITALIC) {
            italic = true;
        }

        if (bold && italic) {
            return Typeface.BOLD_ITALIC;
        }
        if (bold) {
            return Typeface.BOLD;
        }
        if (italic) {
            return Typeface.ITALIC;
        }
        return Typeface.NORMAL;
    }

    private boolean isUnderlineFullyApplied(Editable editable, int start, int end) {
        int safeEnd = Math.min(end, editable.length());
        for (int i = start; i < safeEnd; i++) {
            if (!hasUnderlineAt(editable, i)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasUnderlineAt(Editable editable, int index) {
        UnderlineSpan[] spans = editable.getSpans(index, Math.min(index + 1, editable.length()), UnderlineSpan.class);
        for (UnderlineSpan span : spans) {
            int s = editable.getSpanStart(span);
            int e = editable.getSpanEnd(span);
            if (s <= index && e > index) {
                return true;
            }
        }
        return false;
    }

    private void removeUnderlineFromRange(Editable editable, int start, int end) {
        UnderlineSpan[] spans = editable.getSpans(start, end, UnderlineSpan.class);
        for (UnderlineSpan span : spans) {
            int s = editable.getSpanStart(span);
            int e = editable.getSpanEnd(span);
            int overlapStart = Math.max(s, start);
            int overlapEnd = Math.min(e, end);
            editable.removeSpan(span);

            if (s < overlapStart) {
                editable.setSpan(new UnderlineSpan(), s, overlapStart, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (overlapEnd < e) {
                editable.setSpan(new UnderlineSpan(), overlapEnd, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void setupAttachmentPicker() {
        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri uriFile = data.getClipData().getItemAt(i).getUri();
                                addAttachmentPreview(uriFile);
                            }
                        } else if (data.getData() != null) {
                            Uri uriFile = data.getData();
                            addAttachmentPreview(uriFile);
                        }
                    }
                }
        );

        btn_addAttachment.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("*/*");
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(pickIntent, "Select file(s)"));
        });
    }

    private void addAttachmentPreview(Uri uriFile) {
        if (uriFile == null) {
            return;
        }
        String uriKey = uriFile.toString();
        if (uploadedAttachmentByUri.containsKey(uriKey) || uploadingAttachmentUris.contains(uriKey)) {
            return;
        }
        filesUploaded.add(uriFile);
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add(Tool.getFileName(this, uriFile));
        attachmentPreview.add(new SubmissionDisplay(
                "",
                "",
                "",
                "Uploading...",
                fileNames
        ));
        int previewPosition = attachmentPreview.size() - 1;
        attachmentAdapter.notifyItemInserted(previewPosition);
        startAttachmentUpload(uriFile, previewPosition);
        updateAttachmentUploadUiState();
    }

    private void setupSendButtons() {
        btn_send.setOnClickListener(v -> {
            if (!validateRecipients()) {
                return;
            }
            if (!areAttachmentsReadyToSend()) {
                Toast.makeText(this, "Please wait for all attachments to finish uploading.", Toast.LENGTH_SHORT).show();
                return;
            }
            String subject = et_subject.getText().toString();
            String body = et_body.getText().toString();
            sendMailToSelectedRecipients(subject, body);
        });

        btn_draft.setOnClickListener(v -> {
            if (!validateRecipients()) {
                return;
            }
            if (!areAttachmentsReadyToSend()) {
                Toast.makeText(this, "Please wait for all attachments to finish uploading.", Toast.LENGTH_SHORT).show();
                return;
            }
            String subject = et_subject.getText().toString();
            String body = et_body.getText().toString();
            saveDraftForSelectedRecipients(subject, body);
        });

        updateAttachmentUploadUiState();
    }

    private boolean validateRecipients() {
        if (user == null || !Tool.boolOf(user.getUid())) {
            Toast.makeText(this, "Sender account is unavailable. Please reopen compose.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedRecipients.isEmpty()) {
            actv_to.setError("Please add at least one recipient from the list.");
            return false;
        }
        actv_to.setError(null);
        return true;
    }

    private void sendMailToSelectedRecipients(String subject, String body) {
        try {
            String oldDraftRichBodyId = getRichBodyId(editingMail);
            if (editingMail != null && Tool.boolOf(editingMail.getMailID()) && user != null && Tool.boolOf(user.getUid())) {
                removeMailIdFromDrafts(user, editingMail.getMailID());
                FirebaseDatabase.getInstance()
                        .getReference(FirebaseNode.MAIL.getPath())
                        .child(editingMail.getMailID())
                        .removeValue();
                deleteRichBodyById(oldDraftRichBodyId);
            }

            RichBody richBody = persistRichBodyFromEditor();
            ArrayList<file> attachments = getOrderedUploadedAttachments();
            for (User recipient : selectedRecipients) {
                Mail mail = new Mail(user, recipient, subject, body);
                mail.setRichBody(richBody);
                mail.setAttachments(attachments);
                mail.setDraft(false);
                User.sendMail(mail, user, recipient);
                mail.getRepositoryInstance().save(mail);
            }
            goBackToMailActivity(MailType.OUTBOX);
            Toast.makeText(this, "Mail sent.", Toast.LENGTH_SHORT).show();

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeMailIdFromDrafts(User owner, String mailId) {
        FirebaseDatabase.getInstance()
                .getReference(owner.getFirebaseNode().getPath())
                .child(owner.getUid())
                .child("draftMails")
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<String> updated = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String id = ds.getValue(String.class);
                        if (Tool.boolOf(id) && !mailId.equals(id)) {
                            updated.add(id);
                        }
                    }
                    FirebaseDatabase.getInstance()
                            .getReference(owner.getFirebaseNode().getPath())
                            .child(owner.getUid())
                            .child("draftMails")
                            .setValue(updated);
                });
    }

    private void goBackToMailActivity(MailType mailType){
        Intent intent = new Intent(ComposeMail.this, MailPageFull.class);
        intent.putExtra(User.SERIALIZE_KEY_CODE, user);
        intent.putExtra("PageToOpen", mailType);
        startActivity(intent);
        finish();
    }

    private void saveDraftForSelectedRecipients(String subject, String body) {
        try {
            RichBody richBody = persistRichBodyFromEditor();
            if (editingMail != null) {
                String oldDraftRichBodyId = getRichBodyId(editingMail);
                User recipient = selectedRecipients.isEmpty() ? null : selectedRecipients.get(0);
                editingMail.setFrom(user);
                if (recipient != null) {
                    editingMail.setTo(recipient);
                }
                editingMail.setHeader(subject);
                editingMail.setBody(body);
                editingMail.setRichBody(richBody);
                editingMail.setAttachments(getOrderedUploadedAttachments());
                editingMail.setDraft(true);
                editingMail.getRepositoryInstance().save(editingMail);
                if (Tool.boolOf(oldDraftRichBodyId) && !oldDraftRichBodyId.equals(getRichBodyId(editingMail))) {
                    deleteRichBodyById(oldDraftRichBodyId);
                }
                Toast.makeText(this, "Draft updated.", Toast.LENGTH_SHORT).show();
                goBackToMailActivity(MailType.DRAFT);
                return;
            }

            ArrayList<file> attachments = getOrderedUploadedAttachments();
            for (User recipient : selectedRecipients) {
                Mail mail = new Mail(user, recipient, subject, body);
                mail.setRichBody(richBody);
                mail.setAttachments(attachments);
                mail.setDraft(true);
                user.addDraftMail(mail);
                mail.getRepositoryInstance().save(mail);
            }
            Toast.makeText(this, "Draft saved.", Toast.LENGTH_SHORT).show();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void startAttachmentUpload(Uri uri, int previewPosition) {
        String key = uri.toString();
        uploadingAttachmentUris.add(key);
        failedAttachmentUris.remove(key);

        try {
            Tool.uploadToCloudinary(Tool.uriToFile(uri, this), new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(String object) {
                    file uploaded = new file(
                            Tool.getFileName(ComposeMail.this, uri),
                            object,
                            user,
                            LocalDateTime.now()
                    );
                    new FileRepository(uploaded.getID()).save(uploaded);
                    uploadedAttachmentByUri.put(key, uploaded);
                    uploadingAttachmentUris.remove(key);
                    failedAttachmentUris.remove(key);
                    runOnUiThread(() -> {
                        updateAttachmentPreviewStatus(previewPosition, "Uploaded");
                        updateAttachmentUploadUiState();
                    });
                }

                @Override
                public void onError(DatabaseError error) {
                    uploadingAttachmentUris.remove(key);
                    failedAttachmentUris.add(key);
                    runOnUiThread(() -> {
                        if (Tool.isConnectivityIssue(error)) {
                            updateAttachmentPreviewStatus(previewPosition, "No connection");
                            Toast.makeText(ComposeMail.this, "No internet connection. Attachment upload failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            updateAttachmentPreviewStatus(previewPosition, "Upload failed");
                        }
                        updateAttachmentUploadUiState();
                    });
                }
            });
        } catch (IOException e) {
            uploadingAttachmentUris.remove(key);
            failedAttachmentUris.add(key);
            updateAttachmentPreviewStatus(previewPosition, "Upload failed");
            updateAttachmentUploadUiState();
        }
    }

    private void updateAttachmentPreviewStatus(int position, String status) {
        if (position < 0 || position >= attachmentPreview.size()) {
            return;
        }
        SubmissionDisplay item = attachmentPreview.get(position);
        item.setSubmittedTimeDate(status);
        attachmentAdapter.notifyItemChanged(position);
    }

    private boolean areAttachmentsReadyToSend() {
        return uploadingAttachmentUris.isEmpty()
                && failedAttachmentUris.isEmpty()
                && uploadedAttachmentByUri.size() == filesUploaded.size();
    }

    private ArrayList<file> getOrderedUploadedAttachments() {
        ArrayList<file> ordered = new ArrayList<>();
        for (Uri uri : filesUploaded) {
            if (uri == null) continue;
            file uploaded = uploadedAttachmentByUri.get(uri.toString());
            if (uploaded != null) {
                ordered.add(uploaded);
            }
        }
        return ordered;
    }

    private void updateAttachmentUploadUiState() {
        boolean uploading = !uploadingAttachmentUris.isEmpty();
        boolean failed = !failedAttachmentUris.isEmpty();
        boolean ready = areAttachmentsReadyToSend();

        btn_send.setEnabled(ready);
        btn_draft.setEnabled(ready);

        if (uploading) {
            btn_send.setText("Uploading files...");
            btn_draft.setText("Uploading...");
            return;
        }
        if (failed) {
            btn_send.setText("Fix Attachments");
            btn_draft.setText("Fix Attachments");
            return;
        }
        btn_send.setText("Send Mail");
        btn_draft.setText("Draft");
    }

    private String getRichBodyId(Mail mail) {
        if (mail == null || mail.getRichBody() == null) {
            return null;
        }
        return mail.getRichBody().getID();
    }

    private void deleteRichBodyById(String richBodyId) {
        if (!Tool.boolOf(richBodyId)) {
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.RICH_BODY.getPath())
                .child(richBodyId)
                .removeValue();
    }

    private RichBody persistRichBodyFromEditor() throws IllegalAccessException, InstantiationException {
        RichBody richBody = RichBody.extractRichBody(et_body);
        if (richBody.spans != null) {
            for (TextStyleRange span : richBody.spans) {
                saveTextStyleRangeRaw(span);
            }
        }
        richBody.getRepositoryInstance().save(richBody);
        return richBody;
    }

    private void saveTextStyleRangeRaw(TextStyleRange span) {
        if (span == null || !Tool.boolOf(span.getID())) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", span.getID());
        payload.put("start", span.start);
        payload.put("end", span.end);
        payload.put("bold", span.bold);
        payload.put("italic", span.italic);
        payload.put("underline", span.underline);

        FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.TEXT_STYLE_RANGE.getPath())
                .child(span.getID())
                .setValue(payload);
    }

    private void populateComposeFromEditingMail() {
        if (editingMail == null) {
            return;
        }

        if (Tool.boolOf(editingMail.getHeader())) {
            et_subject.setText(editingMail.getHeader());
        }

        if (editingMail.getRichBody() != null) {
            RichBody.formatTv(et_body, editingMail.getRichBody());
        } else if (Tool.boolOf(editingMail.getBody())) {
            et_body.setText(editingMail.getBody());
        }

        editingMail.getTo().addOnSuccessListener(toUser -> {
            if (toUser != null && !isSelected(toUser) && !isCurrentUser(toUser)) {
                selectedRecipients.add(toUser);
                selectedAdapter.submitList(selectedRecipients);
                updateSelectedRecipientsVisibility();
            }
        });
    }

    private void preloadUsers() {
        cachedUsers.clear();
        cachedUserIds.clear();

        DatabaseReference usersRoot = FirebaseDatabase.getInstance().getReference(FirebaseNode.USER.getPath());
        AtomicInteger pending = new AtomicInteger(4);

        preloadUsersFromPath(usersRoot.child("students"), Student.class, pending);
        preloadUsersFromPath(usersRoot.child("teachers"), Teacher.class, pending);
        preloadUsersFromPath(usersRoot.child("parents"), Parent.class, pending);
        preloadUsersFromPath(usersRoot.child("admins"), Admin.class, pending);
    }

    private <T extends User> void preloadUsersFromPath(DatabaseReference ref, Class<T> modelClass, AtomicInteger pending) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    User candidate = buildUserPreview(child, modelClass);
                    if (candidate == null) {
                        continue;
                    }
                    if (Tool.boolOf(candidate.getUid()) && cachedUserIds.add(candidate.getUid())) {
                        cachedUsers.add(candidate);
                    }
                }
                finalizePreloadIfDone(pending);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                finalizePreloadIfDone(pending);
            }
        });
    }

    private void finalizePreloadIfDone(AtomicInteger pending) {
        if (pending.decrementAndGet() == 0) {
            runOnUiThread(() -> updateRecipientSuggestions(actv_to.getText() != null ? actv_to.getText().toString().trim() : ""));
        }
    }

    private void updateRecipientSuggestions(String query) {
        String normalizedPrefix = query == null ? "" : query.trim().toLowerCase();
        if(normalizedPrefix.isEmpty()){
            rvRecipientResults.setVisibility(GONE);
        }else{
            rvRecipientResults.setVisibility(VISIBLE);
        }
        ArrayList<User> filtered = new ArrayList<>();

        for (User candidate : cachedUsers) {
            if (isSelected(candidate)) {
                continue;
            }
            if (isCurrentUser(candidate)) {
                continue;
            }
            if (!Tool.boolOf(normalizedPrefix) || matchesPrefix(candidate, normalizedPrefix)) {
                filtered.add(candidate);
            }
            if (filtered.size() >= 20) {
                break;
            }
        }

        searchAdapter.submitList(filtered);
    }

    private void addRecipient(User recipient) {
        if (recipient == null || !Tool.boolOf(recipient.getUid()) || isSelected(recipient) || isCurrentUser(recipient)) {
            return;
        }
        selectedRecipients.add(recipient);
        selectedAdapter.submitList(selectedRecipients);
        updateSelectedRecipientsVisibility();
        actv_to.setText("");
        updateRecipientSuggestions("");
    }

    private void removeRecipient(User recipient) {
        if (recipient == null || !Tool.boolOf(recipient.getUid())) {
            return;
        }
        selectedRecipients.removeIf(user -> user != null && Tool.boolOf(user.getUid()) && recipient.getUid().equals(user.getUid()));
        selectedAdapter.submitList(selectedRecipients);
        updateSelectedRecipientsVisibility();
        updateRecipientSuggestions(actv_to.getText() != null ? actv_to.getText().toString().trim() : "");
    }

    private boolean isSelected(User candidate) {
        if (candidate == null || !Tool.boolOf(candidate.getUid())) {
            return false;
        }
        for (User selected : selectedRecipients) {
            if (selected != null && Tool.boolOf(selected.getUid()) && selected.getUid().equals(candidate.getUid())) {
                return true;
            }
        }
        return false;
    }

    private User buildUserPreview(DataSnapshot child, Class<? extends User> modelClass) {
        User candidate;
        if (modelClass == Student.class) {
            candidate = new Student();
        } else if (modelClass == Teacher.class) {
            candidate = new Teacher();
        } else if (modelClass == Parent.class) {
            candidate = new Parent();
        } else if (modelClass == Admin.class) {
            candidate = new Admin();
        } else {
            return null;
        }

        String uid = child.child("uid").getValue(String.class);
        if (!Tool.boolOf(uid)) {
            uid = child.getKey();
        }
        candidate.setUid(uid);

        String fullName = child.child("fullName").getValue(String.class);
        candidate.setFullName(Tool.boolOf(fullName) ? fullName : "");

        String email = child.child("email").getValue(String.class);
        candidate.setEmail(Tool.boolOf(email) ? email : "");

        return candidate;
    }

    private boolean matchesPrefix(User candidate, String normalizedPrefix) {
        String fullName = candidate.getFullName() != null ? candidate.getFullName().toLowerCase() : "";
        String email = candidate.getEmail() != null ? candidate.getEmail().toLowerCase() : "";
        return fullName.startsWith(normalizedPrefix) || email.startsWith(normalizedPrefix);
    }

    private boolean isCurrentUser(User candidate) {
        if (candidate == null || user == null) {
            return false;
        }
        String candidateUid = candidate.getUid();
        String currentUid = user.getUid();
        if (Tool.boolOf(candidateUid) && Tool.boolOf(currentUid) && candidateUid.equals(currentUid)) {
            return true;
        }

        String candidateEmail = candidate.getEmail();
        String currentEmail = user.getEmail();
        return Tool.boolOf(candidateEmail)
                && Tool.boolOf(currentEmail)
                && candidateEmail.equalsIgnoreCase(currentEmail);
    }

    private void updateSelectedRecipientsVisibility() {
        if (rvSelectedRecipients == null || llNoRecipientsSelected == null) {
            return;
        }
        boolean hasSelectedRecipients = !selectedRecipients.isEmpty();
        rvSelectedRecipients.setVisibility(hasSelectedRecipients ? VISIBLE : GONE);
        llNoRecipientsSelected.setVisibility(hasSelectedRecipients ? GONE : VISIBLE);
    }
}
