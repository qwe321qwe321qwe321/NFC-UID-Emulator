package tw.com.test.test.test.nfc_tag;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private  static final String STORAGE_FILENAME = "RfidTagRecords";

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 0;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    List<RfidRecord> recordList = new ArrayList<RfidRecord>();
    MyAdapter adapter;
    String lastDetectUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAddNewDialog();
            }
        });

        /** ListView **/
        recordList = new ArrayList<RfidRecord>();
        //StorageWrite("Name1,00000001\nName2,00000000\nName3,01000000\n");
        String res = StorageRead();
        if(res != null){
            String[] sArray = res.split("\n");
            for(String line : sArray){
                String[] recordInfo = line.split(",");
                if(recordInfo.length == 2){
                    recordList.add(new RfidRecord(recordInfo[0], recordInfo[1]));
                }
            }
        }
        ListView list = (ListView)findViewById(R.id.listView);
        adapter = new MyAdapter(this, recordList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
                final ListView list = (ListView) arg0;
                final RfidRecord record = (RfidRecord)adapter.getItem(arg2);

                final String[] ListStr = {"Emulate UID","Edit","Remove"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(ListStr, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        // Simulate.
                                        Toast.makeText(MainActivity.this, "Overwriting...", Toast.LENGTH_SHORT).show();
                                        WriteUid(record.GetUidFormat());
                                        Toast.makeText(MainActivity.this, "Finish.", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        // Edit record.
                                        ShowEditDialog(record);
                                        break;
                                    case 2:
                                        // Remove record.
                                        recordList.remove(record);
                                        StorageWrite(ConvertRecord(recordList));
                                        // 刷新頁面
                                       adapter.notifyDataSetChanged();
                                        break;
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
        /** NFC **/
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter[] mFilters = new IntentFilter[] {
                ndef,
        };

        // Setup a tech list for all NfcF tags
        String[][] mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };

        Intent intent = getIntent();
        resolveIntent(intent);
    }
    /** Custom Dialog **/
    EditText dialogRecordName, dialogRecordUid;
    public void ShowAddNewDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setTitle("新增Uid")
                .setView(inflater.inflate(R.layout.custom_dialog, null))
                // Add action buttons
                .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        recordList.add( new RfidRecord(dialogRecordName.getText().toString(), dialogRecordUid.getText().toString()));
                        adapter.notifyDataSetChanged();
                        StorageWrite(ConvertRecord(recordList));
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        dialogRecordName = ((EditText) alert.findViewById(R.id.recordName));
        dialogRecordUid = ((EditText) alert.findViewById(R.id.recordUid));
        dialogRecordUid.setText(lastDetectUid);
    }
    public void ShowEditDialog(final RfidRecord record){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setTitle("編輯Uid")
                .setView(inflater.inflate(R.layout.custom_dialog, null))
                // Add action buttons
                .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = dialogRecordName.getText().toString();
                        String uid = dialogRecordUid.getText().toString();
                        if( name.length() > 0 && uid.length() == 8) {
                            record.SetName(name);
                            record.SetUid(uid);
                            adapter.notifyDataSetChanged();
                            StorageWrite(ConvertRecord(recordList));
                        } else { Toast.makeText(MainActivity.this, "必須填寫名稱，以及uid必須為8碼", Toast.LENGTH_SHORT).show(); }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        dialogRecordName = ((EditText) alert.findViewById(R.id.recordName));
        dialogRecordName.setText(record.GetName());
        dialogRecordUid = ((EditText) alert.findViewById(R.id.recordUid));
        dialogRecordUid.setText(record.GetUidOrigin());
    }

    /** NFC **/
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
        resolveIntent(intent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
    }
    void resolveIntent(Intent intent) {
        Log.i("test", "start method");
        // 1) Parse the intent and get the action that triggered this intent
        String action = intent.getAction();
        // 2) Check if it was triggered by a tag discovered interruption.
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Log.i("test", "in if");
            //  3) Get an instance of the TAG from the NfcAdapter
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] id = tagFromIntent.getId();
            try {
                lastDetectUid = getHexString(id);
                // 讀到tag即自動填寫uid欄位
                if(dialogRecordUid != null && dialogRecordUid.isShown()){
                    dialogRecordUid.setText(lastDetectUid);
                }else{
                    ShowAddNewDialog();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }// End of method
        else {
            Log.i("Error", action);
        }
        Log.i("test", "end method");
    }
    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
    /** 改寫UID **/
    public void WriteUid(String uid){
        String RfidInfo = readInfo("RfidRecord/Default.conf");
        // 用Regex查找並更改UID 待改善
        String replaced = RfidInfo.replaceAll("(        33, 04,) [A-Z0-9]{2}, [A-Z0-9]{2}, [A-Z0-9]{2}, [A-Z0-9]{2},", String.format("$1%s,", uid));
        writeInfo("RfidRecord/Copy.conf", replaced);
        // Overwrite uid.
        ExecuteAsRoot.execute(new ArrayList<String>(
                Arrays.asList(
                        "mount -o remount,rw /system",
                        "cp -f /storage/emulated/0/RfidRecord/Copy.conf /system/etc/libnfc-nxp.conf",
                        "mount -o remount,ro /system"
                )));
        // NFC reboot.
        ExecuteAsRoot.execute(new ArrayList<String>(
                Arrays.asList(
                        "svc nfc disable",
                        "svc nfc enable"
                )));
    }
    /** 內部儲存空間 I/O **/
    public void StorageWrite(String writeStr){
        try {
            FileOutputStream fos = openFileOutput(STORAGE_FILENAME, Context.MODE_PRIVATE);
            fos.write(writeStr.getBytes());
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public String StorageRead(){
        try {
            FileInputStream fis = openFileInput(STORAGE_FILENAME);
            byte[] data = new byte[fis.available()];
            fis.read(data);
            fis.close();
            return new String(data,"UTF-8");
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public String ConvertRecord(List<RfidRecord> recordList){
        String convertString = "";
        for(RfidRecord record : recordList){
            convertString += String.format("%s,%s\n",record.GetName(),record.GetUidOrigin());
        }
        return convertString;
    }

    /** 寫入資料**/
    public void writeInfo(String fileName, String strWrite) {
        //WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        try {

            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String savePath = fullPath + File.separator + "/" + fileName;

            File file = new File(savePath);

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(strWrite);

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /** 讀取資料**/
    public String readInfo(String fileName) {
        //READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        BufferedReader br = null;
        String response = null;
        try {
            StringBuffer output = new StringBuffer();
            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String savePath = fullPath + File.separator + "/" + fileName;

            br = new BufferedReader(new FileReader(savePath));
            String line = "";
            while ((line = br.readLine()) != null) {
                output.append(line + "\n");
            }
            response = output.toString();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        return response;
    }
    /** 煩死人的Android 6.0權限問題**/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }
    private void doNext(int requestCode, int[] grantResults) {
        switch(requestCode){
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                } else {
                    // Permission Denied
                }
                break;
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                } else {
                    // Permission Denied
                }
                break;
        }
    }
}