package ir.sbpro.sadegh.FastFM;

import android.app.Activity;
import android.content.Context;

public class RunningActivity {
    private Context context;
    private Activity activity;
    private ProgressAdapter adapter;
    private AdvProgressDialog progressDialog;

    RunningActivity(Context context, AdvProgressDialog progressDialog, ProgressAdapter adapter){
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

    public AdvProgressDialog getProgressDialog() {
        return progressDialog;
    }
}
