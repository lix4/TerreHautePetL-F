package edu.rosehulman.lix4.petlf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;

import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.rosehulman.lix4.petlf.fragments.AccountFragment;
import edu.rosehulman.lix4.petlf.fragments.InfoDetailFragment;

import edu.rosehulman.lix4.petlf.fragments.LostInfoListFragment;
import edu.rosehulman.lix4.petlf.fragments.WelcomeFragment;

import edu.rosehulman.lix4.petlf.models.User;

public class MainActivity extends AppCompatActivity implements AccountFragment.AFCallBack, WelcomeFragment.WFCallBack {
    //Making this two fields is to control UI according to Login state.
    private WelcomeFragment mWelcomeFragment = new WelcomeFragment();

    private BottomNavigationView mNavigation;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private OnCompleteListener mOnCompleteListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.content, mWelcomeFragment);
        ft.commit();

        mAuth = FirebaseAuth.getInstance();
        initilizeListener();
    }

    private void initilizeListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    User newUser = new User();
                    newUser.setUserId(user.getUid());
                    newUser.setEmail(user.getEmail());
                    newUser.setImageUrl(user.getPhotoUrl());
                    ConstantUser.setCurrentUser(newUser);
                } else {
                    ConstantUser.removeCurrentUser();
                }
            }
        };
        mOnCompleteListener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (!task.isSuccessful()) {
                    Log.d("onComplete failed: ", task.getException().toString());
                }
            }
        };
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragmentSelected = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentSelected = mWelcomeFragment;
                    break;
                case R.id.navigation_lost:
                    if (mUser != null) {
                        mCurrentFragment = LostInfoListFragment.newInstance("LOST", mUser.getUid());
                    } else {
                        mCurrentFragment = LostInfoListFragment.newInstance("LOST", "no user here");
                    }
                    break;
                case R.id.navigation_found:
                    if (mUser != null) {
                        mCurrentFragment = LostInfoListFragment.newInstance("FOUND", mUser.getUid());
                    } else {
                        mCurrentFragment = LostInfoListFragment.newInstance("FOUND","no user here");
                    }
                    break;
                case R.id.navigation_account:
                    fragmentSelected = new AccountFragment();
//                    switchToAccountFragment(mLoginState);
                    break;
            }
            if (fragmentSelected != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Slide slideTransition = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    slideTransition = new Slide(Gravity.RIGHT);
                    slideTransition.setDuration(200);
                }
                fragmentSelected.setEnterTransition(slideTransition);
                ft.replace(R.id.content, fragmentSelected);
                ft.commit();
            }
            return true;
        }

    };

//    public void switchToWelcomeFragment(boolean login) {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        mWelcomeFragment.controlButtons(login);
//        ft.replace(R.id.content, mWelcomeFragment);
//        ft.commit();
//    }
//
//    public void switchToAccountFragment(boolean login) {
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        mAccountFragment.controlAButton(login);
//        ft.replace(R.id.content, mAccountFragment);
//        ft.commit();
//    }


    @Override
    public void setNavigationId(int id) {
        mNavigation.setSelectedItemId(id);
    }

    @Override
    public void signOut() {
        mAuth.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void showSignInUpDialog(final boolean switsh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.dialog_signup, null);
        final EditText emailEditText = (EditText) view.findViewById(R.id.edit_username_text_signup);
        final EditText passwordEditText = (EditText) view.findViewById(R.id.edit_password_text_signup);
        final EditText confirmationPasswordEditText = (EditText) view.findViewById(R.id.edit_password_confirm_text_signup);
        TextView confirmationPasswordTitle = (TextView) view.findViewById(R.id.dialog_confirm_email_title_signup);
        if (switsh) {
            builder.setTitle(R.string.signin_dialog_title);
            confirmationPasswordEditText.setVisibility(View.INVISIBLE);
            confirmationPasswordTitle.setVisibility(View.INVISIBLE);
        } else {
            builder.setTitle(R.string.signup_dialog_title);
        }
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (switsh) {
                    //sign in
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(mOnCompleteListener);
                } else {
                    //sign up and login user in automatically
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(mOnCompleteListener);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(mOnCompleteListener);
                    //update user imageUrl and alias
                    FirebaseUser user = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    }
                                }
                            });
                }
                mWelcomeFragment.controlButtons(true);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

}
