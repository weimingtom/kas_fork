package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;

import net.studiomikan.kas4pc.klib.Bitmap;
import net.studiomikan.kas4pc.klib.Canvas;
import net.studiomikan.kas4pc.klib.Context;
import net.studiomikan.kas4pc.klib.Paint;
import net.studiomikan.kas4pc.klib.PorterDuff;

/**
 * トランジションの管理クラス
 * @author okayu
 */
class Transition
{
	/** コンテキスト */
	private Context context = null;
	/** 親 */
	private MainSurfaceView owner = null;

	/** 時間　ミリ秒 */
	private int time = 0;
	/** 開始時間　ミリ秒 */
	private long startTime = 0;
	/** あいまい領域値 */
	private int vague = 10;
	/** メソッド 0:universal 1:crossfade 2:scroll */
	private int method = 0;
	/** 対象レイヤ */
	private KASLayer transLayer = null;
	/** 対象レイヤの番号 */
	private int transLayerNum = 0;
	/** 対象レイヤのタイプ */
	private char transLayerType = 'b';
	/** ユニバーサルトランジションのルール */
	private short[] universalRule = null;

	/** スキップする */
	private boolean doSkip = false;
	/** スキップ可能かどうか */
	public boolean canskip = true;
	/** スキップモード中 */
	public boolean skipMode = false;

	/** 表ページ描画用 */
	public Bitmap bitmap_fore = null;
	/** 表ページ描画用 */
	public Canvas canvas_fore = null;
	/** 裏ページ描画用 */
	public Bitmap bitmap_back = null;
	/** 裏ページ描画用 */
	public Canvas canvas_back = null;
	/** 描画用 Paint */
	public Paint paint = null;

	/** 描画用配列 */
	private int[] bitmapArray = null;
	/** ユニバーサルトランジション用配列 */
	private int[] table = null;
	/** ユニバーサルトランジション用　値の最大値 */
	private int phasemax = 255;

	/** スクロール用 0:nostay 1:stayfore 2:stayback */
	private int stay = 0;
	/** スクロール用 0:top 1:right 2:bottom 3:left */
	private int from = 0;

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 * @param owner 親
	 */
	public Transition(Context context, MainSurfaceView owner)
	{
		this.context = context;
		this.owner = owner;

		bitmap_back = Bitmap.createBitmap(Util.standardDispWidth, Util.standardDispHeight, Bitmap.Config.RGB_565);
		ResourceManager.addSystemMem(bitmap_back);
		canvas_back = new Canvas(bitmap_back);
		paint = new Paint();
		universalRule = new short[Util.standardDispWidth*Util.standardDispHeight];
		bitmapArray = new int[Util.standardDispWidth*Util.standardDispHeight];
		table = new int[256];
	}

	/**
	 * トランジションの準備
	 * @param elm 属性
	 * @param pauseTime 停止時間
	 * @return trueで準備成功　falseで失敗、属性にエラー
	 */
	public boolean setTrans(HashMap<String, String> elm, long pauseTime)
	{
		String layer = elm.get("layer");
		String time_s = elm.get("time");
		String canskip = elm.get("canskip");
		String method = elm.get("method");
		String rule = elm.get("rule");
		String vague = elm.get("vague");

		if(time_s == null)
			return false;

		int time = Integer.decode(time_s);

		this.time = time;
		this.canskip = owner.tag_sub_getBoolean(canskip, true);

		// あいまい領域値
		if(vague == null) this.vague = 64;
		else this.vague = Integer.decode(vague);

		// layer指定がなければ全レイヤ->transLayer=null
		if(layer == null)
			transLayer = null;
		else
		{
			int num;
			// レイヤ指定あり
			if(layer.charAt(0) == 'm')
			{
				num = owner.tag_sub_getMessageLayerNumber(layer);
				transLayer = owner.message_back[num];
				transLayerNum = num;
				transLayerType = 'm';
			}
			else if(layer.charAt(0) == 'b')
			{
				transLayer = owner.baseLayer_back;
				transLayerNum = -1;
				transLayerType = 'b';
			}
			else
			{
				num = Integer.decode(layer);
				transLayer = owner.layer_back[num];
				transLayerNum = num;
				transLayerType = 'l';
			}
		}

		// ユニバーサルトランジション or その他
		if(method == null || method.equals("universal"))
		{
			boolean loaded = ResourceManager.loadRuleFile(context.getResources(), rule, universalRule);
			if(loaded)
			{
				phasemax = 255 + this.vague;
				this.method = 0;
			}
			else
			{
				// ルール画像が無かった場合は強制的にクロスフェード
				Util.log("TRANS ルール画像" + rule + "は存在しません");
				this.method = 1;
			}
		}
		else
		{
			if(method.equals("crossfade"))
				this.method = 1;
			else if(method.equals("scroll"))
			{
				this.method = 2;
				String stay = elm.get("stay");
				String from = elm.get("from");
				if(stay == null) this.stay = 0;
				else if(stay.equals("stayfore")) this.stay = 1;
				else if(stay.equals("stayback")) this.stay = 2;
				else this.stay = 0;
				if(from == null) this.from = 0;
				else if(from.equals("right")) this.from = 1;
				else if(from.equals("bottom")) this.from = 2;
				else if(from.equals("left")) this.from = 3;
				else this.from = 0;
				if(this.from == 0 || this.from == 2)	// 上下
					phasemax = Util.standardDispHeight;
				else
					phasemax = Util.standardDispWidth;
				if(bitmap_fore == null)
					bitmap_fore = Bitmap.createBitmap(Util.standardDispWidth, Util.standardDispHeight, Bitmap.Config.RGB_565);
				if(canvas_fore == null)
					canvas_fore = new Canvas(bitmap_fore);
			}
			else
			{
				Util.log("TRANS " + method + "というトランジションは存在しません");
				this.method = 1;	// 強制的にクロスフェード
			}
		}
		// 画面の準備
		drawBackLayer(canvas_back);
		if(this.method == 0)
		{
			bitmap_back.getPixels(bitmapArray, 0, Util.standardDispWidth, 0, 0, Util.standardDispWidth, Util.standardDispHeight); // 配列に
			for(int i = 0; i < bitmapArray.length; i++)
				bitmapArray[i] &= 0x00ffffff;
		}

		// スキップリセット
		doSkip = false;
		// 開始時間
		startTime = Util.getNowTime() - pauseTime;

		return true;
	}

