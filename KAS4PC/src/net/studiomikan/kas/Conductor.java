package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.studiomikan.kas4pc.klib.Context;
import net.studiomikan.kas4pc.klib.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ゲームの進行を制御するクラス<br>
 * パーサからの結果を整理してメインに渡します。
 * マクロの管理と実行をします。
 * コール・ジャンプの管理をします。
 * 通過・未通過の管理をします。
 * @author okayu
 */
public class Conductor implements Runnable
{
	/** コンテキスト */
	private Context context = null;
	/** このコンダクタを保持するサーフェイスビュー */
	private MainSurfaceView owner = null;

	/** メインスレッドのループフラッグ */
	public boolean threadFlag = true;
	/** 現在の時刻 */
	public long nowTime = 0;
	/** 現在の時刻（ただし、端末スリープ時間を考慮していない） */
	private long nowTime2 = 0;

	/** コンダクタの停止フラグ */
	private boolean stoped = false;
	/** コンダクタを止める時間 0で無ければ止まる */
	private int sleepTime = 0;
	/** コンダクタの一時停止を要求された時間 */
	private long sleepStartTime = 0;
	/** コンダクタの１フレームの時間 */
	static private final int frameTime = 1;
	/** 端末がスリープに入った時間 */
	private long startPauseTime = 0;
	/** 端末がスリープしていた時間の総計 */
	public long pauseTime = 0;

	/** fps用 */
	private int framesInSecond = 0;
	/** fps用 */
	private long fpsCountStartTime = 0;
	/** fps用 */
	public int fps = 0;

	/** スクリプトのパーサ */
	public ScenarioParser parser = null;
	/** パース結果の受け取り用 */
	public String parsedString = "";

	/** 通過済みラベルのデータ */
	private HashMap<String, Integer> trail = null;
	/** 通過済みラベルを保存するファイル名 */
	private static final String trailLabelSaveFileName = "datatrail";
	/** 現在の場所が既読かどうか */
	private boolean hereIsAlreadyRead = false;

	/** callタグ用スタック */
	private Stack<CallData> callStack = null;

	/** マクロのデータ */
	private HashMap<String, Macro> macroMap = null;
	/** 現在マクロ処理中なら true */
	private boolean macroFlag = false;
	/** 現在実行中のマクロ */
	private Macro nowMacro = null;
	/** マクロのネスト用スタック */
	private Stack<Macro> macroStack = null;

	/** if のネストカウント */
	private int ifCount = 0;

	/** タッチイベント受け渡し用 x 座標 */
	private int touch_x = 0;
	/** タッチイベント受け渡し用 y 座標 */
	private int touch_y = 0;
	/** down イベント発生フラグ */
	private boolean touch_down = false;
	/** up イベント発生フラグ */
	private boolean touch_up = false;
	/** move イベント発生フラグ */
	private boolean touch_move = false;

	/** テキストインターバルが0の時の、特別なnowait */
	private boolean textZeroNowait = false;

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 * @param owner このコンダクタを保持するサーフェイスビュー
	 */
	public Conductor(Context context, MainSurfaceView owner)
	{
		this.context = context;
		parser = new ScenarioParser(context);
		this.owner = owner;

		macroMap = new HashMap<String, Macro>();
		macroStack = new Stack<Macro>();

		trail = new HashMap<String, Integer>();
		loadTrailLabelList();	// 既読情報読み出し

		callStack = new Stack<CallData>();

		ifCount = 0;
	}

	/**
	 * 通過ラベルリストの読み込み
	 */
	public void loadTrailLabelList()
	{
		trail.clear();
		String file = Util.file2str(context, trailLabelSaveFileName);
		if(file == null)
			return;

		String[] line = file.split("\n");
		int e;
		String label;
		int value;
		for(int i = 0; i < line.length; i++)
		{
			if(line[i].length() > 0)
			{
				e = line[i].lastIndexOf('\"');
				if(e != -1)
				{
					label = line[i].substring(1, e);
					value = Integer.valueOf( line[i].substring(e+2) );
					trail.put(label, value);
				}
			}
		}
	}

