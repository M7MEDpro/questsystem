package dev.m7med.questsystem.data.model;

import org.bson.Document;

public class QuestProgress {
    private double progress;
    public QuestProgress(double progress) {
        this.progress = progress;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void addProgress(double amount) {
        this.progress += amount;
    }

    public Document toDocument() {
        return new Document("progress", progress);
    }

    public static QuestProgress fromDocument(Document doc) {
        if (doc == null) return new QuestProgress(0.0);
        Number val = doc.get("progress", Number.class);
        return new QuestProgress(val != null ? val.doubleValue() : 0.0);
    }
}