package com.borqs.market.listener;

public interface DownloadListener {
    void downloadSuccess(String productId, String fileUri);

    void downloadFailed(String productId);
}
