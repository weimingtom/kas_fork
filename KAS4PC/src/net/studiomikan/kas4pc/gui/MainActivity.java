package net.studiomikan.kas4pc.gui;


import java.util.HashMap;

public class MainActivity
{
	/** メニューアイテム */
	public static HashMap<String, MenuItem> menuItem;
	/** メニューアイテムの設定データ */
	public static HashMap<String, MenuItemData> menuItemData;

	/** メニューが表示可能かどうか */
	public static boolean menuVisible = true;

	public MainActivity()
	{
	}

	/**
	 * オプションメニューの有効無効
	 * @param name
	 * @param enabled
	 */
	public void setOptionMenuEnabled(String name, boolean enabled)
	{
		if(menuItemData != null && menuItemData.containsKey(name))
			menuItemData.get(name).enabled = enabled;
	}

}
