package com.example.firebase;

import android.app.Application;
import android.text.TextUtils;

import com.example.firebase.data.FirebaseRepository;
import com.google.firebase.FirebaseApp;

/**
 * Gan URL Realtime Database dung voi project (Console &gt; Realtime Database &gt; Data URL).
 * Neu de trong {@code firebase_database_url}: khong ep URL — de SDK mac dinh (thich hop DB theo vung *.firebasedatabase.app).
 */
public class MovieTicketApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        String url = getString(R.string.firebase_database_url).trim();
        if (TextUtils.isEmpty(url)) {
            FirebaseRepository.setDatabaseUrlOverride(null);
        } else {
            FirebaseRepository.setDatabaseUrlOverride(url);
        }
    }
}
