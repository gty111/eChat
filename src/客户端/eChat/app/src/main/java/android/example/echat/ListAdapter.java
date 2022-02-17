package android.example.echat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final LinkedList<String> mUserList;
    private final LinkedList<String> mContentList;
    private final LinkedList<Integer> mTypeList;
    private final LayoutInflater mInflater;

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        public final TextView user;
        public final TextView content;
        public final ListAdapter mAdapter;

        public ViewHolder(View itemView,ListAdapter adapter,int viewType){
            super(itemView);
            mAdapter = adapter;
            //init user and content
            if(viewType==1){
                user = itemView.findViewById(R.id.user1);
                content = itemView.findViewById(R.id.content1);
            }else{
                user = itemView.findViewById(R.id.user2);
                content = itemView.findViewById(R.id.content2);
            }
        }

        @Override
        public void onClick(View view){

        }
    }

    public ListAdapter(Context context,
                       LinkedList<String>userlist,
                       LinkedList<String>contentlist,
                       LinkedList<Integer>typelist){
        mInflater = LayoutInflater.from(context);
        this.mTypeList = typelist;
        this.mUserList = userlist;
        this.mContentList = contentlist;
    }

    @Override
    public int getItemViewType(int position){
        return mTypeList.get(position);
    }



    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View mItemView;
        if(viewType==1){
            mItemView = mInflater.inflate(
                R.layout.item1,parent,false);
        }else{
            mItemView = mInflater.inflate(
                    R.layout.item2,parent,false);
        }
        return new ViewHolder(mItemView,this,viewType);
    }

    @Override
    public void onBindViewHolder(ListAdapter.ViewHolder holder,
                                 int position) {
        String user = mUserList.get(position);
        String content = mContentList.get(position);
        holder.user.setText(user);
        holder.content.setText(content);
    }

    @Override
    public int getItemCount(){
        return mContentList.size();
    }
}
