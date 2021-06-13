package net.studiomikan.kas4pc.klib;

import net.studiomikan.kas.Util;

public class SurfaceView
{
	private SurfaceHolder holder;

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	public SurfaceView(Context context)
	{
		holder = new SurfaceHolder();
	}

	public SurfaceHolder getHolder()
	{
		return holder;
	}

	public final void setFocusable(boolean focusable)
	{
	}

	public final void requestFocus()
	{
	}

	public int getWidth()
	{
		return Util.standardDispWidth;
	}

	public int getHeight()
	{
		return Util.standardDispHeight;
	}

	public final void setVisibility(int visible)
	{
	}

	public boolean onTouchEvent(MotionEvent event)
	{
		return true;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
	}

}
