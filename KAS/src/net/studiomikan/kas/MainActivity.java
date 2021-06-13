package net.studiomikan.kas;

//-------------------------------------------------------------------------------
//
//    KAS - KAG-like Android Script engine -
//
//    start   : 2011/2/18
//    auther  : okayu(おかゆ)
//    version : 0.4.3
//
//    This software is licensed under zlib/libpng License.
//
//    Copyright (c) 2011-2012 スタジオ蜜柑 All Rights Reserved.
//
//    This software is provided 'as-is', without any express or implied
//    warranty. In no event will the authors be held liable for any damages
//    arising from the use of this software.
//
//    Permission is granted to anyone to use this software for any purpose,
//    including commercial applications, and to alter it and redistribute it
//    freely, subject to the following restrictions:
//
//    1. The origin of this software must not be misrepresented; you must not
//    claim that you wrote the original software. If you use this software
//    in a product, an acknowledgment in the product documentation would be
//    appreciated but is not required.
//
//    2. Altered source versions must be plainly marked as such, and must not be
//    misrepresented as being the original software.
//
//    3. This notice may not be removed or altered from any source distribution.
//
//    スタジオ蜜柑 : http://studiomikan.net/
//    KAS配布サイト : http://studiomikan.net/kas/
//    mail : studiomikan@gmail.com
//
//------------------------------------------------------------------------------

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * メインアクティビティ
 * @author okayu
 */
public class MainActivity extends Activity
{
	/** KAN のバージョン */
	private static final String KAS_SystemVersion = "KAS version 0.4.3";

	/** メインのビュー */
	public MainSurfaceView mainView = null;
	/** メニューが表示できるかどうか */
	public static boolean menuVisible = true;
	/** メニューアイテム */
	public static HashMap<String, MenuItem> menuItem;
	/** メニューアイテムの設定データ */
	public static HashMap<String, MenuItemData> menuItemData;

	/** trueなら、スリープに入る時かスリープから復帰する時に終了する */
	public boolean exit = false;
	/** スリープ中 */
	public boolean pausing = false;
	/** ストップ中 */
	public static boolean stop = false;


	/** Called when the mainActivity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Util.log("!onCreate");

		Util.packageName = getPackageName();
		Util.systemVersion = KAS_SystemVersion;

		requestWindowFeature(Window.FEATURE_NO_TITLE);	// タイトル非表示
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);	// フルスクリーン
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	// スリープを抑制
		window.setFormat(PixelFormat.TRANSPARENT);

		// SDカードの存在確認
		String sdState = Environment.getExternalStorageState();
		if(!sdState.equals(Environment.MEDIA_MOUNTED))	// SDカードが無い
		{
			setContentView(R.layout.error);
			Button button = (Button)this.findViewById(R.id.button1);
			button.setOnClickListener((View.OnClickListener) this);
		}
		else	// SDカードがある
		{
			Util.handler = new Handler();	// ハンドラ登録
			Util.context = this.getApplicationContext(); // コンテキスト登録
			Util.mainActivity = this; // アクティビティ登録

			Config.Util_config();	// Util に Config.java の適用

			Util.setRootDirctory(getPackageName());	// ルートディレクトリ設定

			ResourceManager.getReady(getApplicationContext());	// リソース管理のクラスを設定
			Config.ResourceManager_config(Util.getMokaScript());	// ResourceManager に Config.java を適用

			createOptionMenuVisible();	// オプションメニューの表示を生成

			if(ResourceManager.downloadedResource() == 2)	// リソースのダウンロード
				download();
			else	// ゲーム起動
				startGame();
		}
	}

	/**
	 * ダウンロード
	 */
	private void download()
	{
		Util.log("ネットリソースのダウンロード");
		setContentView(R.layout.download);	// 表示
		ResourceManager.preGetNetResource(this);
	}

	/**
	 * ダウンロード終了イベント
	 */
	public void onDownloaded()
	{
		Util.log("ネットリソースのダウンロード終了");
		ResourceManager.endGetNetResource();
		startGame();
		if(Util.mainView != null)
		{
			mainView.onStart();
			mainView.onResume();
		}
	}

