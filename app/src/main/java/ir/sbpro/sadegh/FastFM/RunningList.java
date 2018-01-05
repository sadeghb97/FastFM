package ir.sbpro.sadegh.FastFM;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RunningList extends ArrayList<Log>{
    RunningActivity rActivity;
    boolean result;
    boolean wakeTimer;

    public void setRunningActivity(RunningActivity rActivity){
        this.rActivity=rActivity;
    }

    RunningList(){
        wakeTimer=false;
    }

    @Override
    public boolean add(final Log log) {
        result = RunningList.super.add(log);
        wakeUpAdapter();

        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });

        return result;
    }

    @Override
    public boolean remove(final Object o) {
        result = RunningList.super.remove(o);

        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });

        return result;
    }

    public boolean needAwake(){
        boolean res=false;
        for(int i=0; this.size()>i; i++){
            Log log = get(i);
            if(log.getState() == log.STATE_RUN) res=true;
            else if(log.getState() == Log.STATE_INCOMPLETED){
                remove(log);
            }
        }

        return res;
    }

    public void wakeUpAdapter(){
        if(!wakeTimer) {
            wakeTimer=true;

            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    rActivity.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!needAwake()){
                                cancel();
                                wakeTimer=false;
                            }
                            rActivity.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }, 0, 500);
        }
    }

    public void cancelAll(){
        for(int i=1; size()>i; i++) get(i).makeIncompleted();
        if(size()>0) get(0).makeIncompleted();
    }
}
