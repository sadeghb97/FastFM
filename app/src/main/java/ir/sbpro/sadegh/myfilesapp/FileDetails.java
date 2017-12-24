package ir.sbpro.sadegh.myfilesapp;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.util.Date;

/**
 * Created by sadegh on 12/23/17.
 */

public class FileDetails {
    private File file;
    private int numFiles;
    private int numDirs;
    private long size;


    FileDetails(File file){
        this.file=file;
    }

    public void showProperties(Context context){
        makeDetails();

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage("Properties");
        String type, path, name, size, lastModified, canRead, canWrite, isHidden, contains;
        String delOne = "\n";
        String delTwo = "\n\n";

        name="Name: " + file.getName();

        if(file.isFile()) type="Type: File";
        else type = "Type: Directory";

        String pathFile = file.getAbsolutePath();
        path="Path: " + pathFile.substring(0, pathFile.lastIndexOf("/")+1);

        size = "Size: "+ getSizeStr(this.size);
        lastModified = "Last Modified: " + new Date(file.lastModified());

        contains = numFiles+" Files, "+numDirs + " Folders";

        if(file.canRead()) canRead = "Readable: Yes";
        else canRead = "Raadable: No";

        if(file.canWrite()) canWrite = "Writable: Yes";
        else canWrite = "Writable: No";

        if(file.isHidden()) isHidden = "Hidden: Yes";
        else isHidden = "Hidden: No";

        String message = name + delTwo + type + delTwo + path + delTwo;
        if(this.file.isDirectory()) message+=(contains+delOne);
        message += (size + delTwo + lastModified + delTwo +
                canRead + delOne + canWrite + delOne + isHidden);

        dialog.setMessage(message);
        dialog.show();
    }

    public void makeDetails(){
        numFiles=0;
        numDirs=0;
        size=0;

        recDetails(this.file);
    }

    private void recDetails(File file){
        if(file.isFile()){
            size+=file.length();
            numFiles++;
            return;
        }

        numDirs++;

        File[] list = file.listFiles();
        if(list==null) return;

        for(File item : list) recDetails(item);
    }

    public static String getSizeStr(long size){
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        String out;

        if(size > gb){
            double gbSize = (double) size / gb;
            out = String.format("%.2f", gbSize) + " GB";
            out += " (" + String.format("%,d", size) + " Bytes)";
        }
        else if(size>mb){
            double mbSize = (double) size / mb;
            out = String.format("%.2f", mbSize) + " MB";
            out += " (" + String.format("%,d", size) + " Bytes)";
        }
        else if(size>kb){
            double kbSize = (double) size / kb;
            out = String.format("%.2f", kbSize) + " KB";
            out += " (" + String.format("%,d", size) + " Bytes)";
        }
        else{
            out = String.valueOf(size) + " Bytes";
        }

        return out;
    }

    public int getNumFiles() {
        return numFiles;
    }

    public int getNumDirs() {
        return numDirs;
    }

    public long getSize() {
        return size;
    }
}
