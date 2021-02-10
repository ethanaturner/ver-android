package turnerapps.vertv;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;

public class CollectionSearchResults extends AppCompatActivity {

    static JSONArray results;
    ListView mResultsList;
    CollectionSearchResultsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_search_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mResultsList = (ListView) findViewById(R.id.collectionResultsListView);
        mAdapter = new CollectionSearchResultsAdapter(this, results);
        mResultsList.setAdapter(mAdapter);
    }

}