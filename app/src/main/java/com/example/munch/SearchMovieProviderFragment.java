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
 * Use the {@link SearchMovieProviderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMovieProviderFragment extends Fragment implements View.OnClickListener{

    private static final String ARG_PARAM1 = "providers";

    private HashMap<String, String> providers;

    private FragmentClickListener listener;

    public SearchMovieProviderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SearchMovieProviderFragment.
     */
    public static SearchMovieProviderFragment newInstance(HashMap<String, String> providers) {
        SearchMovieProviderFragment fragment = new SearchMovieProviderFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, providers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            providers = (HashMap<String, String>) bundle.getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_movie_provider, container, false);
        LinearLayout providerLayout = rootView.findViewById(R.id.providerLayout);
        for(String key : providers.keySet()) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(key);
            cb.setTag(providers.get(key));
            providerLayout.addView(cb);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity != null) {
            Button providerNextBtn = activity.findViewById(R.id.providerNextBtn);
            providerNextBtn.setOnClickListener(this);
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