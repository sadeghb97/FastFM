package ir.sbpro.sadegh.FastFM;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.view.View;

import java.io.File;

public class GetFileNameDialog extends GetStringDialog {
    File currentDir;
    int type;

    public GetFileNameDialog(final Context context, int resId, int txtId, String defStr, String title,
                             String message, File currentDir, int type) {

        super(context, resId, txtId, defStr, title, message);
        this.currentDir=currentDir;
        this.type=type;
        setNeutralButton("Help", null);
    }

    @Override
    public void showDialog() {
        super.showDialog();

        ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.helpFileName(getContext(), txtGetInput, currentDir, type, false, -1, -1);
            }
        });

        MainActivity.showKeyboard(context, 30);
    }
}
