package net.studiomikan.kas;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;


/**
 * セーブ・ロード画面のアクティビティ
 * @author okayu
 */
public class SaveActivity extends Activity
{
	/** リストビュー */
	private ListView listView = null;
	/** アダプタ */
	private SaveAdapter adapter = null;
	/** true ならセーブ画面、false ならロード画面 */
	boolean isSave = true;
	/** このアクティビティ終了時にリンクのロックを解除するかどうか　true なら解除する */
	private String unlocklink;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// セット
		Util.saveActivity = this;
		// フルスクリーン
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// スリープを抑制
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// インテントを受け取る
		String text = "";
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			text = extras.getString("text");
			if(text.equals("save"))
				isSave = true;
			else if(text.equals("load"))
				isSave = false;
			unlocklink = extras.getString("unlocklink");
		}
		else
			isSave = false;

		if(isSave)
			this.setTitle("【セーブ】" + Util.gameTitle);
		else
			this.setTitle("【ロード】" + Util.gameTitle);

		// リストビューの生成
		listView = new ListView(this);
		adapter = new SaveAdapter(this);
		setContentView(listView);
		listView.setAdapter(adapter);

		// リストビューのアイテムがクリックされた時のコールバックリスナー
		listView.setOnItemClickListener(
				new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ListView lv = (ListView) parent;
				// クリックされたアイテムを取得します
//				Util.log("SAVE ACTIVITY onItemClick");
				SaveData save = (SaveData) lv.getItemAtPosition(position);
//				Util.log("SAVE ACTIVITY date=" + save.date + " text:" + save.text);
				if(isSave)	// セーブ
				{
					if(Util.mainView != null)
					{
						Util.mainView.saveGame(position);
						save.date = Util.latestSaveDate;
						save.text = Util.latestSaveText;
						adapter.notifyDataSetChanged();
					}
				}
				else	// ロード
				{
					if(Util.mainView != null && Util.saveDataExistence(position))
					{
						Util.mainView.loadGame(position);
						Util.mainView.resumeSound = false;
						Util.saveActivity.finish();
					}
				}
			}
		});
	}

	/**
	 * 破棄イベント
	 */
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(unlocklink != null && unlocklink.equals("true"))
		{
			if(Util.mainView != null)
				Util.mainView.tag_unlocklink();
		}
	}


}
