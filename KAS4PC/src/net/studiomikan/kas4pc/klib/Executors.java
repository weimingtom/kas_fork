package net.studiomikan.kas4pc.klib;

public class Executors
{
	private static final ScheduledExecutorService ses = new ScheduledExecutorService();

	public static ScheduledExecutorService newSingleThreadScheduledExecutor()
	{
		return ses;
	}


}
