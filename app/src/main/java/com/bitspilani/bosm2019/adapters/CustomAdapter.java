
package com.bitspilani.bosm2019.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bitspilani.bosm2019.models.Fixture;
import com.bitspilani.bosm2019.R;
import com.bitspilani.bosm2019.models.PlaceBetModel;
import com.bitspilani.bosm2019.models.UserBetModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.bitspilani.bosm2019.activity.LoginActivity;

import static com.bitspilani.bosm2019.activity.LoginActivity.userBetsList;

public class CustomAdapter extends FirestoreRecyclerAdapter<Fixture,CustomAdapter.ViewHolder> {

    private Context context;
    private String[] teams;
    private int betAmount;
    private int walletamount;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String matchId;

    public CustomAdapter(@NonNull FirestoreRecyclerOptions<Fixture> options,Context context){
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.folding_cell,parent,false);
        return new ViewHolder(view);
    }
    //@NonNull final CustomAdapter.ViewHolder holder, int position
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Fixture fixture) {

        holder.team1.setText(fixture.getCollege1());
        holder.team2.setText(fixture.getCollege2());
        holder.time.setText(fixture.getTimestamp());
        holder.team11.setText(fixture.getCollege1());
        holder.team21.setText(fixture.getCollege2());
        holder.time1.setText(fixture.getTimestamp());
        holder.match_id.setText(fixture.getMatchId());
        //teams = new String[]{};
        holder.fc.initialize(30,500, Color.parseColor("#ffffff"),0);

        holder.fc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.fc.toggle(false);
                if(holder.fc.isUnfolded())
                    holder.content.setVisibility(View.GONE);
                else
                    holder.title.setVisibility(View.GONE);
            }
        });


    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView team1,team2,team11,team21;
        TextView time,venue,time1,venue1;
        final FoldingCell fc;
        FrameLayout content,title;
        Button simple,power;
        SeekBar amount;
        TextView bet;
        SharedPreferences sharedPreferences = context.getSharedPreferences("WalletAmount",Context.MODE_PRIVATE);;
        TextView match_id;
        ArrayList<PlaceBetModel> bets = new ArrayList<>();


        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            team1=itemView.findViewById(R.id.team1);
            team2=itemView.findViewById(R.id.team2);
            team11=itemView.findViewById(R.id.team11);
            team21=itemView.findViewById(R.id.team21);
            time=itemView.findViewById(R.id.time);
            venue=itemView.findViewById(R.id.venue);
            time1=itemView.findViewById(R.id.time1);
            venue1=itemView.findViewById(R.id.venue1);
            fc=itemView.findViewById(R.id.folding_cell);
            content=itemView.findViewById(R.id.cell_content_view);
            title=itemView.findViewById(R.id.cell_title_view);
            simple=itemView.findViewById(R.id.simple);
            power=itemView.findViewById(R.id.power);
            amount=itemView.findViewById(R.id.amount);
            bet=itemView.findViewById(R.id.bet);
            match_id = itemView.findViewById(R.id.match_id);
            simple.setOnClickListener(this);
            power.setOnClickListener(this);
            amount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    bet.setText(""+progress);
                    betAmount=progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        @Override
        public void onClick(View v) {
            if(v.getId()==simple.getId())
                buildDialog(new String[]{team1.getText().toString(),team2.getText().toString()});
            else
                buildDialog(new String[]{team1.getText().toString(),team2.getText().toString()});
        }

        void buildDialog(String[] teams){

            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle("I will place my bet on")
                    .setCancelable(true)
                    .setSingleChoiceItems(teams, -1,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {



                                }
                            }).setPositiveButton("Yay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences sharedPreferences=context.getSharedPreferences("WalletAmount",Context.MODE_PRIVATE);
                    int walletbalance=sharedPreferences.getInt("total",1000);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(betAmount==0)betAmount=100;
                    editor.putInt("total",walletbalance-betAmount);
                    editor.apply();

                    SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                    String userId = sp.getString("username","");


                    Map<String,Object> bet = new HashMap<>();
                    PlaceBetModel ob = new PlaceBetModel(betAmount,userId,"BITS");
                    bets.add(ob);
                    bet.put("roulette",bets);

                    db.collection("matches").document(match_id.getText().toString()).set(bet, SetOptions.merge()).
                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"Bet Placed",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });

                    Map<String,Object> userBet = new HashMap<>();
                   // UserBetModel userOb = new UserBetModel(match_id.getText().toString(),betAmount,"BITS","");
                  //  userBet.put("bets",userBetsList);
                    userBet.put("betAmount",betAmount);
                    userBet.put("match_id",match_id.getText().toString());
                    userBet.put("result",0);
                    userBet.put("team","BITS");

                    db.collection("users").document(userId).collection("bets").add(userBet)
                     .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("test2", "DocumentSnapshot written with ID: " + documentReference.getId());
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("test2", "Error adding document", e);
                                }
                            });
                }
            });
            builder.show();
        }
    }
}


