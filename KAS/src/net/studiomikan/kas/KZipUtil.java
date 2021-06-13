package net.studiomikan.kas;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * Zip 関連のメソッド
 * @author okayu
 */
public class KZipUtil
{
	/** 圧縮機 */
	private static Deflater zipCompresser = new Deflater();
	/** 解凍器 */
	private static Inflater zipDecompresser = new Inflater();

	/** 圧縮後のサイズ */
	public static long compressedSize = -1;
	/** 解凍後のサイズ */
	public static long decompressedSize = -1;

	/** 圧縮レベル 0 ~ 9 */
	private static int level = Deflater.DEFAULT_COMPRESSION;

	/** 作業用バッファ */
	private static final byte[] BUFFER = new byte[1024];

	/** インスタンス化禁止 */
	private KZipUtil()
	{
	}

	/**
	 * 圧縮レベルを設定
	 * @param level
	 */
	public static void setLevel(int level)
	{
		if(level < 0 || 9 < level) KZipUtil.level = Deflater.DEFAULT_COMPRESSION;
		else KZipUtil.level = level;
	}

	/**
	 * データを圧縮する
	 * @param data 圧縮するデータ
	 * @return 圧縮後のデータ　エラー時は null
	 */
	public static byte[] compress(byte[] data)
	{
		byte[] result = null;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.reset();

		try
		{
			compress(data, stream);
			result = stream.toByteArray();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			compressedSize = -1;
			result = null;
		}
		finally
		{
			try { stream.close(); } catch (IOException e1) { e1.printStackTrace(); }
		}

		return result;
	}

	/**
	 * データを圧縮して書き込む
	 * @param data 圧縮するデータ
	 * @param out 出力先
	 * @throws IOException
	 * @return 圧縮後のサイズ
	 */
	public static long compress(byte[] data, OutputStream out) throws IOException
	{
		zipCompresser.reset();
		zipCompresser.setLevel(level);
		zipCompresser.setInput(data);
		zipCompresser.finish();

		int size;
		byte[] buffer = BUFFER;

		compressedSize = 0;

		while(true)
		{
			size = zipCompresser.deflate(buffer);
			if(size <= 0)
				break;
			out.write(buffer, 0, size);
			compressedSize += size;
			if(zipCompresser.finished())
				break;
		}

		return compressedSize;
	}

	/**
	 * データを解凍する
	 * @param data zip 圧縮されたデータ
	 * @return 解凍されたデータ　エラー時は null
	 */
	public static byte[] decompress(byte[] data)
	{
		byte[] result = null;

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		stream.reset();

		try
		{
			decompress(data, stream);
			result = stream.toByteArray();
		}
		catch (DataFormatException e)
		{
			e.printStackTrace();
			result = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			result = null;
		}
		finally
		{
			try { stream.close(); } catch (IOException e1) { e1.printStackTrace(); }
		}

		return result;
	}

	/**
	 * データを解凍して出力する
	 * @param data zip 圧縮されたデータ
	 * @param out 出力先
	 * @return 解凍後のサイズ
	 * @throws DataFormatException
	 * @throws IOException
	 */
	public static long decompress(byte[] data, OutputStream out) throws DataFormatException, IOException
	{
		zipDecompresser.reset();
		zipDecompresser.setInput(data);

		int size;
		byte[] buffer = BUFFER;

		decompressedSize = 0;

		while(true)
		{
			size = zipDecompresser.inflate(buffer);
			if(size <= 0)
				break;
			out.write(buffer, 0, size);
			decompressedSize += size;
			if(zipDecompresser.finished())
				break;
		}

		return decompressedSize;
	}

}
