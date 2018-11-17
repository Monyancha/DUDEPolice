package com.amsavarthan.dudepolice.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.amsavarthan.dudepolice.models.Img;
import com.amsavarthan.dudepolice.R;
import com.amsavarthan.dudepolice.activities.ImagePreview;

import java.util.List;

public class ImageSubAdapter extends RecyclerView.Adapter<ImageSubAdapter.ViewHolder>{

    private List<Img> urls;
    private Context context;

    public ImageSubAdapter(List<Img> url){
       this.urls=url;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myreport_list, parent, false);
        context=parent.getContext();
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Img url=urls.get(position);
        holder.textView.setTextColor(Color.WHITE);
        holder.textView.setText(url.getId());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ImagePreview.class).putExtra("url",url.getUrl()));
            }
        });
        getDrawable(holder,Integer.parseInt(url.getColor()));

    }

    private void getDrawable(ViewHolder holder,int color) {
        switch (color){

            case 0:
                holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_1));
                return ;
            case 1:
                holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_2));
                return ;
            case 2:
                holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_3));
                return ;
            case 3:
                holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_4));
                return ;
            case 4:
                holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_5));
                return ;
            case 5:
                holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_6));
                return ;
                default:
                    holder.card_bg.setBackground(context.getResources().getDrawable(R.drawable.gradient_4));

        }
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View mView;

        private TextView textView;
        private CardView cardView;
        private FrameLayout card_bg;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            card_bg=mView.findViewById(R.id.card_bg);
            cardView=mView.findViewById(R.id.card);
            textView=mView.findViewById(R.id.text);


        }

    }

}