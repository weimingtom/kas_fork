package net.studiomikan.kas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

/**
 * 汎用ごった煮クラス<br>
 * 汎用メソッドを持つ<br>
 * ゲーム全体で共有したい情報を持つ<br>
 * @author okayu
 */
public class Util
{
	/** デバッグモード */
	public static final boolean debug = false;

	/** Moka スクリプト */
	private static MokaScript moka = null;

	/** KAS のバージョン(MainSurfaceViewにて指定される) */
	public static String systemVersion = "undefined system version.";
	/** バージョン情報の文字列(setversionタグで指定される) */
	public static String gameVersion = "unknown version";
	/** ゲームのタイトル(titleタグで指定される) */
	public static String gameTitle = "KAS";
	/** パッケージ名。MainActivity 生成時に代入される */
	public static String packageName = "";

	/** 終了時に尋ねるかどうか */
	public static boolean ask_exit = true;
	/** 存在しないタグをエラーにするかどうか */
	public static boolean notExistTag = true;

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
	public static Context context = null;
	/** メインアクティビティ */
	public static MainActivity mainActivity = null;
	/** メインサーフェイスビュー */
	public static MainSurfaceView mainView = null;
	/** セーブ＆ロードアクティビティ */
	public static SaveActivity saveActivity = null;
	/** UI スレッド呼び出し用のハンドラ */
	public static Handler handler = null;
	/** トーストメッセージ */
	static private Toast toastLog = null;
	/** エラーメッセージ */
	static private String errorMessage = "";

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
	/** 暗号化用キー */
	public static int xorKey = 0x36;
	/** データを圧縮するかどうかのフラグ */
	public static boolean compress = true;

