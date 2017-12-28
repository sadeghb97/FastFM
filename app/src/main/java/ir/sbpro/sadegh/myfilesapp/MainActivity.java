package ir.sbpro.sadegh.myfilesapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final static int DIRECTORIES_ONLY = 1;
    final static int FILES_ONLY = 2;
    final static int BOTH = 0;
    final static int UNKNOWN = -1000;

    final static int MAX_FILE_SIZE = 1024*500 ;

    ProgressDialog pd;

    EditText txtFileName, txtContent, txtDir;
    TextView txvCurrentDir;
    String strStorages, strExternalState;
    Button btnWrite, btnRead, btnOpenFile, btnHelpFile,
            btnDetsFile, btnCopyFile, btnCutFile, btnRemoveFile,
            btnSDCardDir, btnInsertExternalDir,
            btnHelpDir, btnChangeDir, btnShowDir, btnMakeDir,
            btnDetsDir, btnCopyDir, btnCutDir, btnRemoveDir;

    String internalFilesDir;
    String externalDir;
    String sdcardDir;
    String tempStr;
    File currentDir;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;
    Toast deniedToast;
    Toast removedExtToast;
    Toast dirNotFoundToast;
    Toast accessDeniedToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internalFilesDir = getFilesDir().getAbsolutePath().toString();
        externalDir = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        sdcardDir=null;

        if(!haveStoragePermission()) getStoragePermission();

        txtFileName=findViewById(R.id.txtFileName);
        txtContent=findViewById(R.id.txtContent);
        txtDir=findViewById(R.id.txtDir);
        btnWrite=findViewById(R.id.btnWrite);
        btnRead=findViewById(R.id.btnRead);
        btnHelpFile=findViewById(R.id.btnHelpFile);
        btnOpenFile=findViewById(R.id.btnOpenFile);
        btnDetsFile=findViewById(R.id.btnDetFile);
        btnCopyFile=findViewById(R.id.btnCopyFile);
        btnCutFile=findViewById(R.id.btnCutFile);
        btnRemoveFile=findViewById(R.id.btnRemoveFile);
        btnSDCardDir=findViewById(R.id.btnSDCardDir);
        btnInsertExternalDir=findViewById(R.id.btnInsertExternalDir);
        btnHelpDir=findViewById(R.id.btnHelpDir);
        btnChangeDir=findViewById(R.id.btnChangeDir);
        btnShowDir=findViewById(R.id.btnShowDir);
        btnMakeDir=findViewById(R.id.btnMakeDir);
        btnDetsDir=findViewById(R.id.btnDetDir);
        btnCopyDir=findViewById(R.id.btnCopyDir);
        btnCutDir=findViewById(R.id.btnCutDir);
        btnRemoveDir=findViewById(R.id.btnRemoveDir);
        txvCurrentDir=findViewById(R.id.txvCurrentDir);

        sharedPreferences=getSharedPreferences("prefs", MODE_PRIVATE);
        spEditor=sharedPreferences.edit();
        deniedToast = Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG);
        removedExtToast = Toast.makeText(this, "External Storage Removed!", Toast.LENGTH_LONG);
        dirNotFoundToast = Toast.makeText(this, "Directory Not Found!", Toast.LENGTH_LONG);
        accessDeniedToast = Toast.makeText(this, "Access Denied!", Toast.LENGTH_LONG);

        String receivedDir = sharedPreferences.getString("current-dir", externalDir);
        currentDir = new File(receivedDir);
        sdcardDir=sharedPreferences.getString("sdcdir", null);

        int sortBy, sortDir;
        sortBy=sharedPreferences.getInt("sortby", UNKNOWN);
        sortDir=sharedPreferences.getInt("sortdir", UNKNOWN);

        if(sortBy != UNKNOWN){
            RadioButton rbName = findViewById(R.id.rbSortName);
            RadioButton rbSize = findViewById(R.id.rbSortSize);
            RadioButton rbModified = findViewById(R.id.rbSortModified);
            if(sortBy == FileOpen.SORT_BY_NAME)
                rbName.setChecked(true);
            else if(sortBy == FileOpen.SORT_BY_SIZE)
                rbSize.setChecked(true);
            else if(sortBy == FileOpen.SORT_BY_MODIFIED)
                rbModified.setChecked(true);
        }

        if(sortDir != UNKNOWN){
            RadioButton rbAscending = findViewById(R.id.rbSortAscending);
            RadioButton rbDescending = findViewById(R.id.rbSortDescending);
            if(sortDir == FileOpen.SORT_ASCENDING)
                rbAscending.setChecked(true);
            else if(sortDir == FileOpen.SORT_DESCENDING)
                rbDescending.setChecked(true);
        }

        setTxvCurrentDir();
        setTxvExternalState();
        setTxvStorages();

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                String content=txtContent.getText().toString();
                File file=new File(fileName);
                File parent = file.getParentFile();

                if(parent==null){
                    showLongToast("Parent Directory Not Found!");
                    return;
                }

                if(content.length()>parent.getFreeSpace()){
                    showLongToast("No enough space!");
                    return;
                }

                if(isHavePermissionToWriteTextBoxFileName())
                    writeFile(file, content.getBytes());
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                File file = new File(fileName);

                if(isHavePermissionToOpenTextBoxFileName()) {
                    String content = readTxtFile(file);
                    if (content != null) txtContent.setText(content);
                }
            }
        });

        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                File file = new File(fileName);

                if(isHavePermissionToOpenTextBoxFileName()){
                    try {
                        FileOpen.openFile(MainActivity.this, file);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btnHelpFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpFileName(FILES_ONLY);
            }
        });

        btnDetsFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                File file = new File(fileName);

                if(isHavePermissionToOpenTextBoxFileName()) {
                    new FileDetails(file).showProperties(MainActivity.this);
                }
            }
        });

        btnCopyFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                final File file = new File(fileName);

                if(isHavePermissionToOpenTextBoxFileName()) {
                    final GetFileNameDialog reqDestDialog = new GetFileNameDialog(MainActivity.this,
                            R.layout.dialog_getstr, R.id.txtInput, "", "Destination",
                            "Please Enter Destination File Name", currentDir, BOTH);

                    reqDestDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String destFileName = recGetAbsTextBoxFileName(currentDir,
                                    reqDestDialog.getTxtGetInput().getText().toString());
                            File dest = new File(destFileName);

                            if (isHavePermissionToWriteTextBoxFileName(reqDestDialog.getTxtGetInput())) {
                                if(file.length() > dest.getParentFile().getFreeSpace()){
                                    showLongToast("No enough space!");
                                    return;
                                }

                                if (copyFile(file, dest))
                                    showLongToast("File Copied");
                                else
                                    showLongToast("The file can not be copied");
                            }
                        }
                    });

                    reqDestDialog.setNegativeButton("Cancel", null);
                    reqDestDialog.createDialog();
                    reqDestDialog.showDialog();
                }
            }
        });

        btnCutFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                final File file = new File(fileName);

                if(isHavePermissionToOpenTextBoxFileName()) {
                    final GetFileNameDialog reqDestDialog = new GetFileNameDialog(MainActivity.this,
                            R.layout.dialog_getstr, R.id.txtInput, "", "Destination",
                            "Please Enter Destination File Name", currentDir, BOTH);

                    reqDestDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String destFileName = recGetAbsTextBoxFileName(currentDir,
                                    reqDestDialog.getTxtGetInput().getText().toString());
                            File dest = new File(destFileName);

                            if (isHavePermissionToWriteTextBoxFileName(reqDestDialog.getTxtGetInput())) {

                                if (cutFile(file, dest))
                                    showLongToast("File Moved");
                                else
                                    showLongToast("The file can not be copied");
                            }
                        }
                    });

                    reqDestDialog.setNegativeButton("Cancel", null);
                    reqDestDialog.createDialog();
                    reqDestDialog.showDialog();
                }
            }
        });

        btnRemoveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                final File file=new File(fileName);

                if(isHavePermissionToWriteTextBoxFileName() && isHavePermissionToOpenTextBoxFileName()){
                    new SureDialog(MainActivity.this, "Are You Sure?", new Runnable() {
                        @Override
                        public void run() {
                            if(file.delete()){
                                showLongToast("File Deleted!");
                                txtFileName.setText("");
                                txtFileName.requestFocus();
                            }
                            else showLongToast("The file can not be deleted");
                        }
                    }, null).show();
                }
            }
        });

        btnSDCardDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sdcardDir==null || sdcardDir.equals("")) {
                    getSDCardDirInDialog();
                }
                else{
                    txtDir.setText(sdcardDir);
                    txtDir.requestFocus();
                    txtDir.setSelection(txtDir.getText().length());
                    setTxvStorages();
                }
            }
        });

        btnSDCardDir.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getSDCardDirInDialog();
                return false;
            }
        });

        btnInsertExternalDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDir.setText(externalDir);
                txtDir.setSelection(txtDir.getText().length());
            }
        });

        btnHelpDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpFileName(DIRECTORIES_ONLY);
            }
        });

        btnChangeDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtDir.getText().toString().isEmpty()){
                    txtDir.requestFocus();
                    return;
                }

                String txtDirStr= getAbsoluteTextBoxDir();
                File tempDir=new File(txtDirStr);
                if(tempDir.exists()){
                    currentDir=tempDir;
                    setTxvCurrentDir();

                    txtDir.setText("");
                    txtDir.requestFocus();
                }
                else{
                    Toast.makeText(MainActivity.this, "Directory Not Found!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnMakeDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtDir.getText().toString().isEmpty()){
                    txtDir.requestFocus();
                    return;
                }

                if(isExternalReq(txtDir)) {
                    if (!haveStoragePermission()){
                        deniedToast.show();
                        return;
                    }
                    else if (!isExtStorageReady()){
                        removedExtToast.show();
                        return;
                    }
                }

                String txtDirStr = getAbsoluteTextBoxDir();
                File tempDir = new File(txtDirStr);

                if (tempDir.exists()) {
                    Toast.makeText(MainActivity.this, "Directory Exists Now!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(tempDir.mkdirs()){
                        Toast.makeText(MainActivity.this, "Directory Created!", Toast.LENGTH_LONG).show();
                    }
                    else
                        accessDeniedToast.show();
                }

                txtDir.setText("");
                txtDir.requestFocus();
            }
        });

        btnShowDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dir;

                if(txtDir.getText().toString().isEmpty()){
                    dir=currentDir;
                }
                else{
                    String txtDirStr= getAbsoluteTextBoxDir();
                    dir=new File(txtDirStr);
                }

                if(canShowDir(dir))
                    showDirectoryDialog(dir, BOTH);
            }
        });

        btnDetsDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dir;

                if(txtDir.getText().toString().isEmpty()){
                    dir=currentDir;
                }
                else{
                    String txtDirStr= getAbsoluteTextBoxDir();
                    dir=new File(txtDirStr);
                }

                if(canShowDir(dir) && isUserStorage(dir))
                    new FileDetails(dir).showProperties(MainActivity.this);
            }
        });

        btnCopyDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File dir;

                if(txtDir.getText().toString().isEmpty()){
                    dir=currentDir;
                }
                else{
                    String txtDirStr= getAbsoluteTextBoxDir();
                    dir=new File(txtDirStr);
                }

                if(canShowDir(dir)){
                    final GetFileNameDialog reqDestDialog = new GetFileNameDialog(MainActivity.this,
                            R.layout.dialog_getstr, R.id.txtInput, "", "Destination",
                            "Please Enter Destination File Name", currentDir, BOTH);

                    reqDestDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String destFileName = recGetAbsTextBoxFileName(currentDir,
                                    reqDestDialog.getTxtGetInput().getText().toString());
                            final File dest = new File(destFileName);
                            FileDetails fdDir = new FileDetails(dir);
                            fdDir.makeDetails();

                            if(canMakeDir(dest)){
                                if(fdDir.getSize()>dest.getFreeSpace()){
                                    showLongToast("No enough space!");
                                    return;
                                }

                                if (dest.exists()) {
                                    showLongToast(dest.getAbsolutePath());
                                    new SureDialog(MainActivity.this, "The same filename already exists, " +
                                            "Do you want to owerwrite?", new Runnable() {
                                        @Override
                                        public void run() {
                                            if (recCopyDir(dir, dest))
                                                showLongToast("Directory Copied!");
                                            else showLongToast("The Directory can not be copied!");
                                        }
                                    }, null).show();
                                }

                                else {
                                    if (recCopyDir(dir, dest)) showLongToast("Directory Copied!");
                                    else showLongToast("The Directory can not be copied!");
                                }
                            }
                        }
                    });

                    reqDestDialog.setNegativeButton("Cancel", null);
                    reqDestDialog.createDialog();
                    reqDestDialog.showDialog();
                }
            }
        });

        btnCutDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File dir;

                if(txtDir.getText().toString().isEmpty()){
                    dir=currentDir;
                }
                else{
                    String txtDirStr= getAbsoluteTextBoxDir();
                    dir=new File(txtDirStr);
                }

                if(canChangeDir(dir)){
                    final GetFileNameDialog reqDestDialog = new GetFileNameDialog(MainActivity.this,
                            R.layout.dialog_getstr, R.id.txtInput, "", "Destination",
                            "Please Enter Destination File Name", currentDir, BOTH);

                    reqDestDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String destFileName = recGetAbsTextBoxFileName(currentDir,
                                    reqDestDialog.getTxtGetInput().getText().toString());
                            final File dest = new File(destFileName);
                            if(dest.exists()){
                                showLongToast(dest.getAbsolutePath());
                                new SureDialog(MainActivity.this, "The same filename already exists, " +
                                        "Do you want to owerwrite?", new Runnable() {
                                    @Override
                                    public void run() {
                                        if(recCutDir(dir, dest)){
                                            showLongToast("Directory Moved!");
                                            repairCurrentDir();
                                            setTxvCurrentDir();
                                        }
                                        else showLongToast("The Directory can not be moved!");
                                    }
                                }, null).show();
                            }

                            else{
                                if(recCutDir(dir, dest)){
                                    showLongToast("Directory Moved!");
                                    repairCurrentDir();
                                    setTxvCurrentDir();
                                }
                                else showLongToast("The Directory can not be moved!");
                            }
                        }
                    });

                    reqDestDialog.setNegativeButton("Cancel", null);
                    reqDestDialog.createDialog();
                    reqDestDialog.showDialog();
                }
            }
        });

        btnRemoveDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final File dir;

                if(txtDir.getText().toString().isEmpty()){
                    dir=currentDir;
                }
                else{
                    String txtDirStr= getAbsoluteTextBoxDir();
                    dir=new File(txtDirStr);
                }

                if(canChangeDir(dir)){
                    FileDetails fd = new FileDetails(dir);
                    fd.makeDetails();

                    StringBuilder sb = new StringBuilder();
                    sb.append("Are You Sure to remove " + dir.getName() + " Directory?");
                    if(fd.getNumFiles()>0 || fd.getNumDirs()>0){
                        sb.append("\n\nContains: ");
                        int numDirs = fd.getNumDirs();
                        int numFiles = fd.getNumFiles();

                        if(numDirs>0){
                            sb.append(numDirs + " Folders");
                            if(numFiles>0) sb.append(", " + numFiles + " Files");
                        }
                        else if(numFiles>0) sb.append(numFiles + " Files");

                        sb.append("\nSize: " + FileDetails.getSizeStr(fd.getSize()));
                    }

                    String message = sb.toString();

                    new SureDialog(MainActivity.this, message, new Runnable() {
                        @Override
                        public void run() {
                            if(recRemoveDir(dir)){
                                showLongToast("Directory Deleted!");
                                txtFileName.setText("");
                                txtFileName.requestFocus();

                                repairCurrentDir();
                                setTxvCurrentDir();
                            }
                            else showLongToast("The Directory can not be deleted");
                        }
                    }, null).show();
                }
            }
        });

        txvCurrentDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_textview, null);
                TextView textView = view.findViewById(R.id.textView);
                textView.setText(currentDir.getAbsolutePath());
                dialog.setView(view);

                dialog.setTitle("Current Directory");
                dialog.setPositiveButton("OK", null);
                dialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String fileName = txtFileName.getText().toString();

        spEditor.putString("current-dir", currentDir.getAbsolutePath().toString());
        spEditor.putString("sdcdir", sdcardDir);
        spEditor.putInt("sortby", getSortByMethod());
        spEditor.putInt("sortdir", getSortDir());
        spEditor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Storage State").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Storage State")
                        .setMessage(strStorages + "\n\n" + strExternalState)
                        .show();
                return false;
            }
        });

        menu.add("Space Monitor").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(canShowDir(currentDir) && isUserStorage(currentDir)){
                    FileSpaceMonitor fsm = new FileSpaceMonitor(currentDir);
                    fsm.showSizeMonitorDialog(MainActivity.this);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void writeFile(File file, byte[] content){
        if(file.isFile() && file.exists() && file.length() > MAX_FILE_SIZE){
            showLongToast("Max File Size To Override Overflow!");
            return ;
        }

        try {
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(content);
            fos.close();
            Toast.makeText(this, "File Written!", Toast.LENGTH_LONG).show();
        }
        catch (IOException e) {
            if(e.getMessage().toString().contains("Permission denied")){
                accessDeniedToast.show();
                return;
            }

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String readTxtFile(File file){
        if(!file.isFile()){
            Toast.makeText(this, "File Not Found!", Toast.LENGTH_LONG).show();
            return null;
        }

        if(!file.canRead()){
            accessDeniedToast.show();
            return null;
        }

        if(file.length() > MAX_FILE_SIZE){
            showLongToast("Max File Size Overflow!");
            return null;
        }

        try {
            FileInputStream fis=new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb=new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                if(sb.toString().length()>0) sb.append("\n");
                sb.append(line);
            }

            fis.close();

            return sb.toString();
        }

        catch (IOException e) {
            showLongToast(e.getMessage());
        }

        return null;
    }

    /*private boolean copyFile(File src, File dest){
        try {
            FileInputStream fis=new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dest);

            byte[] byteArray = new byte[(int) src.length()];
            int readByte;
            int bIndex=0;
            while((readByte = fis.read()) != -1){
                byteArray[bIndex++] = (byte) readByte;
            }
            fis.close();

            fos.write(byteArray);
            fos.close();
            return true;
        }

        catch (IOException e) {
            showLongToast(e.getMessage());
            return false;
        }
    }*/

    private boolean copyFile(File src, File dest){
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            return true;

        }
        catch (IOException e) {
            showLongToast(e.getMessage());
            return false;
        }
    }

    private boolean cutFile(File src, File dest){
        if(src.renameTo(dest)) return true;
        if(copyFile(src,dest) && src.delete()) return true;
        return false;
    }

    private void showDirectoryDialog(File dir, int type){
        boolean showFile, showDir;
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        String constStr="Showing Directory: "+dir.getAbsolutePath().toString()+"\n\n";
        if(type==1){
            showFile=false;
            showDir=true;
        }
        else if(type==2){
            showFile=true;
            showDir=false;
        }
        else {
            showFile=true;
            showDir=true;
        }

        if(!dir.isDirectory()) dialog.setMessage(constStr + "Directory Does not Exists!");
        else{
            String[] list = dir.list();
            StringBuilder sb=new StringBuilder();

            File[] filesList = new File[list.length];
            for(int i=0; list.length>i; i++){
                filesList[i]=new File(dir.getAbsolutePath().toString(), list[i]);
            }

            FileOpen.sortFiles(filesList, getSortByMethod(), getSortDir());

            for(File ios : filesList){
                String itemStr = "";

                if(ios.isDirectory()){
                    itemStr="* Directory: " + ios.getName();
                }
                else if(ios.isFile()){
                    itemStr="* File: " + ios.getName();
                }

                if(sb.toString().length()>0) sb.append("\n");
                sb.append(itemStr);
            }

            dialog.setMessage(constStr + sb.toString());
        }

        dialog.show();
    }

    public boolean recCopyDir(File src, final File dest){
        File parentDest = dest.getParentFile();
        if(parentDest==null || !parentDest.exists()){
            showLongToast("Parent Directory Not Found!");
            return false;
        }

        if(!dest.exists()){
            if(!dest.mkdir()){
                showLongToast("The Dest Directory can not be created!");
                return false;
            }
        }

        String[] list = src.list();
        if(list!=null){
            for(String item : list){
                File file = new File(src, item);
                File destFile = new File(dest, item);

                if(file.isFile()) copyFile(file, destFile);
                else recCopyDir(file, destFile);
            }
        }

        return true;
    }

    public boolean recCutDir(File src, final File dest){
        File parentDest = dest.getParentFile();
        if(parentDest==null || !parentDest.exists()){
            showLongToast("Parent Directory Not Found!");
            return false;
        }

        if(!dest.exists()){
            if(!dest.mkdir()){
                showLongToast("The Dest Directory can not be created!");
                return false;
            }
        }

        String[] list = src.list();
        if(list!=null) {
            for (String item : list) {
                File file = new File(src, item);
                File destFile = new File(dest, item);

                if (file.isFile()) cutFile(file, destFile);
                else recCutDir(file, destFile);
            }
        }

        if(recRemoveDir(src)) return true;
        return false;
    }

    public static boolean recRemoveDir(File dir){
        if(dir.isFile()){
            if(dir.delete()) return true;
            return false;
        }

        String[] list = dir.list();
        if(list!=null) {
            for(String item : list){
                File file = new File(dir, item);
                recRemoveDir(file);
            }
        }

        if(dir.delete()) return true;
        return false;
    }

    private void setTxvCurrentDir(){
        int maxDirChars=70;
        String dirPath = currentDir.getAbsolutePath();
        StringBuilder dirStrBuilder = new StringBuilder();
        String print;
        String[] list = dirPath.split("/");

        if(list.length>0) {
            if (list[0].equals("")) dirStrBuilder.append("/");
            for (String item : list) {
                if (item.equals("")) continue;
                if (item.length() > 18) dirStrBuilder.append(item.substring(0, 15) + "...");
                else dirStrBuilder.append(item);
                dirStrBuilder.append("/");
            }
        }
        else dirStrBuilder.append("/");

        String dirStr=dirStrBuilder.toString();
        int dirStrLength = dirStr.length();
        if(dirStrLength>maxDirChars)
            dirStr="..." + (dirStr.substring(dirStrLength-maxDirChars));

        print = "Current Dir: " + dirStr;
        txvCurrentDir.setText(print);
    }

    private void setTxvStorages(){
        strStorages = "Internal Directory:\n" + externalDir + "\n\n" + "SDCard Directory:\n";
        if(sdcardDir != null && !sdcardDir.equals(""))
            strStorages+=sdcardDir;
        else
            strStorages+="Unknown";
    }

    private void setTxvExternalState(){
        String tempStr = "Storage State:\n";

        if(haveStoragePermission()) tempStr=tempStr+"Permission Granted";
        else tempStr=tempStr+"Permission Denied";

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            strExternalState = tempStr + " - Ready";
        else if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            strExternalState = tempStr + " - Read Only";
        else
            strExternalState = tempStr + " - Removed";
    }

    private void repairCurrentDir(){
        while(!currentDir.isDirectory() && currentDir.getParentFile()!=null){
            currentDir = currentDir.getParentFile();
        }

        if(currentDir==null) currentDir=new File(externalDir);
    }

    private String getAbsoluteTextBoxFileName(){
        return recGetAbsTextBoxFileName(currentDir, txtFileName.getText().toString());
    }

    private String getAbsoluteTextBoxDir(){
        return recGetAbsTextBoxFileName(currentDir, txtDir.getText().toString());
    }

    public static String recGetAbsTextBoxFileName(File dir, String reqDir){
        if(reqDir.length()>0 && reqDir.charAt(0) == '/')
            return new File(reqDir).getAbsolutePath();
        else if(reqDir.indexOf("../")==0){
            String tempSend;
            File tempDir;

            if(reqDir.length()>3) tempSend=reqDir.substring(3);
            else tempSend="";
            if(dir.getParentFile()==null) tempDir=dir;
            else tempDir=dir.getParentFile();

            return recGetAbsTextBoxFileName(tempDir, tempSend);
        }
        else if(reqDir.indexOf("./")==0){
            String tempSend;

            if(reqDir.length()>2) tempSend=reqDir.substring(2);
            else tempSend="";

            return recGetAbsTextBoxFileName(dir, tempSend);
        }
        else if(reqDir.isEmpty() || reqDir.equals(".")) return dir.getAbsolutePath().toString();
        else if(reqDir.equals("..")){
            if(dir.getParentFile()!=null) return dir.getParentFile().getAbsolutePath();
            else return dir.getAbsolutePath();
        }

        else if(reqDir.lastIndexOf("/..")>=0 && reqDir.lastIndexOf("/..")==reqDir.length()-3){
            if(reqDir.length()==3) return "/";

            File temp = new File(recGetAbsTextBoxFileName(dir, reqDir.substring(0, reqDir.length()-3)));
            if(temp.getParentFile()!=null) return temp.getParentFile().getAbsolutePath();
            else return temp.getAbsolutePath();
        }
        else if(reqDir.lastIndexOf("/.")>=0 && reqDir.lastIndexOf("/.")==reqDir.length()-2){
            if(reqDir.length()==2) return "/";
            return recGetAbsTextBoxFileName(dir, reqDir.substring(0, reqDir.length()-2));
        }

        else return new File(dir, reqDir).getAbsolutePath();
    }

    private boolean isExternalReq(EditText editText){
        String fileName = recGetAbsTextBoxFileName(currentDir, editText.getText().toString());
        File file = new File(fileName);

        if(file.getAbsolutePath().indexOf(externalDir)==0)
            return true;
        else
            return false;
    }

    private boolean isExtStorageReady(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    private boolean isReadableExtStorageReady(){
        if(isExtStorageReady())
            return true;
        else if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            return true;
        else
            return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        setTxvExternalState();
    }

    private Boolean haveStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
            return false;
        }
        else return true;
    }

    private void getStoragePermission(){
        if(!haveStoragePermission()){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private boolean canShowDir(File dir){
        if(isExternalReq(txtDir)){
            if(!haveStoragePermission()){
                deniedToast.show();
                return false;
            }
            else if(!isReadableExtStorageReady()){
                removedExtToast.show();
                return false;
            }
        }

        if(!dir.exists() || !dir.isDirectory()){
            showLongToast("Directory Not Found");
            return false;
        }

        if(!dir.canRead()){
            accessDeniedToast.show();
            return false;
        }

        return true;
    }

    private boolean canChangeDir(File dir){
        if(!canShowDir(dir)) return false;

        if(!dir.canWrite()){
            accessDeniedToast.show();
            return false;
        }

        return true;
    }

    private boolean canMakeDir(File dir){
        File parent = dir.getParentFile();
        if(parent!=null && !parent.isDirectory()){
            showLongToast("Parent Directory Not Found!");
            return false;
        }

        return true;
    }

    private boolean isHavePermissionToOpenTextBoxFileName(EditText txtFN){
        String textBoxFileName = txtFN.getText().toString();
        String fileName=recGetAbsTextBoxFileName(currentDir, textBoxFileName);

        if(textBoxFileName.isEmpty()){
            txtFN.requestFocus();
            return false;
        }

        File file = new File(fileName);

        if(isExternalReq(txtFN)) {
            if (!haveStoragePermission()) {
                deniedToast.show();
                return false;
            }
            if (!isReadableExtStorageReady()) {
                removedExtToast.show();
                return false;
            }
        }

        if(!file.isFile()){
            Toast.makeText(this, "File Not Found!", Toast.LENGTH_LONG).show();
            return false;
        }

        if(!file.canRead()){
            accessDeniedToast.show();
            return false;
        }

        return true;
    }

    private boolean isHavePermissionToOpenTextBoxFileName(){
        return isHavePermissionToOpenTextBoxFileName(txtFileName);
    }

    private boolean isHavePermissionToWriteTextBoxFileName(EditText txtFN){
        String textBoxFileName = txtFN.getText().toString();
        String fileName=recGetAbsTextBoxFileName(currentDir, textBoxFileName);
        File file=new File(fileName);
        File parent=file.getParentFile();

        if(parent!=null && !parent.exists()){
            Toast.makeText(MainActivity.this, "Parent Directory Not Found!", Toast.LENGTH_LONG).show();
            return false;
        }

        if(textBoxFileName.isEmpty()){
            txtFN.requestFocus();
            return false;
        }

        if(isExternalReq(txtFN)){
            if(!haveStoragePermission()){
                deniedToast.show();
                return false;
            }
            if(!isExtStorageReady()){
                removedExtToast.show();
                return false;
            }
        }

        return true;
    }

    private boolean isHavePermissionToWriteTextBoxFileName(){
        return isHavePermissionToWriteTextBoxFileName(txtFileName);
    }

    private boolean isUserStorage(File file){
        String fn = file.getAbsolutePath();
        if(fn.indexOf(externalDir)==0) return true;
        if(sdcardDir!=null && !sdcardDir.equals("") && fn.indexOf(sdcardDir)==0) return true;
        if(fn.equals("/") || fn.equals("/sys") || fn.indexOf("/sys/")==0){
            accessDeniedToast.show();
            return false;
        }
        return true;
    }

    private void showLongToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static void helpFileName(Context context, EditText textBox, File curDir,
                                    int type, boolean getSort, int sortBy, int sortDir){
        String fileName;
        String textBoxStr;
        String txtBoxLastChildName;
        String txtBoxParentName;

        textBoxStr = textBox.getText().toString();
        fileName=recGetAbsTextBoxFileName(curDir, textBoxStr);

        File file=new File(fileName);
        File parentFile;

        if(textBoxStr.isEmpty() || textBoxStr.charAt(textBoxStr.length()-1) == '/')
            parentFile=file;
        else
            parentFile = new File(fileName).getParentFile();

        if(parentFile==null)
            return;

        int position = textBoxStr.lastIndexOf("/");

        if(position==0){
            txtBoxParentName="";
            if(textBoxStr.length()>(position+1))
                txtBoxLastChildName = textBoxStr.substring(position+1);
            else txtBoxLastChildName="";
        }
        else if(position>0){
            txtBoxParentName = textBoxStr.substring(0, position);
            if(textBoxStr.length()>(position+1))
                txtBoxLastChildName = textBoxStr.substring(position+1);
            else txtBoxLastChildName="";
        }
        else{
            txtBoxParentName = "";
            txtBoxLastChildName = textBoxStr;
        }

        String[] filesList= parentFile.list();

        if(filesList==null || filesList.length==0) return;

        String[] tempOffersList = new String[filesList.length];
        int olIndex=0;

        for(String item : filesList){
            File tf= new File(parentFile.getAbsolutePath(), item);
            boolean correctType;
            if(type == DIRECTORIES_ONLY) correctType=tf.isDirectory();
            else if(type == FILES_ONLY) correctType=tf.isFile();
            else correctType=true;

            if(item.indexOf(txtBoxLastChildName)==0 && correctType)
                tempOffersList[olIndex++]=item;
        }

        File []filesOffersList=new File[olIndex];

        for(int i=0; olIndex>i; i++){
            filesOffersList[i]=new File(parentFile.getAbsolutePath(), tempOffersList[i]);
        }

        tempOffersList = null;

        if(getSort)
            FileOpen.sortFiles(filesOffersList, sortBy, sortDir);

        if(filesOffersList.length==1){
            String tempStr;
            if(txtBoxParentName.isEmpty()){
                if(position == 0)
                    tempStr = "/" + filesOffersList[0].getName();
                else
                    tempStr = filesOffersList[0].getName();

            }
            else
                tempStr = txtBoxParentName+"/"+filesOffersList[0].getName();

            if(filesOffersList[0].isDirectory()) tempStr=tempStr+"/";

            textBox.setText(tempStr);
            textBox.setSelection(textBox.getText().length());
        }
        else if(filesOffersList.length>1){
            AlertDialog.Builder dialog;
            dialog=new AlertDialog.Builder(context);
            StringBuilder sb = new StringBuilder();

            for(int i=0; filesOffersList.length>i; i++){
                String tempConstStr;
                if(type == FILES_ONLY || type == DIRECTORIES_ONLY)
                    tempConstStr = "* ";
                else{
                    if(filesOffersList[i].isFile())
                        tempConstStr = "* File: ";
                    else
                        tempConstStr = "* Directory: ";
                }

                if(sb.toString().length()>0) sb.append("\n");
                sb.append(tempConstStr + filesOffersList[i].getName());
            }

            dialog.setMessage(sb.toString());
            dialog.show();
        }
    }

    private void helpFileName(int type){
        EditText textBox;

        if(type == DIRECTORIES_ONLY) textBox = txtDir;
        else textBox = txtFileName;

        helpFileName(this, textBox, currentDir, type, true, getSortByMethod(), getSortDir());
    }

    private int getSortByMethod(){
        RadioGroup rg = findViewById(R.id.rgSortBy);
        int id = rg.getCheckedRadioButtonId();
        if(id == R.id.rbSortName) return FileOpen.SORT_BY_NAME;
        else if(id == R.id.rbSortSize) return FileOpen.SORT_BY_SIZE;
        else return FileOpen.SORT_BY_MODIFIED;
    }

    private int getSortDir(){
        RadioGroup rg = findViewById(R.id.rgSortDir);
        int id = rg.getCheckedRadioButtonId();
        if(id == R.id.rbSortAscending) return FileOpen.SORT_ASCENDING;
        else return FileOpen.SORT_DESCENDING;
    }

    private void getSDCardDirInDialog(){
        String defaultStr;
        if(sdcardDir==null || sdcardDir.equals(""))
            defaultStr="/";
        else
            defaultStr=sdcardDir;

        final GetFileNameDialog alert = new GetFileNameDialog(this, R.layout.dialog_getstr,
                R.id.txtInput, defaultStr, "SDCard Dir", "Please enter your sdcard root directory!",
                 currentDir, DIRECTORIES_ONLY);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tempStr = alert.getTxtGetInput().getText().toString();
                if(tempStr.isEmpty()) return;
                String fileName = recGetAbsTextBoxFileName(currentDir, tempStr);
                File file = new File(fileName);
                if(file.exists() && file.isDirectory()){
                    sdcardDir = file.getAbsolutePath().toString() + "/";
                    txtDir.setText(sdcardDir);
                    txtDir.requestFocus();
                    txtDir.setSelection(txtDir.getText().length());
                    setTxvStorages();
                    showLongToast("SDCard Directory Setted");
                }

                else
                    showLongToast("Directory Not Found");
            }
        });

        alert.setNegativeButton("Cancel", null);
        alert.createDialog();
        alert.showDialog();
    }
}









