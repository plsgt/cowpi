package net.cowpi.android.repository;


import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.DataSource.Factory;

import net.cowpi.android.contact.dummy.DummyContent.DummyItem;

public class DummyRepository {

    public DataSource.Factory<Integer, DummyItem> getDummyContent(){
        return new Factory<Integer, DummyItem>() {
            @NonNull
            @Override
            public DataSource<Integer, DummyItem> create() {
                return new DummyDataSource();
            }
        };

    }
}
