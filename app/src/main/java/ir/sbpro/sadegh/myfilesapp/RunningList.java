package ir.sbpro.sadegh.myfilesapp;

import java.util.ArrayList;

public class RunningList extends ArrayList<Log>{
    RunningActivity rActivity;
    boolean result;

    public void setRunningActivity(RunningActivity rActivity){
        this.rActivity=rActivity;
    }

    @Override
    public boolean add(final Log log) {
        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result = RunningList.super.add(log);
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });

        return result;
    }

    @Override
    public boolean remove(final Object o) {
        rActivity.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                result = RunningList.super.remove(o);
                rActivity.getAdapter().notifyDataSetChanged();
            }
        });

        return result;
    }
}
