package net.cowpi.android.repository;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

import net.cowpi.android.contact.dummy.DummyContent;
import net.cowpi.android.contact.dummy.DummyContent.DummyItem;

import java.util.List;

public class DummyDataSource extends PositionalDataSource<DummyItem> {


    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<DummyItem> callback) {

        int total = DummyContent.ITEMS.size();
        int position = computeInitialLoadPosition(params, total);
        int size = computeInitialLoadSize(params, position, total);


        List<DummyItem> result = DummyContent.ITEMS.subList(position, position+size);
        callback.onResult(result, position, total);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<DummyItem> callback) {
        List<DummyItem> result = DummyContent.ITEMS.subList(params.startPosition, params.startPosition+params.loadSize);
        callback.onResult(result);
    }
}
