package com.example.vote_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.biometric.BiometricPrompt;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class VoteActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private CandidateAdapter candidateAdapter;
    private Button leaveButton;
    private Button confirmVoteButton;
    private ArrayList<Candidate> candidates;
    private Candidate votedCandidate;
    private BiometricPrompt prompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Boolean allowVoting;
    private TextView selectCandidateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        allowVoting=getIntent().getExtras().getBoolean("allowVoting");
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference();
        leaveButton=findViewById(R.id.leaveButton);
        confirmVoteButton=findViewById(R.id.confirmVoteButton);
        RecyclerView candidatesRecyclerView = findViewById(R.id.candidateRecyclerView);
        selectCandidateTextView=findViewById(R.id.vote_text);
        candidates=new ArrayList<>();
        candidateAdapter=new CandidateAdapter(
                candidates,
                candidate -> votedCandidate=candidate,
                allowVoting,
                VoteActivity.this
        );
        GridLayoutManager gridLayoutManager=new GridLayoutManager(
                this,
                2
        );
        candidatesRecyclerView.setAdapter(candidateAdapter);
        candidatesRecyclerView.setLayoutManager(gridLayoutManager);
        databaseReference.child("candidates")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int position=0;
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            String name=dataSnapshot.child("name").getValue(String.class);
                            String id=dataSnapshot.getKey();
                            int votes=dataSnapshot.child("votes").getValue(Integer.class);
                            candidates.add(position, new Candidate(name, id));
                            candidates.get(position).setVotes(votes);
                            candidateAdapter.notifyItemInserted(position);
                            int finalPosition = position;
                            databaseReference.child("candidates")
                                    .child(candidates.get(position).getId()).child("votes")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int votes=snapshot.getValue(Integer.class);
                                            candidates.get(finalPosition).setVotes(votes);
                                            candidateAdapter.notifyItemChanged(finalPosition);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                            position=position+1;
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        leaveButton.setOnClickListener(this);
        confirmVoteButton.setOnClickListener(this);
        Executor executor = ContextCompat.getMainExecutor(this);
        prompt=new BiometricPrompt(
                VoteActivity.this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        confirmVoteButton.setText(R.string.confirm_vote);
                        confirmVoteButton.setEnabled(true);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        confirmVote();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }
                }
        );
        promptInfo=new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.app_name))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser=firebaseAuth.getCurrentUser();
        updateUI();
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.leaveButton){
            finish();
        }
        if (v.getId()==R.id.confirmVoteButton){
            validateVote();
        }
    }

    private void validateVote() {
        if (votedCandidate == null) {
            Toast.makeText(VoteActivity.this, R.string.no_candidate_selected, Toast.LENGTH_SHORT).show();
            return;
        }
        confirmVoteButton.setText(R.string.please_wait);
        confirmVoteButton.setEnabled(false);
        prompt.authenticate(promptInfo);
    }

    private void confirmVote() {
        databaseReference.child("users").child(firebaseUser.getUid()).child("voted")
                .setValue(votedCandidate.getId()).addOnSuccessListener(
                unused -> databaseReference.child("candidates").child(votedCandidate.getId()).child("votes")
                        .setValue(votedCandidate.getVotes()+1).addOnSuccessListener(
                        unused1 -> finish()
                ).addOnFailureListener(e -> Toast.makeText(VoteActivity.this, "Failed: Please contact SecureVote. "+e.getMessage(), Toast.LENGTH_SHORT).show())
        ).addOnFailureListener(e -> {
            Toast.makeText(VoteActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            confirmVoteButton.setText(R.string.confirm_vote);
            confirmVoteButton.setEnabled(true);
        });
    }

    private void updateUI(){
        leaveButton.setText(R.string.home);
        if(allowVoting){
            confirmVoteButton.setVisibility(View.VISIBLE);
            selectCandidateTextView.setVisibility(View.VISIBLE);
        }
    }
}
