package net.studiomikan.kas;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;

/**
 * ResourceManager<br>
 * 画像、音声などの管理をするクラス。
 * 　SDカード内のリソースを管理する<br>
 * 　アーカイブファイル内のリソースを検索して取得する<br>
 * 　ユニバーサルトランジションのルール画像を取得する<br>
 * 　リソースの取得は、リソース->SDアーカイブ->SD生ファイルの順<br>
 * @author okayu
 */
class ResourceManager
{
	/**
	 * キャッシュ情報
	 * @author okayu
	 *
	 */
	private static class CacheData
	{
		public int count = 0;
		public int ref = 0;
		public Object obj = null;
		public CacheData(Object obj)
		{
			this.obj = obj;
			count = 1;
			ref = 1;
		}
	}

	/** ネットリソースのダウンローダ */
	private static ResourceDownloader downloader = null;
	/** ネットリソースの場所 */
	public static ArrayList<String> netResourceUrl = new ArrayList<String>();
	/** ネットリソースのファイルサイズ */
	public static long netResourceFileSize = 0;

	/** 画像ファイルの拡張子 **/
	public static final String[] EXTENSION_IMAGE = {".png", ".PNG", ".jpg", ".JPG", ".jpeg", ".JPEG"};
	/** 音声ファイルの拡張子 **/
	public static final String[] EXTENSION_SOUND = {".ogg", ".OGG", ".wav", ".wave"};
	/** 動画ファイルの拡張子 */
	public static final String[] EXTENSION_VIDEO = {".mp4", ".MP4"};

	/** SD カード内を操作したかどうか */
	private static boolean loadedSDData = false;
	/** SD カード内のディレクトリのマップ */
	private static ArrayList<String> sdDirectory = new ArrayList<String>();

	/** assets */
	private static AssetManager as = null;
	/** assets 内のファイルのマップ */
	private static HashMap<String, Boolean> assetFiles = null;

	/** キャッシュ用マップ */
	private static HashMap<String, CacheData> imagecache = new HashMap<String, CacheData>();
	/** キャッシュ用逆マップ */
	private static HashMap<Bitmap, String> cachename = new HashMap<Bitmap, String>();
	/** キャッシュ用カウンタ */
	private static int checkCacheCount = 0;
	/** メモリ最大量の目安。この量を上回りそうになったら、キャッシュを整理する。 */
	private static long cacheMemMax = 16 * 1024 * 1024;
	/** 現在のネイティブヒープ使用量 */
	private static long mem = 0;
	/** KAS のシステムで使っている画像のメモリ */
	private static long systemMem = 0;

	/** 画像読み込み時のオプション */
	private static final BitmapFactory.Options opt = new BitmapFactory.Options();

	/**
	 * ResourceManager の準備
	 * @param context コンテキスト
	 */
	public static void getReady(Context context)
	{
		// assets準備
		if(as == null)
		{
			as = context.getResources().getAssets();
			assetFiles = new HashMap<String, Boolean>();
			try
			{
				String[] files = as.list("");
				for(int i = 0; i < files.length; i++)
					assetFiles.put(files[i], true);
			}
			catch (IOException e) { e.printStackTrace(); }
		}
		// SDカードの走査
		if(!loadedSDData)
		{
			getSDData();
			loadedSDData = true;
		}
		// オプション設定
		opt.inScaled = false;
		opt.inDither = false;
		opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opt.inTempStorage = new byte[16 * 1024];
		// キャッシュ関係
		systemMem = 0;
		mem = 0;
	}

	/**
	 * ResourceManager の後処理
	 */
	public static void free()
	{
		as = null;
		assetFiles.clear(); assetFiles = null;
		loadedSDData = false;
		sdDirectory.clear();
		clearCache();
	}

	/**
	 * キャッシュクリア
	 * 全ての画像を recycle し、キャッシュの管理から外す。
	 */
	public static void clearCache()
	{
		CacheData data;
		Bitmap bitmap;
		for(Map.Entry<String, CacheData> entry : imagecache.entrySet())
		{
			data = entry.getValue();
			if(data == null) continue;
			bitmap = (Bitmap)data.obj;
			if(!bitmap.isRecycled())
			{
				bitmap.recycle();
				data.obj = null;
			}
		}
		imagecache.clear();
		cachename.clear();
		mem = systemMem;
	}

