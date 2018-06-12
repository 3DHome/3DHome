package com.borqs.se.home3d;

import java.util.ArrayList;
import java.util.List;

import com.borqs.se.R;

import android.content.Context;


public class MoreMenuAdapter extends IconTextListAdapter{
    public final static int SORT_APP               = 1001;
    public final static int HIDE_APP               = 1002;

    public MoreMenuAdapter(Context context) {
        super(context, getData(context));
    }
    
    public int buttonToCommand(int whichButton) {
        AttachmentListItem item = (AttachmentListItem)getItem(whichButton);
        return item.getCommand();
    }

    protected static List<IconListItem> getData(Context context) {
        List<IconListItem> data = new ArrayList<IconListItem>(7);
        addItem(data, context.getString(R.string.app_sorting),
                R.drawable.dock_sort_normal, SORT_APP);

        addItem(data, context.getString(R.string.app_hiding),
                R.drawable.dock_hide_normal, HIDE_APP);


        return data;
    }

    protected static void addItem(List<IconListItem> data, String title,
            int resource, int command) {
        AttachmentListItem temp = new AttachmentListItem(title, resource, command);
        data.add(temp);
    }

    public static class AttachmentListItem extends IconTextListAdapter.IconListItem {
        private int mCommand;

        public AttachmentListItem(String title, int resource, int command) {
            super(title, resource);

            mCommand = command;
        }

        public int getCommand() {
            return mCommand;
        }
    }


}
