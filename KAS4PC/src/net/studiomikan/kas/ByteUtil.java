package net.studiomikan.kas;
import java.io.File;

/**
 * バイトデータの扱いに関するクラス<br>
 * バイト単位でのデータを扱うユーティリティクラスです。
 * @author okayu
 */
class ByteUtil
{
	/**
	 * short型の値をバイトデータにする リトルエンディアン
	 * @param value 変換する値
	 * @return バイトデータ
	 * @throws IllegalArgumentException
	 */
	public static byte[] printShort(short value) throws IllegalArgumentException
	{
		int size = 2;	// Javaのshort型は2バイト固定
		byte[] b = new byte[size];
		for(int i = 0; i < size; i++)
			b[i] = Integer.valueOf(value >> (Byte.SIZE*i)).byteValue();
		return b;
	}

	/**
	 * バイト配列をshort型に リトルエンディアン
	 * @param b バイトデータ
	 * @param offset オフセット
	 * @return 変換後の値
	 * @throws IllegalArgumentException
	 */
	public static short getShort(byte[] b, int offset) throws IllegalArgumentException
	{
		short result = 0;
		int size = 2;	// Javaのshort型は2バイト固定
		if(b == null || b.length < size || offset < 0 || b.length - size < offset)
			throw new IllegalArgumentException();
		else
		{
			for(int i = 0; i < size; i++)
				result |= Integer.valueOf(b[offset+i] & 0xff).shortValue() << (Byte.SIZE*i);
		}
		return result;
	}

	/**
	 * int型の値をバイトデータに リトルエンディアン
	 * @param value 変換する値
	 * @return バイトデータ
	 * @throws IllegalArgumentException
	 */
	public static byte[] printInt(int value) throws IllegalArgumentException
	{
		int size = 4;	// Javaのint型は4バイト固定
		byte[] b = new byte[size];
		for(int i = 0; i < size; i++)
			b[i] = Integer.valueOf(value >> (Byte.SIZE*i)).byteValue();
		return b;
	}

	/**
	 * バイト配列をint型に リトルエンディアン
	 * @param b バイトデータ
	 * @param offset オフセット
	 * @return 変換後の値
	 * @throws IllegalArgumentException
	 */
	public static int getInt(byte[] b, int offset) throws IllegalArgumentException
	{
		short result = 0;
		int size = 2;	// Javaのint型は4バイト固定
		if(b == null || b.length < size || offset < 0 || b.length - size < offset)
			throw new IllegalArgumentException();
		else
		{
			for(int i = 0; i < size; i++)
				result |= Integer.valueOf(b[offset+i] & 0xff).intValue() << (Byte.SIZE*i);
		}
		return result;
	}

	/**
	 * long型の値をバイトデータに リトルエンディアン
	 * @param value 変換する値
	 * @return バイトデータ
	 * @throws IllegalArgumentException
	 */
	public static byte[] printLong(long value) throws IllegalArgumentException
	{
		int size = 8;	// Javaのlong型は8バイト固定
		//int size = Long.SIZE/Byte.SIZE;	// 結果は8
		byte[] b = new byte[size];
		for(int i = 0; i < size; i++)
			b[i] = Long.valueOf(value >> (Byte.SIZE*i)).byteValue();
		return b;
	}

	/**
	 * バイト配列をlong型に リトルエンディアン
	 * @param b バイトデータ
	 * @param offset オフセット
	 * @return 変換後の値
	 * @throws IllegalArgumentException
	 */
	public static long getLong(byte[] b, int offset) throws IllegalArgumentException
	{
		long result = 0;
		int size = 8;	// JavaのLong型は8バイト固定
		//int size = Long.SIZE/Byte.SIZE;	// 結果は8
		if(b == null || b.length < size || offset < 0 || b.length - size < offset)
			throw new IllegalArgumentException();
		else
		{
			for(int i = 0; i < size; i++)
				result |= Integer.valueOf(b[offset+i] & 0xff).longValue() << (Byte.SIZE*i);
		}
		return result;
	}

	/**
	 * ファイルの拡張子と名前を得る。name[0] 及び ex[0] に、それぞれ名前を拡張子が代入される。
	 * @param file ファイル
	 * @param name 名前を収めるための配列
	 * @param ex 拡張子を収めるための配列
	 */
	public static void getExtension(File file, String[] name, String[] ex)
	{
		getExtension(file.getName(), name, ex);
	}
	
	/**
	 * ファイルの拡張子と名前を得る。name[0] 及び ex[0] に、それぞれ名前を拡張子が代入される。
	 * @param filename ファイル名
	 * @param name 名前を収めるための配列
	 * @param ex 拡張子を収めるための配列
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


}
