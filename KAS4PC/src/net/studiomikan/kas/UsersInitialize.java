package net.studiomikan.kas;

/**
 * ゲームが始まる直前に呼ばれる
 * @author okayu
 */
public class UsersInitialize
{
	/** 初期化済みなら true */
	public static boolean initialized = false;

	/**
	 * ゲームが始まる直前に呼ばれる。
	 * ここでプラグインの登録などをすませるとよい。
	 */
	public static final void init()
	{
		// システムボタンを追加
//		new SystemButton();
	}

}
