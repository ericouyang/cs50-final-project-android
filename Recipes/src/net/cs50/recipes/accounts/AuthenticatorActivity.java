package net.cs50.recipes.accounts;

import net.cs50.recipes.BaseActivity;
import net.cs50.recipes.R;
import net.cs50.recipes.util.HttpHelper;
import net.cs50.recipes.util.SyncUtils;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// activity to handle user authentication
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

	// static strings to faciliate cross class communication
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_PASS = "USER_PASS";

    // reference to account manager
    private AccountManager mAccountManager;
    
    // type of token we're trying to get
    private String mAuthTokenType;
    private String mAccountType;

    // Values for username and password at the time of the login attempt
    private String mUsername;
    private String mPassword;

    // UI references
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // initialize Activity with intent parameters
        mAccountManager = AccountManager.get(getBaseContext());
        mAccountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountService.AUTH_TOKEN_TYPE;

        mUsername = getIntent().getStringExtra(ARG_ACCOUNT_NAME);

        mUsernameView = (EditText) findViewById(R.id.username);

        if (mUsername != null) {
            mUsernameView.setText(mUsername);
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully logged in
        if (resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mUsername = mUsernameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a password and verifies min length of password
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // error. focus screen on the item with the error
            focusView.requestFocus();
        } else {
            // show progress spinner
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            
            // set up and run task to attempt to authorize user
            new AsyncTask<Void, Void, Intent>() {
                @Override
                protected Intent doInBackground(Void... params) {
                    String authtoken = HttpHelper.authorize(mUsername, mPassword);

                    if (authtoken == null) {
                        return null;
                    } else {
                        final Intent res = new Intent();
                        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
                        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                        res.putExtra(AccountManager.KEY_AUTHTOKEN, authtoken);
                        res.putExtra(PARAM_USER_PASS, mPassword);
                        return res;
                    }
                }

                @Override
                protected void onPostExecute(Intent intent) {
                    finishLogin(intent);
                }
            }.execute();
        }
    }

    // complete the log in and pass back the result of login
    private void finishLogin(Intent intent) {
        if (intent != null) {
            String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
            final Account account = new Account(accountName, AccountService.ACCOUNT_TYPE);
            
            if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
                String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
                String authtokenType = mAuthTokenType;
                
                // save auth token in account manager, preferences, and base activity 
                mAccountManager.addAccountExplicitly(account, accountPassword, null);
                mAccountManager.setAuthToken(account, authtokenType, authtoken);

                SharedPreferences settings = getSharedPreferences(BaseActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("access_token", authtoken);
                editor.commit();

                BaseActivity.setAccessToken(authtoken);
            } else {
                mAccountManager.setPassword(account, accountPassword);
            }
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);

            finish();
        } else {
    		// authentication failed
        	
            showProgress(false);
            Toast.makeText(this, "Please verify your credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
