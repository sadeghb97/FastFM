package ir.sbpro.sadegh.FastFM;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

public class FileOpen {

    final static int SORT_ASCENDING = 1;
    final static int SORT_DESCENDING = 2;
    final static int SORT_BY_NAME = 1;
    final static int SORT_BY_SIZE = 2;
    final static int SORT_BY_MODIFIED = 3;
    final static int SORT_BY_TYPE = 4;

    public static void openFile(Context context, File url) throws IOException {
        // Create URI
        File file=url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if(url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if(url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if(url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if(url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav");
        } else if(url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if(url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if(url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") ||
                url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if(url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                url.toString().contains(".mpeg") || url.toString().contains(".mpe") ||
                url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, "Select"));
    }

    public static  void sortFiles(File[] filesList, int sortBy, int sortDir){
        for(int i=0; filesList.length>i; i++){
            int chosenIndex = i;
            for(int j=i+1; filesList.length>j; j++){
                boolean change = false;

                if(filesList[chosenIndex].isFile() && filesList[j].isDirectory())
                    change=true;

                else if(filesList[chosenIndex].isDirectory() && filesList[j].isFile())
                    change=false;

                else if(sortBy==SORT_BY_NAME){
                    if(sortDir==SORT_ASCENDING && filesList[chosenIndex].getName().compareToIgnoreCase(filesList[j].getName()) > 0)
                        change=true;
                    else if(sortDir==SORT_DESCENDING && filesList[chosenIndex].getName().compareToIgnoreCase(filesList[j].getName()) < 0)
                        change=true;
                }

                else if(sortBy==SORT_BY_SIZE){
                    if(sortDir==SORT_ASCENDING && filesList[chosenIndex].length()>filesList[j].length())
                        change=true;
                    else if(sortDir==SORT_DESCENDING && filesList[chosenIndex].length()<filesList[j].length())
                        change=true;
                }

                else if(sortBy==SORT_BY_MODIFIED){
                    if(sortDir==SORT_ASCENDING && filesList[chosenIndex].lastModified()>filesList[j].lastModified())
                        change=true;
                    else if(sortDir==SORT_DESCENDING && filesList[chosenIndex].lastModified()<filesList[j].lastModified())
                        change=true;
                }

                else if(sortBy==SORT_BY_TYPE){
                    if(sortDir==SORT_ASCENDING && compareExt(filesList[chosenIndex],filesList[j])==1)
                        change=true;
                    else if(sortDir==SORT_DESCENDING && compareExt(filesList[chosenIndex],filesList[j])==2)
                        change=true;
                }

                if(change){
                    chosenIndex=j;
                }
            }

            if(chosenIndex!=i){
                File tempFile=filesList[i];
                filesList[i]=filesList[chosenIndex];
                filesList[chosenIndex]=tempFile;
            }
        }
    }

    public static int compareExt(File f1, File f2){
        String fn1 = f1.getName();
        String fn2 = f2.getName();
        int p1 = fn1.lastIndexOf(".");
        int p2 = fn2.lastIndexOf(".");

        if(p1<0 && p2<0) return 0;
        if(p1<0) return 2;
        if(p2<0) return 1;

        String ext1, ext2;
        if(fn1.length()>(p1+1)) ext1 = fn1.substring(p1+1);
        else ext1="";
        if(fn2.length()>(p2+1)) ext2 = fn2.substring(p2+1);
        else ext2="";

        if(ext1.compareToIgnoreCase(ext2)>0) return 1;
        if(ext1.compareToIgnoreCase(ext2)<0) return 2;
        return 0;
    }
}
