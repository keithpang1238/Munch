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
import java.util.Map;
import java.util.Set;

public class SearchMovieActivity extends BaseMenuActivity {

    private Map<String, String> searchPayload;

    private Boolean isMovie;
    private HashMap<String, String> movieGenres;
    private HashMap<String, String> tvGenres;
    private HashMap<String, String> imageConfigs;
    private HashMap<String, String> providers;

    private HashMap<String, String> apiParams;
    private String selectedGenresString;   // Pipe-separated string of genre ids
    private String selectedProvidersString;
    private String startDateString;
    private String endDateString;
    private String minRuntimeRangeString;
    private String maxRuntimeRangeString;
    private String ratingString;


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

        searchPayload = new HashMap<>();
        searchPayload.put("watch_region", "AU");

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

        String[] keys = {
                "sort_by",
                "primary_release_date.gte",
                "primary_release_date.lte",
                "with_genres",
                "with_watch_providers",
                "with_runtime.gte",
                "with_runtime.lte",
                "vote_average.gte",
                "vote_average.lte",
                };
    }

    void handleMovieType(View v) {
        if (v.getId() == R.id.tvSelectedBtn) {
            isMovie = false;
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
                retrieveGenres(v);
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, searchGenreFragment)
                .commit();
    }

    void retrieveGenres(View v) {
        HashMap<String, String> genresToConsider;
        Set selectedGenres = new HashSet<>();
        if (isMovie) {
            genresToConsider = movieGenres;
        } else {
            genresToConsider = tvGenres;
        }
        LinearLayout genreLayout = findViewById(R.id.genreLayout);
        CheckBox checkbox;
        for (String key: genresToConsider.keySet()) {
            checkbox = (CheckBox) genreLayout.findViewWithTag(key);
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
                retrieveProviders(v);
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, movieProviderFragment)
                .commit();
    }

    void retrieveProviders(View v) {
        Set selectedProviders = new HashSet<>();

        selectedProvidersString = "";
        LinearLayout genreLayout = findViewById(R.id.providerLayout);
        CheckBox checkbox;

        for (String key: providers.keySet()) {
            checkbox = (CheckBox) genreLayout.findViewWithTag(providers.get(key));
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
                    retrieveYearRatingRuntimeData(v);
                }
            }
        });

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.movie_content_frame, movieYearFragment)
                .commit();
    }

    void retrieveYearRatingRuntimeData(View v) {
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
        } else if (startYearInt != null && endYearInt != null && startYearInt > endYearInt) {
            Toast.makeText(this, "Start year cannot be greater than end year", Toast.LENGTH_SHORT).show();
        } else if (minRatingFloat != null && (minRatingFloat < 0 || minRatingFloat > 10)) {
            Toast.makeText(this, "Rating must be between 0 and 10", Toast.LENGTH_SHORT).show();
        } else if (minRuntimeInt != null && maxRuntimeInt != null && minRuntimeInt > maxRuntimeInt) {
            Toast.makeText(this, "Min runtime must be greater than max runtime", Toast.LENGTH_SHORT).show();
        } else {
            if (minRatingString.length() > 0) {
                apiParams.put("ratingString", "&vote_average.gte=" + minRatingString);
            }
            if (minRuntimeString.length() > 0) {
                apiParams.put("minRuntimeString", "&with_runtime.gte=" + minRuntimeString);
            }
            if (maxRuntimeString.length() > 0) {
                apiParams.put("minRuntimeString", "&with_runtime.lte=" + maxRuntimeString);
            }

            if (isMovie) {
                if (startYearString.length() > 0) {
                    apiParams.put("startDateString", "&primary_release_date.gte=" + startYearString + "-01-01");
                }
                if (endYearString.length() > 0) {
                    apiParams.put("endYearString", "&primary_release_date.lte=" + endYearString + "-12-31");
                }
            } else {
                if (startYearString.length() > 0) {
                    apiParams.put("startDateString", "&first_air_date.gte=" + startYearString + "-01-01");
                }
                if (endYearString.length() > 0) {
                    apiParams.put("endYearString", "&first_air_date.lte=" + endYearString + "-12-31");
                }
            }
            displayShows();
        }
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