	/**
	 * 通過ラベルリストの保存
	 */
	public void saveTrailLabelList()
	{
		if(trail.size() == 0)
			return;

		StringBuilder sb = new StringBuilder();

		String key;
		Integer value;
		for(Map.Entry<String, Integer> entry : trail.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			sb.append('\"');
			sb.append(key);
			sb.append("\"=");
			sb.append(value);
			sb.append('\n');
		}

		Util.str2file(context, sb.toString(), trailLabelSaveFileName);
	}

	/**
	 * 初期化
	 */
	public void reset()
	{
		hereIsAlreadyRead = false;
		callStack.clear();
		macroFlag = false;
		nowMacro = null;
		macroStack.clear();
		clearOldLabel();
		ifCount = 0;
		owner.scripter.eval("__kas__.macroCount = 0;");
	}


	/**
	 * コンダクタを停止する
	 */
	public void stopConductor()
	{
		this.stoped = true;
		sleepTime = 0;
	}

	/**
	 * 停止したコンダクタを再開
	 */
	public void startConductor()
	{
		this.stoped = false;
		sleepTime = 0;
	}

	/**
	 * コンダクタをスリープ
	 * @param sleepTime スリープさせる時間（ミリ秒）
	 */
	public void sleepConductor(int sleepTime)
	{
		this.sleepTime = sleepTime;
		sleepStartTime = nowTime;
	}

	/**
	 * コンダクタの状態を返す
	 * @return 状態を表す文字列
	 */
	public String getConductorState()
	{
		if(stoped)
			return "stop";
		else if(sleepTime != 0)
			return "sleep";
		else
			return "run";
	}

	/**
	 * スレッド終了
	 */
	public void shutdown()
	{
		threadFlag = false;
	}

