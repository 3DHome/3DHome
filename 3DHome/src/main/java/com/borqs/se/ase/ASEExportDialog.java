package com.borqs.se.ase;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.borqs.se.R;
import com.borqs.se.home3d.HomeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ASEExportDialog extends Dialog implements android.view.View.OnClickListener {
//    private EditText mEdtLabel;
    private Button mBtnOK;
    private Button mBtnCancel;
    private String mInputStr;
    private String mResourcePath;
    private ASEScene mASEScene;
    public ASEExportDialog(Context context) {
        super(context);
        setContentView(R.layout.file_ase_export_dialog);
        setTitle(R.string.export_to_sdcard);
        mBtnOK = (Button) findViewById(R.id.ase_export_btnOK);
        mBtnOK.setOnClickListener(this);
        mBtnCancel = (Button) findViewById(R.id.ase_export_btnCancel);
        mBtnCancel.setOnClickListener(this);
        EditText editText = (EditText) findViewById(R.id.ase_export_edtLabel);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strInput = s.toString();
                strInput = strInput.trim();
                if (TextUtils.isEmpty(strInput)) {
                    mInputStr = null;
                    mBtnOK.setEnabled(false);
                } else {
                    mInputStr = s.toString();
                    mBtnOK.setEnabled(true);
                }
            }
        });
    }

    public void setResourcePath(ASEScene scene, String resourcePath) {
        mASEScene = scene;
        mResourcePath = resourcePath;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mBtnOK)) {
            dismiss();
            if ((!TextUtils.isEmpty(mInputStr)) && (!TextUtils.isEmpty(mResourcePath))) {
                mASEScene.updateXML(new Runnable() {
                    public void run() {
                        export(mResourcePath, mInputStr);
                    }
                });
            }
        } else if (v.equals(mBtnCancel)) {
            dismiss();
        }

    }

    private static void export(String resourcePath, String exportFile) {
        File file = new File(resourcePath);
        if (file.exists()) {
            List<String> srcFilePaths = new ArrayList<String>();
            File[] files = file.listFiles();
            for (File item : files) {
                if (item.getName().endsWith(".ASE") || item.getName().endsWith(".max")
                        || item.getName().equals("ResourceManifest.xml") || item.getName().equals("isUser3DModel")) {
                    continue;
                }
                srcFilePaths.add(item.getPath());
            }
            try {
                zipFolder(srcFilePaths, HomeUtils.getSdcardPath() + File.separator + exportFile + ".zip");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private static void zipFolder(List<String> srcFilePaths, String zipFilePath) throws Exception {
        // 创建Zip包
        File zipfile = new File(zipFilePath);
        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(

        zipfile));

        // 打开要输出的文件
        for (String srcFilePath : srcFilePaths) {
            if (!TextUtils.isEmpty(srcFilePath)) {
                java.io.File file = new java.io.File(srcFilePath);

                // 压缩

                zipFiles(file.getParent() + java.io.File.separator, file.getName(), outZip);

                // 完成,关闭
            }
        }
        outZip.finish();

        outZip.close();

    }

    private static void zipFiles(String folderPath, String filePath, java.util.zip.ZipOutputStream zipOut)

    throws Exception {

        if (zipOut == null) {

            return;

        }

        java.io.File file = new java.io.File(folderPath + filePath);

        // 判断是不是文件

        if (file.isFile()) {

            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);

            java.io.FileInputStream inputStream = new java.io.FileInputStream(file);

            zipOut.putNextEntry(zipEntry);

            int len;

            byte[] buffer = new byte[100000];

            while ((len = inputStream.read(buffer)) != -1) {

                zipOut.write(buffer, 0, len);

            }

            inputStream.close();

            zipOut.closeEntry();

        } else {

            // 文件夹的方式,获取文件夹下的子文件

            String fileList[] = file.list();

            // 如果没有子文件, 则添加进去即可

            if (fileList.length <= 0) {

                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath + java.io.File.separator);

                zipOut.putNextEntry(zipEntry);

                zipOut.closeEntry();

            }

            // 如果有子文件, 遍历子文件

            for (int i = 0; i < fileList.length; i++) {

                zipFiles(folderPath, filePath + java.io.File.separator + fileList[i], zipOut);

            }

        }

    }
}
