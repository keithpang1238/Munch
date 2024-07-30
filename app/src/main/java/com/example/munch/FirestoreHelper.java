package com.example.munch;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FirestoreHelper {

    private final FirebaseFirestore mStore;

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

    public Query getArrayContainsQuery(Boolean isMovie, String userID, String arrayField) {
        return mStore.collection(getShowCollection(isMovie)).whereArrayContains(arrayField, userID);
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
        String showTitleField = getShowTitleField(isMovie);

        DocumentReference userDataDoc = getUserDoc(userID);

        if (unlikedShowIDs != null) {
            for (String key : unlikedShowIDs) {
                //userDataDoc.update(likedShowField, FieldValue.arrayRemove(key));

                DocumentReference showRef = mStore.collection(showCollection).document(key);

                mStore.runTransaction((Transaction.Function<Void>) transaction -> {
                    transaction.update(userDataDoc, likedShowField, FieldValue.arrayRemove(key));
                    transaction.update(showRef, "UsersWhoLike", FieldValue.arrayRemove(userID));

                    // Success
                    return null;
                }).addOnFailureListener(e -> System.out.println(e.getMessage()));
            }
        }

        if (likedShows != null) {
            for (String key: likedShows.keySet()) {
                JSONObject showObject = likedShows.get(key);
                if (showObject != null) {
                    // userDataDoc.update(likedShowField, FieldValue.arrayUnion(key));
                    DocumentReference showRef = mStore.collection(showCollection).document(key);

                    mStore.runTransaction((Transaction.Function<Void>) transaction -> {
                        DocumentSnapshot showSnapshot = transaction.get(showRef);
                        transaction.update(userDataDoc, likedShowField, FieldValue.arrayUnion(key));
                        if (showSnapshot.exists()) {
                            transaction.update(showRef, "UsersWhoLike", FieldValue.arrayUnion(userID));
                        } else {
                            Map<String, Object> newShow = new HashMap<>();
                            String[] usersWhoLike = {userID};

                            try {
                                newShow.put("Name", showObject.getString(showTitleField));
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
                            transaction.set(showRef, newShow);
                        }

                        // Success
                        return null;
                    }).addOnFailureListener(e -> System.out.println(e.getMessage()));
                }
            }
        }
    }
}