	/**
	 * 全レイヤ対象トランジションなら true を返す
	 * @return
	 */
	public boolean isAllLayerTrans()
	{
		return transLayer == null;
	}

	/**
	 * スキップの設定
	 * @param skipMode スキップ
	 */
	public void setSkip(boolean skipMode)
	{
		this.skipMode = skipMode;
	}

	/**
	 * 入力処理
	 */
	public void onDown()
	{
		if(canskip)
			doSkip = true;
	}

	/**
	 * 描画
	 * @param c キャンバス
	 * @param nowTime 現在時刻
	 * @return true:終了 false:途中
	 */
	public boolean onDraw(Canvas c, long nowTime)
	{
		// スキップモードなら無条件でtrueを返して終了
		if(skipMode && canskip)
		{
			owner.drawLayers(c, owner.baseLayer_fore, owner.layerOrder_fore);
			stopTrans();
			return true;
		}

		switch(method)
		{
		case 0:	// universal
			transDraw_universal(c, nowTime);
			break;
		case 1:	// crossfade
			transDraw_fade(c, nowTime);
			break;
		case 2:	// scroll
			transDraw_scroll(c, nowTime);
			break;
		default:
			transDraw_fade(c, nowTime);
			break;
		}


		// 終了
		if(doSkip || time < nowTime - startTime)
		{
			stopTrans();
			return true;
		}
		return false;
	}

	/**
	 * トランジションの終了
	 */
	public void stopTrans()
	{
		switchPage();
	}

	/**
	 * 表と裏の入れ替え
	 */
	public void switchPage()
	{
		if(transLayer == null) // 全レイヤ
		{
			Layer l;
			l = owner.baseLayer_fore;
			owner.baseLayer_fore = owner.baseLayer_back;
			owner.baseLayer_back = l;

			Layer[] la = owner.layer_fore;
			owner.layer_fore = owner.layer_back;
			owner.layer_back = la;

			MessageLayer[] ma = owner.message_fore;
			owner.message_fore = owner.message_back;
			owner.message_back = ma;

			ArrayList<KASLayer> pl = owner.pluginsLayer_fore;
			owner.pluginsLayer_fore = owner.pluginsLayer_back;
			owner.pluginsLayer_back = pl;

			// カレントレイヤの入れ替え
			if(owner.currentMessagePage.equals("back"))
				owner.changeCurrent(owner.currentMessageNum, "back");
			else
				owner.changeCurrent(owner.currentMessageNum, "fore");
		}
		else	// 一部レイヤ
		{
			if(transLayerType == 'm')
			{
				MessageLayer m = owner.message_fore[transLayerNum];
				owner.message_fore[transLayerNum] = owner.message_back[transLayerNum];
				owner.message_back[transLayerNum] = m;
			}
			else
			{
				Layer l = owner.layer_fore[transLayerNum];
				owner.layer_fore[transLayerNum] = owner.layer_back[transLayerNum];
				owner.layer_back[transLayerNum] = l;
			}
		}
		// 描画順入れ替え
		KASLayer[] array = owner.layerOrder_fore;
		owner.layerOrder_fore = owner.layerOrder_back;
		owner.layerOrder_back = array;
	}

