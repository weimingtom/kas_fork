package net.studiomikan.kas;


import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Calendar;

import net.studiomikan.kas4pc.gui.AndroidButton;
import net.studiomikan.kas4pc.gui.ConfigFrame;
import net.studiomikan.kas4pc.gui.KGUIUtil;
import net.studiomikan.kas4pc.gui.MainActivity;
import net.studiomikan.kas4pc.gui.MainFrame;
import net.studiomikan.kas4pc.gui.MainPanel;
import net.studiomikan.kas4pc.gui.MenuBar;
import net.studiomikan.kas4pc.gui.MenuFrame;
import net.studiomikan.kas4pc.gui.SaveFrame;
import net.studiomikan.kas4pc.klib.Bitmap;
import net.studiomikan.kas4pc.klib.Context;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 汎用ごった煮クラス
 * @author okayu
 */
public class Util
{
	/** デバッグモード */
	public static final boolean debug = true;

	/** Moka スクリプト */
	private static MokaScript moka = null;

	/** KAS のバージョン(MainSurfaceViewにて指定される) */
	public static String systemVersion = "undefined system version.";
	/** バージョン情報の文字列(setversionタグで指定される) */
	public static String gameVersion = "unknown version";
	/** ゲームのタイトル(titleタグで指定される) */
	public static String gameTitle = "KAS";

	/** 描画の fps */
	public static final int fps = 60;

	/** 終了時に尋ねるかどうか */
	public static boolean ask_exit = true;
	/** 存在しないタグをエラーにするかどうか */
	public static boolean notExistTag = true;
	/** エラーメッセージ */
	static public String errorMessage;

	/** 標準の解像度　この解像度を基準として制作 */
	public static int standardDispWidth = 800;
	/** 標準の解像度　この解像度を基準として制作 */
	public static int standardDispHeight = 450;
	/** ディスプレイの実際の解像度 */
	public static int dispWidth = 800;
	/** ディスプレイの実際の解像度 */
	public static int dispHeight = 450;
	/** 拡大率 */
	public static double dispRate = 1.0;
	/** 拡大率の逆数 */
	public static double dispRate2 = 1.0;
	/** 拡大を適用した後のサイズ standardDispX * dispRate */
	public static int drawAreaWidth = 800;
	/** 拡大を適用した後のサイズ standardDispX * dispRate */
	public static int drawAreaHeight = 450;
	/** 描画位置　左上 */
	public static int basePointX = 0;
	/** 描画位置　左上 */
	public static int basePointY = 0;
	/** 描画領域の右端 */
	public static int drawAreaRight = 800;
	/** 描画領域の下端 */
	public static int drawAreaBottom = 450;
	/** 描画開始位置　拡大せずに表示する時 */
	public static int basePointX2 = 0;
	/** 描画開始位置　拡大せずに表示する時 */
	public static int basePointY2 = 0;
	/** 描画領域の右端　拡大せずに表示する時 */
	public static int drawAreaRight2 = 800;
	/** 描画領域の下端　拡大せずに表示する時 */
	public static int drawAreaBottom2 = 450;
	/** フルスクリーン */
	public static boolean fullscreen = true;

	/** 画面揺らしの補正 */
	public static int quakeX = 0;
	/** 画面揺らしの補正 */
	public static int quakeY = 0;

	/** 画像の読み込み拡大率　一時保存場所（再起動後に有効にするため） */
	public static float imageScale = 1f;

	/** コンテキスト */
	public static Context context;
	/** メインアクティビティ */
	public static MainActivity mainActivity = null;
	/** メインフレーム */
	public static MainFrame mainFrame = null;
	/** メインパネル */
	public static MainPanel mainPanel = null;
	/** アンドロイドキーなど */
	public static AndroidButton androidButton = null;
	/** メニューフレーム */
	public static MenuFrame menuFrame = null;
	/** メニューバー */
	public static MenuBar menuBar = null;
	/** メインビュー */
	public static MainSurfaceView mainView = null;
	/** セーブ＆ロード画面 */
	public static SaveFrame saveView = null;
	/** コンフィグ画面 */
	public static ConfigFrame configView = null;

