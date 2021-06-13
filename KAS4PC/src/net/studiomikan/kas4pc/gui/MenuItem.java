package net.studiomikan.kas4pc.gui;

import javax.swing.JButton;

/**
 * メニューボタン
 * @author okayu
 */
public class MenuItem
{
	/** ボタンの実態 */
	private JButton button;

	/**
	 * 生成
	 * @param button
	 */
	public MenuItem(JButton button)
	{
		this.button = button;
	}

	/**
	 * 有効・無効
	 * @param enabled
	 */
	public final void setEnabled(boolean enabled)
	{
		button.setEnabled(enabled);
	}

}
