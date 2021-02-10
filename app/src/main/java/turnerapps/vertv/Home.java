package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class Home extends AppCompatActivity {

    EditText mSearchField;
    Button mFindButton;
    Button mMyCollectionButton;
    MenuItem mSettingsItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Raleway-Regular.ttf");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSearchField = (EditText) findViewById(R.id.search_field);
        mFindButton = (Button) findViewById(R.id.gosearchbutton);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mSearchField.getText().toString().equals("")) {
                    startSearchEngine();
                }

            }
        });
        mMyCollectionButton = (Button) findViewById(R.id.mycollectionbutton);
        mMyCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goMyCollection();
            }
        });
        MobileAds.initialize(Home.this, getString(R.string.AdPublisherID));
        AdView mAdView = (AdView) findViewById(R.id.HomeAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        mSettingsItem = menu.findItem(R.id.action_settings);
        mSettingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    public void startSearchEngine() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What kind of title is this?");
        builder.setItems(new CharSequence[]
                        {"Movie", "Show"},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent movieIntent = new Intent(Home.this, SearchingActivity.class);
                                SearchingActivity.query = mSearchField.getText().toString();
                                SearchingActivity.type = "movie";
                                startActivity(movieIntent);
                                break;
                            case 1:
                                Intent showIntent = new Intent(Home.this, SearchingActivity.class);
                                SearchingActivity.query = mSearchField.getText().toString();
                                SearchingActivity.type = "show";
                                startActivity(showIntent);
                                break;
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    public void goMyCollection() {
        Intent intent = new Intent(Home.this, MyCollectionActivity.class);
        startActivity(intent);
    }

}