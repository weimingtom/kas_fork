package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * システムボタン用レイヤ
 * @author okayu
 */
class SystemButtonLayer extends KASLayer
{
	/** ファイル名 */
	protected String filename;
	/** 式 */
	protected String exp;
	/** 動作 */
	protected ButtonFunc func;
	/** 画像 */
	protected Bitmap image;

	/** 描画元矩形 */
	protected Rect srcRect = new Rect();
	/** 描画先矩形 */
	protected Rect dstRect = new Rect();

	/** 有効かどうか */
	protected boolean enabled = true;

	/**
	 * コンストラクタ
	 * @param filename 画像ファイル名
	 * @param width 高さ
	 * @param height 幅
	 * @param exp スクリプト
	 * @param func クリック時に実行するメソッド
	 */
	public SystemButtonLayer(String filename, int width, int height, String exp, ButtonFunc func)
	{
		this.index = 10000000;

		this.filename = filename;
		this.width = width;
		this.height = height;
		this.exp = exp;
		this.func = func;

		image = ResourceManager.loadBitmap(Util.context.getResources(), filename, 1f);

		if(image != null)
			srcRect.set(0, 0, image.getWidth()/2, image.getHeight());
		else
			srcRect.set(0, 0, width, height);
		dstRect.set(0, 0, width, height);
	}

	/**
	 * 生成するだけ
	 */
	protected SystemButtonLayer()
	{
		this.index = 10000000;
	}

	/**
	 * 複製を生成
	 * @param src
	 */
	public static SystemButtonLayer getCopy(SystemButtonLayer src)
	{
		SystemButtonLayer sbl = new SystemButtonLayer();
		sbl.copyData(src);
		return sbl;
	}

	/**
	 * 破棄する
	 */
	public void delete()
	{
		ResourceManager.freeImage(image);
		func = null;
		srcRect = null;
		dstRect = null;
		exp = null;
		filename = null;
		enabled = false;
	}

	/**
	 * 状態をコピー
	 * @param src
	 */
	public void copyData(SystemButtonLayer src)
	{
		setPos(src.x, src.y);
		this.visible = src.visible;
		this.width = src.width;
		this.height = src.height;
		this.exp = src.exp;
		this.func = src.func;
		this.image = src.image;
		this.srcRect.set(src.srcRect.left, src.srcRect.top, src.srcRect.right, src.srcRect.bottom);
		this.dstRect.set(src.dstRect.left, src.dstRect.top, src.dstRect.right, src.dstRect.bottom);
		setEnabled(src.enabled);
	}

	// 描画イベント
	@Override
	public void onDraw(Context context, Canvas c, long nowTime)
	{
		if(visible)
		{
			if(image != null && image.isRecycled())
				image = ResourceManager.loadBitmap(Util.context.getResources(), filename, 1f);
			if(image != null && !image.isRecycled())
				c.drawBitmap(image, srcRect, dstRect, null);
		}
	}

	// レイヤのコピー
	@Override
	public void copyLayer(Context context, KASLayer layer, long nowTime)
	{
		copyData((SystemButtonLayer)layer);
		super.copyLayer(context, layer, nowTime);
	}

	// タッチイベント
	@Override
	public boolean onTouchEvent(int x, int y, int action, Context context)
	{
		return false;
	}

