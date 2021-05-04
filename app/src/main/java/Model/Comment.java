package Model;

public class Comment {

    private String commentid;
    private String comment;
    private String publisher;
    private String datetime;

    public Comment() {
    }

    public Comment(String commentid, String comment, String publisher, String datetime) {
        this.commentid = commentid;
        this.comment = comment;
        this.publisher = publisher;
        this.datetime = datetime;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
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
