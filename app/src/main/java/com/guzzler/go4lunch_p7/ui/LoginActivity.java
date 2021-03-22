package com.guzzler.go4lunch_p7.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.guzzler.go4lunch_p7.R;
import com.guzzler.go4lunch_p7.api.firebase.UserHelper;

import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends Activity {

    //FOR DATA
    // 1 - Identifier for Sign-In Activity
    private final int RC_SIGN_IN = 123;
    boolean doubleBackToExitPressedOnce = false;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.log_activity_coordinator_layout)

    // Un CoordinatorLayout est une fonctionnalité super cool de Material Design qui aide à créer des mises en page attrayantes et harmonisées.
            CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        ButterKnife.bind(this);

        // 3 - Launch Sign-In Activity
        this.startSignInActivity();
    }

    // --------------------
    // NAVIGATION
    // --------------------

    // 2 - Launch Sign-In Activity
    private void startSignInActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.drawable.logo_loggin)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build(), // SUPPORT GOOGLE
                                new AuthUI.IdpConfig.GitHubBuilder().build(), // SUPPORT GITHUB
                                new AuthUI.IdpConfig.TwitterBuilder().build(), // SUPPORT TWITTER
                                new AuthUI.IdpConfig.EmailBuilder().build(), // SUPPORT EMAIL
                                new AuthUI.IdpConfig.FacebookBuilder().build())) // SUPPORT FACEBOOK

                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RC_SIGN_IN);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to return to login", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createWorkmate();
                this.finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else { // ERRORS

                // Show Snack Bar with a message
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    protected OnFailureListener onFailureListener() {
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

    @Nullable
    private FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }


    // Méthode utilisée pour créer l'utilisateur qui s'est connecté avec succès et sutout qui n'existe pas déjà
    private void createWorkmate() {

        // TODO : trouver regex pour ne garder que ce qu'il y a avant le @ dans une adresse mail

        if (getCurrentUser() != null) {
            UserHelper.getWorkmate(getCurrentUser().getUid()).addOnCompleteListener(UserTask -> {
                        if (UserTask.isSuccessful()) {
                            if (!UserTask.getResult().exists()) {
                                String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
                                if (getCurrentUser().getDisplayName() != null) {
                                    String name = getCurrentUser().getDisplayName();
                                    String uid = getCurrentUser().getUid();
                                    UserHelper.createWorkmate(uid, urlPicture, name).addOnFailureListener(onFailureListener());
                                } else {
                                    String name = getCurrentUser().getEmail();
                                    String uid = getCurrentUser().getUid();
                                    UserHelper.createWorkmate(uid, urlPicture, name).addOnFailureListener(onFailureListener());
                                }

                            }
                        }
                    }
            );
        }
    }


    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
}