package com.apps.wedding.mvvm;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.apps.wedding.R;
import com.apps.wedding.model.DepartmentModel;
import com.apps.wedding.model.FilterModel;
import com.apps.wedding.model.FilterRangeModel;
import com.apps.wedding.model.FilterRateModel;
import com.apps.wedding.model.UserModel;
import com.apps.wedding.model.WeddingHallModel;
import com.apps.wedding.remote.Api;
import com.apps.wedding.share.Common;
import com.apps.wedding.tags.Tags;
import com.apps.wedding.uis.activity_home.HomeActivity;
import com.google.common.eventbus.Subscribe;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class FragmentVerificationMvvm extends AndroidViewModel {
    private static final String TAG = "FragmentVerficationMvvm";
    private Context context;
    private FirebaseAuth mAuth;
    private String verificationId;
    private String smsCode;
    private boolean canSend = false;
    private String time;
    private String phone, phone_code;
    private String apikey;
    public MutableLiveData<String> smscode = new MutableLiveData<>();
    public MutableLiveData<Boolean> canresnd = new MutableLiveData<>();
    public MutableLiveData<String> timereturn = new MutableLiveData<>();
    public MutableLiveData<UserModel> userModelMutableLiveData = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();

    public FragmentVerificationMvvm(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        mAuth = FirebaseAuth.getInstance();


    }

    public void sendSmsCode(String lang, String phone_code, String phone, HomeActivity activity) {

        startTimer();
        this.phone_code = phone_code;
        this.phone = phone;
        mAuth.setLanguageCode(lang);
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                smsCode = phoneAuthCredential.getSmsCode();
                smscode.postValue(smsCode);
                checkValidCode(smsCode);
            }

            @Override
            public void onCodeSent(@NonNull String verification_id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verification_id, forceResendingToken);
                verificationId = verification_id;
            }


            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e("dkdkdk", e.toString());
                if (e.getMessage() != null) {
                    //   Common.CreateDialogAlert(VerificationCodeActivity.this, e.getMessage());
                } else {
                    // Common.CreateDialogAlert(VerificationCodeActivity.this, getString(R.string.failed));

                }
            }
        };
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                        phone_code + phone,
                        120,
                        TimeUnit.SECONDS,
                        activity,
                        mCallBack

                );


    }

    private void startTimer() {
        canSend = false;
        canresnd.postValue(canSend);
        Observable.intervalRange(1, 120, 1, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        long diff = 120 - aLong;
                        int min = ((int) diff / 60);
                        int sec = ((int) diff % 60);
                        time = String.format(Locale.ENGLISH, "%02d:%02d", min, sec);
                        timereturn.postValue(time);


                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        canSend = true;
                        time = "00:00";
                        timereturn.postValue("00:00");

                        canresnd.postValue(true);
                    }
                });

    }


    public void checkValidCode(String code) {
login();
        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            mAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        login();
                    }).addOnFailureListener(e -> {
                if (e.getMessage() != null) {
                } else {
                }
            });
        } else {
            // Toast.makeText(context, "wait sms", Toast.LENGTH_SHORT).show();
        }

    }

    private void login() {
//        ProgressDialog dialog = Common.createProgressDialog(context, context.getResources().getString(R.string.wait));
//        dialog.setCancelable(false);
//        dialog.show();
        Api.getService(Tags.base_url).login(Tags.api_key,phone_code,phone).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()).subscribe(new SingleObserver<Response<UserModel>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
              disposable.add(d);
            }

            @Override
            public void onSuccess(@NonNull Response<UserModel> userModelResponse) {
                //dialog.dismiss();
                Log.e("dkldkdk",userModelResponse.code()+"");

                if(userModelResponse.isSuccessful()){
                    Log.e("dkldkdk",userModelResponse.body().getStatus()+"");
                    if(userModelResponse.body().getStatus()==200){

                        userModelMutableLiveData.postValue(userModelResponse.body());
                    }
                    else if(userModelResponse.body().getStatus()==401){

                    }
                    else if(userModelResponse.body().getStatus()==409){
                        Toast.makeText(context,context.getResources().getString(R.string.user_blocked),Toast.LENGTH_LONG).show();
                    }
                }
                else{

                }

            }

            @Override
            public void onError(@NonNull Throwable e) {
                //dialog.dismiss();

            }
        });
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
