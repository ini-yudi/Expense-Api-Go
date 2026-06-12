package com.yudi.asmara.expensereport.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;

import com.airbnb.lottie.LottieAnimationView;
import com.yudi.asmara.expensereport.R;

public class LottieDialog {

    private final Dialog dialog;
    private final LottieAnimationView lottieView;

    public LottieDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_lottie);

        lottieView = dialog.findViewById(R.id.lottie_animation);
    }

    public LottieDialog setAnimation(int rawRes) {
        lottieView.setAnimation(rawRes);
        lottieView.playAnimation();
        return this;
    }

    public LottieDialog setRepeatMode(int mode) {
        lottieView.setRepeatMode(mode);
        return this;
    }

    public LottieDialog setRepeatCount(int count) {
        lottieView.setRepeatCount(count);
        return this;
    }

    public LottieDialog setSpeed(float speed) {
        lottieView.setSpeed(speed);
        return this;
    }

    public LottieDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        dialog.setCanceledOnTouchOutside(cancelable);
        return this;
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            lottieView.cancelAnimation();
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public static LottieDialog showLoading(Context context) {
        LottieDialog ld = new LottieDialog(context);
        ld.setAnimation(R.raw.rabbit);
        ld.setSpeed(1.0f);
        ld.setRepeatCount(-1);
        ld.show();
        return ld;
    }

    public static LottieDialog showSuccess(Context context, Runnable onDismiss) {
        LottieDialog ld = new LottieDialog(context);
        ld.setAnimation(R.raw.success);
        ld.setRepeatCount(1);
        ld.show();
        if (onDismiss != null) {
            ld.dialog.getWindow().getDecorView().postDelayed(() -> {
                ld.dismiss();
                onDismiss.run();
            }, 1500);
        }
        return ld;
    }

    public static LottieDialog showWarning(Context context, Runnable onConfirm) {
        LottieDialog ld = new LottieDialog(context);
        ld.setAnimation(R.raw.warning);
        ld.setRepeatCount(0);
        ld.show();
        if (onConfirm != null) {
            ld.dialog.getWindow().getDecorView().postDelayed(() -> {
                ld.dismiss();
                onConfirm.run();
            }, 1200);
        }
        return ld;
    }

    public static LottieDialog showError(Context context) {
        LottieDialog ld = new LottieDialog(context);
        ld.setAnimation(R.raw.error);
        ld.setRepeatCount(1);
        ld.show();
        ld.dialog.getWindow().getDecorView().postDelayed(ld::dismiss, 1500);
        return ld;
    }
}
