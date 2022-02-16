package com.e_co.wedding.uis.activity_home.fragments_home_navigaion;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.e_co.wedding.R;
import com.e_co.wedding.adapter.MapWeddingHallAdapter;
import com.e_co.wedding.adapter.RateFilterAdapter;
import com.e_co.wedding.databinding.BottomSheetDialogBinding;
import com.e_co.wedding.model.FilterModel;
import com.e_co.wedding.model.FilterRangeModel;
import com.e_co.wedding.model.FilterRateModel;
import com.e_co.wedding.model.WeddingHallModel;
import com.e_co.wedding.mvvm.FragmentNearMvvm;
import com.e_co.wedding.uis.activity_base.BaseFragment;
import com.e_co.wedding.databinding.FragmentNearbyBinding;
import com.e_co.wedding.uis.activity_home.HomeActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class FragmentNearby extends BaseFragment implements OnMapReadyCallback {
    private HomeActivity activity;
    private FragmentNearbyBinding binding;
    private GoogleMap mMap;
    private float zoom = 15.0f;
    private ActivityResultLauncher<String> permissionLauncher;
    private FragmentNearMvvm fragmentNearMvvm;
    private MapWeddingHallAdapter adapter;
    private RateFilterAdapter rateFilterAdapter;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (HomeActivity) context;
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_nearby, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Observable.timer(130, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);

                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        initView();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void initView() {
        fragmentNearMvvm = ViewModelProviders.of(this).get(FragmentNearMvvm.class);
        fragmentNearMvvm.getWeddingHall().observe(activity, weddingHallModels -> {
            if (adapter!=null&&weddingHallModels!=null){
                adapter.updateList(fragmentNearMvvm.getWeddingHall().getValue());
            }
        });

        fragmentNearMvvm.getIsLoading().observe(activity, isLoading -> {
            if (isLoading) {
                binding.flLoading.setClickable(true);
                binding.flLoading.setFocusable(true);
                binding.progBar.setVisibility(View.VISIBLE);
                binding.flLoading.setVisibility(View.VISIBLE);
                binding.cardNoData.setVisibility(View.GONE);
                adapter.updateList(null);

            }
        });

        fragmentNearMvvm.getWeddingHall().observe(activity, weddingHallModels -> {
            if (weddingHallModels.size() > 0) {
                adapter.updateList(fragmentNearMvvm.getWeddingHall().getValue());
                updateMapData(weddingHallModels);
                binding.cardNoData.setVisibility(View.GONE);
                binding.flLoading.setVisibility(View.GONE);

            } else {
                binding.flLoading.setVisibility(View.VISIBLE);
                binding.progBar.setVisibility(View.GONE);
                binding.cardNoData.setVisibility(View.VISIBLE);
                adapter.updateList(null);
                mMap.clear();
                binding.flLoading.setClickable(false);
                binding.flLoading.setFocusable(false);
            }

        });


        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recView);
        binding.recView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        adapter = new MapWeddingHallAdapter(activity, this);
        binding.recView.setAdapter(adapter);
        binding.cardFilter.setOnClickListener(v -> {
            createSheetDialog();
        });

        binding.cardViewSearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.fragmentSearch);
        });

        updateUI();
    }


    private void updateUI() {
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);
            fragmentNearMvvm.getWeddingHallData();

        }
    }

    private void addMarker(double lat, double lng) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

    }


    public void setItemWeddingDetails(String id) {
        Bundle bundle = new Bundle();
        bundle.putString("data", id);
        Navigation.findNavController(binding.getRoot()).navigate(R.id.serviceDetailsFragment, bundle);
    }


    private void createSheetDialog() {


        Currency currency = Currency.getInstance("EGP");
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        BottomSheetDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.bottom_sheet_dialog, null, false);
        dialog.setContentView(binding.getRoot());

        binding.slider.setValues(fragmentNearMvvm.getFilterRangeModel().getValue().getSelectedFromValue(), fragmentNearMvvm.getFilterRangeModel().getValue().getSelectedToValue());
        binding.slider.setValueFrom(fragmentNearMvvm.getFilterRangeModel().getValue().getFromValue());
        binding.slider.setValueTo(fragmentNearMvvm.getFilterRangeModel().getValue().getToValue());
        binding.slider.setStepSize(fragmentNearMvvm.getFilterRangeModel().getValue().getStepValue());


        binding.tvFrom.setText(fragmentNearMvvm.getFilterRangeModel().getValue().getFromValue() + currency.getSymbol());
        binding.tvTo.setText(fragmentNearMvvm.getFilterRangeModel().getValue().getToValue() + currency.getSymbol());

        binding.slider.setLabelFormatter(value -> {
            NumberFormat format = NumberFormat.getCurrencyInstance();
            format.setMaximumFractionDigits(0);
            format.setCurrency(currency);
            return format.format(value);
        });

        binding.imageClose.setOnClickListener(v -> {
            fragmentNearMvvm.clearFilterModel();
            dialog.dismiss();
        });

        binding.recView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        rateFilterAdapter = new RateFilterAdapter(activity, this);
        binding.recView.setAdapter(rateFilterAdapter);
        rateFilterAdapter.updateData(fragmentNearMvvm.getFilterModelList().getValue(), fragmentNearMvvm.getFilter().getValue().getRate());


        fragmentNearMvvm.getFilterModelList().observe(activity, filterRateModels -> {
            rateFilterAdapter.updateData(fragmentNearMvvm.getFilterModelList().getValue(), fragmentNearMvvm.getFilter().getValue().getRate());

        });

        binding.btnShowFilterResults.setOnClickListener(v -> {

            float from = binding.slider.getValues().get(0);
            float to = binding.slider.getValues().get(1);
            FilterRangeModel filterRangeModel = fragmentNearMvvm.getFilterRangeModel().getValue();
            filterRangeModel.setSelectedFromValue(from);
            filterRangeModel.setSelectedToValue(to);
            fragmentNearMvvm.getFilterRangeModel().setValue(filterRangeModel);


            FilterModel filterModel = fragmentNearMvvm.getFilter().getValue();
            filterModel.setRate(fragmentNearMvvm.getFilterRateModel().getValue().getTitle());
            filterModel.setFromRange(filterRangeModel.getSelectedFromValue() + "");
            filterModel.setToRange(filterRangeModel.getSelectedToValue() + "");
            fragmentNearMvvm.getFilter().setValue(filterModel);

            fragmentNearMvvm.getWeddingHallData();
            fragmentNearMvvm.clearFilterModel();

            dialog.dismiss();
        });
        dialog.show();
    }

    public void updateFilterRate(FilterRateModel model) {
        fragmentNearMvvm.updateFilterRateModel(model);
    }

    private void updateMapData(List<WeddingHallModel> data) {

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (WeddingHallModel weddingHallModel : data) {
            bounds.include(new LatLng(Double.parseDouble(weddingHallModel.getLatitude()), Double.parseDouble(weddingHallModel.getLongitude())));
            addMarker(Double.parseDouble(weddingHallModel.getLatitude()), Double.parseDouble(weddingHallModel.getLongitude()));
        }

        if (data.size() >= 2) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));

        } else if (data.size() == 1) {
            LatLng latLng = new LatLng(Double.parseDouble(data.get(0).getLatitude()), Double.parseDouble(data.get(0).getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposable.clear();
    }
}