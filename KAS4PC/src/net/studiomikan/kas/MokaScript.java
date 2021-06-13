package net.studiomikan.kas;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import net.studiomikan.kas4pc.klib.Context;
import net.studiomikan.moka.MOKA_VALUE_TYPE;
import net.studiomikan.moka.Moka;
import net.studiomikan.moka.MokaValue;

/**
 * KAS システムと Moka スクリプトとの橋渡し
 * @author okayu
 *
 */
public class MokaScript
{
	/** Moka スクリプト */
	public Moka moka;
	/** sfを保存するファイル名 */
	public static final String sfVariableFileName = "datasu";
	/** KASのシステムを保存するファイル名 */
	public static final String kasVariableFileName = "datasc";

	/** cond の結果 false */
	public static final int COND_FALSE = 0;
	/** cond の結果 true */
	public static final int COND_TRUE = 1;
	/** cond の結果 エラー */
	public static final int COND_ERROR = -1;

	/** __kas__ および sf をロードしているか */
	public boolean loaded_kas_sf = false;

	/** sf の MokaValue */
	public MokaValue moka_sf = null;
	/** __kas__ の MokaValue */
	public MokaValue moka_kas = null;

	/** 色々な操作用 */
	private MokaValue tmpValue = new MokaValue();

	/** ConfigScript */
	public ConfigScript conf = null;

	/** タイマーで呼ぶ関数 */
	private MokaValue[] mokaTimerFunction = null;
	/** タイマーのインターバル */
	private long[] mokaTimerInterval = null;
	/** タイマーの計算用 */
	private long[] mokaTimerStartTime = null;
	/** タイマーに引数を渡すためのリスト */
	private ArrayList<MokaValue> mokaTimerArgs = new ArrayList<MokaValue>();


	/** コンストラクタ */
	public MokaScript()
	{
		moka = new Moka();
		KASFunction.define(moka);
		MokaPlugin.moka = moka;
		MokaPlugin.dicsave = moka.getFunction("dicsave");
		MokaPlugin.dicload = moka.getFunction("dicload");
		// Moka の初期設定。オプションは全部オフに
		setLeader();
		moka.flag_optimize = false;
		moka.flag_outputCode = false;
		moka.flag_outputErrorlog = false;
		moka.flag_printCode = false;
		moka.flag_printStack = false;
		moka.flag_printTree = false;
		// 変数などを用意
		moka.executeScript("var f = %[];");			// ゲーム変数
		moka.executeScript("var tf = %[];");		// 一時変数
		moka.executeScript("var sf = %[];");		// システム変数
		moka.executeScript("var __kas__ = %[];");	// KAS 変数

		loadVariableFromFile(Util.context, "__kas__", kasVariableFileName);
		loadVariableFromFile(Util.context, "sf", sfVariableFileName);
		moka.executeScript("var mp; __kas__.macro = []; __kas__.macroCount = 0;");	// マクロ用

		// それぞれの値を取得しておく
		moka_sf = execute("sf;");
		moka_kas = execute("__kas__;");
		loaded_kas_sf = true;

		// ConfigScript の準備
		conf = new ConfigScript(this);

		// タイマー用意
		mokaTimerFunction = new MokaValue[256];
		mokaTimerInterval = new long[256];
		mokaTimerStartTime = new long[256];
		for(int i = mokaTimerFunction.length-1; i >= 0; i--)
		{
			mokaTimerFunction[i] = null;
			mokaTimerInterval[i] = 0;
			mokaTimerStartTime[i] = -1;
		}
		mokaTimerArgs.clear();
	}

	/**
	 * Moka スクリプトを解放
	 */
	public void free()
	{
		moka = null;
		conf = null;
	}