	/** データのあるディレクトリ名 デフォルトはパッケージ名 */
	public static String rootDirectoryPath = "";
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
			rootDirectoryPath = "/sdcard/" + packageName;
		else
			rootDirectoryPath = "/sdcard/" + rootDirectoryPath_user;
		mMakeDir(new File(rootDirectoryPath));
		Util.log("ルートディレクトリ：" + rootDirectoryPath);
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
		Util.log("basePointX2="+basePointX2 + " basePointY2="+basePointY2);
		drawAreaRight2 = basePointX2 + standardDispWidth;
		drawAreaBottom2 = basePointY2 + standardDispHeight;
		Util.log("drawAreaRight2="+drawAreaRight2 + " drawAreaBottom2="+drawAreaBottom2);
	}

	/**
	 * タイトルを変更
	 * @param title
	 */
	static public void setGameTitle(String title)
	{
		gameTitle = title;
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, baos);
		byte[] bytes = baos.toByteArray();
		Util.data2file(context, bytes, filename, true);
	}

	/**
	 * 残り時間を返す
	 * @param time 目標時間
	 * @param startTime 計測開始時間
	 * @return 残り時間
	 */
	static public final long getRestTime(int time, long startTime)
	{
		return time - (SystemClock.elapsedRealtime() - startTime);
	}

	/**
	 * 経過時間を返す
	 * @param startTime 開始時間
	 * @return 経過時間
	 */
	static public final long getElapsedTIme(long startTime)
	{
		return SystemClock.elapsedRealtime() - startTime;
	}

	/**
	 * 現在の時間を返す
	 * @return 現在の時間
	 */
	static public final long getNowTime()
	{
		return SystemClock.elapsedRealtime();
	}

	/**
	 * ネットワークにつながっているか
	 * @param context コンテキスト
	 * @return 繋がっているなら true ないなら false
	 */
	public static boolean isConnectedNetwork(Context context)
	{
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null)
            return cm.getActiveNetworkInfo().isConnected();
        return false;
	}

	/**
	 * トーストメッセージを表示
	 * @param message メッセージ
	 */
	static public final void toastMessage(String message)
	{
		if(toastLog != null)
			toastLog.cancel();
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
		toastLog = toast;
	}

	/**
	 * ダイアログメッセージ メインスレッド(Activityのスレッド)以外から呼ぶと落ちる
	 * @param title タイトル
	 * @param message メッセージ
	 */
	static public final void dialogMessage(String title, String message)
	{
		if(mainActivity == null) return;
		AlertDialog.Builder ad;
		ad = new AlertDialog.Builder(mainActivity);
		ad.setTitle(title);
		ad.setMessage(message);
		ad.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				if(mainActivity != null)
					mainActivity.setResult(Activity.RESULT_OK);
			}
		});
		ad.create();
		ad.show();
	}

	/**
	 * ダイアログを出してそのまま終了
	 * @param title タイトル
	 * @param message メッセージ
	 */
	static public final void dialogMessage2(String title, String message)
	{
		if(mainActivity == null) return;
		AlertDialog.Builder ad;
		ad = new AlertDialog.Builder(mainActivity);
		ad.setTitle(title);
		ad.setMessage(message);
		ad.setPositiveButton("終了", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				if(mainActivity != null)
				{
					mainActivity.setResult(Activity.RESULT_OK);
					Util.closeGame();
				}
			}
		});
		mainActivity.exit = true;
		ad.setCancelable(false);
		ad.create();
		ad.show();
	}

	/**
	 * ダイアログ 実行メソッド指定
	 * @param title タイトル
	 * @param message メッセージ
	 * @param cancelable キャンセル可能か
	 * @param yes yes ボタンの文字
	 * @param no no ボタンの文字
	 * @param onYesClick yes ボタンクリック時の動作
	 * @param onNoClick no ボタンクリック時の動作
	 */
	static public final void dialogMessage(
			String title,
			String message,
			boolean cancelable,
			String yes,
			String no,
			DialogInterface.OnClickListener onYesClick,
			DialogInterface.OnClickListener onNoClick )
	{
		if(mainActivity == null) return;
		AlertDialog.Builder ad;
		ad = new AlertDialog.Builder(mainActivity);
		ad.setTitle(title);
		ad.setMessage(message);
		ad.setPositiveButton(yes, onYesClick);
		ad.setNegativeButton(no, onNoClick);
		ad.setCancelable(cancelable);
		ad.create();
		ad.show();
	}

	/**
	 * エラーメッセージを表示し、そのまま終了
	 * @param message
	 */
	static public final void error(String message)
	{
		errorMessage = message;
		if(mainView != null)
			mainView.stopConductor();
		log("Error! " + message);
		doWithHandler(new Runnable()
		{
			@Override
			public void run()
			{
				Util.dialogMessage2("Error!", errorMessage);
			}
		});
	}

	/**
	 * ハンドラを介してスレッドを実行
	 * @param thread
	 */
	public static void doWithHandler(Runnable runnable)
	{
		if(handler != null) handler.post(runnable);
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
			android.util.Log.d("KAS ", message);
	}

	/**
	 * ゲーム終了
	 */
	static public final void closeGame()
	{
		if(mainView != null)
			mainView.inTheEnd = true;
		if(mainActivity != null)
		{
			mainActivity.exit = true;
			mainActivity.finish();
		}
	}

	/**
	 * ダイアログを出してゲーム終了
	 * @param ask ダイアログを出すかどうか
	 */
	static public final void closeGameWithAsk(boolean ask)
	{
		if(ask)
		{
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
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
			};
			Util.doWithHandler(runnable);
		}
		else
			closeGame();
	}

	/**
	 * セーブ or ロード画面を呼び出し
	 * @param mode "save" でセーブ画面、 "load" でロード画面呼び出し
	 */
	static public final void callSaveActivity(String mode)
	{
		Intent intent = new Intent(mainActivity, SaveActivity.class);
		// インテントへのパラメータ設定
		if(mode == null || mode.equals("save")) intent.putExtra("text", "save");
		else intent.putExtra("text", "load");
		intent.putExtra("unlocklink", "true");
		mainActivity.startActivityForResult(intent, 0);
	}

	/**
	 * 設定画面を呼び出し
	 */
	static public final void callConfigActivity()
	{
		Intent intent = new Intent(mainActivity, ConfigActivity.class);
		ConfigActivity.setValue();	// 設定
		mainActivity.startActivityForResult(intent, 0);
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
	 * @return 日付
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
	 * SD カード内部のファイルを削除する
	 * @param filename
	 */
	static public void deleteFile(String filename)
	{
		if(filename == null || filename.length() == 0) return;
		File file = new File(rootDirectoryPath + "/" + filename);
		if(file.exists())
			file.delete();
		return;
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
		data2file(context, str.getBytes(), filename);
	}

	/**
	 * ファイル→文字列
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return ファイル内容
	 */
	static public final String file2str(Context context, String filename)
	{
		byte[] w = file2data(context, filename);
		if(w == null)
			return null;
		return new String(w);
	}

	/**
	 * バイトデータ→ファイル
	 * @param context コンテキスト
	 * @param w データ
	 * @param filename ファイル名
	 */
	static public final void data2file(Context context, byte[] w, String filename)
	{
		data2file(context, w, filename, false);
	}

	/**
	 * バイトデータ→ファイル
	 * @param context コンテキスト
	 * @param w データ
	 * @param filename ファイル名
	 * @param asis true なら圧縮や暗号化をしない。true で保存したファイルは file2data で読み込めないので注意
	 */
	static public final void data2file(Context context, byte[] w, String filename, boolean asis)
	{
		OutputStream out = null;

		// sdカードの存在確認
		String sdState = Environment.getExternalStorageState();
		boolean sdMouted = sdState.equals(Environment.MEDIA_MOUNTED);

		// 暗号化
		if(!asis && encrypt)
			w = encode(w);

		try
		{
			// 出力ストリームのオープン
			if(sdMouted)
			{
				String dir = rootDirectoryPath + "/";
				String path = dir  + filename;
				File outFile = new File(path);
				File dirFile = new File(dir);
				if(!dirFile.exists())
					mMakeDir(dirFile);
				out = new FileOutputStream(outFile);
			}
			else
				out = context.openFileOutput(filename, Context.MODE_PRIVATE);
			// 配列の書き込み
			if(!asis && compress)
				KZipUtil.compress(w, out);
			else
				out.write(w, 0, w.length);
			// 閉じる
			out.close();
		}
		catch (IOException e)
		{
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
		// sdカードの存在確認
		String sdState = Environment.getExternalStorageState();
		boolean sdMouted = sdState.equals(Environment.MEDIA_MOUNTED);

		if(sdMouted)
		{
			String dir = rootDirectoryPath + "/";
			String path = dir  + filename;
			File inFile = new File(path);
			return inFile.exists();
		}
		else
			return false;
	}

	/**
	 * ファイル→バイトデータ
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return バイトデータ
	 */
	static public final byte[] file2data(Context context, String filename)
	{
		int size;
		byte[] w = new byte[1024];
		InputStream in = null;

		ByteArrayOutputStream out = null;

		// sdカードの存在確認
		String sdState = Environment.getExternalStorageState();
		boolean sdMouted = sdState.equals(Environment.MEDIA_MOUNTED);

		try
		{
			// ストリームを開く
			if(sdMouted)	// sdカードから
			{
				String dir = rootDirectoryPath + "/";
				String path = dir + filename;
				File inFile = new File(path);
				in = new FileInputStream(inFile);
			}
			else
				in = context.openFileInput(filename);

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
				ex[0] = ex[0].toLowerCase();
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

}
