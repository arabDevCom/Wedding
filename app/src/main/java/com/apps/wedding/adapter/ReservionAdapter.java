

package com.apps.wedding.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.wedding.R;
import com.apps.wedding.databinding.AdditionalItemsRowBinding;
import com.apps.wedding.databinding.OrderRowBinding;
import com.apps.wedding.model.ResevisionModel;
import com.apps.wedding.model.WeddingHallModel;
import com.apps.wedding.uis.activity_home.fragments.FragmentReservisionConfirmation;
import com.apps.wedding.uis.activity_home.fragments_home_navigaion.FragmentCurrentReservation;

import java.util.List;

public class ReservionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ResevisionModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;


    public ReservionAdapter(Context context, Fragment fragment) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
    }

    @androidx.annotation.NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {


        OrderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.order_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));
        ResevisionModel model = list.get(position);
        double total = model.getMain_item_price() + model.getExtra_item_price();

        if (model.getOffer() != null) {
            total = Double.parseDouble(model.getOffer().getPrice()) + model.getExtra_item_price();
        }

        myHolder.binding.setTotal(total + "");
        myHolder.binding.llShow.setOnClickListener(v -> {
            if (fragment instanceof FragmentCurrentReservation) {
                FragmentCurrentReservation fragmentCurrentReservation = (FragmentCurrentReservation) fragment;
                fragmentCurrentReservation.createSheetDialog(list.get(myHolder.getAdapterPosition()));

            }
        });

        myHolder.binding.llEdit.setOnClickListener(v -> {
            if (fragment instanceof FragmentCurrentReservation) {
                FragmentCurrentReservation fragmentCurrentReservation = (FragmentCurrentReservation) fragment;
                fragmentCurrentReservation.createSheetDialog(list.get(myHolder.getAdapterPosition()));

            }
        });

        myHolder.binding.llDelete.setOnClickListener(v -> {
            if (fragment instanceof FragmentCurrentReservation) {
                FragmentCurrentReservation fragmentCurrentReservation = (FragmentCurrentReservation) fragment;
                fragmentCurrentReservation.delete(list.get(myHolder.getAdapterPosition()));

            }
        });

    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public OrderRowBinding binding;

        public MyHolder(OrderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public void updateList(List<ResevisionModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

}
