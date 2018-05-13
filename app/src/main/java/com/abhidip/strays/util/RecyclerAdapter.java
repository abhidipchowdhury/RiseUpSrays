package com.abhidip.strays.util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhidip.strays.model.ChatMessage;
import com.abhidip.strays.riseupsrays.CommentsActivity;
import com.abhidip.strays.riseupsrays.HomeActivity;
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
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick (int position);
    }

    public  void setOnItemClickListener (OnItemClickListener listener) {
        this.mListener = listener;
    }

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
        ImageView thumbImage, mainImage, commentIcon, attendIcon;

    public MyViewholder(View itemView) {
        super(itemView);
        description = (TextView) itemView.findViewById(R.id.description);
        txTitle = (TextView) itemView.findViewById(R.id.description);
        thumbImage = (ImageView) itemView.findViewById(R.id.thumbImage);
        mainImage = (ImageView) itemView.findViewById(R.id.mainImage);
        commentIcon = (ImageView) itemView.findViewById(R.id.commentIcon);
        attendIcon = (ImageView) itemView.findViewById(R.id.attendIcon);


        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position);
                    }
                }

            }
        });

        commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, CommentsActivity.class));
            }
        });

    }

        public void setData(ChatMessage currentObject, int position) {
             this.description.setText(currentObject.getDescription());
             Picasso.with(mContext)
                    .load(currentObject.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(mainImage);

        }
    }
}
