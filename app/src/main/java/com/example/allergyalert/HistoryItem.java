package com.example.allergyalert;

public class HistoryItem
{
    private String product_name;
    private String date;
    private String image;

    public HistoryItem()
    {

    }

    public HistoryItem(String date, String image, String product_name) {
        this.product_name = product_name;
        this.date = date;
        this.image = image;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
