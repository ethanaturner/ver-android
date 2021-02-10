package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class MyCollectionActivity extends AppCompatActivity {

    ListView mCollectionListView;
    ListAdapter mAdapter;
    DatabaseReference mDatabase;
    ArrayList<JSONObject> mCollectionList;
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String capacity = "";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollectionListView = (ListView) findViewById(R.id.collectionListView);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTitles();
            }
        });
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VerUser user = dataSnapshot.getValue(VerUser.class);
                capacity = user.getCapacity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getCollection();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void getCollection() {
        mDatabase.child("Data").child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCollectionList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CollectionItem item = snapshot.getValue(CollectionItem.class);
                        JSONObject collectionItem = new JSONObject();
                        try {
                            collectionItem.put("id", item.getId());
                            collectionItem.put("type", item.getType());
                        } catch (JSONException error) {
                            Log.w("JSONError", error.getMessage());
                        }
                        mCollectionList.add(collectionItem);
                    }
                    mAdapter = new CollectionListAdapter(MyCollectionActivity.this, mCollectionList);
                    mCollectionListView.setAdapter(mAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addTitles() {
        Integer capacityInt = Integer.parseInt(capacity);
        if (mCollectionList.size() >= capacityInt) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Capacity Reached");
            builder.setMessage("Oops, you appear to have reached your Collection's capacity.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Titles");
            builder.setMessage("Find titles to add to your collection.");
            final EditText input = new EditText(this);
            input.setHint("Find a title . . . ");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 20, 10, 10);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setNegativeButton("Find show", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (TextUtils.isEmpty(input.getText().toString())) {
                        dialogInterface.dismiss();

                    } else {
                        Intent showIntent = new Intent(MyCollectionActivity.this, CollectionSearching.class);
                        CollectionSearching.query = input.getText().toString();
                        CollectionSearching.type = "show";
                        startActivity(showIntent);
                    }
                }
            });
            builder.setPositiveButton("Find movie", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (TextUtils.isEmpty(input.getText().toString())) {
                        dialogInterface.dismiss();

                    } else {
                        Intent movieIntent = new Intent(MyCollectionActivity.this, CollectionSearching.class);
                        CollectionSearching.query = input.getText().toString();
                        CollectionSearching.type = "movie";
                        startActivity(movieIntent);
                    }
                }
            });
            builder.create();
            builder.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}