	/**
	 * タッチイベント　
	 * レイヤとしてのタッチイベントでは何もせず、プラグインとしてタッチイベントを処理する。
	 * こうすることで、locklink によるロックを回避する。
	 * また、正しい座標に変換されたタッチ位置を取得できる。
	 * @param x
	 * @param y
	 * @param action
	 * @param context
	 * @return
	 */
	public boolean onTouchEvent2(int x, int y, int action)
	{
		if(!enabled) return false;

		switch(action)
		{
		case MotionEvent.ACTION_DOWN:
			if(inside(dstRect, x, y))
				on();
			break;
		case MotionEvent.ACTION_UP:
			if(inside(dstRect, x, y))
			{
				onClicked();
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(inside(dstRect, x, y))
				on();
			else
				off();
			break;
		}
		return false;
	}

	/**
	 * クリック時の動作
	 */
	public void onClicked()
	{
		if(func != null)
			func.func(func);
		off();
	}

	/**
	 * 矩形の中にいるかどうか
	 * @param rect 矩形
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean inside(Rect rect, int x, int y)
	{
		if(enabled && rect != null)
			if(rect.left < x && x < rect.right &&
				rect.top < y && y < rect.bottom )
				return true;
		return false;
	}

	/**
	 * オンにする
	 */
	public void on()
	{
		if(image == null) return;
		int width = image.getWidth();
		srcRect.set(width/2, 0, width, image.getHeight());
	}

	/**
	 * オフにする
	 */
	public void off()
	{
		if(image == null) return;
		srcRect.set(0, 0, image.getWidth()/2, image.getHeight());
	}

	/**
	 * 有効・無効の設定
	 * @param enabled
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		if(!enabled)
			off();
	}

	/**
	 * 位置設定
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
		dstRect.set(x, y, x+width, y+height);
	}
}


/**
 * システムボタンを実装するクラス
 * @author okayu
 */
class SystemButton extends KASPlugin
{
	/** 表のボタン */
	private SystemButtonLayer[] fore;
	/** 裏のボタン */
	private SystemButtonLayer[] back;
	/** 有効無効 */
	private boolean enabled = true;
	/** セーブデータ */
	private JSONObject json = new JSONObject();
	/** ボタンの数 */
	private int buttonCount = 7;

	/**
	 * コンストラクタ
	 */
	public SystemButton()
	{
		super();

		createButtons();
		KASPlugin.addLayers(fore, back);	// レイヤを管理に追加

		// 表示
		for(int i = 0; i < buttonCount; i++)
		{
			fore[i].visible = true;
			back[i].visible = true;
		}

		// タグとして追加
		addTag("systembutton");
	}

	/**
	 * ボタン生成
	 * ボタンを追加する際にはこの中身を変更する
	 */
	private void createButtons()
	{
		ArrayList<SystemButtonLayer> array = new ArrayList<SystemButtonLayer>();
		SystemButtonLayer b;

		// SAVE
		b = new SystemButtonLayer("systembutton_save.png", 100, 25, null, new SaveAction());
		b.setPos(20, 425);
		array.add(b);
		// LOAD
		b = new SystemButtonLayer("systembutton_load.png", 100, 25, null, new LoadAction());
		b.setPos(130, 425);
		array.add(b);
		// CONFIG
		b = new SystemButtonLayer("systembutton_config.png", 100, 25, null, new ConfigAction());
		b.setPos(240, 425);
		array.add(b);
		// SKIP
		b = new SystemButtonLayer("systembutton_skip.png", 100, 25, null, new SkipAction());
		b.setPos(350, 425);
		array.add(b);
		// AUTO
		b = new SystemButtonLayer("systembutton_auto.png", 100, 25, null, new AutoAction());
		b.setPos(460, 425);
		array.add(b);
		// LOG
		b = new SystemButtonLayer("systembutton_log.png", 100, 25, null, new LogAction());
		b.setPos(570, 425);
		array.add(b);
		// HIDE
		b = new SystemButtonLayer("systembutton_hide.png", 100, 25, null, new HideAction());
		b.setPos(680, 425);
		array.add(b);


		// 登録
		buttonCount = array.size();
		fore = array.toArray(new SystemButtonLayer[0]);

		// 裏レイヤに複製
		back = new SystemButtonLayer[buttonCount];
		for(int i = 0; i < buttonCount; i++)
			back[i] = SystemButtonLayer.getCopy(fore[i]);
	}

	/**
	 * SAVE ボタンの動作
	 * @author okayu
	 */
	private static class SaveAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			// 右クリックサブルーチンとして rclicksave の *init を呼ぶ
			Util.mainView.rclickCall("rclicksave.ks", "*init");
		}
	}

	/**
	 * LOAD ボタンの動作
	 * @author okayu
	 */
	private static class LoadAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			// 右クリックサブルーチンとして rclickload.ks の *init を呼ぶ
			Util.mainView.rclickCall("rclickload.ks", "*init");
		}
	}

	/**
	 * CONFIG ボタンの動作
	 * @author okayu
	 */
	private static class ConfigAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			// 右クリックサブルーチンとして rclickconfig.ks の *start を呼ぶ
			Util.mainView.rclickCall("rclickconfig.ks", "*start");
		}
	}

	/**
	 * SKIP ボタンの動作
	 * @author okayu
	 */
	private static class SkipAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			Util.mainView.startSkip();
		}
	}

	/**
	 * AUTO ボタンの動作
	 * @author okayu
	 */
	private static class AutoAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			Util.mainView.startAuto();
		}
	}

	/**
	 * LOG ボタンの動作
	 * @author okayu
	 */
	private static class LogAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			Util.mainView.tag_showhistory();
		}
	}

	/**
	 * HIDE ボタンの動作
	 * @author okayu
	 */
	private static class HideAction implements ButtonFunc
	{
		@Override
		public void func(ButtonFunc b)
		{
			Util.mainView.tag_hidemessage();
		}
	}

