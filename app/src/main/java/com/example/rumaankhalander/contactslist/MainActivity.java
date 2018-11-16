package com.example.rumaankhalander.contactslist;

import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private List<Contact> contactList = new ArrayList<>();


    private RequestQueue mRequestQueue;

    private ProgressBar mProgress;
    private RecyclerView mRecycler;

    public void addContact(String name, String number) {
        Toast.makeText(this, "Name: " + name + "\n" + "Number: " + number + "\n", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecycler = findViewById(R.id.recycler);
        mProgress = findViewById(R.id.progress_circular);

        findViewById(R.id.floatingActionButton).setOnClickListener(v -> showDialog());

        /* Initialize the Network Request Queue */
        mRequestQueue = Volley.newRequestQueue(this);

        getAllContacts();
    }

    private void showDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DialogFragment dialogFragment = AddContactFragment.newInstance();
        dialogFragment.show(ft, "dialog");
    }

    private void setUpRecyclerView() {
        /* Hide progress bar if already showing */
        mProgress.setVisibility(View.GONE);
        findViewById(R.id.layout).setVisibility(View.VISIBLE);
        TransitionManager.beginDelayedTransition(findViewById(R.id.root));

        ContactsRecyclerAdapter recyclerAdapter = new ContactsRecyclerAdapter(contactList);
        mRecycler.setAdapter(recyclerAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecycler.addItemDecoration(itemDecoration);

    }

    /**
     * Makes an HTTP request to get the list of all available contacts from the REST end point.
     */
    private void getAllContacts() {
        // Create the request
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, ContactsApi.ALL_CONTACTS, response -> {
                    // handling response here
                    try {
                        // parse the JSON response
                        parseJson(response);

                        setUpRecyclerView();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    Log.e(TAG, error.getLocalizedMessage(), error);
                });

        /* Add retry policy if network error */
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add that Request to the queue
        mRequestQueue.add(stringRequest);
    }

    private void parseJson(String response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response);

        JSONArray contacts = jsonObject.getJSONArray("contacts");
        if (contacts.length() == 0) {
            Log.e(TAG, "Contacts List is empty!");
        } else {
            for (int i = 0; i < contacts.length(); i++) {
                JSONObject object = contacts.getJSONObject(i);
                String name = object.getString("name");
                String number = object.getString("number");

                Contact contact = new Contact(name, number);
                contactList.add(contact);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
