package net.studiomikan.kas4pc.gui;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import net.studiomikan.kas.Util;



/**
 * メインフレーム<br>
 * ウィンドウ表示を担う
 * @author okayu
 */
public class MainFrame extends JFrame implements WindowListener
{
	/** ID */
	private static final long serialVersionUID = -9210835142513778158L;
	/** パネル */
	public MainPanel panel = null;

	/** メニューが表示できるかどうか */
	public static boolean menuVisible = true;
	/** メニューアイテム */
	public static HashMap<String, JMenuItem> menuItem;
	/** メニューアイテムの設定データ */
	public static HashMap<String, MenuItemData> menuItemData;

	/**
	 * コンストラクタ
	 */
	public MainFrame()
	{
		super();

		Util.mainFrame = this;

		// アイコン
		KGUIUtil.setIcon(this, "/icon.png");
		// 閉じたときの動作
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		// タイトル
		setTitle(Util.gameTitle);
		// フォント
		KGUIUtil.setGUIFont(this, 13);

		Container contentPane = getContentPane();

		// メニュー
		new MenuFrame();

		// メニューボタン
		Util.androidButton = new AndroidButton();
		contentPane.add(Util.androidButton, BorderLayout.SOUTH);

		// メインのパネル
		Util.mainPanel = panel = new MainPanel();
		contentPane.add(panel, BorderLayout.CENTER);

		// サイズ設定
		setResizable(false);//サイズ変更不可
		pack();

		// コンフィグ
		ConfigFrame config = new ConfigFrame();
		Util.configView = config;

		// セーブ＆ロード
		SaveFrame save = new SaveFrame();
		Util.saveView = save;

		// 表示
		setVisible(true);
	}

	/**
	 * 描画準備
	 */
	public void initDrawPanel()
	{
		panel.initDraw();
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

	/**
	 * オプションメニューの有効無効のリセット
	 */
	public void resetOptionMenuEnabled()
	{
		if(menuItem == null || menuItemData == null)
			return;

		Iterator<String> it = menuItem.keySet().iterator();
		JMenuItem menu;
		String key;
		while(it.hasNext())
		{
			key = it.next();
			if(menuItemData.containsKey(key))
			{
				menu = menuItem.get(key);
				menu.setEnabled(menuItemData.get(key).enabled);
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		Util.closeGame();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}


}
