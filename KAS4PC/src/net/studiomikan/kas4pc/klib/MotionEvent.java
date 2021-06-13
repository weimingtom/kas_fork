package net.studiomikan.kas4pc.klib;

/**
 * クリックイベントなど
 * @author okayu
 */
public class MotionEvent
{
	/** アクション */
	private int action = 0;
	/** 座標 */
	private float x = 0;
	/** 座標 */
	private float y = 0;

	/** ダウン */
	public static final int ACTION_DOWN = 0;
	/** アップ */
	public static final int ACTION_UP = 1;
	/** ムーブ */
	public static final int ACTION_MOVE = 2;

	public MotionEvent(int action, float x, float y)
	{
		this.action = action;
		this.x = x;
		this.y = y;
	}

	/**
	 * アクションを返す
	 * @return アクション
	 */
	public int getAction()
	{
		return action;
	}

	/**
	 * x 座標
	 * @return
	 */
	public float getX()
	{
		return x;
	}

	/**
	 * y 座標
	 * @return
	 */
	public float getY()
	{
		return y;
	}
}
