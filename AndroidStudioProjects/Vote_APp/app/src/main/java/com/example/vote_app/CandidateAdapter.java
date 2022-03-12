package com.example.vote_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.ViewHolder> {
    private ArrayList<Candidate> candidates;
    private int voted=-1;
    private OnVoteConfirmedListener onVoteConfirmedListener;
    private boolean allowVoting;
    private Context context;

    public CandidateAdapter(ArrayList<Candidate> candidates, OnVoteConfirmedListener onVoteConfirmedListener, boolean allowVoting, Context context){
        this.candidates=candidates;
        this.onVoteConfirmedListener=onVoteConfirmedListener;
        this.allowVoting=allowVoting;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_candidate_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getNameTextView().setText(candidates.get(position).getName());
        String votes=candidates.get(position).getVotes()+" "+context.getString(R.string.votes);
        holder.getVotesTextView().setText(votes);
        if(candidates.get(position).isVoted()) {
            holder.getVotedImage().setVisibility(View.VISIBLE);
        }else {
            holder.getVotedImage().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView nameTextView;
        private final ImageView votedImage;
        private final TextView votesTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView=itemView.findViewById(R.id.candidate_name_textview);
            votedImage=itemView.findViewById(R.id.voted_imageview);
            votesTextView=itemView.findViewById(R.id.candidate_votes_textview);
            if(allowVoting) {
                itemView.setOnClickListener(this);
            }
        }

        public TextView getNameTextView() {
            return nameTextView;
        }

        public ImageView getVotedImage() {
            return votedImage;
        }

        public TextView getVotesTextView() {
            return votesTextView;
        }

        @Override
        public void onClick(View v) {
            int i=voted;
            voted=getAdapterPosition();
            if(i>-1) {
                candidates.get(i).setVoted(false);
                notifyItemChanged(i);
            }
            candidates.get(voted).setVoted(true);
            notifyItemChanged(voted);
            onVoteConfirmedListener.onVoteConfirmed(candidates.get(voted));
        }
    }

}
