package ir.sbpro.sadegh.myfilesapp;

import java.util.Calendar;
import java.util.Date;

public class Log {
    String title;
    long max;
    long progress;
    int state;
    Date date;

    final int STATE_RUN = 0;
    final int STATE_SUCCESSFUL = 1;
    final int STATE_UNDONE = 2;
    final int STATE_INCOMPLETED = 3;

    Log(String title){
        this.title=title;
        state=STATE_RUN;
        date= Calendar.getInstance().getTime();
        max=1;
        progress=0;
    }

    Log(String title, long max){
        this.title=title;
        state=STATE_RUN;
        date= Calendar.getInstance().getTime();
        this.max=max;
        this.progress=0;
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

    public void incerementProgress(){
        if((progress+1) <= max){
            progress++;
            if(progress==max) finish();
        }
    }

    public void finish(){
        state=STATE_SUCCESSFUL;
    }

    public void makeUndone(){
        state=STATE_UNDONE;
    }

    public void makeIncompleted(){
        state=STATE_INCOMPLETED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("* "+date.toString()+"\n"+title+"\n"+"State: ");
        if(state==STATE_SUCCESSFUL) sb.append("Finished");
        else if(state==STATE_UNDONE) sb.append("Undone");
        else if(state==STATE_INCOMPLETED) sb.append("Incompleted");
        else{
            long tempProgress = progress*100;
            double progressPercent = tempProgress/max;
            sb.append(String.valueOf((int) progressPercent)+"%");
        }

        return sb.toString();
    }
}
