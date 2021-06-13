package net.studiomikan.kas4pc.klib;

import net.studiomikan.kas.Util;

public class VideoPlayer
{
	public boolean canskip;

	public VideoPlayer(Context context)
	{
	}

	public final void setPos(int x, int y)
	{
	}

	public final void setSize(int width, int height)
	{
	}

	public final void stop()
	{
		if(Util.mainView != null)
		{
			Util.mainView.setVisible(true);
			Util.mainView.conductor.startConductor();
		}
	}

	public final void setVisible(boolean visible)
	{
	}

	public final void setVisibleNow(boolean visible)
	{
	}

	public final void setX(int left)
	{
	}

	public final void setY(int top)
	{
	}

	public final void setWidth(int width)
	{
	}

	public final void setHeight(int height)
	{
	}

	public final void setVolume(float volume)
	{
	}

	public final void setLoop(boolean loop)
	{
	}

	public final void setStart(int position)
	{
	}

	public final void play(String storage)
	{
		stop();
	}

	public final boolean playing()
	{
		return false;
	}

	public final void shutdown()
	{
	}

}
