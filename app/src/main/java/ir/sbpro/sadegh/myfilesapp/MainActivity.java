package ir.sbpro.sadegh.myfilesapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class MainActivity extends AppCompatActivity {
    final static int DIRECTORIES_ONLY = 1;
    final static int FILES_ONLY = 2;
    final static int BOTH = 0;
    final static int UNKNOWN = -1000;

    final static int MAX_FILE_SIZE = 1024*500 ;

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

            }
        });

        btnCopyFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCutFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                if(sdcardDir==null && sdcardDir.equals("")) {
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

                if(isExternalReqTxtDir()) {
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
                    tempDir.mkdirs();
                    Toast.makeText(MainActivity.this, "Directory Created!", Toast.LENGTH_LONG).show();
                }

                txtDir.setText("");
                txtDir.requestFocus();
            }
        });

        btnShowDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExternalReqTxtDir()){
                    if(!haveStoragePermission()){
                        deniedToast.show();
                        return;
                    }
                    else if(!isReadableExtStorageReady()){
                        removedExtToast.show();
                        return;
                    }
                }

                if(currentDir.canRead()) showDirectoryDialog(currentDir, BOTH);
                else accessDeniedToast.show();
            }
        });

        btnDetsDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCopyDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCutDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnRemoveDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            new AlertDialog.Builder(this).setMessage(e.getMessage()).show();
        }

        return null;
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

    private void setTxvCurrentDir(){
        txvCurrentDir.setText("Current Dir: " + currentDir);
    }

    private void setTxvStorages(){
        strStorages = "Internal Directory:\n" + externalDir + "\n\n" + "SDCard Directory:\n";
        if(sdcardDir != null || !sdcardDir.equals(""))
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

    private String getAbsoluteTextBoxFileName(){
        return recGetAbsTextBoxFileName(currentDir, txtFileName.getText().toString());
    }

    private String getAbsoluteTextBoxDir(){
        return recGetAbsTextBoxFileName(currentDir, txtDir.getText().toString());
    }

    private String recGetAbsTextBoxFileName(File dir, String reqDir){
        if(reqDir.length()>0 && reqDir.charAt(0) == '/') return reqDir;
        else if(reqDir.indexOf("../")==0){
            String tempSend;
            File tempDir;

            if(reqDir.length()>3) tempSend=reqDir.substring(3);
            else tempSend="";
            if(dir.getParentFile()==null) tempDir=dir;
            else tempDir=dir.getParentFile();

            return recGetAbsTextBoxFileName(tempDir, tempSend);
        }
        else if(reqDir.isEmpty()) return dir.getAbsolutePath().toString();
        else return dir.getAbsolutePath().toString() + "/" + reqDir;
    }

    private boolean isExternalReqTxtDir(){
        if(currentDir.getAbsolutePath().toString().indexOf(externalDir)==0)
            return true;
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

    private boolean isHavePermissionToOpenTextBoxFileName(){
        String fileName=getAbsoluteTextBoxFileName();
        String textBoxFileName = txtFileName.getText().toString();

        if(textBoxFileName.isEmpty()){
            txtFileName.requestFocus();
            return false;
        }

        File file = new File(fileName);

        if(isExternalReqTxtDir()) {
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

    private boolean isHavePermissionToWriteTextBoxFileName(){
        String fileName=getAbsoluteTextBoxFileName();
        String textBoxFileName = txtFileName.getText().toString();
        String content=txtContent.getText().toString();
        File file=new File(fileName);
        File parent=file.getParentFile();

        if(parent!=null){
            if(!parent.exists()){
                Toast.makeText(MainActivity.this, "Parent Directory Not Found!", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if(textBoxFileName.isEmpty()){
            txtFileName.requestFocus();
            return false;
        }

        if(isExternalReqTxtDir()){
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

    private void showLongToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void helpFileName(int type){
        EditText textBox;
        String fileName;
        String textBoxStr;
        String txtBoxLastChildName;
        String txtBoxParentName;

        if(type == DIRECTORIES_ONLY){
            textBox=txtDir;
            fileName = getAbsoluteTextBoxDir();
        }
        else if(type == FILES_ONLY){
            textBox=txtFileName;
            fileName=getAbsoluteTextBoxFileName();
        }
        else return;

        textBoxStr = textBox.getText().toString();
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
            else correctType=tf.isFile();

            if(item.indexOf(txtBoxLastChildName)==0 && correctType)
                tempOffersList[olIndex++]=item;
        }

        File []filesOffersList=new File[olIndex];

        for(int i=0; olIndex>i; i++){
            filesOffersList[i]=new File(parentFile.getAbsolutePath(), tempOffersList[i]);
        }

        tempOffersList = null;

        FileOpen.sortFiles(filesOffersList, getSortByMethod(), getSortDir());

        if(filesOffersList.length==1){
            String tempStr;
            if(txtBoxParentName.isEmpty())
                tempStr = filesOffersList[0].getName();
            else
                tempStr = txtBoxParentName+"/"+filesOffersList[0].getName();

            if(type==DIRECTORIES_ONLY) tempStr=tempStr+"/";

            textBox.setText(tempStr);
            textBox.setSelection(textBox.getText().length());
        }
        else if(filesOffersList.length>1){
            AlertDialog.Builder dialog;
            dialog=new AlertDialog.Builder(this);
            StringBuilder sb = new StringBuilder();

            for(int i=0; filesOffersList.length>i; i++){
                if(sb.toString().length()>0) sb.append("\n");
                sb.append("* " + filesOffersList[i].getName());
            }

            dialog.setMessage(sb.toString());
            dialog.show();
        }
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_getstr, null);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        final EditText txtGetInput = view.findViewById(R.id.txtInput);
        if(sdcardDir==null || sdcardDir.equals("")) txtGetInput.setText("/");
        else txtGetInput.setText(sdcardDir);
        txtGetInput.setSelection(txtGetInput.getText().toString().length());

        dialog.setTitle("SDCard Dir");
        dialog.setMessage("Please enter your sdcard root directory!");
        dialog.setView(view);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tempStr = txtGetInput.getText().toString();
                if(tempStr.isEmpty()) return;
                String fileName = recGetAbsTextBoxFileName(currentDir, tempStr);
                File file = new File(fileName);
                if(file.exists() && file.isDirectory())
                    sdcardDir = file.getAbsolutePath().toString() + "/";
                else
                    showLongToast("Directory Not Found");

                txtDir.setText(sdcardDir);
                txtDir.requestFocus();
                txtDir.setSelection(txtDir.getText().length());
                setTxvStorages();
            }
        });

        dialog.setNegativeButton("Cancel", null);
        final AlertDialog myDialog = dialog.create();
        myDialog.show();

        txtGetInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(txtGetInput.getText().toString().isEmpty())
                    ((AlertDialog)myDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                else
                    ((AlertDialog)myDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        });
    }
}









