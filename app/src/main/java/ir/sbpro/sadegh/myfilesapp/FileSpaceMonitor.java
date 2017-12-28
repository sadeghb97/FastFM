package ir.sbpro.sadegh.myfilesapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import java.io.File;

/**
 * Created by sadegh on 12/28/17.
 */

public class FileSpaceMonitor extends File {
    private long realSize;
    private FileSpaceMonitor[] childs;

    public FileSpaceMonitor(File file){
        super(file.getAbsolutePath());

        File[] filesList = file.listFiles();
        if(filesList == null) {
            childs=null;
            return;
        }

        childs = new FileSpaceMonitor[filesList.length];
        for(int i=0; filesList.length>i; i++){
            childs[i]=new FileSpaceMonitor(filesList[i]);
        }
    }

    public long calcRealSize(){
        if(isFile()){
            realSize = length();
            return realSize;
        }

        realSize=0;
        if(childs == null){
            return realSize;
        }

        for(int i=0; childs.length>i; i++){
            realSize += (childs[i].calcRealSize());
        }

        return realSize;
    }

    /*public void showSizeMonitorDialog(Context context){
        final AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        final String constStr="Showing Directory: "+this.getAbsolutePath().toString()+"\n\n";

        if(!this.isDirectory()) dialog.setMessage(constStr + "Directory Does not Exists!");
        else {
            pd = new ProgressDialog(context);
            pd.setTitle("Analysing");
            pd.setMessage("Please wait ...");
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.show();

            final StringBuilder sb = new StringBuilder();

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {
                        pd.dismiss();
                        dialog.setMessage(constStr + sb.toString());
                        dialog.show();
                    }

                }
            };

            final Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();

                    FileSpaceMonitor.this.calcRealSize();
                    FileSpaceMonitor.this.sort();

                    sb.append("Size: " + FileDetails.getSizeStr(FileSpaceMonitor.this.getRealSize()) + "\n");
                    sb.append("Free Space: " + FileDetails.getSizeStr(FileSpaceMonitor.this.getFreeSpace()) + "\n");
                    sb.append("\n");

                    boolean first = true;
                    for (int i = 0; childs.length > i; i++) {
                        String itemStr = "";

                        if (childs[i].isDirectory())
                            itemStr = "* Directory: " + childs[i].getName() + "\n";
                        else if (childs[i].isFile())
                            itemStr = "* File: " + childs[i].getName() + "\n";
                        itemStr += ("Size: " + FileDetails.getSizeStr(childs[i].getRealSize()));

                        if (!first) sb.append("\n\n");
                        sb.append(itemStr);

                        first = false;
                    }

                    handler.sendEmptyMessage(0);
                }
            };

            thread.start();
        }
    }*/

    public void showSizeMonitorDialog(final Context context){
        final StringBuilder sb = new StringBuilder();

        AsyncTask async = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                /*pd = new ProgressDialog(context);
                pd.setTitle("Analysing");
                pd.setMessage("Please wait ...");
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setCancelable(false);
                pd.show();*/
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                FileSpaceMonitor.this.calcRealSize();
                FileSpaceMonitor.this.sort();

                sb.append("Size: " + FileDetails.getSizeStr(FileSpaceMonitor.this.getRealSize()) + "\n");
                sb.append("Free Space: " + FileDetails.getSizeStr(FileSpaceMonitor.this.getFreeSpace()) + "\n");
                sb.append("\n");

                boolean first = true;
                for (int i = 0; childs.length > i; i++) {
                    String itemStr = "";

                    if (childs[i].isDirectory())
                        itemStr = "* Directory: " + childs[i].getName() + "\n";
                    else if (childs[i].isFile())
                        itemStr = "* File: " + childs[i].getName() + "\n";
                    itemStr += ("Size: " + FileDetails.getSizeStr(childs[i].getRealSize()));

                    if (!first) sb.append("\n\n");
                    sb.append(itemStr);

                    first = false;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                //pd.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(sb.toString());
                builder.show();
            }
        };

        async.execute();
    }

    public void sort(){
        for(int i=0; childs.length>i; i++){
            int chosenIndex = i;
            for(int j=i+1; childs.length>j; j++){
                if(childs[chosenIndex].getRealSize()<childs[j].getRealSize()) {
                    chosenIndex=j;
                }
            }

            if(chosenIndex!=i){
                FileSpaceMonitor tempFile=childs[i];
                childs[i]=childs[chosenIndex];
                childs[chosenIndex]=tempFile;
            }
        }
    }

    public long getRealSize() {
        return realSize;
    }

    public void setRealSize(long realSize) {
        this.realSize = realSize;
    }
}
