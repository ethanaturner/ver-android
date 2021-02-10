package turnerapps.vertv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Services extends AppCompatActivity {

    ToggleButton mNetflixToggle;
    ToggleButton mHuluToggle;
    ToggleButton mAmazonToggle;
    ToggleButton mAmazonPurchaseToggle;
    ToggleButton mCrunchyrollToggle;
    Button mDoneButton;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNetflixToggle = (ToggleButton) findViewById(R.id.netflixToggle);
        mHuluToggle = (ToggleButton) findViewById(R.id.huluToggle);
        mAmazonToggle = (ToggleButton) findViewById(R.id.amazonToggle);
        mAmazonPurchaseToggle = (ToggleButton) findViewById(R.id.amazonPurchaseToggle);
        mCrunchyrollToggle = (ToggleButton) findViewById(R.id.crunchyrollToggle);
        mDoneButton = (Button) findViewById(R.id.servicesDoneButton);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerServices();
            }
        });
    }

    public void registerServices() {
        HashMap<String, String> userServices = new HashMap<>();
        if (mNetflixToggle.isChecked()) {
            userServices.put("netflix", "Yes");
        } else {
            userServices.put("netflix", "No");
        }
        if (mHuluToggle.isChecked()) {
            userServices.put("hulu", "Paid");
        } else {
            userServices.put("hulu", "Free");
        }
        if (mAmazonToggle.isChecked()) {
            userServices.put("amazon", "Paid");
        } else {
            userServices.put("amazon", "Free");
        }
        if (mAmazonPurchaseToggle.isChecked()) {
            userServices.put("amazon_purchase", "Yes");
        } else {
            userServices.put("amazon_purchase", "No");
        }
        if (mCrunchyrollToggle.isChecked()) {
            userServices.put("crunchyroll", "Paid");
        } else {
            userServices.put("crunchyroll", "Free");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Services").setValue(userServices, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Intent intent = new Intent(Services.this, Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }

}