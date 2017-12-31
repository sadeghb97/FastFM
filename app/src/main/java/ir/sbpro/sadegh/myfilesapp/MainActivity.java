package ir.sbpro.sadegh.myfilesapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final static int DIRECTORIES_ONLY = 1;
    final static int FILES_ONLY = 2;
    final static int BOTH = 0;
    final static int UNKNOWN_INT = -1000;
    final static long UNKNOWN_LONG = -1000;

    final static int MAX_FILE_SIZE = 1024*500 ;

    ScrollView scrollView;
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

    AlertDialog logsDialog;
    Timer timerLogsDialog;
    Timer timerCurrentDir;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;
    LogManager logManager;
    ArrayList<Log> runningList;
    Toast deniedToast;
    Toast removedExtToast;
    Toast dirNotFoundToast;
    Toast accessDeniedToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internalFilesDir = getFilesDir().getAbsolutePath();
        externalDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdcardDir=null;

        if(!haveStoragePermission()) getStoragePermission();

        scrollView=findViewById(R.id.scrollView);
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

        runningList = new ArrayList<>();
        logManager = new LogManager(20, sharedPreferences);

        deniedToast = Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG);
        removedExtToast = Toast.makeText(this, "External Storage Removed!", Toast.LENGTH_LONG);
        dirNotFoundToast = Toast.makeText(this, "Directory Not Found!", Toast.LENGTH_LONG);
        accessDeniedToast = Toast.makeText(this, "Access Denied!", Toast.LENGTH_LONG);

        String receivedDir = sharedPreferences.getString("current-dir", externalDir);
        currentDir = new File(receivedDir);
        sdcardDir=sharedPreferences.getString("sdcdir", null);

        int sortBy, sortDir;
        sortBy=sharedPreferences.getInt("sortby", UNKNOWN_INT);
        sortDir=sharedPreferences.getInt("sortdir", UNKNOWN_INT);

        if(sortBy != UNKNOWN_INT){
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

        if(sortDir != UNKNOWN_INT){
            RadioButton rbAscending = findViewById(R.id.rbSortAscending);
            RadioButton rbDescending = findViewById(R.id.rbSortDescending);
            if(sortDir == FileOpen.SORT_ASCENDING)
                rbAscending.setChecked(true);
            else if(sortDir == FileOpen.SORT_DESCENDING)
                rbDescending.setChecked(true);
        }

        String constBefore="log";
        String constAfter="_";

        for(int i=0; true; i++){
            String start = constBefore+i+constAfter;
            boolean exist = sharedPreferences.getBoolean(start+"exist", false);
            if(exist){
                String title = sharedPreferences.getString(start+"title", null);
                long max = sharedPreferences.getLong(start+"max", UNKNOWN_LONG);
                long progress = sharedPreferences.getLong(start+"progress", UNKNOWN_LONG);
                int state = sharedPreferences.getInt(start+"state", UNKNOWN_INT);
                long dateTime = sharedPreferences.getLong(start+"date", UNKNOWN_LONG);

                logManager.addLog(new Log(title, max, progress, state, new Date(dateTime)));
            }
            else break;
        }

        logsDialog = null;

        timerCurrentDir = new Timer();
        timerCurrentDir.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        repairCurrentDir();
                        setTxvCurrentDir();
                    }
                });
            }
        }, 0, 1000);

        timerLogsDialog = new Timer();
        timerLogsDialog.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(logsDialog!=null)
                            logsDialog.setMessage(getLogsString());
                    }
                });
            }
        }, 0, 500);

        repairCurrentDir();
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

                if(isHavePermissionToWriteTextBoxFileName()){
                    String title = "Writing File: \nFile Name: " + file.getAbsolutePath();
                    Log log = new Log(title);
                    runningList.add(log);

                    if(writeFile(file, content.getBytes())) log.finish();
                    else log.makeUndone();
                    runningList.remove(log);
                    logManager.addLog(log);
                }

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
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                            final File dest = new File(destFileName);

                            if (isHavePermissionToWriteTextBoxFileName(reqDestDialog.getTxtGetInput())) {
                                if(file.length() > dest.getParentFile().getFreeSpace()){
                                    showLongToast("No enough space!");
                                    return;
                                }

                                String title = "Copying File:\nFrom: " + file.getAbsolutePath() + "\n" +
                                        "To: " + dest.getAbsolutePath();
                                final Log log = new Log(title);
                                runningList.add(log);

                                AsyncTask asyncTask = new AsyncTask() {
                                    boolean doing = false;
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        if (copyFile(file, dest, log, false)) {
                                            doing =true;
                                            log.finish();
                                        }
                                        else {
                                            doing =false;
                                            log.makeUndone();
                                        }

                                        runningList.remove(log);
                                        logManager.addLog(log);

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        if(doing) showLongToast("File Copied");
                                        else showLongToast("The file can not be copied");
                                    }
                                };

                                asyncTask.execute();
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
                            final File dest = new File(destFileName);

                            if (isHavePermissionToWriteTextBoxFileName(reqDestDialog.getTxtGetInput())) {
                                String title = "Moving File:\nFrom: " + file.getAbsolutePath() + "\n" +
                                        "To: " + dest.getAbsolutePath();
                                final Log log = new Log(title);
                                runningList.add(log);

                                AsyncTask asyncTask = new AsyncTask() {
                                    boolean doing = false;
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        if (cutFile(file, dest, log, false)){
                                            doing=true;
                                            log.finish();
                                        }
                                        else{
                                            doing=false;
                                            log.makeUndone();
                                        }

                                        runningList.remove(log);
                                        logManager.addLog(log);

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        if(doing) showLongToast("File Moved");
                                        else showLongToast("The file can not be moved");
                                    }
                                };

                                asyncTask.execute();
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
                    String title = "Removing File:\nFile Name: " + file.getAbsolutePath();
                    final Log log = new Log(title);
                    runningList.add(log);

                    new SureDialog(MainActivity.this, "Are You Sure?", new Runnable() {
                        @Override
                        public void run() {
                            if(file.delete()){
                                showLongToast("File Deleted!");
                                txtFileName.setText("");
                                txtFileName.requestFocus();
                                log.finish();
                            }
                            else{
                                showLongToast("The file can not be deleted");
                                log.makeUndone();
                            }

                            runningList.remove(log);
                            logManager.addLog(log);
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
                txtDir.setText(externalDir+"/");
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
                    currentDir=getCaseSensitivePath(tempDir);
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
                    String title = "Make Directory:\nFile Name: " + tempDir.getAbsolutePath();
                    final Log log = new Log(title);
                    runningList.add(log);

                    if(tempDir.mkdirs()){
                        Toast.makeText(MainActivity.this, "Directory Created!", Toast.LENGTH_LONG).show();
                        log.finish();
                    }
                    else {
                        accessDeniedToast.show();
                        log.makeUndone();
                    }

                    runningList.remove(log);
                    logManager.addLog(log);
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
                                long freeSpace;
                                if(dest.exists()) freeSpace=dest.getFreeSpace();
                                else freeSpace = dest.getParentFile().getFreeSpace();

                                if(fdDir.getSize()>freeSpace){
                                    showLongToast("No enough space!");
                                    return;
                                }

                                String title = "Copying Directory:\nFrom: " + dir.getAbsolutePath() + "\n" +
                                        "To: " + dest.getAbsolutePath();
                                final Log log = new Log(title);
                                runningList.add(log);

                                final AsyncTask asyncTask = new AsyncTask() {
                                    boolean doing = false;
                                    @Override
                                    protected Object doInBackground(Object[] objects) {
                                        if (recCopyDir(dir, dest, log, false)){
                                            doing=true;
                                            log.finish();
                                        }
                                        else{
                                            doing=false;
                                            log.makeUndone();
                                        }

                                        runningList.remove(log);
                                        logManager.addLog(log);

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Object o) {
                                        if(doing) showLongToast("Directory Copied");
                                        else showLongToast("The directory can not be copied");
                                    }
                                };

                                if (dest.exists()) {
                                    showLongToast(dest.getAbsolutePath());
                                    new SureDialog(MainActivity.this, "The same filename already exists, " +
                                            "Do you want to owerwrite?", new Runnable() {
                                        @Override
                                        public void run() {
                                            asyncTask.execute();
                                        }
                                    }, null).show();
                                }

                                else {
                                    asyncTask.execute();
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

                            String title = "Moving Directory:\nFrom: " + dir.getAbsolutePath() + "\n" +
                                    "To: " + dest.getAbsolutePath();
                            final Log log = new Log(title);
                            runningList.add(log);

                            final AsyncTask asyncTask = new AsyncTask() {
                                boolean doing = false;
                                @Override
                                protected Object doInBackground(Object[] objects) {
                                    if (recCutDir(dir, dest, log, false)){
                                        doing=true;
                                        log.finish();
                                    }
                                    else{
                                        doing=false;
                                        log.makeUndone();
                                    }

                                    runningList.remove(log);
                                    logManager.addLog(log);

                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Object o) {
                                    if(doing){
                                        showLongToast("Directory Moved");
                                        repairCurrentDir();
                                        setTxvCurrentDir();
                                    }
                                    else showLongToast("The directory can not be moved");
                                }
                            };

                            if(dest.exists()){
                                showLongToast(dest.getAbsolutePath());
                                new SureDialog(MainActivity.this, "The same filename already exists, " +
                                        "Do you want to owerwrite?", new Runnable() {
                                    @Override
                                    public void run() {
                                        asyncTask.execute();
                                    }
                                }, null).show();
                            }

                            else{
                                asyncTask.execute();
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
                            String title = "Removing Directory:\nFile Name: " + dir.getAbsolutePath();
                            final Log log = new Log(title);
                            runningList.add(log);

                            final AsyncTask asyncTask = new AsyncTask() {
                                boolean doing = false;
                                @Override
                                protected Object doInBackground(Object[] objects) {
                                    if (recRemoveDir(dir)){
                                        doing=true;
                                        log.finish();
                                    }
                                    else{
                                        doing=false;
                                        log.makeUndone();
                                    }

                                    runningList.remove(log);
                                    logManager.addLog(log);

                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Object o) {
                                    if(doing){
                                        showLongToast("Directory Deleted!");
                                        txtFileName.setText("");
                                        txtFileName.requestFocus();

                                        repairCurrentDir();
                                        setTxvCurrentDir();
                                    }
                                    else showLongToast("The Directory can not be deleted");
                                }
                            };

                            asyncTask.execute();
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

        txtDir.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, final boolean hasFocus) {
                txtDirFullScroll();
            }
        });

        txtDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDirFullScroll();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String fileName = txtFileName.getText().toString();
        spEditor.clear();

        spEditor.putString("current-dir", currentDir.getAbsolutePath());
        spEditor.putString("sdcdir", sdcardDir);
        spEditor.putInt("sortby", getSortByMethod());
        spEditor.putInt("sortdir", getSortDir());

        String constBefore="log";
        String constAfter="_";
        for(int i=0; logManager.getLength()>i; i++){
            String start=constBefore+i+constAfter;
            Log log = logManager.getLog(logManager.getLength()-i-1);
            spEditor.putBoolean(start+"exist", true);
            spEditor.putString(start+"title", log.getTitle());
            spEditor.putLong(start+"max", log.getMax());
            spEditor.putLong(start+"progress", log.getProgress());
            spEditor.putInt(start+"state", log.getState());
            spEditor.putLong(start+"date", log.getDate().getTime());

        }

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

        menu.add("Logs").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /*AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_textview, null);

                txvLogsDialog = view.findViewById(R.id.textView);
                txvLogsDialog.setText();
                dialog.setView(view);*/
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Logs");
                builder.setMessage(getLogsString());
                builder.setPositiveButton("OK", null);

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        logsDialog=null;
                    }
                });

                logsDialog = builder.create();
                logsDialog.show();

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private boolean writeFile(File file, byte[] content){
        if(file.isFile() && file.exists() && file.length() > MAX_FILE_SIZE){
            showLongToast("Max File Size To Override Overflow!");
            return false;
        }

        try {
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(content);
            fos.close();
            Toast.makeText(this, "File Written!", Toast.LENGTH_LONG).show();
            return true;
        }
        catch (IOException e) {
            if(e.getMessage().contains("Permission denied")){
                accessDeniedToast.show();
                return false;
            }

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
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

    private boolean copyFile(final File src, final File dest, final Log log, boolean noChangeProgress){
        try {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);

            if(log!=null && !noChangeProgress) log.setMax(src.length() / 1024);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                if(log!=null) log.incerementProgress();
            }

            in.close();
            in = null;

            out.flush();
            out.close();
            out = null;

            if(log!=null && !noChangeProgress) log.finish();
            return true;

        }
        catch (IOException e) {
            showLongToast(e.getMessage());
            if(log!=null) log.makeUndone();
            return false;
        }
    }

    private boolean copyFile(final File src, final File dest){
        return copyFile(src, dest, null, false);
    }

    private boolean cutFile(File src, File dest, final Log log, boolean noChangeProgress){
        if(src.renameTo(dest)) return true;
        if(copyFile(src,dest, log, noChangeProgress) && src.delete()) return true;
        return false;
    }

    private boolean cutFile(File src, File dest){
        return cutFile(src, dest, null, false);
    }

    private void showDirectoryDialog(File dir, int type){
        boolean showFile, showDir;
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        String constStr="Showing Directory: "+dir.getAbsolutePath()+"\n\n";
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
                filesList[i]=new File(dir.getAbsolutePath(), list[i]);
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

    public boolean recCopyDir(File src, final File dest, final Log log, boolean noChangeProgress){
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

        FileDetails fd = new FileDetails(src);
        fd.makeDetails();
        if(log!=null && !noChangeProgress) log.setMax(fd.getSize()/1024);

        String[] list = src.list();
        if(list!=null){
            for(String item : list){
                File file = new File(src, item);
                File destFile = new File(dest, item);

                if(file.isFile()) copyFile(file, destFile, log, true);
                else recCopyDir(file, destFile, log, true);
            }
        }

        return true;
    }

    public boolean recCopyDir(File src, final File dest){
        return recCopyDir(src, dest, null, false);
    }

    public boolean recCutDir(File src, final File dest, final Log log, boolean noChangeProgress){
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

        FileDetails fd = new FileDetails(src);
        fd.makeDetails();
        if(log!=null && !noChangeProgress) log.setMax(fd.getSize()/1024);

        String[] list = src.list();
        if(list!=null) {
            for (String item : list) {
                File file = new File(src, item);
                File destFile = new File(dest, item);

                if (file.isFile()) cutFile(file, destFile, log, true);
                else recCutDir(file, destFile, log, true);
            }
        }

        if(recRemoveDir(src)) return true;
        return false;
    }

    public boolean recCutDir(File src, final File dest){
        return recCutDir(src, dest, null, false);
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

        String dirPath = null;
        try {
            dirPath = currentDir.getCanonicalPath();
        }
        catch (IOException e) {
            throw new RuntimeException();
        }

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
        if(!txvCurrentDir.getText().toString().equals(print))
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
        else if(reqDir.isEmpty() || reqDir.equals(".")) return dir.getAbsolutePath();
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

    private boolean canShowDir(File dir, boolean isSilent){
        if(dir.getAbsolutePath().indexOf(externalDir)==0){
            if(!haveStoragePermission()){
                if(!isSilent) deniedToast.show();
                return false;
            }
            else if(!isReadableExtStorageReady()){
                if(!isSilent) removedExtToast.show();
                return false;
            }
        }

        if(!dir.exists() || !dir.isDirectory()){
            if(!isSilent) showLongToast("Directory Not Found");
            return false;
        }

        if(!dir.canRead()){
            if(!isSilent) accessDeniedToast.show();
            return false;
        }

        return true;
    }

    private boolean canShowDir(File dir){
        return canShowDir(dir, false);
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

    public static void helpFileName(Context context, final EditText textBox, File curDir,
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

        File[] filesList= parentFile.listFiles();
        if(filesList==null || filesList.length==0) return;

        File[] tempOffersList = new File[filesList.length];
        int olIndex=0;

        //boolean unique = true;
        File helpResultFile = null;

        for(File item : filesList){
            boolean correctType;
            if(type == DIRECTORIES_ONLY) correctType=item.isDirectory();
            else if(type == FILES_ONLY) correctType=item.isFile();
            else correctType=true;

            /*if(unique){
                if(item.getName().indexOf(txtBoxLastChildName)==0 && correctType){
                    if(helpResultFile == null) helpResultFile = new File(parentFile, item.getName());
                    else unique=false;
                }
            }*/

            if(item.getName().toLowerCase().indexOf(txtBoxLastChildName.toLowerCase())==0 && correctType)
                tempOffersList[olIndex++]=item;
        }

        File []filesOffersList=new File[olIndex];
        for(int i=0; olIndex>i; i++)
            filesOffersList[i] = tempOffersList[i];
        tempOffersList = null;

        if(/*(unique && helpResultFile!=null) || */filesOffersList.length==1){
            /*if(!(unique && helpResultFile!=null))*/ helpResultFile=filesOffersList[0];

            String tempStr;
            if(txtBoxParentName.isEmpty()){
                if(position == 0)
                    tempStr = "/" + helpResultFile.getName();
                else
                    tempStr = helpResultFile.getName();
            }
            else
                tempStr = txtBoxParentName+"/"+helpResultFile.getName();

            if(helpResultFile.isDirectory()) tempStr=tempStr+"/";

            textBox.setText(tempStr);
            textBox.setSelection(textBox.getText().length());

            return;
        }

        if(getSort)
            FileOpen.sortFiles(filesOffersList, sortBy, sortDir);

        if(filesOffersList.length>1){
            String like = filesOffersList[0].getName();
            AlertDialog.Builder dialog;
            dialog=new AlertDialog.Builder(context);
            StringBuilder sb = new StringBuilder();

            for(int i=0; filesOffersList.length>i; i++){
                File item = filesOffersList[i];
                if(!like.toLowerCase().equals(txtBoxLastChildName.toLowerCase()))
                    like=getMaxLengthCommonString(like, item.getName(), false);

                String tempConstStr;
                if(type == FILES_ONLY || type == DIRECTORIES_ONLY)
                    tempConstStr = "* ";
                else{
                    if(item.isFile())
                        tempConstStr = "* File: ";
                    else
                        tempConstStr = "* Directory: ";
                }

                if(sb.toString().length()>0) sb.append("\n");
                sb.append(tempConstStr + item.getName());
            }

            String tempRes = textBox.getText().toString();
            if(!like.toLowerCase().equals(txtBoxLastChildName.toLowerCase())){
                if(txtBoxParentName.isEmpty()){
                    if(position == 0)
                        tempRes = "/" + like;
                    else
                        tempRes = like;
                }
                else
                    tempRes = txtBoxParentName+"/"+like;
            }

            final String textBoxResult = tempRes;
            tempRes=null;

            dialog.setMessage(sb.toString());
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    textBox.setText(textBoxResult);
                    textBox.setSelection(textBox.getText().length());
                }
            });
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
                    sdcardDir = file.getAbsolutePath() + "/";
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

    public static String getMaxLengthCommonString(String strOne, String strTwo, boolean caseSensitive){
        String bigStr, like;
        if(strOne.length()>strTwo.length()){
            bigStr=strOne;
            like=strTwo;
        }
        else{
            bigStr=strTwo;
            like=strOne;
        }

        boolean findLike=false;

        for (int j = 0; like.length() > j; j++) {
            String smallSub = like.substring(0, like.length() - j);
            String likeSub = bigStr.substring(0, like.length() - j);
            if((caseSensitive && smallSub.toLowerCase().equals(likeSub.toLowerCase()))
                    || (!caseSensitive && smallSub.equals(likeSub))){
                like=likeSub;
                findLike=true;
                break;
            }
        }
        if(!findLike) like="";

        return like;
    }

    public static String getMaxLengthCommonString(String strOne, String strTwo){
        return getMaxLengthCommonString(strOne, strTwo, true);
    }

    public void txtDirFullScroll(){
        if(txtDir.hasFocus()) scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(txtDir.hasFocus()) scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 210);
    }

    public String getLogsString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; runningList.size()>i; i++)
            sb.append(runningList.get(i));
        String runningStr=sb.toString();

        String title="";
        if(runningStr.length()==0 && logManager.getLength()==0)
            title = "No Any Logs!";

        else{
            if(runningStr.length()>0) title=runningStr;
            if(logManager.getLength()>0){
                if(runningStr.length()==0) title=logManager.toString();
                else title=title + "\n\n" + logManager.toString();
            }
        }

        return title;
    }

    public File caseSensitiveChildPath(File parent, String child){
        String[] list = parent.list();
        if(list==null) return null;

        for(String item : list){
            if(item.equalsIgnoreCase(child))
                return new File(parent, item);
        }

        return null;
    }

    public File getCaseSensitivePath(File dir){
        try {
            String fileName;
            String startWith = "";
            fileName = dir.getCanonicalPath();

            if (dir.getParentFile() == null) return dir;

            File f = dir;
            boolean notAllowed = false;
            while (f.getParentFile() != null){
                File parent = f.getParentFile();
                if (!canShowDir(parent, true)) {
                    startWith = f.getCanonicalPath()+"/";
                    notAllowed=true;
                    break;
                }
                f=parent;
            }
            if(!notAllowed) startWith="/";

            if(fileName.length()<startWith.length() || fileName.equals(startWith)) return dir;

            String remain = fileName.substring(startWith.length());

            String[] chain = remain.split("/");
            int chainLength = chain.length;

            File swFile = new File(startWith);

            for (int i = 0; chainLength > i; i++) {
                String childName = chain[i];
                swFile = caseSensitiveChildPath(swFile, childName);
            }

            return swFile;
        }
        catch (IOException e){
            return null;
        }
    }
}









