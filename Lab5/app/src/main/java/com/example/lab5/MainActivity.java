package com.example.lab5;

import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

// Firebase imports
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;
    List<Product> products;

    // Firebase database reference
    DatabaseReference databaseProducts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseProducts = FirebaseDatabase.
                getInstance().
                getReference("products");

        editTextName        = (EditText) findViewById(R.id.Name);
        editTextPrice       = (EditText) findViewById(R.id.Price);
        listViewProducts    = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct    = (Button)   findViewById(R.id.addButton);

        products = new ArrayList<>();

        //adding an onclick listener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Firebase listener
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // clearing the previous product list
                products.clear();
                // iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // getting product
                    Product product = postSnapshot.getValue(Product.class);
                    // adding product to the list
                    products.add(product);
                }
                // creating adapter
                ProductList productAdapter = new ProductList(MainActivity.this, products);
                // attaching adapter to the listview
                listViewProducts.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.Name);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.Price);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.Update);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.Delete);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {
        // getting the specified product reference by the id
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        // create a product object with new values and id
        Product product = new Product(id, name, price);
        // update the product in the database
        dR.setValue(product);
        Toast.makeText(getApplicationContext(), "Product Updated", Toast.LENGTH_LONG).show();
    }

    private void deleteProduct(String id) {
        // getting the specified product reference by the id
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        // removing product
        dR.removeValue();
        Toast.makeText(getApplicationContext(), "Product Deleted", Toast.LENGTH_LONG).show();
    }

    private void addProduct() {
        // getting the values to save
        String name = editTextName.getText().toString().trim();
        double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
        // checking if the value is provided
        if (!TextUtils.isEmpty(name)) {
            // getting a unique id using push().getKey() method
            // it will create a unique id and we will use it as the Primary Key for our Product
            String id = databaseProducts.push().getKey();
            // creating an Product Object
            Product product = new Product(id, name, price);
            // saving the Product
            databaseProducts.child(id).setValue(product);
            // setting edittext to blank again to clear the text boxes
            editTextName.setText("");
            editTextPrice.setText("");
            // display message to user
            Toast.makeText(this, "Product added", Toast.LENGTH_LONG).show();
        } else {
            // if the value is not given displaying a toast
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
        }
    }
}