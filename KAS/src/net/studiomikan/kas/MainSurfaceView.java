package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.studiomikan.moka.MOKA_VALUE_TYPE;
import net.studiomikan.moka.MokaValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;


/**
 * メインのサーフェイスビュークラス<br>
 * 各レイヤの保持、管理、処理の統括をする<br>
 * 描画も担当する
 * @author okayu
 */
public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
	/** コンテキスト */
	private Context context = null;
	/** サーフェイスホルダー */
	private SurfaceHolder holder = null;
	/** このビューの入っているレイアウト */
	public FrameLayout layout = null;
	/** キャンバス */
	private Canvas canvas = null;
	/** 描画用キャンバス */
	public Canvas drawCanvas = null;
	/** 描画用ビットマップ */
	private Bitmap drawBitmap = null;
	/** 描画元矩形 */
	private Rect drawRectSrc = null;
	/** 描画先矩形 */
	private Rect drawRectDst = null;
	/** サーフェイスビューの定期的描画用 */
	private ScheduledExecutorService executor = null;
	/** 終了直前に無駄に描画を行わないためのフラグ */
	public boolean inTheEnd = false;

	// ゲーム進行制御関連
	/** コンダクタ */
	public Conductor conductor = null;
	/** コンダクタ実行用スレッド */
	private Thread conductorThread = null;
	/** タグハンドラ */
	private TagHandlers tagHandlers = null;
	/** システム状態の種類 */
	public enum GAMESTATE
	{
		/** デフォルト */
		DEFAULT,
		/** テキスト出力中 */
		TEXT,
		/** キー入力待ち */
		WAITKEY,
		/** キー入力待ち 行末 */
		WAITKEY_LINE,
		/** キー入力待ち 改ページ */
		WAITKEY_PAGE,
		/** 一定時間待っている */
		WAIT,
		/** 止まっている */
		STOP,
		/** トランジション中 */
		TRANS,
		/** 履歴表示中 */
		HISTORY,
		/** メッセージ非表示中 */
		HIDEMESSAGE,
		/** 画面揺れの終了待ち */
		WQ,
		/** 自動移動の終了待ち */
		WM,
		/** BGMフェードの終了待ち */
		WB,
		/** SEフェードの終了待ち */
		WF,
		/** BGM再生の終了待ち */
		WL,
		/** SE再生の終了待ち */
		WS,
		/** ビデオ再生の終了待ち */
		WV,
		/** アニメーションの終了待ち */
		WA,
	};
	/** システムの状態 */
	public GAMESTATE gameState = null;
	/** メニューアイテムのオンオフの、状態記憶用 */
	private boolean menuItemsEnabledBuff;
	/** WS の canskip */
	private boolean ws_canskip = false;
	/** WL の canskip */
	private boolean wl_canskip = false;
	/** WF の canskip */
	private boolean wf_canskip = false;
	/** WB の canskip */
	private boolean wb_canskip = false;
	/** WM の canskip */
	private boolean wm_canskip = false;
	/** WS で待つバッファ */
	private int ws_buff = 0;
	/** WF で待つバッファ */
	private int wf_buff = 0;
	/** WA で待つレイヤ */
	private Layer wa_layer = null;

	// テキスト操作
	/** メッセージを隠す前のシステム状態 */
	private GAMESTATE hidemessage_stateLog = null;
	/** スキップモード */
	public boolean skipMode = false;
	/** スキップモードの種類 */
	public int skipModeType = 1;
	/** ノーウェイト */
	private boolean flag_nowait = false;
	/** オートモード */
	private boolean autoMode = false;
	/** オートモードで待つ時間 ミリ秒 */
	public int autoModeInterval = 500;
	/** オートモードの開始時間 */
	private long autoModeStartTime = 0;
	/** wait の canskip */
	private boolean waitCanskip = false;

	/** テキスト表示のインターバル */
	private int textInterval = 50;
	/** テキスト表示のインターバル ユーザー指定 */
	private int userTextInterval = 50;
	/** テキスト表示の時間をユーザー指定で行う */
	private boolean userDelay = true;

	/** 自動ウェイトの文字とウェイト */
	private HashMap<Character, Integer> autoWaitChar = new HashMap<Character, Integer>();
	/** クリックスキップ */
	private boolean clickskip = true;

	/** 画面を揺らす時間 */
	private int quakeTime = 0;
	/** 画面揺らしの開始時間 */
	private long quakeStartTime = 0;
	/** 画面揺らしの幅 */
	private int quakeHmax = 0;
	/** 画面揺らしの高さ */
	private int quakeVmax = 0;
	/** 画面揺らしフラグ */
	private boolean quakeFlag = false;
	/** 画面揺らしを待つときのcanskip */
	private boolean quake_canskip = false;

	/** リンクのロック */
	public boolean locklink = false;

	/** 簡易演出なら true */
	public boolean easyDirection = false;
	/** 画像読み込みサイズ ただしこの値は次回起動の時に有効になる */
	public float layerImageScale = 1.0f;

	// トランジション関連
	/** トランジション管理クラス */
	public Transition transition = null;

	// 履歴関連
	/** 履歴管理クラス */
	public History history = null;
	/** 履歴表示前のシステム状態 */
	private GAMESTATE history_gameStateLog = null;

	/** 入力イベントを一度だけ無視する */
	private boolean ignoreInputOnce = false;

	// レイヤ関連
	/** 前景レイヤの数 */
	public int layerNum = 3;
	/** メッセージレイヤの数 */
	public int messageLayerNum = 2;
	/** 背景レイヤ fore */
	public Layer baseLayer_fore = null;
	/** 背景レイヤ back */
	public Layer baseLayer_back = null;
	/** 前景レイヤ fore */
	public Layer[] layer_fore = null;
	/** 前景レイヤ back */
	public Layer[] layer_back = null;
	/** メッセージレイヤ fore */
	public MessageLayer[] message_fore = null;
	/** メッセージレイヤ back */
	public MessageLayer[] message_back = null;
	/** カレントメッセージレイヤ */
	public MessageLayer currentMessage = null;
	/** カレントメッセージレイヤの裏 */
	public MessageLayer currentMessageBack = null;
	/** カレントメッセージレイヤの番号 */
	public int currentMessageNum = 0;
	/** カレントメッセージレイヤのページ */
	public String currentMessagePage = "";

	/** レイヤ描画順用配列 fore */
	public KASLayer[] layerOrder_fore = null;
	/** レイヤ描画順用配列 back */
	public KASLayer[] layerOrder_back = null;

	// プラグイン
	/** プラグインレイヤ 表 */
	public ArrayList<KASLayer> pluginsLayer_fore = new ArrayList<KASLayer>();
	/** プラグインレイヤ 裏 */
	public ArrayList<KASLayer> pluginsLayer_back = new ArrayList<KASLayer>();
	/** プラグイン */
	public ArrayList<KASPlugin> kasPlugins = new ArrayList<KASPlugin>();

	// タッチイベント関連
	/** タッチ操作によるノーウェイト */
	private boolean touch_nowait = false;

	// スクリプト関連
	/** スクリプト管理 */
	public MokaScript scripter = null;

	// 音楽
	/** BGM バッファ */
	public Sound bgmBuff = null;
	/** SE バッファ */
	public Sound[] seBuff = null;
	/** SE バッファの数 */
	public int seBuffNum = 3;

	// メニューボタンクリック（右クリック相当）
	/** メニュークリック時のジャンプ先 */
	public String menuClickStorage = "";
	/** メニュークリック時のジャンプ先ラベル */
	public String menuClickTarget = "";
	/** メニュークリックの有効無効 */
	public boolean menuClickEnabled = true;
	/** メニュークリック時の動作 シナリオジャンプ */
	public boolean menuClickJump = false;
	/** メニュークリック時の動作 サブルーチン呼び出し */
	public boolean menuClickCall = false;

	// セーブデータ関連
	/** 一度だけ startanchor を無視する */
	private boolean ignoreStartanchor = false;
	/** 最終ラベル時点でのセーブデータ */
	private JSONObject saveBuffer = null;
	/** 最終ラベル時点でのセーブデータのラベルテキスト */
	private String saveBuffer_text = "";
	/** オートセーブ用セーブデータ */
	private JSONObject autoSaveBuffer = null;
	/** オートセーブの有効無効 */
	private boolean enableAutoSave = false;
	/** オートレジュームの有効無効 */
	private boolean enableAutoResume = true;
	/** セーブ＆ロード機能の有効無効 */
	private boolean enableStore = true;
	/** セーブ機能の有効無効 */
	private boolean enableSave = false;
	/** ロード機能の有効無効 */
	private boolean enableLoad = true;
	/** 最初へ戻る機能の有効無効 */
	public boolean enableStartAnchor = true;
	/** メモリへのセーブ用バッファ */
	private HashMap<Integer, JSONObject> tmpSave;
	/** サムネイル用画像 */
	private Bitmap thumbnailBitmap = null;
	/** サムネイル用キャンバス */
	private Canvas thumbnailCanvas = null;
	/** サムネイルの矩形 */
	private Rect thumbnailRect = null;
	/** サムネイルのロック */
	private boolean lockThumbnail = false;

	// ダイアログ
	/** YesNo ダイアログ */
	private YesNoDialog yesNoDialog = null;
	/** 閉じるときの動作 */
	private ButtonFunc closeFunc = null;
	/** ダイアログ終了時の動作 */
	private ButtonFunc endYesNoFunc = null;
	/** セーブ動作 */
	private ButtonFunc saveFunc = null;
	/** ロード動作 */
	private ButtonFunc loadFunc = null;
	/** セーブ or ロードの場所 */
	private int ask_saveOrLoadPlace = 0;
	/** 最初へ戻る動作 */
	private ButtonFunc gotostartFunc = null;

	// 動画
	/** 動画再生クラス */
	public VideoPlayer video = null;

	/** onRestoreInstanceState で受けとった復帰用セーブデータ */
	private JSONObject savedInstanceData = null;
	/** onResume で、サウンドをリスタートするかどうか */
	public boolean resumeSound = true;	// サウンドをリスタートするかどうか
	/** onResume で、autosave を読み出すかどうか */
	public boolean resumeAutoSave = false;


	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	public MainSurfaceView(Context context)
	{
		super(context);
		this.context = context;
		enableAutoResume = Util.enableAutoResume;

		// サーフェイスホルダーの生成
		holder = getHolder();
		holder.addCallback(this);
		holder.setFixedSize(getWidth(), getHeight());
		setFocusable(true);
		requestFocus();

		// 描画の準備
		drawBitmap = Bitmap.createBitmap(Util.standardDispWidth, Util.standardDispHeight, Bitmap.Config.RGB_565);
		ResourceManager.addSystemMem(drawBitmap);
		drawCanvas = new Canvas(drawBitmap);
		drawRectSrc = new Rect(0, 0, Util.standardDispWidth, Util.standardDispHeight);
		setDrawRectDst(); // この時点では仮設定。

		// スクリプトの用意
		scripter = Util.getMokaScript();

		// ゲームの初期化
		initGame();
		setMenuEnabled(false);

		// レイアウトに格納
		layout = new FrameLayout(context);
		layout.addView(this);
	}

	/**
	 * ゲームの初期化
	 */
	private void initGame()
	{
		// レイヤの作成
		createLayers();

		// Config.java の適用
		Config.Main_config(this, scripter);

		gameState = GAMESTATE.DEFAULT;

		// セーブ準備
		tmpSave = new HashMap<Integer, JSONObject>();
		int thumbnailHeight = (int)( Util.thumbnailWidth * ((double)Util.standardDispHeight/Util.standardDispWidth) );
		thumbnailRect = new Rect(0, 0, Util.thumbnailWidth, thumbnailHeight);
		thumbnailBitmap = Bitmap.createBitmap(Util.thumbnailWidth, thumbnailHeight, Bitmap.Config.RGB_565);
		ResourceManager.addSystemMem(thumbnailBitmap);
		thumbnailCanvas = new Canvas(thumbnailBitmap);

		// ダイアログの準備
		yesNoDialog = new YesNoDialog(context.getResources());
		closeFunc = new ButtonFunc(){
			@Override
			public void func(ButtonFunc b) { Util.closeGame(); }
		};
		endYesNoFunc = new ButtonFunc() {
			@Override
			public void func(ButtonFunc b) { yesNoDialog.visible = false; conductor.startConductor(); };
		};
		saveFunc = new ButtonFunc() {
			@Override
			public void func(ButtonFunc b) { saveGame(ask_saveOrLoadPlace); conductor.startConductor(); }
		};
		loadFunc = new ButtonFunc() {
			@Override
			public void func(ButtonFunc b) { loadGame(ask_saveOrLoadPlace); conductor.startConductor(); }
		};
		gotostartFunc = new ButtonFunc() {
			@Override
			public void func(ButtonFunc b) { gotostart(); conductor.startConductor(); }
		};

		tagHandlers = new TagHandlers(this);		// タグハンドラの作成
		conductor = new Conductor(context, this);	// コンダクタ生成
		transition = new Transition(context, this);	// トランジションの作成
		history = new History(this);				// 履歴の作成

		// 音
		bgmBuff = new Sound();
		seBuff = new Sound[seBuffNum];
		for(int i = 0; i < seBuff.length; i++)
			seBuff[i] = new Sound();

		// ビデオの準備
		video = new VideoPlayer(context);
		video.setPos(0, 0);
		video.setSize(Util.standardDispWidth, Util.standardDispHeight);

		// システム変数の適用
		loadSystemStateFromSf();
	}

	/**
	 * レイヤの生成
	 */
	private void createLayers()
	{
		// 背景レイヤ
		baseLayer_fore = new Layer(context);
		baseLayer_back = new Layer(context);
		baseLayer_fore.layerType = 'B';
		baseLayer_fore.setPos(0, 0);
		baseLayer_fore.setSize(Util.standardDispWidth, Util.standardDispHeight);
		baseLayer_fore.setVisible(true);
		baseLayer_back.layerType = 'B';
		baseLayer_back.setPos(0, 0);
		baseLayer_back.setSize(Util.standardDispWidth, Util.standardDispHeight);
		baseLayer_back.setVisible(true);
		// 前景レイヤ
		layer_fore = new Layer[layerNum];
		layer_back = new Layer[layerNum];
		for(int i = 0; i < layerNum; i++)
		{
			layer_fore[i] = new Layer(context);
			layer_back[i] = new Layer(context);
			layer_fore[i].index = layer_back[i].index = 1000 + (1000*i);
		}
		// メッセージレイヤ
		message_fore = new MessageLayer[messageLayerNum];
		message_back = new MessageLayer[messageLayerNum];
		for(int i = 0; i < messageLayerNum; i++)
		{
			message_fore[i] = new MessageLayer(context);
			message_back[i] = new MessageLayer(context);
			message_fore[i].index = message_back[i].index = 1000000 + (1000*i);
		}
		currentMessagePage = "fore";		// 表の
		currentMessageNum = 0;				// ゼロに
		currentMessage = message_fore[0];	// 設定
		currentMessage.setVisible(true);	// 表示
		currentMessageBack = message_back[0];	// カレントの裏

		setLayerOrder();
	}

	/**
	 * レイヤの描画順を設定
	 */
	public void setLayerOrder()
	{
		layerOrder_fore = new KASLayer[layerNum+messageLayerNum+pluginsLayer_fore.size()];
		layerOrder_back = new KASLayer[layerNum+messageLayerNum+pluginsLayer_back.size()];
		// とりあえず入れる
		int i = 0;
		int j = 0;
		for(i = 0; i < layerNum; i++)
		{
			layerOrder_fore[j] = layer_fore[i];
			layerOrder_back[j] = layer_back[i];
			j++;
		}
		for(i = 0; i < messageLayerNum; i++)
		{
			layerOrder_fore[j] = message_fore[i];
			layerOrder_back[j] = message_back[i];
			j++;
		}
		int size;
		size = pluginsLayer_fore.size();
		for(i = 0; i < size; i++)
		{
			layerOrder_fore[j] = pluginsLayer_fore.get(i);
			layerOrder_back[j] = pluginsLayer_back.get(i);
			j++;
		}
		DataComparator dc = new DataComparator();
		Arrays.sort(layerOrder_fore, dc);
		Arrays.sort(layerOrder_back, dc);
	}

	/**
	 * 前景レイヤの数を変更
	 * @param newCount 変更後のレイヤ数
	 */
	public void changeLayerCount(int newCount)
	{
		if(newCount < 0)
			return;

		int oldCount = layer_fore.length;
		if(newCount != oldCount)
		{
			Layer[] newLayers_fore = new Layer[newCount];
			Layer[] newLayers_back = new Layer[newCount];
			if(newCount < oldCount)	// これまでより減る
			{
				for(int i = 0; i < newCount; i++)
				{
					newLayers_fore[i] = layer_fore[i];
					newLayers_back[i] = layer_back[i];
				}
			}
			else	// 増える
			{
				int i;
				for(i = 0; i < oldCount; i++)
				{
					newLayers_fore[i] = layer_fore[i];
					newLayers_back[i] = layer_back[i];
				}
				for( ; i < newCount; i++)	// 追加分
				{
					newLayers_fore[i] = new Layer(context);
					newLayers_back[i] = new Layer(context);
					newLayers_fore[i].index = newLayers_back[i].index = 1000 + (1000*i);
				}
			}
			layerNum = newCount;
			layer_fore = newLayers_fore;
			layer_back = newLayers_back;

			setLayerOrder();
		}
	}

	/**
	 * メッセージレイヤの数を変更
	 * @param newCount 変更後のレイヤ数
	 */
	public void changeMessageLayerCount(int newCount)
	{
		if(newCount < 0)
			return;

		int oldCount = message_fore.length;
		if(newCount != oldCount)
		{
			MessageLayer[] newLayers_fore = new MessageLayer[newCount];
			MessageLayer[] newLayers_back = new MessageLayer[newCount];
			if(newCount < oldCount)	// 減る
			{
				for(int i = 0; i < newCount; i++)
				{
					newLayers_fore[i] = message_fore[i];
					newLayers_back[i] = message_back[i];
				}
			}
			else	// 増える
			{
				int i;
				for(i = 0; i < oldCount; i++)
				{
					newLayers_fore[i] = message_fore[i];
					newLayers_back[i] = message_back[i];
				}
				for( ; i < newCount; i++)	// 追加分
				{
					newLayers_fore[i] = new MessageLayer(context);
					newLayers_back[i] = new MessageLayer(context);
					newLayers_fore[i].index = newLayers_back[i].index = 1000000 + (1000*i);
				}
			}
			messageLayerNum = newCount;
			message_fore = newLayers_fore;
			message_back = newLayers_back;

			setLayerOrder();
		}
	}

	/**
	 * 表示の設定
	 */
	private void setDrawRectDst()
	{
		if(drawRectDst == null)
			drawRectDst = new Rect();
		if(Util.fullscreen)
			drawRectDst.set(Util.basePointX, Util.basePointY, Util.drawAreaRight, Util.drawAreaBottom);
		else
			drawRectDst.set(Util.basePointX2, Util.basePointY2, Util.drawAreaRight2, Util.drawAreaBottom2);
	}

	/**
	 * フルスクリーン
	 * @param fullscreen フルスクリーンにするか
	 */
	public void setFullScreen(boolean fullscreen)
	{
		if(Util.fullscreen != fullscreen)
		{
			Util.fullscreen = fullscreen;
			setDrawRectDst();
		}
	}

	/**
	 * ゲームの保存　バッファに保存する
	 */
	public void saveGame_toBuffer()
	{
		// セーブ可能ラベルを通過したので、セーブ＆ロードを有効に
		if(enableStore)
		{
			enableSave = true;
			enableLoad = true;
		}
		// 状態と変数をセーブ
		saveBuffer = saveGame_toJSON();
		try
		{
			saveBuffer.put("variable", scripter.saveVariable("f"));	// ゲーム変数
		} catch (JSONException e) { e.printStackTrace(); }
		// 保持
		saveBuffer_text = conductor.getLabelText();
		// オートセーブ
		if(enableAutoSave)
		{
			Util.log("オートセーブ");
			autoSaveBuffer = saveBuffer;
		}
	}

	/**
	 * ゲームの保存　変数は保存しない　gotostartで使う
	 */
	public void saveGame_gotostart()
	{
		// 状態のみセーブ
		JSONObject data = saveGame_toJSON();
		if(data != null)
		{
			try
			{
				// 日付追加
				data.put("date", "--/--/-- --:--:--\n");
				// ファイルに保存
				Util.str2file(context, data.toString(1), Util.getSaveDataFileNameAtNum(999));
			} catch (JSONException e) { e.printStackTrace(); }
		}
	}

	/**
	 * ゲームのメモリへの保存
	 * @param place
	 */
	public void saveGame_toTmp(int place)
	{
		JSONObject save = saveGame_toJSON();
		String date = Util.getDate();
		try
		{
			save.put("date", date);	// 日付追加
		} catch (JSONException e) { e.printStackTrace(); }

		tmpSave.put(place, save);
	}

	/**
	 * ゲームの保存　バッファのデータをファイルに保存
	 * @param saveFileNum セーブ番号
	 */
	public void saveGame(int saveFileNum)
	{
		saveGame(saveFileNum, saveBuffer);
	}

	/**
	 * ゲームの保存　指定データを保存
	 * @param saveFileNum セーブ番号 マイナスでオートセーブ
	 * @param data セーブデータ
	 */
	public void saveGame(int saveFileNum, JSONObject data)
	{
		if(!enableSave || !enableStore || data == null || data.length() == 0)
			return;

		String saveFileName;
		saveFileName = Util.getSaveDataFileNameAtNum(saveFileNum);

		Util.log("セーブします：" + saveFileName);

		// 日付を追加して保存
		String date = Util.getDate();
		try
		{
			data.put("date", date);
			Util.str2file(context, data.toString(1), saveFileName);
		} catch (JSONException e) { e.printStackTrace(); }
		Util.latestSaveDate = date;
		Util.latestSaveText = saveBuffer_text;

		// サムネイルの保存
		if(Util.saveThumbnail)
			Util.saveBitmap(thumbnailBitmap, Util.thumbnailPrefix + saveFileNum + ".png");
	}

	/**
	 * ゲーム保存の実際の処理
	 * @return 保存データ
	 */
	public JSONObject saveGame_toJSON()
	{
		JSONObject json = new JSONObject();
		JSONArray array;
		try
		{
			// シナリオの状態
			json.put("labeltext", conductor.getLabelText());
			json.put("labelname", conductor.getLabelName());
			json.put("filename", conductor.getFileName());
			// セーブの可不可
			json.put("enablesave", enableSave);
			json.put("enableload", enableLoad);
			// 現在の場所が既読か未読か
			json.put("alreadyread", conductor.isAlreadyRead());	// これいらないかも
			// テキスト関連
			json.put("flagnowait", flag_nowait);
			json.put("userdelay", userDelay);
			// ヒストリー
			json.put("historyenabled", history.enabled);
			json.put("historyoutput", history.output);
			// 音関連
			json.put("sebuffnum", seBuffNum);
			array = new JSONArray();
			for(int i = 0; i < seBuffNum; i++)
				array.put(seBuff[i].save());
			json.put("se", array);
			json.put("bgm", bgmBuff.save());
			// レイヤの数
			json.put("layernum", layerNum);
			json.put("messagenum", messageLayerNum);
			// レイヤの状態
			json.put("current", currentMessageNum);
			json.put("currentpage", currentMessagePage);
			json.put("basefore", baseLayer_fore.saveLayer());
			json.put("baseback", baseLayer_back.saveLayer());

			array = new JSONArray();
			for(int i = 0; i < layerNum; i++)
				array.put(layer_fore[i].saveLayer());
			json.put("layerfore", array);

			array = new JSONArray();
			for(int i = 0; i < layerNum; i++)
				array.put(layer_back[i].saveLayer());
			json.put("layerback", array);

			array = new JSONArray();
			for(int i = 0; i < messageLayerNum; i++)
				array.put(message_fore[i].saveLayer());
			json.put("messagefore", array);
			array = new JSONArray();

			for(int i = 0; i < messageLayerNum; i++)
				array.put(message_back[i].saveLayer());
			json.put("messageback", array);

			// 右クリックサブルーチン関連
			json.put("rclickJump", menuClickJump);
			json.put("rclickCall", menuClickCall);
			json.put("rclickEnabled", menuClickEnabled);
			json.put("rclickStorage", menuClickStorage);
			json.put("rclickTarget", menuClickTarget);

			// リンクのロック
			json.put("locklink", locklink);

			// マクロ
			if(Util.saveMacros)
				conductor.storeMacro(json);

			// プラグインのセーブ
			int size = kasPlugins.size();
			for(int i = 0; i < size; i++)
				kasPlugins.get(i).onStore(json);
		}
		catch (JSONException e) { e.printStackTrace(); }
		return json;
	}

	/**
	 * ゲームの読み込み　通常の読み込み
	 * @param saveFileNum セーブファイル番号
	 */
	public void loadGame(int saveFileNum)
	{
		if(!enableLoad || !enableStore)
			return;

		if(!Util.saveDataExistence(saveFileNum))
			return;
		String saveFileName = Util.getSaveDataFileNameAtNum(saveFileNum);


		// 読み込み
		String buff = Util.file2str(context, saveFileName);
		if(buff == null)	// ファイル読み込み失敗
			return;

		// 適用
		shutdownSurfaceDraw();
		JSONObject data;
		try
		{
			data = new JSONObject(buff);
			loadGame_fromJSON(data, true, true, false, true);
		} catch (JSONException e) { e.printStackTrace(); }
		startSurfaceDraw();
	}

	/**
	 * ゲームの読み込み
	 * @param data 保存データ
	 * @param se 効果音を読み込むかどうか
	 * @param bgm BGMを読み込むかどうか
	 * @param backlay 保存データの表ページを裏ページに復元するかどうか
	 * @param clear メッセージレイヤをクリアするかどうか
	 */
	synchronized public void loadGame_fromJSON(JSONObject data, boolean se, boolean bgm, boolean backlay, boolean clear)
	{
		if(data == null)
			return;

		yesNoDialog.visible = false;	// ダイアログ消去

		JSONArray array;
		int len;
		long nowTime = conductor.nowTime;
		try
		{
			// シナリオの状態
			if(!backlay)
			{
				data.get("labeltext");
				String filename = data.getString("filename");
				String labelname = data.getString("labelname");
				conductor.reset();				// コンダクタリセット
				conductor.jumpScenario(filename, labelname); // ジャンプ
			}
			// セーブの可不可
			enableSave = data.getBoolean("enablesave");
			enableLoad = data.getBoolean("enableload");
			// テキスト関連
			flag_nowait = data.getBoolean("flagnowait");
			userDelay = data.getBoolean("userdelay");
			// 履歴
			if(!backlay)
			{
				history.enabled = data.getBoolean("historyenabled");
				history.output = data.getBoolean("historyoutput");
			}
			// 音
			if(se)
			{
				for(int j = 0; j < seBuff.length; j++) // 止める
					seBuff[j].stopSound();
				changeSeBufferNum(data.getInt("sebuffnum"));
				array = data.getJSONArray("se");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						seBuff[i].load(context, array.getJSONObject(i));
				}
			}
			if(bgm)
			{
				bgmBuff.stopSound();
				bgmBuff.load(context, data.getJSONObject("bgm"));
			}
			// レイヤの数
			changeLayerCount(data.getInt("layernum"));
			changeMessageLayerCount(data.getInt("messagenum"));
			changeCurrent(data.getInt("current"), data.getString("currentpage"));
			if(backlay) // セーブデータの表を、裏にロード
			{
				baseLayer_back.loadLayer(context, data.getJSONObject("basefore"), nowTime);
				array = data.getJSONArray("layerfore");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						layer_back[i].loadLayer(context, array.getJSONObject(i), nowTime);
				}
				array = data.getJSONArray("messagefore");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						message_back[i].loadLayer(context, array.getJSONObject(i), nowTime);
				}
			}
			else	// 普通にロード
			{
				Util.log("背景レイヤ");
				baseLayer_fore.loadLayer(context, data.getJSONObject("basefore"), nowTime);
				baseLayer_back.loadLayer(context, data.getJSONObject("baseback"), nowTime);
				array = data.getJSONArray("layerfore");
				Util.log("前景レイヤfore");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						layer_fore[i].loadLayer(context, array.getJSONObject(i), nowTime);
				}
				array = data.getJSONArray("layerback");
				Util.log("前景レイヤback");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						layer_back[i].loadLayer(context, array.getJSONObject(i), nowTime);
				}
				array = data.getJSONArray("messagefore");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						message_fore[i].loadLayer(context, array.getJSONObject(i), nowTime);
				}
				array = data.getJSONArray("messageback");
				if(array != null)
				{
					len = array.length();
					for(int i = 0; i < len; i++)
						message_back[i].loadLayer(context, array.getJSONObject(i), nowTime);
				}
			}
			// 変数
			if(!backlay)
			{
				if(data.has("variable"))
					scripter.loadVariable("f", data.getString("variable"));
			}

			// 右クリックサブルーチン関連
			if(!backlay)
			{
				if(data.has("rclickJump"))
				{
					menuClickJump = data.getBoolean("rclickJump");
					menuClickCall = data.getBoolean("rclickCall");
					menuClickEnabled = data.getBoolean("rclickEnabled");
					menuClickStorage = data.getString("rclickStorage");
					menuClickTarget = data.getString("rclickTarget");
				}
			}
			// リンクのロック状態
			if(!backlay)
			{
				if(data.has("locklink"))
					locklink = data.getBoolean("locklink");
			}

			// マクロ
			conductor.restoreMacro(data);

			// プラグインのロード
			int size = kasPlugins.size();
			for(int i = 0; i < size; i++)
				kasPlugins.get(i).onRestore(data, clear, backlay);


			// clear == true ならメッセージレイヤの初期化
			if(clear)
			{
				for(int j = 0; j < messageLayerNum; j++)
				{
					message_fore[j].clearMessage();
					message_fore[j].glyphOff();
					message_back[j].clearMessage();
					message_back[j].glyphOff();
				}
			}

			// 開始
			if(!backlay)
			{
				endSkip();						// スキップを停止
				changeState(GAMESTATE.DEFAULT);	// 状態を変更
				setMenuEnabled(false);			// 各種メニューを選択不能に
				conductor.startConductor();		// コンダクタ開始
				saveGame_toBuffer();			// ロード直後の状態を保持
			}

		} catch (JSONException e) {	e.printStackTrace(); }

	}

	/**
	 * 最初に戻る
	 */
	public void gotostart()
	{
		loadGame(999);
	}

	/**
	 * システム変数に保持していた値の読み出し
	 */
	public void loadSystemStateFromSf()
	{
		HashMap<String, MokaValue> dic = scripter.moka_kas.dic;
		if(dic == null) return;

		MokaValue value;

		// 画質
		value = dic.get("GRAPHICSCALE");
		if(value != null && value.cast(MOKA_VALUE_TYPE.REAL))
			layerImageScale = (float)value.realValue;

		Util.imageScale = layerImageScale;
		Layer.scale = layerImageScale;

		// BGM 大域音量
		value = dic.get("BGMGVOLUME");
		if(value != null && value.cast(MOKA_VALUE_TYPE.REAL))
			bgmBuff.setGlobalVolume((float)value.realValue);

		// SE 大域音量
		value = dic.get("SEGVOLUME");
		if(value != null && value.type == MOKA_VALUE_TYPE.ARRAY)
		{
			int size = value.array.size();
			MokaValue vol;
			for(int i = 0; i < size && i < seBuffNum; i++)
			{
				vol = value.array.get(i);
				if(vol != null && vol.cast(MOKA_VALUE_TYPE.REAL))
					seBuff[i].setGlobalVolume((float)vol.realValue);
			}
		}

		// 文字速度
		value = dic.get("TEXTINTERVAL");
		if(value != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			setUserTextInterval((int)value.intValue);

		// 全文スキップ
		value = dic.get("SKIPMODETYPE");
		if(value != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			skipModeType = (int)value.intValue;

		// オート速度
		value = dic.get("AUTOINTERVAL");
		if(value != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			autoModeInterval = (int)value.intValue;

		// 簡易演出
		value = dic.get("EASYDIRECTION");
		if(value != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
		{
			if(value.intValue == 0)
				easyDirection = false;
			else
				easyDirection = true;
		}

		// フルスクリーン
		value = dic.get("FULLSCREEN");
		if(value != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
		{
			if(value.intValue == 0)
				setFullScreen(false);
			else
				setFullScreen(true);
		}
	}

	/**
	 * システム変数に値を保持
	 */
	public void saveSystemStateToVariable()
	{
		MokaValue kas = scripter.moka_kas;
		if(kas == null) return;

		MokaValue value = new MokaValue();

		// バージョン
		value.assign(Util.gameVersion);
		kas.addDicValue2("VERSION", value);
		// ゲームタイトル
		value.assign(Util.gameTitle);
		kas.addDicValue2("GAMETITLE", value);
		// 画像読み込みサイズ
		value.assign(layerImageScale);
		kas.addDicValue2("GRAPHICSCALE", value);
		// BGM 大域音量
		value.assign(getBgmGlobalVol());
		kas.addDicValue2("BGMGVOLUME", value);
		// SE 大域音量
		MokaValue array = MokaValue.getNewArray();
		for(int i = 0; i < seBuffNum; i++)
		{
			value.assign(getSeGlobalVol(i));
			array.addArrayValue2(value);
		}
		value.assign(array);
		kas.addDicValue2("SEGVOLUME", value);
		// テキスト表示のインターバル
		value.assign(userTextInterval);
		kas.addDicValue2("TEXTINTERVAL", value);
		// スキップモードのタイプ
		value.assign(skipModeType);
		kas.addDicValue2("SKIPMODETYPE", value);
		// オートモードのインターバル
		value.assign(autoModeInterval);
		kas.addDicValue2("AUTOINTERVAL", value);
		// 簡易演出
		if(easyDirection) value.assign(1);
		else value.assign(0);
		kas.addDicValue2("EASYDIRECTION", value);
		// フルスクリーン
		if(Util.fullscreen) value.assign(1);
		else value.assign(0);
		kas.addDicValue2("FULLSCREEN", value);
	}

	/**
	 * メニューの有効無効
	 * @param enabled true で有効
	 */
	public void setMenuEnabled(boolean enabled)
	{
		if(!Util.builtInOptionMenu)
			return;

		if(MainActivity.menuItem == null)
		{
			// まだメニューが生成されていない場合は記憶するのみ
			if(Util.mainActivity != null)
			{
				Util.mainActivity.setOptionMenuEnabled("save", enableSave && enableStore && enabled);
				Util.mainActivity.setOptionMenuEnabled("load", enableLoad && enableStore && enabled);
				Util.mainActivity.setOptionMenuEnabled("history", history.enabled && enabled);
				Util.mainActivity.setOptionMenuEnabled("hide", enabled);
				Util.mainActivity.setOptionMenuEnabled("auto", enabled);
				Util.mainActivity.setOptionMenuEnabled("skip", enabled);
				Util.mainActivity.setOptionMenuEnabled("config", enabled);
				Util.mainActivity.setOptionMenuEnabled("title", enabled);
			}
		}
		else
		{
			// 既に生成されている場合は直接操作する
			menuItemsEnabledBuff = enabled;
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					if(MainActivity.menuItem != null)
					{
						MenuItem menu;

						menu = MainActivity.menuItem.get("save");
						if(menu != null)
							menu.setEnabled(enableSave && enableStore && menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("load");
						if(menu != null)
							menu.setEnabled(enableLoad && enableStore && menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("history");
						if(menu != null)
							menu.setEnabled(history.enabled && menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("hide");
						if(menu != null)
							menu.setEnabled(menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("auto");
						if(menu != null)
							menu.setEnabled(menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("skip");
						if(menu != null)
							menu.setEnabled(menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("config");
						if(menu != null)
							menu.setEnabled(menuItemsEnabledBuff);

						menu = MainActivity.menuItem.get("title");
						if(menu != null)
							menu.setEnabled(menuItemsEnabledBuff);
					}
				}
			};
			Util.doWithHandler(runnable);
		}
	}

	/**
	 * タグハンドラ タグの仕分けとか実行とか
	 * @param tagName タグ名
	 * @param elm 属性
	 */
	public void tagHandlers(String tagName, HashMap<String, String> elm)
	{
		TagObject obj = tagHandlers.getTagObject(tagName);
		if(obj != null) // タグ
		{
			Util.log("TAG:" + tagName);
			if(tagCond(elm))
			{
				entity(elm);	// エンティティを適用
				obj.run(elm);
			}
		}
		else if(conductor.containsMacro(tagName)) // マクロ
		{
			if(tagCond(elm))
			{
				entity(elm);	// エンティティを適用
				addMacroElm(tagName, elm);	// マクロの属性を mp にセット
				conductor.conductMacro(tagName);
			}
		}
		else // 存在しないタグ
		{
			if(Util.notExistTag)
				Util.error("存在しないタグです:" + tagName);
		}
	}

	/**
	 * エンティティを適用
	 * @param elm 属性
	 */
	private void entity(HashMap<String, String> elm)
	{
		String key;
		String value;
		String script;
		String emb;
		for(Map.Entry<String, String> entry : elm.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			if(value != null && value.length() >= 2 && value.charAt(0) == '&')
			{
				script = value.substring(1);
				emb = scripter.emb(script);
				elm.put(key, emb);
			}
		}
	}

	/**
	 * マクロ呼び出し時の属性を設定
	 * @param elm
	 */
	private void addMacroElm(String name, HashMap<String, String> elm)
	{
		String key;
		String value;
		scripter.eval("__kas__.macro[__kas__.macroCount++] = mp = %[];");
		for(Map.Entry<String, String> entry : elm.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			scripter.eval("mp[\"" + key + "\"] = \"" + value + "\";");	// mp に追加
		}
	}

	/**
	 * cond 属性の処理
	 * @param elm 属性
	 * @return true で実行してよい
	 */
	public boolean tagCond(HashMap<String, String> elm)
	{
		String cond = elm.get("cond");
		if(cond == null)
			return true;

		int result = scripter.cond(cond);
		switch(result)
		{
		case MokaScript.COND_FALSE:
			return false;
		case MokaScript.COND_TRUE:
			return true;
		default:
			String message =
				conductor.parser.fileName +
				" line:" + conductor.parser.getLineNumber() + "\n" +
				"cond属性でエラー";
			Util.error(message);
			break;
		}
		return true;
	}

	/**
	 * タグを追加する。プラグイン用
	 * @param name 名前
	 * @param tag タグ
	 */
	public void addTag(String name, TagObject tag)
	{
		tagHandlers.addTag(name, tag);
	}

	/**
	 * リンククリック時の動作
	 * @param storage ジャンプ先シナリオファイル名
	 * @param target ジャンプ先ラベル名
	 * @param countpage countpage 属性
	 * @param exp 式
	 */
	public void clickedLink(String storage, String target, boolean countpage, String exp)
	{
		locklink = true;	// リンクをロック
		if(exp != null)
			scripter.eval(exp);	// 式を実行
		if(storage != null || target != null)
		{
			conductor.jumpScenario(storage, target, countpage);
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
		}
	}

	/**
	 * SEバッファの数を変更
	 * @param newCount 変更後の数
	 */
	public void changeSeBufferNum(int newCount)
	{
		if(newCount < 0)
			return;
		int oldCount = seBuffNum;
		if(newCount != oldCount)
		{
			Sound[] newSeBuff = new Sound[newCount];
			if(newCount < oldCount)	// 減る
			{
				for(int i = 0; i < newCount; i++)
					newSeBuff[i] = seBuff[i];
			}
			else	// 増える
			{
				int i;
				for(i = 0; i < oldCount; i++)
					newSeBuff[i] = seBuff[i];
				for( ; i < newCount; i++)
					newSeBuff[i] = new Sound();
			}
			seBuffNum = newCount;
			seBuff = newSeBuff;

			if(seBuffNum > 0)
				setAllSeGlobalVol(seBuff[0].gvolume);
		}
	}

	/**
	 * 状態の変更
	 * @param state 状態
	 */
	public void changeState(GAMESTATE state)
	{
		boolean oldStable = inStable();
		gameState = state;
		boolean nowStable = inStable();

		if(oldStable != nowStable) // 安定から不安定、あるいは不安定から安定に変わったとき
		{
			int size = kasPlugins.size();
			for(int i = 0; i < size; i++)
				kasPlugins.get(i).onStableStateChanged(nowStable);
		}
	}

	/**
	 * システムが安定しているかどうか
	 * @return 安定しているなら true
	 */
	public boolean inStable()
	{
		switch(gameState)
		{
//		case WAITKEY:			// キー入力待ち
//		case WAIT:				// 一定時間待っている
		case WAITKEY_LINE:		// キー入力待ち 行末
		case WAITKEY_PAGE:		// キー入力待ち 改ページ
		case STOP:				// 止まっている
			return true;
		}
		return false;
	}

	/**
	 * スキップの開始
	 */
	public void startSkip()
	{
		if(gameState == GAMESTATE.HIDEMESSAGE) endHideMessage();
		skipMode = true;
		conductor.setNowait(true);
		transition.setSkip(true);
		endAuto();
		currentMessage.glyphOff();		// グリフをオフにする
	}

	/**
	 * スキップの終了
	 */
	public void endSkip()
	{
		skipMode = false;
		conductor.setNowait(flag_nowait);
		transition.setSkip(false);
	}

	/**
	 * オートモードの開始
	 */
	public void startAuto()
	{
		if(gameState == GAMESTATE.HIDEMESSAGE) endHideMessage();
		autoMode = true;
		autoModeStartTime = conductor.nowTime;
		endSkip();
	}

	/**
	 * オートモードの終了
	 */
	public void endAuto()
	{
		autoMode = false;
	}

	/**
	 * 履歴の開始
	 */
	public void startHistory()
	{
		if(history.enabled && gameState != GAMESTATE.HISTORY)
		{
			if(gameState == GAMESTATE.HIDEMESSAGE) endHideMessage();
			history_gameStateLog = gameState;
			changeState(GAMESTATE.HISTORY);
			conductor.stopConductor();
		}
	}

	/**
	 * 履歴の終了
	 */
	public void endHistory()
	{
		changeState(history_gameStateLog);	// 元に戻す
		if(gameState != GAMESTATE.STOP &&
		   gameState != GAMESTATE.WAIT &&
		   gameState != GAMESTATE.WAITKEY_LINE &&
		   gameState != GAMESTATE.WAITKEY_PAGE)
			conductor.startConductor();			// 再開
	}

	/**
	 * メッセージ消去の開始
	 */
	public void startHideMessage()
	{
		if(gameState != GAMESTATE.HIDEMESSAGE)
		{
			if(gameState == GAMESTATE.HISTORY) endHistory();
			hidemessage_stateLog = gameState;
			changeState(GAMESTATE.HIDEMESSAGE);
			conductor.stopConductor();
			// プラグイン
			int size = kasPlugins.size();
			for(int i = 0; i < size; i++)
				kasPlugins.get(i).onMessageHiddenStateChanged(true);
		}
	}

	/**
	 * メッセージ消去の終了
	 */
	public void endHideMessage()
	{
		changeState(hidemessage_stateLog);		// 復帰
		if(!inStable())	// 停止中に消去した場合以外は、また動かす
			conductor.startConductor();
		// プラグイン
		int size = kasPlugins.size();
		for(int i = 0; i < size; i++)
			kasPlugins.get(i).onMessageHiddenStateChanged(false);
	}

	/**
	 * カレントレイヤの変更
	 * @param num 新しいカレントレイヤの番号
	 * @param page ページ
	 */
	public void changeCurrent(int num, String page)
	{
		if(page.equals("back"))
		{
			currentMessage = message_back[num];
			currentMessageNum = num;
			currentMessagePage = "back";
			currentMessageBack = message_fore[num];
		}
		else
		{
			currentMessage = message_fore[num];
			currentMessageNum = num;
			currentMessagePage = "fore";
			currentMessageBack = message_back[num];
		}
	}

	/**
	 * カレントレイヤの裏表の入れ替え
	 */
	public void switchCurrent()
	{
		if(currentMessagePage.equals("back"))
			changeCurrent(currentMessageNum, "fore");
		else
			changeCurrent(currentMessageNum, "back");
	}

	/**
	 * メッセージを追加
	 * @param message メッセージ
	 */
	public void onAddMessage(String message)
	{
		currentMessage.addMessage(message);
		if(currentMessage.withback)
			currentMessageBack.addMessage(message);

		history.addMessage(message);
		changeState(GAMESTATE.TEXT);

		if(!flag_nowait)	// 文字ウェイト
		{
			if(message.length() == 1 && autoWaitChar.containsKey(message.charAt(0))) // 自動ウェイト
				conductor.sleepConductor(autoWaitChar.get(message.charAt(0)) * getNowTextInterval());
			else
				conductor.sleepConductor(getNowTextInterval());	// 待つ
		}
	}

	/**
	 * 処理一周ごとに一度呼ばれるメソッド
	 */
	synchronized public void loopProcess()
	{
		skip();
		auto();
		move();
		sound();
		anim();
	}

	/**
	 * アニメーション関連
	 */
	private void anim()
	{
		if(gameState == GAMESTATE.WA)
		{
			if(wa_layer == null || wa_layer.isStopedAnimation())
			{
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
				return;
			}
		}
	}

	/**
	 * 自動移動関連
	 */
	private void move()
	{
		int re = 0;
		int nomove = 0;
		int finish = 0;
		int i;
		for(i = 0; i < layer_fore.length; i++)
		{
			re = layer_fore[i].onMove(conductor.nowTime);
			switch(re)
			{
			case -1: nomove++; break; // 移動していない
			case  1: finish++; break; // 移動完了
			}
		}
		for(i = 0; i < layer_back.length; i++)
		{
			re = layer_back[i].onMove(conductor.nowTime);
			switch(re)
			{
			case -1: nomove++; break; // 移動していない
			case  1: finish++; break; // 移動完了
			}
		}

		for(i = 0; i < message_fore.length; i++)
		{
			re = message_fore[i].onMove(conductor.nowTime);
			switch(re)
			{
			case -1: nomove++; break; // 移動していない
			case  1: finish++; break; // 移動完了
			}
		}
		for(i = 0; i < message_back.length; i++)
		{
			re = message_back[i].onMove(conductor.nowTime);
			switch(re)
			{
			case -1: nomove++; break; // 移動していない
			case  1: finish++; break; // 移動完了
			}
		}

		if(gameState == GAMESTATE.WM)
		{
			if(nomove >= layer_fore.length*2 + message_fore.length*2)	// 一つも動いてない
			{
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
			else if(finish != 0)	// 自動移動待ち終了
			{
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
		}
	}

	/**
	 * 音関連
	 */
	private void sound()
	{
		// BGM再生終了待ち
		if(gameState == GAMESTATE.WL && bgmBuff.playState == 0)	// 終了
		{
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
		}

		// SE再生終了待ち
		if(gameState == GAMESTATE.WS && seBuff[ws_buff].playState == 0)
		{
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
		}

		// フェード処理とフェード待ち
		boolean finish;
		finish = bgmBuff.fade(conductor.nowTime);
		if(finish && gameState == GAMESTATE.WB)	// フェード待ち終了
		{
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
		}
		for(int i = 0; i < seBuff.length; i++)
		{
			finish = seBuff[i].fade(conductor.nowTime);
			if(finish && gameState == GAMESTATE.WF && wf_buff == i)	// フェード待ち終了
			{
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
		}
	}

	/**
	 * スキップに関する処理
	 */
	public void skip()
	{
		if(skipMode)
		{
			// スキップ中は問答無用で入力イベント発生
			input();
			// 既読・未読で止める処理
			if(skipModeType == 1)
			{
				// スキップの種類 1:既読のみ 2:未読も
				if(!conductor.isAlreadyRead())	// 未読に来たら止める
				{
					Util.log("未読だったのでスキップ終了");
					endSkip();
				}
			}
		}
	}

	/**
	 * オートモードに関する処理
	 */
	public void auto()
	{
		if(autoMode) // オートモードがオン
		{
			// クリック待ちの時のみ動作
			if(gameState == GAMESTATE.WAITKEY_LINE || gameState == GAMESTATE.WAITKEY_PAGE)
			{
				if(conductor.nowTime-autoModeStartTime >= autoModeInterval)
				{
//					Util.log("auto " + autoModeInterval + " start:" + autoModeStartTime + " now:" + conductor.nowTime);
					input();	// オートクリック
					autoModeStartTime = conductor.nowTime;
				}
			}
			else	// 他の時は時間をリセット
				autoModeStartTime = conductor.nowTime;
		}
	}

	/**
	 * 画面揺れの処理　画面更新タイミングで呼ばれる
	 */
	private void quake()
	{
		if(quakeFlag)
		{
			if(quakeHmax != 0)
				Util.quakeX = ((int)(Math.random()*10000) - 5000 ) % quakeHmax;
			else
				Util.quakeX = 0;
			if(quakeVmax != 0)
				Util.quakeY = ((int)(Math.random()*10000) - 5000 ) % quakeVmax;
			else
				Util.quakeY = 0;

			if(conductor.nowTime-quakeStartTime > quakeTime)
				endQuake();
			else
			{
				if(Util.fullscreen)
				{
					drawRectDst.set(
							Util.basePointX + Util.quakeX,
							Util.basePointY + Util.quakeY,
							Util.drawAreaRight + Util.quakeX,
							Util.drawAreaBottom + Util.quakeY);
				}
				else	// フルスクリーンでない場合
				{
					drawRectDst.set(
							Util.basePointX2 + Util.quakeX,
							Util.basePointY2 + Util.quakeY,
							Util.drawAreaRight2 + Util.quakeX,
							Util.drawAreaBottom2 + Util.quakeY);
				}
			}
		}
	}

	/**
	 * 画面揺れの終了
	 */
	private void endQuake()
	{
		quakeFlag = false;
		Util.quakeX = 0;
		Util.quakeY = 0;
		setDrawRectDst();	// 位置を戻す
		if(gameState == GAMESTATE.WQ)
		{
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
		}
	}

	/** fps用 */
	private int framesInSecond = 0;
	/** fps用 */
	private long fpsCountStartTime = 0;
	/** fps用 */
	private int fps = 0;
	/** fps用 */
	private Paint fpsPaint = null;
	/**
	 * fps表示
	 * @param c
	 */
	private void fps(Canvas c)
	{
		if(Util.debug)
		{
			long nowTime = Util.getNowTime();
			long diff = nowTime - fpsCountStartTime;
			if(diff >= 1000)
			{
				fps = framesInSecond;
				framesInSecond = 0;
				fpsCountStartTime = nowTime;
			}
			framesInSecond++;
			if(fpsPaint == null)
			{
				fpsPaint = new Paint();
				fpsPaint.setTextSize(20);
				fpsPaint.setColor(Color.WHITE);
				fpsPaint.setAntiAlias(true);
				fpsPaint.setStyle(Style.FILL);
			}
			fpsPaint.setColor(Color.BLACK);
			fpsPaint.setStyle(Style.STROKE);
			fpsPaint.setStrokeWidth(2);
			c.drawText("fps:" + fps + " " + gameState, 0, 20, fpsPaint);
			fpsPaint.setColor(Color.WHITE);
			fpsPaint.setStyle(Style.FILL);
			fpsPaint.setStrokeWidth(1);
			c.drawText("fps:" + fps + " " + gameState, 0, 20, fpsPaint);
		}
	}

	/**
	 * 描画
	 */
	synchronized public void draw()
	{
		// 既に終了処理中なら描画しない
		if(inTheEnd) return;

		canvas = holder.lockCanvas();	// ダブルバッファリング
		if(canvas != null)
		{
			quake();	// 画面揺れの処理

			canvas.drawColor(Color.BLACK);	// 黒で初期化
			drawCanvas.drawColor(Color.BLACK);
			switch(gameState)
			{
			case TRANS:
				transDraw(drawCanvas);
				break;
			case HISTORY:
				drawLayers(drawCanvas, baseLayer_fore, layerOrder_fore);
				history.onDraw(drawCanvas);
				break;
			default:
				drawLayers(drawCanvas, baseLayer_fore, layerOrder_fore);
				break;
			}

			// yesNoダイアログ
			yesNoDialog.onDraw(drawCanvas);

			fps(drawCanvas);

			if(Util.saveThumbnail && !lockThumbnail)	// サムネイル
				thumbnailCanvas.drawBitmap(drawBitmap, drawRectSrc, thumbnailRect, null);

			canvas.drawBitmap(drawBitmap, drawRectSrc, drawRectDst, null);
			holder.unlockCanvasAndPost(canvas); // 描画
		}
	}

	/**
	 * トランジション中の描画
	 * @param c キャンバス
	 */
	public void transDraw(Canvas c)
	{
		if(transition.onDraw(c, conductor.nowTime))
		{
			// トランジション終了
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
			// 全画面入れ替えならプラグインに教える
			if(transition.isAllLayerTrans())
			{
				int size = kasPlugins.size();
				for(int i = 0; i < size; i++)
					kasPlugins.get(i).onExchangeForeBack();
			}
		}
	}

	/**
	 * レイヤの描画
	 * @param c キャンバス
	 * @param base 背景レイヤ
	 * @param orderArray レイヤ配列
	 */
	public void drawLayers(Canvas c, KASLayer base, KASLayer[] orderArray)
	{
		long nowTime = conductor.nowTime;
		KASLayer layer;

		base.onDraw(context, c, nowTime);

		for(int i = 0; i < orderArray.length; i++)
		{
			layer = orderArray[i];
			if(layer.visible)
			{
				switch(layer.layerType)
				{
				case 'L':	// 前景レイヤ
					if( !(gameState == GAMESTATE.HIDEMESSAGE && ((Layer)layer).autohide) )
						layer.onDraw(context, c, nowTime);
					break;
				case 'M':	// メッセージレイヤ
					if(gameState != GAMESTATE.HIDEMESSAGE)
						layer.onDraw(context, c, nowTime);
					break;
				default:	// その他のレイヤ（プラグインレイヤ）
					layer.onDraw(context, c, nowTime);
					break;
				}
			}
		}
	}

	/**
	 * タッチイベント
	 * コンダクタで一回のループに一度呼ばれる。
	 * @param x x 座標
	 * @param y y 座標
	 * @param action イベントの種類
	 */
	public void onTouch(int x, int y, int action)
	{
		if(gameState == GAMESTATE.HISTORY)	// 履歴表示中はhistoryに直接渡す
			history.onTouchEvent(x, y, action);
		else
		{
			// 各レイヤに伝える
			// KAS の locklink はリンクへのイベント伝達そのものを殺す
			boolean result = false;
			if(!locklink)	// ロックされてなかったら
			{
				for(int i = 0; i < layerOrder_fore.length; i++)
				{
					if(layerOrder_fore[i].visible)
					{
						result = layerOrder_fore[i].onTouchEvent(x, y, action, context);
						if(result) return; // 処理がなされた
					}
				}
			}

			// プラグインにも教える
			// プラグインの場合は locklink の影響を受けない
			int size = kasPlugins.size();
			for(int i = 0; i < size; i++)
			{
				result = kasPlugins.get(i).onTouchEvent(x, y, action);
				if(result) return;
			}

			// レイヤでイベントが起きなかった場合は、各イベントを発生させる
			switch(action)
			{
			case MotionEvent.ACTION_DOWN: onTouchDown(x, y); break;
			case MotionEvent.ACTION_UP: onTouchUp(x, y); break;
			case MotionEvent.ACTION_MOVE: onTouchMove(x, y); break;
			}
		}
	}

	/**
	 * onTouchDown イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public void onTouchDown(int x, int y)
	{
		if(ignoreInputOnce)	// 一度だけ無視
		{
			ignoreInputOnce = false;
			return;
		}

		if(yesNoDialog.visible) // ダイアログが出ているならそちらへ回す
		{
			yesNoDialog.onDown(x, y);
			return;
		}
	}

	/**
	 * onTouchUp イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public void onTouchUp(int x, int y)
	{
		if(yesNoDialog.visible)
		{
			yesNoDialog.onUp(x, y);
			return;
		}

		if(autoMode)
			endAuto();	// オートモード終了

		if(skipMode)
			endSkip();	// スキップ終了

		input();
	}

	/**
	 * onTouchMove イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public void onTouchMove(int x, int y)
	{
		if(yesNoDialog.visible)
		{
			yesNoDialog.onMove(x, y);
			return;
		}
	}

	/**
	 * メニューボタンクリック時の動作
	 */
	public void onClickMenu()
	{
		Util.log("onClickMenu stable=" + inStable() + " enabled=" + menuClickEnabled);
		if(menuClickEnabled && inStable())
		{
			if(menuClickCall)
				rclickCall(menuClickStorage, menuClickTarget);
			else if(menuClickJump)
				rclickJump(menuClickStorage, menuClickTarget);
			else
			{
				if(gameState == GAMESTATE.HIDEMESSAGE)
					endHideMessage();
				else
					startHideMessage();
			}
		}
	}

	/**
	 * 入力による処理 タッチイベントから呼ばれる
	 */
	public void input()
	{
		if(ignoreInputOnce)	// 一度だけ無視
		{
			ignoreInputOnce = false;
			return;
		}

		switch(gameState)
		{
		case TEXT:			// テキスト表示中
			if(clickskip)
			{
				touch_nowait = true;
				conductor.setNowait(true);
				conductor.startConductor();
			}
			break;
		case WAIT:			// ウェイト中
			if(waitCanskip && clickskip)
				conductor.startConductor();
			break;
		case WAITKEY:		// キー待ち
			if(autoMode || skipMode)
				return;
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
			setMenuEnabled(false);
			break;
		case WAITKEY_LINE:	// 行末待ち
			currentMessage.glyphOff();
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
			setMenuEnabled(false);
			break;
		case WAITKEY_PAGE:	// 改ページ待ち
			currentMessage.glyphOff();
			changeState(GAMESTATE.DEFAULT);
			conductor.startConductor();
			setMenuEnabled(false);
			break;
		case TRANS:			// トランジション中
			if(clickskip)
				transition.onDown();
			break;
		case HIDEMESSAGE:	// メッセージ消去中
			endHideMessage();
			break;
		case WQ:			// 画面揺れまち
			if(quake_canskip && clickskip)
			{
				endQuake();
				conductor.startConductor();
			}
			break;
		case WM:			// 自動移動待ち
			if(wm_canskip && clickskip)
			{
				tag_stopmove();
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
			break;
		case WB:			// BGMフェード待ち
			if(wb_canskip && clickskip)
			{
				bgmBuff.endFade();
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
			break;
		case WF:			// 効果音フェード待ち
			if(wf_canskip && clickskip)
			{
				seBuff[wf_buff].endFade();
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
			break;
		case WL:			// BGM再生終了待ち
			if(wl_canskip && clickskip)
			{
				bgmBuff.stopSound();
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
			break;
		case WS:			// SE再生終了待ち
			if(ws_canskip && clickskip)
			{
				seBuff[ws_buff].stopSound();
				changeState(GAMESTATE.DEFAULT);
				conductor.startConductor();
			}
			break;
		}
	}

	/**
	 * 右クリックサブルーチンのコール
	 * @param storage
	 * @param target
	 */
	public void rclickCall(String storage, String target)
	{
		setMenuEnabled(false);
		changeState(GAMESTATE.DEFAULT);
		conductor.callRclickSubroutine(storage, target);
		conductor.startConductor();	// 再開
	}

	/**
	 * 右クリックサブルーチンのジャンプ
	 * @param storage
	 * @param target
	 */
	public void rclickJump(String storage, String target)
	{
		setMenuEnabled(false);
		changeState(GAMESTATE.DEFAULT);
		conductor.jumpScenario(storage, target);
		conductor.startConductor();	// 再開
	}

	/**
	 * イベントハンドラ 画面のタッチイベント
	 * このタッチイベントはコンダクタにそのまま渡す
	 * @param event
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		conductor.onTouch(event);
		return true;
	}

	/**
	 * OutOfMemoryError時の動作 表示されていない画像をリサイクル
	 */
	public void onOutOfMemoryError()
	{
		Util.log("!onOutOfMemoryError!!!");
	}

	/**
	 * サーフェイスビューの定期的描画を開始
	 */
	public void startSurfaceDraw()
	{
		if(executor == null)
		{
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(new Runnable()
			{
				@Override
				public void run()
				{
					draw();	// 描画
				}
			}, 0, 34, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * サーフェイスビューの定期的描画を停止
	 */
	public void shutdownSurfaceDraw()
	{
		if(executor != null)
		{
			executor.shutdown();
			executor = null;
		}
	}

	/**
	 * イベントハンドラ サーフェイスの変更
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	}

	/**
	 * イベントハンドラ サーフェイスの生成
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Util.log("!onSurfaceCreated " + getWidth() + "*" + getHeight());
		// 画面サイズを正しいものに更新
		Util.setDispSize(getWidth(), getHeight());
		setDrawRectDst();
		// 描画の開始
		startSurfaceDraw();
		// ビデオを隠す
		video.setVisibleNow(false);
	}

	/**
	 * イベントハンドラ サーフェイスの破棄
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Util.log("!onSurfaceDestroyed");
		// 定期描画停止
		shutdownSurfaceDraw();
	}

	/**
	 * onStart
	 */
	public void onStart()
	{
		// ユーザーによる初期化
		if(!UsersInitialize.initialized)
		{
			UsersInitialize.init();
			UsersInitialize.initialized = true;
		}
	}

	/**
	 * onStop
	 */
	public void onStop()
	{
		shutdownSurfaceDraw();
	}

	/**
	 * onPause
	 */
	public void onPause()
	{
		startSurfaceDraw();
		conductor.onPause();
		conductor.shutdown();
		conductorThread = null;
		bgmBuff.pauseSound();
		for(int i = 0; i < seBuff.length; i++)
			seBuff[i].pauseSound();
		if(video.playing())
			video.stop();
	}

	/**
	 * onResume
	 */
	public void onResume()
	{
		if(resumeAutoSave && enableAutoResume)
		{
			// スリープからの復帰時などに、autosave を読み出す場合
			if(conductorThread != null)
			{
				conductor.shutdown();
				conductorThread = null;
			}
			conductor.stopConductor();
			conductor.onResume();
			loadGame_fromJSON(savedInstanceData, true, true, false, true);
			conductorThread = new Thread(conductor);
			conductorThread.start();
		}
		else
		{
			if(conductorThread == null)
			{
				conductorThread = new Thread(conductor);
				conductorThread.start();
				conductor.onResume();
			}
			if(resumeSound)
			{
				bgmBuff.restartSound();
				for(int i = 0; i < seBuff.length; i++)
					seBuff[i].restartSound();
			}
		}
		resumeSound = true;
		resumeAutoSave = false;
	}

	/**
	 * onSaveInstanceState
	 * @param outState
	 */
	public void onSaveInstanceState(Bundle outState)
	{
		if(!enableAutoResume || saveBuffer == null) return;
		try
		{
			if(!Util.saveMacros) // マクロがセーブデータに保存されていない場合は保存
				conductor.storeMacro(saveBuffer);
			outState.putString("tmpsavedata", saveBuffer.toString(2));
		} catch (JSONException e) { e.printStackTrace(); }
	}

	/**
	 * onRestoreInstanceState
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		if(enableAutoResume && savedInstanceState.containsKey("tmpsavedata"))
		{
			try
			{
				String data = savedInstanceState.getString("tmpsavedata");
				savedInstanceData = new JSONObject(data);
				resumeAutoSave = true;
			} catch (JSONException e) { e.printStackTrace(); }
		}
	}

	/**
	 * ゲームが終わるときにMainActivityから呼ばれる
	 */
	public void onEndGame()
	{
		Util.log("!onEndGame  KAS finished.");
		// 動画停止
		video.shutdown();
		// 停止
		conductor.shutdown();
		shutdownSurfaceDraw();
		video.stop();
		// 自動セーブ
		if(enableAutoSave && autoSaveBuffer != null)
			saveGame(-1, autoSaveBuffer);
		// プラグインなどを再度初期化
		UsersInitialize.initialized = false;
		// 音楽停止
		bgmBuff.stopSound();
		for(int i = 0; i < seBuff.length; i++)
			seBuff[i].stopSound();
		// システム変数セーブ
		saveSystemStateToVariable();
		scripter.saveVariable2File(context, "sf", MokaScript.sfVariableFileName);
		// KAS 変数セーブ
		scripter.saveVariable2File(context, "__kas__", MokaScript.kasVariableFileName);
		// 通過記録保存
		conductor.saveTrailLabelList();
		// リソースの解放
		ResourceManager.free();
		// Moka スクリプトの解放
		Util.freeMokaScript();
	}

	/**
	 * 表示非表示の設定
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		Runnable runnable;
		if(visible)
		{
			runnable = new Runnable(){
				@Override
				public void run(){
					setVisibleNow(true);
				}
			};
		}
		else
		{
			runnable = new Runnable(){
				@Override
				public void run(){
					setVisibleNow(false);
				}
			};
		}
		Util.doWithHandler(runnable);
	}

	/**
	 * 表示非表示の設定 ただちに行う
	 * @param visible
	 */
	public void setVisibleNow(boolean visible)
	{
		if(visible)
		{
			layout.setVisibility(View.VISIBLE);
			setVisibility(View.VISIBLE);
		}
		else
		{
			layout.setVisibility(View.INVISIBLE);
			setVisibility(View.INVISIBLE);
		}
	}

	// ゲッター・セッター
	public void stopConductor()	// コンダクタの停止
	{
		conductor.stopConductor();
	}
	public void startConductor()	// コンダクタの開始
	{
		conductor.startConductor();
	}

	public int getUserTextInterval()	// ユーザ設定のインターバル
	{
		return this.userTextInterval;
	}
	public int getNowTextInterval()
	{
		if(userDelay)
			return this.userTextInterval;
		else
			return this.textInterval;
	}
	public void setTextInterval(int textInterval)
	{
		this.textInterval = textInterval;
		if(textInterval == 0)
			conductor.setTextZeroNowait(true);
		else
			conductor.setTextZeroNowait(false);
	}
	public void setUserTextInterval(int textInterval)
	{
		this.userTextInterval = textInterval;
		if(userTextInterval == 0)
			conductor.setTextZeroNowait(true);
		else
			conductor.setTextZeroNowait(false);
	}
	public float getBgmVol()
	{
		return bgmBuff.getVolume();
	}
	public void setBgmVol(float vol)
	{
		bgmBuff.setVolume(vol);
	}
	public float getBgmGlobalVol()
	{
		return bgmBuff.getGlobalVolume();
	}
	public void setBgmGlobalVol(float vol)
	{
		bgmBuff.setGlobalVolume(vol);
	}

	public float getSeVol()
	{
		return seBuff[0].getVolume();
	}
	public void setSeVol(int num, float vol)
	{
		if(num < seBuffNum)
			seBuff[num].setVolume(vol);
	}
	public float getSeGlobalVol(int num)
	{
		if(num < seBuffNum)
			return seBuff[num].getGlobalVolume();
		else
			return 0;
	}
	public void setSeGlobalVol(int num, float vol)
	{
		if(num < seBuffNum)
			seBuff[num].setGlobalVolume(vol);
	}
	public void setAllSeGlobalVol(float vol)
	{
		for(int i = 0; i < seBuff.length; i++)
			seBuff[i].setGlobalVolume(vol);
	}


//-----------------------------------------------------------------------------------------------------
//	ここからタグの中身
//-----------------------------------------------------------------------------------------------------

	// pimage
	public void tag_pimage(HashMap<String, String> elm)
	{
		String storage = elm.get("storage");
		String layer = elm.get("layer");
		String page = elm.get("page");
		String _dx = elm.get("dx");
		String _dy = elm.get("dy");
		String _sx = elm.get("sx");
		String _sy = elm.get("sy");
		String _sw = elm.get("sw");
		String _sh = elm.get("sh");
		String _opacity = elm.get("opacity");

		if(storage == null || layer == null || _dx == null || _dy == null) return;

		KASLayer kl = tag_sub_getKASLayer(layer, page);
		if(kl == null || kl.layerType == 'M' || kl.layerType == 'K') return;

		int dx, dy, sx, sy, sw, sh, opacity;
		dx = tag_sub_getInteger(_dx, 0);
		dy = tag_sub_getInteger(_dy, 0);
		sx = tag_sub_getInteger(_sx, 0);
		sy = tag_sub_getInteger(_sy, 0);
		sw = tag_sub_getInteger(_sw, -1);
		sh = tag_sub_getInteger(_sh, -1);
		opacity = tag_sub_getInteger(_opacity, 255);

		((Layer)kl).loadAdditionalImage(context, storage, dx, dy, sx, sy, sw, sh, opacity);
	}

	// wa
	public void tag_wa(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		String page = elm.get("page");
		Layer l = tag_sub_getLayer(layer, page);
		if(l != null && !l.isStopedAnimation() && !l.isLoopAnimation())
		{
			changeState(GAMESTATE.WA);
			wa_layer = l;
			conductor.stopConductor();
		}
	}

	// resetfont
	public void tag_resetfont()
	{
		currentMessage.resetFont();
	}

	// resetstyle
	public void tag_resetstyle()
	{
		currentMessage.resetStyle();
	}

	// font
	public void tag_font(HashMap<String, String> elm)
	{
		if(elm != null)
			currentMessage.setFont(elm);
	}

	// style
	public void tag_style(HashMap<String, String> elm)
	{
		if(elm != null)
			currentMessage.setStyle(elm);
	}

	// autoresume : KAS オリジナル
	public void tag_autoresume(HashMap<String, String> elm)
	{
		String enabled = elm.get("enabled");
		if(enabled != null)
			enableAutoResume = tag_sub_getBoolean(enabled, false);
	}

	// locksnapshot
	public void tag_locksnapshot()
	{
		if(!lockThumbnail)
		{
			lockThumbnail = true;
			// 描く
			drawCanvas.drawColor(Color.BLACK);
			drawLayers(drawCanvas, baseLayer_fore, layerOrder_fore);
			thumbnailCanvas.drawBitmap(drawBitmap, drawRectSrc, thumbnailRect, null);
		}
	}

	// locksnapshot
	public void tag_unlocksnapshot()
	{
		lockThumbnail = false;
	}

	// startautomode : KAS オリジナル
	public void tag_startautomode()
	{
		startAuto();
	}

	// startskip : KAS オリジナル
	public void tag_startskip()
	{
		startSkip();
	}

	// rclick
	public void tag_rclick(HashMap<String, String> elm)
	{
		String call = elm.get("call");
		String jump = elm.get("jump");
		String target = elm.get("target");
		String storage = elm.get("storage");
		String enabled = elm.get("enabled");

		if(call != null && Util.isBoolean(call))
			menuClickCall = Boolean.valueOf(call);
		if(jump != null && Util.isBoolean(jump))
			menuClickJump = Boolean.valueOf(jump);
		if(target != null)
			menuClickTarget = target;
		if(storage != null)
			menuClickStorage = storage;
		if((enabled != null && Util.isBoolean(enabled)))
			menuClickEnabled = Boolean.valueOf(enabled);

		if(menuClickCall && menuClickJump)
		{
			if(call == null) menuClickCall = false;
			else menuClickJump = false;
		}
	}

	// glyph
	public void tag_glyph(HashMap<String, String> elm)
	{
		String line = elm.get("line");
		String page = elm.get("page");
		String fix = elm.get("fix");
		String left = elm.get("left");
		String top = elm.get("top");

		if(line != null)
			currentMessage.loadGlyph(context, line, "line");
		if(page != null)
			currentMessage.loadGlyph(context, line, "page");
		if(fix != null && Util.isBoolean(fix))
			currentMessage.glyphFixedPosition = Boolean.valueOf(fix);
		if(left != null && Util.isInteger(left))
			currentMessage.glyphFixedLeft = Integer.decode(left);
		if(top != null && Util.isInteger(top))
			currentMessage.glyphFixedTop = Integer.decode(top);
	}

	// animstop
	public void tag_animstop(HashMap<String, String> elm)
	{
		Layer layer = tag_sub_getLayer(elm.get("layer"), elm.get("page"));
		if(layer != null)
			layer.stopAnimation();
	}

	// animstart : KAG と大きく異なる
	public void tag_animstart(HashMap<String, String> elm)
	{
		Layer layer = tag_sub_getLayer(elm.get("layer"), elm.get("page"));
		String storage = elm.get("storage");
		boolean loop = tag_sub_getBoolean(elm.get("loop"), true);

		if(layer != null)
		{
			if(storage == null)
				layer.loadAnimationFile(context);
			else
				layer.loadAnimationFile(context, storage);
			layer.startAnimation(conductor.nowTime, loop);
		}
	}

	// autosave : KAS オリジナル
	public void tag_autosave(HashMap<String, String> elm)
	{
		String enabled = elm.get("enabled");
		if(enabled != null)
			enableAutoSave = tag_sub_getBoolean(enabled, false);
	}

	// playvideo
	public void tag_playvideo(HashMap<String, String> elm)
	{
		String storage = elm.get("storage");
		int left = tag_sub_getInteger(elm.get("left"), 0);
		int top = tag_sub_getInteger(elm.get("top"), 0);
		int width = tag_sub_getInteger(elm.get("width"), Util.standardDispWidth);
		int height = tag_sub_getInteger(elm.get("height"), Util.standardDispHeight);
		int volume = tag_sub_getInteger(elm.get("volume"), 100);
		boolean loop = tag_sub_getBoolean(elm.get("loop"), false);
		int position = tag_sub_getInteger(elm.get("position"), 0);
		boolean canskip = tag_sub_getBoolean(elm.get("canskip"), true);

		if(storage == null)
			return;

		conductor.stopConductor();

		video.setX(left);
		video.setY(top);
		video.setWidth(width);
		video.setHeight(height);
		video.setVolume(volume);
		video.canskip = canskip;
		video.setLoop(loop);
		video.setStart(position);

		video.play(storage);
	}

	// iscript スクリプトの開始ではなく、スクリプトの文字列自体を受け取る
	public void tag_iscript(HashMap<String, String> elm)
	{
		if(elm == null) return;
		String s = elm.get("script");
		if(s != null)
		{
			boolean result = scripter.eval(s);
			if(!result)
			{
				String message =
					conductor.parser.fileName +
					" line:" + elm.get("line") + "\n" +
					"iscript内部でエラー";
				Util.error(message);
			}
		}
	}

	// eval
	public void tag_eval(HashMap<String, String> elm)
	{
		if(elm == null) return;
		String exp = elm.get("exp");
		if(exp != null)
		{
			boolean result = scripter.eval(exp);
			if(!result)
			{
				String message =
					conductor.parser.fileName +
					" line:" + conductor.parser.getLineNumber() + "\n" +
					"evalのスクリプトでエラー";
				Util.error(message);
			}
		}
	}

	// emb 式の評価結果を出力
	public void tag_emb(HashMap<String, String> elm)
	{
		String exp = elm.get("exp");
		if(exp != null)
		{
			String str = scripter.emb(exp);
			onAddMessage(str);
		}
	}

	// button
	public void tag_button(HashMap<String, String> elm)
	{
		String graphic = elm.get("graphic");
		String storage = elm.get("storage");
		String target = elm.get("target");
		String exp = elm.get("exp");
//		String onenter = elm.get("onenter");
//		String onleave = elm.get("onleave");
		String clickse = elm.get("clickse");
		String clicksebuf = elm.get("clicksebuf");
		boolean countpage = tag_sub_getBoolean(elm.get("countpage"), true);
		int left = tag_sub_getInteger(elm.get("left"), 0);
		int top = tag_sub_getInteger(elm.get("top"), 0);
		int width = tag_sub_getInteger(elm.get("width"), 100);
		int height = tag_sub_getInteger(elm.get("height"), 100);

		if(graphic == null)
			return;

		Sound se;
		if(clickse != null)
			se = tag_sub_getSeBuff(clicksebuf);
		else
			se = null;

		currentMessage.addButton(
				context.getResources(), graphic, width, height,
				left, top,
				storage, target, countpage, exp,
				clickse, se);

		locklink = false;
	}

	// locate
	public void tag_locate(HashMap<String, String> elm)
	{
		String x_s = elm.get("x");
		String y_s = elm.get("y");
		Integer x = null, y = null;
		if(x_s != null && Util.isInteger(x_s))
			x = Integer.decode(x_s);
		if(y_s != null && Util.isInteger(y_s))
			y = Integer.decode(y_s);
		currentMessage.locate(x, y);
	}

	// tempload
	public void tag_tempload(HashMap<String, String> elm)
	{
		int place = tag_sub_getInteger(elm.get("place"), 0);
		if(place < 0)
			place = 0;
		JSONObject saveBuff = tmpSave.get(place);
		if(saveBuff == null)
			return;

		boolean se = tag_sub_getBoolean(elm.get("se"), true);
		boolean bgm = tag_sub_getBoolean(elm.get("bgm"), true);
		boolean backlay = tag_sub_getBoolean(elm.get("backlay"), false);

		loadGame_fromJSON(saveBuff, se, bgm, backlay, false);
	}

	// tempsave
	public void tag_tempsave(HashMap<String, String> elm)
	{
		int place = tag_sub_getInteger(elm.get("place"), 0);
		if(place < 0)
			place = 0;
		saveGame_toTmp(place);
	}

	// store
	public void tag_store(HashMap<String, String> elm)
	{
		if(!elm.containsKey("enabled"))
			return;
		enableStore = tag_sub_getBoolean(elm.get("enabled"), true);
		setMenuEnabled(enableStore);
	}

	// erasebookmark
	public void tag_erasebookmark(HashMap<String, String> elm)
	{
		String place = elm.get("place");
		int num = tag_sub_getInteger(place, 0);
		if(Util.saveDataExistence(num))
			Util.deleteSave(num);
	}

	// disablestore
	public void tag_disablestore(HashMap<String, String> elm)
	{
		boolean store = tag_sub_getBoolean(elm.get("store"), true);
		boolean restore = tag_sub_getBoolean(elm.get("restore"), true);

		if(store)
		{
			enableSave = false;
			setMenuEnabled(false);
		}
		if(restore)
		{
			enableLoad = false;
			setMenuEnabled(false);
		}
	}

	// copybookmark セーブデータのコピー
	public void tag_copybookmark(HashMap<String, String> elm)
	{
		String from = elm.get("from");
		String to = elm.get("to");

		if(from == null || to == null)
			return;
		if(!Util.isInteger(from) || !Util.isInteger(to))
			return;

		int f = Integer.decode(from);
		int t = Integer.decode(to);

		if(f < 0 || t < 0 || f == t)
			return;

		String data = Util.file2str(context, Util.getSaveDataFileNameAtNum(f));
		if(data == null)	// ファイル読み込み失敗
			return;

		Util.str2file(context, data, Util.getSaveDataFileNameAtNum(t));
	}

	// cleartmpvar : KAS オリジナル 一時変数の削除
	public void tag_cleartmpvar()
	{
		scripter.eval("tf = %[];");
	}

	// clearvar	ゲーム変数の全削除
	public void tag_clearvar()
	{
		scripter.eval("f = %[];");
	}

	// ws SEの再生終了待ち
	public void tag_ws(HashMap<String, String> elm)
	{
		String buf = elm.get("buf");
		ws_buff = tag_sub_getSeBuffNum(buf);
		if(seBuff[ws_buff].playState == 0)	// 再生してない
			return;
		if(seBuff[ws_buff].loop)	// ループ再生
			return;
		ws_canskip = tag_sub_getBoolean(elm.get("canskip"), false);
		changeState(GAMESTATE.WS);
		conductor.stopConductor();
	}

	// wl BGMの再生終了待ち
	public void tag_wl(HashMap<String, String> elm)
	{
		if(bgmBuff.playState == 0)	// 停止中
			return;
		if(bgmBuff.loop)	// ループ再生
			return;
		wl_canskip = tag_sub_getBoolean(elm.get("canskip"), false);
		changeState(GAMESTATE.WL);
		conductor.stopConductor();
	}

	// wf SEのフェード待ち
	public void tag_wf(HashMap<String, String> elm)
	{
		wf_buff = tag_sub_getSeBuffNum(elm.get("buf"));
		if(seBuff[wf_buff].flag_fade == 0)	// フェード中じゃない
			return;
		wf_canskip = tag_sub_getBoolean(elm.get("canskip"), false);
		changeState(GAMESTATE.WF);
		conductor.stopConductor();
	}

	// wb BGMのフェード待ち
	public void tag_wb(HashMap<String, String> elm)
	{
		if(bgmBuff.flag_fade == 0)	// フェード中じゃない
			return;
		wb_canskip = tag_sub_getBoolean(elm.get("canskip"), false);
		changeState(GAMESTATE.WB);
		conductor.stopConductor();
	}

	// resumebgm BGMの再生再開
	public void tag_resumebgm()
	{
		bgmBuff.restartSound();
	}

	// pausebgm BGMの一時停止
	public void tag_pausebgm()
	{
		bgmBuff.pauseSound();
	}

	// fadese SEのフェード
	public void tag_fadese(HashMap<String, String> elm)
	{
		String volume = elm.get("volume");
		String time = elm.get("time");
		Sound s = tag_sub_getSeBuff(elm.get("buf"));
		if(volume != null && time != null && Util.isInteger(volume) && Util.isInteger(time))
			s.startFade(Integer.decode(time), Float.valueOf(volume), conductor.nowTime);
	}

	// fadebgm BGMのフェード
	public void tag_fadebgm(HashMap<String, String> elm)
	{
		String volume = elm.get("volume");
		String time = elm.get("time");
		if(volume != null && time != null && Util.isInteger(volume) && Util.isInteger(time))
			bgmBuff.startFade(Integer.decode(time), Float.valueOf(volume), conductor.nowTime);
	}

	// wm 自動移動の終了待ち
	public void tag_wm(HashMap<String, String> elm)
	{
		wm_canskip = tag_sub_getBoolean(elm.get("canskip"), true);
		changeState(GAMESTATE.WM);
		conductor.stopConductor();
	}

	// stoptrans トランジションの強制終了
	public void tag_stoptrans()
	{
		transition.stopTrans();
	}

	// stopmove 自動移動の強制終了
	public void tag_stopmove()
	{
		for(int i = 0; i < layer_fore.length; i++)
		{
			layer_fore[i].stopMove();
			layer_back[i].stopMove();
		}
		for(int i = 0; i < message_fore.length; i++)
		{
			message_fore[i].stopMove();
			message_back[i].stopMove();
		}
	}

	// freeimage レイヤ画像の解放
	public void tag_freeimage(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		String page = elm.get("page");

		if(layer == null)
			return;

		Layer l;
		if(layer.equals("base"))
		{
			l = tag_sub_getBaceLayer(page);
			if(l != null)
				l.freeImage();
		}
		else if(Util.isInteger(layer))
		{
			l = tag_sub_getLayer(layer, page);
			if(l != null)
				l.freeImage();
		}
	}

	// copylay レイヤ情報のコピー
	public void tag_copylay(HashMap<String, String> elm)
	{
		String srclayer = elm.get("srclayer");
		String destlayer = elm.get("destlayer");
		String srcpage = elm.get("srcpage");
		String destpage = elm.get("destpage");

		if(srclayer == null || destlayer == null)
			return;

		KASLayer slay = tag_sub_getKASLayer(srclayer, srcpage);
		KASLayer dlay = tag_sub_getKASLayer(destlayer, destpage);

		if(slay != null && dlay != null && slay.layerType == dlay.layerType)
		{
			dlay.copyLayer(context, slay, conductor.nowTime);
			setLayerOrder();
		}
	}

	// delay テキスト表示のウェイト
	public void tag_delay(HashMap<String, String> elm)
	{
		String speed = elm.get("speed");
		if(speed != null)
		{
			if(speed.equals("user"))
			{
				userDelay = true;
				setUserTextInterval(userTextInterval);	// nowait関係で一度呼んでおく
			}
			else if(speed.equals("nowait"))
			{
				userDelay = false;
				setTextInterval(0);
			}
			else if(Util.isInteger(speed))
			{
				userDelay = false;
				setTextInterval(Integer.decode(speed));
			}
		}
	}

	// ch テキストを表示
	public void tag_ch(HashMap<String, String> elm)
	{
		String text = elm.get("text");
		if(text != null)
			onAddMessage(text);
	}

	// cancelskip
	public void tag_cancelskip()
	{
		endSkip();
	}

	// cancelautomode
	public void tag_cancelautomode()
	{
		endAuto();
	}

	// erasemacro
	public void tag_erasemacro(HashMap<String, String> elm)
	{
		String name = elm.get("name");
		if(name != null)
			conductor.removeMacro(name);
	}

	// close
	public void tag_close(HashMap<String, String> elm)
	{
		boolean ask = tag_sub_getBoolean(elm.get("ask"), true);
		if(ask)
		{
			ignoreInputOnce = true;
			conductor.stopConductor();
			yesNoDialog.ask("終了しますか？", closeFunc, endYesNoFunc);
		}
		else
			Util.closeGame();
	}

	// clickskip
	public void tag_clickskip(HashMap<String, String> elm)
	{
		String enabled = elm.get("enabled");
		if(enabled == null)
			return;
		if(enabled.equals("true"))
			clickskip = true;
		else if(enabled.equals("false"))
			clickskip = false;
	}

	// clearsysvar
	public void tag_clearsysvar()
	{
		scripter.eval("sf = %[];");
	}

	// autowc
	public void tag_autowc(HashMap<String, String> elm)
	{
		String enabled = elm.get("enabled");
		String ch = elm.get("ch");
		String time_s = elm.get("time");

		if(enabled == null)
			return;

		int[] times = new int[ch.length()];
		for(int i = 0; i < times.length; i++)
			times[i] = 4;

		if(time_s != null)
		{
			String[] times_s = time_s.split(",");
			for(int i = 0; i < times_s.length; i++)
			{
				if(Util.isInteger(times_s[i]))
					times[i] = Integer.decode(times_s[i]);
			}
		}

		int len = ch.length();
		if(enabled.equals("true"))	// 有効化
		{
			for(int i = 0; i < len; i++)
				autoWaitChar.put(ch.charAt(i), times[i]);
		}
		else if(enabled.equals("false"))	// 無効化
		{
			for(int i = 0; i < len; i++)
			{
				if(autoWaitChar.containsKey(ch.charAt(i)))
					autoWaitChar.remove(ch.charAt(i));
			}
		}
	}

	// title
	public void tag_title(HashMap<String, String> elm)
	{
		String name = elm.get("name");
		if(name != null)
			Util.setGameTitle(name);
	}

	// waitclick
	public void tag_waitclick()
	{
		if(touch_nowait)	// タッチ操作によるnowaitを終了
		{
			conductor.setNowait(flag_nowait);
			touch_nowait = false;
		}
		changeState(GAMESTATE.WAITKEY);
		conductor.stopConductor();
		setMenuEnabled(true);
	}

	// wc
	public void tag_wc(HashMap<String, String> elm)
	{
		if(textInterval == 0)
			return;

		String time_s = elm.get("time");

		if(time_s == null)
			return;
		if(Util.isInteger(time_s))
		{
			gameState = GAMESTATE.WAIT;		// 状態を待ち状態に
			conductor.sleepConductor(Integer.decode(time_s) * textInterval);	// コンダクタを一定時間停止する
		}
	}

	// hidemessage メッセージを消す
	public void tag_hidemessage()
	{
		startHideMessage();
	}

	// indent
	public void tag_indent()
	{
		currentMessage.setIndent();
	}

	// endindent
	public void tag_endindent()
	{
		currentMessage.endIndent();
	}

	// er カレントメッセージレイヤのクリア
	public void tag_er()
	{
		currentMessage.clearMessage();
	}

	// ct 全メッセージレイヤのクリア、カレントをmessage0 foreに
	public void tag_ct()
	{
		for(int i = 0; i < messageLayerNum; i++)
		{
			message_fore[i].clearMessage();
			message_back[i].clearMessage();
		}
		changeCurrent(0, "fore");
	}

	// cm 全メッセージレイヤのクリア
	public void tag_cm()
	{
		for(int i = 0; i < messageLayerNum; i++)
		{
			message_fore[i].clearMessage();
			message_back[i].clearMessage();
		}
	}

	// nowait ノーウェイト
	public void tag_nowait()
	{
		conductor.setNowait(true);
		flag_nowait = true;
	}

	// endnowait ノーウェイト終了
	public void tag_endnowait()
	{
		conductor.setNowait(false);
		flag_nowait = false;
	}

	// stopquake 画面揺れ停止
	public void tag_stopquake()
	{
		endQuake();
	}

	// startanchor
	public void tag_startanchor(HashMap<String, String> elm)
	{
		boolean enabled = tag_sub_getBoolean(elm.get("enabled"), true);
		if(enabled)
		{
			enableStartAnchor = true;
			if(!ignoreStartanchor)
				saveGame_gotostart();	// セーブ
			ignoreStartanchor = false;
		}
		else
			enableStartAnchor = false;
	}

	// locklink
	public void tag_locklink()
	{
		locklink = true;
	}

	// unlocklink
	public void tag_unlocklink()
	{
		locklink = false;
	}

	// showhistory
	public void tag_showhistory()
	{
		startHistory();
	}

	// save
	public void tag_save(HashMap<String, String> elm)
	{
		int place = tag_sub_getInteger(elm.get("place"), 0);
		boolean ask = tag_sub_getBoolean(elm.get("ask"), false);

		if(place < 0)
			return;

		if(ask)
		{
			ask_saveOrLoadPlace = place;
			conductor.stopConductor();
			yesNoDialog.ask("No." + place + "にセーブしますか？", saveFunc, endYesNoFunc);
		}
		else
			saveGame(place);
	}

	// load
	public void tag_load(HashMap<String, String> elm)
	{
		int place;
		String place_s = elm.get("place");
		if(place_s.equals("auto"))
			place = -1;
		else
		{
			place = tag_sub_getInteger(elm.get("place"), 0);
			if(place < 0)
				place = -1;
		}
		boolean ask = tag_sub_getBoolean(elm.get("ask"), false);

		if(!Util.saveDataExistence(place))
			return;

		if(ask)
		{
			ask_saveOrLoadPlace = place;
			conductor.stopConductor();
			if(place != -1)
				yesNoDialog.ask("No." + place + "をロードしますか？", loadFunc, endYesNoFunc);
			else
				yesNoDialog.ask("前回終了時の状態をロードしますか？", loadFunc, endYesNoFunc);
		}
		else
			loadGame(place);
	}

	// clearhistory : KAS オリジナル
	public void tag_clearhistory()
	{
		history.clear();
	}

	// hr
	public void tag_hr(HashMap<String, String> elm)
	{
		history.addMessage("\n");
	}

	// history
	public void tag_history(HashMap<String, String> elm)
	{
		String output = elm.get("output");
		String enabled = elm.get("enabled");

		if(output != null)
		{
			if(output.equals("true"))
				history.output = true;
			else if(output.equals("false"))
				history.output = false;
		}

		if(enabled != null)
		{
			if(enabled.equals("true"))
				history.enabled = true;
			else if(enabled.equals("false"))
				history.enabled = false;
		}

	}

	// move
	public void tag_move(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		String page = elm.get("page");
		String time_s = elm.get("time");
		String path = elm.get("path");
		float accel = tag_sub_getFloat(elm.get("accel"), 0f);

		if(layer==null || time_s==null || path==null)
			return;
		if(!Util.isInteger(time_s))
			return;
		if(layer.equals("base"))
			return;

		int time = Integer.decode(time_s);

		KASLayer kl = tag_sub_getKASLayer(layer, page);
		if(kl == null)
			return;
		try
		{
			kl.startMove(path, accel, time, conductor.nowTime);
		} catch (Exception e) { e.printStackTrace(); }
	}

	// fadeinse
	public void tag_fadeinse(HashMap<String, String> elm)
	{
		String time = elm.get("time");
		String storage = elm.get("storage");
		boolean loop = tag_sub_getBoolean(elm.get("loop"), false);

		if(storage == null || time == null || !Util.isInteger(time))
			return;

		Sound s = tag_sub_getSeBuff(elm.get("buf"));

		s.stopSound();
		s.startFadeIn(Integer.decode(time), conductor.nowTime);
		s.playSound(context, storage, loop);
	}

	// fadeinbgm
	public void tag_fadeinbgm(HashMap<String, String> elm)
	{
		String time = elm.get("time");
		String storage = elm.get("storage");
		boolean loop = tag_sub_getBoolean(elm.get("loop"), true);

		if(storage == null || time == null)
			return;

		bgmBuff.stopSound();
		bgmBuff.playSound(context, storage, loop);
		bgmBuff.startFadeIn(Integer.parseInt(time), conductor.nowTime);
	}

	// fadeoutse
	public void tag_fadeoutse(HashMap<String, String> elm)
	{
		String time = elm.get("time");
		Sound s = tag_sub_getSeBuff(elm.get("buf"));
		if(Util.isInteger(time))
			s.startFadeOut(Integer.decode(time), conductor.nowTime);
	}

	// fadeoutbgm
	public void tag_fadeoutbgm(HashMap<String, String> elm)
	{
		String time = elm.get("time");
		if(Util.isInteger(time))
			bgmBuff.startFadeOut(Integer.decode(time), conductor.nowTime);
	}

	// laycount
	public void tag_laycount(HashMap<String, String> elm)
	{
		String layers = elm.get("layers");
		String messages = elm.get("messages");

		if(layers != null && Util.isInteger(layers))
		{
			int newNum = Integer.decode(layers);
			changeLayerCount(newNum);
		}
		if(messages != null && Util.isInteger(messages))
		{
			int newNum = Integer.decode(messages);
			changeMessageLayerCount(newNum);
		}
	}

	// defstyle
	public void tag_defstyle(HashMap<String, String> elm)
	{
		currentMessage.setDefStyle(elm);
	}

	// deffont
	public void tag_deffont(HashMap<String, String> elm)
	{
		currentMessage.setDefFont(elm);
	}

	// return
	public void tag_return()
	{
		conductor.returnScenario();
	}

	// call
	public void tag_call(HashMap<String, String> elm)
	{
		boolean countpage = tag_sub_getBoolean(elm.get("countpage"), false);
		conductor.callScenario(elm.get("storage"), elm.get("target"), countpage);
	}

	// gotostart
	public void tag_gotostart(HashMap<String, String> elm)
	{
		if(enableStartAnchor)
		{
			boolean ask;
			if(elm == null)
				ask = false;
			else
				ask = tag_sub_getBoolean(elm.get("ask"), false);
			if(ask)
			{
				conductor.stopConductor();
				yesNoDialog.ask("タイトルに戻りますか？", gotostartFunc, endYesNoFunc);
			}
			else
				gotostart();
		}
	}

	// link
	public void tag_link(HashMap<String, String> elm)
	{
		String storage = elm.get("storage");
		String target = elm.get("target");
		String clickse = elm.get("clickse");
		String clicksebuf = elm.get("clicksebuf");
		boolean countpage = tag_sub_getBoolean(elm.get("countpage"), true);
		int color = tag_sub_getInteger(elm.get("color"), currentMessage.defLinkColor);
		String exp = elm.get("exp");

		Sound se;
		if(clickse != null)
		{
			if(clicksebuf != null && Util.isInteger(clicksebuf))
				se = seBuff[Integer.decode(clicksebuf)];
			else
				se = seBuff[0];
		}
		else
			se = null;

		currentMessage.startLink(storage, target, countpage, clickse, se, color, exp);
		locklink = false;
	}

	// endlink
	public void tag_endlink()
	{
		currentMessage.endLink();
	}

	// wait
	public void tag_wait(HashMap<String, String> elm)
	{
		String time_s = elm.get("time");
		String canskip_s = elm.get("canskip");

		if(time_s == null || !Util.isInteger(time_s))
			return;

		conductor.sleepConductor(Integer.decode(time_s));	// コンダクタを一定時間停止する
		waitCanskip = tag_sub_getBoolean(canskip_s, true);
		changeState(GAMESTATE.WAIT);		// 状態を待ち状態に
	}

	// wq
	public void tag_wq(HashMap<String, String> elm)
	{
		if(!quakeFlag)
			return;
		quake_canskip = tag_sub_getBoolean(elm.get("canskip"), false);
		conductor.stopConductor();
		changeState(GAMESTATE.WQ);
	}

	// quake
	public void tag_quake(HashMap<String, String> elm)
	{
		String time_s = elm.get("time");
		String hmax_s = elm.get("hmax");
		String vmax_s = elm.get("vmax");

		if(time_s == null || !Util.isInteger(time_s))
			return;

		int time = Integer.decode(time_s);
		int hmax = tag_sub_getInteger(hmax_s, 10);
		int vmax = tag_sub_getInteger(vmax_s, 10);

		quakeFlag = true;
		quakeHmax = hmax;
		quakeVmax = vmax;
		quakeTime = time;
		quakeStartTime = conductor.nowTime;
	}

	// playse
	public void tag_playse(HashMap<String, String> elm)
	{
		String storage = elm.get("storage");
		String loop_s = elm.get("loop");
		String buf_s = elm.get("buf");

		if(storage == null)
			return;

		int buf = tag_sub_getInteger(buf_s, 0);
		boolean loop = tag_sub_getBoolean(loop_s, false);

		seBuff[buf].stopSound();
		seBuff[buf].playSound(context, storage, loop);
	}

	// stopse
	public void tag_stopse(HashMap<String, String> elm)
	{
		String buf_s = elm.get("buf");
		int buf = tag_sub_getInteger(buf_s, 0);
		seBuff[buf].stopSound();
	}

	// seopt
	public void tag_seopt(HashMap<String, String> elm)
	{
		String gvolume = elm.get("gvolume");
		String volume = elm.get("volume");

		Sound s = tag_sub_getSeBuff(elm.get("buf"));

		if(gvolume != null && Util.isInteger(gvolume))
		{
			int gvol = Integer.decode(gvolume);
			s.setGlobalVolume(gvol);
		}
		if(volume != null && Util.isInteger(volume))
			s.setVolume(Integer.decode(volume));
	}

	// playbgm
	public void tag_playbgm(HashMap<String, String> elm)
	{
		String storage = elm.get("storage");
		String loop_s = elm.get("loop");

		if(storage == null)
			return;

		boolean loop = tag_sub_getBoolean(loop_s, true);
		bgmBuff.stopSound();
		bgmBuff.playSound(context, storage, loop);
	}

	// stopbgm
	public void tag_stopbgm(HashMap<String, String> elm)
	{
		bgmBuff.stopSound();
	}

	// bgmopt
	public void tag_bgmopt(HashMap<String, String> elm)
	{
		String gvolume = elm.get("gvolume");
		String volume = elm.get("volume");

		if(gvolume != null && Util.isInteger(gvolume))
			bgmBuff.setGlobalVolume(Integer.decode(gvolume));
		if(volume != null && Util.isInteger(volume))
			bgmBuff.setVolume(Integer.decode(volume));
	}

	// backlay
	public void tag_backlay(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		long nowTime = conductor.nowTime;
		if(layer != null) // レイヤの指定があった時
		{
			int num = 0;
			if(layer.indexOf("message") == 0)
			{
				num = tag_sub_getMessageLayerNumber(layer);
				if(num == -1)
					currentMessageBack.copyLayer(context, currentMessage, nowTime);
				else
					message_back[num].copyLayer(context, message_fore[num], nowTime);
				setLayerOrder();
			}
			else if(layer.indexOf("base") == 0)
			{
				baseLayer_back.copyLayer(context, baseLayer_fore, nowTime);
			}
			else if(Util.isInteger(layer))
			{
				num = Integer.decode(layer);
				layer_back[num].copyLayer(context, layer_fore[num], nowTime);
				setLayerOrder();
			}
		}
		else	// レイヤ指定無し＝全レイヤ
		{
			baseLayer_back.copyLayer(context, baseLayer_fore, nowTime);	// 背景レイヤ
			for(int i = 0; i < layerNum; i++)	// 前景レイヤ
				layer_back[i].copyLayer(context, layer_fore[i], nowTime);
			for(int i = 0; i < messageLayerNum; i++)	// メッセージレイヤ
				message_back[i].copyLayer(context, message_fore[i], nowTime);
			for(int i = pluginsLayer_fore.size()-1; i >= 0; i--)	// プラグインのレイヤ
				pluginsLayer_back.get(i).copyLayer(context, pluginsLayer_fore.get(i), nowTime);
			setLayerOrder();
			// プラグインのイベントを呼ぶ
			int size = kasPlugins.size();
			for(int i = 0; i < size; i++)
				kasPlugins.get(i).onCopyLayer(true);
		}
	}

	// trans
	public void tag_trans(HashMap<String, String> elm)
	{
		if(easyDirection) // 簡易モードの時は強制的にクロスフェード
		{
			elm.put("method", "crossfade");
			elm.put("rule", null);
		}
		if(transition.setTrans(elm, conductor.pauseTime))
		{
			changeState(GAMESTATE.TRANS);	// 状態をTRANSに
			conductor.stopConductor();		// コンダクタ停止
		}
	}

	// current
	public void tag_current(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		String page = elm.get("page");
		boolean withback = tag_sub_getBoolean(elm.get("withback"), false);

		int num;

		if(layer == null)
			num = 0;
		else
			num = tag_sub_getMessageLayerNumber(layer);
		if(num != -1)
		{
			if(page == null || page.equals("fore"))
				changeCurrent(num, "fore");
			else if(page.equals("back"))
				changeCurrent(num, "back");
		}
		currentMessage.withback = withback;
	}

	// s シナリオ停止
	public void tag_s()
	{
		changeState(GAMESTATE.STOP);
		conductor.addOldLabel();	// 既読にする
		conductor.stopConductor();	// コンダクタを停止
		setMenuEnabled(true);
	}

	// r 改行
	public void tag_r()
	{
		onAddMessage("\n");
		changeState(GAMESTATE.DEFAULT);
	}

	// p 改ページ停止
	public void tag_p()
	{
		if(touch_nowait)	// タッチ操作によるnowaitを終了
		{
			conductor.setNowait(flag_nowait);
			touch_nowait = false;
		}
		changeState(GAMESTATE.WAITKEY_PAGE);	// キー待ち状態に
		currentMessage.glyphOn(1, conductor.nowTime);	// グリフ表示
		conductor.stopConductor();			// コンダクタを止める
		history.addMessage("\n\n");
		setMenuEnabled(true);
	}

	// l 行末停止
	public void tag_l()
	{
		if(touch_nowait)	// タッチ操作によるnowaitを終了
		{
			conductor.setNowait(flag_nowait);
			touch_nowait = false;
		}
		changeState(GAMESTATE.WAITKEY_LINE);	// キー待ち状態に
		currentMessage.glyphOn(0, conductor.nowTime);	// グリフ表示
		conductor.stopConductor();			// コンダクタを止める
		setMenuEnabled(true);
	}

	// jump シナリオをジャンプ
	public void tag_jump(HashMap<String, String> elm)
	{
		boolean countpage = tag_sub_getBoolean(elm.get("countpage"), true);
		conductor.jumpScenario(elm.get("storage"), elm.get("target"), countpage);
	}

	// position メッセージレイヤの設定
	public void tag_position(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		String page = elm.get("page");

		MessageLayer ml;
		if(layer == null)
			ml = currentMessage;
		else
			ml = tag_sub_getMessagelayer(layer, page);

		if(ml != null && ml.layerType == 'M')
		{
			ml.clearMessage();
			ml.setOption(context.getResources(), elm);
			if(elm.containsKey("index")) setLayerOrder();
		}
	}

	// layopt レイヤの設定
	public void tag_layopt(HashMap<String, String> elm)
	{
		String layer = elm.get("layer");
		String page = elm.get("page");

		if(layer != null && layer.equals("all"))
		{
			// 全部のレイヤ
			MessageLayer[] messages;
			Layer[] layers;
			Layer base;
			if(page == null)
				page = "fore";
			if(page.equals("back"))
			{
				base = baseLayer_back;
				layers = layer_back;
				messages = message_back;
			}
			else
			{
				base = baseLayer_fore;
				layers = layer_fore;
				messages = message_fore;
			}
			base.setOption(elm);
			for(int i = 0; i < layers.length; i++)
				layers[i].setOption(elm);
			String opacity_s = elm.get("opacity");
			elm.remove("opacity");	// opacityは取り除く
			for(int i = 0; i < messages.length; i++)
				messages[i].setOption(context.getResources(), elm);
			if(opacity_s != null && Util.isInteger(opacity_s))
			{
				int opacity = Integer.decode(opacity_s);
				for(int i = 0; i < messages.length; i++)
					messages[i].setLayerOpacity(opacity);
			}
		}
		else
		{
			// 単独レイヤ
			MessageLayer m;
			Layer l;
			KASLayer kl = tag_sub_getKASLayer(layer, page);
			if(kl == null)
				return;
			switch(kl.layerType)
			{
			case 'B':		// 背景
			case 'L':		// 前景
				l = (Layer)kl;
				l.setOption(elm);
				break;
			case 'M':		// メッセージ
				String opacity_s = elm.get("opacity");
				elm.remove("opacity");	// opacityは取り除く
				m = (MessageLayer)kl;
				m.setOption(context.getResources(), elm);
				if(opacity_s != null && Util.isInteger(opacity_s))
					m.setLayerOpacity(Integer.decode(opacity_s));
				break;
			}
		}

		if(elm.containsKey("index"))
			setLayerOrder();
	}

	// image 画像の読み込み
	public void tag_image(HashMap<String, String> elm)
	{
		// storage layer page mode top left visible pos opacity
		String layer = elm.get("layer");
		String page = elm.get("page");
		String storage = elm.get("storage");

		if(layer == null || storage == null)
			return;

		Layer l;
		KASLayer kl = tag_sub_getKASLayer(layer, page);
		if(kl == null)
			return;

		switch(kl.layerType)
		{
		case 'B':
		case 'L':
			l = (Layer)kl;
			l.loadImage(context, storage);
			l.setOption(elm);
			l.startAnimation(conductor.nowTime, true);	// 自動ロードされたアニメーションの開始
			break;
		default:
			return;
		}

		if(elm.containsKey("index"))
			setLayerOrder();

		// TODO: ガンマ値、輝度、色ブレンドなどのパラメータ
	}

	// setversion : KASオリジナル
	public void tag_setversion(HashMap<String, String> elm)
	{
		String text = elm.get("text");
		if(text != null)
			Util.gameVersion = text;
	}

	// callload 組み込みロード画面の呼び出し
	public void tag_callload()
	{
		Util.callSaveActivity("load");
	}

	// callsave 組み込みセーブ画面の呼び出し
	public void tag_callsave()
	{
		Util.callSaveActivity("save");
	}

	// callconfig 組み込み設定画面の呼び出し
	public void tag_callconfig()
	{
		Util.callConfigActivity();
	}

	// assign 変数への代入 : KASオリジナル
	public void tag_assign(HashMap<String, String> elm)
	{
		String var = elm.get("var");
		String value = elm.get("value");
		if(var == null || value == null || var.length() == 0)
			return;
		scripter.eval("var " + var + "=" + value + ";");
	}

	/**
	 * 背景レイヤを返す
	 * page が省略されている場合は fore として返す
	 * @param page
	 * @return
	 */
	public Layer tag_sub_getBaceLayer(String page)
	{
		if(page == null || page.equals("fore"))
			return baseLayer_fore;
		else if(page.equals("back"))
			return baseLayer_back;
		else
			return null;
	}

	/**
	 * 前景レイヤを返す
	 * page が省略されている場合は fore として返す
	 * @param layer
	 * @param page
	 * @return
	 */
	public Layer tag_sub_getLayer(String layer, String page)
	{
		if(layer != null)
		{
			if(Util.isInteger(layer))
			{
				if(page == null || page.equals("fore"))
					return layer_fore[Integer.decode(layer)];
				else if(page.equals("back"))
					return layer_back[Integer.decode(layer)];
			}
		}
		return null;
	}

	/**
	 * メッセージレイヤを返す
	 * page が省略されている場合は fore として返す
	 * @param layer
	 * @param page
	 * @return
	 */
	public MessageLayer tag_sub_getMessagelayer(String layer, String page)
	{
		if(layer == null || !layer.contains("message"))
			return null;
		layer = layer.replaceAll("message", "");
		if(layer.length() == 0)
		{
			if(page == null || page.equals("fore"))
				return currentMessage;
			else if(page.equals("back"))
				return currentMessageBack;
		}
		else if(Util.isInteger(layer))
		{
			if(page == null || page.equals("fore"))
				return message_fore[Integer.decode(layer)];
			else if(page.equals("back"))
				return message_back[Integer.decode(layer)];
		}
		return null;
	}

	/**
	 * レイヤを判別して返す
	 * page が省略されている場合は fore として返す
	 * @param layer
	 * @param page
	 * @return
	 */
	public KASLayer tag_sub_getKASLayer(String layer, String page)
	{
		if(layer == null || layer.length() == 0)
			return null;

		if(layer.equals("base"))	// 背景レイヤ
			return tag_sub_getBaceLayer(page);
		else if(layer.indexOf("message") == 0)	// メッセージレイヤと見なす
			return tag_sub_getMessagelayer(layer, page);
		else if(Util.isInteger(layer))	// 前景レイヤ
			return tag_sub_getLayer(layer, page);
		else
			return null;
	}

	/**
	 * message00を受け取って00を返す。messageの場合は-1
	 * @param layer
	 * @return
	 */
	public int tag_sub_getMessageLayerNumber(String layer)
	{
		int len = layer.length();
		if(len <= 7)
			return -1;
		return Integer.decode(layer.substring(7));
	}

	/**
	 * SEバッファ番号を返す
	 * @param buf
	 * @return
	 */
	public int tag_sub_getSeBuffNum(String buf)
	{
		int b;
		if(buf == null)
			b = 0;
		else if(Util.isInteger(buf))
		{
			b = Integer.decode(buf);
			if(b < 0 || seBuff.length < b)
				b = 0;
		}
		else
			b = 0;
		return b;
	}

	/**
	 * SEバッファを返す
	 * @param buf
	 * @return
	 */
	public Sound tag_sub_getSeBuff(String buf)
	{
		return seBuff[tag_sub_getSeBuffNum(buf)];
	}

	/**
	 * trueかfalseかを返す
	 * @param value
	 * @param def
	 * @return
	 */
	public boolean tag_sub_getBoolean(String value, boolean def)
	{
		if(value == null)
			return def;
		else if(value.equals("true"))
			return true;
		else if(value.equals("false"))
			return false;
		else
			return def;
	}

	/**
	 * 数値を返す
	 * @param value
	 * @param def
	 * @return
	 */
	public int tag_sub_getInteger(String value, int def)
	{
		if(value == null)
			return def;
		if(Util.isInteger(value))
			return Integer.decode(value);
		else
			return def;
	}

	/**
	 * 数値を返す
	 * @param value
	 * @param def
	 * @return
	 */
	public float tag_sub_getFloat(String value, float def)
	{
		if(value == null)
			return def;
		if(Util.isFloat(value))
			return Float.valueOf(value);
		else if(Util.isInteger(value))
			return Integer.decode(value);
		else
			return def;
	}

}// end of MainSurfaceView class
