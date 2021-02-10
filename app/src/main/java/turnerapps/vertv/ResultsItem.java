package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultsItem extends AppCompatActivity {

    static String mItemID = "";
    static String mItemType = "";
    ImageView mPosterView;
    TextView mTitleView;
    TextView mYearView;
    TextView mRatingView;
    TextView mOverviewView;
    TextView mSourcesView;
    Integer retryAttempts = 0;
    DatabaseReference mDatabase;
    DatabaseReference mAddDatabase;
    DatabaseReference mSourcesDatabase;
    ArrayList mCollectionList;
    ArrayList mIDList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPosterView = (ImageView) findViewById(R.id.resultPoster);
        mTitleView = (TextView) findViewById(R.id.resultTitle);
        mYearView = (TextView) findViewById(R.id.resultYear);
        mRatingView = (TextView) findViewById(R.id.resultRating);
        mOverviewView = (TextView) findViewById(R.id.resultOverview);
        mSourcesView = (TextView) findViewById(R.id.resultSources);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ResultsItem.this);
                builder.setMessage("Are you sure you want to add this title to your collection?")
                        .setTitle("Add to Collection");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("Data").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    mIDList = new ArrayList();
                                    mCollectionList = new ArrayList<>();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        CollectionItem item = snapshot.getValue(CollectionItem.class);
                                        if (item.getType().equals(mItemType)) {
                                            mIDList.add(item.getId());
                                        }
                                    }
                                    if (mIDList.contains(mItemID)) {
                                        Log.i("Add to Collection", "Already in Collection");
                                    } else {
                                        mAddDatabase = FirebaseDatabase.getInstance().getReference().child("Data").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push();
                                        HashMap<String, String> newRecord = new HashMap<>();
                                        newRecord.put("id", mItemID);
                                        newRecord.put("type", mItemType);
                                        mAddDatabase.setValue(newRecord, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                Log.i("Add to Collection", "Added.");
                                            }
                                        });
                                    }
                                } else {
                                    mAddDatabase = FirebaseDatabase.getInstance().getReference().child("Data").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push();
                                    HashMap<String, String> newRecord = new HashMap<>();
                                    newRecord.put("id", mItemID);
                                    newRecord.put("type", mItemType);
                                    mAddDatabase.setValue(newRecord, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            Log.i("Add to Collection", "Added.");
                                        }
                                    });
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getData(mItemID, mItemType);
    }

    public void getData(String id, final String type) {
        String requestURL = "";
        if (type.equals("show")) {
            requestURL = getString(R.string.guideboxApiURL) + "shows/" + id + "?" + getString(R.string.guideboxApiKey);
        } else {
            requestURL = getString(R.string.guideboxApiURL) + "movies/" + id + "?" + getString(R.string.guideboxApiKey);
        }
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, requestURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mTitleView.setText(response.getString("title"));
                            if (type.equals("show")) {
                                String firstaired = response.getString("first_aired");
                                final String showYear = firstaired.substring(0,4);
                                mYearView.setText(showYear);
                                String posterURL = response.getString("poster");
                                Picasso.with(ResultsItem.this).load(posterURL).into(mPosterView);
                                getSources(response);
                            } else {
                                mYearView.setText(response.getString("release_year"));
                                String posterURL = response.getString("poster_240x342");
                                Picasso.with(ResultsItem.this).load(posterURL).into(mPosterView);
                                getSources(response);
                            }
                            mRatingView.setText(response.getString("rating"));
                            mOverviewView.setText(response.getString("overview"));
                        } catch (JSONException error) {
                            Log.w("JSONError", error.toString());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 500) {
                            if (retryAttempts >= 4) {
                                getData(mItemID, mItemType);
                                retryAttempts++;
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ResultsItem.this);
                                builder.setMessage("It appears our server is receiving a large amount of requests. Please try again in a moment.")
                                        .setTitle("Server Error");
                                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                        }
                    }
                });
        VerSingleton.getInstance(ResultsItem.this).addToRequestQueue(jsObjRequest);
    }

    public void getSources(final JSONObject movieresponse) {
        final ArrayList<String> availableSources = new ArrayList<>();
        final ArrayList<String> userSources = new ArrayList<>();
        availableSources.clear();
        userSources.clear();
        mSourcesDatabase = FirebaseDatabase.getInstance().getReference();
        mSourcesDatabase.child("Data").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        CollectionItem childItem = child.getValue(CollectionItem.class);
                        if (childItem.getType().equals(ResultsItem.mItemType)) {
                            if (childItem.getId().equals(ResultsItem.mItemID)) {
                                availableSources.add("Your Collection");
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mSourcesDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Services").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Object> userAvailable = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (userAvailable.get("netflix").equals("Yes")) {
                        userSources.add("netflix");
                    }
                    if (userAvailable.get("hulu").equals("Paid")) {
                        userSources.add("hulu_plus");
                    } else {
                        userSources.add("hulu_free");
                    }
                    if (userAvailable.get("amazon").equals("Paid")) {
                        userSources.add("amazon_prime");
                    } else {
                        userSources.add("amazon");
                    }
                    if (userAvailable.get("amazon_purchase").equals("Yes")) {
                        userSources.add("amazon_buy");
                    }
                    if (userAvailable.get("crunchyroll").equals("Paid")) {
                        userSources.add("crunchyroll_premium");
                    } else {
                        userSources.add("crunchyroll_free");
                    }
                    if (ResultsItem.mItemType.equals("show")) {
                        String sourcerequestURL = getString(R.string.guideboxApiURL) + "shows/" + ResultsItem.mItemID + "/available_content?" + getString(R.string.guideboxApiKey);
                        final JsonObjectRequest jsObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, sourcerequestURL, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONObject results = response.getJSONObject("results");
                                            JSONObject webSources = results.getJSONObject("web");
                                            JSONObject episodes = webSources.getJSONObject("episodes");
                                            JSONArray allSources = episodes.getJSONArray("all_sources");
                                            for (int i = 0; i < allSources.length(); i++) {
                                                JSONObject source = allSources.getJSONObject(i);
                                                for (String usersource : userSources) {
                                                    String sourceName = source.getString("source");
                                                    if (usersource.equals(sourceName)) {
                                                        if (source.getString("source").equals("amazon_buy")) {
                                                            if (availableSources.contains("Amazon (Purchase)")) {

                                                            } else {
                                                                availableSources.add("Amazon (Purchase)");
                                                            }
                                                        } else {
                                                            if (availableSources.contains(sourceName)) {

                                                            } else {
                                                                availableSources.add(sourceName);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (availableSources.isEmpty()) {
                                                mSourcesView.setText("Sorry, this title doesn't appear to be available on any of your services.");
                                            } else {
                                                mSourcesView.setText(android.text.TextUtils.join(", ", availableSources).replace("netflix", "Netflix").replace("amazon_prime", "Amazon Instant Video").replace("hulu_plus", "Hulu+").replace("hulu_free", "Hulu"));
                                            }
                                        } catch (JSONException error) {
                                            Log.w("JSONError", error.toString());
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        if (error.networkResponse.statusCode == 500) {
                                            if (retryAttempts >= 4) {
                                                getSources(null);
                                                retryAttempts++;
                                            } else {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ResultsItem.this);
                                                builder.setMessage("It appears our server is receiving a large amount of requests. Please try again in a moment.")
                                                        .setTitle("Server Error");
                                                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }

                                        }
                                    }
                                });
                        VerSingleton.getInstance(ResultsItem.this).addToRequestQueue(jsObjectRequest);
                    } else {
                        try {
                            JSONArray free_web_sources = movieresponse.getJSONArray("free_web_sources");
                            for (int i = 0; i < free_web_sources.length(); i++) {
                                JSONObject source = free_web_sources.getJSONObject(i);
                                String sourceName = source.getString("source");
                                if (source.getString("source").equals("amazon_buy")) {
                                    if (availableSources.contains("Amazon (Purchase)")) {

                                    } else {
                                        availableSources.add("Amazon (Purchase)");
                                    }
                                } else {
                                    if (availableSources.contains(sourceName)) {

                                    } else {
                                        availableSources.add(sourceName);
                                    }
                                }
                            }
                            JSONArray subscription_web_sources = movieresponse.getJSONArray("subscription_web_sources");
                            for (int i = 0; i < subscription_web_sources.length(); i++) {
                                JSONObject source = subscription_web_sources.getJSONObject(i);
                                for (String usersource : userSources) {
                                    String sourceName = source.getString("source");
                                    if (usersource.equals(sourceName)) {
                                        if (source.getString("source").equals("amazon_buy")) {
                                            if (availableSources.contains("Amazon (Purchase)")) {

                                            } else {
                                                availableSources.add("Amazon (Purchase)");
                                            }
                                        } else {
                                            if (availableSources.contains(sourceName)) {

                                            } else {
                                                availableSources.add(sourceName);
                                            }
                                        }
                                    }
                                }
                            }
                            JSONArray purchase_web_sources = movieresponse.getJSONArray("purchase_web_sources");
                            for (int i = 0; i < purchase_web_sources.length(); i++) {
                                JSONObject source = purchase_web_sources.getJSONObject(i);
                                for (String usersource : userSources) {
                                    String sourceName = source.getString("source");
                                    if (usersource.equals(sourceName)) {
                                        if (source.getString("source").equals("amazon_buy")) {
                                            if (availableSources.contains("Amazon (Purchase)")) {

                                            } else {
                                                availableSources.add("Amazon (Purchase)");
                                            }
                                        } else {
                                            if (availableSources.contains(sourceName)) {

                                            } else {
                                                availableSources.add(sourceName);
                                            }
                                        }
                                    }
                                }
                            }
                            if (availableSources.isEmpty()) {
                                mSourcesView.setText("Sorry, this title doesn't appear to be available on any of your services.");
                            } else {
                                mSourcesView.setText(android.text.TextUtils.join(", ", availableSources).replace("netflix", "Netflix").replace("amazon_prime", "Amazon Instant Video").replace("hulu_plus", "Hulu+").replace("hulu_free", "Hulu"));
                            }
                        } catch (JSONException error) {
                            Log.w("JSON", error.toString());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}