package com.example.munch;

import androidx.annotation.NonNull;
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

import java.util.HashMap;

public class ViewLikedActivity extends BaseMenuActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_liked);
        ViewPager2 favouritesViewPager = findViewById(R.id.favouritesViewPager);
        favouritesViewPager.setAdapter(
                new FavouritesFragmentAdapter(this)
        );

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
    }

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
            return new FavouritesFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}