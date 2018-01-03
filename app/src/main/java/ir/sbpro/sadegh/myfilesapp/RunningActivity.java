package ir.sbpro.sadegh.myfilesapp;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.content.Context;

public class RunningActivity {
    private Context context;
    private Activity activity;
    private ProgressAdapter adapter;
    private AlertDialog progressDialog;

    RunningActivity(Context context, AlertDialog progressDialog, ProgressAdapter adapter){
        this.context=context;
        this.activity=(Activity) context;
        this.adapter=adapter;
        this.progressDialog=progressDialog;
    }

    public Context getContext() {
        return context;
    }

    public Activity getActivity() {
        return activity;
    }

    public ProgressAdapter getAdapter() {
        return adapter;
    }

    public AlertDialog getProgressDialog() {
        return progressDialog;
    }
}
