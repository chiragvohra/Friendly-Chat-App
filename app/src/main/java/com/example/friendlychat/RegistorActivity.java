package com.example.friendlychat;

//import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegistorActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registor);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();


        InitializeFields();


        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            if(TextUtils.isEmpty(email))
                Toast.makeText(this,"Please Enter Email ID ...",Toast.LENGTH_LONG).show();
            if(TextUtils.isEmpty(password))
                Toast.makeText(this,"Please Enter Password ...",Toast.LENGTH_LONG).show();
        }

        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait while we create your account.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            loadingBar.dismiss();
                            if(task.isSuccessful())
                            {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                RootRef.child("Users").child(currentUserId).setValue("");

                                RootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);

                                Toast.makeText(RegistorActivity.this,"Account Created Successfully",Toast.LENGTH_LONG).show();
                                SendUserToMainActivity();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegistorActivity.this,"Error :- " + message,Toast.LENGTH_LONG).show();
                            }
                        }
                    });




//            mAuth.createUserWithEmailAndPassword(email,password)
//                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
//                    {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task)
//                        {
//                            if (task.isSuccessful())
//                            {
//                                final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                                FirebaseInstanceId.getInstance().getInstanceId()
//                                        .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
//                                            @Override
//                                            public void onSuccess(InstanceIdResult instanceIdResult)
//                                            {
//
//                                                RootRef.child("Users").child(currentUserId).setValue("");
//
//                                                String deviceToken = instanceIdResult.getToken();
//                                                RootRef.child("Users").child(currentUserId).child("device_token")
//                                                        .setValue(deviceToken)
//                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<Void> task)
//                                                            {
//                                                                if (task.isSuccessful())
//                                                                {
//                                                                    // Sign in success, update UI with the signed-in user's information
//                                                                    Toast.makeText(RegistorActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
//                                                                    SendUserToMainActivity();
//                                                                    loadingBar.dismiss();
//                                                                }
//                                                            }
//                                                        });
//                                            }
//                                        });
//
//                            } else
//                            {
//                                // If sign in fails, display a message to the user.
//                                Toast.makeText(RegistorActivity.this, "Error: "+task.getException(),
//                                        Toast.LENGTH_SHORT).show();
//                                loadingBar.dismiss();
//                            }
//                        }
//                    });
//

















        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegistorActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitializeFields()
    {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);

    }
    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegistorActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
