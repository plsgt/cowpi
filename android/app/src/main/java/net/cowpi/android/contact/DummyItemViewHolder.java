package net.cowpi.android.contact;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.cowpi.android.R;
import net.cowpi.android.contact.dummy.DummyContent.DummyItem;


public class DummyItemViewHolder extends ViewHolder {

    private final View mView;
    private final TextView mIdView;
    private final TextView mContentView;
    private DummyItem mItem;

    public DummyItemViewHolder(@NonNull View view) {
        super(view);
        mView = view;
        mIdView = (TextView) view.findViewById(R.id.item_number);
        mContentView = (TextView) view.findViewById(R.id.content);
    }



    public void bind(DummyItem item) {
        mItem = item;
        mIdView.setText(item.id);
        mContentView.setText(item.content);
    }
}
