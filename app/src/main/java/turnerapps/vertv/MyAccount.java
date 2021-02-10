package turnerapps.vertv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class MyAccount extends AppCompatActivity {

    TextView mUsername;
    TextView mCapacity;
    Button mUpgradeButton;
    Button mSignOutButton;
    private DatabaseReference mDatabase;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editServices();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUpgradeButton = (Button) findViewById(R.id.upgradebutton);
        mSignOutButton = (Button) findViewById(R.id.signoutbutton);
        mUpgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upgradeCapacity();
            }
        });
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        mUsername = (TextView) findViewById(R.id.username);
        mCapacity = (TextView) findViewById(R.id.capacity);
        getUserInfo();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void getUserInfo() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VerUser user = dataSnapshot.getValue(VerUser.class);
                String fullname = user.getFirstname() + " " + user.getLastname();
                mUsername.setText(fullname);
                String capacity = "DVD Capacity: " + user.getCapacity();
                mCapacity.setText(capacity);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("My Account", "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    private void editServices() {
        Intent intent = new Intent(MyAccount.this, Services.class);
        startActivity(intent);

    }

    private void upgradeCapacity() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccount.this);
        builder.setMessage("This feature is coming soon!")
                .setTitle("Coming Soon");
        builder.setPositiveButton("Okay, cool.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyAccount.this);
        builder.setMessage("Are you sure you want to sign out?")
                .setTitle("Sign Out");
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MyAccount.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}