package com.apps.wedding.uis.activity_home.fragments_home_navigaion;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.wedding.R;
import com.apps.wedding.adapter.RateFilterAdapter;
import com.apps.wedding.adapter.WeddingHallAdapter;
import com.apps.wedding.adapter.WeddingHallDepartmentAdapter;
import com.apps.wedding.databinding.BottomSheetDialogBinding;
import com.apps.wedding.model.FilterModel;
import com.apps.wedding.model.FilterRangeModel;
import com.apps.wedding.model.FilterRateModel;
import com.apps.wedding.model.WeddingHallModel;
import com.apps.wedding.mvvm.FragmentHomeMvvm;
import com.apps.wedding.uis.activity_base.BaseFragment;
import com.apps.wedding.databinding.FragmentHomeBinding;
import com.apps.wedding.uis.activity_home.HomeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.LabelFormatter;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;


public class FragmentHome extends BaseFragment {
    private HomeActivity activity;
    private FragmentHomeBinding binding;
    private FragmentHomeMvvm fragmentHomeMvvm;
    private WeddingHallAdapter weddingHallAdapter;
    private WeddingHallDepartmentAdapter weddingHallDepartmentAdapter;
    private RateFilterAdapter rateFilterAdapter;
    private float startRange = 0.0f;
    private float endRange = 100000.0f;
    private float steps = 500.0f;
    private FilterRangeModel model;
    private FilterRangeModel filterRangeModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (HomeActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        fragmentHomeMvvm = ViewModelProviders.of(this).get(FragmentHomeMvvm.class);
        fragmentHomeMvvm.getWeddingHall().observe(activity, weddingHallModels -> weddingHallAdapter.updateList(fragmentHomeMvvm.getWeddingHall().getValue()));
        fragmentHomeMvvm.getCategoryWeddingHall().observe(activity, weddingHallModels -> weddingHallDepartmentAdapter.updateList(fragmentHomeMvvm.getCategoryWeddingHall().getValue()));
        fragmentHomeMvvm.getFilter().observe(activity, filterModel -> {
            Log.e("data", filterModel.getRate() + "____" + filterModel.getFromRange() + "+" + filterModel.getToRange());
        });
        setUpFilter();

        binding.recViewCategory.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        weddingHallDepartmentAdapter = new WeddingHallDepartmentAdapter(activity);
        binding.recViewCategory.setAdapter(weddingHallDepartmentAdapter);

        binding.recViewHall.setLayoutManager(new LinearLayoutManager(activity));
        weddingHallAdapter = new WeddingHallAdapter(activity);
        binding.recViewHall.setAdapter(weddingHallAdapter);
        binding.imageFilter.setOnClickListener(v -> {
            createSheetDialog();
        });


    }

    private void setUpFilter() {
        filterRangeModel = fragmentHomeMvvm.getFilterRange().getValue();
        if (filterRangeModel==null){
            filterRangeModel = new FilterRangeModel(startRange,endRange);
        }
        model = fragmentHomeMvvm.getFilterRange().getValue();
        if (model == null) {
            model = new FilterRangeModel(startRange, endRange);
            fragmentHomeMvvm.updateFilterRange(model);
        }

    }

    private void createSheetDialog() {


        Currency currency = Currency.getInstance("EGP");
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        BottomSheetDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.bottom_sheet_dialog, null, false);
        dialog.setContentView(binding.getRoot());
        binding.slider.setValues(model.getFromValue(), model.getToValue());
        binding.slider.setValueFrom(startRange);
        binding.slider.setValueTo(endRange);
        binding.slider.setStepSize(steps);


        binding.tvFrom.setText(startRange + currency.getSymbol());
        binding.tvTo.setText(endRange + currency.getSymbol());

        binding.slider.setLabelFormatter(value -> {
            NumberFormat format = NumberFormat.getCurrencyInstance();
            format.setMaximumFractionDigits(0);
            format.setCurrency(currency);
            return format.format(value);
        });

        binding.imageClose.setOnClickListener(v -> {
            fragmentHomeMvvm.clearFilterModel();
            dialog.dismiss();
        });
        binding.recView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        rateFilterAdapter = new RateFilterAdapter(activity, this);
        binding.recView.setAdapter(rateFilterAdapter);
        rateFilterAdapter.updateData(fragmentHomeMvvm.getFilterModelList().getValue());


        fragmentHomeMvvm.getFilterModelList().observe(activity, filterRateModels -> {
            model.setRate(fragmentHomeMvvm.getFilterRateModel().getValue().getTitle());
            rateFilterAdapter.updateData(fragmentHomeMvvm.getFilterModelList().getValue());

        });

        binding.btnShowFilterResults.setOnClickListener(v -> {

            float from = binding.slider.getValues().get(0);
            float to = binding.slider.getValues().get(1);
            filterRangeModel.setFromValue(from);
            filterRangeModel.setToValue(to);
            filterRangeModel.setRate(fragmentHomeMvvm.getFilterRateModel().getValue().getTitle());
            FilterModel filterModel = new FilterModel("",filterRangeModel.getFromValue()+"",filterRangeModel.getToValue()+"",filterRangeModel.getRate());
            fragmentHomeMvvm.getFilter().setValue(filterModel);
        });
        dialog.show();
    }

    public void updateFilterRate(FilterRateModel model) {
        fragmentHomeMvvm.updateFilterRateModel(model);
    }
}