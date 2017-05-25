package tw.com.test.test.test.nfc_tag;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Huang on 2017/5/20.
 */

public class RfidRecord {
    private String name;
    private String[] uid;
    public  RfidRecord(){
        this.name = "";
        this.uid = new String[4];
    }
    public  RfidRecord(String name, String[] tag){
        this.name = name;
        this.uid= tag;
    }
    public  RfidRecord(String name, String tag){
        this.name = name;
        this.uid = new String[4];
        SetUid(tag);
    }
    public String GetName(){
        return name;
    }
    public String GetUidOrigin(){
        return String.format("%s%s%s%s", uid[0],uid[1],uid[2],uid[3]);
    }
    public String GetUidString(){
        return String.format("%s:%s:%s:%s", uid[0],uid[1],uid[2],uid[3]);
    }
    public String GetUidFormat(){
        return String.format(" %s, %s, %s, %s", uid[0],uid[1],uid[2],uid[3]);
    }
    public void SetName(String name){
        this.name = name;
    }
    public void SetUid(String[] uid){
        this.uid = uid;
    }
    public void SetUid(String uid){
        for(int i=0;i<4;i++) {
            Log.d("FFF",uid.substring(i*2, (i+1) * 2));
            this.uid[i] = uid.substring(i*2, (i+1) * 2);
        }
    }
}
