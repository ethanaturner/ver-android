package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class CollectionItemViewer extends AppCompatActivity {

    static String mItemID = "";
    static String mItemType = "";
    MenuItem mDeleteItem;
    ImageView mPosterView;
    TextView mTitleView;
    TextView mYearView;
    TextView mRatingView;
    TextView mOverviewView;
    TextView mSourcesView;
    Integer retryAttempts = 0;
    DatabaseReference mDatabase;
    DatabaseReference mDeleteDatabase;
    DatabaseReference mSourcesDatabase;
    ArrayList mCollectionList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_item_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPosterView = (ImageView) findViewById(R.id.itemPoster);
        mTitleView = (TextView) findViewById(R.id.itemViewerTitle);
        mYearView = (TextView) findViewById(R.id.itemViewerYear);
        mRatingView = (TextView) findViewById(R.id.itemViewerRating);
        mOverviewView = (TextView) findViewById(R.id.itemViewerOverview);
        mSourcesView = (TextView) findViewById(R.id.itemViewerSources);
        getInformation(mItemID, mItemType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collection_item, menu);
        mDeleteItem = menu.findItem(R.id.action_delete);
        mDeleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CollectionItemViewer.this);
                builder.setMessage("Are you sure you want to remove this item from your collection?")
                        .setTitle("Remove From Collection");
                builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("Data").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    mCollectionList = new ArrayList<>();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        CollectionItem item = snapshot.getValue(CollectionItem.class);
                                        if (item.getType().equals(mItemType)) {
                                            if (item.getId().equals(mItemID)) {
                                                deleteItem(snapshot.getKey());
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
        return true;
    }

    public void deleteItem(String key) {
        mDeleteDatabase = FirebaseDatabase.getInstance().getReference();
        mDeleteDatabase.child("Data").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                finish();
            }
        });
    }

    public void getInformation(String id, final String type) {
        String requestURL = "";
        if (type.equals("show")) {
            requestURL = getString(R.string.guideboxApiURL) + "shows/" + id + "?" + getString(R.string.guideboxApiKey);
        } else {
            requestURL = getString(R.string.guideboxApiURL) + "movies/" + id + "?" + getString(R.string.guideboxApiKey);
        }
        Log.i("RequestURL", requestURL);
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, requestURL, new JSONObject(), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mTitleView.setText(response.getString("title"));
                            if (type.equals("show")) {
                                String firstaired = response.getString("first_aired");
                                final String showYear = firstaired.substring(0,4);
                                mYearView.setText(showYear);
                                String posterURL = response.getString("poster");
                                Picasso.with(CollectionItemViewer.this).load(posterURL).into(mPosterView);
                                getSources(response);
                            } else {
                                mYearView.setText(response.getString("release_year"));
                                String posterURL = response.getString("poster_240x342");
                                Picasso.with(CollectionItemViewer.this).load(posterURL).into(mPosterView);
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
                                getInformation(mItemID, mItemType);
                                retryAttempts++;
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CollectionItemViewer.this);
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
        VerSingleton.getInstance(CollectionItemViewer.this).addToRequestQueue(jsObjRequest);
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
                        if (childItem.getType().equals(CollectionItemViewer.mItemType)) {
                            if (childItem.getId().equals(CollectionItemViewer.mItemID)) {
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
                    if (CollectionItemViewer.mItemType.equals("show")) {
                        String sourcerequestURL = getString(R.string.guideboxApiURL) + "shows/" + CollectionItemViewer.mItemID + "/available_content?" + getString(R.string.guideboxApiKey);
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
                                                AlertDialog.Builder builder = new AlertDialog.Builder(CollectionItemViewer.this);
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
                        VerSingleton.getInstance(CollectionItemViewer.this).addToRequestQueue(jsObjectRequest);
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

}