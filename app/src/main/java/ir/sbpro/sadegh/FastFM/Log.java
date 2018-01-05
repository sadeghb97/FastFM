package ir.sbpro.sadegh.FastFM;

import java.util.Calendar;
import java.util.Date;

public class Log {
    RunningActivity rActivity;
    String title;
    long max;
    long progress;
    int state;
    Date date;
    String cause;

    final static int STATE_RUN = 0;
    final static int STATE_SUCCESSFUL = 1;
    final static int STATE_UNDONE = 2;
    final static int STATE_INCOMPLETED = 3;

    Log(RunningActivity rActivity, String title) {
        this.rActivity = rActivity;
        this.title = title;
        state = STATE_RUN;
        date = Calendar.getInstance().getTime();
        max = 1;
        progress = 0;
        cause="";
    }

    Log(RunningActivity rActivity, String title, long max){
        this.rActivity=rActivity;
        this.title=title;
        state=STATE_RUN;
        date= Calendar.getInstance().getTime();
        this.max=max;
        this.progress=0;
        cause="";
    }

    Log(String title, long max, long progress, int state, Date date, String cause){
        this.title=title;
        this.max=max;
        this.progress=progress;
        this.state=state;
        this.date=date;
        this.cause=cause;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        if(progress<=max) this.progress = progress;
        else throw new RuntimeException();
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public int getState(){
        return state;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("* "+date.toString()+"\n"+title+"\n"+"State: ");
        if(state==STATE_SUCCESSFUL) sb.append("Finished");
        else if(state==STATE_UNDONE) sb.append("Undone");
        else if(state==STATE_INCOMPLETED) sb.append("Incompleted");
        else sb.append(getProgressPercent());

        return sb.toString();
    }

    public String getProgressPercent(){
        long tempProgress = progress*100;
        double progressPercent = tempProgress/max;
        return String.valueOf((int) progressPercent)+"%";
    }

    public void waitAndShowDialog(){
        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (state == STATE_RUN && !Log.this.rActivity.getProgressDialog().isShowing())
                            Log.this.rActivity.getProgressDialog().show();
                    }
                }, 300);
            }
        });
    }

    public void incerementProgress(){
        if((progress+1) <= max){
            progress++;
            if(progress==max) finish();
            /*rActivity.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rActivity.getAdapter().notifyDataSetChanged();
                }
            });*/
        }
    }

    public void finish(){
        state=STATE_SUCCESSFUL;
        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void makeUndone(){
        state=STATE_UNDONE;
        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void makeIncompleted(){
        state=STATE_INCOMPLETED;
        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });
    }
}
