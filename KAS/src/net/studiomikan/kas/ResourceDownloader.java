package net.studiomikan.kas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

/**
 * ResourceDownloader クラス<br>
 * バックグラウンドでファイルをダウンロードするクラス
 * @author okayu
 */
class ResourceDownloader extends AsyncTask<String, Integer, String> implements OnCancelListener
{
	/** メインアクティビティへの参照 */
	private MainActivity activity;
	/** ダウンロードするファイル名 */
	private String netResourceName;
	/** ダウンロードするファイルパス */
	private String netResourceUrl;
	/** ダウンロードするファイルのサイズ */
	private long netResourceFileSize;

	/** バッファサイズ */
	private static final int bufferSize = 4096;
	/** プログレスダイアログ */
	public ProgressDialog dialog = null;
	/** HTTP コネクション */
	private HttpURLConnection httpConnect = null;
	/** HTTP ストリーム */
	private InputStream httpIn = null;
	/** アウトプットストリーム */
	private FileOutputStream httpOut = null;
	/** 出力ファイル */
	private File outputFile = null;

	/** ダウンロード中かどうか */
	public boolean downloading = false;
	/** ループ時のフラグ */
	private boolean flag = false;

	/** レジューム時のスキップサイズ */
	private long skipsize = 0;

	/**
	 * コンストラクタ
	 * @param activity メインのアクティビティ
	 * @param netResourceName ダウンロードするファイル
	 * @param netResourceUrl ダウンロードするファイルの URL
	 * @param filesize ファイルサイズ
	 */
	public ResourceDownloader(MainActivity activity, String netResourceName, String netResourceUrl, long filesize)
	{
		this.activity = activity;
		this.netResourceName = netResourceName;
		this.netResourceUrl = netResourceUrl;
		this.netResourceFileSize = filesize;
	}

	/**
	 * ダウンロードしたファイルを取得
	 * @return ダウンロードしたファイル
	 */
	public File getFile()
	{
		return outputFile;
	}

	/**
	 * プログレスバー準備
	 */
	private void createProgressDialog()
	{
		dialog = new ProgressDialog(activity);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setCancelable(true);	// キャンセル可能
		dialog.setTitle("ゲームデータのダウンロード");	// タイトル
		dialog.setMax((int)(netResourceFileSize / 1024));
		dialog.setButton(
				DialogInterface.BUTTON_NEGATIVE,
				"キャンセル",
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.cancel();
					}
				}
		);
		dialog.setOnCancelListener(this);
		dialog.show();
	}

	/**
	 * プログレスバー破棄
	 */
	public void deleteProgressDialog()
	{
		if(dialog != null)
		{
			dialog.dismiss();
			dialog = null;
		}
	}

	/**
	 * ポーズ
	 */
	public void onPause()
	{
		deleteProgressDialog();
	}

	/**
	 * 復帰
	 * @param activity メインのアクティビティ
	 */
	public void onResume(MainActivity activity)
	{
		this.activity = activity;
		createProgressDialog();
	}

	/**
	 * 実行前(UIスレッド)
	 */
	@Override
	protected void onPreExecute()
	{
		// HTTP接続
		URL url;
		try
		{
			url = new URL(netResourceUrl);
			httpConnect = (HttpURLConnection)url.openConnection();
			httpConnect.setRequestMethod("GET");
			if(skipsize > 0)
			{
				httpConnect.setRequestProperty("Range",
				        String.format (
				                "bytes=%d-%d",
				                skipsize,
				                netResourceFileSize
				        )
				);
			}
			httpConnect.connect();
//			System.out.println("返答：" + httpConnect.getResponseCode());
			if(httpConnect.getResponseCode() == HttpURLConnection.HTTP_OK || httpConnect.getResponseCode() == HttpURLConnection.HTTP_PARTIAL)
			{
				httpIn = httpConnect.getInputStream();
				Util.log("ファイルサイズ：" + httpConnect.getContentLength());
				String filePath = Util.rootDirectoryPath + "/" + netResourceName;
				outputFile = new File(filePath);
				if(skipsize > 0)
					httpOut = new FileOutputStream(outputFile, true);
				else
					httpOut = new FileOutputStream(outputFile, false);
				Util.mMakeDir(outputFile);
			}
			else
			{
				httpConnect = null;
				httpIn = null;
				httpOut = null;
				return;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			httpConnect = null;
			httpIn = null;
			httpOut = null;
			return;
		}
		// プログレスバー準備
		createProgressDialog();
	}

	/**
	 * 実行(別スレッド)
	 */
	@Override
	protected String doInBackground(String... arg0)
	{
		if(httpConnect == null || httpIn == null || httpOut == null) return "notconnected";

		downloading = true;
		int size;
		long sum = 0;
		byte[] w = new byte[bufferSize];
		try
		{
			if(skipsize > 0) sum = skipsize;
			flag = true;
			while(flag)
			{
				size = httpIn.read(w);
				if(size <= 0)
					break;
				sum += size;
				httpOut.write(w, 0, size);
	            publishProgress((int)(sum / 1024));	// プログレスバーへ進捗状況を設定
			}
            publishProgress(dialog.getMax());
			// クローズ
            close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return "disconnected";
		}

		// キャンセルでループを抜けた
		if(!flag)
			return "cancel";

		// 容量チェック
		if(outputFile.length() == netResourceFileSize)
			return "success";
		else
			return "Illegalsize";
	}

	/**
	 * 終了する
	 */
	private void close()
	{
		try
		{
			httpOut.close();
			httpIn.close();
			httpConnect.disconnect();
			if(Util.mainActivity != null && !Util.mainActivity.pausing)
				dialog.dismiss();
			downloading = false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * プログレスバーのアップデート(UIスレッド)
	 */
	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		if(dialog != null)
			dialog.setProgress(progress[0]);
	}

	/**
	 * 後処理(UIスレッド)
	 */
	@Override
	protected void onPostExecute(String result)
	{
		Util.log("onPostExecute:" + result);
		deleteProgressDialog();
		if(result.equals("success"))	// 成功
		{
			activity.onDownloaded();
		}
		else if(result.equals("cancel"))	// キャンセル
		{
			Util.dialogMessage2(Util.gameTitle, "ゲームデータのダウンロードを中止しました。");
		}
		else if(result.equals("notconnected"))	// 接続に失敗
		{
			Util.dialogMessage2("エラー", "ネットワークの接続に失敗しました。終了します。");
		}
		else if(result.equals("Illegalsize"))	// サイズが不正
		{
			Util.dialogMessage2("エラー", "ファイルのダウンロードに失敗しました。終了します。");
			Util.log("Illegalsize:" + outputFile.length());
			if(outputFile.exists())
				outputFile.delete();
		}
		else	// ダウンロードに失敗
		{
			if(outputFile.exists())
				outputFile.delete();
			Util.dialogMessage2("エラー", "ファイルのダウンロードに失敗しました。終了します。");
		}
	}

	/**
	 * ダウンロードするファイルの名前を返す
	 * @return
	 */
	public String getNetResourceName()
	{
		return netResourceName;
	}

	/**
	 * skipsize を設定する
	 * @param skipsize
	 */
	public void setSkipsize(long skipsize)
	{
		this.skipsize = skipsize;
	}

	@Override
	public void onCancel(DialogInterface arg0)
	{
		flag = false;
		Util.log("*onResourceDownloader Canselled");
	}

}
