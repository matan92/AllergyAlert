package com.example.allergyalert;

public class ReviewItem {

    private String userId;
    private String username;
    private Long rating;
    private String date;
    private String feedback;
    private String product_name;

    public ReviewItem()
    {

    }

    public ReviewItem(String userId, String username,Long rating, String date, String product_name, String feedback) {
        this.userId= userId;
        this.username = username;
        this.rating = rating;
        this.date = date;
        this.product_name= product_name;
        this.feedback = feedback;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }


}