	/** セーブデータの最大値 */
	static public int saveNumMax = 20;
	/** セーブデータ受け渡し用 */
	public static String latestSaveDate = "--/--/-- --:--:--";
	/** セーブデータ受け渡し用 */
	public static String latestSaveText = "no data";
	/** セーブデータのファイル名のプレフィックス */
	public static String saveDataPrefix = "savedata_";
	/** サムネイルのファイル名のプレフィックス */
	public static String thumbnailPrefix = "thumbnail_";
	/** サムネイルの幅 */
	public static int thumbnailWidth = 128;
	/** サムネイルを保存するかどうか */
	public static boolean saveThumbnail = false;
	/** マクロをセーブデータに保存するかどうか */
	public static boolean saveMacros = true;
	/** データを暗号化するかどうかのフラグ */
	public static boolean encrypt = false;
	/** データを圧縮するかどうかのフラグ */
	public static boolean compress = true;

	/** データのあるディレクトリ名 デフォルトはパッケージ名 */
	public static String rootDirectoryPath = "data";
	/** ユーザの指定したディレクトリ名　空文字にするとパッケージ名になる */
	public static String rootDirectoryPath_user = "";

	/** メニューボタンの動作 true ならオプションメニュー、それ以外ならサブルーチン呼び出し */
	public static boolean builtInOptionMenu = true;
	/** オートレジュームの有効無効 */
	public static boolean enableAutoResume = true;

	/**
	 * ルートディレクトリの設定
	 * @param packageName パッケージ名
	 */
	static public final void setRootDirctory(String packageName)
	{
		if(rootDirectoryPath_user.length() == 0)
			rootDirectoryPath = packageName;
		else
			rootDirectoryPath = rootDirectoryPath_user;
		mMakeDir(new File(rootDirectoryPath));
		Util.log("Root Directory：" + rootDirectoryPath);
		System.out.println("Root Directory：" + rootDirectoryPath);
	}

	/**
	 * ディスプレイサイズと拡大率
	 * @param width ディスプレイの幅
	 * @param height ディスプレイの高さ
	 */
	static public final void setDispSize(int width, int height)
	{
		dispWidth = width;
		dispHeight = height;
		Util.log("DISPWIDTH=" + dispWidth + " DISPWIDTH" + dispHeight);

		double widthRate = (double)dispWidth / (double)standardDispWidth;
		double heightRate = (double)dispHeight / (double)standardDispHeight;
		Util.log("WIDTHRAGE=" + widthRate + " HEIGHTRATE" + heightRate);
		if(widthRate <= heightRate)
			dispRate = widthRate;
		else
			dispRate = heightRate;

		dispRate2 = 1.0/dispRate;

		drawAreaWidth = (int)(standardDispWidth * dispRate);
		drawAreaHeight = (int)(standardDispHeight * dispRate);
		Util.log("drawWidth=" + drawAreaWidth + " drawHeight:" + drawAreaHeight);

		int centerX = dispWidth / 2;
		int centerY = dispHeight / 2;

		basePointX = centerX - (drawAreaWidth/2);
		basePointY = centerY - (drawAreaHeight/2);
		Util.log("basePointX="+basePointX + " basePointY="+basePointY);
		drawAreaRight = basePointX + drawAreaWidth;
		drawAreaBottom = basePointY + drawAreaHeight;
		Util.log("drawAreaRight="+drawAreaRight + " drawAreaBottom="+drawAreaBottom);

		basePointX2 = centerX - (standardDispWidth/2);
		basePointY2 = centerY - (standardDispHeight/2);
		Util.log("basePointX="+basePointX2 + " basePointY="+basePointY2);
		drawAreaRight2 = basePointX2 + standardDispWidth;
		drawAreaBottom2 = basePointY2 + standardDispHeight;
		Util.log("drawAreaRight="+drawAreaRight2 + " drawAreaBottom="+drawAreaBottom2);
	}

	/**
	 * タイトルを変更
	 * @param title
	 */
	static public void setGameTitle(String title)
	{
		gameTitle = title;
		mainFrame.setTitle(title);
	}

	/**
	 * Moka スクリプトの実行器を返す
	 * @return
	 */
	static public MokaScript getMokaScript()
	{
		if(moka == null)
			moka = new MokaScript();
		return moka;
	}

	/**
	 * Moka スクリプトを解放する
	 */
	static public void freeMokaScript()
	{
		if(moka != null)
		{
			moka.free();
			moka = null;
		}
	}

