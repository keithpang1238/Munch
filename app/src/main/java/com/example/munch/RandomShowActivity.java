package com.example.munch;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomShowActivity extends BaseMenuActivity implements View.OnClickListener{

    private JSONArray showArray;
    private Integer numViewed;

    private Integer[] moviePageNumbers;
    private Integer[] tvPageNumbers;
    private Integer currMoviePage;
    private Integer currTVPage;
    private Boolean attemptedToGetMoviePages;
    private Boolean attemptedToGetTVPages;
    private final Integer MAX_PAGES = 20;
    private Boolean randomIsMovie;
    private Boolean userGivenIsMovie;
    private JSONObject currShow;
    private String currShowID;
    private Boolean currShowIsMovie;

    private Button getNextRandom;
    public ProgressBar loadingShowFetchBar;
    private RequestQueue queue;
    private final String TAG_SEARCH_SHOW = "tagShow";
    private final String TAG_IMAGE_CONFIG = "tagImageConfig";
    private final String URL_PREFIX_MOVIE = "https://api.themoviedb.org/3/discover/movie?";
    private final String URL_PREFIX_TV = "https://api.themoviedb.org/3/discover/tv?";
    private final String API = "&api_key=" + BuildConfig.movieAPIKey;
    private final String REGION = "&watch_region=AU";
    private final String WATCH_TYPES = "&with_watch_monetization_types=free|ads|flatrate";

    private final String DEFAULT_URL_IMAGE_PREFIX = "https://image.tmdb.org/t/p/original/";
    private String url_image_prefix;

    private APIHelper apiHelper;
    private Random rand;

    private Map<String, String> imageConfigs;
    private Map<String, String> apiParams;

    private HashMap<String, JSONObject> likedMovies;
    private HashMap<String, JSONObject> likedTVShows;
    private HashSet<String> unlikedMovieIDs;
    private HashSet<String> unlikedTVShowIDs;

    private String userID;

    private FirestoreHelper firestoreHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_show);

        likedMovies = new HashMap<>();
        likedTVShows = new HashMap<>();
        unlikedMovieIDs = new HashSet<>();
        unlikedTVShowIDs = new HashSet<>();

        attemptedToGetMoviePages = false;
        attemptedToGetTVPages = false;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        firestoreHelper = new FirestoreHelper();

        DocumentReference userDataDoc = firestoreHelper.getUserDoc(userID);
        userDataDoc.addSnapshotListener(this, (documentSnapshot, e) -> {
            if (documentSnapshot == null) {
                return;
            }
            List<String> likedMovieIDs = (List<String>) documentSnapshot.get("LikedMovies");
            for (String movieID : likedMovieIDs) {
                likedMovies.put(movieID, new JSONObject());
            }

            List<String> likedTVShowIDs = (List<String>) documentSnapshot.get("LikedTVShows");
            for (String tvShowID : likedTVShowIDs) {
                likedTVShows.put(tvShowID, new JSONObject());
            }
        });



        queue = Volley.newRequestQueue(this);
        apiHelper = new APIHelper();
        imageConfigs = new HashMap<>();

        getNextRandom = findViewById(R.id.getNextRandom);
        getNextRandom.setOnClickListener(this);
        rand = new Random();
        rand.setSeed(999);

        loadingShowFetchBar = findViewById(R.id.loadingShowFetchBar);
        loadingShowFetchBar.setVisibility(View.VISIBLE);


        Intent intent = getIntent();

        try {
            imageConfigs = (HashMap<String, String>) intent.getSerializableExtra("imageConfigs");
            generateURLImagePrefix();
        } catch (Error e) {
            url_image_prefix = DEFAULT_URL_IMAGE_PREFIX;
        }

        apiParams = (HashMap<String, String>) intent.getSerializableExtra("apiParams");


        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("isMovie")) {
                userGivenIsMovie = extras.getBoolean("isMovie");
            }
        }
        newShowFetch(getIsMovie());
    }

    private Boolean getIsMovie() {
        if (userGivenIsMovie != null) {
            return userGivenIsMovie;
        }
        randomIsMovie = rand.nextBoolean();
        return randomIsMovie;
    }

    private void generateURLImagePrefix() {
        try {
            JSONArray poster_sizes = new JSONArray(imageConfigs.get("poster_sizes"));
            if (poster_sizes.length() > 0) {
                url_image_prefix = imageConfigs.get("secure_base_url") + poster_sizes.getString(poster_sizes.length() - 1);
            }
            else {
                Toast.makeText(RandomShowActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(RandomShowActivity.this, "Using default image url", Toast.LENGTH_SHORT).show();
        }
    }


    // Source: https://stackoverflow.com/questions/5531130/an-efficient-way-to-shuffle-a-json-array-in-java
    private static void shuffleJsonArray (JSONArray array) throws JSONException {
        // Implementing Fisher–Yates shuffle
        Random rnd = new Random();
        for (int i = array.length() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            // Simple swap
            Object object = array.get(j);
            array.put(j, array.get(i));
            array.put(i, object);
        }
    }

    private static void shuffleArray(Integer[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Integer a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public void newShowFetch(Boolean isMovie) {
        numViewed = 0;
        String URL_PREFIX;
        String pageString = "";

        if (isMovie) {
            URL_PREFIX = URL_PREFIX_MOVIE;
            if (moviePageNumbers != null && moviePageNumbers.length > 0) {
                pageString = "&page=" + moviePageNumbers[(currMoviePage + 1) % moviePageNumbers.length].toString();
                currMoviePage++;
            }
        } else {
            URL_PREFIX = URL_PREFIX_TV;
            if (tvPageNumbers != null && tvPageNumbers.length > 0) {
                pageString = "&page=" + tvPageNumbers[(currTVPage + 1) % tvPageNumbers.length].toString();
                currTVPage++;
            }
        }

        StringBuilder urlBuilder = new StringBuilder(URL_PREFIX + API + pageString + REGION + WATCH_TYPES);
        if (apiParams != null) {
            for (String key : apiParams.keySet()) {
                urlBuilder.append(apiParams.get(key));
            }
        }
        String url = urlBuilder.toString();
        queue.cancelAll(TAG_SEARCH_SHOW);

        StringRequest stringRequest = apiHelper.movieTVAPICall(url,
                new HTTPListener() {
                @Override
                public void onResponseReceived(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        if ((isMovie && attemptedToGetMoviePages) || (!isMovie && attemptedToGetTVPages)) {
                            showArray = responseJSON.getJSONArray("results");
                            if (showArray.length() > 0) {
                                shuffleJsonArray(showArray);
                                replaceFragment(showArray.getJSONObject(0));
                            }
                            else {
                                Toast.makeText(RandomShowActivity.this, "Requirements too strict", Toast.LENGTH_LONG).show();
                            }
                            if (loadingShowFetchBar != null) {
                                loadingShowFetchBar.setVisibility(View.INVISIBLE);
                            }
                            return;
                        }
                        if (!responseJSON.has("total_pages")) { // only one page of shows
                            return;
                        }
                        try {
                            int total = Math.min(responseJSON.getInt("total_pages"), MAX_PAGES);
                            if (isMovie && !attemptedToGetMoviePages) {
                                attemptedToGetMoviePages = true;
                                if (moviePageNumbers == null) {
                                    moviePageNumbers = generatePageNumbers(total);
                                    currMoviePage = 1;
                                    RandomShowActivity.this.randomIsMovie = true;
                                    newShowFetch(true);
                                }
                                return;
                            }
                            if (!isMovie && !attemptedToGetTVPages) {
                                attemptedToGetTVPages = true;
                                if (tvPageNumbers == null){
                                    tvPageNumbers = generatePageNumbers(total);
                                    currTVPage = 1;
                                    RandomShowActivity.this.randomIsMovie = false;
                                    newShowFetch(false);
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RandomShowActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(RandomShowActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        if (loadingShowFetchBar != null) {
                            loadingShowFetchBar.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(RandomShowActivity.this, "Movie API source is not responding", Toast.LENGTH_LONG).show();
                    if (loadingShowFetchBar != null) {
                        loadingShowFetchBar.setVisibility(View.INVISIBLE);
                    }
                }
            }
        );
        stringRequest.setTag(TAG_SEARCH_SHOW);
        queue.add(stringRequest);
    }

    private void replaceFragment(JSONObject movieObject) {
        try {
            movieObject.put("url_image_prefix", url_image_prefix);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(RandomShowActivity.this, "Could not load image", Toast.LENGTH_SHORT).show();
        }

        currShow = movieObject;
        currShowIsMovie = !movieObject.has("first_air_date");
        try {
            currShowID = currShow.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Boolean isLiked = (currShowIsMovie && likedMovies.containsKey(currShowID)) || (!currShowIsMovie && likedTVShows.containsKey(currShowID));

        ShowDisplayFragment fragment = ShowDisplayFragment.newInstance(replaceDateWithYear(movieObject).toString(), randomIsMovie, isLiked);
        fragment.setOnClickListener(v -> {
            if (v.getId() == R.id.overviewTxt && isOverviewEllipsized()) {
                displayOverviewPopup(v, movieObject.optString("overview"));
            } else if (v.getId() == R.id.likeButton) {
                handleUserLike();
            }
        });
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.random_movie_content_frame, fragment)
                .commit();
        numViewed++;
    }

    void handleUserLike() {
        ImageView likeButton = findViewById(R.id.likeButton);
        if (likeButton.getTag() != null && likeButton.getTag().equals(R.drawable.like_button)) {
            // User is liking the movie or tv show
            likeButton.setImageResource(R.drawable.like_button_filled);
            likeButton.setTag(R.drawable.like_button_filled);
            if (currShowIsMovie) {
                likedMovies.put(currShowID, currShow);
                unlikedMovieIDs.remove(currShowID);
            } else {
                likedTVShows.put(currShowID, currShow);
                unlikedTVShowIDs.remove(currShowID);
            }
        } else {
            // user is unliking the movie or tv show
            likeButton.setImageResource(R.drawable.like_button);
            likeButton.setTag(R.drawable.like_button);
            if (currShowIsMovie) {
                likedMovies.remove(currShowID);
                unlikedMovieIDs.add(currShowID);
            } else {
                likedTVShows.remove(currShowID);
                unlikedTVShowIDs.add(currShowID);
            }
        }

        if (currShow != null && currShow.has("id") && currShowIsMovie != null) {
            if (currShowID == null) {
                Toast.makeText(apiHelper, "Show has no id", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        Toast.makeText(this, "Could not get movie details", Toast.LENGTH_SHORT).show();
    }

    public static Integer[] generatePageNumbers(Integer total) {
        Integer [] pageNumbers = new Integer[total];
        for (int i = 0; i < total; i++) {
            pageNumbers[i] = i+1;
        }
        shuffleArray(pageNumbers);
        return pageNumbers;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.getNextRandom) {
            return;
        }
        if (showArray != null && numViewed < showArray.length()) {
            try {
                replaceFragment(showArray.getJSONObject(numViewed));
            } catch (JSONException e) {
                Toast.makeText(RandomShowActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }
        loadingShowFetchBar.setVisibility(View.VISIBLE);
        newShowFetch(getIsMovie());
    }

    private JSONObject replaceDateWithYear(JSONObject obj) {
        String movieTextToDisplay = obj.optString("release_date");
        String tvTextToDisplay = obj.optString("first_air_date");
        if (movieTextToDisplay.length() >= 4) {
            try {
                obj.put("release_date", movieTextToDisplay.substring(0, 4));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (tvTextToDisplay.length() >= 4) {
            try {
                obj.put("first_air_date", tvTextToDisplay.substring(0, 4));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    void displayOverviewPopup(View v, String overview) {
        if (v.getId() != R.id.overviewTxt) {
            return;
        }

        Dialog dialog = new AlertDialog.Builder(RandomShowActivity.this)
                .setMessage(overview).show();

        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.copyFrom(dialog.getWindow().getAttributes());

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.50);

        lp.dimAmount=0.8f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

    }

    Boolean isOverviewEllipsized() {
        TextView overviewTxt = findViewById(R.id.overviewTxt);
        Layout l = overviewTxt.getLayout();
        if (l == null) {
            return false;
        }
        int lines = l.getLineCount();
        return lines > 0 && l.getEllipsisCount(lines-1) > 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Write new liked movies to database
        firestoreHelper.handleUserLike(userID, unlikedMovieIDs, likedMovies, true);
        // Write new liked tv shows to database
        firestoreHelper.handleUserLike(userID, unlikedTVShowIDs, likedTVShows, false);

    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG_SEARCH_SHOW);
            queue.cancelAll(TAG_IMAGE_CONFIG);
        }
    }
}