package com.borqs.market.fragment;

import android.database.Cursor;
import android.view.View;
import android.widget.Toast;

import com.borqs.market.R;
import com.borqs.market.db.DownloadInfoColumns;
import com.borqs.market.db.PlugInColumns;
import com.borqs.market.json.Product;
import com.borqs.market.utils.IntentUtil;

public class ProductLocalDetailFragment extends ProductBasicDetailFragment {

    @Override
    protected int queryProductStatus(String productID) {
        Cursor themeCursor = downLoadHelper.queryTheme(productID);
        if (themeCursor != null && themeCursor.getCount() > 0) {
            themeCursor.moveToFirst();
            mData.current_version_name = themeCursor.getString(themeCursor.getColumnIndex(PlugInColumns.VERSION_NAME));
            mData.current_version_code = themeCursor.getInt(themeCursor.getColumnIndex(PlugInColumns.VERSION_CODE));
            boolean isApplyed = themeCursor.getInt(themeCursor.getColumnIndex(PlugInColumns.IS_APPLY)) == 1;
            themeCursor.close();
            if (mData.version_code > mData.current_version_code) {
                Cursor cursor = downLoadHelper.queryDownloadFile(productID);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int downStatus = cursor.getInt(cursor.getColumnIndex(DownloadInfoColumns.DOWNLOAD_STATUS));
                    if (downStatus == DownloadInfoColumns.DOWNLOAD_STATUS_DOWNLOADING) {
                        downLoadID = cursor.getLong(cursor.getColumnIndex(DownloadInfoColumns.DOWNLOAD_ID));
                        cursor.close();
                        return PRODUCT_STATUS_DOWNLOADING;
                    }
                }
                return PRODUCT_STATUS_NEED_UPDATE;
            }
            if (isApplyed) {
                return PRODUCT_STATUS_APPLYED;
            } else {
                return PRODUCT_STATUS_INSTALLED;
            }
        }
        if (themeCursor != null)
            themeCursor.close();
        return PRODUCT_STATUS_INSTALLED;
    }

    @Override
    protected void showLoadMessage(int resId) {
        Toast.makeText(mActivity, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btn_download) {
            if (PRODUCT_STATUS == PRODUCT_STATUS_DOWNLOADING) {
                // TODO cancle download
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_INSTALLED) {
                if (Product.isSupportApply(mData.type)) {
                    btn_download.setText(R.string.action_wallpaper_setting);
                    IntentUtil.sendBroadCastforApplyed(mActivity, mData.installed_file_path, mData.product_id);
                } else {
                }
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_NEED_UPDATE) {
                purchase();
            } else if (PRODUCT_STATUS == PRODUCT_STATUS_APPLYED) {

            }
        } else if (id == R.id.btn_delete) {
            // 删除
            showConfirmDialog();
        }
    }

}
