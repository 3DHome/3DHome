package com.borqs.market.api;



public interface PurchaseListener {
	void onComplete(boolean hasPurchased);
	
	void onError(Exception e);
}
