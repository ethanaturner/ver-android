package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by ethanturner on 2/25/17.
 */

public class CollectionSearchResultsAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private JSONArray results;
    DatabaseReference mDatabase;
    DatabaseReference mAddDatabase;
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    ArrayList mCollectionList;
    ArrayList mIDList;

    public CollectionSearchResultsAdapter(Context context, JSONArray results) {
        this.context = context;
        this.results = results;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount () {
        return this.results.length();
    }

    @Override
    public long getItemId (int position) {
        return position;
    }

    @Override
    public Object getItem (int position) {
        try {
            return this.results.get(position);
        } catch (JSONException error) {
            return null;
        }

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.results_row, parent, false);
        final TextView titleView = ((TextView)convertView.findViewById(R.id.resultsTitle));
        final TextView yearView = ((TextView)convertView.findViewById(R.id.resultsYear));
        final ImageView imageView = ((ImageView)convertView.findViewById(R.id.resultsPoster));
        try {
            final JSONObject resultItem = results.getJSONObject(position);
            if (CollectionSearching.type.equals("show")) {
                titleView.setText(resultItem.getString("title"));
                String firstaired = resultItem.getString("first_aired");
                final String showYear = firstaired.substring(0,4);
                yearView.setText(showYear);
                String posterURL = resultItem.getString("artwork_208x117");
                Picasso.with(context).load(posterURL).into(imageView);
            } else {
                titleView.setText(resultItem.getString("title"));
                yearView.setText(resultItem.getString("release_year"));
                String posterURL = resultItem.getString("poster_240x342");
                Picasso.with(context).load(posterURL).into(imageView);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        final String newID = resultItem.getString("id");
                        final String newType = CollectionSearching.type;
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                                                if (item.getType().equals(newType)) {
                                                    mIDList.add(item.getId());
                                                }
                                            }
                                            if (mIDList.contains(newID)) {
                                                Log.i("Add to Collection", "Already in Collection");
                                                Toast.makeText(context, "Already in Collection", Toast.LENGTH_LONG).show();
                                            } else {
                                                mAddDatabase = FirebaseDatabase.getInstance().getReference().child("Data").child(uuid).push();
                                                HashMap<String, String> newRecord = new HashMap<>();
                                                newRecord.put("id", newID);
                                                newRecord.put("type", newType);
                                                mAddDatabase.setValue(newRecord, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        Log.i("Add to Collection", "Added.");
                                                        Toast.makeText(context, "Added.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            mAddDatabase = FirebaseDatabase.getInstance().getReference().child("Data").child(uuid).push();
                                            HashMap<String, String> newRecord = new HashMap<>();
                                            newRecord.put("id", newID);
                                            newRecord.put("type", newType);
                                            mAddDatabase.setValue(newRecord, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Log.i("Add to Collection", "Added.");
                                                    Toast.makeText(context, "Added.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
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


                    } catch (JSONException error) {
                        Log.w("JSON", "JSON Conversion Error");
                    }
                }
            });
        } catch (JSONException error) {
            Log.w("JSON", "JSON Conversion Error");
        }
        return convertView;
    }
}