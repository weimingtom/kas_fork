package net.studiomikan.kas4pc.gui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JPanel;

import net.studiomikan.kas.Util;


/**
 * メニューを表示するウィンドウ
 * @author okayu
 */
public class MenuFrame extends ModalFrame
{
	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public MenuFrame()
	{
		super();

		Util.menuFrame = this;
		Container contentPane = getContentPane();

		setTitle("Menu");

		// メインのパネル
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);

		// メニュー
		panel.add(Util.menuBar = new MenuBar(this));

		contentPane.add(panel);
		setResizable(false);//サイズ変更不可
		pack();
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		int x = Util.mainFrame.getX();
		int y = Util.mainFrame.getY() + Util.mainFrame.getHeight();
		setLocation(x, y);
	}
}
