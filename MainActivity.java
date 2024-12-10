package com.example.projekt_zaliczeniowy_sklep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.Manifest;


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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper; // Obsługa bazy danych
    private List<Product> spinner1Products, spinner2Products, spinner3Products;
    private Map<String, Product> selectedProducts = new HashMap<>(); // Mapa wybranych produktów
    private TextView wynik; // TextView do wyświetlania sumy
    private List<Order> orderList = new ArrayList<>();
    private EditText editTextName;
    private EditText editTextPhone;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicjalizacja bazy danych
        databaseHelper = new DatabaseHelper(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Pobranie TextView do wyświetlania sumy
        wynik = findViewById(R.id.wynik);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);

        // Inicjalizacja listy zamówień
        initializeOrderList();

        // Spinner 1 i CheckBox
        CheckBox checkboxKomp = findViewById(R.id.checkbox_komp);
        Spinner spinner1 = findViewById(R.id.spinner1);
        spinner1Products = createSpinner1Products();
        setupSpinner(spinner1, spinner1Products, "Komp");

        checkboxKomp.setOnClickListener(v -> handleCheckboxClick(checkboxKomp, spinner1, "Komp"));

        // Spinner 2 i CheckBox
        CheckBox checkboxMysz = findViewById(R.id.checkbox_mysz);
        Spinner spinner2 = findViewById(R.id.spinner2);
        spinner2Products = createSpinner2Products();
        setupSpinner(spinner2, spinner2Products, "Mysz");

        checkboxMysz.setOnClickListener(v -> handleCheckboxClick(checkboxMysz, spinner2, "Mysz"));

        // Spinner 3 i CheckBox
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


        // Przycisk Zamówienie
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

        if (userName.isEmpty()) {
            Toast.makeText(this, "Proszę podać swoje imię przed złożeniem zamówienia!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Nie dodano żadnych produktów do zamówienia.", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalPrice = 0.0;
        for (Product product : selectedProducts.values()) {
            totalPrice += product.getPrice();
        }

        for (Product product : selectedProducts.values()) {
            databaseHelper.addProductToOrder(product);
        }

        orderList.clear();
        for (Product product : selectedProducts.values()) {
            orderList.add(new Order(product.getName(), product.getPrice(), new Date()));
        }

        Toast.makeText(this, "Zamówienie zapisane w bazie danych.", Toast.LENGTH_SHORT).show();

        // Odświeżanie sumy i reset
        updateTotalPrice();
        resetOrder();
    }

    private void resetOrder() {
        // Wyczyść mapę produktów
        selectedProducts.clear();

        // Zresetuj sumę wyświetlaną w TextView
        wynik.setText("Koszt za zamówienie: 0.00 zł");

        // Opcjonalnie: Resetowanie Spinnerów i CheckBoxów
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




    //zamknicie klawaitury
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View rootView = findViewById(android.R.id.content);
        setUpKeyboardDismiss(rootView);
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

// Podobnie dla innych spinnerów...


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

        // Logowanie zawartości orderList
        for (Order order : orderList) {
            Log.d("OrderList", order.toString());
        }
    }




    private void showOrderList() {
        if (orderList.isEmpty()) {
            Toast.makeText(this, "Nie ma żadnych zamówień do wyświetlenia", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = editTextName.getText().toString().trim(); // Pobranie imienia użytkownika

        // Obliczamy sumę wszystkich cen
        double totalSum = 0.0;
        for (Order order : orderList) {
            totalSum += order.getPrice();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Podsumowanie Zamówienia")
                .setMessage(userName + " - Suma: " + String.format("%.2f zł", totalSum) + " - " +
                        android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", getLastOrderDate()))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
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

        String userName = editTextName.getText().toString().trim();
        double totalSum = 0.0;

        // Obliczanie sumy wszystkich zamówień
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

        // Sprawdzenie, czy istnieje aplikacja do udostępniania
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, "Udostępnij zamówienie za pomocą:"));
        } else {
            Toast.makeText(this, "Brak aplikacji umożliwiającej udostępnienie", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSms() {
        // Pobierz numer telefonu z EditText
        String phoneNumber = editTextPhone.getText().toString().trim();

        // Sprawdź, czy numer telefonu został podany
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Proszę podać numer telefonu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pobierz imię użytkownika z EditText
        String userName = editTextName.getText().toString().trim();

        // Sprawdź, czy imię użytkownika zostało podane
        if (userName.isEmpty()) {
            Toast.makeText(this, "Proszę podać swoje imię!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdź, czy wybrano produkty
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Brak wybranych produktów do wysłania w SMS!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Zbuduj listę produktów i oblicz sumę
        StringBuilder productDetails = new StringBuilder();
        double totalPrice = 0.0;
        for (Map.Entry<String, Product> entry : selectedProducts.entrySet()) {
            Product product = entry.getValue();
            productDetails.append(product.getName()).append(" - ").append(String.format("%.2f zł", product.getPrice())).append("\n");
            totalPrice += product.getPrice();
        }

        // Utwórz treść wiadomości SMS
        String message = "Imię: " + userName + "\n" +
                "Suma zamówienia: " + String.format("%.2f zł", totalPrice) + "\n" +
                "Produkty:\n" + productDetails.toString();

        // Utwórz Intent, który otworzy domyślną aplikację do wysyłania SMS
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber)); // Ustaw numer telefonu
        intent.putExtra("sms_body", message); // Ustaw treść wiadomości

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nie można otworzyć aplikacji do wysyłania SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    public void sendEmail() {
        // Pobierz imię użytkownika z EditText
        String userName = editTextName.getText().toString().trim();

        // Sprawdź, czy imię użytkownika zostało podane
        if (userName.isEmpty()) {
            Toast.makeText(this, "Proszę podać swoje imię!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sprawdź, czy wybrano produkty
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "Brak wybranych produktów do wysłania w e-mail!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Zbuduj listę produktów i oblicz sumę
        StringBuilder productDetails = new StringBuilder();
        double totalPrice = 0.0;
        for (Map.Entry<String, Product> entry : selectedProducts.entrySet()) {
            Product product = entry.getValue();
            productDetails.append(product.getName()).append(" - ").append(String.format("%.2f zł", product.getPrice())).append("\n");
            totalPrice += product.getPrice();
        }

        // Pobierz aktualną datę
        String orderDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Utwórz treść wiadomości e-mail
        String emailBody = "Imię: " + userName + "\n" +
                "Suma zamówienia: " + String.format("%.2f zł", totalPrice) + "\n" +
                "Data zamówienia: " + orderDate + "\n" +
                "Produkty:\n" + productDetails.toString();

        // Utwórz Intent do otwarcia domyślnej aplikacji e-mail
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Otwiera aplikację e-mail
        intent.putExtra(Intent.EXTRA_SUBJECT, "Nowe zamówienie od: " + userName);
        intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Nie można otworzyć aplikacji do wysyłania e-mail: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }




    // Pobranie daty ostatniego zamówienia
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
