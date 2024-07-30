package com.example.munch;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import pl.droidsonroids.gif.GifImageView;

public class ProfileActivity extends BaseMenuActivity {
    private final String unknownText = "Unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userID = mAuth.getCurrentUser().getUid();

        FirestoreHelper firestoreHelper = new FirestoreHelper();
        DocumentReference userDataDoc = firestoreHelper.getUserDoc(userID);

        TextView nameText = findViewById(R.id.nameProfileTxt);
        TextView emailText = findViewById(R.id.emailProfileTxt);

        userDataDoc.addSnapshotListener(this, (documentSnapshot, e) -> {
            String accountType;
            if (documentSnapshot != null) {
                String firstName = documentSnapshot.getString("FirstName");
                String lastName = documentSnapshot.getString("LastName");
                String email = documentSnapshot.getString("Email");
                accountType = documentSnapshot.getString("AccountType");

                String fullName = firstName + " " + lastName;
                nameText.setText(fullName);
                emailText.setText(email);
            } else {
                accountType = null;
                nameText.setText(unknownText);
                emailText.setText(unknownText);
            }

            if (accountType == null || !accountType.equals("vvip")) {
                return;
            }

            GifImageView cuteGif = findViewById(R.id.cuteGif);
            if (Holiday.isChristmasPeriod()) {
                cuteGif.setImageResource(R.drawable.cute_gif_christmas);
            }
            cuteGif.setVisibility(View.VISIBLE);
            TextView cuteText = findViewById(R.id.cuteMessage);
            cuteText.setVisibility(View.VISIBLE);
        });
    }
}