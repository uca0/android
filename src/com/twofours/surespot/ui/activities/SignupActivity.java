package com.twofours.surespot.ui.activities;

import java.security.KeyPair;

import org.spongycastle.jce.interfaces.ECPublicKey;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.twofours.surespot.R;
import com.twofours.surespot.SurespotApplication;
import com.twofours.surespot.SurespotConstants;
import com.twofours.surespot.SurespotIdentity;
import com.twofours.surespot.encryption.EncryptionController;
import com.twofours.surespot.main.MainActivity;
import com.twofours.surespot.network.IAsyncCallback;
import com.twofours.surespot.network.NetworkController;

public class SignupActivity extends Activity {

	private Button signupButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		this.signupButton = (Button) this.findViewById(R.id.bSignup);
		this.signupButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				signup();
			}
		});

		EditText editText = (EditText) findViewById(R.id.etSignupPassword);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					//
					signup();
					handled = true;
				}
				return handled;
			}

		});

	}

	private void signup() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setIndeterminate(true);
		progressDialog.setMessage("Generating Keys...");
		progressDialog.show();

		// generate key pair
		EncryptionController.generateKeyPair(new IAsyncCallback<KeyPair>() {

			@Override
			public void handleResponse(final KeyPair keyPair) {
				if (keyPair != null) {

					final String username = ((EditText) SignupActivity.this.findViewById(R.id.etSignupUsername)).getText().toString();
					String password = ((EditText) SignupActivity.this.findViewById(R.id.etSignupPassword)).getText().toString();

					// get the gcm id
					SharedPreferences settings = SurespotApplication.getAppContext().getSharedPreferences(SurespotConstants.PREFS_FILE,
							android.content.Context.MODE_PRIVATE);
					String gcmId = settings.getString(SurespotConstants.GCM_ID, null);				

					NetworkController.addUser(username, password, EncryptionController.encodePublicKey((ECPublicKey) keyPair.getPublic()),
							gcmId, new AsyncHttpResponseHandler() {

								@Override
								public void onSuccess(String arg0) {

									progressDialog.dismiss();

									// save key pair now that we've created a
									// user successfully
									// TODO add setkey pair method to encryption
									// controller to not have to pass it
									// into the callback
									// and back into the encryption controller
									EncryptionController.saveIdentity(new SurespotIdentity(username, keyPair));

								//	SurespotApplication.getUserData().setUsername(username);
									Intent intent = new Intent(SignupActivity.this, MainActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);

									finish();

								}

								// TODO implement
								public void onFailure(Throwable arg0, String arg1) {
									progressDialog.dismiss();
									Log.e("SignupActivity",arg0.toString());
									Toast.makeText(SignupActivity.this, "Error creating user.", Toast.LENGTH_LONG).show();
								};

							});
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_credential_management, menu);
		return true;
	}

}
