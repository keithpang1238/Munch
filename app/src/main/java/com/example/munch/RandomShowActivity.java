package com.example.munch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final Integer MAX_PAGES = 15;
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
    private final String URL_PREFIX_CONFIG = "https://api.themoviedb.org/3/configuration?";
    private final String API = "&api_key=" + BuildConfig.movieAPIKey;
    private final String REGION = "&watch_region=AU";

    private final String DEFAULT_URL_IMAGE_PREFIX = "https://image.tmdb.org/t/p/original/";
    private String url_image_prefix;

    private APIHelper apiHelper;
    private Random rand;

    private DBHelper dbHelper;
    private Map<String, String> imageConfigs;
    private Map<String, String> apiParams;

    private HashMap<String, JSONObject> likedMovies;
    private HashMap<String, JSONObject> likedTVShows;
    private HashSet<String> unlikedMovieIDs;
    private HashSet<String> unlikedTVShowIDs;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private DocumentReference userDataDoc;




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

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        userDataDoc = mStore.collection("users").document(userID);
        ListenerRegistration registration = userDataDoc.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {
                            List<String> likedMovieIDs = (List<String>) documentSnapshot.get("LikedMovies");
                            for (String movieID : likedMovieIDs) {
                                likedMovies.put(movieID, new JSONObject());
                            }

                            List<String> likedTVShowIDs = (List<String>) documentSnapshot.get("LikedTVShows");
                            for (String tvShowID : likedTVShowIDs) {
                                likedTVShows.put(tvShowID, new JSONObject());
                            }

                        }
                    }
                });



        queue = Volley.newRequestQueue(this);
        apiHelper = new APIHelper();
        dbHelper = new DBHelper(this);
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

        StringBuilder urlBuilder = new StringBuilder(URL_PREFIX + API + pageString + REGION);
        if (apiParams != null) {
            for (String key : apiParams.keySet()) {
                urlBuilder.append(apiParams.get(key));
            }
        }
        String url = urlBuilder.toString();
        System.out.println(url);

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
                                Toast.makeText(RandomShowActivity.this, "Requirements too strict ma dude", Toast.LENGTH_LONG).show();
                            }
                            if (loadingShowFetchBar != null) {
                                loadingShowFetchBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        else if (responseJSON.has("total_pages")) {
                            try {
                                Integer total = responseJSON.getInt("total_pages");
                                total = Math.min(total, MAX_PAGES);
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

        currShow = movieObject;
        currShowIsMovie = !movieObject.has("first_air_date");
        try {
            currShowID = currShow.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Boolean isLiked = (currShowIsMovie && likedMovies.containsKey(currShowID)) || (!currShowIsMovie && likedTVShows.containsKey(currShowID));

        ShowDisplayFragment fragment = ShowDisplayFragment.newInstance(replaceDateWithYear(movieObject).toString(), randomIsMovie, isLiked);
        fragment.setOnClickListener(new FragmentClickListener(){
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.overviewTxt && isOverviewEllipsized()) {
                    displayOverviewPopup(v, movieObject.optString("overview"));
                } else if (v.getId() == R.id.likeButton) {
                    handleUserLike(v);
                }
            }
        });
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.random_movie_content_frame, fragment)
                .commit();
        numViewed++;
    }

    void handleUserLike(View v) {
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
            if (currShowID != null) {
                Toast.makeText(this, currShowID + " " + currShowIsMovie, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(apiHelper, "Show has no id", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Could not get movie deets", Toast.LENGTH_SHORT).show();
        }
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

    Boolean isOverviewEllipsized() {
        TextView overviewTxt = findViewById(R.id.overviewTxt);
        Layout l = overviewTxt.getLayout();
        if (l != null) {
            int lines = l.getLineCount();
            return lines > 0 && l.getEllipsisCount(lines-1) > 0;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Write new liked movies to database
        DocumentReference userDataDoc = mStore.collection("users").document(userID);

        for (String key : unlikedMovieIDs) {
            userDataDoc.update("LikedMovies", FieldValue.arrayRemove(key));

            DocumentReference movieRef = mStore.collection("movies").document(key);
            movieRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            movieRef.update("UsersWhoLike", FieldValue.arrayRemove(userID));
                        }
                    }
                }
            });
        }

        for (String key : unlikedTVShowIDs) {
            userDataDoc.update("LikedTVShows", FieldValue.arrayRemove(key));

            DocumentReference tvShowRef = mStore.collection("tvShows").document(key);
            tvShowRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            tvShowRef.update("UsersWhoLike", FieldValue.arrayRemove(userID));
                        }
                    }
                }
            });
        }

        for (String key: likedMovies.keySet()) {
            JSONObject showObject = likedMovies.get(key);
            if (showObject != null) {
                userDataDoc.update("LikedMovies", FieldValue.arrayUnion(key));
                DocumentReference movieRef = mStore.collection("movies").document(key);
                movieRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                movieRef.update("UsersWhoLike", FieldValue.arrayUnion(userID));
                            } else {
                                Map<String, Object> newMovie = new HashMap<>();
                                String[] usersWhoLike = {userID};

                                try {
                                    newMovie.put("Name", showObject.getString("title"));
                                } catch (JSONException jsonException) {
                                    newMovie.put("Name", "Unknown");
                                    jsonException.printStackTrace();
                                }

                                try {
                                    newMovie.put("Overview", showObject.getString("overview"));
                                } catch (JSONException jsonException) {
                                    newMovie.put("Overview", "Unknown");
                                    jsonException.printStackTrace();
                                }

                                newMovie.put("UsersWhoLike", Arrays.asList(usersWhoLike));
                                mStore.collection("movies").document(key).set(newMovie);
                            }
                        } else {
                            System.out.println("Something went wrong with finding show in db");
                        }
                    }
                });
            }
        }

        for (String key: likedTVShows.keySet()) {
            JSONObject showObject = likedTVShows.get(key);
            if (showObject != null) {
                userDataDoc.update("LikedTVShows", FieldValue.arrayUnion(key));
                DocumentReference tvRef = mStore.collection("tvShows").document(key);

                tvRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                tvRef.update("UsersWhoLike", FieldValue.arrayUnion(userID));
                            } else {
                                Map<String, Object> newTVShow = new HashMap<>();
                                String[] usersWhoLike = {userID};

                                try {
                                    newTVShow.put("Name", showObject.getString("name"));
                                } catch (JSONException jsonException) {
                                    newTVShow.put("Name", "Unknown");
                                    jsonException.printStackTrace();
                                }

                                try {
                                    newTVShow.put("Overview", showObject.getString("overview"));
                                } catch (JSONException jsonException) {
                                    newTVShow.put("Overview", "Unknown");
                                    jsonException.printStackTrace();
                                }

                                newTVShow.put("UsersWhoLike", Arrays.asList(usersWhoLike));

                                mStore.collection("tvShows").document(key).set(newTVShow);                            }
                        } else {
                            System.out.println("Something went wrong with finding show in db");
                        }
                    }
                });
            }
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