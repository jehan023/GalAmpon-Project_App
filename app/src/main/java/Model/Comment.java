package Model;

public class Comment {

    private int id;
    private String comment;
    private String publisher;
    private String datetime;

    public Comment() {
    }

    public Comment(int id, String comment, String publisher, String datetime) {
        this.id = id;
        this.comment = comment;
        this.publisher = publisher;
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDatetime() { return datetime; }

    public void setDatetime(String datetime) { this.datetime = datetime; }
}
