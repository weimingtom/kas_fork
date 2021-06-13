package net.studiomikan.kas4pc.klib;

/**
 * 矩形
 * @author okayu
 */
public class Rect
{
	public int left;
	public int top;
	public int right;
	public int bottom;

	public Rect()
	{
		left = 0;
		top = 0;
		right = 0;
		bottom = 0;
	}

	public Rect(int left, int top, int right, int bottom)
	{
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public void set(int left, int top, int right, int bottom)
	{
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
}
