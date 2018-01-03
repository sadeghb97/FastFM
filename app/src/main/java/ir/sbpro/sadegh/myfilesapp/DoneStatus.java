package ir.sbpro.sadegh.myfilesapp;

/**
 * Created by sadegh on 1/4/18.
 */

public class DoneStatus {
    final static int STATUS_UNSET = 0;
    final static int STATUS_UNDONE = 1;
    final static int STATUS_FINISHED = 2;
    private int status;

    DoneStatus(){
        status=STATUS_UNSET;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUndone(){
        status=STATUS_UNDONE;
    }

    public void setFinished(){
        status=STATUS_FINISHED;
    }

    public boolean isFinished(){
        return status==STATUS_FINISHED;
    }

    public boolean isUndone(){
        return status==STATUS_UNDONE;
    }
}
