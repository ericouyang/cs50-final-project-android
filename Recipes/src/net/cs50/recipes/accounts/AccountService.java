package net.cs50.recipes.accounts;

import net.cs50.recipes.util.HttpHelper;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

// service for accounts for our app
public class AccountService extends Service {
    private static final String TAG = "RecipesAccountService";

    public static final String ACCOUNT_TYPE = "net.cs50.recipes";
    public static final String ACCOUNT_NAME = "sync";
    public static final String AUTH_TOKEN_TYPE = "Full Access";

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.i(TAG, "Service created");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    // authenticator class that interfaces with Android system for handling user accounts
    // based off of Android Sync Adapter Examples
    public class Authenticator extends AbstractAccountAuthenticator {

        private Context mContext;

        public Authenticator(Context context) {
            super(context);

            mContext = context;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse,
                String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                String authTokenType, String[] requiredFeatures, Bundle bundle)
                throws NetworkErrorException {

            final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
            intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType);
            intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);
            intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle bundle) throws NetworkErrorException {

            final AccountManager am = AccountManager.get(mContext);

            // get authToken from account manager
            String authToken = am.peekAuthToken(account, authTokenType);

            // if authToken empty, try to get new authentication token 
            if (TextUtils.isEmpty(authToken)) {
                final String password = am.getPassword(account);
                if (password != null) {
                    authToken = HttpHelper.authorize(account.name, password);
                }
            }

            // If we get an authToken - we return it as a bundle
            if (!TextUtils.isEmpty(authToken)) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }

            // prompt users for credentials via the AuthenticatorActivity
            final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type);
            intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType);

            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        @Override
        public String getAuthTokenLabel(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                Account account, String s, Bundle bundle) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
                Account account, String[] strings) throws NetworkErrorException {
            throw new UnsupportedOperationException();
        }
    }

}