	/** Moka にファイル読み込み機能を設定 */
	public void setLeader()
	{
		Moka.Leader leader = new Moka.Leader()
		{
			@Override
			public String getFileString(String filename, String encoding)
			{
				try
				{
					BufferedReader br = ResourceManager.loadScenario(filename, Util.context);
					if(br == null) return null;

					StringBuilder sb = new StringBuilder();
					char[] buf = new char[256];
					int size;

					while(true)
					{
						size = br.read(buf);
						if(size <= 0) break;
						sb.append(buf, 0, size);
					}

					br.close();
					return sb.toString();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					return null;
				}
			}
		};
		Moka.setFileStringReader(leader);
	}

//	/**
//	 * __kas__ および sf をファイルから復帰
//	 */
//	public void LoadVariable_Kas_SF()
//	{
//		if(!loaded_kas_sf)
//		{
//			loadVariableFromFile(Util.context, "__kas__", kasVariableFileName);
//			loadVariableFromFile(Util.context, "sf", sfVariableFileName);
//			moka.executeScript("var mp; __kas__.macro = []; __kas__.macroCount = 0;");	// マクロ用
//			// それぞれの値を取得しておく
//			moka_sf = execute("sf;");
//			moka_kas = execute("__kas__;");
//			loaded_kas_sf = true;
//		}
//	}

	/**
	 * 実行前にスクリプトを整える
	 * @param script スクリプト
	 * @return 整形後のスクリプト
	 */
	private String trim(String script)
	{
		if(!script.endsWith(";"))
			script += ";";
		return script;
	}

	/**
	 * 実行し、スタックトップを得る。
	 * @param script スクリプト
	 * @return スタックトップ　実行失敗した場合は null
	 */
	public MokaValue execute(String script)
	{
		if(script == null) return null;
		script = trim(script);
		moka.flag_optimize = false;
		boolean result = moka.executeScript(script);
		if(result)
			return new MokaValue(moka.getStackTop());
		else
			return null;
	}

	/**
	 * eval<br>
	 * 実行し、結果は捨てる
	 * @param script スクリプト
	 * @return 正常終了で true エラーがあった場合は false
	 */
	public boolean eval(String script)
	{
		if(script == null) return true;
		script = trim(script);
		moka.flag_optimize = true;
		boolean result = moka.executeScript(script);
		return result;
	}

	/**
	 * cond 属性を実行<br>
	 * 実行し、結果の真偽を判定する。script が無い場合は真とする。
	 * @param script スクリプト
	 * @return false なら 0、true なら 1、エラーなら -1
	 */
	public int cond(String script)
	{
		if(script == null) return 1;
		script = trim(script);
		moka.flag_optimize = false;	// 式の削除を防ぐために最適化をしない
		boolean result = moka.executeScript(script);
		if(result)
		{
			MokaValue cond = moka.getStackTop();
			if(cond.cast(MOKA_VALUE_TYPE.BOOLEAN))
			{
				if(cond.intValue != 0) return 1;
				else return 0;
			}
			else
			{
				// Moka言語の仕様としては、真偽値であるべきところに
				// それ以外の値が来るのはエラーだが、
				// TJS2 との互換のためにここは false を返すようにする
				return 0;
			}
		}
		return -1;
	}

	/**
	 * emb<br>
	 * 実行し、結果を文字列に変換して返す。エラーの場合は空文字が返る。
	 * @param script スクリプト
	 * @return 評価結果を文字列にしたもの
	 */
	public String emb(String script)
	{
		if(script == null) return "";
		script = trim(script);
		moka.flag_optimize = false;	// 式の削除を防ぐために最適化をしない
		boolean result = moka.executeScript(script);
		if(result)
		{
			MokaValue emb = moka.getStackTop();
			return emb.toString();
		}
		else
			return "";
	}

	/**
	 * 変数をファイルから復帰
	 * @param context コンテキスト
	 * @param var 変数 f, tf, sf, __kas__ のいずれか
	 * @param filename ファイル名
	 */
	public void loadVariableFromFile(Context context, String var, String filename)
	{
		// var が f の時のスクリプトはこんなの
		//
		// __kas__.tmp = fread('datasc');
		// if(__kas__.tmp != null) f = dicload(__kas__.tmp);
		// __kas__.tmp = null;
		//
		String script =
				"__kas__.tmp = fread('" + filename + "');" +
				"if(__kas__.tmp !== null) " + var + "= dicload(__kas__.tmp);" +
				"__kas__.tmp = void;";

		boolean result = moka.executeScript(script);
		if(!result)
		{
			Util.log("変数のファイルからの復元に失敗：" + var + " <- " + filename);
			moka.executeScript(var + "=%[];");	// f=%[];
		}
	}

