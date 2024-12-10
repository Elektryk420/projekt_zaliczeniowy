package com.example.projekt_zaliczeniowy_sklep;

public class Product {
    private String name;
    private double price;
    private int imageResId; // ID zasobu obrazu

    public Product(String name, double price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

    @Override
    public String toString() {
        return name; // Używane do wyświetlania w spinnerze
    }
}

