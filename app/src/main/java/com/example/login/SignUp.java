package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;

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

public class SignUp extends AppCompatActivity {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    boolean isValid;
    // [START declare_auth]

    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;
    Button signup,gSignUp;
    EditText username,email,password;
    TextView login;
    FirebaseAuth fauth;
    ImageView imageView;
    private RelativeLayout relativeLayout;
    FirebaseDatabase database ;
    DatabaseReference myRef ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        fauth= FirebaseAuth.getInstance();
        imageView =findViewById(R.id.img);
      //  mAuth=FirebaseAuth.getInstance();
       // username =findViewById(R.id.suname);
        relativeLayout=findViewById(R.id.l2);
        password=findViewById(R.id.spwd);
        email=findViewById(R.id.suemail);
        signup=findViewById(R.id.Signup);
        login=findViewById(R.id.signin);
        gSignUp=findViewById(R.id.sGoogle);
        database= FirebaseDatabase.getInstance();
        myRef= database.getReference("user");

        gSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    signIn();
                  //  startActivity(new Intent(SignUp.this,MainActivity.class));
                  //  finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(SignUp.this, loginn.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uemail = email.getText().toString().trim();
                String upassword = password.getText().toString().trim();
             //   String uname= username.getText().toString().trim();

                if (TextUtils.isEmpty(uemail)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(upassword)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                fauth.createUserWithEmailAndPassword(uemail, upassword)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignUp.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUp.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {

                                    onAuthSuccess(task.getResult().getUser());
                                }
                            }
                        });
            }
        });

    }
    private void onAuthSuccess(FirebaseUser user) {
      //  String username = usernameFromEmail(user.getEmail());
        String mail=email.getText().toString();
        String pwd=password.getText().toString();

        // Write new user
        if(validateEmail(mail))
        saveData(user.getUid(),mail,pwd);
        else{
            Toast.makeText(SignUp.this, "User with thid email already registered.\n Please use another mail.",Toast.LENGTH_SHORT).show();
            return;
        }
        // Go to MainActivity
        startActivity(new Intent(SignUp.this, MainActivity.class));
        finish();
    }

    public void saveData(String userID,String mail, String pwd){
        // Write a message to the database


        //String mail=email.getText().toString();
       // String pwd=password.getText().toString();
        usermodel user=new usermodel(mail,pwd);
        myRef.child(userID).setValue(user);

    }
    private boolean validateEmail(String email) {
         isValid = true;
        myRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    isValid=false;

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

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        fauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = fauth.getCurrentUser();
                            if(validateEmail(acct.getEmail())) {
                                saveData(user.getUid(),acct.getEmail(),acct.getDisplayName());
                                // startActivity(new Intent(SignUp.this, MainActivity.class));
                                updateUI(user);
                            }
                            else{
                                Toast.makeText(SignUp.this, "User with thid email already registered.\n Please use another mail.",Toast.LENGTH_SHORT).show();
                                return;

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                           updateUI(null);

                        }

                        // ...
                    }
                });
    }

    private void signIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void updateUI(FirebaseUser user) {
        // hideProgressBar();
        if (user != null) {
            Intent i=new Intent( SignUp.this,MainActivity.class);
            startActivity(i);
        } else {
            Snackbar s= Snackbar.make(relativeLayout,"Connect to Internet...", Snackbar.LENGTH_SHORT);
            s.show();
        }
    }
}
