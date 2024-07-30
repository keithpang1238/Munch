package com.example.munch;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowDisplayFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "showJSONObjectString";
    private static final String ARG_PARAM2 = "isMovie";
    private static final String ARG_PARAM3 = "isLiked";

    private JSONObject showJSONObject;
    private FragmentClickListener listener;

    private TextView movieTitleTxt;
    private TextView ratingTxt;
    private TextView yearTxt;
    private TextView overviewTxt;
    private ImageView posterImage;

    private Boolean isMovie;
    private Boolean isLiked;

    public ShowDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param showObjectString stringified JSON Object representing movie or tv show.
     * @return A new instance of fragment ShowDisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowDisplayFragment newInstance(String showObjectString, Boolean isMovie, Boolean isLiked) {
        ShowDisplayFragment fragment = new ShowDisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, showObjectString);
        args.putString(ARG_PARAM2, isMovie.toString());
        args.putString(ARG_PARAM3, isLiked.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        try {
            showJSONObject = new JSONObject(args.getString(ARG_PARAM1));
            isMovie = Boolean.parseBoolean(args.getString(ARG_PARAM2));
            isLiked = Boolean.parseBoolean(args.getString(ARG_PARAM3));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_show_display, container, false);

        ImageView likeButton = rootView.findViewById(R.id.likeButton);

        if (isLiked) {
            likeButton.setImageResource(R.drawable.like_button_filled);
            likeButton.setTag(R.drawable.like_button_filled);
        } else {
            likeButton.setImageResource(R.drawable.like_button);
            likeButton.setTag(R.drawable.like_button);
        }

        likeButton.setOnClickListener(this);

        if (showJSONObject != null) {
            movieTitleTxt = rootView.findViewById(R.id.movieTitleTxt);
            ratingTxt = rootView.findViewById(R.id.ratingTxt);
            yearTxt = rootView.findViewById(R.id.releaseYearTxt);
            overviewTxt = rootView.findViewById(R.id.overviewTxt);
            posterImage = rootView.findViewById(R.id.posterImage);

            if (isMovie) {
                setText(movieTitleTxt, "title", "Title not found");
                setText(yearTxt, "release_date", "Unknown year");
            } else {
                setText(movieTitleTxt, "name", "Title not found");
                setText(yearTxt, "first_air_date", "Unknown year");

            }

            setText(ratingTxt, "vote_average", "N/A");
            setText(overviewTxt, "overview", "No description provided.");

            overviewTxt.setOnClickListener(this);

            if (showJSONObject.has("url_image_prefix") && showJSONObject.has("poster_path")) {
                try {
                    String full_poster_url = showJSONObject.getString("url_image_prefix") +
                            showJSONObject.getString("poster_path");
                    Picasso.get().load(full_poster_url).into(posterImage);
                } catch (JSONException e) {
                    Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                posterImage.setImageResource(R.drawable.no_image_found);
            }
        }
        return rootView;
    }


    private void setText(TextView textView, String key, String altText) {
        String textToDisplay = showJSONObject.optString(key);
        if (!textToDisplay.isEmpty()) {
            textView.setText(textToDisplay);
        } else {
            textView.setText(altText);
        }
    }

    public void setOnClickListener(FragmentClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view);
    }
}