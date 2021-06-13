package net.studiomikan.kas4pc.gui;

/**
 * メニューの設定情報<br>
 * @author okayu
 */
public class MenuItemData
{
	/** 表示文字列 */
	public String text = "";
	/** 表示するかどうか */
	public boolean visible = true;
	/** 選択可能かどうか */
	public boolean enabled = true;

	/**
	 * コンストラクタ
	 * @param text 表示文字列
	 */
	public MenuItemData(String text)
	{
		this.text = text;
	}

	/**
	 * 設定
	 * @param text 表示文字列
	 * @param visible 表示非表示
	 */
	public void set(String text, boolean visible)
	{
		this.text = text;
		this.visible = visible;
	}
}
