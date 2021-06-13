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

import net.studiomikan.kas4pc.klib.Bitmap;
import net.studiomikan.kas4pc.klib.BitmapFactory;
import net.studiomikan.kas4pc.klib.Context;
import net.studiomikan.kas4pc.klib.Resources;
import net.studiomikan.kas4pc.klib.Typeface;



/**
 * ResourceManager<br>
 * 画像、音声などの管理をするクラス。
 * 　SDカード内のリソースを管理する<br>
 * 　アーカイブファイル内のリソースを検索して取得する<br>
 * 　ユニバーサルトランジションのルール画像を取得する<br>
 * 　リソースの取得は、リソース->SDアーカイブ->SD生ファイルの順<br>
 * @author okayu
 */
public class ResourceManager
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

	/** ネットリソースの場所 */
	public static ArrayList<String> netResourceUrl = new ArrayList<String>();
	/** ネットリソースのファイルサイズ */
	public static long netResourceFileSize = 0;

	/** 画像ファイルの拡張子 **/
	public static final String[] EXTENSION_IMAGE = {".png", ".PNG", ".jpg", ".JPG", ".jpeg", ".JPEG"};
	/** 音声ファイルの拡張子 **/
	public static final String[] EXTENSION_SOUND = {".ogg", ".OGG"};
	/** 動画ファイルの拡張子 */
	public static final String[] EXTENSION_VIDEO = {".mp4", ".MP4"};

	/** ディレクトリを走査したかどうか */
	private static boolean loadedDirctory = false;
	/** ディレクトリのマップ */
	private static ArrayList<String> directory = new ArrayList<String>();

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


	/**
	 * ResourceManager の準備
	 * @param context コンテキスト
	 */
	public static void getReady()
	{
		// ディレクトリの走査
		if(!loadedDirctory)
		{
			getDirData();
			loadedDirctory = true;
		}
		// キャッシュ関係
		systemMem = 0;
		mem = 0;
	}

	/**
	 * ResourceManager の後処理
	 */
	public static void free()
	{
		loadedDirctory = false;
		directory.clear();
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
		Util.log("==========>画像free:" + name);
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

		Util.log("=============>画像ロード：" + filename);

		Bitmap image;

		// キャッシュから
		image = getCacheImage(filename);
		if(image != null)
		{
			return image;
		}

		// 新規読み込み
		InputStream is = loadStream(filename, EXTENSION_IMAGE);
		if(is == null)
		{
			Util.log("画像読み込み失敗");
			return null;
		}

		image = BitmapFactory.decodeStream(is);
		try
		{
			is.close();
		}catch (IOException e) { e.printStackTrace(); }
		addToCache(filename, image);
		return image;
	}

	/**
	 * ファイル名から音声ファイルを読み込み
	 * @param filename ファイル名
	 * @return 読み込んだ音声ファイルのストリーム
	 */
	public static final InputStream loadSound(String filename)
	{
		if(filename == null || filename.length() == 0)
			return null;

//		Util.log("音声ロード：" + filename);

		return loadStream(filename, EXTENSION_SOUND);
	}

	/**
	 * ファイル名から動画ファイルを読み込み
	 * @param filename ファイル名
	 * @return 読み込んだ動画ファイルのストリーム
	 */
	public static final InputStream loadVideo(String filename)
	{
		if(filename == null || filename.length() == 0)
			return null;

//		Util.log("動画ロード：" + filename);

		return loadStream(filename, EXTENSION_VIDEO);
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

//		Util.log("テキストロード：" + filename);

		// 名前取り出し
		String[] name = new String[1];
		String[] ex = new String[1];
		Util.getExtension(filename, name, ex);

		if(name[0].length() == 0) return null;
		if(ex[0].length() == 0) filename = name[0] + ".ks";

		InputStream is;
		BufferedReader br;
		long[] start = new long[1];
		long[] size = new long[1];

		is = loadStream(filename, new String[]{""}, start, size);
		if(is != null)
		{
			if(start[0] != -1)
			{
				ByteArrayInputStream bis = getByteStream(is, start[0], size[0]);
				br = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
			}
			else
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			return br;
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
	private static ByteArrayInputStream getByteStream(InputStream in, long start, long filesize) throws IOException
	{
		if(in == null) return null;

		long skiped = in.skip(start);
		if(skiped != start) return null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

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




	/**
	 * フォントの読み込み
	 * @param context コンテキスト
	 * @param filename フォントファイル名
	 * @return フォント
	 */
	public static Typeface loadTypeFace(Context context, String filename)
	{
		Typeface face = Typeface.createFromAsset(context.getAssets(), filename);
		return face;
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

		image = resizeRuleImage(image);		// リサイズ
//		Util.log("リサイズ後：" + image.getWidth() + "*" + image.getHeight());
		getRuleArray(image, array);	// 配列化
		return true;
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
		image = null;
		for(int i = 0; i < array2.length; i++)
		{
			array[i] = (short)((array2[i]&0x00ff0000 + array2[i]&0x0000ff00 + array2[i]&0x000000ff));
		}
	}

	/**
	 * 画像のリサイズ
	 * @param image 画像
	 * @param scale 倍率
	 * @return リサイズ後の画像
	 */
	public static Bitmap resizeBitmap(Bitmap image, float scale)
	{
		if(0 < scale && scale < 1f)
		{
			image = Bitmap.createScaledBitmap(image, (int)(image.getWidth()*scale), (int)(image.getHeight()*scale), true);
		}
//		Util.log("image size: " + image.getWidth() + "*" + image.getHeight());
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
			fis = loadStreamFromDir(filename2);
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
	 * ディレクトリからストリームを読み込み
	 * @param filename ファイル名
	 * @return ストリーム
	 */
	public static FileInputStream loadStreamFromDir(String filename)
	{
		if(filename == null) return null;
		String pathName = getDirResource(filename);
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
	 * ディレクトリ内の、指定されたファイル名のファイルを探してパスを返す
	 * @param filename ファイル名
	 * @return ファイルパス
	 */
	public static final String getDirResource(String filename)
	{
		int size = directory.size();
		File dir;
		File[] files;
		File f;
		for(int i = 0; i < size; i++)
		{
			dir = new File(directory.get(i));
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
	 * ディレクトリ内のデータを取得
	 * @return 取得成功で true 失敗で false
	 */
	public static boolean getDirData()
	{
//		Util.log("SDカードを走査");
		directory.clear();

		// ディレクトリを取得
		File root = new File(Util.rootDirectoryPath);
		directory.add(Util.rootDirectoryPath);
		try
		{
			getDirData_addDir(root);
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
	public static final void getDirData_addDir(File dir) throws IOException
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
				directory.add(f.getPath());
				getDirData_addDir(f);	// 再帰
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
