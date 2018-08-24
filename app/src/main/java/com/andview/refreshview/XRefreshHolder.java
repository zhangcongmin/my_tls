package com.andview.refreshview;

public class XRefreshHolder {

	public float mOffsetY;

	public void move(float deltaY) {
		mOffsetY += deltaY;
	}

	public boolean hasHeaderPullDown() {
		return mOffsetY > 0;
	}

	public boolean hasFooterPullUp() {
		return mOffsetY < 0;
	}
	public boolean isOverHeader(float deltaY){
		return mOffsetY<-deltaY;
		
	}
}
