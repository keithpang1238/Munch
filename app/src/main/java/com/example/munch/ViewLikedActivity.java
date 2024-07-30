package com.example.munch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

public class ViewLikedActivity extends BaseMenuActivity {

    private String userID;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_liked);

        // Set up viewpager for handling tabs
        ViewPager2 favouritesViewPager = findViewById(R.id.favouritesViewPager);
        favouritesViewPager.setAdapter(
                new FavouritesFragmentAdapter(this)
        );

        // Set up tabs with tab layout mediator
        TabLayout favouritesTabLayout = findViewById(R.id.favouritesTabLayout);
        new TabLayoutMediator(
            favouritesTabLayout,
            favouritesViewPager,
            (tab, position) -> {
                if (position == 0) {
                    tab.setText("Liked Movies");
                } else {
                    tab.setText("Liked TV Shows");
                }
            }
        ).attach();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
    }


    // class for handling tab display for viewpager
    class FavouritesFragmentAdapter extends FragmentStateAdapter {
        public FavouritesFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new FavouritesFragment(userID, true);
            }
            return new FavouritesFragment(userID, false);
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