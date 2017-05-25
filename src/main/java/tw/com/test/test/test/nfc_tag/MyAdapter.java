package tw.com.test.test.test.nfc_tag;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class MyAdapter extends BaseAdapter{
    private LayoutInflater myInflater;
    private List<RfidRecord> records;

    public MyAdapter(Context context,List<RfidRecord> records){
        myInflater = LayoutInflater.from(context);
        this.records = records;
    }
    /*private view holder class*/
    private class ViewHolder {
        TextView txtTitle;
        TextView txtUid;
        public ViewHolder(TextView txtTitle, TextView txtUid){
            this.txtTitle = txtTitle;
            this.txtUid = txtUid;
        }
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int arg0) {
        return records.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return records.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.title),
                    (TextView) convertView.findViewById(R.id.uid)
            );
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        RfidRecord record = (RfidRecord)getItem(position);
        int color_title = Color.WHITE;
        int color_Uid = Color.WHITE;

        holder.txtTitle.setText(record.GetName());
        holder.txtTitle.setTextColor(color_title);
        holder.txtUid.setText(record.GetUidString());
        holder.txtUid.setTextColor(color_Uid);

        return convertView;
    }
}