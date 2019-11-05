package net.cowpi.android.contact;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import net.cowpi.android.contact.dummy.DummyContent.DummyItem;
import net.cowpi.android.repository.DummyRepository;

public class ContactFragmentViewModel extends ViewModel {

    LiveData<PagedList<DummyItem>> getDummyContent(){
        DummyRepository repository = new DummyRepository();
        return new LivePagedListBuilder(repository.getDummyContent(), 20).build();
    }
}
