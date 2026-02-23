package com.example.turgo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.MutableData;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MailSmallAdapter extends RecyclerView.Adapter<MailSmallViewHolder> {
    private String userID;
    private ArrayList<Mail>mailSelected;
    private ArrayList<Mail>mails;
    private boolean selectMode;
    private boolean editable;
    private MailPageFull activity;
    private User currentUser;
    private MailType mailType;
    private final HashSet<String> selectedMailIds = new HashSet<>();

    public MailSmallAdapter(String userID, ArrayList<Mail>mails, boolean editable,  MailPageFull activity, User currentUser, MailType mailType){
        this.userID = userID;
        this.mails = mails;
        this.mailSelected = new ArrayList<>();
        this.editable = editable;
        this.activity = activity;
        this.currentUser = currentUser;
        this.mailType = mailType;
    }
    public MailSmallAdapter(String userID, ArrayList<Mail>mails, boolean editable) {
        this.userID = userID;
        this.mails = mails;
        this.mailSelected = new ArrayList<>();
        this.editable = editable;
    }
    @NonNull
    @Override
    public MailSmallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mail_small_display, parent, false);
        return new MailSmallViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MailSmallViewHolder holder, int position) {
        Mail mail = mails.get(position);
        holder.mail = mail;
        holder.tv_titleHeading.setText(Tool.boolOf(mail.getHeader()) ? mail.getHeader() : "(No Subject)");

        String preview = Tool.boolOf(mail.getBody()) ? mail.getBody().trim() : "";
        if (preview.length() > 30) {
            preview = preview.substring(0, 30) + "...";
        }
        holder.tv_shortPreview.setText(Tool.boolOf(preview) ? preview : "(No preview)");

        holder.tv_fromUser.setText("-");
        boolean senderIsCurrent = Tool.boolOf(mail.getFromId()) && mail.getFromId().equals(userID);
        if (senderIsCurrent) {
            mail.getTo().addOnSuccessListener(counterpart -> {
                if (holder.mail != null && mail.getMailID().equals(holder.mail.getMailID()) && counterpart != null) {
                    if (Tool.boolOf(counterpart.getFullName())) {
                        holder.tv_fromUser.setText(counterpart.getFullName());
                    } else if (Tool.boolOf(counterpart.getEmail())) {
                        holder.tv_fromUser.setText(counterpart.getEmail());
                    }
                }
            });
        } else {
            mail.getFrom().addOnSuccessListener(counterpart -> {
                if (holder.mail != null && mail.getMailID().equals(holder.mail.getMailID()) && counterpart != null) {
                    if (Tool.boolOf(counterpart.getFullName())) {
                        holder.tv_fromUser.setText(counterpart.getFullName());
                    } else if (Tool.boolOf(counterpart.getEmail())) {
                        holder.tv_fromUser.setText(counterpart.getEmail());
                    }
                }
            });
        }

        boolean isSelected = selectedMailIds.contains(mail.getMailID());
        holder.setSelectedVisual(isSelected);

        holder.ll_mailPreview.setOnClickListener(v -> {
            if (selectMode) {
                toggleSelection(mail);
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(adapterPosition);
                }
                return;
            }

            if (editable) {
                Intent intent = new Intent(holder.itemView.getContext(), ComposeMail.class);
                intent.putExtra(User.SERIALIZE_KEY_CODE, currentUser);
                intent.putExtra(Mail.SERIALIZE_KEY_CODE, mail);
                holder.itemView.getContext().startActivity(intent);
            } else {
                Intent intent = new Intent(holder.itemView.getContext(), MailExpandFull.class);
                intent.putExtra(Mail.SERIALIZE_KEY_CODE, mail);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.ll_mailPreview.setOnLongClickListener(v -> {
            if (!selectMode) {
                setSelectMode(true);
                if (activity != null) {
                    activity.setSelectionMode(true);
                }
            }
            toggleSelection(mail);
            notifyDataSetChanged();
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return mails.size();
    }

    public MailPageFull getActivity() {
        return activity;
    }

    public void setActivity(MailPageFull activity) {
        this.activity = activity;
    }

    public ArrayList<Mail> getMailSelected() {
        return mailSelected;
    }

    public void setMailSelected(ArrayList<Mail> mailSelected) {
        this.mailSelected = mailSelected;
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }
    @SuppressLint("NotifyDataSetChanged")
    public void exitSelectMode(){
        selectMode = false;
        mailSelected.clear();
        selectedMailIds.clear();
        notifyDataSetChanged();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void deleteSelectedMails(){
        ArrayList<Mail> toDelete = new ArrayList<>(mailSelected);
        String mailboxField = mailboxFieldFor(mailType);

        for(Mail mail: toDelete){
            mails.remove(mail);
            if (currentUser != null && Tool.boolOf(currentUser.getUid())) {
                removeMailIdFromMailbox(mailboxField, mail.getMailID());
            }

            if (mailType == MailType.DRAFT && Tool.boolOf(mail.getMailID())) {
                FirebaseDatabase.getInstance()
                        .getReference(FirebaseNode.MAIL.getPath())
                        .child(mail.getMailID())
                        .removeValue();
            }
        }
        mailSelected.clear();
        selectedMailIds.clear();
        selectMode = false;
        if (activity != null) {
            activity.setSelectionMode(false);
        }
        notifyDataSetChanged();
    }

    public String getUserID() {
        return userID;
    }


    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<Mail> getMails() {
        return mails;
    }

    public void setMails(ArrayList<Mail> mails) {
        this.mails = mails;
    }


    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void addMail(Mail mail) {
        mails.add(mail);
        notifyItemInserted(mails.size() - 1);
    }

    private void toggleSelection(Mail mail) {
        if (mail == null || !Tool.boolOf(mail.getMailID())) return;
        String id = mail.getMailID();
        if (selectedMailIds.contains(id)) {
            selectedMailIds.remove(id);
            mailSelected.removeIf(m -> m != null && id.equals(m.getMailID()));
        } else {
            selectedMailIds.add(id);
            mailSelected.add(mail);
        }
        if (selectedMailIds.isEmpty() && activity != null && selectMode) {
            activity.setSelectionMode(false);
            selectMode = false;
        }
    }

    private String mailboxFieldFor(MailType type) {
        if (type == MailType.OUTBOX) return "outbox";
        if (type == MailType.DRAFT) return "draftMails";
        return "inbox";
    }

    private void removeMailIdFromMailbox(String mailboxField, String mailId) {
        FirebaseDatabase.getInstance()
                .getReference(currentUser.getFirebaseNode().getPath())
                .child(currentUser.getUid())
                .child(mailboxField)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        Object raw = currentData.getValue();
                        ArrayList<String> next = new ArrayList<>();
                        if (raw instanceof List) {
                            for (Object item : (List<?>) raw) {
                                if (item instanceof String && !mailId.equals(item)) {
                                    next.add((String) item);
                                }
                            }
                        }
                        currentData.setValue(next);
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(com.google.firebase.database.DatabaseError error, boolean committed, com.google.firebase.database.DataSnapshot currentData) {
                    }
                });
    }

}
