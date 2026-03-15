package dev.m7med.questsystem.data.model;

import org.bson.Document;

/**
 * Represents the progress of a single ongoing quest for a player.
 */
public class QuestProgress {

    /**
     * The current progress amount towards the quest's required goal.
     */
    private int progress;

    /**
     * Constructs a new QuestProgress instance with the specified initial progress.
     *
     * @param progress The initial progress amount.
     */
    public QuestProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Gets the current progress amount.
     *
     * @return The current progress.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the current progress amount to a specific value.
     *
     * @param progress The new progress amount.
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * Adds the specified amount to the current progress.
     *
     * @param amount The amount to add.
     */
    public void addProgress(int amount) {
        this.progress += amount;
    }

    /**
     * Serializes this QuestProgress instance into a MongoDB Document.
     *
     * @return A Document representing this progress.
     */
    public Document toDocument() {
        return new Document("progress", progress);
    }

    /**
     * Deserializes a MongoDB Document into a QuestProgress instance.
     *
     * @param doc The Document to deserialize. Can be null.
     * @return A new QuestProgress instance with the data from the Document, or 0 if
     *         null.
     */
    public static QuestProgress fromDocument(Document doc) {
        if (doc == null)
            return new QuestProgress(0);
        return new QuestProgress(doc.getInteger("progress", 0));
    }
}
