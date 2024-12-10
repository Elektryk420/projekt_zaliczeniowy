package com.example.projekt_zaliczeniowy_sklep;

import java.util.Date;

public class Order {
    private String name;
    private double price;
    private Date date;

    public Order(String name, double price, Date date) {
        this.name = name;
        this.price = price;
        this.date = date;
    }

    public String getName() {
        return name; // Dodanie metody getName()
    }

    public double getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return name + "\nCena: " + String.format("%.2f zł", price) + "\nData: " + date.toString();
    }
}

