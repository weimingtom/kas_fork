package net.studiomikan.kas4pc.klib;

import net.studiomikan.kas.Util;

public class SurfaceHolder
{
	/**
	 * コールバックインターフェース
	 * @author okayu
	 */
	public static interface Callback
	{
	}

	public SurfaceHolder()
	{
	}

	public final void addCallback(SurfaceView view)
	{
	}

	public final void setFixedSize(int width, int height)
	{
	}

	public Canvas lockCanvas()
	{
		if(Util.mainPanel == null) return null;
		else return Util.mainPanel.lockCanvas();
	}

	public void unlockCanvasAndPost(Canvas canvas)
	{
		if(Util.mainPanel != null)
			Util.mainPanel.paintScreen();
	}

}
