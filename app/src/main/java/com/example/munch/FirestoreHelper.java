package com.example.munch;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FirestoreHelper {

    private FirebaseFirestore mStore;

    public FirestoreHelper() {
        mStore = FirebaseFirestore.getInstance();
    }

    private String getShowCollection(Boolean isMovie) {
        if (isMovie) {
            return "movies";
        }
        return "tvShows";
    }

    private String getLikedShowField(Boolean isMovie) {
        if (isMovie) {
            return "LikedMovies";
        }
        return "LikedTVShows";
    }

    private String getShowTitleField(Boolean isMovie) {
        if (isMovie) {
            return "title";
        }
        return "name";
    }

    public DocumentReference getUserDoc(String userID) {
        return mStore.collection("users").document(userID);
    }


    public void handleUserLike(
            String userID,
            HashSet<String> unlikedShowIDs,
            HashMap<String, JSONObject> likedShows,
            Boolean isMovie) {

        String showCollection = getShowCollection(isMovie);
        String likedShowField = getLikedShowField(isMovie);

        DocumentReference userDataDoc = getUserDoc(userID);

        if (unlikedShowIDs != null) {
            for (String key : unlikedShowIDs) {
                userDataDoc.update(likedShowField, FieldValue.arrayRemove(key));

                DocumentReference showRef = mStore.collection(showCollection).document(key);
                showRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                showRef.update("UsersWhoLike", FieldValue.arrayRemove(userID));
                            }
                        }
                    }
                });
            }
        }

        if (likedShows != null) {
            for (String key: likedShows.keySet()) {
                JSONObject showObject = likedShows.get(key);
                if (showObject != null) {
                    userDataDoc.update(likedShowField, FieldValue.arrayUnion(key));
                    DocumentReference showRef = mStore.collection(showCollection).document(key);
                    showRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    showRef.update("UsersWhoLike", FieldValue.arrayUnion(userID));
                                } else {
                                    Map<String, Object> newShow = new HashMap<>();
                                    String[] usersWhoLike = {userID};

                                    try {
                                        newShow.put("Name", showObject.getString("title"));
                                    } catch (JSONException jsonException) {
                                        newShow.put("Name", "Unknown");
                                        jsonException.printStackTrace();
                                    }

                                    try {
                                        newShow.put("Overview", showObject.getString("overview"));
                                    } catch (JSONException jsonException) {
                                        newShow.put("Overview", "Unknown");
                                        jsonException.printStackTrace();
                                    }

                                    newShow.put("UsersWhoLike", Arrays.asList(usersWhoLike));
                                    mStore.collection(showCollection).document(key).set(newShow);
                                }
                            } else {
                                System.out.println("Something went wrong with finding show in db");
                            }
                        }
                    });
                }
            }
        }

    }
}
