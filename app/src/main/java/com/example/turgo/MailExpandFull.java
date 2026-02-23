package com.example.turgo;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.turgo.databinding.ActivityMailExpandFullBinding;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.ZoneId;
import java.util.ArrayList;

public class MailExpandFull extends AppCompatActivity {
    ActivityMailExpandFullBinding binding;
    ImageButton btn_back;
    ShapeableImageView siv_userProfilePicture;
    TextView tv_header, tv_body, tv_fromName, tv_timeAgo, tv_attachmentHeader;
    RecyclerView rv_attachments;

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMailExpandFullBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Mail mail = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mail = getIntent().getSerializableExtra(Mail.SERIALIZE_KEY_CODE, Mail.class);
        } else {
            Object extra = getIntent().getSerializableExtra(Mail.SERIALIZE_KEY_CODE);
            if (extra instanceof Mail) {
                mail = (Mail) extra;
            }
        }
        if(mail == null){
            Log.d("MailExpandFull", "mail is null");
            return;
        }
        btn_back = findViewById(R.id.ib_MEF_Back);
        tv_header = findViewById(R.id.tv_MEF_FullMailHeader);
        tv_body = findViewById(R.id.tv_MEF_FullMailBody);
        tv_fromName = findViewById(R.id.tv_MEF_SenderNameLarge);
        tv_timeAgo =  findViewById(R.id.tv_MEF_MailSentTime);
        tv_attachmentHeader = findViewById(R.id.tv_MEF_AttachmentLabel);

        rv_attachments = findViewById(R.id.rv_MEF_Attachments);

        tv_attachmentHeader.setText("FILES ATTACHED (" + mail.getAttachments().size() + ")");
        ArrayList<SubmissionDisplay> submissionDisplays = new ArrayList<>();
        SubmissionAdapter submissionAdapter = new SubmissionAdapter(submissionDisplays, SubmissionItemMode.FILE_PICKER);
        rv_attachments.setLayoutManager(new LinearLayoutManager(this));
        rv_attachments.setAdapter(submissionAdapter);

        mail.loadAttachmentFiles().addOnSuccessListener(files -> {
            submissionDisplays.clear();
            for (file attachment : files) {
                if (attachment == null) continue;
                ArrayList<String> fileNames = new ArrayList<>();
                fileNames.add(attachment.getFileName());
                submissionDisplays.add(new SubmissionDisplay(
                        "",
                        "",
                        "",
                        attachment.getFileCreateDate() != null ? attachment.getFileCreateDate().toString() : "",
                        fileNames
                ));
            }
            tv_attachmentHeader.setText("FILES ATTACHED (" + submissionDisplays.size() + ")");
            submissionAdapter.notifyDataSetChanged();
        });

        btn_back.setOnClickListener(v-> getOnBackPressedDispatcher().onBackPressed());

        tv_header.setText(mail.getHeader());

        if (mail.getRichBody() != null) {
            RichBody.formatTv(tv_body, mail.getRichBody());
        } else {
            tv_body.setText(mail.getBody());
        }

        mail.getFrom().addOnSuccessListener(fromUser -> {
            if (fromUser == null) return;
            if (Tool.boolOf(fromUser.getFullName())) {
                tv_fromName.setText(fromUser.getFullName());
            } else {
                tv_fromName.setText(fromUser.getEmail());
            }
        });

        if (mail.getTimeSent() != null) {
            long sentMillis = mail.getTimeSent()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    sentMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            tv_timeAgo.setText(relative);
        } else {
            tv_timeAgo.setText("");
        }
    }
}
