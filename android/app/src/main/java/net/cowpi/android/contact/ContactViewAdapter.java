package net.cowpi.android.contact;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.cowpi.android.R;
import net.cowpi.android.contact.dummy.DummyContent.DummyItem;

public class ContactViewAdapter extends PagedListAdapter<DummyItem, DummyItemViewHolder> {


    private static final DiffUtil.ItemCallback<DummyItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<DummyItem>(){

        @Override
        public boolean areItemsTheSame(@NonNull DummyItem oldItem, @NonNull DummyItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull DummyItem oldItem, @NonNull DummyItem newItem) {
            return oldItem.content.equals(newItem.content);
        }
    };

    public ContactViewAdapter(){
        super(DIFF_CALLBACK);
    }

    @Override
    public DummyItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);
        return new DummyItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DummyItemViewHolder holder, int position) {
        DummyItem item = getItem(position);
        holder.bind(item);
    }

}
