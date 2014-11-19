package com.borqs.market.api;



public interface PurchaseListener {
	public void onComplete(boolean hasPurchased);
	
	public void onError(Exception e);
}
