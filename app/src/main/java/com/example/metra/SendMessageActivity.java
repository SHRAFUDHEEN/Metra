package com.example.metra;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class SendMessageActivity extends AppCompatActivity {

    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    Context activityContext = this;
    EditText editTextPhoneNumber, editTextMessageBody;
    boolean securityFlag = false;
    String existingSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        editTextPhoneNumber = findViewById(R.id.edit_text_phone_number);
        editTextMessageBody = findViewById(R.id.edit_text_message);
        Button buttonSend = findViewById(R.id.button_send);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(activityContext, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToSendSMS();
                } else {
                    sendSMS();
                }
            }
        });

        handleIncomingData();
    }

    private void sendSMS() {

        if (securityFlag) {

            new AlertDialog.Builder(this).setTitle("Caution!").setMessage("Trying to forward a message from trusted source - " + existingSender + ", Continue?")

                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            sendIt();
                        }
                    })

                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            sendIt();
        }
    }

    private void sendIt() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(editTextPhoneNumber.getText().toString(), null, editTextMessageBody.getText().toString(), null, null);
        Toast.makeText(activityContext, "Message sent!", Toast.LENGTH_SHORT).show();
        editTextMessageBody.setText("");
    }

    private void getPermissionToSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                Toast.makeText(this, "Please allow permission! - Send SMS", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please allow permission! - Send SMS", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
                sendSMS();
            } else {
                Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        } else {
            Toast.makeText(this, "Unexpected value: " + requestCode, Toast.LENGTH_LONG).show();
        }
    }

    private void handleIncomingData() {

        // Get intent & action
        Intent intent = getIntent();
        String action = intent.getAction();

        if (Objects.equals(action, Intent.ACTION_SENDTO)) {
            String messageContent = intent.getStringExtra("sms_body");
            if (messageContent != null) {
                LogUtils.debug("Done");
                MessagesDatabaseHelper messagesDatabaseHelper = new MessagesDatabaseHelper(this);
                existingSender = messagesDatabaseHelper.checkMessage(messageContent);
                if (!existingSender.isEmpty()) {
                    securityFlag = true;
                }
            }
        }

//        String type = intent.getType();
//        if (Intent.ACTION_SEND.equals(action) && type != null) {
//            if ("text/plain".equals(type)) {
//                // Handle text being sent
//                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
//                if (sharedText != null) {
//                    // Update UI to reflect text being shared
//                    LogUtils.debug("Received : " + sharedText);
//                }
//            }
//        }

    }
}
