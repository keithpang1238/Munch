package com.example.munch;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchMovieTypeFragment extends Fragment implements View.OnClickListener {

    private FragmentClickListener listener;

    public SearchMovieTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_show_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            Button movieSelectBtn = activity.findViewById(R.id.movieSelectedBtn);
            Button tvSelectBtn = activity.findViewById(R.id.tvSelectedBtn);
            movieSelectBtn.setOnClickListener(this);
            tvSelectBtn.setOnClickListener(this);
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