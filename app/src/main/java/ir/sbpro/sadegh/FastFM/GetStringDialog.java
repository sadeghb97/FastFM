package ir.sbpro.sadegh.FastFM;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class GetStringDialog extends AlertDialog.Builder {
    EditText txtGetInput;
    AlertDialog dialog;
    Context context;

    public GetStringDialog(Context context, int resId, int txtId, String defStr, String title
            , String message) {
        super(context);

        this.context=context;
        setTitle(title);
        setMessage(message);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resId, null);

        txtGetInput = view.findViewById(txtId);
        txtGetInput.setText(defStr);
        txtGetInput.setSelection(txtGetInput.getText().toString().length());

        setView(view);

        txtGetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                positiveOnOff();
            }
        });
    }

    public void createDialog(){
        dialog=create();
    }

    public void showDialog(){
        dialog.show();
        positiveOnOff();
    }

    private void positiveOnOff(){
        if(txtGetInput.getText().toString().isEmpty())
            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        else
            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    public EditText getTxtGetInput(){
        return txtGetInput;
    }
}