	/**
	 * ゲームの開始
	 */
	public void startGame()
	{
		setContentView(R.layout.main);	// 表示

		FrameLayout layout = (FrameLayout)this.findViewById(R.id.MainFrameLayout);
		FrameLayout kasLayout = (FrameLayout)findViewById(R.id.KASLayout);

		if(mainView == null)
			mainView = new MainSurfaceView(getApplicationContext());
		Util.mainView = this.mainView;

		layout.setBackgroundColor(Color.BLACK);
		layout.setFocusable(true);
		kasLayout.setBackgroundColor(Color.BLACK);
		kasLayout.setFocusable(true);

		kasLayout.addView(mainView.video.getLayout());	// ビデオ再生ビューを追加
		kasLayout.addView(mainView.layout);	// メインのビューを追加
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		Util.log("!onStop");
		if(Util.mainView != null)
			mainView.onStop();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Util.log("!onStart");
		if(Util.mainView != null)
			mainView.onStart();
	}

	@Override
	protected void onPause()
	{
		pausing = true;
		super.onPause();
		Util.log("!onPause");
		if(exit)
		{
			// 終了する
			exit = false;
			Util.closeGame();
		}
		else
		{
			if(mainView != null)
				mainView.onPause();
			ResourceManager.onPause();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		Util.log("!onResume");
		Util.mainActivity = this;
		if(exit)
		{
			// 終了する
			exit = false;
			Util.closeGame();
		}
		else
		{
			if(mainView != null)
				mainView.onResume();
			else
				ResourceManager.onResume(this);
			pausing = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		Util.log("!onSaveInstanceState");
		if(mainView != null)
			mainView.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		Util.log("!onRestoreInstanceState");
		if(mainView != null)
			mainView.onRestoreInstanceState(savedInstanceState);
	}

	// アクティビティの破棄
	@Override
	protected void onDestroy()
	{
		Util.mainActivity = null;
		Util.log("!onDestory");
		super.onDestroy();
		if(mainView != null)
			mainView.onEndGame();
		Util.mainView = this.mainView = null;
	}

	// オプションメニューの生成
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		Util.log("!onCreateOptionsMenu");

		if(menuItem == null) menuItem = new HashMap<String, MenuItem>();
		else menuItem.clear();

		if(!menuVisible)
			return false;

		// セーブ　ロード　メッセージ履歴　メッセージ非表示　スキップ　オート　設定　タイトル　終了

		// メニューアイテムの追加
		if(menuItemData.get("save").visible)
		{
			MenuItem itemSave = menu.add(menuItemData.get("save").text);
			itemSave.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
						callSaveActivity();
					return false;
			}});
			itemSave.setIcon(android.R.drawable.ic_menu_save);
			menuItem.put("save", itemSave);
		}

		if(menuItemData.get("load").visible)
		{
			MenuItem itemLoad = menu.add(menuItemData.get("load").text);
			itemLoad.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
						callLoadActivity();
					return false;
			}});
			itemLoad.setIcon(android.R.drawable.ic_menu_upload);
			menuItem.put("load", itemLoad);
		}

		if(menuItemData.get("history").visible)
		{
			MenuItem itemHistory = menu.add(menuItemData.get("history").text);
			itemHistory.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
						mainView.startHistory();
					return false;
			}});
			itemHistory.setIcon(android.R.drawable.ic_menu_recent_history);
			menuItem.put("history", itemHistory);
		}

		if(menuItemData.get("skip").visible)
		{
			MenuItem itemSkip = menu.add(menuItemData.get("skip").text);
			itemSkip.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
					{
						Util.toastMessage("スキップ開始");
						mainView.startSkip();
					}
					return false;
			}});
			itemSkip.setIcon(android.R.drawable.ic_menu_set_as);
			menuItem.put("skip", itemSkip);
		}

		if(menuItemData.get("hide").visible)
		{
			MenuItem itemHide = menu.add(menuItemData.get("hide").text);
			itemHide.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
						mainView.tag_hidemessage();
					return false;
			}});
			itemHide.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			menuItem.put("hide", itemHide);
		}

		if(menuItemData.get("auto").visible)
		{
			MenuItem itemAuto = menu.add(menuItemData.get("auto").text);
			itemAuto.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
					{
						Util.toastMessage("オートモード開始");
						mainView.startAuto();
					}
					return false;
			}});
			itemAuto.setIcon(android.R.drawable.ic_menu_rotate);
			menuItem.put("auto", itemAuto);
		}

		if(menuItemData.get("config").visible)
		{
			MenuItem itemConfig = menu.add(menuItemData.get("config").text);
			itemConfig.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
						callConfigActivity();
					return false;
			}});
			itemConfig.setIcon(android.R.drawable.ic_menu_preferences);
			menuItem.put("config", itemConfig);
		}

		if(menuItemData.get("title").visible)
		{
			MenuItem itemTitle = menu.add(menuItemData.get("title").text);
			itemTitle.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					if(mainView != null)
						mainView.tag_gotostart(null);
					return false;
			}});
			itemTitle.setIcon(android.R.drawable.ic_menu_revert);
			menuItem.put("title", itemTitle);
		}

		if(menuItemData.get("deldata").visible && ResourceManager.downloadedResource() == 1)
		{
			MenuItem itemDelete = menu.add(menuItemData.get("deldata").text);
			itemDelete.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem item){
					callDeleteResActivity();
					return false;
			}});
			itemDelete.setIcon(android.R.drawable.ic_menu_revert);
			menuItem.put("deldata", itemDelete);
		}

		if(menuItemData.get("exit").visible)
		{
			MenuItem itemExit = menu.add(menuItemData.get("exit").text);
			itemExit.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				public boolean onMenuItemClick(MenuItem arg0) {
					if(mainView != null)
						onClickExit();
					return false;
			}});
			itemExit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			menuItem.put("exit", itemExit);
		}

		resetOptionMenuEnabled();
		return true;
	}

	/**
	 * オプションメニュー：終了 が押された
	 */
	public void onClickExit()
	{
		if(Util.ask_exit)
		{
			Util.dialogMessage(
					Util.gameTitle, "終了しますか？", true,
					"はい", "いいえ",
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which){
							Util.closeGame();
						}
					},
					null
			);
		}
		else
			Util.closeGame();
	}

	/**
	 * オプションメニュー：設定 が押された
	 */
	public void callConfigActivity()
	{
		Intent intent = new Intent(this, ConfigActivity.class);
		ConfigActivity.setValue();	// 設定
		startActivityForResult(intent, 0);
	}

	/**
	 * オプションメニュー：ロード が押された
	 */
	public void callLoadActivity()
	{
		Intent intent = new Intent(this, SaveActivity.class);
		intent.putExtra("text", "load");
		startActivityForResult(intent, 0);
	}

	/**
	 * オプションメニュー：セーブ が押された
	 */
	public void callSaveActivity()
	{
		Intent intent = new Intent(this, SaveActivity.class);
		intent.putExtra("text", "save");
		startActivityForResult(intent, 0);
	}

	/**
	 * オプションメニュー：ゲームデータ削除 が押された
	 */
	public void callDeleteResActivity()
	{
		Intent intent = new Intent(this, DeleteResActivity.class);
		startActivityForResult(intent, 0);
	}

	// オプションメニューの表示
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		Util.log("!onMenuOpened");
    	if(Util.builtInOptionMenu)
    		return super.onPrepareOptionsMenu(menu);
    	else
    	{
    		if(Util.mainView != null)
    			Util.mainView.onClickMenu();
    		return false;
    	}
	}

	/**
	 * オプションメニューの表示・非表示を生成
	 */
	public void createOptionMenuVisible()
	{
		// セーブ　ロード　メッセージ履歴　メッセージ非表示　スキップ　オート　設定　タイトル　終了
		menuItemData = new HashMap<String, MenuItemData>();
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
		// Config.java 適用
		Config.Menu_config(Util.getMokaScript());
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
		MenuItem menu;
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
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		// 各種キーの動作
		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
		    switch(event.getKeyCode())
		    {
		    case KeyEvent.KEYCODE_BACK:	// 戻るボタンの動作を乗っ取る
	        	onClickExit();
	            return false;
		    }
		}
    	return super.dispatchKeyEvent(event);
	}

}

/**
 * メニューの設定情報<br>
 * @author okayu
 */
class MenuItemData
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


