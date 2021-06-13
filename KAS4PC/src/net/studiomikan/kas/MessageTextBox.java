package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;

import net.studiomikan.kas4pc.klib.Canvas;
import net.studiomikan.kas4pc.klib.Paint;
import net.studiomikan.kas4pc.klib.Paint.Style;
import net.studiomikan.kas4pc.klib.Typeface;

/**
 * メッセージ表示ボックス。これが一行を表す
 * @author okayu
 */
public class MessageTextBox
{
	/**
	 * 縦書き時の回転情報
	 * @author okayu
	 */
	public static class RotateChar
	{
		/** 回転角 */
		public float angle;
		/** 座標 */
		public float x;
		/** 座標 */
		public float y;
		/**
		 * 縦書き時の回転情報
		 * @param angle 角度
		 * @param x 座標
		 * @param y 座標
		 */
		public RotateChar(float angle, float x, float y)
		{
			this.angle = angle;
			this.x = x;
			this.y = y;
		}
	}

	/**
	 * 文字データ
	 * @author okayu
	 */
	public static class Ch
	{
		public char ch;
		public int x;
		public int y;
		public FontFace fontface;
		public float fontsize;
		public boolean bold;
		public boolean italic;
		public int color;
		public boolean shadow;
		public int shadowColor;
		public boolean edge;
		public int edgeColor;

		public Ch(char ch, int x, int y, FontFace fontface, float fontsize, boolean bold, boolean italic, int color, boolean shadow, int shadowColor, boolean edge, int edgeColor)
		{
			this.ch = ch;
			this.x = x;
			this.y = y;
			this.fontface = fontface;
			this.fontsize = fontsize;
			this.bold = bold;
			this.italic = italic;
			this.color = color;
			this.shadow = shadow;
			this.shadowColor = shadowColor;
			this.edge = edge;
			this.edgeColor = edgeColor;
		}

		public Ch(Ch src)
		{
			this(src.ch, src.x, src.y, src.fontface, src.fontsize, src.bold, src.italic, src.color, src.shadow, src.shadowColor, src.edge, src.edgeColor);
		}

		public void copy(Ch src)
		{
			ch = src.ch;
			x = src.x;
			y = src.y;
			fontface = src.fontface;
			fontsize = src.fontsize;
			bold = src.bold;
			italic = src.italic;
			color = src.color;
			shadow = src.shadow;
			shadowColor = src.shadowColor;
			edge = src.edge;
			edgeColor = src.edgeColor;
		}
	}

	/** 基点座標 */
	public int x = 0;
	/** 基点座標 */
	public int y = 0;
	/** 幅 */
	public int width = 0;
	/** 高さ */
	public int height = 0;
	/** 文字数 */
	public int count = 0;
	/** 縦書きフラグ */
	public boolean vertical;
	/** 透明度 */
	public float opacity = 255;
	/** 文字 */
	public ArrayList<Ch> text = new ArrayList<Ch>();

	/** 縦書きの回転補正 */
	public static HashMap<Character, RotateChar> rotateChar = null;
	/** 角度 */
	public static final float RAD90 = (float)(90);

	/** メインのペイント */
	public Paint paintText = new Paint();
	/** 影用ペイント */
	public Paint paintShadow = new Paint();
	/** 縁取り用ペイント */
	public Paint paintEdge = new Paint();

