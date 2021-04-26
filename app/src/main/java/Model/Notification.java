package Model;

public class Notification {
    private String userid;
    private String text;
    private String postid;
    private String datetime;
    private boolean isPost;

    public Notification() {
    }

    public Notification(String userid, String text, String postid, String datetime, boolean isPost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.datetime = datetime;
        this.isPost = isPost;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getDatetime() { return datetime; }

    public void setDatetime(String datetime) { this.datetime = datetime; }

    public boolean isIsPost() {
        return isPost;
    }

    public void setIsPost(boolean post) {
        isPost = post;
    }
}
