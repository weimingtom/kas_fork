package net.studiomikan.kas4pc.klib;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.MemoryImageSource;

import net.studiomikan.kas.Util;
import net.studiomikan.kas4pc.gui.KGUIUtil;
import net.studiomikan.kas4pc.klib.PorterDuff.Mode;


/**
 * キャンバス
 * @author okayu
 */
public class Canvas
{
	/** 描画用グラフィック */
	private Graphics2D g = null;
	/** 幅 */
	private int width = 0;
	/** 高さ */
	private int height = 0;
	/** 標準フォント */
	public static Font deffont = null;
	/** フォント */
	public Font font = null;

	/**
	 * 回転
	 * @param angle 角度
	 * @param cx 中心
	 * @param cy 中心
	 */
	public void rotate(double angle, double cx, double cy)
	{
		final AffineTransform at = new AffineTransform();
		at.rotate(angle, cx, cy);
		g.setTransform(at);
	}

	/**
	 * キャンバスの状態を保存
	 * 今のところ、なんにもしない
	 */
	public void save()
	{
	}

	/**
	 * save で保存した状態に戻す
	 * 今のところ、回転を戻すためにしか使われていないので、回転を元に戻す
	 */
	public void restore()
	{
		rotate(0, 0, 0);
		g.setClip(0, 0, width, height);
	}

	public Canvas()
	{
		if(deffont == null)
			deffont = KGUIUtil.getGUIFont(Util.mainFrame);
		if(font == null)
			font = deffont;
	}

	public Canvas(Bitmap image)
	{
		this();
		if(image != null)
			setGraphics2D((Graphics2D)image.image.getGraphics(), image.getWidth(), image.getHeight());
	}

	public Graphics2D getGraphics2d()
	{
		return g;
	}

	/**
	 * 描画先グラフィックを指定
	 * @param graph
	 */
	public final void setGraphics2D(Graphics2D graph, int width, int height)
	{
		this.g = graph;
		this.width = width;
		this.height = height;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	/**
	 * 画像を描画
	 * @param image 画像
	 * @param rectSrc 描画元矩形
	 * @param rectDst 描画先矩形
	 * @param paint ペイント
	 */
	public void drawBitmap(Bitmap image, Rect src, Rect dst, Paint paint)
	{
		if(image == null) return;
		if(paint != null) setAlpha(paint.alpha);
		g.drawImage(image.image, dst.left, dst.top, dst.right, dst.bottom, src.left, src.top, src.right, src.bottom, null);
		if(paint != null )setAlpha(255);
	}

	/**
	 * 画像を描画
	 * @param image 画像
	 * @param x 描画座標
	 * @param y 描画座標
	 * @param paint ペイント
	 */
	public void drawBitmap(Bitmap image, int x, int y, Paint paint)
	{
		if(image == null) return;
		if(paint != null) setAlpha(paint.alpha);
		g.drawImage(image.image, x, y, null);
		if(paint != null) setAlpha(255);
	}

	/**
	 * 画素の配列から描画
	 * @param colors 配列
	 * @param offset オフセット
	 * @param stride ストライド
	 * @param x 描画座標
	 * @param y 描画座標
	 * @param width 幅
	 * @param height 高さ
	 * @param hasAlpha アルファ値があるかどうか
	 * @param paint ペイント
	 */
	public void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint)
	{
		MemoryImageSource mi = new MemoryImageSource(width, height, colors, offset, stride);
		if(paint != null) setAlpha(paint.alpha);
		g.drawImage(Util.mainPanel.createImage(mi), x, y, null);
		if(paint != null) setAlpha(255);
	}

	/**
	 * ペイントをフォントに適用
	 * @param paint
	 */
	public void setPaint2Font(Paint paint)
	{
		if(paint != null)
		{
			if(paint.typeface != null) font = paint.typeface.font;
			if(font == null) font = deffont;
			font = font.deriveFont((float)paint.textSize);
			if(paint.typeface.equals(Typeface.DEFAULT_BOLD))
			{
				font = font.deriveFont(Font.BOLD);
			}
			else
			{
				switch(paint.typeface.opt)
				{
				case Typeface.NORMAL:
					font = font.deriveFont(Font.PLAIN);
					break;
				case Typeface.BOLD:
					font = font.deriveFont(Font.BOLD);
					break;
				case Typeface.ITALIC:
					font = font.deriveFont(Font.ITALIC);
					break;
				case Typeface.BOLD_ITALIC:
					font = font.deriveFont(Font.BOLD & Font.ITALIC);
					break;
				default:
					font = font.deriveFont(Font.PLAIN);
					break;
				}
			}
			g.setFont(font);
		}
	}

