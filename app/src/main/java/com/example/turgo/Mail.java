package com.example.turgo;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Mail implements RequireUpdate<Mail, MailFirebase, MailRepository>, Serializable {
    private final String mailID;
    private final FirebaseNode fbn = FirebaseNode.MAIL;
    public static final String SERIALIZE_KEY_CODE = "mailObj";

    private String from;
    private String to;
    private String fromType;
    private String toType;
    private LocalDateTime timeSent;
    private ArrayList<String> attachments;
    private LocalDateTime timeOpened;
    private String header;
    private String body;
    private RichBody richBody;
    private boolean draft;
    private boolean opened;
    private transient User fromUserCache;
    private transient User toUserCache;
    private transient ArrayList<file> attachmentCache;

    public Mail() {
        this.mailID = UUID.randomUUID().toString();
        this.attachments = new ArrayList<>();
        this.attachmentCache = new ArrayList<>();
    }

    public Mail(User from, User to, String header, String body) {
        this.mailID = UUID.randomUUID().toString();
        setFrom(from);
        setTo(to);
        this.timeSent = LocalDateTime.now();
        this.header = header;
        this.body = body;
        this.opened = false;
        this.attachments = new ArrayList<>();
        this.attachmentCache = new ArrayList<>();
    }

    public Mail(User from, User to) {
        this.mailID = UUID.randomUUID().toString();
        setFrom(from);
        setTo(to);
        this.timeSent = LocalDateTime.now();
        this.attachments = new ArrayList<>();
        this.attachmentCache = new ArrayList<>();
    }

    public User getToUserCache() {
        return toUserCache;
    }

    public void setToUserCache(User toUserCache) {
        this.toUserCache = toUserCache;
    }

    public User getFromUserCache() {
        return fromUserCache;
    }

    public void setFromUserCache(User fromUserCache) {
        this.fromUserCache = fromUserCache;
    }

    public ArrayList<String> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public ArrayList<String> getAttachmentIds() {
        return getAttachments();
    }

    public void setAttachmentIds(ArrayList<String> attachmentIds) {
        this.attachments = attachmentIds != null ? attachmentIds : new ArrayList<>();
        this.attachmentCache = new ArrayList<>();
    }

    public void setAttachments(ArrayList<file> files) {
        this.attachments = new ArrayList<>();
        this.attachmentCache = new ArrayList<>();
        if (files == null) {
            return;
        }
        for (file attachment : files) {
            if (attachment == null || !Tool.boolOf(attachment.getID())) {
                continue;
            }
            this.attachments.add(attachment.getID());
            this.attachmentCache.add(attachment);
            attachment.getRepositoryInstance().save(attachment);
        }
    }

    public Task<ArrayList<file>> loadAttachmentFiles() {
        if (attachments == null || attachments.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        ArrayList<Task<file>> tasks = new ArrayList<>();
        for (String fileId : attachments) {
            if (Tool.boolOf(fileId)) {
                tasks.add(new FileRepository(fileId).loadAsNormal());
            }
        }

        if (tasks.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        TaskCompletionSource<ArrayList<file>> tcs = new TaskCompletionSource<>();
        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    ArrayList<file> loaded = new ArrayList<>();
                    for (Object result : results) {
                        if (result instanceof file) {
                            file loadedFile = (file) result;
                            if (loadedFile != null) {
                                loaded.add(loadedFile);
                            }
                        }
                    }
                    attachmentCache = loaded;
                    tcs.setResult(loaded);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.MAIL;
    }

    @Override
    public Class<MailFirebase> getFirebaseClass() {
        return MailFirebase.class;
    }

    @Override
    public String getID() {
        return mailID;
    }

    @Override
    public Class<MailRepository> getRepositoryClass() {
        return MailRepository.class;
    }

    public String getMailID() {
        return mailID;
    }

    public FirebaseNode getFbn() {
        return fbn;
    }

    public Task<User> getFrom() {
        if (fromUserCache != null) {
            return com.google.android.gms.tasks.Tasks.forResult(fromUserCache);
        }
        return resolveUserById(from, fromType);
    }

    public void setFrom(User from) {
        if (from == null) {
            this.from = null;
            this.fromUserCache = null;
            this.fromType = null;
            return;
        }
        this.from = from.getUid();
        this.fromUserCache = from;
        if (from != null && from.getUserType() != null) {
            this.fromType = from.getUserType().name();
        }
    }

    public void setFrom(String from) {
        this.from = from;
        this.fromUserCache = null;
    }

    public String getFromId() {
        return from;
    }

    public User getFromCached() {
        return fromUserCache;
    }

    public Task<User> getTo() {
        if (toUserCache != null) {
            return com.google.android.gms.tasks.Tasks.forResult(toUserCache);
        }
        return resolveUserById(to, toType);
    }

    public void setTo(User to) {
        if (to == null) {
            this.to = null;
            this.toUserCache = null;
            this.toType = null;
            return;
        }
        this.to = to.getUid();
        this.toUserCache = to;
        if (to != null && to.getUserType() != null) {
            this.toType = to.getUserType().name();
        }
    }

    public void setTo(String to) {
        this.to = to;
        this.toUserCache = null;
    }

    public String getToId() {
        return to;
    }

    public User getToCached() {
        return toUserCache;
    }

    public String getFromType() {
        return fromType;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public String getToType() {
        return toType;
    }

    public void setToType(String toType) {
        this.toType = toType;
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(LocalDateTime timeSent) {
        this.timeSent = timeSent;
    }

    public LocalDateTime getTimeOpened() {
        return timeOpened;
    }

    public void setTimeOpened(LocalDateTime timeOpened) {
        this.timeOpened = timeOpened;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public RichBody getRichBody() {
        return richBody;
    }

    public void setRichBody(RichBody richBody) {
        this.richBody = richBody;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public String getPreview() {
        if (!Tool.boolOf(body)) return "";
        return body.length() > 30 ? body.substring(0, 30) : body;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    private Task<User> resolveUserById(String userId, String typeHint) {
        TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(userId)) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        Task<User> hinted = loadUserByType(userId, typeHint);
        if (hinted != null) {
            hinted.addOnSuccessListener(user -> {
                if (user != null) {
                    cacheResolvedUser(user);
                    tcs.setResult(user);
                    return;
                }
                loadUserByRepositoryFallback(userId, tcs);
            }).addOnFailureListener(e -> loadUserByRepositoryFallback(userId, tcs));
            return tcs.getTask();
        }

        loadUserByRepositoryFallback(userId, tcs);
        return tcs.getTask();
    }

    private void loadUserByRepositoryFallback(String userId, TaskCompletionSource<User> tcs) {
        Tasks.whenAllSuccess(
                        new StudentRepository(userId).loadAsNormal(),
                        new TeacherRepository(userId).loadAsNormal(),
                        new ParentRepository(userId).loadAsNormal(),
                        new AdminRepository(userId).loadAsNormal()
                )
                .addOnSuccessListener(results -> {
                    User resolved = null;
                    for (Object object : results) {
                        if (object instanceof User) {
                            resolved = (User) object;
                            if (resolved != null) {
                                break;
                            }
                        }
                    }
                    if (resolved != null) {
                        cacheResolvedUser(resolved);
                    }
                    tcs.setResult(resolved);
                })
                .addOnFailureListener(tcs::setException);
    }

    private Task<User> loadUserByType(String userId, String typeHint) {
        if (!Tool.boolOf(typeHint)) {
            return null;
        }
        String normalized = typeHint.trim();
        if ("STUDENT".equalsIgnoreCase(normalized) || "Student".equalsIgnoreCase(normalized)) {
            return new StudentRepository(userId).loadAsNormal().continueWith(task -> task.getResult());
        }
        if ("TEACHER".equalsIgnoreCase(normalized) || "Teacher".equalsIgnoreCase(normalized)) {
            return new TeacherRepository(userId).loadAsNormal().continueWith(task -> task.getResult());
        }
        if ("PARENT".equalsIgnoreCase(normalized) || "Parent".equalsIgnoreCase(normalized)) {
            return new ParentRepository(userId).loadAsNormal().continueWith(task -> task.getResult());
        }
        if ("ADMIN".equalsIgnoreCase(normalized) || "Admin".equalsIgnoreCase(normalized)) {
            return new AdminRepository(userId).loadAsNormal().continueWith(task -> task.getResult());
        }
        return null;
    }

    private void cacheResolvedUser(User user) {
        if (user == null || !Tool.boolOf(user.getUid())) {
            return;
        }
        if (user.getUid().equals(from)) {
            fromUserCache = user;
        }
        if (user.getUid().equals(to)) {
            toUserCache = user;
        }
    }
}
