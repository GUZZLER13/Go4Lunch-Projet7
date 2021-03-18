package com.guzzler.go4lunch_p7.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.guzzler.go4lunch_p7.R;
import com.guzzler.go4lunch_p7.api.firebase.UserHelper;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SettingsActivity extends AppCompatActivity {
    protected SharedViewModel mSharedViewModel;
    @BindView(R.id.activity_main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.settings_switch)
    Switch mSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        mSharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        this.configureToolbar();
        this.retrieveUserSettings();
        this.setListenerAndFilters();
        this.setTitle(getString(R.string.settings_toolbar));
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // do something, the isChecked will be
            // true if the switch is in the On position
            UserHelper.updateUserSettings(getCurrentUser().getUid(), mSwitch.isChecked()).addOnSuccessListener(
                    updateTask -> {
                        Log.e("settingActivity", "settings Saved on firebase");
                        if (mSwitch.isChecked()) {
                            Toast.makeText(this, "NOTIFICATIONS ON", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "NOTIFICATIONS OFF", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void configureToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void retrieveUserSettings() {
        UserHelper.getWorkmatesCollection().document(getCurrentUser().getUid()).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.e("TAG", "Listen failed.", e);
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                Log.e("TAG", "Current data: " + documentSnapshot.getData());
                if (documentSnapshot.getData().get("notification").equals(true)) {
                    mSwitch.setChecked(true);
                } else {
                    mSwitch.setChecked(false);

                }
            } else {
                Log.e("TAG", "Current data: null");
            }
        });
    }

    private void setListenerAndFilters() {
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        });
    }

    @Nullable
    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

}


