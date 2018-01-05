package ir.sbpro.sadegh.myfilesapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by sadegh on 12/18/17.
 */

public class SureDialog extends AlertDialog.Builder{
    public SureDialog(Context context, String str, final Runnable posRunnable, final Runnable negRunnable) {
        super(context);
        setMessage(str);

        setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                posRunnable.run();
            }
        });

        setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(negRunnable!=null) negRunnable.run();
            }
        });
    }
}
