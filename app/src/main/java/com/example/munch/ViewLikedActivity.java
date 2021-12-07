package com.example.munch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;

public class ViewLikedActivity extends BaseMenuActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private ListenerRegistration registration;

    private List<String> likedMovieIDs;
    private List<String> likedTVShowIDs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_liked);

        // Set up viewpager for handling tabs
        ViewPager2 favouritesViewPager = findViewById(R.id.favouritesViewPager);
        favouritesViewPager.setAdapter(
                new FavouritesFragmentAdapter(this)
        );

        // Set up tabs with tablayoutmediator
        TabLayout favouritesTabLayout = findViewById(R.id.favouritesTabLayout);
        new TabLayoutMediator(
                favouritesTabLayout,
                favouritesViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        if (position == 0) {
                            tab.setText("Liked Movies");
                        } else {
                            tab.setText("Liked TV Shows");
                        }
                    }
                }
        ).attach();

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        /*
        // Get liked ids
        DocumentReference userDataDoc = mStore.collection("users").document(userID);
        registration = userDataDoc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    likedMovieIDs = (List<String>) documentSnapshot.get("LikedMovies");
                    likedTVShowIDs = (List<String>) documentSnapshot.get("LikedTVShows");
                }
            }
        });
         */
    }


    // class for handling tab display for viewpager
    class FavouritesFragmentAdapter extends FragmentStateAdapter {
        public FavouritesFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        public FavouritesFragmentAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public FavouritesFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new FavouritesFragment(userID, true);
            } else {
                return new FavouritesFragment(userID, false);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

}