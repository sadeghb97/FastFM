package ir.sbpro.sadegh.myfilesapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sadegh on 1/2/18.
 */

public class ProgressAdapter extends ArrayAdapter{

    public ProgressAdapter(@NonNull Context context, @NonNull List objects) {
        super(context, R.layout.layout_progress_list_item, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log log = (Log) getItem(position);
        ViewHolder holder;
        if(convertView==null){
            LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=inflater.inflate(R.layout.layout_progress_list_item, parent, false);

            holder=new ViewHolder(convertView);
            holder.fill(log);
            convertView.setTag(holder);
            android.util.Log.d("Reclyce "+position, "No");
        }
        else{
            holder= (ViewHolder) convertView.getTag();
            android.util.Log.d("Reclyce "+position, "Yes");
        }

        holder.fill(log);

        return convertView;
    }

    class ViewHolder implements View.OnClickListener {
        public TextView txvTitle;
        public ProgressBar prgProgress;

        ViewHolder(View parent){
            txvTitle=parent.findViewById(R.id.txvTitle);
            prgProgress=parent.findViewById(R.id.prgProgress);
        }

        public void fill(Log log){
            txvTitle.setText(log.getTitle()+"\nProgress: "+log.getProgressPercent());
            prgProgress.setMax((int) log.getMax());
            prgProgress.setProgress((int) log.getProgress());
        }

        @Override
        public void onClick(View v) {

        }
    }
}
