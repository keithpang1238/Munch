package com.example.munch;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchMovieYearFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMovieYearFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "isMovie";

    // TODO: Rename and change types of parameters
    private Boolean isMovie;
    private FragmentClickListener listener;

    public SearchMovieYearFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SearchMovieYearFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchMovieYearFragment newInstance(Boolean isMovie) {
        SearchMovieYearFragment fragment = new SearchMovieYearFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, isMovie);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isMovie = getArguments().getBoolean(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_search_movie_year, container, false);
        Activity activity = getActivity();
        root.post(new Runnable() {
            @Override
            public void run() {
                EditText startYearTxt = activity.findViewById(R.id.startYearTxt);
                EditText endYearTxt = activity.findViewById(R.id.endYearTxt);
                EditText minTimeTxt = activity.findViewById(R.id.minTimeTxt);
                EditText maxTimeTxt = activity.findViewById(R.id.maxTimeTxt);

                if (startYearTxt.getMeasuredWidth() > endYearTxt.getMeasuredWidth()) {
                    endYearTxt.setWidth(startYearTxt.getMeasuredWidth());
                } else {
                    startYearTxt.setWidth(endYearTxt.getMeasuredWidth());
                }

                if (minTimeTxt.getMeasuredWidth() > maxTimeTxt.getMeasuredWidth()) {
                    maxTimeTxt.setWidth(minTimeTxt.getMeasuredWidth());
                } else {
                    minTimeTxt.setWidth(maxTimeTxt.getMeasuredWidth());
                }
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            TextView yearText = activity.findViewById(R.id.yearText);
            TextView ratingText = activity.findViewById(R.id.ratingText);
            TextView runtimeText = activity.findViewById(R.id.runtimeText);
            Button submitBtn = activity.findViewById(R.id.submitBtn);

            submitBtn.setOnClickListener(this);

            ratingText.setText("What is your minimum acceptable rating?");
            if (isMovie) {
                yearText.setText("What release dates were you considering?");
                runtimeText.setText("How long do you want the movie to go for?");
            } else {
                yearText.setText("What first-air dates were you considering?");
                runtimeText.setText("How long do you want episodes to go for?");
            }
        }
    }

    public void setOnClickListener(FragmentClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }
}