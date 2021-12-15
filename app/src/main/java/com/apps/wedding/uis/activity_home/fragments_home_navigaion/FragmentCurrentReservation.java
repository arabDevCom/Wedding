package com.apps.wedding.uis.activity_home.fragments_home_navigaion;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.wedding.R;
import com.apps.wedding.adapter.RateFilterAdapter;
import com.apps.wedding.adapter.ReservionAdapter;
import com.apps.wedding.adapter.WeddingHallDepartmentAdapter;
import com.apps.wedding.databinding.BottomSheetDialogBinding;
import com.apps.wedding.databinding.BottomSheetServiceDetailsBinding;
import com.apps.wedding.model.FilterModel;
import com.apps.wedding.model.FilterRangeModel;
import com.apps.wedding.model.ResevisionModel;
import com.apps.wedding.mvvm.FragmentCurrentReservisonMvvm;
import com.apps.wedding.uis.activity_base.BaseFragment;
import com.apps.wedding.databinding.FragmentCurrentReservationBinding;
import com.apps.wedding.uis.activity_home.HomeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;


public class FragmentCurrentReservation extends BaseFragment {
    private FragmentCurrentReservationBinding binding;
    private FragmentCurrentReservisonMvvm fragmentCurrentReservisonMvvm;
    private HomeActivity activity;
    private ReservionAdapter reservionAdapter;

    public static FragmentCurrentReservation newInstance() {
        FragmentCurrentReservation fragment = new FragmentCurrentReservation();
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (HomeActivity) context;
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
        fragmentCurrentReservisonMvvm = ViewModelProviders.of(this).get(FragmentCurrentReservisonMvvm.class);
        fragmentCurrentReservisonMvvm.getIsLoading().observe(activity, isLoading -> {
            if (isLoading) {
                binding.cardNoData.setVisibility(View.GONE);


            }
            binding.swipeRefresh.setRefreshing(isLoading);
        });



        fragmentCurrentReservisonMvvm.getReservionList().observe(activity, weddingHallModels -> {
            if (weddingHallModels.size() > 0) {
                reservionAdapter.updateList(fragmentCurrentReservisonMvvm.getReservionList().getValue());
                binding.cardNoData.setVisibility(View.GONE);

            } else {
                reservionAdapter.updateList(null);

                binding.cardNoData.setVisibility(View.VISIBLE);

            }

        });
        binding.swipeRefresh.setOnRefreshListener(() -> {
            fragmentCurrentReservisonMvvm.getReservionData(getUserModel());
        });

        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        binding.recView.setLayoutManager(new LinearLayoutManager(activity));
        reservionAdapter = new ReservionAdapter(activity, this);
        binding.recView.setAdapter(reservionAdapter);
        fragmentCurrentReservisonMvvm.getReservionData(getUserModel());
    }

    public void createSheetDialog(ResevisionModel model) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        BottomSheetServiceDetailsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.bottom_sheet_service_details, null, false);
        dialog.setContentView(binding.getRoot());
        binding.setModel(model);
        double total = model.getMain_item_price() + model.getExtra_item_price();
        binding.setTotal(String.valueOf(total));
        StringBuilder details = new StringBuilder();
        if (model.getReservation_extra_items() != null && model.getReservation_extra_items().size() > 0) {
            int index = 0;
            for (ResevisionModel.ResevisionExtraItems items : model.getReservation_extra_items()) {
                if (index == model.getReservation_extra_items().size() - 1) {
                    details.append(items.getItem_name());
                } else {
                    details.append(items.getItem_name()).append("-");
                }

            }

        } else {
            details = new StringBuilder(model.getService().getText());
        }


        binding.setDetails(details.toString());

        binding.imageClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        binding.btnChange.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", model);
            Navigation.findNavController(FragmentCurrentReservation.this.binding.getRoot()).navigate(R.id.fragmentEditReservation, bundle);
            dialog.dismiss();
        });
        dialog.show();
    }

    public void delete(ResevisionModel model) {
        fragmentCurrentReservisonMvvm.deleteReservation(activity, model, getUserModel());
    }
}