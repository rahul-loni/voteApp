package com.example.vote_app;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Button registerButton;
    private Button loginButton;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText emailEditText1;
    private EditText passwordEditText1;
    private EditText nameEditText;
    private EditText idEditText;
    private CheckBox eligibleCheckBox;
    private BiometricPrompt prompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private ConstraintLayout registerLayout;
    private ConstraintLayout loginLayout;
    private String localeCode;
    private SharedPreferences sharedPreferences;
    private boolean login=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getApplicationContext().getSharedPreferences("SecureVote", MODE_PRIVATE);
        localeCode=sharedPreferences.getString("localeCode", "ne");
        setLocale(localeCode);
        setContentView(R.layout.activity_main);
        firebaseAuth=FirebaseAuth.getInstance();
        registerButton=findViewById(R.id.registerButton);
        loginButton=findViewById(R.id.loginButton);
        emailEditText=findViewById(R.id.emailEditText);
        passwordEditText=findViewById(R.id.passwordEditText);
        emailEditText1=findViewById(R.id.emailEditText1);
        passwordEditText1=findViewById(R.id.passwordEditText1);
        nameEditText=findViewById(R.id.nameEditText);
        idEditText=findViewById(R.id.citizenshipIdEditText);
        eligibleCheckBox=findViewById(R.id.eligibleCheckbox);
        registerLayout=findViewById(R.id.registerForm);
        loginLayout=findViewById(R.id.loginForm);
        Button languageButton = findViewById(R.id.languageButton);
        Button loginSubtitle1Button = findViewById(R.id.loginSubtitle1Button);
        Button registerSubtitle1Button = findViewById(R.id.registerSubtitle1Button);
        registerButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        languageButton.setOnClickListener(this);
        loginSubtitle1Button.setOnClickListener(this);
        registerSubtitle1Button.setOnClickListener(this);
        setupBiometricPrompt();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            proceedToVote();
        }
    }

    @Override
    public void recreate() {
        super.recreate();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.registerButton) {
            proceedToRegister();
        }
        if (v.getId() == R.id.loginButton) {
            proceedToLogin();
        }
        if (v.getId() == R.id.registerSubtitle1Button) {
            login=false;
            updateUI();
        }
        if(v.getId() == R.id.loginSubtitle1Button) {
            login=true;
            updateUI();
        }
        if (v.getId()==R.id.languageButton){
            changeLocale();
        }
    }

    private void changeLocale() {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("localeCode", localeCode.equals("ne") ?"en":"ne");
        editor.apply();
        recreate();
    }

    private void setLocale(String languageCode) {
        Locale locale=new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources=getBaseContext().getResources();
        Configuration configuration=resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    private void proceedToRegister() {
        registerButton.setText(R.string.please_wait);
        registerButton.setEnabled(false);
        String name;
        String email;
        String id;
        String password;
        name=nameEditText.getText().toString().trim();
        email=emailEditText.getText().toString().trim().toLowerCase(Locale.ROOT);
        id=idEditText.getText().toString().trim();
        password=passwordEditText.getText().toString();
        if(validate(name, email, id, password)) {
            prompt.authenticate(promptInfo);
        }
    }

    private boolean validate(String name, String email, String id, String password) {
        boolean validation=true;
        if(name.isEmpty()) {
            nameEditText.setHintTextColor(getResources().getColor(R.color.red));
            nameEditText.setHint(R.string.name_required);
            validation=false;
        }
        if(email.isEmpty()) {
            emailEditText.setHintTextColor(getResources().getColor(R.color.red));
            emailEditText.setHint(R.string.email_required);
            validation=false;
        }
        if(id.isEmpty()) {
            idEditText.setHintTextColor(getResources().getColor(R.color.red));
            idEditText.setHint(R.string.id_required);
            validation=false;
        }
        if(password.length() < 6) {
            passwordEditText.setHintTextColor(getResources().getColor(R.color.red));
            passwordEditText.setHint(R.string.password_validity);
            if(!password.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.password_validity, Toast.LENGTH_SHORT).show();
            }
            validation=false;
        }
        if(!eligibleCheckBox.isChecked()) {
            eligibleCheckBox.setTextColor(getResources().getColor(R.color.red));
            if(validation)
                Toast.makeText(MainActivity.this, R.string.eligible_required, Toast.LENGTH_SHORT).show();
            validation=false;
        }
        if(!validation) {
            registerButton.setText(R.string.register);
            registerButton.setEnabled(true);
        }
        return validation;
    }

    private void register(){
        String name;
        String email;
        String password;
        name=nameEditText.getText().toString().trim();
        email=emailEditText.getText().toString().trim().toLowerCase(Locale.ROOT);
        password=passwordEditText.getText().toString();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    firebaseUser=firebaseAuth.getCurrentUser();
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    firebaseUser.updateProfile(profileChangeRequest)
                            .addOnSuccessListener(unused -> proceedToVote())
                            .addOnFailureListener(e -> {
                                registerButton.setText(R.string.register);
                                registerButton.setEnabled(true);
                            });
                })
                .addOnFailureListener(e -> {
                    registerButton.setText(R.string.register);
                    registerButton.setEnabled(true);
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        prompt=new BiometricPrompt(
                MainActivity.this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if(login){
                            loginButton.setText(R.string.login);
                            loginButton.setEnabled(true);
                        }else{
                            registerButton.setText(R.string.register);
                            registerButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if(login)
                            login();
                        else
                            register();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        if(login){
                            loginButton.setText(R.string.login);
                            loginButton.setEnabled(true);
                        }else{
                            registerButton.setText(R.string.register);
                            registerButton.setEnabled(true);
                        }
                    }
                }
        );
        promptInfo=new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.app_name))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
    }

    private void updateUI() {
        if(login){
            registerLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }else {
            loginLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        }
    }

    private void proceedToLogin(){
        loginButton.setText(R.string.please_wait);
        loginButton.setEnabled(false);
        String email;
        String password;
        email=emailEditText1.getText().toString().trim().toLowerCase(Locale.ROOT);
        password=passwordEditText1.getText().toString();
        if(validate(email, password)) {
            prompt.authenticate(promptInfo);
        }
    }

    private void login() {
        String email;
        String password;
        email=emailEditText1.getText().toString().trim().toLowerCase(Locale.ROOT);
        password=passwordEditText1.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> proceedToVote())
                .addOnFailureListener(e -> {
                    loginButton.setText(R.string.login);
                    loginButton.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validate(String email, String password) {
        boolean validation=true;
        if(email.isEmpty()) {
            emailEditText1.setHintTextColor(getResources().getColor(R.color.red));
            emailEditText1.setHint(R.string.email_required);
            validation=false;
        }
        if(password.length() < 5) {
            passwordEditText1.setHintTextColor(getResources().getColor(R.color.red));
            passwordEditText1.setHint(R.string.password_validity);
            if(!password.isEmpty())
                Toast.makeText(MainActivity.this, R.string.password_validity, Toast.LENGTH_SHORT).show();
            validation=false;
        }
        if(!validation) {
            loginButton.setText(R.string.login);
            loginButton.setEnabled(true);
        }
        return validation;
    }

    private void proceedToVote() {
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

}