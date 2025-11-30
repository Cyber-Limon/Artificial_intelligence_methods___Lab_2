public class Question {
    private final long id;
    private final long difficulty;
    private final String topic;

    public Question(long id, long difficulty, String topic) {
        this.id = id;
        this.difficulty = difficulty;
        this.topic = topic;
    }

    public long getId() {
        return id;
    }

    public long getDifficulty() {
        return difficulty;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "Question{ id=" + id + ", difficulty=" + difficulty + ", topic=" + topic + "}";
    }
}
