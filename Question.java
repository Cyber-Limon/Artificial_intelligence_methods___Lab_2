public class Question {
    private final long id;
    private final long difficulty;
    private final String topic;

    public Question(long id, long difficulty, String topic) {
        this.id = id;
        this.difficulty = difficulty;
        this.topic = topic;
    }

    public Question(String str) {
        String[] fields = str.split(",");
        this.id = Long.parseLong(fields[0]);
        this.difficulty = Long.parseLong(fields[1]);
        this.topic = fields[2];
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
        return id + "," + difficulty + "," + topic;
    }
}
