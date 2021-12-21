package com.example.munch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import pl.droidsonroids.gif.GifImageView;

public class ProfileActivity extends BaseMenuActivity {

    private FirestoreHelper firestoreHelper;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestoreHelper = new FirestoreHelper();
        String userID = mAuth.getCurrentUser().getUid();
        DocumentReference userDataDoc = firestoreHelper.getUserDoc(userID);

        TextView nameText = findViewById(R.id.nameProfileTxt);
        TextView emailText = findViewById(R.id.emailProfileTxt);


        registration = userDataDoc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String accountType = null;
                if (documentSnapshot != null) {
                    String firstName = documentSnapshot.getString("FirstName");
                    String lastName = documentSnapshot.getString("LastName");
                    String email = documentSnapshot.getString("Email");
                    accountType = documentSnapshot.getString("AccountType");

                    String fullName = firstName + " " + lastName;

                    nameText.setText(fullName);
                    emailText.setText(email);
                } else {
                    nameText.setText("Unknown");
                    emailText.setText("Unknown");
                }



                // show cute gif
                if (accountType != null && accountType.equals("vvip")) {
                    GifImageView cuteGif = findViewById(R.id.cuteGif);
                    if (Holiday.isChristmasPeriod()) {
                        cuteGif.setImageResource(R.drawable.cute_gif_christmas);
                    }
                    cuteGif.setVisibility(View.VISIBLE);

                    TextView cuteText = findViewById(R.id.cuteMessage);
                    cuteText.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}