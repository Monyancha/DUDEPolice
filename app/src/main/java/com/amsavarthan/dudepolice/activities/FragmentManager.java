package com.amsavarthan.dudepolice.activities;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.dudepolice.models.MyReport;
import com.amsavarthan.dudepolice.adapters.MyReportsAdapter;
import com.amsavarthan.dudepolice.R;
import com.amsavarthan.dudepolice.utils.UserDatabase;
import com.amsavarthan.dudepolice.utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.amsavarthan.dudepolice.utils.Utils.fadeView;

public class FragmentManager extends Fragment {

    View rootview;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    ProgressDialog mDialog;
    RecyclerView mRecyclerView;
    MyReportsAdapter adapter;
    List<MyReport> reportList=new ArrayList<>();
    String status;
    TextView greeting,name;
    LinearLayout top;
    CardView greeting_card;
    ScrollView dashboard_view;
    LinearLayout fragment_layout;
    TextView frag_name;
    TextView total,inves,rec,clo;
    ImageButton about,about2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootview=inflater.inflate(R.layout.fragment,container,false);
        return rootview;
    }

    public static FragmentManager newInstance(String status)
    {
        Bundle args = new Bundle();
        args.putString("status", status);

        FragmentManager fragment = new FragmentManager();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {

            status=getArguments().getString("status");
            mRecyclerView = view.findViewById(R.id.recyclerView);
            fragment_layout=view.findViewById(R.id.fragment_layout);
            frag_name=view.findViewById(R.id.frag_name);
            mAuth = FirebaseAuth.getInstance();
            mFirestore = FirebaseFirestore.getInstance();

            name =view.findViewById(R.id.name);

            greeting=view.findViewById(R.id.greeting);
            greeting_card=view.findViewById(R.id.greeting_card);
            top=view.findViewById(R.id.top);
            dashboard_view=view.findViewById(R.id.dashboard_view);
            about=view.findViewById(R.id.about);
            about2=view.findViewById(R.id.about2);

            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(rootview.getContext())
                            .title("About")
                            .content("DUDE POLICE\nCreated by Amsavarthan")
                            .positiveText("OK")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            about2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(rootview.getContext())
                            .title("About")
                            .content("DUDE POLICE\nCreated by Amsavarthan")
                            .positiveText("OK")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });

            if(!status.equals("dashboard")) {

                fragment_layout.setVisibility(View.VISIBLE);
                dashboard_view.setVisibility(View.GONE);
                adapter = new MyReportsAdapter(reportList,status);
                frag_name.setText(status);

                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setLayoutManager(new LinearLayoutManager(rootview.getContext()));
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(adapter);

                getReports(status);

            }else{

                fragment_layout.setVisibility(View.GONE);
                dashboard_view.setVisibility(View.VISIBLE);

                fadeView(greeting_card,300);
                changeBg();

                UserDatabase userDatabase=new UserDatabase(getActivity());

                Cursor rs = userDatabase.getData(1);
                rs.moveToFirst();

                String username = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_NAME));

                if (!rs.isClosed()) {
                    rs.close();
                }

                name.setText(username);

            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        changeBg();

        UserDatabase userDatabase=new UserDatabase(getActivity());

        Cursor rs = userDatabase.getData(1);
        rs.moveToFirst();

        String username = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_NAME));

        if (!rs.isClosed()) {
            rs.close();
        }

        name.setText(username);

    }

    public void changeBg(){

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            greeting.setText("Good Morning");
            greeting_card.setCardBackgroundColor(getResources().getColor(R.color.blue));
            top.setBackgroundResource(R.drawable.gradient_morning);
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            greeting.setText("Good Afternoon");
            greeting_card.setCardBackgroundColor(getResources().getColor(R.color.light_blue));
            top.setBackgroundResource(R.drawable.gradient_afternoon);
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            greeting.setText("Good Evening");
            greeting_card.setCardBackgroundColor(getResources().getColor(R.color.orange));
            top.setBackgroundResource(R.drawable.gradient_eve);
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            greeting.setText("Good Night");
            greeting_card.setCardBackgroundColor(getResources().getColor(R.color.black));
            top.setBackgroundResource(R.drawable.gradient_night);
        }


    }


    public void getReports(String status) {

        FirebaseFirestore.getInstance()
                .collection("Reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("status",status)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType()== DocumentChange.Type.ADDED) {
                                MyReport report = doc.getDocument().toObject(MyReport.class).withId(doc.getDocument().getId());
                                reportList.add(report);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(rootview.getContext(), "Some technical error occurred", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });

    }
}