	/**
	 * ユニバーサルトランジション
	 * @param c キャンバス
	 * @param nowTime 現在時刻
	 */
	private void transDraw_universal(Canvas c, long nowTime)
	{
		owner.drawLayers(c, owner.baseLayer_fore, owner.layerOrder_fore);	// 表

		univTransGetAlpha(nowTime);
		int len = universalRule.length;
		int rule;
		for(int i = 0; i < len; ++i)
		{
			rule = universalRule[i];
			bitmapArray[i] = (bitmapArray[i] & 0x00FFFFFF) + table[rule];
		}

		c.drawBitmap(bitmapArray, 0, Util.standardDispWidth, 0, 0, Util.standardDispWidth, Util.standardDispHeight, true, null);
	}

	/**
	 * ユニバーサルトランジションの濃度を得る
	 * @param nowTime
	 */
	private void univTransGetAlpha(long nowTime)
	{
		int phase = (int)( (nowTime-startTime) * phasemax / time );
		phase -= vague;

		for(int i = 0; i < 256; ++i)
		{
			if(i < phase) table[i] = 0xFF000000;
			else if(i >= phasemax) table[i] = 0;
			else
			{
				int tmp = 255-(( i - phase)*255 / vague);
				if(tmp < 0) tmp = 0;
				if(tmp > 255) tmp = 255;
				table[i] = tmp<<24;
			}
		}
	}

	/**
	 * 裏の描画
	 * @param c キャンバス
	 */
	private void drawBackLayer(Canvas c)
	{
		// クリア
		c.drawColor(0,PorterDuff.Mode.CLEAR);
		owner.drawLayers(c, owner.baseLayer_back, owner.layerOrder_back);
	}

	/**
	 * 組み込みトランジション　フェードイン
	 * @param c キャンバス
	 * @param nowTime 現在時刻
	 */
	private void transDraw_fade(Canvas c, long nowTime)
	{
		int phase = (int)( (nowTime - startTime) * 255 / time );
		if(phase < 0) phase = 0;
		if(phase > 255) phase = 255;

		// foreを描画
		owner.drawLayers(c, owner.baseLayer_fore, owner.layerOrder_fore);
		// backを描画
		paint.setAlpha(phase);
		c.drawBitmap(bitmap_back, 0, 0, paint);
	}

	/**
	 * 組み込みトランジション　スクロール
	 * @param c キャンバス
	 * @param nowTime 現在時刻
	 */
	private void transDraw_scroll(Canvas c, long nowTime)
	{
		int phase = (int)( (double)(nowTime - startTime) * phasemax / time );
		if(phase < 0) phase = 0;
		if(phase > phasemax) phase = phasemax;

		switch(stay)
		{
		case 0:	// nostay
			owner.drawLayers(canvas_fore, owner.baseLayer_fore, owner.layerOrder_fore);
			switch(from)
			{
			case 0:
				c.drawBitmap(bitmap_back, 0, phase-phasemax, null);
				c.drawBitmap(bitmap_fore, 0, phase, null);
				break;
			case 1:
				c.drawBitmap(bitmap_back, phasemax-phase, 0, null);
				c.drawBitmap(bitmap_fore, -phase, 0, null);
				break;
			case 2:
				c.drawBitmap(bitmap_back, 0, phasemax-phase, null);
				c.drawBitmap(bitmap_fore, 0, -phase, null);
				break;
			case 3:
				c.drawBitmap(bitmap_back, phase-phasemax, 0, null);
				c.drawBitmap(bitmap_fore, phase, 0, null);
				break;
			}
			break;
		case 1:	// stayfore
			owner.drawLayers(c, owner.baseLayer_fore, owner.layerOrder_fore);
			switch(from)
			{
			case 0: c.drawBitmap(bitmap_back, 0, phase-phasemax, null); break;
			case 1: c.drawBitmap(bitmap_back, phasemax-phase, 0, null); break;
			case 2: c.drawBitmap(bitmap_back, 0, phasemax-phase, null); break;
			case 3: c.drawBitmap(bitmap_back, phase-phasemax, 0, null); break;
			}
			break;
		case 2:	// stayback
			c.drawBitmap(bitmap_back, 0, 0, null);
			owner.drawLayers(canvas_fore, owner.baseLayer_fore, owner.layerOrder_fore);
			switch(from)
			{
			case 0: c.drawBitmap(bitmap_fore, 0, phase, null); break;
			case 1: c.drawBitmap(bitmap_fore, -phase, 0, null); break;
			case 2: c.drawBitmap(bitmap_fore, 0, -phase, null); break;
			case 3: c.drawBitmap(bitmap_fore, phase, 0, null); break;
			}
			break;
		}

	}

}