	/**
	 * 文字列を描画
	 * @param text 文字列
	 * @param x 座標
	 * @param y 座標
	 * @param paint ペイント
	 */
	public void drawText(String text, int x, int y, Paint paint)
	{
		if(text == null || text.length() == 0) return;
		if(paint == null)
			g.drawString(text, x, y);
		else
		{
			setAlpha(paint.alpha);
			setPaint2Font(paint);
			g.setColor(new Color(paint.color, true));
			int w = paint.strokeWidth;
			switch(paint.style)
			{
			case Paint.Style.STROKE:
				g.drawString(text, x, y-w);
				g.drawString(text, x+w, y-w);
				g.drawString(text, x+w, y);
				g.drawString(text, x+w, y+w);
				g.drawString(text, x, y+w);
				g.drawString(text, x-w, y+w);
				g.drawString(text, x-w, y);
				g.drawString(text, x-w, y-w);
				break;
			case Paint.Style.FILL:
				g.drawString(text, x, y);
				break;
			case Paint.Style.FILL_AND_STROKE:
				g.drawString(text, x, y);
				break;
			default:
				g.drawString(text, x, y);
				break;
			}
			setAlpha(255);
		}

	}

	/**
	 * 文字列を描画
	 * @param text 文字列
	 * @param index 開始位置
	 * @param count 文字数
	 * @param x 座標
	 * @param y 座標
	 * @param paint ペイント
	 */
	public void drawText(char[] text, int index, int count, int x, int y, Paint paint)
	{
		if(text == null || text.length == 0) return;
		if(paint == null)
			g.drawChars(text, index, count, x, y);
		else
		{
			setAlpha(paint.alpha);
			setPaint2Font(paint);
			g.setColor(new Color(paint.color, true));
			int w = paint.strokeWidth;
			switch(paint.style)
			{
			case Paint.Style.STROKE:
				g.drawChars(text, index, count, x, y-w);
				g.drawChars(text, index, count, x+w, y-w);
				g.drawChars(text, index, count, x+w, y);
				g.drawChars(text, index, count, x+w, y+w);
				g.drawChars(text, index, count, x, y+w);
				g.drawChars(text, index, count, x-w, y+w);
				g.drawChars(text, index, count, x-w, y);
				g.drawChars(text, index, count, x-w, y-w);
				break;
			case Paint.Style.FILL:
				g.drawChars(text, index, count, x, y);
				break;
			case Paint.Style.FILL_AND_STROKE:
				g.drawChars(text, index, count, x, y);
				break;
			default:
				g.drawChars(text, index, count, x, y);
				break;
			}
			setAlpha(255);
		}
	}

	/**
	 * 矩形を描画
	 * @param rect 矩形
	 * @param paint ペイント
	 */
	public void drawRect(Rect rect, Paint paint)
	{
		if(paint == null)
			return;
		g.setColor(new Color(paint.color, true));
		setAlpha(paint.alpha);
		switch(paint.style)
		{
		case Paint.Style.STROKE:
			g.drawRect(rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top);
			break;
		default:
			g.fillRect(rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top);
			break;
		}
		setAlpha(255);
	}

	/**
	 * 塗りつぶし
	 * @param color
	 */
	public void drawColor(int color)
	{
		g.setColor(new Color(color, true));
		g.fillRect(0, 0, width, height);
	}

	/**
	 * 塗りつぶし
	 * @param color
	 * @param mode
	 */
	public void drawColor(int color, Mode mode)
	{
		// トランジションで、画像をクリアするのに使われる
		// よって普通に塗りつぶしちゃう
		drawColor(0xff000000);
	}

	/**
	 * 塗りつぶし
	 * @param alpha アルファ
	 * @param red 赤
	 * @param green 緑
	 * @param blue 青
	 */
	public void drawARGB(int alpha, int red, int green, int blue)
	{
		int color = alpha*0x1000000 + red*0x10000 + green*0x100 + blue;
		g.setColor(new Color(color, true));
		g.fillRect(0, 0, Util.standardDispWidth, Util.standardDispHeight);
	}

	/**
	 * 文字幅を得る
	 * @param str
	 * @return
	 */
	public int stringWidth(String str)
	{
		if(g != null)
		{
			FontMetrics fm = g.getFontMetrics();
			return fm.stringWidth(str);
		}
		else
			return -1;
	}

	/**
	 * アルファブレンドの濃度設定
	 * @param alpha
	 */
	public void setAlpha(int alpha)
	{
		float a = 0;
		if(alpha <= 0)
			a = 0;
		else if(alpha > 255)
			a = 1.0f;
		else
		{
			a = (float)alpha / 255f;
			if(a > 1.0f)
				a = 1.0f;
		}
		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);
		g.setComposite(composite);
	}

	/**
	 * クリップする
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void clipRect(int left, int top, int right, int bottom)
	{
		g.clipRect(left, top, right-left, bottom-top);
	}

}