	/**
	 * Bitmap を保存する
	 * @param bitmap
	 * @param filename
	 */
	static public final void saveBitmap(Bitmap bitmap, String filename)
	{
		String dir = rootDirectoryPath + "/";
		String path = dir + filename;
		File dirFile = new File(dir);
		if(!dirFile.exists())
			Util.mMakeDir(dirFile);
		System.out.println("filename:" + filename);
		System.out.println("dir:" + dirFile.getPath());
		System.out.println("path:" + path);
		bitmap.saveAsPng(path);
	}

	/**
	 * ハンドラを介してスレッドを実行
	 * @param thread
	 */
	public static final void doWithHandler(Runnable runnable)
	{
		// 別に必要がないので、いきなり実行する
		Thread thread = new Thread(runnable);
		thread.start();
		try
		{
			thread.join();
		} catch (InterruptedException e) { e.printStackTrace(); }
	}

	/**
	 * セーブ or ロード画面を呼び出し
	 * @param mode "save" でセーブ画面、 "load" でロード画面呼び出し
	 */
	public static final void callSaveActivity(String mode)
	{
		if(Util.saveView != null)
		{
			Util.saveView.setMode(mode);
			Util.saveView.setVisible(true);
		}
	}

	/**
	 * 設定画面を呼び出し
	 */
	public static final void callConfigActivity()
	{
		if(Util.configView != null)
			Util.configView.setVisible(true);
	}

	/**
	 * シナリオの再読み込みを行う
	 */
	public static final void reloadScenario()
	{
		System.out.println("シナリオを再読込-----------------------------------------------");
		mainView.conductor.reset();
		mainView.conductor.jumpScenario(mainView.conductor.getFileName(), null);
		mainView.gameState = MainSurfaceView.GAMESTATE.DEFAULT;
		mainView.conductor.startConductor();
	}

	/**
	 * セーブデータの情報を取得
	 * @param num セーブ番号
	 * @return 保存データ
	 * @throws JSONException
	 */
	static public final JSONObject getSavedataData(int num) throws JSONException
	{
		if(!saveDataExistence(num)) return null;
		String buff = file2str(context, getSaveDataFileNameAtNum(num));
		if(buff == null) return null;
		return new JSONObject(buff);
	}

	/**
	 * セーブファイルの日付取得
	 * @param num セーブ番号
	 * @return 日付
	 * @throws JSONException
	 */
	static public final String getSaveDataDate(int num) throws JSONException
	{
		JSONObject json = getSavedataData(num);
		if(json != null && json.has("date"))
			return json.getString("date");
		else
			return "--/--/-- --:--:--";
	}

	/**
	 * セーブデータのテキスト取得
	 * @param num セーブ番号
	 * @return テキスト
	 * @throws JSONException
	 */
	static public final String getSaveDataText(int num) throws JSONException
	{
		JSONObject json = getSavedataData(num);
		if(json != null && json.has("date"))
			return json.getString("labeltext");
		else
			return "NO DATA";
	}

	/**
	 * セーブデータ番号からファイル名を得る
	 * @param num セーブデータ番号
	 * @return
	 */
	static public String getSaveDataFileNameAtNum(int num)
	{
		String filename;
		if(num < 0)
			filename = saveDataPrefix + "auto";
		else
			filename = saveDataPrefix + num;
		return filename;
	}

	/**
	 * セーブデータの存在確認
	 * @param num セーブ番号
	 * @return 存在するなら true
	 */
	static public final boolean saveDataExistence(int num)
	{
		return fileExistence(getSaveDataFileNameAtNum(num));
	}

	/**
	 * セーブデータを削除
	 * @param num 番号
	 */
	static public final void deleteSave(int num)
	{
		File file = new File(getSaveDataFileNameAtNum(num));
		if(file.exists())
			file.delete();
	}

	/**
	 * 残り時間を返す
	 * @param time 目標時間
	 * @param startTime 計測開始時間
	 * @return 残り時間
	 */
	static public final long getRestTime(int time, long startTime)
	{
		return time - ((System.nanoTime() / 1000000l) - startTime);
	}

	/**
	 * 経過時間を返す
	 * @param startTime 開始時間
	 * @return 経過時間
	 */
	static public final long getElapsedTIme(long startTime)
	{
		return (System.nanoTime() / 1000000l) - startTime;
	}

	/**
	 * 現在の時間を返す
	 * @return 現在の時間
	 */
	static public final long getNowTime()
	{
		return System.nanoTime() / 1000000l;
	}

