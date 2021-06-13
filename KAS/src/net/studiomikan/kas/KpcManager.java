package net.studiomikan.kas;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * KpcManager クラス<br>
 * .kpcアーカイブの管理をする<br>
 * アーカイブファイルのヘッダを読み込み、ファイル一覧を作成する<br>
 * 指定されたファイル名からストリームを取得する<br>
 * @author okayu
 */
class KpcManager
{
	/**
	 * アーカイブデータ
	 * @author okayu
	 */
	public static class ArchiveData
	{
		/** アーカイブファイル */
		public File file;
		/** ファイルパス */
		public String path;
		/** アーカイブの内容 */
		public TreeMap<String, FileData> fileMap = new TreeMap<String, FileData>();
	}

	/**
	 * アーカイブ内のファイルデータ
	 * @author okayu
	 */
	public static class FileData
	{
		/** 開始アドレス */
		public long startAddress;
		/** ファイルサイズ */
		public long fileSize;

		@Override
		public String toString()
		{
			return "start:" + startAddress + " size:" + fileSize;
		}
	}

	/** アーカイブデータ */
	private static ArrayList<ArchiveData> archive = new ArrayList<ArchiveData>();
	/** アーカイブ数 */
	private static int archiveCount = 0;

	/**
	 * アーカイブファイルの存在確認
	 * @param filename ファイル名
	 * @param filesize ファイルサイズ
	 * @return 存在するなら true
	 */
	public static boolean existArchive(String filename, long filesize)
	{
		String[] name1 = new String[1];
		String[] name2 = new String[1];
		String[] buff = new String[1];
		Util.getExtension(filename, name1, buff);
		int len = archive.size();
		ArchiveData ad;
		for(int i = 0; i < len; i++)
		{
			ad = archive.get(i);
			Util.getExtension(ad.file, name2, buff);
			if(name1[0].equals(name2[0])) // ある
			{
				if(ad.file.length() == filesize) // ファイルサイズが等しい
				{
//					Util.log(filename + "を発見:" + filesize + "=" + ad.file.length());
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * KPCファイルを読み込んでマップに追加
	 * @param file KPCファイル
	 * @return 成功なら true 失敗なら false
	 */
	public static boolean getKpcFiles(File file)
	{
		// エラーチェックとか
		if(!file.exists())
			return false;

		// ヘッダ読み込みと登録
		readHeader(file);
		return true;	// 正常終了
	}

	/**
	 * ファイル名から FileInputStreamを返す<br>
	 * 通常のストリームで開始アドレスまでジャンプする
	 * @param filename ファイル名
	 * @return ファイルのストリーム
	 */
	public static FileInputStream getFileStream(String filename)
	{
		return getFileStream(filename, null, null);
	}

	/**
	 * ファイル名から、アーカイブのFileInputStreamと、ファイルの情報を返す
	 * @param filename ファイル名
	 * @param start ファイルの開始位置
	 * @param size ファイルのサイズ
	 * @return アーカイブのストリーム
	 */
	public static final FileInputStream getFileStream(String filename, long[] start, long[] size)
	{
		if(filename == null || archiveCount == 0) return null;

		ArchiveData ad;
		FileData fd;
		File file;
		FileInputStream fis;

		for(int i = 0; i < archiveCount; i++)
		{
			ad = archive.get(i);
			if(ad == null)
				break;
			fd = ad.fileMap.get(filename);
			if(fd != null)	// アーカイブの中にファイルを見つけた
			{
				file = ad.file;
				if(file.exists())
				{
					try
					{
						// アーカイブから Stream を生成して返す
						fis = new FileInputStream(file);
						if(start != null) start[0] = fd.startAddress;
						if(size != null) size[0] = fd.fileSize;
						return fis;
					}
					catch (IOException e)
					{
						e.printStackTrace();
						break;
					}
				}
			}
		}
//		Util.log("getFileStreamで見つからなかった:" + filename);
		return null;
	}

	/**
	 * ヘッダ部の読み込みと管理マップへの追加
	 * @param file アーカイブファイル
	 * @return ヘッダ読み込み結果
	 * @throws IOException アーカイブ読み込みに失敗すると発生
	 */
	public static boolean readHeader(File file)
	{
		FileInputStream in = null;
		boolean result;

		try
		{
			in = new FileInputStream(file);

			int size;
			byte[] b2 = new byte[2];
			byte[] b4 = new byte[4];
			byte[] b8 = new byte[8];
			byte[] b;
			String str;

				size = in.read(b8);
			if(size != 8) return false;

			str = new String(b8);
			if(!str.equals("KASPAC01")) return false;

			size = in.read(b2);
			if(size != 2) return false;

			size = in.read(b4);
			int fileCount = ByteUtil.getInt(b4, 0);

			ArchiveData ad = new ArchiveData();
			ad.file = file;
			ad.path = file.getPath();

			FileData fileData;
			String filename;
			for(int i = 0; i < fileCount; i++)
			{
				fileData = new FileData();

				size = in.read(b2);
				if(size != 2) return false;

				b = new byte[ByteUtil.getShort(b2, 0)];
				size = in.read(b);
				if(size != b.length) return false;
				filename = new String(b, "UTF-8");

				size = in.read(b8);
				if(size != 8) return false;
				fileData.startAddress = ByteUtil.getLong(b8, 0);

				size = in.read(b8);
				if(size != 8) return false;
				fileData.fileSize = ByteUtil.getLong(b8, 0);

				size = in.read(b4);
				if(size != 4) return false;

				ad.fileMap.put(filename, fileData);
			}

			in.close();
			archive.add(ad);
			archiveCount = archive.size();

			result = true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			result = false;
		}
		finally
		{
			try
			{
				in.close();
			} catch (IOException e1){ e1.printStackTrace(); }
		}

		return result;
	}



}