	/**
	 * 変数を復帰
	 * @param var 変数 f, tf, sf, __kas__ のいずれか
	 * @param data 復帰元データ
	 */
	public void loadVariable(String var, String data)
	{
		moka.flag_optimize = false;

		// __kas__.tmp に data の文字列を格納する
		tmpValue.assign(data);
		moka_kas.addDicValue2("tmp", tmpValue);

		// その上で以下のスクリプトを呼ぶ
		//
		// if(__kas__.tmp !== null) f = dicload(__kas__.tmp);
		// __kas__.tmp = null;
		//
		String script =
				"if(__kas__.tmp !== null) " + var + "= dicload(__kas__.tmp);" +
				"__kas__.tmp = void;";

		boolean result = moka.executeScript(script);
		if(!result)
		{
			Util.log("変数のデータからの復元に失敗：" + var);
			moka.executeScript(var + "=%[];");	// f=%[];
		}
	}

	/**
	 * 変数をファイルに保存
	 * @param context コンテキスト
	 * @param var 変数 f, tf, sf, __kas__ のいずれか
	 * @param filename ファイル名
	 */
	public void saveVariable2File(Context context, String var, String filename)
	{
		String data = saveVariable(var);
		if(data != null)
			Util.str2file(context, data, filename);
	}

	/**
	 * 指定された変数（辞書配列）を保存用文字列に変換
	 * @param var 変数 f, tf, sf, __kas__ のいずれか
	 * @return 保存用文字列　失敗した場合は null
	 */
	public String saveVariable(String var)
	{
		moka.flag_optimize = false;
		// dicsave(f);
		String script = "dicsave(" + var + ");";
		boolean result = moka.executeScript(script);
		if(result)
		{
			MokaValue value = moka.getStackTop();
			if(value.type == MOKA_VALUE_TYPE.STRING)
				return value.stringValue();
		}
		Util.log("辞書配列の保存に失敗。");
		return null;
	}

	/**
	 * タイマーを追加
	 * @param func 呼び出す関数
	 * @param interval インターバル
	 * @param nowTime 現在時間
	 * @return 管理番号。追加失敗時は -1
	 */
	public int addMokaTimer(MokaValue func, MokaValue interval, long nowTime)
	{
		if(func.type != MOKA_VALUE_TYPE.FUNCTION)
			return -1;

		int i;
		int len = mokaTimerFunction.length;
		for(i = 0; i < len; i++)
		{
			if(mokaTimerFunction[i] == null)
				break;
		}
		if(i == len) return -1;

		mokaTimerFunction[i] = new MokaValue(func);
		mokaTimerInterval[i] = (int)interval.intValue;
		mokaTimerStartTime[i] = nowTime;

		return i;
	}

	/**
	 * タイマー処理
	 * @param nowTime 現在時刻
	 */
	public void mokaTimer(long nowTime)
	{
		int len = mokaTimerFunction.length;
		if(len == 0) return;

		long elapsed;

		for(int i = 0; i < len; i++)
		{
			if(mokaTimerFunction[i] == null) continue;
			if(mokaTimerStartTime[i] < 0) continue;

			elapsed = nowTime - mokaTimerStartTime[i];
			if(elapsed >= mokaTimerInterval[i])
			{
				moka.execFunction(mokaTimerFunction[i], mokaTimerArgs);
				mokaTimerStartTime[i] = nowTime;
			}
		}
	}

	/**
	 * タイマー消去
	 * @param num 管理番号
	 */
	public void deleteMokaTimer(int num)
	{
		if(num < 0 || num >= mokaTimerFunction.length) return;
		mokaTimerFunction[num].clear();
		mokaTimerFunction[num] = null;
		mokaTimerInterval[num] = 0;
		mokaTimerStartTime[num] = -1;
	}

}
