package net.studiomikan.kas4pc.klib;

import java.io.InputStream;

public class BitmapFactory
{

	/**
	 * ストリームから画像生成
	 * @param is ストリーム
	 * @return 画像
	 */
	public static Bitmap decodeStream(InputStream is)
	{
		return new Bitmap(is);
	}

}
