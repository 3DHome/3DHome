package com.borqs.digitalframe;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.borqs.digitalframe.R;

import butterknife.ButterKnife;

public class AlbumFolderActivity extends FragmentActivity implements
        LoaderCallbacks<Cursor> {

    private ArrayList<ImageFileItemInfo> mImageFileList = new ArrayList<ImageFileItemInfo>();
    ArrayList<HashMap<String, Object>> mFolderListItem = new ArrayList<HashMap<String, Object>>();
    private GridView mGridView;
    private int mMode; // 0, file; 1, folder
    private Cursor mCursor;
    public static final int REQUEST_CODE_SELECT_FILE = 11111;

    private static final String[] PROJECTION = {
            MediaStore.Images.Media.DISPLAY_NAME, 
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID, 
            MediaStore.Images.Media.DATA };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_albums);
        getSupportLoaderManager().initLoader(0, null, this);
        mGridView = ButterKnife.findById(this, R.id.photo_albums);

        getSupportLoaderManager().initLoader(0, null, this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mMode = bundle.getInt("mode");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        CursorLoader cursorLoader = new CursorLoader(this,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION,
                null, null, null);

        return cursorLoader;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    public String getFolderPath(String filepath) {
        String folderPath = filepath.substring(0, filepath.lastIndexOf('/'));
        return folderPath;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0,Cursor cursor) {
        mCursor = cursor;
        if(mCursor == null)return;

        final HashMap<String, String> hmap = new HashMap<String, String>();

        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            String name = mCursor.getString(0);
            long id = mCursor.getLong(1);
            String foldername = mCursor.getString(2);
            String folderid = mCursor.getString(3);// folder

            String path = mCursor.getString(4);
            String folderpath = getFolderPath(path);

            ImageFileItemInfo mbp = new ImageFileItemInfo();
            mbp.setDisplayName(name);
            mbp.setId(id);
            mbp.setFolderName(foldername);
            mbp.setFolderId(folderid);
            mbp.setPath(path);
            mbp.setFolderPath(folderpath);

            hmap.put(folderid, folderpath);
            mImageFileList.add(mbp);
        }

        new Thread(){
            public void run(){
                Iterator folderset = hmap.entrySet().iterator();
                while (folderset.hasNext()) {
                    Entry entry = (Entry) folderset.next();
                    String folderid = (String) entry.getKey();
                    String folderpath = (String) entry.getValue();

                    String firstPath = null;
                    int count = 0;
                    count = getAllFilesPathForFolder(folderpath).size();

                    for (int i = 0; i < mImageFileList.size(); i++) {
                        if (((ImageFileItemInfo) mImageFileList.get(i)).getFolderPath().equals(
                                folderpath)) {
                            firstPath = ((ImageFileItemInfo) mImageFileList.get(i)).getPath();
                            break;
                        }
                    }
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("itemId", folderid);
                    map.put("itemIcon",
                            getImageThumbnail(AlbumFolderActivity.this, firstPath));
                    map.put("itemTitle", getFolderName(folderpath));
                    map.put("folderpath", folderpath);
                    map.put("pictureCount", count);
                    mFolderListItem.add(map);
                }
                mHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
            case 1:
                mGridView.setAdapter(new GalleryPickerAdapter(getLayoutInflater()));
                mGridView.setOnItemClickListener(new GalleryItemListener());
                mCursor.close();
                break;
            }
        }
    };

    public String getFolderName(String folderpath) {
        return folderpath.substring(folderpath.lastIndexOf('/') + 1,
                folderpath.length());
    }

    public static Bitmap getImageThumbnail(Context context, String fileName) {
        ContentResolver cr = context.getContentResolver();
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String whereClause = "upper(" + MediaStore.Images.ImageColumns.DATA
                + ") = '" + fileName.toUpperCase() + "'";
        Cursor cursor = null;
        try {
            cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[] { MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.ORIENTATION }, whereClause,
                    null, null);
        } catch (Exception ex) {
        }
        if (cursor == null)
            return null;
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        String imageId = cursor.getString(cursor
                .getColumnIndex(MediaStore.Images.Media._ID));
        int orientation = cursor.getInt(cursor
                .getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        if (imageId == null) {
            cursor.close();
            return null;
        }
        cursor.close();
        long imageIdLong = Long.parseLong(imageId);

        bitmap = Images.Thumbnails.getThumbnail(cr, imageIdLong,
                Images.Thumbnails.MINI_KIND, options);

        if (orientation > 0) {
            bitmap = rotateBitmap(bitmap, orientation);
        }

        return bitmap;
    }

    private static Bitmap rotateBitmap(Bitmap b, int orientation) {
        if(b == null){
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(1, 1);
        switch (orientation) {
        case 90:
            matrix.setRotate(90);
            break;
        case 270:
            matrix.setRotate(270);
            break;
        case 180:
            matrix.setRotate(180);
            break;
        default:
            break;
        }
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                matrix, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class GalleryItemListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            // mGridView.
            HashMap<String, Object> item = mFolderListItem.get(arg2);
            String folderPath = (String) item.get("folderpath");

            // folder
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("folder_path", folderPath);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private ArrayList<String> getAllFilesPathForFolder(String folderpath) {
        ArrayList<String> allFilePath = new ArrayList<String>();
        File dir = new File(folderpath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String absolutePath = files[i].getAbsolutePath();
                if (absolutePath.endsWith("jpeg") || absolutePath.endsWith("png") || absolutePath.endsWith("jpg")) {
                    allFilePath.add(absolutePath);
                }
            }
        }
        return allFilePath;
    }

    class GalleryPickerAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        GalleryPickerAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public int getCount() {
            return mFolderListItem.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                ViewGroup parent) {
            View v;

            if (convertView == null) {
                v = mInflater.inflate(R.layout.photo_album_item, null);
            } else {
                v = convertView;
            }

            TextView titleView = ButterKnife.findById(v, R.id.title);
            PhotoAlbumItem iv = ButterKnife.findById(v, R.id.thumbnail);
            HashMap map = mFolderListItem.get(position);
            if (map.get("itemIcon") != null) {
                iv.setImageBitmap((Bitmap) map.get("itemIcon"));
                String title = mFolderListItem.get(position).get("itemTitle") + " ("
                        + mFolderListItem.get(position).get("pictureCount") + ")";
                titleView.setText(title);
            } else {
                iv.setImageResource(android.R.color.transparent);
                titleView.setText(mFolderListItem.get(position).get("itemTitle")
                        .toString());
            }
            titleView.requestLayout();

            return v;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_OK) {
            String path = data.getExtras().getString("picture_paths");
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("picture_paths", path);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
