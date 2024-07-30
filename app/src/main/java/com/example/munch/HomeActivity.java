package com.example.munch;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends BaseMenuActivity implements View.OnClickListener{

    private Button searchMovieButton;
    private Button randomShowBtn;
    private Button viewLikedBtn;
    private ProgressBar progressBar;

    private RequestQueue queue;
    private APIHelper apiHelper;
    private DBHelper dbHelper;

    private final String TAG_SEARCH_SHOW = "tagShow";
    private final String TAG_IMAGE_CONFIG = "tagImageConfig";
    private final String TAG_MOVIE_GENRE = "tagMovieGenre";
    private final String TAG_TV_GENRE = "tagTVGenre";
    private final String TAG_PROVIDER = "tagProvider";
    private final String URL_PREFIX_MOVIE_GENRE = "https://api.themoviedb.org/3/genre/movie/list?";
    private final String URL_PREFIX_TV_GENRE = "https://api.themoviedb.org/3/genre/tv/list?";
    private final String URL_PREFIX_CONFIG = "https://api.themoviedb.org/3/configuration?";
    private final String URL_PREFIX_PROVIDER = "https://api.themoviedb.org/3/watch/providers/movie?";
    private final String API = "&api_key=" + BuildConfig.movieAPIKey;

    private HashMap<String, String> imageConfigs;
    private HashMap<String, String> movieGenres;
    private HashMap<String, String> tvGenres;
    private HashMap<String, String> providers;

    private Boolean attemptedImageConfigRetrieval;
    private Boolean attemptedMovieGenreRetrieval;
    private Boolean attemptedTVGenreRetrieval;
    private Boolean attemptedProviderRetrieval;

    private FirestoreHelper firestoreHelper;
    private String userID;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialise the queue object for making API calls
        queue = Volley.newRequestQueue(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        firestoreHelper = new FirestoreHelper();

        searchMovieButton = findViewById(R.id.searchShowBtn);
        randomShowBtn = findViewById(R.id.randomShowBtn);
        viewLikedBtn = findViewById(R.id.viewLikedButton);
        progressBar = findViewById(R.id.loadingConfigsBar);

        searchMovieButton.setVisibility(View.GONE);
        randomShowBtn.setVisibility(View.GONE);
        viewLikedBtn.setVisibility(View.GONE);

        apiHelper = new APIHelper();
        dbHelper = new DBHelper(this);

        movieGenres = new HashMap<>();
        tvGenres = new HashMap<>();
        imageConfigs = new HashMap<>();
        providers = new HashMap<>();

        providers.put("Netflix", "");
        providers.put("Amazon Prime Video", "");
        providers.put("Disney Plus", "");
        providers.put("Stan", "");
        providers.put("BINGE", "");
        providers.put("ABC iview", "");
        providers.put("SBS On Demand", "");

        attemptedMovieGenreRetrieval = false;
        attemptedTVGenreRetrieval = false;
        attemptedImageConfigRetrieval = false;
        attemptedProviderRetrieval = false;

        retrieveImageConfigs();
        retrieveGenres(true);
        retrieveGenres(false);
        retrieveProviders();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.searchShowBtn) {
            intent = new Intent(HomeActivity.this, SearchMovieActivity.class);
            intent.putExtra("movieGenres", movieGenres);
            intent.putExtra("tvGenres", tvGenres);
            intent.putExtra("providers", providers);
        } else if (view.getId() == R.id.randomShowBtn) {
            intent = new Intent(HomeActivity.this, RandomShowActivity.class);
        } else {
            intent = new Intent(HomeActivity.this, ViewLikedActivity.class);
        }
        intent.putExtra("imageConfigs", imageConfigs);
        startActivity(intent);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
        if (queue != null) {
            queue.cancelAll(TAG_SEARCH_SHOW);
            queue.cancelAll(TAG_TV_GENRE);
            queue.cancelAll(TAG_MOVIE_GENRE);
            queue.cancelAll(TAG_PROVIDER);
            queue.cancelAll(TAG_IMAGE_CONFIG);
        }
    }

    private void handleAllRetrievalsAttempted() {
        if (!(attemptedTVGenreRetrieval
                && attemptedMovieGenreRetrieval
                && attemptedImageConfigRetrieval
                && attemptedProviderRetrieval)) {
            return;
        }
        DocumentReference userDataDoc = firestoreHelper.getUserDoc(userID);
        registration = userDataDoc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                // Hide loader
                progressBar.setVisibility(View.INVISIBLE);

                // Prepare buttons
                searchMovieButton.setVisibility(View.VISIBLE);
                searchMovieButton.setOnClickListener(HomeActivity.this);
                randomShowBtn.setVisibility(View.VISIBLE);
                randomShowBtn.setOnClickListener(HomeActivity.this);
                viewLikedBtn.setVisibility(View.VISIBLE);
                viewLikedBtn.setOnClickListener(HomeActivity.this);
            }
        });
    }

    private void setGenreRetrievalAttempt(Boolean isMovie) {
        if (isMovie) {
            attemptedMovieGenreRetrieval = true;
        } else {
            attemptedTVGenreRetrieval = true;
        }
    }

    private void retrieveImageConfigs() {
        if (dbHelper.shouldUpdate()) {
            // Retrieve the data then populate the db
            String url = URL_PREFIX_CONFIG + API;
            queue.cancelAll(TAG_IMAGE_CONFIG);

            StringRequest stringRequest = apiHelper.movieTVAPICall(url,
                    new HTTPListener() {
                        @Override
                        public void onResponseReceived(String response) {
                            try {
                                JSONObject responseJSON = new JSONObject(response);
                                JSONObject imageJSON  = responseJSON.getJSONObject("images");
                                imageConfigs.put("secure_base_url", imageJSON.getString("secure_base_url"));
                                imageConfigs.put("backdrop_sizes", imageJSON.getJSONArray("backdrop_sizes").toString());
                                imageConfigs.put("poster_sizes", imageJSON.getJSONArray("poster_sizes").toString());
                                dbHelper.addImageConfigs(imageConfigs);
                            } catch (JSONException e) {
                                Toast.makeText(HomeActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
                            }
                            attemptedImageConfigRetrieval = true;
                            handleAllRetrievalsAttempted();
                        }
                        @Override
                        public void onError(VolleyError error) {
                            Toast.makeText(HomeActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
                            attemptedImageConfigRetrieval = true;
                            handleAllRetrievalsAttempted();
                        }
                    });
            stringRequest.setTag(TAG_IMAGE_CONFIG);
            queue.add(stringRequest);
        }
        else {
            imageConfigs = dbHelper.getImageConfigs();
            attemptedImageConfigRetrieval = true;
            handleAllRetrievalsAttempted();
        }
    }

    private void retrieveGenres(Boolean isMovie) {
        if (!dbHelper.shouldUpdate()) {
            if (isMovie) {
                movieGenres = dbHelper.getGenres(true);
            } else {
                tvGenres = dbHelper.getGenres(false);
            }
            setGenreRetrievalAttempt(isMovie);
            handleAllRetrievalsAttempted();
            return;
        }
        // Retrieve the data then populate the db
        String url;
        Map<String, String> genreMap;
        if (isMovie) {
            url = URL_PREFIX_MOVIE_GENRE + API;
            genreMap = movieGenres;
            queue.cancelAll(TAG_MOVIE_GENRE);
        } else {
            url = URL_PREFIX_TV_GENRE + API;
            genreMap = tvGenres;
            queue.cancelAll(TAG_TV_GENRE);
        }

        StringRequest stringRequest = apiHelper.movieTVAPICall(url,
                new HTTPListener() {
                @Override
                public void onResponseReceived(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        JSONArray genresJSON  = responseJSON.getJSONArray("genres");
                        for (int i = 0; i < genresJSON.length(); i++) {
                            JSONObject genreJSON = genresJSON.getJSONObject(i);
                            genreMap.put(genreJSON.getString("id"), genreJSON.getString("name"));
                        }
                        dbHelper.addGenres(genreMap, isMovie);
                        setGenreRetrievalAttempt(isMovie);

                    } catch (JSONException e) {
                        Toast.makeText(HomeActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
                        setGenreRetrievalAttempt(isMovie);
                    }
                    handleAllRetrievalsAttempted();
                }
                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(HomeActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
                    setGenreRetrievalAttempt(isMovie);
                    handleAllRetrievalsAttempted();
                }
            });
        stringRequest.setTag(TAG_IMAGE_CONFIG);
        queue.add(stringRequest);
    }

    private void retrieveProviders() {
        if (!dbHelper.shouldUpdate()) {
            providers = dbHelper.getProviders();
            attemptedProviderRetrieval = true;
            handleAllRetrievalsAttempted();
        }

        String url = URL_PREFIX_PROVIDER + API;
        queue.cancelAll(TAG_PROVIDER);
        StringRequest stringRequest = apiHelper.movieTVAPICall(url,
                new HTTPListener() {
                @Override
                public void onResponseReceived(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        JSONArray providersJSON  = responseJSON.getJSONArray("results");
                        for (int i = 0; i < providersJSON.length(); i++) {
                            JSONObject providerJSON = providersJSON.getJSONObject(i);
                            String providerName = providerJSON.getString("provider_name");
                            if (providers.containsKey(providerName)) {
                                providers.put(
                                    providerName,
                                    providers.get(providerName) + "|" + providerJSON.getString("provider_id")
                                );
                            }
                        }
                        dbHelper.addProviders(providers);
                    } catch (JSONException e) {
                        Toast.makeText(HomeActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
                    }
                    attemptedProviderRetrieval = true;
                    handleAllRetrievalsAttempted();
                }
                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(HomeActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
                    attemptedProviderRetrieval = true;
                    handleAllRetrievalsAttempted();
                }
            });
        stringRequest.setTag(TAG_IMAGE_CONFIG);
        queue.add(stringRequest);
    }
}