//	// 追加用ひな形
//	private static class HogeAction implements ButtonFunc
//	{
//		@Override
//		public void func(ButtonFunc b)
//		{
//			// ここに処理を書く
//		}
//	}






	//--------------------------------------------------------------------
	// ここから下はプラグインとしての動作なので変更しないでください
	//--------------------------------------------------------------------

	// 安定したときの入れ替え
	@Override
	public void onStableStateChanged(boolean stable)
	{
		if(enabled)
		{
			if(stable)	// 安定した
			{
				for(int i = 0; i < buttonCount; i++)
					fore[i].setEnabled(true);
			}
			else	// 動き出した
			{
				for(int i = 0; i < buttonCount; i++)
					fore[i].setEnabled(false);
			}
		}
	}

	// 表と裏が入れ替わったとき
	@Override
	public void onExchangeForeBack()
	{
		// 管理が逆になるので、入れ替え
		SystemButtonLayer[] tmp = fore;
		fore = back;
		back = tmp;
	}

	// メッセージレイヤを隠す時の動作
	@Override
	public void onMessageHiddenStateChanged(boolean hidden)
	{
		for(int i = 0; i < buttonCount; i++)
			fore[i].visible = !hidden;
	}

	// タグとして呼ばれたときの動作
	@Override
	public void run(HashMap<String, String> elm)
	{
		String page = elm.get("page");
		String visible_s = elm.get("visible");
		String enabled_s = elm.get("enabled");
		boolean visible;
		boolean enabled;

		SystemButtonLayer[] layers;
		if(page == null || page.equals("fore")) layers = fore;
		else layers = back;

		if(visible_s != null)
		{
			if(visible_s.equals("true")) visible = true;
			else visible = false;
			for(int i = 0; i < buttonCount; i++)
				layers[i].visible = visible;
		}

		if(enabled_s != null)
		{
			if(enabled_s.equals("true")) enabled = true;
			else enabled = false;
			for(int i = 0; i < buttonCount; i++)
				layers[i].setEnabled(enabled);
			this.enabled = enabled;
		}
	}

	// セーブされるときの動作
	@Override
	public void onStore(JSONObject data) throws JSONException
	{
		json.put("forevisible", fore[0].visible);
		json.put("backvisible", back[0].visible);
		json.put("foreenabled", fore[0].enabled);
		json.put("backenabled", back[0].enabled);
		data.put("systembuttondata", json);
	}

	// ロードされるときの動作
	@Override
	public void onRestore(JSONObject data, boolean clear, boolean toback) throws JSONException
	{
		if(!data.has("systembuttondata")) return;

		JSONObject json = data.getJSONObject("systembuttondata");
		if(json == null) return;

		boolean forevisible, backvisible;
		boolean foreenabled, backenabled;

		if(toback) // 裏画面へのロード時
		{
			backvisible = json.getBoolean("forevisible");
			backenabled = json.getBoolean("foreenabled");
			for(int i = 0; i < buttonCount; i++)
			{
				back[i].visible = backvisible;
				back[i].setEnabled(backenabled);
			}
		}
		else	// 通常ロード
		{
			forevisible = json.getBoolean("forevisible");
			backvisible = json.getBoolean("backvisible");
			foreenabled = json.getBoolean("foreenabled");
			backenabled = json.getBoolean("backenabled");
			for(int i = 0; i < buttonCount; i++)
			{
				fore[i].visible = forevisible;
				fore[i].setEnabled(foreenabled);
				back[i].visible = backvisible;
				back[i].setEnabled(backenabled);
			}
		}

		// 有効にする
		enabled = true;
	}

	// レイヤ情報のコピーが行われるときに呼ばれる
	@Override
	public void onCopyLayer(boolean toback)
	{
		// 表示・非表示の情報のみコピーする
		if(toback)
		{
			for(int i = 0; i < buttonCount; i++)
				back[i].visible = fore[i].visible;
		}
		else
		{
			for(int i = 0; i < buttonCount; i++)
				fore[i].visible = back[i].visible;
		}
	}

	// タッチイベント
	@Override
	public boolean onTouchEvent(int x, int y, int action)
	{
		boolean result = false;
		for(int i = 0; i < buttonCount; i++)
		{
			if(fore[i].visible)
			{
				result = fore[i].onTouchEvent2(x, y, action);
				if(result)
					return true;
			}
		}
		return false;
	}

}


