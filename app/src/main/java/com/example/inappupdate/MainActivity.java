package com.example.inappupdate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FakeAppUpdateManager mAppUpdateManager;
    private static final int REQUEST_APP_UPDATE = 130;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAppUpdateManager = new FakeAppUpdateManager(this);
        mAppUpdateManager.setUpdatePriority(2);
        mAppUpdateManager.setUpdateAvailable(5,AppUpdateType.FLEXIBLE);
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if(result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    mAppUpdateManager.startUpdateFlowForResult(result,
                            AppUpdateType.FLEXIBLE,
                            MainActivity.this,
                            REQUEST_APP_UPDATE);
                    mAppUpdateManager.userAcceptsUpdate();
                    mAppUpdateManager.downloadStarts();
                    mAppUpdateManager.downloadCompletes();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d("InAppUpdate","Failed "+e.toString());
            }
        });
    }

    private InstallStateUpdatedListener installStateUpdatedListener =new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletedUpdate();
            }
        }
    };

    @Override
    protected void onStop() {
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
        super.onStop();
    }

    private void showCompletedUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"New app ready to install",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppUpdateManager.completeUpdate();
            }
        });
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
            } else {
                Log.d("InAppUpdate","Activity Result");

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}