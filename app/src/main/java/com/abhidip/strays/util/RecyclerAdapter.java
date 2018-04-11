package com.abhidip.strays.util;

import android.content.Context;
import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhidip.strays.model.ChatMessage;
import com.abhidip.strays.riseupsrays.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by admin on 4/8/2018.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewholder> {

    private List<ChatMessage> chatMessagesList;
    private LayoutInflater layoutInflater;
    private Context mContext;

    public RecyclerAdapter(Context context, List<ChatMessage> data) {
        this.mContext = context;
        this.chatMessagesList = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.d(TAG, "" );
        View view = layoutInflater.inflate(R.layout.list_item, parent, false);
        MyViewholder myViewholder = new MyViewholder(view);
        return myViewholder;
    }

    @Override
    public int getItemCount() {
        return chatMessagesList.size();
    }

    @Override
    public void onBindViewHolder(MyViewholder holder, int position) {

        ChatMessage currentObject = chatMessagesList.get(position);
        holder.setData(currentObject, position);
    }

    class MyViewholder extends  RecyclerView.ViewHolder {
        ChatMessage current;
        int position;
        TextView description, txTitle;
        ImageView thumbImage;

    public MyViewholder(View itemView) {
            super(itemView);
            description = (TextView)itemView.findViewById(R.id.description);
            txTitle = (TextView)itemView.findViewById(R.id.description);
            thumbImage = (ImageView) itemView.findViewById(R.id.thumbImage);
    }

        public void setData(ChatMessage currentObject, int position) {
             this.description.setText(currentObject.getDescription());
             Picasso.with(mContext)
                    .load(currentObject.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(thumbImage);

        }
    }
}