	/**
	 * システムで使用している画像のメモリを教える
	 * @param image 画像
	 */
	public static void addSystemMem(Bitmap image)
	{
		long m = image.getRowBytes() * image.getHeight();
		systemMem += m;
		mem += m;
	}

	/**
	 * キャッシュ用のメモリ量の最大値を設定
	 * この値を超えた時点で、強制的にキャッシュの整理を行います。
	 * 整理を行うだけで、この値を絶対に超えないわけではありません。
	 * 画像を 100 枚読み込んで、この値を超えたとしても、100 枚分は必ず確保されます
	 * @param max 最大値（byte）
	 */
	public static void setCacheMax(long max)
	{
		cacheMemMax = max;
	}

	/**
	 * キャッシュに追加
	 * @param bitmap 画像
	 */
	public static void addToCache(String filename, Bitmap image)
	{
		// キャッシュを整頓
		tidyUpCache();

		CacheData data = new CacheData(image);
		imagecache.put(filename, data);
		cachename.put(image, filename);
		mem += image.getHeight() * image.getRowBytes();
//		System.out.println("now memory : " + filename + " : "  + mem + "/" + cacheMemMax);
	}

	/**
	 * キャッシュを整理
	 */
	public static void tidyUpCache()
	{
		if(mem >= cacheMemMax)
		{
			// 限界値を超えているので強制的に整理
			tidyUpCacheNow(0);
		}
		else
		{
			// 10 回の読み込み動作毎の整理
			checkCacheCount++;
			if(checkCacheCount >= 10)
			{
				checkCacheCount = 0;
				tidyUpCacheNow(5);
			}
		}
	}

	/**
	 * キャッシュを整理
	 * @param border 解放する基準値
	 */
	public static void tidyUpCacheNow(int border)
	{
		CacheData data;
		Bitmap bitmap;
		for(Map.Entry<String, CacheData> entry : imagecache.entrySet())
		{
			data = entry.getValue();
			if(data == null) continue;
			bitmap = (Bitmap)data.obj;
			data.count++;
			if(bitmap.isRecycled())
				imagecache.put(entry.getKey(), null);
			else
			{
				// 参照がゼロ、かつ一定回数以上管理を通過していたら解放
				if(data.count >= border && data.ref <= 0)
				{
//					System.out.println("   free cache count:" + data.count + " filename:" + entry.getKey());
					mem -= bitmap.getHeight() * bitmap.getRowBytes();
//					System.out.println("free memory : " + bitmap.getHeight() * bitmap.getRowBytes() + " : " + cachename.get(bitmap));
					cachename.remove(bitmap);
					imagecache.put(entry.getKey(), null);
					bitmap.recycle();
					data.obj = null;
				}
			}
		}
	}

	/**
	 * キャッシュから画像を取得
	 * @param filename ファイル名
	 * @return 画像
	 */
	public static Bitmap getCacheImage(String filename)
	{
		CacheData data = imagecache.get(filename);
		if(data != null)
		{
			Bitmap bitmap = (Bitmap)data.obj;
			data.ref++;
//			System.out.println("   loadImage from cache ref=" + data.ref + " name:" + filename);
			return bitmap;
		}
		return null;
	}

	/**
	 * 画像を解放する
	 * キャッシュ管理との兼ね合いがあるので、必ずこのメソッドを使う。
	 * @param image 画像
	 */
	public static void freeImage(Bitmap image)
	{
		String name = cachename.get(image);
		if(name == null) return;
		CacheData data = imagecache.get(name);
		if(data == null) return;
		data.ref--;
	}

	/**
	 * OutOfMemoryError 時の動作
	 * @param count 何回目の試行か
	 */
	public static void onOutOfMemoryError(int count)
	{
		System.gc();
		long newMax = mem;
		if(count == 0)
		{
			// 一回目のエラー時はキャッシュの無駄な要素を整理する
			tidyUpCacheNow(0);
			System.gc();
			if(Util.mainView != null)
				Util.mainView.onOutOfMemoryError();
		}
		else if(count <= 5)
		{
			// 2 ~ 5 回目のエラーでは画像を一旦全て recycle してしまう。
			newMax = mem;
			clearCache();
			System.gc();
			if(Util.mainView != null)
				Util.mainView.onOutOfMemoryError();
		}
		else
		{
			// それ以降のエラーではメモリが足りないことを告げて終了
			Util.mainView.conductor.shutdown();
			Util.doWithHandler(new Runnable()
			{
				@Override
				public void run()
				{
					Util.dialogMessage2(Util.gameTitle, "メモリ不足により強制終了します。");
				}
			});
		}
		if(newMax < cacheMemMax)
			cacheMemMax = newMax;
	}

