package com.malik.doc4wheel;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.malik.doc4wheel.historyRecyclerView.HistoryAdapter;
import com.malik.doc4wheel.historyRecyclerView.HistoryObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private String customerOrMechanic, userId;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;
//
    private TextView mBalance;

    private Double Balance = 0.0;

    private Button mPayout;

    private EditText mPayoutEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mBalance = findViewById(R.id.balance);
        mPayout = findViewById(R.id.payout);
        mPayoutEmail = findViewById(R.id.payoutEmail);

        mHistoryRecyclerView = findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);


        customerOrMechanic = getIntent().getExtras().getString("customerOrMechanic");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

        if (customerOrMechanic.equals("Mechanics")) {
            mBalance.setVisibility(View.VISIBLE);
            mPayout.setVisibility(View.VISIBLE);
            mPayoutEmail.setVisibility(View.VISIBLE);
        }

        mPayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payoutRequest();
            }
        });
    }


    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrMechanic).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot history : dataSnapshot.getChildren()) {
                        if (history.getValue().toString().equals("true"))
                            FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    String destination = "Destination: --";
                    Double ridePrice = 0.0;

                    if (dataSnapshot.child("timestamp").getValue() != null) {
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }
                    if (dataSnapshot.child("destination").getValue() != null) {
                        destination = dataSnapshot.child("destination").getValue().toString();
                    }

                    if (dataSnapshot.child("customerPaid").getValue() != null && dataSnapshot.child("mechanicPaidOut").getValue() == null) {
                        if (dataSnapshot.child("price").getValue() != null) {
                            ridePrice = Double.valueOf(dataSnapshot.child("price").getValue().toString());
                            Balance += ridePrice;
                            mBalance.setText("Balance: " + String.valueOf(Balance) + " $");
                        }
                    }


                    HistoryObject obj = new HistoryObject(rideId, getDate(timestamp), destination);
                    resultsHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();

    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }


    ProgressDialog progress;

    private void payoutRequest() {
        try {
            progress = new ProgressDialog(this);
            progress.setTitle("Processing your payout");
            progress.setMessage("please wait");
            progress.setCancelable(false);
            progress.show();

            CountDownTimer countDownTimer = new CountDownTimer(3000, 3000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    progress.cancel();
                    Toast.makeText(HistoryActivity.this, "Processed successfully!", Toast.LENGTH_LONG).show();
                }
            };

            countDownTimer.start();
        } catch (Exception e) {
        }
    }
}















