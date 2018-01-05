package ir.sbpro.sadegh.FastFM;

import android.support.v7.app.AlertDialog;
import android.view.View;

public class AdvProgressDialog {
    private AlertDialog dialog;
    private RunningList runningList;

    AdvProgressDialog(AlertDialog dialog, RunningList runningList){
        this.runningList=runningList;
        this.dialog=dialog;
    }

    public void setView(View view){
        dialog.setView(view);
    }

    public void show(){
        dialog.show();

        ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runningList.cancelAll();
            }
        });
    }

    public boolean isShowing(){
        return dialog.isShowing();
    }
}
