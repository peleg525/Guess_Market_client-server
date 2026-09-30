package gm.engine.dto;

/**
 * Result of uploading one events file in Exercise 3: since files accumulate (they no longer
 * replace what was loaded before), this reports both how many events this particular upload added
 * and how many events the whole system now holds in total.
 */
public class LoadResultDto {

    private final int addedEventCount;
    private final int totalEventCount;

    public LoadResultDto(int addedEventCount, int totalEventCount) {
        this.addedEventCount = addedEventCount;
        this.totalEventCount = totalEventCount;
    }

    public int getAddedEventCount() {
        return addedEventCount;
    }

    public int getTotalEventCount() {
        return totalEventCount;
    }
}
