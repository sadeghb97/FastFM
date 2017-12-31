package ir.sbpro.sadegh.myfilesapp;

import android.content.SharedPreferences;

public class LogManager {
    private Log[] logs;
    private int maxSize;
    private int first;
    private int length;
    SharedPreferences sp;

    LogManager(int maxSize, SharedPreferences sp){
        this.maxSize=maxSize;
        logs = new Log[maxSize];
        this.sp=sp;
        first =-1;
        length=0;
    }

    public void addLog(Log log){
        int head;

        if(length==0){
            first =0;
            length=1;
            head=0;
        }
        else {
            head = (first + length) % maxSize;
            if (head == 0) first++;
            else length++;
        }

        logs[head]=log;
    }

    public void clear(){
        first =-1;
        length=0;
    }

    public Log getLog(int index){
        if(length<=index) throw new RuntimeException();
        return logs[(first+length-1-index)%maxSize];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0; length>i; i++){
            if(sb.length()!=0) sb.append("\n\n");
            sb.append(getLog(i).toString());
        }

        return sb.toString();
    }

    public int getLength() {
        return length;
    }
}