	/**
	 * スレッド一回の処理が一定時間になるように寝る
	 * @param start 開始時間
	 * @param now 現在の時間
	 */
	public void keepFps(long start, long now)
	{
		long diff = now - start;
		if(diff <= frameTime)
		{
			try
			{
				Thread.sleep(frameTime - diff);
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
	}

	/**
	 * onPause イベント
	 */
	public void onPause()
	{
		startPauseTime = Util.getNowTime();
	}

	/**
	 * onResume イベント
	 */
	public void onResume()
	{
		if(startPauseTime != 0)
		{
			pauseTime += Util.getNowTime() - startPauseTime;
			startPauseTime = 0;
		}
	}

	/**
	 * コンダクタのfpsを計測
	 */
	private void fps()
	{
		if(Util.debug)
		{
			long nowTime = Util.getNowTime();
			long diff = nowTime - fpsCountStartTime;
			if(diff >= 10000)
			{
				fps = framesInSecond;
				framesInSecond = 0;
				fpsCountStartTime = nowTime;
				Util.log("CONDUCTOR FPS:" + fps + " " + owner.gameState);
			}
			framesInSecond++;
		}
	}

	/**
	 * コンダクタのメイン処理 スレッド
	 */
	@Override
	public void run()
	{
		Util.log("!onStartConductor");
		threadFlag = true;
		long afterTime = 0; // 処理後の時間
		while(threadFlag)
		{
			fps();

			// この周回の時間を取得
			nowTime2 = Util.getNowTime();
			nowTime = nowTime2 - pauseTime;

			// タップイベントを発生
			if(touch_down)
			{
				touch_down = false;
				owner.onTouch(touch_x, touch_y, MotionEvent.ACTION_DOWN);
			}
			if(touch_move)
			{
				touch_move = false;
				owner.onTouch(touch_x, touch_y, MotionEvent.ACTION_MOVE);
			}
			if(touch_up)
			{
				touch_up = false;
				owner.onTouch(touch_x, touch_y, MotionEvent.ACTION_UP);
			}

			// ownerの処理色々
			owner.loopProcess(nowTime);

			// 停止中
			if(stoped)
			{
				keepFps(nowTime2, Util.getNowTime());
				continue;
			}

			// スリープ中
			if(sleepTime != 0)
			{
				if( sleepTime > (nowTime-sleepStartTime) )
					continue;
				else	// スリープ終了
					sleepTime = 0;
			}

			// owner に指示
			synchronized(owner)
			{
				conduct();
				while(owner.touch_nowait)
					conduct();
			}

			// 調節
			if(sleepTime != 0)
			{
				afterTime = Util.getNowTime() - pauseTime;
				sleepTime -= afterTime - nowTime;
			}
		}
		Util.log("!onEndConductor");
	}

	/**
	 * owner に指示する
	 */
	public void conduct()
	{
		//----------------------------------------------------------------
		//	 タグ、あるいはマクロの指示
		boolean flag = true;
		while(flag)
		{
			flag = false;	// 普通は一回で抜ける
			getNextCommand();
			switch(parser.result)	// パースの結果で分岐
			{
			case ScenarioParser.PARCE_RESULT_TEXT:	// 通常のテキスト
				text();
//				if(parser.nowait) 	// ノーウェイトならどんどん読み込む
//					flag = true;
				break;
			case ScenarioParser.PARCE_RESULT_TAG:		// タグ
				flag = tag();	// if タグなどの場合は続けて処理することがある
				break;
			case ScenarioParser.PARCE_RESULT_VOIDLINE:// 空行
				voidLine();
				break;
			case ScenarioParser.PARCE_RESULT_LABEL:	// ラベル
				label();
				break;
//			case ScenarioParser.PARCE_RESULT_NAME:	// 名前
//				name();
//				break;
//			case ScenarioParser.PARCE_RESULT_RETURN:	// 改行
//				owner.onAddMessage("\n");
//				break;
			default:
				break;
			}
		}
	}

	/**
	 * 次の命令を取得する。結果は parsedString に収められる。
	 * このメソッドを介さずに parser.parse() を呼ぶとマクロが実行されないので注意
	 */
	private void getNextCommand()
	{
		while(true)
		{
			if(macroFlag && !parser.parseTextFlag)	// マクロ実行中、かつテキストパース中でない
			{
				String macroString = nowMacro.pop();
				Util.log("::MACRO " + macroString);
				if(macroString == null)	// マクロ終了
				{
					owner.scripter.eval("__kas__.macroCount--;");
					// 次のマクロが null なら終了
					if(macroStack.isEmpty()) nowMacro = null;
					else nowMacro = macroStack.pop();
					if(nowMacro == null)
					{
						macroFlag = false;
						owner.scripter.eval("mp = %[]; __kas__.macroCount = 0;");
						continue;
					}
					else // 元のマクロに復帰
					{
						owner.scripter.eval("mp = __kas__.macro[__kas__.macroCount-1];");
						continue;
					}
				}
				else	// マクロ実行
					parsedString = parser.parseFromBuff(macroString);	// パースした文字列を受け取る
			}
			else	// 通常実行
				parsedString = parser.parse(); // パースした文字列を受け取る

			break;	// 普通は一回で抜ける
		}
	}

	/**
	 * サブルーチンのコール
	 * @param storage シナリオファイル名
	 * @param target ジャンプ先のラベル名
	 * @param countpage countpage 属性
	 */
	public void callScenario(String storage, String target, boolean countpage)
	{
		if(storage == null && target == null)
			return;

		CallData callData = new CallData();
		// スタックに状態を保存
		callData.filename = parser.getFileName();
		callData.point = parser.getPoint();
		callData.label = oldLabel;
		callData.alreadyRead = hereIsAlreadyRead;
		callData.inMacro = macroFlag;
		callData.macro = nowMacro;
		callData.rclick = false;
		callData.locklink = owner.locklink;

		callStack.push(callData);

		Util.log("Call storage=" + storage + " point=" + callData.point);

		// マクロ終了、区切りを挿入
		macroFlag = false;
		macroStack.push(null);
		nowMacro = null;

		// 飛ぶ
		jumpScenario(storage, target, countpage);
	}

	/**
	 * 右クリックサブルーチンのコール
	 * @param storage シナリオファイル名
	 * @param target ジャンプ先のラベル名
	 */
	public void callRclickSubroutine(String storage, String target)
	{
		if(storage == null && target == null)
			return;
		// 普通に処理
		callScenario(storage, target, true);
		// 少しいじる
		CallData callData = callStack.peek();;
		callData.rclick = true;
	}

	/**
	 * サブルーチンからの復帰
	 */
	public void returnScenario()
	{
		if(callStack.empty())
		{
			//Util.log("KAS.Conducter.returnScenario", "スタックが空です call~returnの対応がおかしい");
			return;
		}

		// 現在の位置のラベルを既読に
		addOldLabel();

		// 状態復帰
		CallData data = callStack.pop();
		String filename = data.filename;
		int point = data.point;

		if(data.rclick)
		{
			macroFlag = data.inMacro;
			nowMacro = data.macro;
			// 右クリックサブルーチンの場合、マクロ中に呼び出される可能性がある
			// その場合は、マクロに復帰する。
			// マクロ中で無い場合は、呼び出し元のタグをもう一度実行する。
			// いずれの場合も、呼び出し元のタグは l p s のどれかであるはず。
			//（安定している状態で呼ばれているはずだから）
			if(macroFlag && nowMacro != null)
				nowMacro.back();
			else
				point--;

			// 右クリックサブルーチン内部で link を使った場合、
			// 戻った時にロックされている可能性がある。これを戻す。
			owner.locklink = data.locklink;
		}

		// マクロスタックに積んだ区切りを除去
		if(!macroStack.isEmpty() && macroStack.peek() == null)
			macroStack.pop();

		// 飛ぶ
		parser.loadFile(filename);
		parser.jumpPoint(point + 1);	// call タグの次の命令
		oldLabel = data.label;
	}

	/**
	 * シナリオのジャンプ countpage 属性は true
	 * @param storage シナリオファイル名
	 * @param target ラベル名
	 */
	public void jumpScenario(String storage, String target)
	{
		jumpScenario(storage, target, true);
	}

	/**
	 * シナリオのジャンプ
	 * @param storage シナリオファイル名
	 * @param target ラベル名
	 * @param countpage countpage 属性
	 */
	public void jumpScenario(String storage, String target, boolean countpage)
	{
		if(countpage)
			addOldLabel();
		else
			clearOldLabel();

		// ファイルを開く
		if(storage != null && storage.length() != 0)
			parser.loadFile(storage);
		else	// 空の時は現在のファイルを読み直し
			parser.jumpPoint(1);
		// ラベル移動
		if(target != null && target.length() != 0)
			parser.searchLabel(target);
		// if のネストをクリア
		ifCount = 0;
		// セーブを更新
//		owner.saveGame_toBuffer();
		// 既読未読を設定
		label();
	}

	/**
	 * 現在のシナリオファイル名を返す
	 * @return シナリオファイル名
	 */
	public String getFileName()
	{
		return parser.getFileName();
	}

	/**
	 * 現在位置のラベル名を返す
	 * @return ラベル名
	 */
	public String getLabelName()
	{
		return parser.getLabelName();
	}

	/**
	 * 現在位置のラベルテキストを返す
	 * @return ラベルテキスト
	 */
	public String getLabelText()
	{
		return parser.getLabelText();
	}

	/**
	 * 現在位置が既読かどうかを返す
	 * @return 既読なら true 未読なら false
	 */
	public boolean isAlreadyRead()
	{
		return hereIsAlreadyRead;
	}

	/**
	 * nowaitの設定
	 * @param nowait
	 */
	public void setNowait(boolean nowait)
	{
		if(textZeroNowait)
			parser.nowait = true;
		else
			parser.nowait = nowait;
	}

	/**
	 * テキストのウェイトがゼロの時の、特別なnowaitを設定
	 * @param nowait
	 */
	public void setTextZeroNowait(boolean nowait)
	{
		textZeroNowait = nowait;
		setNowait(nowait);
	}

	/** ひとつ前のラベル */
	private String oldLabel = null;
	/**
	 * パーサからラベルを受け取った時の処理
	 */
	private void label()
	{
		String label = parser.getLabelName();
		if(label != null)
		{
			// セーブ可能ラベルならバッファにセーブ
			String labelText = parser.map.get("labelText");
			if(labelText != null)
			{
//				Util.log("::セーブ可能ラベルを通過：" + labelText);
				owner.saveGame_toBuffer();
			}

			// 新しいラベルは、そのラベルの次のラベルが読み込まれたときに
			// 初めて追加される。
			addOldLabel();

			// 既読カウント
			label = parser.getFileName() + label;
			if(trail.containsKey(label))	// 既にあるならカウントアップ
			{
				int value = trail.get(label);
				if(Integer.MIN_VALUE < value  && value < Integer.MAX_VALUE)
					trail.put(label, value+1);
				hereIsAlreadyRead = true;	// 既読フラグ
//				Util.log("カウントアップ：" + (value+1) + ":" + label);
			}
			else	// 無い時は保留
			{
				oldLabel = label;	// 予約
				hereIsAlreadyRead = false;
			}
		}
	}

	/**
	 * 一つ前のラベルを既読にする
	 */
	public void addOldLabel()
	{
		if(oldLabel != null)
		{
//			Util.log("***ラベル" + oldLabel + "を追加");
			trail.put(oldLabel, 1);
			oldLabel = null;
		}
	}

	/**
	 * 一つ前のラベルをクリアする
	 */
	public void clearOldLabel()
	{
		oldLabel = null;
	}

//	/**
//	 * パーサから名前を受け取った時の処理
//	 */
//	private void name()
//	{
//		owner.setName(parsedString);
//	}

	/**
	 * パーサから空行を受け取った時の処理
	 */
	private void voidLine()
	{
		// 今のところ何もしない
	}

	/**
	 * パーサから文字列を受け取った時の処理
	 */
	private void text()
	{
		owner.onAddMessage(parsedString);	// カレントメッセージレイヤに追加
	}

	/**
	 * パーサからタグを受け取った時の処理
	 * @return 続けて処理したい場合は true
	 */
	private boolean tag()
	{
		if(parsedString.equals("macro")) // macroタグだった
		{
			String name = parser.map.get("name");
			if(name != null)
				readMacro(name);
			return false;
		}
		else if(parsedString.equals("if")) // if
		{
			onIf();
			return true;
		}
		else if(parsedString.equals("elsif") || parsedString.equals("else")) // elsif else
		{
			jump2Endif(ifCount);
			onEndif();
			return true;
		}
		else if(parsedString.equals("endif")) // endif
		{
			Util.log("*onEndif " + parsedString);
			onEndif();
			return true;
		}

		if(macroFlag)	// マクロ中なら、特殊属性(hoge=%piyo)を適用
			checkMacroAtt();

		owner.tagHandlers(parsedString, parser.map);// オーナーのタグハンドラに任せる
		return false;
	}

	/**
	 * if タグを読み込んだときの動作
	 */
	private void onIf()
	{
		int count = ifCount++;	// カウントアップ

		String exp;
		boolean flag = true;
		int result, jumpResult;

		// 最初の条件分岐
		exp = parser.map.get("exp");

		while(flag)
		{
			if(exp == null)
			{
				Util.error("line:" + parser.getLineNumber() + "\nif 関連タグの条件が空です");
				break;
			}
			if(!exp.endsWith(";"))
				exp += ";";

			result = owner.scripter.cond(exp);	// 条件

			switch(result)
			{
			case MokaScript.COND_TRUE:	// true ならそのまま続けて実行
				flag = false;
				break;
			case MokaScript.COND_FALSE:	// false なら elsif か else か endif に飛ぶ
				jumpResult = jumpNextIf(count);
				switch(jumpResult)
				{
				case 0:	// endif
					ifCount--;	// if ブロックを脱出
					Util.log("ifブロック脱出 ifCount=" + ifCount);
					flag = false;
					break;
				case 1: // elsif
					exp = parser.map.get("exp");
					break;
				case 2: // else
					flag = false;
					break;
				}
				break;
			default:	// エラー
				Util.error("line:" + parser.getLineNumber() + "\nif 関連タグの条件式でエラー");
				flag = false;
				break;
			}
		}
	}

	/**
	 * endif タグを読み込んだときの動作  現在の if ブロックから脱出する
	 */
	private void onEndif()
	{
		ifCount--;
		if(ifCount < 0)
			Util.error("line:" + parser.getLineNumber() + "\nif~endifの対応がおかしい(" + ifCount + ")");
	}

	/**
	 * 現在の if elsif などに対応する、elsif else endif にジャンプする。
	 * @param startCount 現在の if ブロックの深さ
	 * @return 0:終了 1:elsif 2:else
	 */
	private int jumpNextIf(int startCount)
	{
		String buff;
		boolean flag = true;
		int count = startCount;
		int result = 0;
		setNowait(true);
		while(flag)
		{
			getNextCommand();	// 次の命令
			buff = parsedString;
			switch(parser.result)
			{
			case ScenarioParser.PARCE_RESULT_TAG:
				if(buff.equals("if"))	// if のネスト
					count++;
				else if(buff.equals("endif"))
				{
					if(count == startCount)	// 開始時と同じ深さ
					{
						result = 0;
						flag = false;	// 終了
					}
					count--;
				}
				else if(buff.equals("elsif"))
				{
					if(count == startCount)
					{
						result = 1;
						flag = false;
					}
				}
				else if(buff.equals("else"))
				{
					if(count == startCount)
					{
						result = 2;
						flag = false;
					}
				}
				break;
			case ScenarioParser.PARCE_RESULT_LABEL:
				Util.error(parser.fileName + " line:" + parser.getLineNumber() + "\nif~endif中にラベルを書いてはいけない");
				flag = false;
				break;
			case ScenarioParser.PARCE_RESULT_EOF:
				Util.error(parser.fileName + " line:" + parser.getLineNumber() + "\nif~endifの対応がおかしい");
				flag = false;
				break;
			}
		}
		setNowait(false);
		return result;
	}

	/**
	 * 現在の if ブロックの末尾の endif へ飛ぶ
	 * @param startCount 現在の if ブロックの深さ
	 */
	private void jump2Endif(int startCount)
	{
		String buff;
		boolean flag = true;
		int count = startCount;
		setNowait(true);
		while(flag)
		{
			getNextCommand();	// 次の命令
			buff = parsedString;
			switch(parser.result)
			{
			case ScenarioParser.PARCE_RESULT_TAG:
				if(buff.equals("if"))	// if のネスト
					count++;
				else if(buff.equals("endif"))
				{
					if(count == startCount)	// 開始時と同じ深さ
						flag = false;	// 終了
					count--;
				}
				break;
			}
		}
		setNowait(false);
	}

	/**
	 * マクロの読み込みと登録
	 * @param name マクロ名
	 */
	private void readMacro(String name)
	{
		Macro m = new Macro(name);
		String buff;
		boolean flag = true;

		setNowait(true);
		while(flag)
		{
			buff = parser.parse();	// パースする
			switch(parser.result)
			{
			case ScenarioParser.PARCE_RESULT_TEXT:
				m.addMacro(buff);
				break;
			case ScenarioParser.PARCE_RESULT_TAG:
				if(buff.equals("endmacro"))// endmacroだった
					flag = false; // ループ脱出
				else
					m.addMacro(parser.scenarioBuff);
				break;
			case ScenarioParser.PARCE_RESULT_RETURN:
				// なんにもしない
				break;
			case ScenarioParser.PARCE_RESULT_VOIDLINE:
				// なんにもしない
				break;
			case ScenarioParser.PARCE_RESULT_NAME:
				m.addMacro(parser.scenarioBuff);
				break;
			case ScenarioParser.PARCE_RESULT_LABEL:
				Util.error(parser.fileName + " line:" + parser.getLineNumber() + "\nmacro中にラベルを書いてはいけない");
//				flag = false;
				break;
			case ScenarioParser.PARCE_RESULT_EOF:
				// 警告
				Util.error(parser.fileName + " line:" + parser.getLineNumber() + "\nmacro~endmacroの対応がおかしい");
				flag = false;
				break;
			default:// ここに来ることは無いはず
				//Util.log("エラー readMacro あるはずのないdefault");
				break;
			}
		}
		setNowait(false);
		macroMap.put(name, m);
//		macroMap.get(name).print();
	}

	/**
	 * マクロの存在確認
	 * @param name マクロ名
	 * @return 存在するなら true
	 */
	public boolean containsMacro(String name)
	{
		if(macroMap != null)
			return macroMap.containsKey(name);
		else
			return false;
	}

	/**
	 * マクロの実行
	 * @param name マクロ名
	 */
	public void conductMacro(String name)
	{
		Macro m = macroMap.get(name);
		if(m == null)
			return;

		// 前のマクロをスタックに積む
		if(nowMacro != null)
		{
			// 同じ名前だったら実行しない
			if(!macroStack.isEmpty() && macroStack.peek() != null && macroStack.peek().getName().equals(name))
				return;
			macroStack.push(nowMacro);
		}

		// 新規にマクロを開始
		macroFlag = true;
		nowMacro = m;
		m.reset();

		// マクロに属性を保持させる
		// 属性はparser.mapの中にある
		m.mp = new HashMap<String, String>(parser.map);
	}

	/**
	 * マクロの特殊属性をチェック、適用
	 */
	private void checkMacroAtt()
	{
		String key;
		String at;
		String[] buff;
		String get;
		for(Map.Entry<String, String> entry : parser.map.entrySet())
		{
			key = entry.getKey();
			at = entry.getValue();
			if(at != null && at.length() >= 2 && at.charAt(0) == '%')
			{
				at = at.substring(1);	// %を消す
				// |で分割しとく
				buff = at.split("\\|");
				get = nowMacro.mp.get(buff[0]);	// 取得
				if(get != null)	// マクロ引数にあった
				{
//					Util.log("key=" + key + " value=" + get);
					parser.map.put(key, get);	// 入れ替える
				}
				else	// 無かった……
				{
					if(buff.length >= 2)// |以降がある
						parser.map.put(key, buff[1]);	// 入れ替え
					else	// |以降がなかった＝必須にもかかわらず省略
						parser.map.put(key, null);
				}
			}
		}
	}

	/**
	 * マクロの削除
	 * @param name マクロ名
	 */
	public void removeMacro(String name)
	{
		if(macroMap.containsKey(name))
			macroMap.remove(name);
	}

	/**
	 * マクロの保存
	 * @param json 保存先
	 * @throws JSONException
	 */
	public void storeMacro(JSONObject json) throws JSONException
	{
		if(json == null) return;

		JSONArray list = new JSONArray();	// マクロ一覧保存用
		JSONArray objList = new JSONArray();	// マクロ一覧保存

		String key;
		Macro value;
		for(Map.Entry<String, Macro> entry : macroMap.entrySet())
		{
			key = entry.getKey();
			value = entry.getValue();
			list.put(key);
			objList.put(value.store());
		}

		JSONObject mac = new JSONObject();
		mac.put("list", list);
		mac.put("objList", objList);
		json.put("macro", mac);
	}

	/**
	 * マクロの復元
	 * @param json 復元元
	 * @throws JSONException
	 */
	public void restoreMacro(JSONObject json) throws JSONException
	{
		if(json == null || !json.has("macro")) return;

		macroMap.clear();

		JSONArray list, objList;
		JSONObject mac;

		mac = json.getJSONObject("macro");
		list = mac.getJSONArray("list");
		objList = mac.getJSONArray("objList");

		String name;
		Macro macro;
		int size = list.length();
		for(int i = 0; i < size; i++)
		{
			name = list.getString(i);
			macro = new Macro(name);
			macro.restore(objList.getJSONArray(i));
			macroMap.put(name, macro);
		}
	}

	/**
	 * SurfaceViewがタッチされたときのイベントを受け取る
	 * @param event イベント
	 */
	public void onTouch(MotionEvent event)
	{
		int action = event.getAction();
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:	// ダウン
			touch_down = true;
			break;
		case MotionEvent.ACTION_UP:		// アップ
			touch_up = true;
			break;
		case MotionEvent.ACTION_MOVE:	// 移動
			touch_move = true;
			break;
		default:
			return;
		}
		touch_x = Util.getConvertedX(event.getX());
		touch_y = Util.getConvertedY(event.getY());
	}


}//end of Conductor class



/**
 * マクロクラス
 * @author okayu
 */
class Macro
{
	/** マクロ名 */
	private String name;
	/** マクロの内容 */
	private ArrayList<String> array = new ArrayList<String>();
	/** 現在の場所（行） */
	private int point = 0;
	/** マクロ呼び出し時に与えられた属性 */
	public HashMap<String, String> mp;

