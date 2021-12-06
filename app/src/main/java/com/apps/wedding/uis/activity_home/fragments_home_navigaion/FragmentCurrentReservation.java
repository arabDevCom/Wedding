package com.apps.wedding.uis.activity_home.fragments_home_navigaion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.wedding.R;
import com.apps.wedding.uis.activity_base.BaseFragment;
import com.apps.wedding.databinding.FragmentCurrentReservationBinding;


public class FragmentCurrentReservation extends BaseFragment {
    private FragmentCurrentReservationBinding binding;

    public static FragmentCurrentReservation newInstance() {
        FragmentCurrentReservation fragment = new FragmentCurrentReservation();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_current_reservation, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

    }

    private void initView() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        getData();
    }

    private void getData() {
        binding.cardNoData.setVisibility(View.VISIBLE);
    }
}