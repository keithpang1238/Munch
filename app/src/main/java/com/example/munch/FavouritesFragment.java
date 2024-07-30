package com.example.munch;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;


public class FavouritesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match

    private String userID;
    private Boolean isMovie;
    private FirestoreHelper firestoreHelper;
    private FirestoreRecyclerAdapter adapter;
    private RecyclerView mFirestoreList;


    public FavouritesFragment(String userID, Boolean isMovie) {
        this.userID = userID;
        this.isMovie = isMovie;
        firestoreHelper = new FirestoreHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_favourites, container, false);
        mFirestoreList = root.findViewById(R.id.showRecyclerView);

        Query query = firestoreHelper.getArrayContainsQuery(isMovie, userID, "UsersWhoLike");
        FirestoreRecyclerOptions<ShowModel> options = new FirestoreRecyclerOptions.Builder<ShowModel>()
                .setQuery(query, ShowModel.class).build();
        adapter = new FirestoreRecyclerAdapter<ShowModel, ShowsViewHolder>(options) {
            @NonNull
            @Override
            public ShowsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_list_item_single, parent, false);
                return new ShowsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull @NotNull ShowsViewHolder holder, int position, @NonNull @NotNull ShowModel model) {
                holder.showName.setText(model.getName());
                String showID = getSnapshots().getSnapshot(position).getId();
                holder.deleteLikeButton.setTag(showID);
                holder.overview = model.getOverview();
            }
        };

        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mFirestoreList.setAdapter(adapter);

        return root;
    }

    private class ShowsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView showName;
        private final Button deleteLikeButton;
        private String overview;

        public ShowsViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            showName = itemView.findViewById(R.id.nameListText);
            deleteLikeButton = itemView.findViewById(R.id.deleteLikeButton);
            deleteLikeButton.setOnClickListener(this);
            showName.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.deleteLikeButton) {
                String showID = view.getTag().toString();
                HashSet<String> unlikedIDs = new HashSet<>();
                unlikedIDs.add(showID);
                firestoreHelper.handleUserLike(userID, unlikedIDs, null, isMovie);
            } else if (view.getId() == R.id.nameListText) {
                Dialog dialog = new AlertDialog.Builder(getActivity())
                        .setMessage(overview).setTitle(showName.getText().toString()).show();

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
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }
}