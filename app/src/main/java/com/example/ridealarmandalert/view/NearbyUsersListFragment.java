package com.example.ridealarmandalert.view;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.adapter.NearbyUserListAdapter;


public class NearbyUsersListFragment extends Fragment {
    View v;

    private RecyclerView recyclerViewTableList;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView txtNoData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.users_list, container, false);

        recyclerViewTableList = v.findViewById(R.id.recyclerview_list);

        mLayoutManager = new LinearLayoutManager(getContext());
        txtNoData = v.findViewById(R.id.txt_no_data);

        recyclerViewTableList.setLayoutManager(mLayoutManager);
        recyclerViewTableList.setItemAnimator(new DefaultItemAnimator());


        adapter = new NearbyUserListAdapter(getActivity(), txtNoData,(Location) getArguments().getParcelable("location"));

        recyclerViewTableList.setAdapter(adapter);

        return v;
    }
//    String uniqueID = UUID.randomUUID().toString();


}
