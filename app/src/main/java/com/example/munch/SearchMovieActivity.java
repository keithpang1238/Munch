package com.example.munch;

import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SearchMovieActivity extends BaseMenuActivity {

    private Boolean isMovie;
    private HashMap<String, String> movieGenres;
    private HashMap<String, String> tvGenres;
    private HashMap<String, String> imageConfigs;
    private HashMap<String, String> providers;
    private HashMap<String, String> apiParams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);

        apiParams = new HashMap<>();
        apiParams.put("selectedGenresString", "");
        apiParams.put("selectedProvidersString", "");
        apiParams.put("startDateString", "");
        apiParams.put("endDateString", "");
        apiParams.put("minRuntimeString", "");
        apiParams.put("maxRuntimeString", "");
        apiParams.put("ratingString", "");
        apiParams.put("watch_region", "AU");


        Intent intent = getIntent();
        try {
            // Values stored in SQLite that should not change often
            movieGenres = (HashMap<String, String>) intent.getSerializableExtra("movieGenres");
            tvGenres = (HashMap<String, String>) intent.getSerializableExtra("tvGenres");
            imageConfigs = (HashMap<String, String>) intent.getSerializableExtra("imageConfigs");
            providers = (HashMap<String, String>) intent.getSerializableExtra("providers");
        } catch (Error e) {
            Toast.makeText(this, "Could not retrieve genres", Toast.LENGTH_SHORT).show();
        }

        isMovie = true;
        SearchMovieTypeFragment fragment = new SearchMovieTypeFragment();
        fragment.setOnClickListener(new FragmentClickListener(){
            @Override
            public void onClick(View v) {
                handleMovieType(v);
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, fragment)
                .commit();
    }

    void handleMovieType(View v) {
        if (v.getId() == R.id.tvSelectedBtn) {
            isMovie = false;
        } else if (v.getId() == R.id.movieSelectedBtn) {
            isMovie = true;
        } else {
            return;
        }
        SearchMovieGenreFragment searchGenreFragment;
        if (isMovie) {
            searchGenreFragment = SearchMovieGenreFragment.newInstance(movieGenres);
        } else {
            searchGenreFragment = SearchMovieGenreFragment.newInstance(tvGenres);
        }
        searchGenreFragment.setOnClickListener(new FragmentClickListener(){
            @Override
            public void onClick(View v) {
                retrieveGenres();
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, searchGenreFragment)
                .commit();
    }

    void retrieveGenres() {
        HashMap<String, String> genresToConsider;
        Set<String> selectedGenres = new HashSet<>();
        if (isMovie) {
            genresToConsider = movieGenres;
        } else {
            genresToConsider = tvGenres;
        }
        LinearLayout genreLayout = findViewById(R.id.genreLayout);
        CheckBox checkbox;
        for (String key: genresToConsider.keySet()) {
            checkbox = genreLayout.findViewWithTag(key);
            if (checkbox != null && checkbox.isChecked()) {
                selectedGenres.add(key);
            }
        }
        apiParams.put("selectedGenresString", "&with_genres=" + TextUtils.join("|", selectedGenres));

        SearchMovieProviderFragment movieProviderFragment =
                SearchMovieProviderFragment.newInstance(providers);
        movieProviderFragment.setOnClickListener(new FragmentClickListener(){
            @Override
            public void onClick(View v) {
                retrieveProviders();
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, movieProviderFragment)
                .commit();
    }

    void retrieveProviders() {
        Set<String> selectedProviders = new HashSet<>();
        LinearLayout genreLayout = findViewById(R.id.providerLayout);
        CheckBox checkbox;

        for (String key: providers.keySet()) {
            checkbox = genreLayout.findViewWithTag(providers.get(key));
            if (checkbox != null && checkbox.isChecked()) {
                selectedProviders.add(providers.get(key));
            }
        }

        apiParams.put("selectedProvidersString", "&with_watch_providers=" + TextUtils.join("|", selectedProviders));

        SearchMovieYearFragment movieYearFragment =
                SearchMovieYearFragment.newInstance(isMovie);
        movieYearFragment.setOnClickListener(new FragmentClickListener(){
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.submitBtn) {
                    retrieveYearRatingRuntimeData();
                }
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, movieYearFragment)
                .commit();
    }

    void retrieveYearRatingRuntimeData() {
        EditText startYear = findViewById(R.id.startYearTxt);
        EditText endYear = findViewById(R.id.endYearTxt);
        EditText minRating = findViewById(R.id.minRatingTxt);
        EditText minRuntime = findViewById(R.id.minTimeTxt);
        EditText maxRuntime = findViewById(R.id.maxTimeTxt);

        String startYearString = startYear.getText().toString().trim();
        String endYearString = endYear.getText().toString().trim();
        String minRatingString = minRating.getText().toString().trim();
        String minRuntimeString = minRuntime.getText().toString().trim();
        String maxRuntimeString = maxRuntime.getText().toString().trim();

        Integer startYearInt = getInputIntegerValue(startYearString);
        Integer endYearInt = getInputIntegerValue(endYearString);
        Integer minRuntimeInt = getInputIntegerValue(minRuntimeString);
        Integer maxRuntimeInt = getInputIntegerValue(maxRuntimeString);

        Float minRatingFloat;
        if (!minRatingString.matches("")) {
            minRatingFloat = Float.parseFloat(minRatingString);
        } else {
            minRatingFloat = null;
        }

        int currYear = Calendar.getInstance().get(Calendar.YEAR);

        if ((startYearInt != null && startYearInt > currYear)
                || (endYearInt != null && endYearInt > currYear)) {
            Toast.makeText(this, "Cannot provide year in the future", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startYearInt != null && endYearInt != null && startYearInt > endYearInt) {
            Toast.makeText(this, "Start year cannot be greater than end year", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minRatingFloat != null && (minRatingFloat < 0 || minRatingFloat > 10)) {
            Toast.makeText(this, "Rating must be between 0 and 10", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minRuntimeInt != null && maxRuntimeInt != null && minRuntimeInt > maxRuntimeInt) {
            Toast.makeText(this, "Min runtime must be greater than max runtime", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!minRatingString.isEmpty()) {
            apiParams.put("ratingString", "&vote_average.gte=" + minRatingString);
        }
        if (!minRuntimeString.isEmpty()) {
            apiParams.put("minRuntimeString", "&with_runtime.gte=" + minRuntimeString);
        }
        if (!maxRuntimeString.isEmpty()) {
            apiParams.put("minRuntimeString", "&with_runtime.lte=" + maxRuntimeString);
        }

        if (isMovie) {
            if (!startYearString.isEmpty()) {
                apiParams.put("startDateString", "&primary_release_date.gte=" + startYearString + "-01-01");
            }
            if (!endYearString.isEmpty()) {
                apiParams.put("endYearString", "&primary_release_date.lte=" + endYearString + "-12-31");
            }
        } else {
            if (!startYearString.isEmpty()) {
                apiParams.put("startDateString", "&first_air_date.gte=" + startYearString + "-01-01");
            }
            if (!endYearString.isEmpty()) {
                apiParams.put("endYearString", "&first_air_date.lte=" + endYearString + "-12-31");
            }
        }
        displayShows();
    }

    void displayShows() {
        Intent intent = new Intent(SearchMovieActivity.this, RandomShowActivity.class);
        intent.putExtra("apiParams", apiParams);
        intent.putExtra("isMovie", isMovie);
        intent.putExtra("imageConfigs", imageConfigs);
        startActivity(intent);
    }

    private Integer getInputIntegerValue(String s) {
        if (s.matches("")) {
            return null;
        }
        return Integer.parseInt(s);
    }

}