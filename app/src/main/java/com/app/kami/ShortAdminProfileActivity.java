package com.app.kami;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by israe_000 on 4/8/2015.
 */
public class ShortAdminProfileActivity extends Activity {
    private UserLoginTask mAuthTask = null;
    Context ctx;

    private View mProgressView;
    private View mLoginFormView;

    EditText username;
    EditText adminKey;
    Button finish;

    int adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        ctx = getApplicationContext();

        username = (EditText)findViewById(R.id.admin_profile_username);
        adminKey = (EditText)findViewById(R.id.admin_profile_key);

        finish = (Button)findViewById(R.id.admin_profile_finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.admin_profile_login_progress);
        mLoginFormView = findViewById(R.id.admin_profile_login_form);

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        username.setError(null);
        adminKey.setError(null);

        // Store values at the time of the login attempt.
        String name = username.getText().toString();
        String key = username.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid email address.
        if (TextUtils.isEmpty(name)) {
            username.setError(getString(R.string.error_field_required));
            focusView = username;
            cancel = true;
        }

        if (TextUtils.isEmpty(key)) {
            adminKey.setError(getString(R.string.error_field_required));
            focusView = adminKey;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(name, key);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        JSONParser jsonParser = new JSONParser();
        private static final String login = "http://youngstroke.org/Genius/KamiPHP/create_admin.php";

        private final String mUsername;
        private final String mKey;
        String message;

        UserLoginTask(String name, String key) {

            mUsername = name;
            mKey = key;
        }

        @Override
        protected String doInBackground(Void... params) {
            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("name", mUsername));
            args.add(new BasicNameValuePair("key", mKey));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(login,"POST", args);

            // check log cat for response
            Log.d("Create Response", json.toString());

            try {
                message = json.getString("message");
                if(message.equals("User succesfully created")){
                    adminId = json.getInt("admin_id");
                }
                return message;

            } catch (JSONException e) {
                e.printStackTrace();
                return "JSON Exception";
            }
        }

        @Override
        protected void onPostExecute(final String message) {
            mAuthTask = null;
            showProgress(false);

            switch (message) {
                case "User successfully created":
                    Toast.makeText(ctx, "User successfully created", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ctx, MainUserActivity.class);
                    intent.putExtra("admin_id", adminId);
                    startActivity(intent);
                    finish();

                    break;
                case "An error occurred":
                    Toast.makeText(ctx, "Error", Toast.LENGTH_SHORT).show();

                    break;
                case "JSON Exception":
                    // Handle JSON Exception here
                    Toast.makeText(ctx, "JSON Exception", Toast.LENGTH_SHORT).show();

                    break;
                default:
                    username.setError(getString(R.string.error_incorrect_password));
                    username.requestFocus();
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
