package com.example.projekt_zaliczeniowy_sklep;



import android.content.Intent;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;



import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Database database;
    private List<Product> spinner1Products, spinner2Products, spinner3Products;
    private Map<String, Product> selectedProducts = new HashMap<>();
    private TextView wynik;
    private List<Order> orderList = new ArrayList<>();
    private EditText editTextName;
    private EditText editTextPhone;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private String savedUserName = "Nieznany użytkownik";

    private String savedPhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        database = new Database(this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        wynik = findViewById(R.id.wynik);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);


        initializeOrderList();


        CheckBox checkboxKomp = findViewById(R.id.checkbox_komp);
        Spinner spinner1 = findViewById(R.id.spinner1);
        spinner1Products = createSpinner1Products();
        setupSpinner(spinner1, spinner1Products, "Komp");

        checkboxKomp.setOnClickListener(v -> handleCheckboxClick(checkboxKomp, spinner1, "Komp"));


        CheckBox checkboxMysz = findViewById(R.id.checkbox_mysz);
        Spinner spinner2 = findViewById(R.id.spinner2);
        spinner2Products = createSpinner2Products();
        setupSpinner(spinner2, spinner2Products, "Mysz");

        checkboxMysz.setOnClickListener(v -> handleCheckboxClick(checkboxMysz, spinner2, "Mysz"));


        CheckBox checkboxKlawiatura = findViewById(R.id.checkbox_klawiatura);
        Spinner spinner3 = findViewById(R.id.spinner3);
        spinner3Products = createSpinner3Products();
        setupSpinner(spinner3, spinner3Products, "Klawiatura");

        checkboxKlawiatura.setOnClickListener(v -> handleCheckboxClick(checkboxKlawiatura, spinner3, "Klawiatura"));

        CheckBox checkboxMonitorExtra = findViewById(R.id.checkbox_monitor_extra);
        Spinner spinnerMonitorExtra = findViewById(R.id.spinner4);
        List<Product> monitorExtraProducts = createSpinnerMonitorExtraProducts();
        setupSpinner(spinnerMonitorExtra, monitorExtraProducts, "MonitorExtra");

        checkboxMonitorExtra.setOnClickListener(v -> handleCheckboxClick(checkboxMonitorExtra, spinnerMonitorExtra, "MonitorExtra"));



        Button submitOrderButton = findViewById(R.id.guzik);
        submitOrderButton.setOnClickListener(v -> submitOrder());
    }

    private void setupSpinner(Spinner spinner, List<Product> productList, String key) {
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, productList);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = productList.get(position);
                selectedProducts.put(key, selectedProduct);
                updateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProducts.remove(key);
                updateTotalPrice();
            }
        });
    }


    private void handleCheckboxClick(CheckBox checkbox, Spinner spinner, String key) {
        if (checkbox.isChecked()) {
            Product selectedProduct = (Product) spinner.getSelectedItem();
            if (selectedProduct != null) {
                selectedProducts.put(key, selectedProduct);
                Toast.makeText(this, "Dodano: " + selectedProduct.getName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            selectedProducts.remove(key);
            Toast.makeText(this, "Usunięto produkt z kategorii: " + key, Toast.LENGTH_SHORT).show();
        }
        updateTotalPrice();
    }

    private void submitOrder() {
        String userName = editTextName.getText().toString().trim();
        String phoneNumber = editTextPhone.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, "Proszę podać swoje imię przed złożeniem zamówienia!", Toast.LENGTH_SHORT).show();
            return;
        }


        savedUserName = userName;
        savedPhoneNumber = phoneNumber;

        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Nie dodano żadnych produktów do zamówienia.", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalPrice = 0.0;
        for (Product product : selectedProducts.values()) {
            totalPrice += product.getPrice();
        }

        for (Product product : selectedProducts.values()) {
            database.addProductToOrder(product);
        }

        orderList.clear();
        for (Product product : selectedProducts.values()) {
            orderList.add(new Order(product.getName(), product.getPrice(), new Date()));
        }

        Toast.makeText(this, "Zamówienie zapisane w bazie danych.", Toast.LENGTH_SHORT).show();


        updateTotalPrice();
        resetOrder();
    }


    private void resetOrder() {

        selectedProducts.clear();

        wynik.setText("Koszt za zamówienie: 0.00 zł");


        Spinner spinner1 = findViewById(R.id.spinner1);
        Spinner spinner2 = findViewById(R.id.spinner2);
        Spinner spinner3 = findViewById(R.id.spinner3);
        Spinner spinnerMonitorExtra = findViewById(R.id.spinner4);

        CheckBox checkboxKomp = findViewById(R.id.checkbox_komp);
        CheckBox checkboxMysz = findViewById(R.id.checkbox_mysz);
        CheckBox checkboxKlawiatura = findViewById(R.id.checkbox_klawiatura);
        CheckBox checkboxMonitorExtra = findViewById(R.id.checkbox_monitor_extra);

        checkboxKomp.setChecked(false);
        checkboxMysz.setChecked(false);
        checkboxKlawiatura.setChecked(false);
        checkboxMonitorExtra.setChecked(false);

        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextPhone = findViewById(R.id.editTextPhone);
        editTextName.setText("");
        editTextPhone.setText("");

        spinner1.setSelection(0);
        spinner2.setSelection(0);
        spinner3.setSelection(0);
        spinnerMonitorExtra.setSelection(0);

        Toast.makeText(this, "Zamówienie zostało zresetowane.", Toast.LENGTH_SHORT).show();
    }





    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View rootView = findViewById(android.R.id.content);
        setUpKeyboardDismiss(rootView);
        setUpKeyboardDismiss2(rootView);
    }

    private void setUpKeyboardDismiss(View view) {
        view.setOnTouchListener((v, event) -> {
            if (editTextName.hasFocus()) {
                Rect outRect = new Rect();
                editTextName.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    editTextName.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
            return false;
        });
    }
    private void setUpKeyboardDismiss2(View view) {
        view.setOnTouchListener((v, event) -> {
            if (editTextPhone.hasFocus()) {
                Rect outRect = new Rect();
                editTextPhone.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    editTextPhone.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
            return false;
        });
    }


    private void updateTotalPrice() {
        double totalPrice = 0.0;
        for (Product product : selectedProducts.values()) {
            totalPrice += product.getPrice();
        }
        wynik.setText(String.format("Koszt za zamówienie: %.2f zł", totalPrice));
    }

    private List<Product> createSpinner1Products() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Lenovo Legion Gaming PC", 4199.99, R.drawable.komp1));
        products.add(new Product("HP EliteDesk Desktop", 3299.99, R.drawable.komp2));
        products.add(new Product("Acer Nitro 5 Gaming PC", 3999.99, R.drawable.komp3));
        return products;
    }




    private List<Product> createSpinner2Products() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Razer DeathAdder Elite", 79.99,R.drawable.mysz1));
        products.add(new Product("Logitech G502 Hero", 99.99,R.drawable.mysz2));
        products.add(new Product("SteelSeries Rival 3", 69.99,R.drawable.mysz3));
        return products;
    }

    private List<Product> createSpinner3Products() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Corsair K95 RGB Platinum", 229.99,R.drawable.klawiatura1));
        products.add(new Product("Logitech G915 Wireless Keyboard", 199.99,R.drawable.klawiatura2));
        products.add(new Product("Keychron K2 Bluetooth Keyboard", 159.99,R.drawable.klawiatura3));
        return products;
    }


    private List<Product> createSpinnerMonitorExtraProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Monitor Ultra HD 27", 999.99,R.drawable.monitor1));
        products.add(new Product("Monitor Gaming 34", 1299.99,R.drawable.monitor2));
        products.add(new Product("Monitor 4K 32", 1499.99,R.drawable.monitor3));
        return products;
    }

    private void initializeOrderList() {
        orderList.clear();

        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Brak wybranych produktów!", Toast.LENGTH_SHORT).show();
        }

        for (Product product : selectedProducts.values()) {
            orderList.add(new Order(product.getName(), product.getPrice(), new Date()));
        }


        for (Order order : orderList) {
            Log.d("OrderList", order.toString());
        }
    }




    private void showOrderList() {
        if (orderList.isEmpty()) {
            Toast.makeText(this, "Nie ma żadnych zamówień do wyświetlenia", Toast.LENGTH_SHORT).show();
            return;
        }


        String userName = savedUserName;


        double totalSum = 0.0;
        for (Order order : orderList) {
            totalSum += order.getPrice();
        }


        Date lastOrderDate = orderList.get(orderList.size() - 1).getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(lastOrderDate);


        String summary = "Imię: " + userName +
                "\nŁączna suma: " + String.format("%.2f zł", totalSum) +
                "\nData ostatniego zamówienia: " + formattedDate;


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Podsumowanie Zamówienia")
                .setMessage(summary)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void showInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Informacje o Autorze")
                .setMessage("Autor aplikacji: Tymon Wudarski")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void saveOrder() {
        Toast.makeText(this, "Zapisano zamówienie", Toast.LENGTH_SHORT).show();
        initializeOrderList();
        resetOrder();
    }
    private void shareOrder() {
        if (orderList.isEmpty()) {
            Toast.makeText(this, "Nie ma żadnych zamówień do udostępnienia", Toast.LENGTH_SHORT).show();
            return;
        }


        String userName = savedUserName;
        double totalSum = 0.0;


        for (Order order : orderList) {
            totalSum += order.getPrice();
        }

        String orderSummary = "Imię użytkownika: " + userName + "\n" +
                "Suma zamówienia: " + String.format("%.2f zł", totalSum) + "\n" +
                "Data zamówienia: " + android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", getLastOrderDate());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Podsumowanie Zamówienia");
        shareIntent.putExtra(Intent.EXTRA_TEXT, orderSummary);


        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, "Udostępnij zamówienie za pomocą:"));
        } else {
            Toast.makeText(this, "Brak aplikacji umożliwiającej udostępnienie", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSms() {

        if (orderList.isEmpty()) {
            Toast.makeText(this, "Nie ma żadnych zamówień do wysłania w SMS!", Toast.LENGTH_SHORT).show();
            return;
        }


        double totalSum = 0.0;
        StringBuilder productDetails = new StringBuilder();

        for (Order order : orderList) {
            totalSum += order.getPrice();
            productDetails.append(order.getName())
                    .append(" - ")
                    .append(String.format("%.2f zł", order.getPrice()))
                    .append("\n");
        }


        String userName = savedUserName;
        String phoneNumber = savedPhoneNumber;
        Date lastOrderDate = getLastOrderDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(lastOrderDate);


        String message = "Podsumowanie Zamówienia:\n" +
                "Imię użytkownika: " + userName + "\n" +
                "Łączna suma: " + String.format("%.2f zł", totalSum) + "\n" +
                "Data ostatniego zamówienia: " + formattedDate + "\n" +
                "Produkty:\n" + productDetails.toString();


        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nie można otworzyć aplikacji do wysyłania SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    public void sendEmail() {


        String userName = savedUserName;



        if (userName.isEmpty()) {
            Toast.makeText(this, "Proszę podać swoje imię!", Toast.LENGTH_SHORT).show();
            return;
        }





        double totalSum = 0.0;
        StringBuilder productDetails = new StringBuilder();

        for (Order order : orderList) {
            totalSum += order.getPrice();
            productDetails.append(order.getName())
                    .append(" - ")
                    .append(String.format("%.2f zł", order.getPrice()))
                    .append("\n");
        }




        String orderDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());


        String emailBody = "Imię: " + userName + "\n" +
                "Suma zamówienia: " + String.format("%.2f zł", totalSum) + "\n" +
                "Data zamówienia: " + orderDate + "\n" +
                "Produkty:\n" + productDetails.toString();


        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Nowe zamówienie od: " + userName);
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nie można otworzyć aplikacji do wysyłania e-mail: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }





    private Date getLastOrderDate() {
        if (orderList.isEmpty()) {
            return new Date();
        }
        return orderList.get(orderList.size() - 1).getDate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opcja1:
                showOrderList();
                return true;

            case R.id.opcja2:
                saveOrder();
                return true;

            case R.id.opcja3:
                showInfo();
                return true;

            case R.id.opcja4:
                shareOrder();
                return true;

            case R.id.opcja5:
                sendSms();
                return true;
            case R.id.opcja6:
                sendEmail();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
