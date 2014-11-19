package com.borqs.se.addobject;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;

import com.borqs.se.addobject.AddObjectDialog.OnObjectsSelectedListener;
import com.borqs.se.shortcut.AppItemInfo;

public class AddRemoveAppDialog extends AddObjectDialog implements OnObjectsSelectedListener {
    public static final String TAG = "AddRemoveAppDialog";
    private OnAddRemoveListener mOnAddRemoveListener;

    public AddRemoveAppDialog(Context context, LayoutInflater inflater, ArrayList<AppItemInfo> itemInfos, int max) {
        super(context, inflater, itemInfos, AddObjectDialog.ADD_OR_HIDE, max, null, true);
        setInternalListener(this);
    }

    @Override
    public void onObjectSelected(ArrayList<AddObjectItemInfo> selectedList) {
        if (mOnAddRemoveListener != null) {
            mOnAddRemoveListener.OnAddRemoveApps(selectedList);
        }
    }

    public void setOnAddRemoveListener(OnAddRemoveListener l) {
        mOnAddRemoveListener = l;
    }

    @Override
    public void onObjectChecked(AddObjectItemInfo selected, boolean checked, int currentNum) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onObjectCheckedDirectly(AddObjectItemInfo selected) {
        // TODO Auto-generated method stub
    }

    /**
     * Interface for add or remove apps
     */
    public interface OnAddRemoveListener {
        public void OnAddRemoveApps(ArrayList<AddObjectItemInfo> unSelectedList);
    }

}
