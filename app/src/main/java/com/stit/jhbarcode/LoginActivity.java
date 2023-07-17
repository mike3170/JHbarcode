package com.stit.jhbarcode;

import com.google.gson.reflect.TypeToken;

import com.koushikdutta.ion.Ion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
// import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.stit.jhbarcode.model.ApiResponse;
import com.stit.jhbarcode.model.CodMast;
import com.stit.jhbarcode.model.MP3;
import com.stit.jhbarcode.model.Response;
import com.stit.jhbarcode.repo.MyDao;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

  private final static String TAG = LoginActivity.class.getName();
  private MyMediaPlay myMediaPlay;

  /**
   * Id to identity READ_CONTACTS permission request.
   */
  private static final int REQUEST_READ_CONTACTS = 0;

  /**
   * Keep track of the login task to ensure we can cancel it if requested.
   */
  private UserLoginTask mAuthTask = null;

  // UI references.
  private AutoCompleteTextView mUserNameView;

  private EditText mPasswordView;

  private View mProgressView;

  private View mLoginFormView;

  private SharedPreferences mPreferences;

  private MyApplication myApplication;
  //private MyStore myStore;

  private MyDao myDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    this.myApplication = (MyApplication) this.getApplication();
    // this.myStore = MyStore.getInstance(this);

    this.myMediaPlay = new MyMediaPlay(this);

    // david
    this.myDao = new MyDao(this);

    mPreferences = getSharedPreferences(MyInfo.SPKey, Context.MODE_PRIVATE);
    if (mPreferences.contains("name")) {
      if (mPreferences.getString("name", "").length() > 0) {
        gotoMainActivity();
      }
    }

    // Set up the login form.
    mUserNameView = (AutoCompleteTextView) findViewById(R.id.username);
    populateAutoComplete();

    mPasswordView = (EditText) findViewById(R.id.password);
    mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });


    Button mEmailSignInButton = (Button) findViewById(R.id.name_sign_in_button);
    mEmailSignInButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });

    mLoginFormView = findViewById(R.id.login_form);
    mProgressView = findViewById(R.id.login_progress);

    // for test
    // this.mUserNameView.setText("dav");
    // this.mPasswordView.setText("dav");

  }

  private void populateAutoComplete() {
    if (!mayRequestContacts()) {
      return;
    }

    getLoaderManager().initLoader(0, null, this);
  }

  private boolean mayRequestContacts() {
    if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
      return true;
    }
    if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
      return true;
    }
    if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
      Snackbar.make(mUserNameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
          .setAction(android.R.string.ok, new OnClickListener() {
            @Override
            @TargetApi(VERSION_CODES.M)
            public void onClick(View v) {
              requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
            }
          });
    } else {
      requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }
    return false;
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode == REQUEST_READ_CONTACTS) {
      if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        populateAutoComplete();
      }
    }
  }

  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private void attemptLogin() {
    if (mAuthTask != null) {
      return;
    }

    // Reset errors.
    mUserNameView.setError(null);
    mPasswordView.setError(null);

    // Store values at the time of the login attempt.
    String username = mUserNameView.getText().toString();
    String password = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid email address.
    if (TextUtils.isEmpty(username)) {
      mUserNameView.setError(getString(R.string.error_field_required));
      focusView = mUserNameView;
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
      mAuthTask = new UserLoginTask(username, password);
      mAuthTask.execute((Void) null);
    }
  }

  /**
   * Shows the progress UI and hides the login form.
   */
  @TargetApi(VERSION_CODES.HONEYCOMB_MR2)
  private void showProgress(final boolean show) {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
    if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR2) {
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

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return new CursorLoader(this,
        // Retrieve data rows for the device user's 'profile' contact.
        Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
            ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

        // Select only email addresses.
        ContactsContract.Contacts.Data.MIMETYPE +
            " = ?", new String[]{ContactsContract.CommonDataKinds.Email
        .CONTENT_ITEM_TYPE},

        // Show primary email addresses first. Note that there won't be
        // a primary email address if the user hasn't specified one.
        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    List<String> emails = new ArrayList<>();
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      emails.add(cursor.getString(ProfileQuery.ADDRESS));
      cursor.moveToNext();
    }

    addEmailsToAutoComplete(emails);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {

  }

  private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
    //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(LoginActivity.this,
            android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

    mUserNameView.setAdapter(adapter);
  }


  private interface ProfileQuery {

    String[] PROJECTION = {
        ContactsContract.CommonDataKinds.Email.ADDRESS,
        ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
    };

    int ADDRESS = 0;
    int IS_PRIMARY = 1;
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate
   * the user.
   */
  public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mUserName;
    private final String mPassword;

    private String errMessage = null;

    UserLoginTask(String username, String password) {
      mUserName = username;
      mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

      Response response = null;

      //String myUrl = MyInfo.API_URL + "login?empNo=" + mUserName + "&chgPswd=" + mPassword;
      //System.out.println("url:" +myUrl);

      try {
        response = Ion.with(LoginActivity.this)
            .load(MyInfo.API_URL + "login?empNo=" + mUserName + "&chgPswd=" + mPassword)
            .as(new TypeToken<Response>() {})
            .get();

        if (response.status == Response.Status.ERROR) {
          LoginActivity.this.myMediaPlay.play(MP3.beepError);
          this.errMessage = "登入失敗!";
          return false;
        }

        // for cod_mast-----------------------------
        boolean deleteOK = myDao.deleteCodMast();
        if (! deleteOK) {
          this.errMessage = "刪除 cod_mast 表格失敗!";
          return false;
        }

        String url = MyInfo.JH_API_URL + "codMast/list";
        ApiResponse<List<CodMast>> apiResponse = Ion.with(LoginActivity.this)
                .load(url)
                .as(new TypeToken<ApiResponse<List<CodMast>>>() {})
                .get();

        List<CodMast> codMastList  = apiResponse.data;
        //for (CodMast cm : codMastList) {
        //  System.out.println(cm.toString());
        //}

        boolean insertOk = myDao.insertCodMast(codMastList);
        if (! insertOk) {
          this.errMessage = "新增 cod_mast 表格失敗!";
          return false;
        }

        return true;  // every thing is ok.

      } catch (InterruptedException e) {
        this.errMessage = getString(R.string.network_error);
        return false;
      } catch (ExecutionException e) {
        this.errMessage = getString(R.string.timeout) +" -  " + e.getMessage();
        //System.out.println(e.getMessage());
        return false;
      } catch (final Exception ex) {
        this.errMessage = ex.getMessage();
        return false;
      }

    }

    @Override
    protected void onPostExecute(final Boolean success) {
      mAuthTask = null;
      showProgress(false);

      if (success) {
        LoginActivity.this.myMediaPlay.play(MP3.sweet);
        mPreferences.edit().putString("name", mUserName).apply();
        gotoMainActivity();
      } else {
        mPasswordView.setError(this.errMessage);
        showError(this.errMessage);
        mPasswordView.requestFocus();
      }
    }

    @Override
    protected void onCancelled() {
      mAuthTask = null;
      showProgress(false);
    }
  }

  private void gotoMainActivity() {
    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  //private void showLog(final int res) {
  //  runOnUiThread(new Runnable() {
  //    @Override
  //    public void run() {
  //      Toast.makeText(LoginActivity.this
  //          , getString(res), Toast.LENGTH_LONG).show();
  //    }
  //  });
  //}

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  public void showError(String msg) {
    AlertDialog alertDialog = new AlertDialog.Builder(this)
        .setTitle("錯誤視窗")
        .setMessage(msg)
        .setCancelable(false)
        .setPositiveButton("確認", new AlertDialog.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        })
        .create();

    alertDialog.show();
  }



} // end class