	/** 通常のフォント */
	public static final Typeface TYPE_NORMAL = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
	/** 太字フォント */
	public static final Typeface TYPE_BOLD = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
	/** 斜体フォント */
	public static final Typeface TYPE_ITALIC = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
	/** 太字斜体フォント */
	public static final Typeface TYPE_BOLD_ITALIC = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC);

	/** 最後に追加された文字 */
	public int lastChWidth = 0;

	/**
	 * 回転の必要な文字についての情報を生成
	 */
	private void createMap()
	{
		rotateChar = new HashMap<Character, RotateChar>();
		rotateChar.put('。', new RotateChar(  0.0f,  0.7f, -0.4f ));
		rotateChar.put('、', new RotateChar(  0.0f,  0.7f, -0.4f ));
		rotateChar.put('「', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('」', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('『', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('』', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('（', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('）', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('〔', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('〕', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('｛', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('｝', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('〈', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('〉', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('《', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('》', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('【', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('】', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('［', new RotateChar( RAD90, -0.1f, -0.1f ));
		rotateChar.put('］', new RotateChar( RAD90,  0.5f, -0.1f ));
		rotateChar.put('＜', new RotateChar( RAD90,  0.2f, -0.05f ));
		rotateChar.put('＞', new RotateChar( RAD90,  0.1f, -0.1f ));
		rotateChar.put('“', new RotateChar(  0.0f,  0.0f,  0.4f ));
		rotateChar.put('”', new RotateChar(  0.0f,  0.0f,  0.4f ));
		rotateChar.put('‘', new RotateChar(  0.0f,  0.0f,  0.4f ));
		rotateChar.put('’', new RotateChar(  0.0f,  0.0f,  0.4f ));

		rotateChar.put('：', new RotateChar( RAD90,  0.25f, -0.05f ));
		rotateChar.put('；', new RotateChar( RAD90,  0.25f, -0.05f ));

		rotateChar.put('ー', new RotateChar( RAD90,  0.15f, -0.05f ));
		rotateChar.put('～', new RotateChar( RAD90,  0.15f, -0.05f ));
		rotateChar.put('―', new RotateChar( RAD90,  0.15f, -0.05f ));
		rotateChar.put('＝', new RotateChar( RAD90,  0.15f, -0.05f ));
		rotateChar.put('－', new RotateChar( RAD90,  0.15f, -0.05f ));
		rotateChar.put('｜', new RotateChar( RAD90,  0.10f, -0.1f ));

		rotateChar.put('ぁ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ぃ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ぅ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ぇ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ぉ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('っ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ゃ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ゅ', new RotateChar( 0.0f,  0.2f, -0.05f ));
		rotateChar.put('ょ', new RotateChar( 0.0f,  0.2f, -0.05f ));
	}

	/**
	 * メッセージ表示ボックス。これが一行を表す
	 * @param vertical 縦書きかどうか
	 */
	public MessageTextBox(boolean vertical)
	{
		if(rotateChar == null) createMap();
		this.vertical = vertical;
		initPaints();
	}

	/**
	 * メッセージ表示ボックス。これが一行を表す
	 * @param owner 親のメッセージレイヤ
	 * @param src コピー元
	 */
	public MessageTextBox(MessageLayer owner, MessageTextBox src)
	{
		copyText(src.text, text);
		x = src.x;
		y = src.y;
		width = src.width;
		height = src.height;
		count = src.count;
		vertical = src.vertical;
		opacity = src.opacity;
		initPaints();
	}

	/**
	 * 描画用 Paint を初期化
	 */
	public void initPaints()
	{
		// 文字描画用
		paintText.setAntiAlias(true);
		paintText.setStyle(Style.FILL_AND_STROKE);
		// 影用
		paintShadow.setAntiAlias(true);
		paintShadow.setStyle(Style.FILL_AND_STROKE);
		// 縁取り
		paintEdge.setAntiAlias(true);
		paintEdge.setStyle(Paint.Style.STROKE);
		paintEdge.setStrokeWidth(2);
	}

	/**
	 * 文字をコピー
	 * @param src
	 * @param dst
	 */
	public void copyText(ArrayList<Ch> src, ArrayList<Ch> dst)
	{
		int size = src.size();
		for(int i = 0; i < size; i++)
			dst.add(new Ch(src.get(i)));
	}

	/**
	 * 文字を追加
	 * @param ch 文字
	 * @param x 座標
	 * @param y 座標
	 * @param pitch 文字間隔
	 * @param fontSize フォントサイズ
	 */
	public void add(char ch, int x, int y, int pitch, FontFace fontface, float fontsize, boolean bold, boolean italic, int color, boolean shadow, int shadowColor, boolean edge, int edgeColor)
	{
		color += 0xff000000;
		shadowColor += 0xff000000;
		edgeColor += 0xff000000;
		Ch c = new Ch(ch, x, y, fontface, fontsize, bold, italic, color, shadow, shadowColor, edge, edgeColor);
		text.add(c);
		count++;

		resetPaints(c);

		char[] bufc = { ch };
		int w = (int)paintText.measureText(bufc, 0, 1);
		width = x + w + pitch;
		height = y + (int)fontsize + pitch;

		lastChWidth = w;
	}

	/**
	 * 最後に追加した文字の文字幅を取得
	 */
	public int getLastCharWidth()
	{
		return lastChWidth;
	}

	/**
	 * 与えられた文字に合わせて paint を設定
	 * @param ch 文字
	 */
	public void resetPaints(Ch ch)
	{
		paintText.setColor(ch.color);
		paintShadow.setColor(ch.shadowColor);
		paintEdge.setColor(ch.edgeColor);

		paintText.setTextSize(ch.fontsize);
		paintShadow.setTextSize(ch.fontsize);
		paintEdge.setTextSize(ch.fontsize);

		if(ch.bold && ch.italic)
		{
			paintText.setTypeface(ch.fontface.bolditalic);
			paintShadow.setTypeface(ch.fontface.bolditalic);
			paintEdge.setTypeface(ch.fontface.bolditalic);
		}
		else if(ch.bold)
		{
			paintText.setTypeface(ch.fontface.bold);
			paintShadow.setTypeface(ch.fontface.bold);
			paintEdge.setTypeface(ch.fontface.bold);
		}
		else if(ch.italic)
		{
			paintText.setTypeface(ch.fontface.italic);
			paintShadow.setTypeface(ch.fontface.italic);
			paintEdge.setTypeface(ch.fontface.italic);
		}
		else
		{
			paintText.setTypeface(ch.fontface.normal);
			paintShadow.setTypeface(ch.fontface.normal);
			paintEdge.setTypeface(ch.fontface.normal);
		}
	}

	/**
	 * 描画
	 * @param c キャンバス
	 * @param opacity 透明度
	 */
	public void draw(Canvas c, int opacity)
	{
		Ch ch;
		char[] array = new char[1];
		for(int i = 0; i < count; i++)
		{
			ch = text.get(i);
			array[0] = ch.ch;

			resetPaints(ch);

			paintText.setAlpha(opacity);
			paintShadow.setAlpha(opacity);
			paintEdge.setAlpha(opacity);

			drawChar(c, array, x+ch.x, y+ch.y, paintText, ch.shadow, paintShadow, ch.edge, paintEdge);
		}
//		Rect r = new Rect(x, y, x+width, y+height);
//		Paint p = new Paint();
//		p.setColor(0x81ff0000);
//		c.drawRect(r, p);
	}

	/**
	 * 一文字描画
	 * @param c キャンバス
	 * @param ch 文字
	 * @param x 座標
	 * @param y 座標
	 * @param paint ペイント
	 * @param shadow 影
	 * @param shadowPaint 影用ペイント
	 * @param edge 縁取り
	 * @param edgePaint 縁取り用ペイント
	 */
	protected void drawChar(Canvas c, char[] ch, int x, int y, Paint paint, boolean shadow, Paint shadowPaint, boolean edge, Paint edgePaint)
	{
		RotateChar r = null;
		if(vertical) r = rotateChar.get(ch[0]);
		if(r != null)	// 回転する場合
		{
			float cx = x + paint.getTextSize()/2;
			float cy = y - paint.getTextSize()/2;
			x += paint.getTextSize() * r.x;
			y += paint.getTextSize() * r.y;
			c.save();
			c.rotate(r.angle, cx, cy);
			if(shadow)
			{
				// 線形変換
				double mc2 = 2 * Math.cos(r.angle);
				double ms2 = 2 * Math.sin(r.angle);
				int sx = (int)( mc2 + ms2 );
				int sy = (int)( -ms2 + mc2 );
				c.drawText(	ch, 0, 1, x+sx, y+sy, shadowPaint ); // 陰
			}
			if(edge)
			{
				c.drawText(	ch, 0, 1, x, y, edgePaint ); // 縁取り
			}
			c.drawText(	ch, 0, 1, x, y, paint ); // 文字
			c.restore();
		}
		else
		{
			if(shadow)
				c.drawText(	ch, 0, 1, x+2, y+2, shadowPaint ); // 陰
			if(edge)
				c.drawText(	ch, 0, 1, x, y, edgePaint ); // 縁取り
			c.drawText(	ch, 0, 1, x, y, paint ); // 文字
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < count; i++)
			sb.append(text.get(i).ch);
		return sb.toString();
	}

}
