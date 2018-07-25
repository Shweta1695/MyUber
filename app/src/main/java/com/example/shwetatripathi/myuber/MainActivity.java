package com.example.shwetatripathi.myuber;

import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginButton;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.Session;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.error.ErrorParser;
import com.uber.sdk.rides.client.model.UserProfile;
import com.uber.sdk.rides.client.services.RidesService;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;
import static com.uber.sdk.android.core.utils.Preconditions.checkState;

public class MainActivity extends AppCompatActivity {
    Button button1;




        public static final String CLIENT_ID = "bLwET2iqo-_e3StBnBA18ipEPx1kVH5P";
        public static final String REDIRECT_URI = "com.example.shwetatripathi.myuber.uberauth://redirect";
        public static final String SERVER_TOKEN="LWDgVstT_fcFvy0qD8b9QuaMHfUrxQPfsc4fCjvV";

        private static final String LOG_TAG = "MainActivity";

        private static final int LOGIN_BUTTON_CUSTOM_REQUEST_CODE = 1112;
        private static final int CUSTOM_BUTTON_REQUEST_CODE = 1113;


        private LoginButton blackButton;

        private Button customButton;
        private AccessTokenStorage accessTokenStorage;
        private LoginManager loginManager;
        private SessionConfiguration configuration;

        @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            configuration = new SessionConfiguration.Builder()
                    .setClientId(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .setScopes(Arrays.asList(Scope.RIDE_WIDGETS,Scope.PROFILE))
                    .build();

            validateConfiguration(configuration);

            accessTokenStorage = new AccessTokenManager(this);

             //Create a button using a custom AccessTokenStorage
            //Custom Scopes are set using XML for this button as well in R.layout.activity_sample
            blackButton = (LoginButton) findViewById(R.id.uber_button_black);
            blackButton.setAccessTokenManager((AccessTokenManager) accessTokenStorage)
                    .setCallback(new SampleLoginCallback())
                    .setSessionConfiguration(configuration)
                    .setRequestCode(LOGIN_BUTTON_CUSTOM_REQUEST_CODE);


            //Use a custom button with an onClickListener to call the LoginManager directly
            loginManager = new LoginManager((AccessTokenManager) accessTokenStorage,
                    new SampleLoginCallback(),
                    configuration,
                    CUSTOM_BUTTON_REQUEST_CODE);

            customButton = findViewById(R.id.custom_uber_button);
            customButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginManager.login(MainActivity.this);
                }
            });
        }

        @Override
        protected void onResume () {
            super.onResume();
            if (loginManager.isAuthenticated()) {

                loadProfileInfo();
            }
            Log.d(LOG_TAG,"Login error occured ");
        }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            Log.i(LOG_TAG, String.format("onActivityResult requestCode:[%s] resultCode [%s]",
                    requestCode, resultCode));

            //Allow each a chance to catch it.

            blackButton.onActivityResult(requestCode, resultCode, data);

            loginManager.onActivityResult(this, requestCode, resultCode, data);
        }

        private class SampleLoginCallback implements LoginCallback {

            @Override
            public void onLoginCancel() {
                Toast.makeText(MainActivity.this, "cancel", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLoginError(@NonNull AuthenticationError error) {
                Toast.makeText(MainActivity.this,
                        "error", Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onLoginSuccess(@NonNull AccessToken accessToken) {
                Toast.makeText(MainActivity.this,"SUCCESS LOGIN", Toast.LENGTH_LONG).show();
                loadProfileInfo();
            }

            @Override
            public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {
                Toast.makeText(MainActivity.this, "auth",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }

        private void loadProfileInfo () {
            Session session = loginManager.getSession();
            RidesService service = UberRidesApi.with(session).build().createService();

            service.getUserProfile()
                    .enqueue(new Callback<UserProfile>() {
                        @Override
                        public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Greetings hello", Toast.LENGTH_LONG).show();
                            } else {
                                ApiError error = ErrorParser.parseError(response);
                                Toast.makeText(MainActivity.this, error.getClientErrors().get(0).getTitle(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserProfile> call, Throwable t) {

                        }
                    });
        }



        /**
         * Validates the local variables needed by the Uber SDK used in the sample project
         * @param configuration
         */
        private void validateConfiguration (SessionConfiguration configuration){
            String nullError = "%s must not be null";
            String sampleError = "Please update your %s in the gradle.properties of the project before " +
                    "using the Uber SDK Sample app. For a more secure storage location, " +
                    "please investigate storing in your user home gradle.properties ";

            checkNotNull(configuration, String.format(nullError, "SessionConfiguration"));
            checkNotNull(configuration.getClientId(), String.format(nullError, "Client ID"));
            checkNotNull(configuration.getRedirectUri(), String.format(nullError, "Redirect URI"));
            checkState(!configuration.getClientId().equals("insert_your_client_id_here"),
                    String.format(sampleError, "Client ID"));
            checkState(!configuration.getRedirectUri().equals("insert_your_redirect_uri_here"),
                    String.format(sampleError, "Redirect URI"));
        }
    }
