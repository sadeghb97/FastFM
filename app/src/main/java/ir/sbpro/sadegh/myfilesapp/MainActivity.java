package ir.sbpro.sadegh.myfilesapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    final static int MAX_FILE_SIZE = 1024*500 ;

    EditText txtFileName, txtContent, txtDir;
    TextView txvStorages, txvExternalState, txvCurrentDir;
    Button btnWrite, btnRead, btnOpenFile, btnHelpFile, btnInsertInternalDir, btnInsertExternalDir,
    btnHelpDir, btnChangeDir, btnShowDir, btnMakeDir;

    String internalFilesDir;
    String externalDir;
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

        if(!haveStoragePermission()) getStoragePermission();

        txtFileName=findViewById(R.id.txtFileName);
        txtContent=findViewById(R.id.txtContent);
        txtDir=findViewById(R.id.txtDir);
        btnWrite=findViewById(R.id.btnWrite);
        btnRead=findViewById(R.id.btnRead);
        btnHelpFile=findViewById(R.id.btnHelpFile);
        btnOpenFile=findViewById(R.id.btnOpenFile);
        btnInsertInternalDir=findViewById(R.id.btnInsertInternalDir);
        btnInsertExternalDir=findViewById(R.id.btnInsertExternalDir);
        btnHelpDir=findViewById(R.id.btnHelpDir);
        btnChangeDir=findViewById(R.id.btnChangeDir);
        btnShowDir=findViewById(R.id.btnShowDir);
        btnMakeDir=findViewById(R.id.btnMakeDir);
        txvStorages=findViewById(R.id.txvStorages);
        txvExternalState=findViewById(R.id.txvExternalState);
        txvCurrentDir=findViewById(R.id.txvCurrentDir);

        sharedPreferences=getSharedPreferences("prefs", MODE_PRIVATE);
        spEditor=sharedPreferences.edit();
        deniedToast = Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG);
        removedExtToast = Toast.makeText(this, "External Storage Removed!", Toast.LENGTH_LONG);
        dirNotFoundToast = Toast.makeText(this, "Directory Not Found!", Toast.LENGTH_LONG);
        accessDeniedToast = Toast.makeText(this, "Access Denied!", Toast.LENGTH_LONG);

        String receivedDir = sharedPreferences.getString("current-dir", externalDir);
        currentDir = new File(receivedDir);

        setTxvCurrentDir();
        setTxvExternalState();
        setTxvStorages();

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                String content=txtContent.getText().toString();
                File file=new File(fileName);
                File parent=file.getParentFile();

                if(parent!=null){
                    if(!parent.exists()){
                        Toast.makeText(MainActivity.this, "Parent Directory Not Found!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if(fileName.isEmpty()){
                    txtFileName.requestFocus();
                    return;
                }

                if(isExternalReqTxtDir()){
                    if(!haveStoragePermission()){
                        deniedToast.show();
                        return;
                    }
                    if(!isExtStorageReady()){
                        removedExtToast.show();
                        return;
                    }
                }

                writeFile(file, content.getBytes());
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=getAbsoluteTextBoxFileName();
                File file = new File(fileName);

                if(getPermissionToOpenTextBoxFileName()) {
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

                if(getPermissionToOpenTextBoxFileName()){
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

        btnInsertInternalDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDir.setText(internalFilesDir);
                txtDir.setSelection(txtDir.getText().length());
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String fileName = txtFileName.getText().toString();

        spEditor.putString("current-dir", currentDir.getAbsolutePath().toString());
        spEditor.apply();
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

            for(String item : list){
                String itemStr = "";
                File ios=new File(dir.getAbsolutePath().toString(), item);

                if(ios.isDirectory()){
                    itemStr="* Directory: " + item;
                }
                else if(ios.isFile()){
                    itemStr="* File: " + item;
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
        //txvStorages.setText("Internal: " + internalFilesDir + "\n" + "External: " + externalDir);
        txvStorages.setText("External: " + externalDir);
    }

    private void setTxvExternalState(){
        String tempStr = "External Storage State: ";

        if(haveStoragePermission()) tempStr=tempStr+"Permission Granted";
        else tempStr=tempStr+"Permission Denied";

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            txvExternalState.setText(tempStr + " - Ready");
        else if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY))
            txvExternalState.setText(tempStr + " - Read Only");
        else
            txvExternalState.setText(tempStr + " - Removed");
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

    private boolean getPermissionToOpenTextBoxFileName(){
        String fileName=getAbsoluteTextBoxFileName();
        File file = new File(fileName);

        if(fileName.isEmpty()){
            txtFileName.requestFocus();
            return false;
        }

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
            txtBoxLastChildName="";
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

        String[] offersList = new String[filesList.length];
        int olIndex=0;

        for(String item : filesList){
            File tf= new File(parentFile.getAbsolutePath(), item);
            boolean correctType;
            if(type == DIRECTORIES_ONLY) correctType=tf.isDirectory();
            else correctType=tf.isFile();

            if(item.indexOf(txtBoxLastChildName)==0 && correctType)
                offersList[olIndex++]=item;
        }

        if(olIndex==1){
            String tempStr;
            if(txtBoxParentName.isEmpty())
                tempStr = offersList[0];
            else
                tempStr = txtBoxParentName+"/"+offersList[0];

            if(type==DIRECTORIES_ONLY) tempStr=tempStr+"/";

            textBox.setText(tempStr);
            textBox.setSelection(textBox.getText().length());
        }
        else if(olIndex>1){
            AlertDialog.Builder dialog;
            dialog=new AlertDialog.Builder(this);
            StringBuilder sb = new StringBuilder();

            for(int i=0; olIndex>i; i++){
                if(sb.toString().length()>0) sb.append("\n");
                sb.append("* " + offersList[i]);
            }

            dialog.setMessage(sb.toString());
            dialog.show();
        }
    }
}









