package Model;

public class Post {
    private String description;
    private String imageurl;
    private String postid;
    private String publisher;
    private String date;
    private String postlocation;
    private Double postlatitude;
    private Double postlongitude;

    // insert Location

    public Post() {
    }

    public Post(String description, String imageurl, String postid, String publisher, String date, String postlocation, Double postlatitude, Double postlongitude) {
        this.description = description;
        this.imageurl = imageurl;
        this.postid = postid;
        this.publisher = publisher;
        this.date = date;
        this.postlocation = postlocation;
        this.postlatitude = postlatitude;
        this.postlongitude = postlongitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) { this.date = date; }

    public String getPostlocation() { return postlocation; }

    public void setPostlocation(String postlocation) { this.postlocation = postlocation; }

    public Double getPostlatitude() { return postlatitude; }

    public void setPostlatitude(Double postlatitude) { this.postlatitude = postlatitude; }

    public Double getPostlongitude() { return postlongitude; }

    public void setPostlongitude(Double postlongitude) { this.postlongitude = postlongitude; }

}
