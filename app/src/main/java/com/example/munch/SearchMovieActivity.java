package com.example.munch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

public class SearchMovieActivity extends BaseMenuActivity {

    private Map<String, String> searchPayload;

    private Boolean isMovie;
    private HashMap<String, String> movieGenres;
    private HashMap<String, String> tvGenres;
    private HashMap<String, String> imageConfigs;
    private HashMap<String, String> providers;

    private String selectedGenresString;   // Pipe-separated string of genre ids
    private String selectedProvidersString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_movie);

        Intent intent = getIntent();
        try {
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
        selectedGenresString = TextUtils.join("|", selectedGenres);

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
        selectedProvidersString = "";
        LinearLayout genreLayout = findViewById(R.id.providerLayout);
        CheckBox checkbox;
        for (String key: providers.keySet()) {
            checkbox = (CheckBox) genreLayout.findViewWithTag(providers.get(key));
            if (checkbox != null && checkbox.isChecked()) {
                selectedProvidersString += providers.get(key);
            }
        }
        displayShows();
    }

    void displayShows() {
        Intent intent;
        intent = new Intent(SearchMovieActivity.this, RandomShowActivity.class);
        intent.putExtra("genres", selectedGenresString);
        intent.putExtra("providers", selectedProvidersString);
        intent.putExtra("isMovie", isMovie);
        intent.putExtra("imageConfigs", imageConfigs);
        startActivity(intent);
    }

}