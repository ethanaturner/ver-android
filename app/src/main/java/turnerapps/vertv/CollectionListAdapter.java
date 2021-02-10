package turnerapps.vertv;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;


/**
 * Created by ethanturner on 2/12/17.
 */

public class CollectionListAdapter extends BaseAdapter {
    private ArrayList<JSONObject> collection;
    private Context context;
    private LayoutInflater mInflater;

    public CollectionListAdapter(Context context, ArrayList<JSONObject> collection) {
        this.context = context;
        this.collection = collection;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount () {
        return collection.size();
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public Object getItem (int position) {
        return collection.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final JSONObject item = collection.get(position);
        convertView = mInflater.inflate(R.layout.collection_row, parent, false);
        final TextView titleView = ((TextView)convertView.findViewById(R.id.itemTitle));
        final TextView yearView = ((TextView)convertView.findViewById(R.id.itemYear));
        final ImageView imageView = ((ImageView)convertView.findViewById(R.id.poster));
        try {
            String id = item.getString("id");
            final String type = item.getString("type");
            String typeFinal = "";
            if (type.equals("show")) {
                typeFinal = "shows";
            } else if (type.equals("movie")) {
                typeFinal = "movies";
            }
            String requestURL = "https://api-public.guidebox.com/v2/" + typeFinal + "/" + id + "?api_key=<KEY>";
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestURL, new JSONObject(), new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    try {
                        titleView.setText(response.getString("title"));
                        if (type.equals("show")) {
                            String firstaired = response.getString("first_aired");
                            final String showYear = firstaired.substring(0,4);
                            yearView.setText(showYear);
                            String posterURL = response.getString("poster");
                            Picasso.with(context).load(posterURL).into(imageView);
                        } else {
                            yearView.setText(response.getString("release_year"));
                            String posterURL = response.getString("poster_240x342");
                            Picasso.with(context).load(posterURL).into(imageView);
                        }
                    } catch (JSONException error) {
                        Log.w("JSONError", error.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.i("VolleyError", volleyError.toString());
                    if (volleyError.networkResponse.statusCode == 500) {
                        Toast.makeText(context, "Internal server error: High request volume.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy());
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException error) {

        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CollectionItemViewer.class);
                try {
                    CollectionItemViewer.mItemID = item.getString("id");
                    CollectionItemViewer.mItemType = item.getString("type");
                } catch (JSONException error) {
                    Log.w("JSONError", error.getMessage());
                }
                context.startActivity(intent);
            }
        });
        return convertView;
    }

}
