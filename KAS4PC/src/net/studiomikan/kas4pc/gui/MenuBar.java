package net.studiomikan.kas4pc.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.studiomikan.kas.Config;
import net.studiomikan.kas.Util;


/**
 * メニューバーを実現
 * @author okayu
 */
public class MenuBar extends JPanel
{
	/** ID */
	private static final long serialVersionUID = 1L;

//	/** メニューが表示できるかどうか */
//	public static boolean menuVisible = true;
	/** メニューアイテム */
	public static HashMap<String, MenuItem> menuItem;
	/** メニューアイテムの設定データ */
	private HashMap<String, MenuItemData> menuItemData;


	public MenuBar(JFrame owner)
	{
		super();

		setPreferredSize(new Dimension(Util.standardDispWidth, 50));
		setFocusable(true);
		setBackground(new Color(0xeeeeee));
		GridLayout layout = new GridLayout();
		setLayout(layout);
		layout.setRows(2);

		// 表示非表示の設定を取得する
		MainActivity.menuItemData = new HashMap<String, MenuItemData>();
		menuItemData = MainActivity.menuItemData;
		menuItemData.put("save", 	new MenuItemData("セーブ"));
		menuItemData.put("load", 	new MenuItemData("ロード"));
		menuItemData.put("history",	new MenuItemData("バックログ"));
		menuItemData.put("hide",	new MenuItemData("メッセージを隠す"));
		menuItemData.put("skip",	new MenuItemData("スキップ開始"));
		menuItemData.put("config",	new MenuItemData("設定"));
		menuItemData.put("auto",	new MenuItemData("オートモード開始"));
		menuItemData.put("title",	new MenuItemData("タイトルへ戻る"));
		menuItemData.put("deldata",	new MenuItemData("ゲームデータ削除"));
		menuItemData.put("exit",	new MenuItemData("終了"));
		menuItemData.put("reload",	new MenuItemData("再読み込み"));
		// Config.java 適用
		Config.Menu_config(Util.getMokaScript());


		menuItem = new HashMap<String, MenuItem>();

		// メニューアイテムの追加
		// セーブ　ロード　メッセージ履歴　メッセージ非表示　スキップ　オート　設定　タイトル　終了
		addMenu("save", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				if(Util.saveView != null)
				{
					Util.saveView.setMode("save");
					Util.saveView.setVisible(true);
				}
			}
		});
		addMenu("load", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				if(Util.saveView != null)
				{
					Util.saveView.setMode("load");
					Util.saveView.setVisible(true);
				}
			}
		});
		addMenu("history", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				Util.mainView.startHistory();
			}
		});

		addMenu("hide", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				Util.mainView.tag_hidemessage();
			}
		});

		addMenu("skip", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				Util.mainView.startSkip();
			}
		});

		addMenu("auto", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				Util.mainView.startAuto();
			}
		});

		addMenu("config", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				if(Util.configView != null)
					Util.configView.setVisible(true);
			}
		});

		addMenu("title", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				Util.mainView.tag_gotostart(null);
			}
		});

		addMenu("exit", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				onClickExit();
			}
		});

		addMenu("reload", new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Util.menuFrame.setVisible(false);
				boolean result = KGUIUtil.yesNoDialog(Util.mainFrame, "現在のシナリオファイルを\n再読み込みしますか？", Util.gameTitle);
				if(result)
				{
					Util.reloadScenario();
				}
			}
		});


		// MainSurfaceView からのアクセス用に格納
		MainActivity.menuItem = menuItem;
		MainActivity.menuItemData = menuItemData;
	}

	/**
	 * メニューを追加する
	 * @param key
	 * @param action
	 */
	private void addMenu(String key, ActionListener action)
	{
		Color color = new Color(0xeeeeee);
		if(menuItemData.get(key).visible)
		{
			JButton button;
			button = new JButton(menuItemData.get(key).text);
			button.addActionListener(action);
			button.setBackground(color);
			button.setFocusPainted(false);
			this.add(button);
			menuItem.put(key, new MenuItem(button));	// 管理に追加
		}
	}

	/**
	 * 終了が押された時の動作
	 */
	private void onClickExit()
	{
		Util.closeGameWithAsk(Util.ask_exit);
	}

}
