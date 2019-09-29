package com.example.allergyalert;

public class FavouriteItem {

    private String image;
    private String product_name;

    public FavouriteItem()
    {

    }

    public FavouriteItem(String image, String product_name) {
        this.image = image;
        this.product_name = product_name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }
}
