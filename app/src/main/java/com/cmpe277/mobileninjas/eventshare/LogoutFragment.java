package com.cmpe277.mobileninjas.eventshare;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class LogoutFragment extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener{
	
	public LogoutFragment(){}
    private GoogleApiClient mGoogleApiClient;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.activity_login, container, false);

       GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .enableAutoManage(new FragmentActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Toast.makeText(getActivity().getApplicationContext(), "Successfully Logged Out", Toast.LENGTH_LONG).show();
                    }
                });

//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.disconnect();
//        }



        return rootView;
    }

//    public void clearCookies(){
//        CookieSyncManager.createInstance(GooglePlusActivity.this);
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.removeAllCookie();
//    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("PM", "onConnectionFailed:" + connectionResult);
    }
}
