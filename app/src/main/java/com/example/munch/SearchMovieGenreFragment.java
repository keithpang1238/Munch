package com.example.munch;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchMovieGenreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMovieGenreFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "genres";

    private HashMap<String, String> genres;

    private FragmentClickListener listener;

    public SearchMovieGenreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param genres HashMap of genres to display.
     * @return A new instance of fragment SearchMovieGenreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchMovieGenreFragment newInstance(HashMap<String, String> genres) {
        SearchMovieGenreFragment fragment = new SearchMovieGenreFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, genres);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            genres = (HashMap<String, String>) bundle.getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_show_genre, container, false);
        LinearLayout genreLayout = rootView.findViewById(R.id.genreLayout);
        for(String key : genres.keySet()) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(genres.get(key));
            cb.setTag(key);
            genreLayout.addView(cb);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            Button genreNextBtn = activity.findViewById(R.id.genreNextBtn);
            genreNextBtn.setOnClickListener(this);
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