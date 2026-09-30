package gm.engine.dto;

/** Bonus: one message in the system-wide chat room. */
public class ChatMessageDto {

    private final long sequence;
    private final String username;
    private final String text;
    private final long sentAtEpochMillis;

    public ChatMessageDto(long sequence, String username, String text, long sentAtEpochMillis) {
        this.sequence = sequence;
        this.username = username;
        this.text = text;
        this.sentAtEpochMillis = sentAtEpochMillis;
    }

    public long getSequence() {
        return sequence;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public long getSentAtEpochMillis() {
        return sentAtEpochMillis;
    }
}
