package com.example.vote_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Button leaveButton;
    private Button electionStatusButton;
    private TextView candidateNameTextView;
    private TextView candidateVotesTextView;
    private TextView usernameTextView;
    private Boolean voted=false;
    private TextView waitTextView;
    private CardView votedCandidateCardView;
    private CardView voteCandidateCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference();
        leaveButton=findViewById(R.id.leaveButton);
        electionStatusButton =findViewById(R.id.electionStatusButton);
        candidateNameTextView=findViewById(R.id.candidateNameTextView);
        candidateVotesTextView=findViewById(R.id.candidateVotesTextView);
        usernameTextView=findViewById(R.id.usernameTextView);
        waitTextView=findViewById(R.id.waitTextView);
        votedCandidateCardView=findViewById(R.id.votedCandidateLayout);
        voteCandidateCardView=findViewById(R.id.voteCandidateLayout);
        leaveButton.setOnClickListener(this);
        electionStatusButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        leaveButton.setText(R.string.leave);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            databaseReference.child("users").child(firebaseUser.getUid()).child("voted")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String id = snapshot.getValue(String.class);
                            waitTextView.setVisibility(View.GONE);
                            if (id != null) {
                                databaseReference.child("candidates").child(id).addValueEventListener(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                String name = snapshot.child("name").getValue(String.class);
                                                int votes = snapshot.child("votes").getValue(Integer.class);
                                                String votesText = votes + " " + getString(R.string.votes);
                                                candidateNameTextView.setText(name);
                                                candidateVotesTextView.setText(votesText);
                                                voted = true;
                                                updateUI();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        }
                                );
                            }else{
                                updateUI();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            usernameTextView.setText(firebaseUser.getDisplayName());
        }
    }

    private void updateUI() {
        if (voted){
            voteCandidateCardView.setVisibility(View.GONE);
            votedCandidateCardView.setVisibility(View.VISIBLE);
            electionStatusButton.setText(R.string.election_status);
        }else{
            votedCandidateCardView.setVisibility(View.GONE);
            voteCandidateCardView.setVisibility(View.VISIBLE);
            electionStatusButton.setText(R.string.place_vote);
        }
        electionStatusButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.leaveButton){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
        if (v.getId()==R.id.electionStatusButton){
            Intent intent=new Intent(HomeActivity.this, VoteActivity.class);
            intent.putExtra("allowVoting", !voted);
            startActivity(intent);
        }
    }
}