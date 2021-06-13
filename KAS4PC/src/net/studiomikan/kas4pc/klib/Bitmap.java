package net.studiomikan.kas4pc.klib;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;


/**
 * ビットマップ
 * @author okayu
 */
public class Bitmap
{
	public Image image;

	public static class Config
	{
		public static final int RGB_565 = 0;
		public static final int ARGB_8888 = 1;
	}

	private Bitmap(Image image)
	{
		this.image = image;
	}

	/**
	 * 指定サイズの画像を生成する
	 * @param width
	 * @param height
	 */
	public Bitmap(int width, int height)
	{
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * ビットマップを生成する
	 * @param width 幅
	 * @param height 高さ
	 * @param type 種類
	 * @return 画像
	 */
	public static Bitmap createBitmap(int width, int height, int type)
	{
		switch(type)
		{
		case Config.RGB_565:
			return new Bitmap( new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) );
		default:
			return new Bitmap( new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) );
		}
	}

	/**
	 * ファイル名から画像生成
	 * @param filename
	 */
	public Bitmap(String filename)
	{
		File file = new File(filename);
		if(file.exists())
		{
			try
			{
				image = ImageIO.read(file);
				System.out.println("画像読み込み " + filename);
			}
			catch (IOException e)
			{
				System.out.println("画像読み込み失敗 " + filename);
				throw new RuntimeException(e);
			}
		}
		else
			System.out.println("画像読み込み失敗 " + filename);
	}

	/**
	 * ストリームから画像生成
	 * @param is
	 */
	public Bitmap(InputStream is)
	{
		try
		{
			image = ImageIO.read(is);
		}
		catch (IOException e)
		{
			System.out.println("画像読み込み失敗");
			throw new RuntimeException(e);
		}
	}

	/**
	 * PNG として保存
	 * @param path 保存先パス
	 * @return
	 */
	public boolean saveAsPng(String path)
	{
		BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(image, 0, 0, null);
		boolean result;
		try
		{
			result = ImageIO.write(bi, "png", new File(path));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return image.hashCode();
	}

	/**
	 * 指定サイズの画像を生成する
	 * @param image 元画像
	 * @param width 幅
	 * @param height 高さ
	 * @param flag スムージング
	 * @return 画像
	 */
	public static Bitmap createScaledBitmap(Bitmap image, int width, int height, boolean flag)
	{
		return new Bitmap( image.image.getScaledInstance(width, height, Image.SCALE_SMOOTH) );
	}

	/**
	 * 画像をトリミングして生成する
	 * @param image 画像
	 * @param x トリミング位置
	 * @param y トリミング位置
	 * @param width 幅
	 * @param height 高さ
	 * @return 画像
	 */
	public static Bitmap createBitmap(Bitmap image, int x, int y, int width, int height)
	{
		BufferedImage cliped = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = cliped.createGraphics();
		g.drawImage(image.image, -x, -y, null);
		return new Bitmap( cliped );
	}

	/**
	 * 幅取得
	 * @return 画像の幅
	 */
	public int getWidth()
	{
		return image.getWidth(null);
	}

	/**
	 * 高さ取得
	 * @return 画像の高さ
	 */
	public int getHeight()
	{
		return image.getHeight(null);
	}

	/**
	 * 画素を取得
	 * @param pixels 受けとる配列
	 * @param offset オフセット
	 * @param stride ストライド
	 * @param x 開始地点
	 * @param y 開始地点
	 * @param width 幅
	 * @param height 高さ
	 */
	public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height)
	{
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
		try
		{
			pg.grabPixels();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 一行のバイトサイズを返す
	 * @return
	 */
	public long getRowBytes()
	{
		return image.getWidth(null) * 4;
	}

	/**
	 * つじつま合わせ　何もしない
	 */
	public void recycle()
	{
	}

	/**
	 * つじつま合わせ　常に false
	 * @return
	 */
	public boolean isRecycled()
	{
		return false;
	}

}
