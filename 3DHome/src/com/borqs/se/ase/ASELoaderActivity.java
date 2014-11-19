package com.borqs.se.ase;

import java.io.File;
import java.io.IOException;

import com.borqs.se.R;
import com.borqs.se.download.Utils;
import com.borqs.se.engine.SERenderView;
import com.borqs.se.engine.SESceneManager;
import com.borqs.se.home3d.HomeUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ASELoaderActivity extends Activity implements OnTouchListener {
    private static final int MSG_TYPE_LOADED_FINISHED = 1;
    private static final int MSG_TYPE_ADD_TO_HOUSE = 1000;
    private static int FILE_DIALOG_ID = 1;
    private static int ASE_EXPORT_DIALOG_ID = 2;
    private FrameLayout mContent;
    private View mTextView;
    private ASEScene mASEScene;
    private Handler mHandler;
    private boolean mHasLoaded = false;
    private String mResourcePath;
    private boolean mNeedUpdate;
    private MenuItem mExportZip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.load_ase_title);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        setContentView(R.layout.file_ase_loader_activity);

        final Button fileChoooseBtn = (Button) findViewById(R.id.file_choose_btn);
        fileChoooseBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mHasLoaded) {
                    mASEScene.updateXML(new Runnable() {
                        public void run() {
                            mHandler.sendEmptyMessage(MSG_TYPE_ADD_TO_HOUSE);
                        }
                    });

                } else {
                    showDialog(FILE_DIALOG_ID);
                }
            }

        });
        final Button resetBtn = (Button) findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mHasLoaded) {
                    mASEScene.reset();
                }
            }

        });
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int what = msg.what;
                switch (what) {
                case MSG_TYPE_ADD_TO_HOUSE:
                    Intent intent = new Intent("com.borqs.se.intent.action.ADD_MODEL_TO_HOUSE");
                    intent.putExtra("NEED_UPDATE", true);
                    intent.putExtra("RESOURCE_PATH", mResourcePath);
                    sendBroadcast(intent);
                    finish();
                    break;
                case MSG_TYPE_LOADED_FINISHED:
                    mHasLoaded = true;
                    mTextView.setVisibility(View.GONE);
                    resetBtn.setVisibility(View.VISIBLE);
                    fileChoooseBtn.setText(R.string.add_it_to_house);
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.dialog_add_user_object, mContent, false);
                    mContent.addView(v);
                    initAdjustPanel();
                    mExportZip.setEnabled(true);
                    break;
                case MSG_TYPE_NARROW:
                    sendEmptyMessageDelayed(MSG_TYPE_NARROW, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_SCALE, 0.97f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_SCALE, 0.99f);
                    }
                    break;
                case MSG_TYPE_EAPAND:
                    sendEmptyMessageDelayed(MSG_TYPE_EAPAND, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_SCALE, 1.03f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_SCALE, 1.01f);
                    }
                    break;
                case MSG_TYPE_LEFT:
                    sendEmptyMessageDelayed(MSG_TYPE_LEFT, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_X, -5f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_X, -2f);
                    }
                    break;
                case MSG_TYPE_RIGHT:
                    sendEmptyMessageDelayed(MSG_TYPE_RIGHT, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_X, 5f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_X, 2f);
                    }
                    break;
                case MSG_TYPE_UP:
                    sendEmptyMessageDelayed(MSG_TYPE_UP, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Z, 5f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Z, 2f);
                    }
                    break;
                case MSG_TYPE_DOWN:
                    sendEmptyMessageDelayed(MSG_TYPE_DOWN, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Z, -5f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Z, -2f);
                    }
                    break;
                case MSG_TYPE_NEAR:
                    sendEmptyMessageDelayed(MSG_TYPE_NEAR, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Y, 5f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Y, 2f);
                    }
                    break;
                case MSG_TYPE_FAR:
                    sendEmptyMessageDelayed(MSG_TYPE_FAR, 100);
                    if (mActionDown) {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Y, -5f);
                    } else {
                        SESceneManager.getInstance().handleMessage(ASEScene.MSG_TYPE_MOVE_Y, -2f);
                    }
                    break;
                }
                mActionDown = false;

            }

        };
        SESceneManager.getInstance().initEngine(getApplicationContext());
        SESceneManager.getInstance().setGLActivity(this);
        SESceneManager.getInstance().setHandler(new Handler());
        SERenderView renderView = new SERenderView(this, true);
        mContent = (FrameLayout) findViewById(R.id.content);
        mContent.addView(renderView);
        mTextView = findViewById(R.id.content_describe);
        SESceneManager.getInstance().setGLSurfaceView(renderView);
        mASEScene = new ASEScene(getApplicationContext(), "ASEScene");
        SESceneManager.getInstance().setCurrentScene(mASEScene);
        SESceneManager.getInstance().dataIsReady();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file_ase_menu, menu);
        mExportZip = menu.findItem(R.id.export_zip);
        mExportZip.setEnabled(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.export_zip:
            showDialog(ASE_EXPORT_DIALOG_ID);
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SESceneManager.getInstance().handleBackKey();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            SESceneManager.getInstance().handleMenuKey();
        }
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return false;
        }
        return super.onKeyUp(keyCode, event);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SESceneManager.getInstance().onActivityDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onPause() {
        super.onPause();
        SESceneManager.getInstance().onActivityPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SESceneManager.getInstance().onActivityResume();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        if (id == ASE_EXPORT_DIALOG_ID) {
            ASEExportDialog exportDialog = (ASEExportDialog) dialog;
            exportDialog.setResourcePath(mASEScene, mResourcePath);
        } else {
            SESceneManager.getInstance().onPrepareDialog(id, dialog, bundle);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        if (id == FILE_DIALOG_ID) {
            Dialog dialog = new ASEFileSelectDialog(this, R.string.select_file_btn,
                    new ASEFileSelectDialog.DoSelectedListener() {

                        @Override
                        public void onSelected(final String fileName, final String filePath) {
                            new Thread() {

                                @Override
                                public void run() {
                                    File srcFile = new File(filePath);
                                    if (srcFile.exists() && srcFile.isFile()) {

                                        long zipFileLength = srcFile.length();
                                        String zipFileMd5 = HomeUtils.getFileMD5(srcFile);
                                        String unzipFilename = fileName;
                                        if (unzipFilename.endsWith(".zip") || unzipFilename.endsWith(".ZIP")) {
                                            unzipFilename = unzipFilename.substring(0, unzipFilename.length() - 4);
                                        }
                                        String unzipFilePath = getLocalFilePath(unzipFilename);
                                        File fileInfoFile = new File(unzipFilePath + File.separator + zipFileMd5 + "_"
                                                + zipFileLength);
                                        if (fileInfoFile.exists()) {
                                            String aseFilePath = searchASEFile(unzipFilePath);
                                            File aseFile = new File(aseFilePath);
                                            String asePath = aseFile.getParent();
                                            mASEScene.add3DMaxModelToScene(asePath, new Runnable() {
                                                public void run() {
                                                    mHandler.sendEmptyMessage(MSG_TYPE_LOADED_FINISHED);
                                                }
                                            });
                                            mNeedUpdate = false;
                                            mResourcePath = asePath;
                                            return;
                                        } else {
                                            HomeUtils.deleteFile(unzipFilePath);
                                        }
                                        try {
                                            Utils.unzipDataPartition(filePath, unzipFilePath);
                                            String aseFilePath = searchASEFile(unzipFilePath);
                                            if (aseFilePath != null) {
                                                File aseFile = new File(aseFilePath);
                                                File newAseFile = new File(unzipFilePath + File.separator
                                                        + unzipFilename + ".ASE");
                                                aseFile.renameTo(newAseFile);
                                                String asePath = newAseFile.getParent();
                                                String aseName = newAseFile.getName().substring(0,
                                                        newAseFile.getName().length() - 4);
                                                SESceneManager.getInstance().createCBF_JNI(asePath, aseName);
                                                mASEScene.add3DMaxModelToScene(asePath, new Runnable() {
                                                    public void run() {
                                                        mHandler.sendEmptyMessage(MSG_TYPE_LOADED_FINISHED);
                                                    }
                                                });
                                                mNeedUpdate = true;
                                                mResourcePath = asePath;
                                                fileInfoFile.createNewFile();
                                            }

                                        } catch (IOException e1) {
                                            // TODO Auto-generated catch block
                                            e1.printStackTrace();
                                        }
                                    }
                                }

                            }.start();

                        }
                    });
            return dialog;
        } else if (id == ASE_EXPORT_DIALOG_ID) {
            return new ASEExportDialog(this);
        } else {
            return SESceneManager.getInstance().onCreateDialog(id, bundle);
        }
    }

    private String searchASEFile(String aseFileName) {
        File file = new File(aseFileName);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File item : files) {
                    if (item.isFile() && item.getName().endsWith(".ASE")) {
                        return item.getPath();
                    } else if (item.isDirectory()) {
                        String result = searchASEFile(item.getPath());
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getLocalFilePath(String name) {
        if (TextUtils.isEmpty(name)) {
            return HomeUtils.getDownloadedFilePath(this, null);
        } else {
            return HomeUtils.getDownloadedFilePath(this, null) + File.separator + name;
        }
    }

    private static final int MSG_TYPE_NARROW = MSG_TYPE_LOADED_FINISHED + 1;
    private static final int MSG_TYPE_EAPAND = MSG_TYPE_NARROW + 1;
    private static final int MSG_TYPE_LEFT = MSG_TYPE_EAPAND + 1;
    private static final int MSG_TYPE_RIGHT = MSG_TYPE_LEFT + 1;
    private static final int MSG_TYPE_UP = MSG_TYPE_RIGHT + 1;
    private static final int MSG_TYPE_DOWN = MSG_TYPE_UP + 1;
    private static final int MSG_TYPE_NEAR = MSG_TYPE_DOWN + 1;
    private static final int MSG_TYPE_FAR = MSG_TYPE_NEAR + 1;
    private boolean mActionDown = false;
    private ImageView mNarrow;
    private ImageView mExpand;
    private ImageView mLeft;
    private ImageView mRight;
    private ImageView mUp;
    private ImageView mDown;
    private ImageView mNear;
    private ImageView mFar;

    private void initAdjustPanel() {
        mNarrow = (ImageView) findViewById(R.id.ase_btn_narrow);
        mNarrow.setOnTouchListener(this);
        mExpand = (ImageView) findViewById(R.id.ase_btn_expand);
        mExpand.setOnTouchListener(this);

        mUp = (ImageView) findViewById(R.id.ase_btn_up);
        mUp.setOnTouchListener(this);
        mDown = (ImageView) findViewById(R.id.ase_btn_down);
        mDown.setOnTouchListener(this);

        mLeft = (ImageView) findViewById(R.id.ase_btn_left);
        mLeft.setOnTouchListener(this);

        mRight = (ImageView) findViewById(R.id.ase_btn_right);
        mRight.setOnTouchListener(this);

        mNear = (ImageView) findViewById(R.id.ase_btn_near);
        mNear.setOnTouchListener(this);

        mFar = (ImageView) findViewById(R.id.ase_btn_far);
        mFar.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mActionDown = true;
            if (v == mNarrow) {
                mHandler.removeMessages(MSG_TYPE_NARROW);
                mHandler.sendEmptyMessage(MSG_TYPE_NARROW);
            } else if (v == mExpand) {
                mHandler.removeMessages(MSG_TYPE_EAPAND);
                mHandler.sendEmptyMessage(MSG_TYPE_EAPAND);
            } else if (v == mUp) {
                mHandler.removeMessages(MSG_TYPE_UP);
                mHandler.sendEmptyMessage(MSG_TYPE_UP);
            } else if (v == mDown) {
                mHandler.removeMessages(MSG_TYPE_DOWN);
                mHandler.sendEmptyMessage(MSG_TYPE_DOWN);
            } else if (v == mLeft) {
                mHandler.removeMessages(MSG_TYPE_LEFT);
                mHandler.sendEmptyMessage(MSG_TYPE_LEFT);
            } else if (v == mRight) {
                mHandler.removeMessages(MSG_TYPE_RIGHT);
                mHandler.sendEmptyMessage(MSG_TYPE_RIGHT);
            } else if (v == mNear) {
                mHandler.removeMessages(MSG_TYPE_NEAR);
                mHandler.sendEmptyMessage(MSG_TYPE_NEAR);
            } else if (v == mFar) {
                mHandler.removeMessages(MSG_TYPE_FAR);
                mHandler.sendEmptyMessage(MSG_TYPE_FAR);
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP
                || !v.isPressed()) {
            mHandler.removeMessages(MSG_TYPE_NARROW);
            mHandler.removeMessages(MSG_TYPE_EAPAND);
            mHandler.removeMessages(MSG_TYPE_LEFT);
            mHandler.removeMessages(MSG_TYPE_RIGHT);
            mHandler.removeMessages(MSG_TYPE_UP);
            mHandler.removeMessages(MSG_TYPE_DOWN);
            mHandler.removeMessages(MSG_TYPE_NEAR);
            mHandler.removeMessages(MSG_TYPE_FAR);
        }
        return false;
    }

}
