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

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ethanturner on 2/15/17.
 */

public class ResultsListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private JSONArray results;

    public ResultsListAdapter(Context context, JSONArray results) {
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
                if (SearchingActivity.type.equals("show")) {
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
                            ResultsItem.mItemID = resultItem.getString("id");
                            Log.i("ID", ResultsItem.mItemID);
                            ResultsItem.mItemType = SearchingActivity.type;
                            Log.i("Type", ResultsItem.mItemType);
                        } catch (JSONException error) {
                            Log.w("JSON", "JSON Conversion Error");
                        }
                        Intent intent = new Intent(context, ResultsItem.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                });
            } catch (JSONException error) {
                Log.w("JSON", "JSON Conversion Error");
            }
        return convertView;
    }

}