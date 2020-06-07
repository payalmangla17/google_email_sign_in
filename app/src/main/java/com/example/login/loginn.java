package com.example.login;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginn extends AppCompatActivity {
EditText username,ppassword;
Button  google;
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;

    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;
    TextView text,create_account;
   Boolean isValid;
    Button signin;
    private Context context;
    private RelativeLayout relativeLayout;
    FirebaseDatabase database ;
    DatabaseReference myRef ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            finish();
            Intent i = new Intent(loginn.this, MainActivity.class);
           // i.putExtra("choice", 3);
            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);

        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");

        }

        setContentView(R.layout.login);
        database=FirebaseDatabase.getInstance();
        myRef=database.getReference("user");
        relativeLayout=findViewById(R.id.l1);
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        if (netInfo == null || !netInfo.isConnected()) {
          //  Toast t = Toast.makeText(login.this, "Unable to connect to internet...", Toast.LENGTH_SHORT);
         //   t.setGravity(Gravity.BOTTOM, 0, 0);
         //   t.show();
            Snackbar.make(relativeLayout,"Unable to connect to internet...", Snackbar.LENGTH_LONG).show();
        }
        final Intent i = new Intent(loginn.this, SignUp.class);
        username = findViewById(R.id.uname);
        ppassword = findViewById(R.id.pwd);
     //   facebook=findViewById(R.id.Facebook);
        google=findViewById(R.id.Google);
        signin=findViewById(R.id.Submit);
        create_account=findViewById(R.id.createacc);
        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(loginn.this, SignUp.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();
   /*   facebook.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              startActivity(new Intent(MainActivity.this,FacebookSignIn.class));
          }
      });*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();

                //startActivity(new Intent(login.this, MainActivity.class));


            }
        });


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = username.getText().toString();
                final String password = ppassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }


                //authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(loginn.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        ppassword.setError(("Incorrect password"));
                                    } else {
                                        //Toast.makeText(login.this, "Please Create Account", Toast.LENGTH_LONG).show();
                                        Snackbar s= Snackbar.make(relativeLayout,"Please create account!", Snackbar.LENGTH_SHORT);
                                        s.show();
                                    }


                                } else {
                                    if(validateEmail(email)) {
                                        Intent intent = new Intent(loginn.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(loginn.this,"Please make accoount first.",Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }
                        });
            }
        });
        mAuth = FirebaseAuth.getInstance();
    }
    private boolean validateEmail(String email) {
        isValid = false;
        myRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    Log.d("as","found");
                    isValid=true;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return isValid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }

        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            if(validateEmail(acct.getEmail())) {

                                updateUI(user);
                                finish();
                            }
                            else{
                                Toast.makeText(loginn.this,"Please create account first.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                       //     Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);

                        }

                        // ...
                    }
                });

    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
       /* mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });*/
    }
    private void updateUI(FirebaseUser user) {
       // hideProgressBar();
        if (user != null) {
                Intent i=new Intent(loginn.this,MainActivity.class);
                startActivity(i);
        } else {
           //Toast.makeText(this,"Connect to internet..",Toast.LENGTH_SHORT).show();
           Snackbar s= Snackbar.make(relativeLayout,"Connect to Internet...", Snackbar.LENGTH_SHORT);
           s.show();
        }
    }

}