	/**
	 * ゲーム終了
	 */
	static public final void closeGame()
	{
		if(mainView != null)
			mainView.onEndGame();
		System.exit(0);
	}

	/**
	 * ダイアログを出してゲーム終了
	 * @param ask ダイアログを出すかどうか
	 */
	static public final void closeGameWithAsk(boolean ask)
	{
		if(ask)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					boolean ans = KGUIUtil.yesNoDialog(mainFrame, "終了しますか？", "確認");
					if(ans)
						closeGame();
				}
			}).start();
		}
		else
			closeGame();
	}


	/**
	 * 与えられた文字列が数値ならtrue
	 * @param str 文字列
	 * @return 数値なら true
	 */
	static public final boolean isFloat(String str)
	{
		if(str == null) return false;
		try { Float.parseFloat(str); return true; }
		catch(Exception e) { return false; }
	}

	/**
	 * 与えられた文字列が整数ならtrue
	 * @param str 文字列
	 * @return 整数なら true
	 */
	static public final boolean isInteger(String str)
	{
		if(str == null) return false;
		try { Integer.decode(str); return true; }
		catch(Exception e) { return false; }
	}

	/**
	 * 与えられた文字列が真偽値ならtrue
	 * @param str 文字列
	 * @return 真偽値なら true
	 */
	static public final boolean isBoolean(String str)
	{
		if(str == null) return false;
		try { Boolean.parseBoolean(str); return true; }
		catch (Exception e) { return false; }
	}

	/**
	 * 日付を返す
	 * @return 日付
	 */
	static public final String getDate()
	{
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		return year + "/" + (month + 1) + "/" + day + " " + hour + ":" + minute + ":" + second;
	}

	/**
	 * ログ出力
	 * @param message ログメッセージ
	 */
	static public final void log(String message)
	{
		if(debug)
			System.out.println(message);
	}

	/**
	 * エラー
	 * @param message エラーメッセージ
	 */
	static public final void error(String message)
	{
		errorMessage = message;
		System.out.println(errorMessage);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				KGUIUtil.okDialog(mainFrame, errorMessage, "エラー");
				System.exit(0);
			}
		}).start();
	}

	/**
	 * 現在の値を取得 レギュラー
	 * @param start 開始値
	 * @param end 終了値
	 * @param time 時間
	 * @param count 現在時間
	 * @return 値
	 */
	static public final float regular(float start, float end, int time, int count)
	{
		if(count > time)
			return end;
		if(start == end)
			return end;

		return start + ( (end-start) * ((float)count/(float)time) );
	}

	/**
	 * ディレクトリを生成
	 * @param file ファイル
	 */
	static public final void mMakeDir(File file)
	{
		if(file.exists())
			return;
		file.mkdirs();
		if(file.isFile() && file.exists())
			file.delete();
	}

	/**
	 * 暗号化解読
	 * @param data 暗号化されたデータ
	 * @return 解読されたデータ
	 */
	static public final byte[] decode(byte[] data)
	{
		return xorEncode(data);
	}

	/**
	 * 暗号化　ただの XOR
	 * @param data データ
	 * @return 暗号化されたデータ
	 */
	static public final byte[] encode(byte[] data)
	{
		return xorEncode(data);
	}

	/** 暗号化用キー */
	static public int xorKey = 0x36;
	/**
	 * バイトデータにXORをかける
	 * @param data データ
	 * @return XOR されたデータ
	 */
	static public final byte[] xorEncode(byte[] data)
	{
		int len = data.length;
		for(int i = 0; i < len; i++)
			data[i] ^= xorKey;
		return data;
	}

	/**
	 * 文字列→ファイル
	 * @param context コンテキスト
	 * @param str 文字列
	 * @param filename ファイル名
	 */
	static public final void str2file(Context context, String str, String filename)
	{
		try
		{
			data2file(str.getBytes("UTF-8"), filename);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * ファイル→文字列
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return ファイル内容
	 */
	static public final String file2str(Context context, String filename)
	{
		byte[] w = file2data(filename);
		if(w == null)
			return null;
		String str = null;
		try
		{
			str = new String(w, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			str = null;
		}
		return str;
	}

	/**
	 * バイトデータ→ファイル
	 * @param context コンテキスト
	 * @param w データ
	 * @param filename ファイル名
	 */
	static public final void data2file(byte[] w, String filename)
	{
		OutputStream out = null;

		if(encrypt)
			w = encode(w);

		try
		{
			// 出力ストリームのオープン
			String dir = rootDirectoryPath + "/";
			String path = dir + filename;
			File outFile = new File(path);
			File dirFile = new File(dir);
			if(!dirFile.exists())
				mMakeDir(dirFile);
			out = new FileOutputStream(outFile);
			// 配列の書き込み
			if(compress)
				KZipUtil.compress(w, out);
			else
				out.write(w, 0, w.length);
			// 閉じる
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			try
			{
				if(out != null)
					out.close();
			}
			catch (Exception e2) {}
		}
	}

	/**
	 * ファイルの存在確認
	 * @param filename ファイル名
	 * @return 存在するなら true
	 */
	static public final boolean fileExistence(String filename)
	{
		String dir = rootDirectoryPath + "/";
		String path = dir  + filename;
		File inFile = new File(path);
		return inFile.exists();
	}

	/**
	 * ファイル→バイトデータ
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return バイトデータ
	 */
	static public final byte[] file2data(String filename)
	{
		int size;
		byte[] w = new byte[1024];
		InputStream in = null;

		ByteArrayOutputStream out = null;

		try
		{
			// ストリームを開く
			String dir = rootDirectoryPath + "/";
			String path = dir + filename;
			File inFile = new File(path);
			if(!inFile.exists())
				return null;
			in = new FileInputStream(inFile);
			// バイト配列の書き込み
			out = new ByteArrayOutputStream();
			while(true)
			{
				size = in.read(w);
				if(size <= 0)
					break;
				out.write(w, 0, size);
			}
			out.close();
			in.close();
			// ByteArrayOutputStreamのバイト配列化
			byte[] data = out.toByteArray();
			// 暗号化解読
			if(compress)
				data = KZipUtil.decompress(data);
			if(encrypt)
				data = decode(data);
			return data;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			try
			{
				if(in != null)
					in.close();
				if(out != null)
					out.close();
			}
			catch(IOException e1){}
		}
		return null;
	}


	/**
	 * ファイルの拡張子と名前を得る
	 * @param file ファイル
	 * @param name 名前を受け取るための配列
	 * @param ex 拡張子を受け取るための配列
	 */
	public static void getExtension(File file, String[] name, String[] ex)
	{
		getExtension(file.getName(), name, ex);
	}

	/**
	 * ファイルの拡張子と名前を得る
	 * @param filename ファイル名
	 * @param name 名前を受け取るための配列
	 * @param ex 拡張子を受け取るための配列
	 */
	public static void getExtension(String filename, String[] name, String[] ex)
	{
		name[0] = filename;
		int point = name[0].lastIndexOf('.');
		if(point == -1)
		{
			ex[0] = "";
			return;
		}
		else
		{
			if(point+1 < name[0].length())
			{
				ex[0] = name[0].substring(point+1);
				name[0] = name[0].substring(0, point);
				return;
			}
			else
			{
				name[0] = name[0].substring(0, name[0].length()-1);
				ex[0] = "";
				return;
			}
		}
	}

	/**
	 * 座標変換<br>
	 * KAS 内部で言う座標と、画面上で言う座標のズレを吸収するメソッド。
	 * @param x 座標
	 * @return KAS の内部座標
	 */
	static public int getConvertedX(float x)
	{
		if(fullscreen)
		{
			if(x < basePointX || drawAreaRight < x)
				return -1;
			else
				return (int)( (x-basePointX) / dispRate);
		}
		else
		{
			if(x < basePointX2 || drawAreaRight2 < x)
				return -1;
			else
				return (int)(x - basePointX2);
		}
	}

	/**
	 * 座標変換<br>
	 * KAS 内部で言う座標と、画面上で言う座標のズレを吸収するメソッド。
	 * @param x 座標
	 * @return KAS の内部座標
	 */
	static public int getConvertedY(float y)
	{
		if(fullscreen)
		{
			if(y < basePointY || drawAreaBottom < y)
				return -1;
			else
				return (int)( (y-basePointY) / dispRate);
		}
		else
		{
			if(y < basePointY2 || drawAreaBottom2 < y)
				return -1;
			else
				return (int)(y - basePointY2);
		}
	}

	/**
	 * 指定 URL の Web サイトを開く
	 * @param url Web サイトの URL
	 */
	public static void showUrl(String url)
	{
		try
		{
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(new URI(url));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
