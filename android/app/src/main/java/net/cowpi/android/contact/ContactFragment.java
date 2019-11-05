package net.cowpi.android.contact;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.cowpi.android.R;
import net.cowpi.android.contact.dummy.DummyContent;

public class ContactFragment extends Fragment {

    private ContactFragmentViewModel viewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(ContactFragmentViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ContactViewAdapter contactViewAdapter = new ContactViewAdapter();

        viewModel.getDummyContent().observe(this, list -> {
            contactViewAdapter.submitList(list);
        });

        recyclerView.setAdapter(contactViewAdapter);

        return view;
    }
}
