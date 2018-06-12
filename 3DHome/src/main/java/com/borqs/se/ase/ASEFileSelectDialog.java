package com.borqs.se.ase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.borqs.se.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ASEFileSelectDialog extends Dialog implements OnItemClickListener {
    private Context mContext;
    private ListView mListView;
    private static final String sRoot = "/";
    private static final String sParent = "..";
    private static final String sOnErrorMsg = "No rights to access!";
    private List<FileData> mFileDatas;
    private int mIndex = 0;
    private int mTop = 0;

    public interface DoSelectedListener {
        void onSelected(String fileName, String filePath);
    }

    public ASEFileSelectDialog(Context context, int titleId, DoSelectedListener callback) {
        super(context);
        mContext = context;
        mCallback = callback;
        setTitle(titleId);
        setContentView(R.layout.file_dialog);
        Window win = getWindow();
        WindowManager.LayoutParams p = win.getAttributes();
        p.height = WindowManager.LayoutParams.MATCH_PARENT;
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(p);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        restoreStatus(getContext());
        refreshFileList();
        if (mIndex != 0 || mTop != 0) {
            mListView.setSelectionFromTop(mIndex, mTop);
        }
    }

    private Drawable mRootIconDrawable;

    private Drawable getRootIconDrawable() {
        if (mRootIconDrawable == null) {
            mRootIconDrawable = mContext.getResources().getDrawable(R.drawable.filedialog_root);
        }
        return mRootIconDrawable;
    }

    private Drawable mFolderUpIconDrawable;

    private Drawable getFolderUpIconDrawable() {
        if (mFolderUpIconDrawable == null) {
            mFolderUpIconDrawable = mContext.getResources().getDrawable(R.drawable.filedialog_folder_up);
        }
        return mFolderUpIconDrawable;
    }

    private Drawable mFolderIconDrawable;

    private Drawable getFolderIconDrawable() {
        if (mFolderIconDrawable == null) {
            mFolderIconDrawable = mContext.getResources().getDrawable(R.drawable.filedialog_folder);
        }
        return mFolderIconDrawable;
    }

    private Drawable mFileIconDrawable;

    private Drawable getFileIconDrawable() {
        if (mFileIconDrawable == null) {
            mFileIconDrawable = mContext.getResources().getDrawable(R.drawable.filedialog_file);
        }
        return mFileIconDrawable;
    }

    private DoSelectedListener mCallback = null;
    private String mCurPath = sRoot;

    private int refreshFileList() {
        File[] files = null;
        try {
            files = new File(mCurPath).listFiles();
        } catch (Exception e) {
            files = null;
        }
        if (files == null) {
            Toast.makeText(getContext(), sOnErrorMsg, Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (mFileDatas != null) {
            mFileDatas.clear();
        } else {
            mFileDatas = new ArrayList<FileData>();
        }

        List<FileData> lfolders = new ArrayList<FileData>();
        List<FileData> lfiles = new ArrayList<FileData>();

        if (!mCurPath.equals(sRoot)) {
            FileData fileData = new FileData();
            fileData.mFileName = sRoot;
            fileData.mFilePath = sRoot;
            fileData.mImage = getRootIconDrawable();
            mFileDatas.add(fileData);

            fileData = new FileData();
            fileData.mFileName = sParent;
            fileData.mFilePath = mCurPath;
            fileData.mImage = getFolderUpIconDrawable();
            mFileDatas.add(fileData);
        }

        for (File file : files) {
            if (file.isDirectory() && file.listFiles() != null) {
                FileData fileData = new FileData();
                fileData.mFileName = file.getName();
                fileData.mFilePath = file.getPath();
                fileData.mImage = getFolderIconDrawable();
                lfolders.add(fileData);
            } else if (file.isFile()) {
                FileData fileData = new FileData();
                fileData.mFileName = file.getName();
                fileData.mFilePath = file.getPath();
                fileData.mImage = getFileIconDrawable();
                lfiles.add(fileData);

            }
        }
        Collections.sort(lfolders, new SortByName());
        Collections.sort(lfiles, new SortByName());
        mFileDatas.addAll(lfolders);
        mFileDatas.addAll(lfiles);
        FileAdapter adapter = new FileAdapter(mContext, mFileDatas);
        mListView.setAdapter(adapter);
        return files.length;
    }

    /*
     * private String getSuffix(String filename) { int dix =
     * filename.lastIndexOf('.'); if (dix < 0) { return ""; } else { return
     * filename.substring(dix); } }
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        FileData fileData = mFileDatas.get(position);

        String pt = fileData.mFilePath;
        String fn = fileData.mFileName;
        if (fn.equals(sRoot) || fn.equals(sParent)) {
            File fl = new File(pt);
            String ppt = fl.getParent();
            if (ppt != null) {
                mCurPath = ppt;
            } else {
                mCurPath = sRoot;
            }
        } else {
            File fl = new File(pt);
            if (fl.isFile()) {
                saveCurrentStatus();
                dismiss();
                if (mCallback != null) {
                    mCallback.onSelected(fn, pt);
                }

                return;
            } else if (fl.isDirectory()) {
                mCurPath = pt;
            }
        }
        refreshFileList();
    }

    private void saveCurrentStatus() {
        int index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        int top = (v == null) ? 0 : v.getTop();
        String savePath = mCurPath;
        saveStatusToFile(getContext(), index, top, savePath);
    }

    private void restoreStatus(Context context) {
        String fileName = context.getFilesDir() + File.separator + "ase_file_select_status";
        File saveFile = new File(fileName);
        if (!saveFile.exists()) {
            return;
        }
        try {
            FileReader reader = new FileReader(saveFile);
            BufferedReader bRreader = new BufferedReader(reader);
            String line = null;
            int count = 0;
            while ((line = bRreader.readLine()) != null) {
                if (count == 0) {
                    mIndex = Integer.parseInt(line);
                } else if (count == 1) {
                    mTop = Integer.parseInt(line);
                } else if (count == 2) {
                    mCurPath = line;
                }
                count++;
            }
            bRreader.close();

        } catch (Exception e) {

        }
    }

    private static void saveStatusToFile(Context context, int index, int top, String curFilePath) {
        try {
            String fileName = context.getFilesDir() + File.separator + "ase_file_select_status";
            File saveFile = new File(fileName);
            if (saveFile.exists()) {
                saveFile.delete();
            }
            saveFile.createNewFile();
            FileOutputStream writer = new FileOutputStream(saveFile, true);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(Integer.toString(index));
            stringBuffer.append("\n");
            stringBuffer.append(Integer.toString(top));
            stringBuffer.append("\n");
            stringBuffer.append(curFilePath);
            writer.write(stringBuffer.toString().getBytes());
            writer.flush();

            writer.close();

        } catch (Exception e) {

        }
    }

    private class FileAdapter extends BaseAdapter {
        private List<FileData> mData;
        private LayoutInflater mInflater;

        public FileAdapter(Context context, List<FileData> data) {
            mData = data;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = mInflater.inflate(R.layout.file_dialog_item, parent, false);
            } else {
                v = convertView;
            }
            bindView(position, v);
            return v;
        }

        private void bindView(int position, View view) {
            FileData fileData = (FileData) getItem(position);
            ImageView imageView = (ImageView) view.findViewById(R.id.filedialogitem_img);
            imageView.setImageDrawable(fileData.mImage);

            TextView title = (TextView) view.findViewById(R.id.filedialogitem_name);
            title.setText(fileData.mFileName);

            TextView summary = (TextView) view.findViewById(R.id.filedialogitem_path);
            summary.setText(fileData.mFilePath);
        }

    }

    private class FileData {
        public String mFileName;
        public String mFilePath;
        public Drawable mImage;
    }

    private class SortByName implements Comparator<FileData> {
        public int compare(FileData lhs, FileData rhs) {
            return Collator.getInstance().compare(lhs.mFileName, rhs.mFileName);
        }
    }

}
