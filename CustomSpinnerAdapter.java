package com.example.projekt_zaliczeniowy_sklep;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projekt_zaliczeniowy_sklep.Product;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<Product> {
    private Context context;
    private List<Product> products;

    public CustomSpinnerAdapter(Context context, List<Product> products) {
        super(context, R.layout.spinner_item, products);
        this.context = context;
        this.products = products;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        Product product = products.get(position);

        ImageView imageView = convertView.findViewById(R.id.item_image);
        TextView textView = convertView.findViewById(R.id.item_text);

        imageView.setImageResource(product.getImageResId());
        textView.setText(product.getName());

        return convertView;
    }
}

