package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CollectionSearching extends AppCompatActivity {

    static String query;
    static String type;
    Integer retryAttempts = 0;
    JSONArray results = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_searching);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        requestData();
    }

    private void requestData() {
        String rawQuery = CollectionSearching.query.trim().replace(" ", "+");
        String requestURL = getString(R.string.guideboxApiURL) + "search?type=" + CollectionSearching.type + "&query=" + rawQuery + "&" + getString(R.string.guideboxApiKey);
        Log.i("RequestURL", requestURL);
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, requestURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            results = response.getJSONArray("results");
                        } catch (JSONException error) {
                            Log.w("JSON", "JSON Conversion Error");
                        }
                        if (results != null) {
                            if (results.length() == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CollectionSearching.this);
                                builder.setMessage("Sorry, we couldn't find any titles for that search")
                                        .setTitle("No Results");
                                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                toResults(results);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse.statusCode == 500) {
                            if (retryAttempts >= 4) {
                                requestData();
                                retryAttempts++;
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(CollectionSearching.this);
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
        VerSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    public void toResults(JSONArray results) {
        Intent intent = new Intent(CollectionSearching.this, CollectionSearchResults.class);
        CollectionSearchResults.results = results;
        startActivity(intent);
        finish();
    }

}