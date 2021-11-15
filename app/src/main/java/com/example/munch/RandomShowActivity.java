package com.example.munch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomShowActivity extends AppCompatActivity implements View.OnClickListener{

    private JSONArray showArray;
    private Integer numViewed;

    private Integer[] moviePageNumbers;
    private Integer[] tvPageNumbers;
    private Integer currMoviePage;
    private Integer currTVPage;
    private Boolean attemptedToGetMoviePages;
    private Boolean attemptedToGetTVPages;
    private final Integer MAX_PAGES = 30;
    private Boolean randomIsMovie;
    private Boolean userGivenIsMovie;

    private Button getNextRandom;
    public ProgressBar loadingShowFetchBar;
    private RequestQueue queue;
    private final String TAG_SEARCH_SHOW = "tagShow";
    private final String TAG_IMAGE_CONFIG = "tagImageConfig";
    private final String URL_PREFIX_MOVIE = "https://api.themoviedb.org/3/discover/movie?";
    private final String URL_PREFIX_TV = "https://api.themoviedb.org/3/discover/tv?";
    private final String URL_PREFIX_CONFIG = "https://api.themoviedb.org/3/configuration?";
    private final String API = "&api_key=" + BuildConfig.movieAPIKey;
    private final String REGION = "&watch_region=AU";
    private String genreString;
    private String providerString;

    private final String DEFAULT_URL_IMAGE_PREFIX = "https://image.tmdb.org/t/p/original/";
    private String url_image_prefix;

    private APIHelper apiHelper;
    private Random rand;

    private DBHelper dbHelper;
    private Map<String, String> imageConfigs;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_show);

        attemptedToGetMoviePages = false;
        attemptedToGetTVPages = false;

        queue = Volley.newRequestQueue(this);
        apiHelper = new APIHelper();
        dbHelper = new DBHelper(this);
        imageConfigs = new HashMap<>();

        getNextRandom = findViewById(R.id.getNextRandom);
        getNextRandom.setOnClickListener(this);
        rand = new Random();

        loadingShowFetchBar = findViewById(R.id.loadingShowFetchBar);
        loadingShowFetchBar.setVisibility(View.VISIBLE);

        genreString = "";
        providerString = "";

        Intent intent = getIntent();

        try {
            imageConfigs = (HashMap<String, String>) intent.getSerializableExtra("imageConfigs");
            generateURLImagePrefix();
        } catch (Error e) {
            url_image_prefix = DEFAULT_URL_IMAGE_PREFIX;
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("genres")) {
                genreString = "&with_genres=" + extras.getString("genres");
            }
            if (extras.containsKey("providers")) {
                providerString = "&with_watch_providers=" + extras.getString("providers");
            }
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
                System.out.println(url_image_prefix);
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
    private static JSONArray shuffleJsonArray (JSONArray array) throws JSONException {
        // Implementing Fisher–Yates shuffle
        Random rnd = new Random();
        for (int i = array.length() - 1; i > 0; i--)
        {
            int j = rnd.nextInt(i + 1);
            // Simple swap
            Object object = array.get(j);
            array.put(j, array.get(i));
            array.put(i, object);
        }
        return array;
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
        final String voteString = "&vote_average.gte=2";


        if (isMovie) {
            URL_PREFIX = URL_PREFIX_MOVIE;
            if (moviePageNumbers != null && moviePageNumbers.length > 0) {
                pageString = "&page=" + moviePageNumbers[(currMoviePage + 1) % moviePageNumbers.length].toString();
                currMoviePage++;
            }
        }
        else {
            URL_PREFIX = URL_PREFIX_TV;
            if (tvPageNumbers != null && tvPageNumbers.length > 0) {
                pageString = "&page=" + tvPageNumbers[(currTVPage + 1) % tvPageNumbers.length].toString();
                currTVPage++;
            }
        }

        String url = URL_PREFIX + API + pageString + voteString + genreString + providerString + REGION;
        queue.cancelAll(TAG_SEARCH_SHOW);

        StringRequest stringRequest = apiHelper.movieTVAPICall(url, this,
            new HTTPListener() {
                @Override
                public void onResponseReceived(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        if ((isMovie && attemptedToGetMoviePages) || (!isMovie && attemptedToGetTVPages)) {
                            showArray = responseJSON.getJSONArray("results");
                            if (showArray.length() > 0) {
                                showArray = shuffleJsonArray(showArray);
                                replaceFragment(showArray.getJSONObject(0));
                            }
                            else {
                                Toast.makeText(RandomShowActivity.this, "No response", Toast.LENGTH_LONG).show();
                            }
                            if (loadingShowFetchBar != null) {
                                loadingShowFetchBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        else if (responseJSON.has("total_pages")) {
                            try {
                                Integer total = responseJSON.getInt("total_pages");
                                total = Math.max(total, MAX_PAGES);
                                if (isMovie && !attemptedToGetMoviePages) {
                                    attemptedToGetMoviePages = true;
                                    if (moviePageNumbers == null) {
                                        moviePageNumbers = generatePageNumbers(total);
                                        currMoviePage = 1;
                                        RandomShowActivity.this.randomIsMovie = true;
                                        newShowFetch(true);

                                    }
                                } else if (!attemptedToGetTVPages) {
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
        ShowDisplayFragment fragment = ShowDisplayFragment.newInstance(movieObject.toString(), randomIsMovie);
        fragment.setOnClickListener(new FragmentClickListener(){
            @Override
            public void onClick(View v) {
                displayOverviewPopup(v, movieObject.optString("overview"));
            }
        });
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.random_movie_content_frame, fragment)
                .commit();
        numViewed++;
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
        if (view.getId() == R.id.getNextRandom) {
            if (showArray != null && numViewed < showArray.length()) {
                try {
                    replaceFragment(showArray.getJSONObject(numViewed));
                } catch (JSONException e) {
                    Toast.makeText(RandomShowActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            else {
                loadingShowFetchBar.setVisibility(View.VISIBLE);
                newShowFetch(getIsMovie());
            }
        }
    }

    void displayOverviewPopup(View v, String overview) {
        if (v.getId() == R.id.overviewTxt) {

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