package com.borqs.market.controller;



public class DownloadController extends Controller{
    
    @SuppressWarnings("unused")
    private static final String TAG = DownloadController.class.getSimpleName();

    public static final int MESSAGE_DOWNLOAD = 0X0001;
    @Override
    public boolean handleMessage(int what, Object data) {
        // TODO Auto-generated method stub
        return false;
    }
    
//    public void download(Product data) {
//        Request downReq = new Request(Uri.parse(url));
//        int endPos = url.lastIndexOf("/");
//        String fileName = url.substring(endPos + 1,
//                url.length());
//        String filepath = QiupuConfig.getMarketThemePath()
//                + fileName;
//        downReq.setTitle(mData.name);
//        downReq.setDestinationUri(Uri.fromFile(new File(
//                filepath)));
//        // downReq.setVisibleInDownloadsUi(false);
//        // downReq.setDestinationInExternalFilesDir(mActivity,QiupuConfig.THEME_PATH,fileName);
//        // refreshProductStatus(PRODUCT_STATUS_DOWNLOADED);refreshProductStatus(PRODUCT_STATUS_DOWNLOADED);
//        // downHelper.insert(mData,filepath,
//        // dm.enqueue(downReq));
//        downHelper.insert(mData, filepath, dm.enqueue(downReq));
//        mDownloadOrDeleteButton
//                .setText(R.string.action_downloading);
//    }
    
//    private void purchase() {
//        if (isLoading) {
//            BLog.v(TAG, "is Loading ...");
//            return;
//        }
//
//        if (!DataConnectionUtils.testValidConnection(mActivity)) {
//            Toast.makeText(mActivity, R.string.dlg_msg_no_active_connectivity,
//                    Toast.LENGTH_SHORT).show();
//        }
//
//        synchronized (mLocked) {
//            isLoading = true;
//        }
//
//        BLog.v(TAG, "begin purchase");
//        // begin();
//
//        mApiUtil = ApiUtil.getInstance(new Oauth2AccessToken(
//                "MTA0MDlfMTM1NzI2NTA0OTg0N183NjE1", "100000000"));
//        mApiUtil.productPurchase(mActivity, product_id, version,
//                new RequestListener() {
//
//                    @Override
//                    public void onIOException(IOException e) {
//                        BLog.v(TAG,
//                                "productPurchase onIOException  "
//                                        + e.getMessage());
//                        synchronized (mLocked) {
//                            isLoading = false;
//                        }
//
//                        Message mds = mHandler.obtainMessage(PURCHASE_END);
//                        mds.getData().putBoolean(RESULT, false);
//                        mHandler.sendMessage(mds);
//
//                    }
//
//                    @Override
//                    public void onError(WutongException e) {
//                        BLog.v(TAG,
//                                "productPurchase onError  " + e.getMessage());
//                        synchronized (mLocked) {
//                            isLoading = false;
//                        }
//
//                        Message mds = mHandler.obtainMessage(PURCHASE_END);
//                        mds.getData().putBoolean(RESULT, false);
//                        mHandler.sendMessage(mds);
//
//                    }
//
//                    @Override
//                    public void onComplete(String response) {
//                        JSONObject obj;
//                        String url = null;
//                        try {
//                            obj = new JSONObject(response);
//                            if (!obj.has("data"))
//                                return;
//                            if (obj.optJSONObject("data") != null) {
//                                url = obj.optJSONObject("data")
//                                        .optString("url");
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        synchronized (mLocked) {
//                            isLoading = false;
//                        }
//
//                        Message mds = mHandler.obtainMessage(PURCHASE_END);
//                        mds.getData().putBoolean(RESULT, true);
//                        mds.getData().putString(URL, url);
//                        mHandler.sendMessage(mds);
//
//                    }
//                });
//    }
}