	/**
	 * URL 文字列からファイル名を取得
	 * @param url URL 文字列
	 * @return ファイル名
	 */
	public static final String getFileNameFromURLString(String url)
	{
		int pos = url.lastIndexOf('/');
		if(pos == -1)
			return "data.kpc";
		return url.substring(pos+1);
	}

	/**
	 * ネットリソースの存在確認
	 * @return 0:指定されていない 1:ダウンロード済み 2:未ダウンロード
	 */
	public static int downloadedResource()
	{
		// 指定がされていないなら 0
		if(netResourceUrl == null || netResourceUrl.size() == 0) return 0;

		// どれか一つでも空文字が指定されているなら 0
		int size = netResourceUrl.size();
		String url;
		for(int i = 0; i < size; i++)
		{
			url = netResourceUrl.get(i);
			if(url != null && url.length() == 0)
				return 0;
		}

		String filename = getFileNameFromURLString(netResourceUrl.get(0));
		if(filename == null)	// エラー？
			return 0;

		if(KpcManager.existArchive(filename, netResourceFileSize))
			return 1;
		else
			return 2;
	}

	/**
	 * ネットリソースのダウンロード準備
	 * @param activity メインアクティビティ
	 */
	public static final void preGetNetResource(MainActivity activity)
	{
		if(downloader != null && downloader.downloading)
		{
			onResume(activity);
		}
		else
		{
			if(Util.isConnectedNetwork(activity))
			{
				String message;
				if(netResourceFileSize < 1048576)	// 1MB より小さい
				{
					message = "初回起動時のみ、ゲームデータをネットワークからダウンロードします。" +
					"データサイズは約 " + (int)(netResourceFileSize/1024) + " KB です。" +
					"\nWi-Fi回線での接続を推奨します。";
				}
				else
				{
					message = "初回起動時のみ、ゲームデータをネットワークからダウンロードします。" +
					"データサイズは約 " + (int)(netResourceFileSize/1024/1024) + " MBです。" +
					"\nWi-Fi回線での接続を推奨します。";
				}

				String url = netResourceUrl.get((int)(Math.random()*netResourceUrl.size()));
				downloader = new ResourceDownloader(activity, getFileNameFromURLString(url), url, netResourceFileSize);
				DialogInterface.OnClickListener onYesClick = new DialogInterface.OnClickListener(){ // ダウンロード
					@Override
					public void onClick(DialogInterface dialog, int which){
						startGetNetResource();
					}
				};
				DialogInterface.OnClickListener onNoClick = new DialogInterface.OnClickListener(){ // 終了
					@Override
					public void onClick(DialogInterface dialog, int which){
						Util.closeGame();
					}
				};
				DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener(){	// キャンセル
					@Override
					public void onCancel(DialogInterface arg0){
						Util.closeGame();
					}
				};

				AlertDialog.Builder ad;
				ad = new AlertDialog.Builder(Util.mainActivity);
				ad.setTitle(Util.gameTitle);
				ad.setMessage(message);
				ad.setPositiveButton("ダウンロード開始", onYesClick);
				ad.setNegativeButton("終了", onNoClick);
				ad.setCancelable(true);
				ad.setOnCancelListener(onCancel);
				ad.create();
				ad.show();
			}
			else
			{
				String message = "ネットワークに接続されていません。\nこのゲームは初回起動時にゲームデータをダウンロードします。";
				Util.dialogMessage2(Util.gameTitle, message);
			}
		}
	}

