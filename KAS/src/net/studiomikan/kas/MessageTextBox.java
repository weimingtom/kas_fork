package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;

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
		public float fontsize;
		public boolean bold;
		public boolean italic;
		public int color;
		public boolean shadow;
		public int shadowColor;
		public boolean edge;
		public int edgeColor;
		public Ch(char ch, int x, int y, float fontsize, boolean bold, boolean italic, int color, boolean shadow, int shadowColor, boolean edge, int edgeColor)
		{
			this.ch = ch;
			this.x = x;
			this.y = y;
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
			this(src.ch, src.x, src.y, src.fontsize, src.bold, src.italic, src.color, src.shadow, src.shadowColor, src.edge, src.edgeColor);
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

	/** 文字 */
	public ArrayList<Ch> text = new ArrayList<Ch>();

//	/** 文字 */
//	public ArrayList<Character> text;
//	/** 文字の位置 */
//	public ArrayList<Integer> textX;
//	/** 文字の位置 */
//	public ArrayList<Integer> textY;
//	/** フォントサイズ */
//	public ArrayList<Integer> fontSize;

	/** 縦書きフラグ */
	public boolean vertical;
	/** 縦書きの回転補正 */
	public static HashMap<Character, RotateChar> rotateChar = null;
	/** 角度 */
	public static final float RAD90 = (float)(90);

	public Paint paintText = new Paint();
	public Paint paintShadow = new Paint();
	public Paint paintEdge = new Paint();

	public static final Typeface TYPE_NORMAL = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
	public static final Typeface TYPE_BOLD = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
	public static final Typeface TYPE_ITALIC = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC);
	public static final Typeface TYPE_BOLD_ITALIC = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);

	public float opacity = 255;


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
	public void add(char ch, int x, int y, int pitch, float fontsize, boolean bold, boolean italic, int color, boolean shadow, int shadowColor, boolean edge, int edgeColor)
	{
		color += 0xff000000;
		shadowColor += 0xff000000;
		edgeColor += 0xff000000;
		Ch c = new Ch(ch, x, y, fontsize, bold, italic, color, shadow, shadowColor, edge, edgeColor);
		text.add(c);
		count++;
		width = x + (int)fontsize + pitch;
		height = y + (int)fontsize + pitch;
	}

	/**
	 * 描画
	 * @param c キャンバス
	 * @param paint ペイント
	 * @param shadow 影を描画するか
	 * @param sp 影用ペイント
	 * @param edge 縁を描画するか
	 * @param ep 縁用ペイント
	 */
	public void draw(Canvas c, int opacity)
	{
		Ch ch;
		char[] array = new char[1];
		for(int i = 0; i < count; i++)
		{
			ch = text.get(i);
			array[0] = ch.ch;

			paintText.setColor(ch.color);
			paintShadow.setColor(ch.shadowColor);
			paintEdge.setColor(ch.edgeColor);

			paintText.setTextSize(ch.fontsize);
			paintShadow.setTextSize(ch.fontsize);
			paintEdge.setTextSize(ch.fontsize);

			if(ch.bold && ch.italic)
			{
				paintText.setTypeface(TYPE_BOLD_ITALIC);
				paintShadow.setTypeface(TYPE_BOLD_ITALIC);
				paintEdge.setTypeface(TYPE_BOLD_ITALIC);
			}
			else if(ch.bold)
			{
				paintText.setTypeface(TYPE_BOLD);
				paintShadow.setTypeface(TYPE_BOLD);
				paintEdge.setTypeface(TYPE_BOLD);
			}
			else if(ch.italic)
			{
				paintText.setTypeface(TYPE_ITALIC);
				paintShadow.setTypeface(TYPE_ITALIC);
				paintEdge.setTypeface(TYPE_ITALIC);
			}
			else
			{
				paintText.setTypeface(TYPE_NORMAL);
				paintShadow.setTypeface(TYPE_NORMAL);
				paintEdge.setTypeface(TYPE_NORMAL);
			}

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
	 * @param shadow 影
	 * @param edge 縁取り
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
