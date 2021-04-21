package br.com.kamarugosan.songlist.storage;

public class DeletionAttempt {
    private final int totalToDelete;
    private int successfulDeletions = 0;
    private int failedDeletions = 0;

    public DeletionAttempt(int totalToDelete) {
        this.totalToDelete = totalToDelete;
    }

    public void addSuccessfulDeletion() {
        validateAddition();
        successfulDeletions++;
    }

    public void addFailedDeletion() {
        validateAddition();
        failedDeletions++;
    }

    public boolean isSuccessful() {
        return successfulDeletions == totalToDelete;
    }

    public int getFailedDeletions() {
        return failedDeletions;
    }

    private void validateAddition() {
        if (successfulDeletions + failedDeletions == totalToDelete) {
            throw new RuntimeException("Cannot exceed the total amount");
        }
    }
}
