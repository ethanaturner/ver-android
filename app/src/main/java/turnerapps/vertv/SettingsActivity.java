package turnerapps.vertv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    Button mMyAccountButton;
    Button mSendFeedbackButton;
    Button mAboutVerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mMyAccountButton = (Button) findViewById(R.id.myaccountbutton);
        mSendFeedbackButton = (Button) findViewById(R.id.sendfeebackbutton);
        mAboutVerButton = (Button) findViewById(R.id.aboutverbutton);
        mMyAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyAccount.class);
                startActivity(intent);
            }
        });
        mSendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"turnerapps1@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Ver - Feedback");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });
        mAboutVerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AboutVer.class);
                startActivity(intent);
            }
        });
    }
}