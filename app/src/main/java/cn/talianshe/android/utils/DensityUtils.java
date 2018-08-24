package cn.talianshe.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author zcm
 * @ClassName: DensityUtils
 * @Description: DensityUtils是一个像素与dp转换的工具
 * @date 2017/11/3 17:34
 */
public class DensityUtils {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 * 
	 * @param context
	 * @param dpValue
	 *            dp值
	 * @return 返回像素值
	 */
	public static int dipTopx(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(dpValue*scale + 0.5f*(dpValue>=0?1:-1)); 
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 * 
	 * @param context
	 * @param pxValue
	 *            像素值
	 * @return 返回dp值
	 */
	public static int pxTodip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int pxTosp(Context context,float pxValue){
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int)(pxValue/fontScale + 0.5f);
	}
	
	public static int spTopx(Context context,float spValue){
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int)(spValue * fontScale + 0.5f);
	}
	
	@SuppressLint("NewApi")
	public static int[] getScreenSize(Context context) {
		int[] screenSize = new int[2];
		int measuredWidth = 0;
		int measuredheight = 0;
		Point size = new Point();
		WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			w.getDefaultDisplay().getSize(size);
			measuredWidth = size.x;
			measuredheight = size.y;
		} else {
			Display d = w.getDefaultDisplay();
			measuredWidth = d.getWidth();
			measuredheight = d.getHeight();
		}
		screenSize[0] = measuredWidth;
		screenSize[1] = measuredheight;

		return screenSize;
	}

}
