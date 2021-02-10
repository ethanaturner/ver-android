package turnerapps.vertv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import org.json.JSONArray;

public class Results extends AppCompatActivity {

    static JSONArray results;
    ListView mResultsList;
    ResultsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mResultsList = (ListView) findViewById(R.id.resultsListView);
        mAdapter = new ResultsListAdapter(this, results);
        mResultsList.setAdapter(mAdapter);
    }

}