	/** startGetNetResource で用いるファイル */
	private static File startGetNetResource_file = null;
	/**
	 * ダウンロード開始
	 */
	public static final void startGetNetResource()
	{
		// ファイルの存在確認
		File file = new File(Util.rootDirectoryPath + "/" + downloader.getNetResourceName());
		if(file.exists())
		{
			startGetNetResource_file = file;
			String message = "ファイルが既に存在します。続きからダウンロードしますか？";
			Util.dialogMessage(Util.gameTitle, message, false, "はい", "いいえ",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						downloader.setSkipsize(startGetNetResource_file.length());
						downloader.execute("START");
					}
				},
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						downloader.setSkipsize(0);
						startGetNetResource_file.delete();
						downloader.execute("START");
					}
				}
			);
		}
		else
			downloader.execute("START");
	}

	/**
	 * ダウンロード完了時
	 */
	public static final void endGetNetResource()
	{
		// ネットリソースを追加
		File file = downloader.getFile();
		KpcManager.getKpcFiles(file);
		downloader = null;
	}

	/**
	 * ダウンロード時のポーズ
	 */
	public static final void onPause()
	{
		if(downloader != null && downloader.downloading)
			downloader.onPause();
	}

	/**
	 * ダウンロード時のレジューム
	 * @param activity メインアクティビティ
	 */
	public static final void onResume(MainActivity activity)
	{
		if(downloader != null && downloader.downloading)
			downloader.onResume(activity);
	}

	/**
	 * ルートディレクトリのネットワークリソース（kpc ファイル）を削除する
	 */
	public static final void deleteNetResource()
	{
		File root = new File(Util.rootDirectoryPath);
		File[] files = root.listFiles();
		if(files == null) return;

		String[] name = new String[1];
		String[] ex = new String[1];

		for(int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if(f.isFile())
			{
				Util.getExtension(f, name, ex);
				if(ex[0].equals("kpc"))
				{
					Util.deleteFile(f.getName());
				}
			}
		}
	}





	/**
	 * ファイル名から画像読み込み
	 * @param r リソース
	 * @param filename ファイル名
	 * @param scale 拡大率
	 * @return 画像　引数が不正な場合は null
	 */
	public static Bitmap loadBitmap(Resources r, String filename, float scale)
	{
		if(filename == null || filename.length() == 0)
			return null;

		// 名前取り出し
		String[] name = new String[1]; String[] ex = new String[1];
		Util.getExtension(filename, name, ex);
		if(name[0].length() == 0) return null;

		for(int i = 0; i < 3; i++)
		{
			try
			{
				Bitmap image = null;
				// キャッシュから探す
				image = getCacheImage(filename);
				if(image != null)
					return image;

				// リソースから探す
				int id = r.getIdentifier(name[0], "drawable", Util.packageName);
				if(id != 0)
				{
					image = BitmapFactory.decodeResource(r, id, opt);
					addToCache(filename, image);
					return resizeBitmap(image, filename, scale);
				}

				// assets、アーカイブ、SDカードから探す
				FileInputStream fis = loadStream(filename, EXTENSION_IMAGE);
				if(fis != null)
				{
					image = BitmapFactory.decodeStream(fis, null, opt);
					try
					{
						fis.close();
					}catch (IOException e) { e.printStackTrace(); }
					addToCache(filename, image);
					return resizeBitmap(image, filename, scale);
				}
				break;
			}
			catch(OutOfMemoryError e) { onOutOfMemoryError(i); }
		}
		return null;
	}

	/**
	 * ファイル名から音声ファイルを読み込み
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return 読み込んだ音声ファイルの MediaPlayer
	 */
	public static final MediaPlayer loadSound(Context context, String filename)
	{
		if(filename == null || filename.length() == 0)
			return null;

//		Util.log("音声ロード：" + filename);

		// 名前取り出し
		String[] name = new String[1]; String[] ex = new String[1];
		Util.getExtension(filename, name, ex);
		if(name[0].length() == 0) return null;

		MediaPlayer player;

		for(int i = 0; i < 3; i++)
		{
			try
			{
				// リソースから探す
				int id = context.getResources().getIdentifier(name[0], "raw", Util.packageName);
				if(id != 0)
				{
					player = MediaPlayer.create(context, id);
					if(player != null)
						return player;
				}

				// アーカイブ、SD カードから探す
				long[] start = new long[1];
				long[] size = new long[1];
				FileInputStream fis = loadStream(filename, EXTENSION_SOUND, start, size);
				if(fis != null)
				{
					try
					{
						player = new MediaPlayer();
						if(start[0] != -1 && size[0] != -1)
							player.setDataSource(fis.getFD(), start[0], size[0]);
						else
							player.setDataSource(fis.getFD());
						fis.close();
						player.prepare();
						return player;
					}
					catch (IOException e)
					{
						e.printStackTrace();
						if(fis != null)
						{
							try { fis.close(); } catch (IOException e1) { e.printStackTrace(); }
						}
						Util.error("音声の読み込みに失敗\n" + filename);
					}
				}
			}
			catch(OutOfMemoryError e) { onOutOfMemoryError(i); }
		}
		return null;
	}

	/**
	 * ファイル名から動画ファイルを読み込み
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return 読み込んだ動画ファイルの MediaPlayer
	 */
	public static final MediaPlayer loadVideo(Context context, String filename, SurfaceHolder holder)
	{
		if(filename == null || filename.length() == 0)
			return null;

//		Util.log("動画ロード：" + filename);

		// 名前取り出し
		String[] name = new String[1];
		String[] ex = new String[1];
		Util.getExtension(filename, name, ex);
		if(name[0].length() == 0) return null;

		MediaPlayer player;

		for(int i = 0; i < 3; i++)
		{
			try
			{
				// リソースから探す
				int id = context.getResources().getIdentifier(name[0], "raw", Util.packageName);
				if(id != 0)
				{
					Uri uri = Uri.parse("android.resource://" + Util.packageName +"/" + id);
					player = new MediaPlayer();
					try
					{
						player.setDataSource(context, uri);
						if(player != null)
							return player;
					} catch (Exception e) { e.printStackTrace(); }
				}

				// アーカイブ、SD カードから探す
				long[] start = new long[1];
				long[] size = new long[1];
				FileInputStream fis = loadStream(filename, EXTENSION_VIDEO, start, size);
				if(fis != null)
				{
					try
					{
						player = new MediaPlayer();
						if(start[0] != -1 && size[0] != -1)
							player.setDataSource(fis.getFD(), start[0], size[0]);
						else
							player.setDataSource(fis.getFD());
						fis.close();
						return player;
					}
					catch (IOException e)
					{
						e.printStackTrace();
						if(fis != null)
						{
							try { fis.close(); } catch (IOException e1) { e.printStackTrace(); }
						}
						Util.error("動画の読み込みに失敗\n" + filename);
					}
				}
			}
			catch(OutOfMemoryError e) { onOutOfMemoryError(i); }
		}
		return null;
	}

	/**
	 * ファイル名からテキストファイルを読み込む<br>
	 * @param filename ファイル名　拡張子が省略された場合は自動的にシナリオファイル(.ks)を付け足す
	 * @param context コンテキスト
	 * @return 読み込んだテキストファイルのストリーム　ファイルがないなら null
	 * @throws IOException ファイル読み込みに失敗すると発生
	 */
	public static BufferedReader loadScenario(String filename, Context context) throws IOException
	{
		if(filename == null || filename.length() == 0)
			return null;

		// 名前取り出し
		String[] name = new String[1];
		String[] ex = new String[1];
		Util.getExtension(filename, name, ex);

		if(name[0].length() == 0) return null;
		if(ex[0].length() == 0) filename = name[0] + ".ks";


		InputStream is;
		BufferedReader br;

		for(int i = 0; i < 3; i++)
		{
			try
			{
				// assets から
				if(as != null && assetFiles.containsKey(filename))
				{
					is = as.open(filename);
					br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					return br;
				}

				// アーカイブ、SD カードから
				long[] start = new long[1];
				long[] size = new long[1];
				is = loadStream(filename, new String[]{""}, start, size);
				if(is != null)
				{
					if(start[0] != -1)
					{
						ByteArrayInputStream bis = getByteStream(is, start[0], size[0]);
						is.close();
						if(bis != null)
						{
							br = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
							return br;
						}
					}
					else
					{
						br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
						return br;
					}
				}
			}
			catch(OutOfMemoryError e) { onOutOfMemoryError(i); }
		}
		return null;
	}

	/**
	 * ストリームの内容を読み込んでバイトストリームを取得
	 * @param in ストリーム
	 * @param start 開始位置
	 * @param filesize サイズ
	 * @return バイトストリーム
	 * @throws IOException
	 */
	private static ByteArrayInputStream getByteStream(InputStream in, long start, long filesize)
	{
		if(in == null) return null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try
		{
			long skiped = in.skip(start);
			if(skiped != start) return null;

			int size;
			long sum = 0;
			byte[] buf = new byte[256];

			while(true)
			{
				size = in.read(buf);
				if(size <= 0)
					break;
				else
				{
					sum += size;
					if(sum <= filesize)
						out.write(buf, 0, size);
					else	// 超過して読み込んだ
					{
						sum -= size;
						out.write(buf, 0, (int)(filesize-sum));
						break;
					}
				}
			}
			byte[] data = out.toByteArray();
			out.close();

			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			return bis;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			try { out.close(); }
			catch (IOException e1) { e1.printStackTrace(); }
			return null;
		}
	}




	/**
	 * ルール画像の読み込み
	 * @param r リソース
	 * @param filename ファイル名
	 * @param array ルールを収める配列
	 * @return 読み込み、変換成功で true 失敗で false
	 */
	public static boolean loadRuleFile(Resources r, String filename, short[] array)
	{
		if(filename == null || filename.length() == 0)
			return false;

		// ルール画像読み込み
		Bitmap image = loadBitmap(r, filename, 1f);
		if(image == null)
			return false;

		for(int i = 0; i < 2; i++)
		{
			try
			{
				image = resizeRuleImage(image);	// リサイズ
//				Util.log("リサイズ後：" + image.getWidth() + "*" + image.getHeight());
				getRuleArray(image, array);	// 配列化
				freeImage(image);	// 解放
				return true;
			}
			catch(OutOfMemoryError e)
			{
				e.printStackTrace();
				if(Util.mainView != null)
					Util.mainView.onOutOfMemoryError();
			}
		}
		return false;
	}

	/**
	 * ルール画像をリサイズ
	 * @param image ルール画像
	 * @return リサイズ後の画像
	 */
	public static final Bitmap resizeRuleImage(Bitmap image)
	{
		if(image.getWidth() != Util.standardDispWidth || image.getHeight() != Util.standardDispHeight)
			image = Bitmap.createScaledBitmap(image, Util.standardDispWidth, Util.standardDispHeight, true);
		return image;
	}

	/**
	 * ルール画像から配列を確保
	 * @param image ルール画像
	 * @param array 変換後の配列
	 */
	public static final void getRuleArray(Bitmap image, short[] array)
	{
		int[] array2 = new int[Util.standardDispWidth*Util.standardDispHeight];
		image.getPixels(array2, 0, Util.standardDispWidth, 0, 0, Util.standardDispWidth, Util.standardDispHeight);
		for(int i = 0; i < array2.length; i++)
			array[i] = (short)((array2[i]&0x00ff0000 + array2[i]&0x0000ff00 + array2[i]&0x000000ff));
	}

	/**
	 * 画像のリサイズ
	 * 元の画像は解放されるので注意
	 * @param image 画像
	 * @param scale 倍率
	 * @return リサイズ後の画像
	 */
	public static Bitmap resizeBitmap(Bitmap image, String filename, float scale)
	{
		if(0 < scale && scale < 1f)
		{
			Bitmap tmp = Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), true);
			freeImage(image);
			addToCache(filename, tmp);
			return tmp;
		}
		else
			return image;
	}

	/**
	 * 画像を切り抜く
	 * 元の画像は解放されるので注意
	 * @param image 画像
	 * @param filename ファイル名
	 * @param x 切り抜く x 座標
	 * @param y 切り抜く y 座標
	 * @param width 切り抜く幅
	 * @param height 切り抜く高さ
	 * @return 切り抜いた画像　エラー時は元画像
	 */
	public static Bitmap clipBitmap(Bitmap image, String filename, int x, int y, int width, int height)
	{
		if(image == null || filename == null || filename.length() == 0) return image;

		int w = image.getWidth();
		int h = image.getHeight();

		if(x < 0) x = 0;
		if(x > w) x = w;
		if(y < 0) y = 0;
		if(y > h) y = h;

		if(width < 0) width = 0;
		if(height < 0) height = 0;

		if(x == 0 && y == 0 && width == w && height == h) return image;
		if(image.isRecycled())
		{
			System.out.println("isRecycled " + filename);
			return null;
		}

		Bitmap tmp = Bitmap.createBitmap(image, x, y, width, height);
		if(tmp == null) return image;

		freeImage(image);
		addToCache(filename+x+y+width+height, tmp);
		return tmp;
	}





	/**
	 * 拡張子を補完しつつストリームを取得
	 * @param filename ファイル名
	 * @param exs 拡張子の配列
	 * @param start 開始位置を受けとる配列。先頭からの場合は -1
	 * @param size ファイルサイズを受けとる配列。ファイル全体の場合は -1
	 * @return ストリーム
	 */
	public static FileInputStream loadStream(String filename, String[] exs, long[] start, long[] size)
	{
		if(filename == null || filename.length() == 0)
			return null;

		if(start != null && start.length != 0) start[0] = -1;
		if(size != null && size.length != 0) size[0] = -1;

		// 名前取り出し
		String[] name = new String[1];
		String[] ex = new String[1];
		Util.getExtension(filename, name, ex);
		if(name[0].length() == 0) return null;
		if(ex[0].length() != 0)	// そもそも拡張子が指定されている
			exs = new String[]{ "." + ex[0] };

		FileInputStream fis = null;
		int len = exs.length;
		String filename2;

		// アーカイブから
		for(int i = 0; i < len; i++)
		{
			filename2 = name[0] + exs[i];
			fis = KpcManager.getFileStream(filename2, start, size);
			if(fis != null) return fis;
		}

		if(start != null) start[0] = -1;
		if(size != null) size[0] = -1;

		// SD カードから
		for(int i = 0; i < len; i++)
		{
			filename2 = name[0] + exs[i];
			fis = loadStreamFromSD(filename2);
			if(fis != null) return fis;
		}

		return null;
	}

	/**
	 * 拡張子を補完しつつストリームを取得
	 * 取得後、開始位置までスキップしてから返す
	 * @param filename ファイル名
	 * @param exs 拡張子の配列
	 * @return ストリーム
	 */
	public static FileInputStream loadStream(String filename, String[] exs)
	{
		long[] start = new long[1];
		FileInputStream fis = loadStream(filename, exs, start, null);
		if(fis != null && start[0] != -1)
		{
			try
			{
				long size = fis.skip(start[0]);
				if(size != start[0])
				{
					fis.close();
					return null;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		return fis;
	}

	/**
	 * ファイル名からストリームを取得
	 * @param filename ファイル名
	 * @return ストリーム
	 */
	public static FileInputStream loadStream(String filename)
	{
		return loadStream(filename, new String[]{""});
	}

	/**
	 * SD カードからストリーム読み込み
	 * @param filename ファイル名
	 * @return ストリーム
	 */
	public static FileInputStream loadStreamFromSD(String filename)
	{
		if(filename == null) return null;
		String pathName = getSDResource(filename);
		if(pathName == null) return null;

		try
		{
			return new FileInputStream(pathName);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SD カード内の、指定されたファイル名のファイルを探してパスを返す
	 * @param filename ファイル名
	 * @return ファイルパス
	 */
	public static final String getSDResource(String filename)
	{
		int size = sdDirectory.size();
		File dir;
		File[] files;
		File f;
		for(int i = 0; i < size; i++)
		{
			dir = new File(sdDirectory.get(i));
			if(dir.exists())
			{
				files = dir.listFiles();
				if(files != null)
				{
					for(int j = 0; j < files.length; j++)
					{
						f = files[j];
						if(f.isFile())
						{
							if(filename.equals(f.getName()))
								return f.getPath();
						}
					}
				}
			}
		}
		return null;
	}


	/**
	 * SDカード内のデータを取得
	 * @return 取得成功で true 失敗で false
	 */
	public static boolean getSDData()
	{
//		Util.log("SDカードを走査");
		sdDirectory.clear();

		// SDカードのディレクトリを取得
		File root = new File(Util.rootDirectoryPath);
		sdDirectory.add(Util.rootDirectoryPath);
		try
		{
			getSDData_addDir(root);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * ディレクトリ内を走査し、ディレクトリパスの保持、KPCファイルの追加を行う
	 * @param dir ディレクトリ
	 * @throws IOException ディレクトリが空だと発生
	 */
	public static final void getSDData_addDir(File dir) throws IOException
	{
		if(dir == null)
			throw new IOException();
		File[] files = dir.listFiles();
		if(files == null)
			throw new IOException();

		String[] name = new String[1];
		String[] ex = new String[1];

		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if(f.isDirectory()) // ディレクトリ
			{
				sdDirectory.add(f.getPath());
				getSDData_addDir(f);	// 再帰
			}
			else	// ファイル
			{
				Util.getExtension(f, name, ex);
				ex[0] = ex[0].toLowerCase();
				if(ex[0].equals("kpc"))
					KpcManager.getKpcFiles(f);
			}
		}
	}

}
