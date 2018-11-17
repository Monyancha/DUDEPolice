package com.amsavarthan.dudepolice.adapters;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.dudepolice.models.Img;
import com.amsavarthan.dudepolice.models.MyReport;
import com.amsavarthan.dudepolice.R;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MyReportsAdapter extends RecyclerView.Adapter<MyReportsAdapter.ViewHolder> {

    private List<MyReport> reportList;
    private Context context;
    private String status;

    public MyReportsAdapter(List<MyReport> reportList,String status) {

        this.reportList = reportList;
        this.status=status;

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_report, parent, false);
        context = parent.getContext();
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final MyReport report = reportList.get(position);

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog(status,report,holder.getAdapterPosition());
                return false;
            }
        });

        holder.name.setText(String.format(" %s ", report.getName()));
        holder.info.setText(report.getInfo());
        holder.timestamp.setText(TimeAgo.using(Long.parseLong(report.getTimestamp())));

        FirebaseFirestore.getInstance().collection("Users")
                .document(report.getUser_id())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {

                        if(!documentSnapshot.getString("name").equals(report.getName())){

                            Map<String,Object> name=new HashMap<>();
                            name.put("name",documentSnapshot.getString("name"));

                            FirebaseFirestore.getInstance().collection("Reports")
                                    .document(report.reportID)
                                    .update(name)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            holder.name.setText(String.format(" %s ", documentSnapshot.getString("name")));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                        }
                                    });

                        }

                        holder.name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new MaterialDialog.Builder(context)
                                        .title("Reporter Details")
                                        .content(String.format("Name : %s\nAge : %s\nPhone Number: %s\nLocation: %s, %s",
                                                documentSnapshot.getString("name"),
                                                documentSnapshot.getString("age"),
                                                documentSnapshot.getString("phone"),
                                                documentSnapshot.getString("city"),
                                                documentSnapshot.getString("state")))
                                        .positiveText("Call")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                try {
                                                    Intent intent = new Intent(Intent.ACTION_CALL);
                                                    intent.setData(Uri.parse("tel:" + documentSnapshot.getString("phone")));
                                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                        return;
                                                    }
                                                    context.startActivity(intent);
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        })
                                        .negativeText("Ok")
                                        .show();
                            }
                        });

                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        holder.date.setText(String.format(" %s ", report.getDate()));
        holder.location.setText(String.format(" %s ", report.getLocation()));

        if(report.getImage_count()==0){
            holder.attachments.setVisibility(View.GONE);
        }else{

            List<Img> urls = new ArrayList<>();

            ImageSubAdapter adapter=new ImageSubAdapter(urls);
            holder.attachments.setVisibility(View.VISIBLE);
            holder.attachments.setItemAnimator(new DefaultItemAnimator());
            LinearLayoutManager layoutManager=new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.attachments.setLayoutManager(layoutManager);
            holder.attachments.setHasFixedSize(true);
            holder.attachments.setAdapter(adapter);

            String color= String.valueOf(new Random().nextInt(5));

            if(!TextUtils.isEmpty(report.getImage_url_0())){
                Img img=new Img("1",report.getImage_url_0(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

            if(!TextUtils.isEmpty(report.getImage_url_1())){
                Img img=new Img("2",report.getImage_url_1(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

            if(!TextUtils.isEmpty(report.getImage_url_2())){
                Img img=new Img("3",report.getImage_url_2(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

            if(!TextUtils.isEmpty(report.getImage_url_3())){
                Img img=new Img("4",report.getImage_url_3(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

            if(!TextUtils.isEmpty(report.getImage_url_4())){
                Img img=new Img("5",report.getImage_url_4(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

            if(!TextUtils.isEmpty(report.getImage_url_5())){
                Img img=new Img("6",report.getImage_url_5(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

            if(!TextUtils.isEmpty(report.getImage_url_6())){
                Img img=new Img("7",report.getImage_url_6(), color);
                urls.add(img);
                adapter.notifyDataSetChanged();
            }

        }



    }

    private void showDialog(String status,final MyReport report,final int position) {

        switch (status){

            case "Closed":

                new MaterialDialog.Builder(context)
                        .title("Status")
                        .content("Change status as...")
                        .positiveText("Investigating")
                        .neutralText("Cancel")
                        .canceledOnTouchOutside(true)
                        .negativeText("Received")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                                final ProgressDialog mDialog = new ProgressDialog(context);
                                mDialog.setMessage("Please wait....");
                                mDialog.setIndeterminate(true);
                                mDialog.setCancelable(false);
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();

                                FirebaseFirestore.getInstance().collection("Cops")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", "Investigating");
                                                map.put("by", documentSnapshot.getString("name"));

                                                FirebaseFirestore.getInstance().collection("Reports")
                                                        .document(report.reportID)
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDialog.dismiss();
                                                                Toast.makeText(context, "Report status updated", Toast.LENGTH_SHORT).show();
                                                                notifyItemChanged(position);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mDialog.dismiss();
                                                Log.e("Error", e.getLocalizedMessage());
                                            }
                                        });



                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                                final ProgressDialog mDialog = new ProgressDialog(context);
                                mDialog.setMessage("Please wait....");
                                mDialog.setIndeterminate(true);
                                mDialog.setCancelable(false);
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();

                                FirebaseFirestore.getInstance().collection("Cops")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", "Received");
                                                map.put("by", documentSnapshot.getString("name"));

                                                FirebaseFirestore.getInstance().collection("Reports")
                                                        .document(report.reportID)
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDialog.dismiss();
                                                                Toast.makeText(context, "Report status updated", Toast.LENGTH_SHORT).show();
                                                                notifyItemChanged(position);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                            }
                                                        });


                                            }
                                        });


                            }
                        })
                        .show();

                return;

            case "Investigating":

                new MaterialDialog.Builder(context)
                        .title("Status")
                        .content("Change status as...")
                        .positiveText("Closed")
                        .neutralText("Cancel")
                        .canceledOnTouchOutside(true)
                        .negativeText("Received")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                dialog.dismiss();

                                final ProgressDialog mDialog = new ProgressDialog(context);
                                mDialog.setMessage("Please wait....");
                                mDialog.setIndeterminate(true);
                                mDialog.setCancelable(false);
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();

                                FirebaseFirestore.getInstance().collection("Cops")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", "Closed");
                                                map.put("by", documentSnapshot.getString("name"));

                                                FirebaseFirestore.getInstance().collection("Reports")
                                                        .document(report.reportID)
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDialog.dismiss();
                                                                Toast.makeText(context, "Report status updated", Toast.LENGTH_SHORT).show();
                                                                notifyItemChanged(position);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                            }
                                                        });
                                            }
                                        });

                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                                final ProgressDialog mDialog = new ProgressDialog(context);
                                mDialog.setMessage("Please wait....");
                                mDialog.setIndeterminate(true);
                                mDialog.setCancelable(false);
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();

                                FirebaseFirestore.getInstance().collection("Cops")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", "Received");
                                                map.put("by", documentSnapshot.getString("name"));

                                                FirebaseFirestore.getInstance().collection("Reports")
                                                        .document(report.reportID)
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDialog.dismiss();
                                                                Toast.makeText(context, "Report status updated", Toast.LENGTH_SHORT).show();
                                                                notifyItemChanged(position);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                            }
                                                        });


                                            }
                                        });


                            }
                        })
                        .show();

                return;

            case "Received":

                new MaterialDialog.Builder(context)
                        .title("Status")
                        .content("Change status as...")
                        .positiveText("Investigating")
                        .neutralText("Cancel")
                        .canceledOnTouchOutside(true)
                        .negativeText("Closed")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();

                                final ProgressDialog mDialog = new ProgressDialog(context);
                                mDialog.setMessage("Please wait....");
                                mDialog.setIndeterminate(true);
                                mDialog.setCancelable(false);
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();

                                FirebaseFirestore.getInstance().collection("Cops")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", "Investigating");
                                                map.put("by", documentSnapshot.getString("name"));

                                                FirebaseFirestore.getInstance().collection("Reports")
                                                        .document(report.reportID)
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDialog.dismiss();
                                                                Toast.makeText(context, "Report status updated", Toast.LENGTH_SHORT).show();
                                                                notifyItemChanged(position);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mDialog.dismiss();
                                                Log.e("Error", e.getLocalizedMessage());
                                            }
                                        });



                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                dialog.dismiss();

                                final ProgressDialog mDialog = new ProgressDialog(context);
                                mDialog.setMessage("Please wait....");
                                mDialog.setIndeterminate(true);
                                mDialog.setCancelable(false);
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();

                                FirebaseFirestore.getInstance().collection("Cops")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("status", "Closed");
                                                map.put("by", documentSnapshot.getString("name"));

                                                FirebaseFirestore.getInstance().collection("Reports")
                                                        .document(report.reportID)
                                                        .update(map)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDialog.dismiss();
                                                                Toast.makeText(context, "Report status updated", Toast.LENGTH_SHORT).show();
                                                                notifyItemChanged(position);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                mDialog.dismiss();
                                                                Log.e("Error", e.getLocalizedMessage());
                                                            }
                                                        });
                                            }
                                        });

                            }
                        })
                        .show();

        }

    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private CardView layout;
        private TextView info,timestamp,location,date,name;
        private RecyclerView attachments;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            layout=mView.findViewById(R.id.layout);
            location=mView.findViewById(R.id.location);
            date=mView.findViewById(R.id.date);
            info=mView.findViewById(R.id.info);
            timestamp=mView.findViewById(R.id.timestamp);
            name=mView.findViewById(R.id.name);
            attachments=mView.findViewById(R.id.attachments);

        }

    }

}