package com.borqs.market.listener;

public interface DownloadListener {
    public void downloadSuccess(String productId, String fileUri);

    public void downloadFailed(String productId);
}
