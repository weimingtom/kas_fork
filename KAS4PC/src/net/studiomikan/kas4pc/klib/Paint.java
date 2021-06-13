package net.studiomikan.kas4pc.klib;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import net.studiomikan.kas.Util;


/**
 * Android の Paint に合わせる
 * @author okayu
 */
public class Paint
{
	/** 色 */
	public int color = 0;
	/** アルファ値 */
	public int alpha = 255;
	/** アンチエイリアス */
	public boolean antiAlias = true;
	/** テキストサイズ */
	public float textSize = 22f;
	/** スタイル */
	public int style = 0;
	/** ストロークの太さ */
	public int strokeWidth = 1;
	/** 文字の種類 */
	public Typeface typeface = Typeface.DEFAULT;

	/** スタイル */
	public static class Style
	{
		public static final int FILL = 0;
		public static final int STROKE = 10;
		public static final int FILL_AND_STROKE = 20;
	}


	public Paint()
	{
	}

	/**
	 * 他の Paint から複製
	 * @param src
	 */
	public void set(Paint src)
	{
		this.color = src.color;
		this.alpha = src.alpha;
		this.antiAlias = src.antiAlias;
		this.textSize = src.textSize;
		this.style = src.style;
		this.strokeWidth = src.strokeWidth;
		this.typeface = src.typeface;
	}

	/**
	 * alpha 値
	 * @param alpha
	 */
	public void setAlpha(int alpha)
	{
		this.alpha = alpha;
	}
	public int getAlpha() { return alpha; }

	/**
	 * 色
	 * @param color
	 */
	public void setColor(int color)
	{
		this.color = color;
	}


	/**
	 * アンチエイリアス
	 * @param flag
	 */
	public void setAntiAlias(boolean flag)
	{
		this.antiAlias = flag;
	}

	/**
	 * スタイル
	 * @param style
	 */
	public void setStyle(int style)
	{
		this.style = style;
	}

	/**
	 * ストロークの太さ
	 * @param width
	 */
	public void setStrokeWidth(int width)
	{
		this.strokeWidth = width;
	}

	/**
	 * 文字サイズ
	 * @param size
	 */
	public void setTextSize(float size)
	{
		textSize = size;
	}

	/**
	 * 文字サイズを得る
	 */
	public float getTextSize()
	{
		return textSize;
	}

	/**
	 * 文字列の幅を返す
	 * @param text 文字列
	 * @return 幅
	 */
	public int measureText(String text)
	{
		Graphics2D g = Util.mainView.drawCanvas.getGraphics2d();
		if(g != null)
		{
			Util.mainView.drawCanvas.setPaint2Font(this);
//			FontMetrics fm = g.getFontMetrics(Canvas.font);
			Font font;
			if(this.typeface == null)
				font = Util.mainPanel.canvas.font;
			else
				font = this.typeface.font;
			font = font.deriveFont(textSize);
			FontMetrics fm = g.getFontMetrics(font);
			return fm.stringWidth(text);
		}
		else
			return -1;
	}

	/**
	 * 文字列の幅を返す
	 * @param bufc 文字列
	 * @param index 開始位置
	 * @param count 文字数
	 * @return 幅
	 */
	public int measureText(char[] bufc, int index, int count)
	{
		String str = String.valueOf(bufc, index, count);
		return measureText(str);
	}

	/**
	 * 文字の種類
	 * @param type
	 */
	public void setTypeface(Typeface typeface)
	{
		if(typeface == null)
			this.typeface = Typeface.DEFAULT;
		else
			this.typeface = typeface;
	}


}