	/**
	 * コンストラクタ
	 * @param name マクロ名
	 */
	public Macro(String name)
	{
		this.name = name;
	}

	/**
	 * マクロにタグを追加
	 * @param str
	 */
	public void addMacro(String str)
	{
		array.add(str);
	}

	/**
	 * マクロからタグを一つ取得
	 * @return
	 */
	public String pop()
	{
		int i = point;
		if(point < array.size())
		{
			point++;
			return array.get(i);
		}
		else
			return null;
	}

	/**
	 * マクロの実行位置をリセット
	 */
	public void reset()
	{
		point = 0;
	}

	/**
	 * 一つ戻す
	 */
	public void back()
	{
		point--;
		if(point < 0)
			point = 0;
	}

	/**
	 * マクロのタグ数を返す
	 * @return マクロの保持するタグの数
	 */
	public int getCount()
	{
		return array.size();
	}

	/**
	 * マクロ名を返す
	 * @return マクロ名
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * マクロを保存する
	 * @throws JSONException
	 */
	public JSONArray store() throws JSONException
	{
		return new JSONArray(array);
	}

	/**
	 * マクロを復元する
	 * @param obj
	 * @throws JSONException
	 */
	public void restore(JSONArray json) throws JSONException
	{
		if(json == null) return;
		int size = json.length();
		for(int i = 0; i < size; i++)
			addMacro(json.getString(i));
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(' ');
		int size = array.size();
		for(int i = 0; i < size; i++)
		{
			sb.append(array.get(i));
			sb.append(' ');
		}
		return sb.toString();
	}
}


/**
 * サブルーチン用クラス。サブルーチン呼び出しと復帰に必要なデータを保持します。
 * @author okayu
 */
class CallData
{
	/** 位置 */
	public int point;
	/** ファイル名 */
	public String filename;
	/** ラベル */
	public String label;
	/** 既読かどうか */
	public boolean alreadyRead;
	/** マクロ中かどうか */
	public boolean inMacro;
	/** 実行していたマクロ */
	public Macro macro;
	/** 右クリックサブルーチンの呼び出しなら true */
	public boolean rclick;
	/** リンクのロック状況 */
	public boolean locklink;
}




