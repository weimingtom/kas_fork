package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * メッセージレイヤ
 * @author okayu
 */
class MessageLayer extends KASLayer
{
//	これらはKASLayerから継承
//	private boolean visible = false;				// trueで表示
//	public char layerType = 'k';					// レイヤのタイプ
//	private int x = 0;								// x座標
//	private int y = 0;								// y座標
//	private int width = 100;						// 幅
//	private int height = 100;						// 高さ

	/** （デフォルト）フォントサイズ */
	public float defFontSize = 22f;
	/** （デフォルト）文字の色　初期値白 */
	public int defColor = 0xffffff;
	/** （デフォルト）影を描画するかどうか */
	public boolean defShadow = true;
	/** （デフォルト）縁取りを描画するかどうか */
	public boolean defEdge = true;
	/** （デフォルト）太字にするかどうか */
	public boolean defBold = false;
	/** （デフォルト）斜体にするかどうか */
	public boolean defItalic = false;
	/** （デフォルト）文字影の色 */
	public int defShadowColor = 0x000000;
	/** （デフォルト）縁取りの色 */
	public int defEdgeColor = 0x000000;

	/** フォントサイズ */
	private float fontSize = 22f;
	/** 文字の色　初期値白 */
	private int color = 0xffffff;
	/** 影を描画するかどうか */
	private boolean shadow = true;
	/** 縁取りを描画するかどうか */
	private boolean edge = true;
	/** 太字にするかどうか */
	private boolean bold = false;
	/** 斜体にするかどうか */
	private boolean italic = false;
	/** 文字影の色 */
	private int shadowColor = 0x000000;
	/** 縁取りの色 */
	private int edgeColor = 0x000000;

	/** （デフォルト）行間 */
	public int defLinespacing = 18;
	/** （デフォルト）行の高さ */
	public int defLinesize = 22;
	/** （デフォルト）字間 */
	public int defPitch = 0;
	/** （デフォルト）自動改行 */
	public boolean defAutoreturn = true;
	/** （デフォルト）文字揃えの位置 l:左 c:中央 r:右 */
	public char defAlign = 'l';

	/** 行間 */
	private int linespacing = 18;
	/** 行の高さ */
	private int linesize = 22;
	/** 字間 */
	private int pitch = 0;
	/** 自動改行 */
	private boolean autoreturn = true;
	/** 文字揃えの位置 l:左 c:中央 r:右 */
	private char align = 'l';

	/** 裏にもメッセージを出力するかどうか */
	public boolean withback = false;
	/** 縦書き */
	public boolean vertical = false;
	/** 右端(縦書きの場合は下端)に確保する禁則処理用余白 */
	public int marginRCh = 2;

	/** 次の一文字の位置 */
	private int textX = 0;
	/** 次の一文字の位置 */
	private int textY = 0;
	/** メッセージの描画開始位置 */
	private int startX = 0;
	/** メッセージの行 */
	private ArrayList<MessageTextBox> lines = new ArrayList<MessageTextBox>();
	/** 現在の行 */
	private MessageTextBox line = new MessageTextBox(false);

	/** 枠の色 */
	private int frameColor = 0x000000;
	/** 枠の透明度　文字の透明度ではない */
	private int opacity = 127;
	/** レイヤの透明度 */
	private int layerOpacity = 255;
	/** （デフォルト）リンクの色 */
	public int defLinkColor = 0x00469d;
	/** （デフォルト）リンクの濃度 */
	public int defLinkOpacity = 127;

	/** 枠の外から文字までの距離 */
	private int margin_left = 10;
	/** 枠の外から文字までの距離 */
	private int margin_right = 10;
	/** 枠の外から文字までの距離 */
	private int margin_top = 10;
	/** 枠の外から文字までの距離 */
	private int margin_bottom = 10;

	/** 枠の描画用矩形 */
	private Rect rect = null;
	/** フレーム画像のファイル名 */
	private String frameFileName = "";
	/** フレーム画像 */
	private Bitmap frame = null;
	/** フレーム画像描画元矩形 */
	private Rect rect_src = null;

	/** 枠の描画用 Paint */
	private Paint paint = null;

	/** 改ページグリフのレイヤ */
	private Layer pageglyph = null;
	/** 改行グリフのレイヤ */
	private Layer lineglyph = null;
	/** グリフを固定表示 */
	public boolean glyphFixedPosition = false;
	/** グリフ固定表示の位置 */
	public int glyphFixedLeft = 0;
	/** グリフ固定表示の位置 */
	public int glyphFixedTop = 0;
	/** 改ページグリフのファイル名 */
	public String pageBreakGlyph = "pagebreak";
	/** 改行グリフのファイル名 */
	public String lineBreakGlyph = "linebreak";
//	/** 改ページグリフの幅 */
//	public int pageBreakWidth = 100;
//	/** 改ページグリフの高さ */
//	public int pageBreakHeight = 100;
//	/** 改行グリフの幅 */
//	public int lineBreakWidth = 100;
//	/** 改行グリフの高さ */
//	public int lineBreakHeight = 100;

	/** 現在のリンクボタン */
	private LinkButton linkButtonBuff = null;
	/** リンク中 */
	private boolean inLink = false;
	/** リンクの番号 */
	private int linkNum = 0;
	/** リンクボタン */
	private ArrayList<LinkButton> link = null;
	/** グラフィカルボタン */
	private ArrayList<ButtonLayer> button = null;

	/** 行頭禁則文字２ */
	static String followingStringWeak =
		"!.?､･ｧｨｩｪｫｬｭｮｯｰ・？！ーぁぃぅぇぉっゃゅょゎァィゥェォッャュョヮヵヶ";
	/** 行頭禁則文字１ */
	static String followingString =
		"%),:;]}｡｣ﾞﾟ。，、．：；゛゜ヽヾゝゞ々’”）〕］｝〉》」』】°′″℃￠％‰" + followingStringWeak;
	/** 行末禁則文字 */
	static String leadingString =
		"\\$([{｢‘“（〔［｛〈《「『【￥＄￡";

	/** このメッセージレイヤの保存用 */
	private JSONObject json = new JSONObject();

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	public MessageLayer(Context context)
	{
		layerType = 'M';
		width = 760;
		height = 410;
		x = 20;
		y = 20;
		opacity = 127;

		// フレーム用ペイント
		paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setColor(frameColor);
		paint.setAlpha(opacity);
		// 描画用Rect
		rect = new Rect();
		rect_src = new Rect();
		// グリフ
		pageglyph = new Layer(context);
		pageglyph.setVisible(false);
		lineglyph = new Layer(context);
		lineglyph.setVisible(false);
		// リンクボタン
		link = new ArrayList<LinkButton>();
		// グラフィカルボタン
		button = new ArrayList<ButtonLayer>();

		// Config.java を適用
		Config.MessageLayer_config(this, Util.getMokaScript());

		// グリフの作成
		loadGlyph(context, pageglyph, pageBreakGlyph);
		loadGlyph(context, lineglyph, lineBreakGlyph);
		// 一度クリア
		clearMessage();
	}

	/**
	 * move 中の setPos
	 */
	@Override
	public void move_setPos(int x, int y)
	{
		this.setPos(x, y);
	}

	/**
	 * move 中の setOpacity
	 */
	@Override
	public void move_setOpacity(int opacity)
	{
		this.setLayerOpacity(opacity);
	}

	/**
	 * move での開始濃度
	 */
	@Override
	protected int move_getStartOpacity()
	{
		return layerOpacity;
	}

	/**
	 * 描画イベント
	 */
	@Override
	public void onDraw(Context context, Canvas c, long nowTime)
	{
		if(visible)
		{
			if(frame != null && frame.isRecycled())
				loadFrame(context.getResources(), this.frameFileName);

			// フレーム描画
			if(frame != null && !frame.isRecycled())
			{
				rect.set(x, y, x+width, y+height);
				c.drawBitmap(frame, rect_src, rect, paint);
			}
			else
			{
				rect.set(x, y, x+width, y+height);
				c.drawRect(rect, paint);
			}
			// 文字描画
			drawMessage(c);
			// グリフ描画
			pageglyph.onDraw(context, c, nowTime);
			lineglyph.onDraw(context, c, nowTime);

			// リンクボタン描画
			int len;
			len = link.size();
			LinkButton l;
			for(int i = 0; i< len; i++)
			{
				l = link.get(i);
				l.onDraw(c);
			}
			// グラフィカルボタン描画
			len = button.size();
			ButtonLayer b;
			for(int i = 0; i < len; i++)
			{
				b = button.get(i);
				b.onDraw(c);
			}
		}
	}

	/**
	 * メッセージの描画
	 * @param c キャンバス
	 */
	private void drawMessage(Canvas c)
	{
		if(lines == null) return;
		int size = lines.size();
		for(int i = 0; i < size; i++)
			lines.get(i).draw(c, layerOpacity);
	}

	/**
	 * メッセージの追加
	 * @param s メッセージ文字列
	 */
	public void addMessage(String s)
	{
		if(s == null) return;

		int limitWidth;	// 一行の限界幅
		if(vertical)
			limitWidth = (int)(height - margin_top - margin_bottom - (int)fontSize - pitch - marginRCh*fontSize);
		else
			limitWidth = (int)(width - margin_left - margin_right - (int)fontSize - pitch - marginRCh*fontSize);

		int len = s.length();
		char ch;
		LinkButton l;
		for(int i = 0; i < len; i++)
		{
			ch = s.charAt(i);

			if(ch == '\n')
				cr();
			else
			{
				// 自動改行
				if(autoreturn)
				{
					if(vertical)
					{
						if(textY >= limitWidth && !checkJapaneseHyphenation(ch))
							cr();
					}
					else
					{
						if(textX >= limitWidth && !checkJapaneseHyphenation(ch))
							cr();
					}
				}
				// リンク中ならリンクへ追加
				if(inLink)
				{
					if(vertical)
						linkButtonBuff.add(textX, textY, (int)fontSize, linesize+pitch);
					else
						linkButtonBuff.add(textX, textY+linesize-(int)fontSize, (int)fontSize+pitch, (int)fontSize+5);
				}
				// リンクの行末にスペースを追加
				for(int j = link.size()-1; j >= 0; j--)
				{
					l = link.get(j);
					if(!l.locked)
					{
						if(vertical)
							l.addEndSpace(textX, textY, (int)fontSize, linesize+pitch);
						else
							l.addEndSpace(textX, textY+linesize-(int)fontSize, (int)fontSize+pitch, (int)fontSize+5);
					}
				}
				// 追加
				line.add(ch, textX, textY, pitch, fontSize, bold, italic, color, shadow, shadowColor, edge, edgeColor);

				if(vertical)
					textY += (int)fontSize + pitch;
				else
					textX += (int)fontSize + pitch;

				initLinePos(line);	// 位置移動
				initAllLinkPos();	// リンクの位置移動
			}
		}
		// グリフの位置を設定
		resetGlyphPos();
	}

	/**
	 * 改行
	 */
	private void cr()
	{
		line = new MessageTextBox(vertical);
		lines.add(line);
		if(vertical)
		{
			textY = startX;
			textX -= linesize + linespacing;
			initLinePos(line);
		}
		else
		{
			textY += linesize + linespacing;
			textX = startX;
			initLinePos(line);
		}
		if(inLink)
		{
			// リンク内部で改行した場合は、コピーを用意する
			String storage = linkButtonBuff.storage;
			String target = linkButtonBuff.target;
			boolean countpage = linkButtonBuff.countpage;
			String clickse = linkButtonBuff.clickse;
			Sound clicksebuf = linkButtonBuff.clicksebuf;
			int color = linkButtonBuff.color;
			String exp = linkButtonBuff.exp;
			endLink();
			linkNum--; // 改行の場合は同じリンクにする
			startLink(storage, target, countpage, clickse, clicksebuf, color, exp);
		}
		lockLinkButton();	// リンクの移動をロック
	}

	/**
	 * 文字が禁則文字かどうかチェック
	 * @param c 文字
	 * @return 禁則文字ならtrue
	 */
	private boolean checkJapaneseHyphenation(char c)
	{
		if(followingString.indexOf(c) != -1) // 見つかった
			return true;
		else
			return false;
	}

	/**
	 * 行の位置を初期化
	 */
	public void initLinePos(MessageTextBox line)
	{
		if(vertical)
		{
			line.x = x + width - margin_left - (int)fontSize;
			switch(align)
			{
			case 'l':	// 上
			case 't':
				line.y = y + margin_top + (int)fontSize;
				break;
			case 'c':	// 中央
				int center = y + margin_top + (height-(startX+margin_top)-margin_bottom)/2;
				line.y = center - line.height/2 + (int)fontSize;
				break;
			case 'r':	// 下
			case 'b':
				int bottom = y + height - margin_bottom;
				line.y = bottom - line.height;
				break;
			}
		}
		else
		{
			line.y = y + margin_top + linesize;
			switch(align)
			{
			case 'l':
			case 't':
				line.x = x + margin_left;
				break;
			case 'c':
				int center = x + margin_left + (width-(startX+margin_left)-margin_right)/2;
				line.x = center - line.width/2;
				break;
			case 'r':
			case 'b':
				int right = x + width - margin_right;
				line.x = right - line.width;
				break;
			}
		}
	}

	/**
	 * 全行の位置を初期化
	 */
	public void initAllLinePos()
	{
		for(int i = lines.size()-1; i >= 0; i--)
			initLinePos(lines.get(i));
	}

	/**
	 * リンクの位置を初期化
	 * @param lb
	 */
	private void initLinkPos(LinkButton lb)
	{
		if(vertical)
		{
			lb.x = x + width - margin_left - (int)fontSize;
			switch(align)
			{
			case 'l':	// 上
			case 't':
				lb.y = y + margin_top;
				break;
			case 'c':	// 中央
				int center = y + margin_top + (height-(startX+margin_top)-margin_bottom)/2;
				lb.y = center - lb.height/2;
				break;
			case 'r':	// 下
			case 'b':
				int bottom = y + height - margin_bottom;
				lb.y = bottom - lb.height - (int)fontSize;
				break;
			}
		}
		else
		{
			lb.y = y + margin_top;
			switch(align)
			{
			case 'l':
			case 't':
				lb.x = x + margin_left;
				break;
			case 'c':
				int center = x + margin_left + (width-(startX+margin_left)-margin_right)/2;
				lb.x = center - lb.width/2;
				break;
			case 'r':
			case 'b':
				int right = x + width - margin_right;
				lb.x = right - lb.width;
				break;
			}
		}
	}

	/**
	 * 全リンクの位置を初期化
	 */
	private void initAllLinkPos()
	{
		for(int i = link.size()-1; i >= 0; i --)
			initLinkPos(link.get(i));
	}

	/**
	 * リンクの開始
	 * @param storage ジャンプ先シナリオファイル名
	 * @param target ジャンプ先ラベル名
	 * @param countpage countpage 属性
	 * @param clickse クリック時効果音名
	 * @param clicksebuf クリック時効果音バッファ
	 * @param color リンク色
	 * @param exp スクリプト
	 */
	public void startLink(String storage, String target, boolean countpage, String clickse, Sound clicksebuf, int color, String exp)
	{
		inLink = true;
		LinkButton lb = new LinkButton(
				linkNum,
				storage,
				target,
				countpage,
				clickse,
				clicksebuf,
				color,
				defLinkColor,
				exp);
		linkButtonBuff = lb;
	}

	/**
	 * リンクの終わり
	 */
	public void endLink()
	{
		inLink = false;
		LinkButton lb = linkButtonBuff;
		linkButtonBuff = null;
		if(lb != null)
		{
			lb.setVisible(true);
			link.add(lb);
			initLinkPos(lb);
			linkNum++;
		}
	}

	/**
	 * リンクを移動しないようロックする
	 */
	private void lockLinkButton()
	{
		LinkButton l;
		for(int i = link.size()-1; i >= 0; i--)
		{
			l = link.get(i);
			l.locked = true;
		}
	}

	/**
	 * リンクの状態をオンにする
	 */
	private void setLinkState(int linkNum)
	{
		LinkButton l;
		for(int i = link.size()-1; i >= 0; i--)
		{
			l = link.get(i);
			if(l.linkNum == linkNum)
				l.state = 1;
			else
				l.state = 0;
		}
	}

	/**
	 * locate タグ　次の位置文字の描画位置を変更
	 * @param x 座標
	 * @param y 座標
	 */
	public void locate(Integer x, Integer y)
	{
		if(x != null && y != null && textX == x && textY == y) return;

		int tmpX = textX;
		int tmpY = textY;

		if(line.count != 0)
			cr();

		if(vertical)	// 無様なつじつま合わせ
			if(x != null) x = -line.x + x + (int)fontSize + margin_left;

		if(x != null) textX = startX + x;
		else textX = tmpX;
		if(y != null) textY = y;
		else textY = tmpY;
	}

	/**
	 * メッセージのクリア
	 */
	public void clearMessage()
	{
		textX = 0;
		textY = 0;
		startX = 0;

		line = new MessageTextBox(vertical);
		lines.clear();
		lines.add(line);
		initLinePos(line);

		resetStyle();
		resetFont();
		resetGlyphPos();

		// リンク関連をリセットしてる
		// つまり、link~endlink間に呼ばれるとバグる
		initAllLinkPos();
		linkButtonBuff = null;
		inLink = false;
		int size = link.size();
		for(int i = 0; i < size; i++)
			link.get(i).clear();
		link.clear();
		linkNum = 0;

		// グラフィカルボタンを消去
		int len = button.size();
		for(int i = 0; i < len; i++)
			button.get(i).clear();
		button.clear();

		// インデントは解除される
		endIndent();
	}

	/**
	 * フォントのリセット
	 */
	public void resetFont()
	{
		setFontSize(defFontSize);
		setColor(defColor);
		setShadow(defShadow);
		setShadowColor(defShadowColor);
		setEdge(defEdge);
		setEdgeColor(defEdgeColor);
		setBold(defBold);
		setItalic(defItalic);
	}

	/**
	 * フォントの設定
	 * @param elm 属性値
	 */
	public void setFont(HashMap<String, String> elm)
	{
		String size = elm.get("size");
//		String face = elm.get("face");
		String color = elm.get("color");
		String bold = elm.get("bold");
		String italic = elm.get("italic");
//		String rubysize = elm.get("rubysize");
//		String rubyoffset = elm.get("rubyoffset");
		String shadow = elm.get("shadow");
		String edge = elm.get("edge");
		String shadowcolor = elm.get("shadowcolor");
		String edgecolor = elm.get("edgecolor");

		if(size != null)
		{
			if(size.equals("default")) setFontSize(defFontSize);
			else if(Util.isFloat(size)) setFontSize(Integer.decode(size));
		}
		if(color != null)
		{
			if(color.equals("default")) setColor(defColor);
			else if(Util.isInteger(color)) setColor(Integer.decode(color));
		}
		if(bold != null)
		{
			if(bold.equals("default")) setBold(defBold);
			else if(Util.isBoolean(bold)) setBold(Boolean.parseBoolean(bold));
		}
		if(italic != null)
		{
			if(italic.equals("default")) setItalic(defItalic);
			else if(Util.isBoolean(italic)) setItalic(Boolean.parseBoolean(italic));
		}
		if(shadow != null)
		{
			if(shadow.equals("default")) setShadow(defShadow);
			else if(Util.isBoolean(shadow)) setShadow(Boolean.parseBoolean(shadow));
		}
		if(edge != null)
		{
			if(edge.equals("default")) setEdge(defEdge);
			if(Util.isBoolean(edge)) setEdge(Boolean.parseBoolean(edge));
		}
		if(shadowcolor != null)
		{
			if(shadowcolor.equals("default")) setShadowColor(defShadowColor);
			else if(Util.isInteger(shadowcolor)) setShadowColor(Integer.decode(shadowcolor));
		}
		if(edgecolor != null)
		{
			if(edgecolor.equals("default")) setEdgeColor(Integer.decode(edgecolor));
			else if(Util.isInteger(edgecolor)) setEdgeColor(Integer.decode(edgecolor));
		}
	}

	/**
	 * フォントのデフォルト値の設定
	 * @param elm 属性値
	 */
	public void setDefFont(HashMap<String, String> elm)
	{
		String size = elm.get("size");
		String color = elm.get("color");
		String bold = elm.get("bold");
		String italic = elm.get("italic");
		String shadow = elm.get("shadow");
		String edge = elm.get("edge");
		String shadowcolor = elm.get("shadowcolor");
		String edgecolor = elm.get("edgecolor");

		if(size != null && Util.isFloat(size))			defFontSize = (Integer.decode(size));
		if(color != null && Util.isInteger(color))		defColor = (Integer.decode(color));
		if(bold != null && Util.isBoolean(bold))		defBold = (Boolean.parseBoolean(bold));
		if(italic != null && Util.isBoolean(italic))	defItalic = (Boolean.parseBoolean(italic));
		if(shadow != null && Util.isBoolean(shadow))	defShadow = (Boolean.parseBoolean(shadow));
		if(edge != null && Util.isBoolean(italic))		defEdge = (Boolean.parseBoolean(edge));
		if(shadowcolor != null && Util.isInteger(shadowcolor)) defShadowColor = (Integer.decode(shadowcolor));
		if(edgecolor != null && Util.isInteger(edgecolor)) defEdgeColor = (Integer.decode(edgecolor));
	}

	/**
	 * スタイルのリセット
	 */
	public void resetStyle()
	{
		setLineSpacing(defLinespacing);
		setLineSize(defLinesize);
		setPitch(defPitch);
		setAutoReturn(defAutoreturn);
		setAlign(defAlign);
	}

	/**
	 * スタイルの設定
	 * @param elm
	 */
	public void setStyle(HashMap<String, String> elm)
	{
		String linespacing = elm.get("linespacing");
		String linesize = elm.get("linesize");
		String pitch = elm.get("pitch");
		String autoreturn = elm.get("autoreturn");
		String align = elm.get("align");

		if(linespacing != null)
		{
			if(linespacing.equals("default")) setLineSpacing(defLinespacing);
			else if(Util.isInteger(linespacing)) setLineSpacing(Integer.decode(linespacing));
		}
		if(linesize != null)
		{
			if(linesize.equals("default")) setLineSize(defLinesize);
			else if(Util.isInteger(linesize)) setLineSize(Integer.decode(linesize));
		}
		if(pitch != null)
		{
			if(pitch.equals("default")) setPitch(defPitch);
			else if(Util.isInteger(pitch)) setPitch(Integer.decode(pitch));
		}
		if(autoreturn != null)
		{
			if(autoreturn.equals("default")) setAutoReturn(defAutoreturn);
			else if(Util.isBoolean(autoreturn)) setAutoReturn(Boolean.parseBoolean(autoreturn));
		}
		if(align != null)
		{
			if(align.equals("default")) setAlign(defAlign);
			else if(align.length() != 0) setAlign(align.charAt(0));
		}
	}

	/**
	 * スタイルのデフォルト値の設定
	 * @param elm
	 */
	public void setDefStyle(HashMap<String, String> elm)
	{
		String linespacing = elm.get("linespacing");
		String linesize = elm.get("linesize");
		String pitch = elm.get("pitch");
		String autoreturn = elm.get("autoreturn");
		String align = elm.get("align");

		if(linespacing != null && Util.isInteger(linespacing))
			defLinespacing = Integer.decode(linespacing);
		if(linesize != null && Util.isInteger(linesize))
			defLinesize = Integer.decode(linesize);
		if(pitch != null && Util.isInteger(pitch))
			defPitch = Integer.decode(pitch);
		if(autoreturn != null && Util.isBoolean(autoreturn))
			defAutoreturn = Boolean.parseBoolean(autoreturn);
		if(align != null && align.length() != 0)
			defAlign = align.charAt(0);
	}

	/**
	 * グリフ画像の読み込み
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @param width 幅
	 * @param height 高さ
	 */
	public void loadGlyph(Context context, String filename, String type)
	{
		Layer layer;
		if(type.equals("page"))
			layer = pageglyph;
		else
			layer = lineglyph;
		loadGlyph(context, layer, filename);
	}

	/**
	 * グリフ画像の読み込み
	 * @param context コンテキスト
	 * @param glyph レイヤ（グリフ）
	 * @param filename ファイル名
	 */
	public void loadGlyph(Context context, Layer glyph, String filename)
	{
		glyph.loadImage(context, filename);
	}

	/**
	 * グリフの表示　
	 * @param type タイプ　0:ライン 1:ページ
	 */
	public void glyphOn(int type, long nowTime)
	{
		switch(type)
		{
		case 0:
			pageglyph.setVisible(false);
			lineglyph.setVisible(true);
			lineglyph.startAnimation(nowTime, true);
			break;
		case 1:
			pageglyph.setVisible(true);
			lineglyph.setVisible(false);
			pageglyph.startAnimation(nowTime, true);
			break;
		default:
			pageglyph.setVisible(false);
			lineglyph.setVisible(false);
		}
	}

	/**
	 * グリフの表示終了
	 */
	public void glyphOff()
	{
		pageglyph.setVisible(false);
		lineglyph.setVisible(false);
	}

	/**
	 * グリフ位置のリセット
	 */
	public void resetGlyphPos()
	{
		int glyphX = 0;
		int glyphY = 0;
		if(vertical)
		{
			glyphX = x + width - margin_left + textX - (int)fontSize;
			switch(align)
			{
			case 'l':	// 上
			case 't':
				glyphY = y + margin_top + line.height + (int)fontSize + pitch;
				break;
			case 'c':	// 中央
				int center = y + margin_top + (height-(startX+margin_top)-margin_bottom)/2;
				glyphY = center + line.height/2 + (int)fontSize + pitch;
				break;
			case 'r':	// 下
			case 'b':
				int bottom = y + height - margin_bottom;
				glyphY = bottom;
				break;
			}
		}
		else
		{
			glyphY = this.y + margin_top + textY + linesize;
			switch(align)
			{
			case 'l':
			case 't':
				glyphX = this.x + margin_left + textX;
				break;
			case 'c':
				int center = x + margin_left + (width-(startX+margin_left)-margin_right)/2;
				glyphX = center + line.width/2;
				break;
			case 'r':
			case 'b':
				int right = x + width - margin_right;
				glyphX = right;
			}
		}
		setGlyphPos(glyphX, glyphY);
	}

	/**
	 * グリフの位置を設定
	 * @param x グリフの座標（底辺）
	 * @param y グリフの座標（底辺）
	 */
	public void setGlyphPos(int x, int y)
	{
		if(glyphFixedPosition)
		{
			pageglyph.setPos(this.x + glyphFixedLeft, this.y + glyphFixedTop);
			lineglyph.setPos(this.x + glyphFixedLeft, this.y + glyphFixedTop);
		}
		else
		{
			pageglyph.setPos(x, y - pageglyph.getHeight());
			lineglyph.setPos(x, y - lineglyph.getHeight());
		}
	}

	/**
	 * グラフィカルボタンの追加
	 * @param resources リソース
	 * @param filename ファイル名
	 * @param width 幅
	 * @param height 高さ
	 * @param left 左位置
	 * @param top 上位置
	 * @param storage ジャンプ先シナリオファイル名
	 * @param target ジャンプ先ラベル名
	 * @param countpage countpage 属性
	 * @param exp スクリプト
	 * @param clickse クリック時効果音名
	 * @param clickbuf クリック時効果音バッファ
	 */
	public void addButton(
			Resources resources,
			String filename, int width, int height,
			int left, int top,
			String storage, String target, boolean countpage, String exp,
			String clickse, Sound clickbuf)
	{
		ButtonLayer b = new ButtonLayer(
				resources, filename, Layer.scale,
				width, height,
				clickse, clickbuf,
				storage, target, countpage, exp,
				null);
		b.setPos(this.x+this.margin_left+left, this.y+this.margin_top+top);
		b.visible = true;
		button.add(b);
	}

	/**
	 * インデントする
	 */
	public void setIndent()
	{
		startX = textX;
	}

	/**
	 * インデント終了
	 */
	public void endIndent()
	{
		startX = 0;
	}

	/**
	 * フレーム画像の読み込み
	 * @param resource リソース
	 * @param filename ファイル名
	 */
	public void loadFrame(Resources resource, String filename)
	{
		if(filename == null || filename.length() == 0)	// 普通枠
			freeFrameImage();
		else
		{
			freeFrameImage();
			frame = ResourceManager.loadBitmap(resource, filename, 1);
			if(frame != null)
			{
				frameFileName = filename;
				setPos(x, y);
				rect_src.set(0, 0, frame.getWidth(), frame.getHeight());
			}
		}
	}

	/**
	 * フレーム画像の開放
	 */
	public void freeFrameImage()
	{
		if(frame != null)
		{
			ResourceManager.freeImage(frame);
			frame = null;
			frameFileName = "";
		}
	}

	//イベントハンドラ 画面のタッチイベント
	@Override
	public boolean onTouchEvent(int x, int y, int action, Context context)
	{
		int size1 = link.size();
		int size2 = button.size();
		int state;
		boolean result;
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:	// ダウン
			for(int i = 0; i < size1; i++)
			{
				state = link.get(i).onDown(x, y);
				if(state != 0)
				{
					setLinkState(link.get(i).linkNum);
					return true;	// どれか一つのリンクが確定しているなら、それ以外のイベントは無視
				}
			}
			for(int i = 0; i < size2; i++)
				button.get(i).onDown(x, y);
			break;
		case MotionEvent.ACTION_UP:		// アップ
			for(int i = 0; i < size1; i++)
			{
				result = link.get(i).onUp(context, x, y);
				if(result)
					return true;
			}
			for(int i = 0; i < size2; i++)
			{
				result = button.get(i).onUp(x, y);
				if(result)
					return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:	// 移動
			for(int i = 0; i < size1; i++)
			{
				state = link.get(i).onMove(x, y);
				if(state != 0)
				{
					setLinkState(link.get(i).linkNum);
					return true;	// どれか一つのリンクが確定しているなら、それ以外のイベントは無視
				}
			}
			for(int i = 0; i < size2; i++)
				button.get(i).onMove(x, y);
			break;
		}
		return false;
	}

	/**
	 * レイヤ設定
	 * @param resources リソース
	 * @param elm 属性
	 */
	public void setOption(Resources resources, HashMap<String, String> elm)
	{
		String buff;

		buff = elm.get("index");
		if(buff != null && Util.isInteger(buff))
			this.index = Integer.decode(buff);

		buff = elm.get("top");
		if(buff != null && Util.isInteger(buff))
			setY(Integer.decode(buff));

		buff = elm.get("left");
		if(buff != null && Util.isInteger(buff))
			setX(Integer.decode(buff));

		buff = elm.get("width");
		if(buff != null && Util.isInteger(buff))
			setWidth(Integer.decode(buff));

		buff = elm.get("height");
		if(buff != null && Util.isInteger(buff))
			setHeight(Integer.decode(buff));

		buff = elm.get("color");
		if(buff != null && Util.isInteger(buff))
			setFrameColor(Integer.decode(buff));

		buff = elm.get("opacity");
		if(buff != null && Util.isInteger(buff))
			setOpacity(Integer.decode(buff));

		buff = elm.get("margin");
		if(buff != null && Util.isInteger(buff))
			setMargin(Integer.decode(buff));

		buff = elm.get("marginl");
		if(buff != null && Util.isInteger(buff))
			setMarginLeft(Integer.decode(buff));

		buff = elm.get("margint");
		if(buff != null && Util.isInteger(buff))
			setMarginTop(Integer.decode(buff));

		buff = elm.get("marginr");
		if(buff != null && Util.isInteger(buff))
			setMarginRight(Integer.decode(buff));

		buff = elm.get("marginb");
		if(buff != null && Util.isInteger(buff))
			setMarginBottom(Integer.decode(buff));

		buff = elm.get("visible");
		if(buff != null && Util.isBoolean(buff))
			setVisible(Boolean.valueOf(buff));

		buff = elm.get("frame");
		if(buff != null)
			loadFrame(resources, buff);

		buff = elm.get("vertical");
		if(buff != null && Util.isBoolean(buff))
			setVertical(Boolean.valueOf(buff));

	}

	/**
	 * レイヤのコピー
	 */
	@Override
	public void copyLayer(Context context, KASLayer layer, long nowTime)
	{
		MessageLayer m = (MessageLayer)layer;
		Resources resource = context.getResources();

		this.index = m.index;
		setVisible(m.visible);
		setVertical(m.vertical);

		setPos(m.x, m.y);

		setSize(m.width, m.height);

		setFontSize(m.fontSize);
		setColor(m.color);
		setBold(m.bold);
		setItalic(m.italic);
		setShadow(m.shadow);
		setEdge(m.edge);
		setShadowColor(m.shadowColor);
		setEdgeColor(m.edgeColor);

		defFontSize = m.defFontSize;
		defColor = m.defColor;
		defBold = m.defBold;
		defItalic = m.defItalic;
		defShadow = m.defShadow;
		defEdge = m.defEdge;
		defShadowColor = m.defShadowColor;
		defEdgeColor = m.defEdgeColor;

		setLineSpacing(m.linespacing);
		setLineSize(m.linesize);
		setPitch(m.pitch);
		setAlign(m.align);
		setAutoReturn(m.autoreturn);

		defLinespacing = m.defLinespacing;
		defLinesize = m.defLinesize;
		defPitch = m.defPitch;
		defAlign = m.defAlign;
		defAutoreturn = m.defAutoreturn;

		setFrameColor(m.frameColor);
		setBothOpacity(m.opacity, m.layerOpacity);
		defLinkColor = m.defLinkColor;
		defLinkOpacity = m.defLinkOpacity;
		loadFrame(resource, m.frameFileName);

		setMargin(m.margin_top, m.margin_right, m.margin_bottom, m.margin_left);
		marginRCh = m.marginRCh;

		clearMessage();

		// メッセージのコピー
		lines.clear();
		int size = m.lines.size();
		for(int i = 0; i < size; i++)
			lines.add(new MessageTextBox(this, m.lines.get(i)));
		line = lines.get(size-1);
		textX = m.textX;
		textY = m.textY;
		startX = m.startX;

		link.clear();
		int len = m.link.size();
		for(int i = 0; i < len; i++)
			link.add(new LinkButton(m.link.get(i)));

		button.clear();
		len = m.button.size();
		for(int i = 0; i < len; i++)
			button.add(new ButtonLayer(resource, Layer.scale, m.button.get(i)));

		loadGlyph(context, pageglyph, m.pageglyph.getFileName());
		loadGlyph(context, lineglyph, m.lineglyph.getFileName());
		pageglyph.setPos(m.pageglyph.getX(), m.pageglyph.getY());
		lineglyph.setPos(m.lineglyph.getX(), m.lineglyph.getY());

		super.copyLayer(context, layer, nowTime);
	}

	/**
	 * レイヤ状態の保存
	 * @return 保存データ
	 * @throws JSONException
	 */
	public JSONObject saveLayer() throws JSONException
	{
		json.put("visible", visible);
		json.put("index", index);
		json.put("x", x);
		json.put("y", y);
		json.put("vertical", vertical);

		json.put("width", width);
		json.put("height", height);

//		json.put("fontsize", fontSize);
//		json.put("color", color);
//		json.put("bold", bold);
//		json.put("italic", italic);
//		json.put("shadow", shadow);
//		json.put("edge", edge);
//		json.put("shadowcolor", shadowColor);
//		json.put("edgecolor", edgeColor);

		json.put("deffontsize", defFontSize);
		json.put("defcolor", defColor);
		json.put("defbold", defBold);
		json.put("defitalic", defItalic);
		json.put("defshadow", defShadow);
		json.put("defedge", defEdge);
		json.put("defshadowcolor", defShadowColor);
		json.put("defedgecolor", defEdgeColor);

//		json.put("linespacing", linespacing);
//		json.put("linesize", linesize);
//		json.put("pitch", pitch);
//		json.put("autoreturn", autoreturn);
//		json.put("align", (int)align);

		json.put("deflinespacing", defLinespacing);
		json.put("deflinesize", defLinesize);
		json.put("defpitch", defPitch);
		json.put("defautoreturn", defAutoreturn);
		json.put("defalign", (int)defAlign);

		json.put("deflinkcolor", defLinkColor);
		json.put("deflinkopacity", defLinkOpacity);

		json.put("margin_top", margin_top);
		json.put("margin_right", margin_right);
		json.put("margin_bottom", margin_bottom);
		json.put("margin_left", margin_left);
		json.put("marginrch", marginRCh);

		json.put("opacity", opacity);
		json.put("layeropacity", layerOpacity);

		json.put("framecolor", frameColor);

		json.put("pageglyph", pageglyph.saveLayer());
		json.put("lineglyph", lineglyph.saveLayer());

		return json;
	}

	/**
	 * レイヤ状態の復元
	 * @param context コンテキスト
	 * @param json 保存データ
	 * @param nowTime 現在時刻
	 * @throws JSONException
	 */
	public void loadLayer(Context context, JSONObject json, long nowTime) throws JSONException
	{
		if(json == null)
			return;

		if(json.has("visible"))			visible 		= json.getBoolean("visible");
		if(json.has("index"))			index			= json.getInt("index");
		if(json.has("x"))				x 				= json.getInt("x");
		if(json.has("y"))				y 				= json.getInt("y");
		if(json.has("vertical"))		vertical		= json.getBoolean("vertical");

		if(json.has("width"))			width			= json.getInt("width");
		if(json.has("height"))			height			= json.getInt("height");

		if(json.has("deffontsize"))		defFontSize		= json.getInt("deffontsize");
		if(json.has("defcolor"))		defColor		= json.getInt("defcolor");
		if(json.has("defbold"))			defBold			= json.getBoolean("defbold");
		if(json.has("defitalic"))		defItalic		= json.getBoolean("defitalic");
		if(json.has("defshadow"))		defShadow		= json.getBoolean("defshadow");
		if(json.has("defedge"))			defEdge			= json.getBoolean("defedge");
		if(json.has("defshadowcolor"))	defShadowColor	= json.getInt("defshadowcolor");
		if(json.has("defedgecolor"))	defEdgeColor	= json.getInt("defedgecolor");

		if(json.has("deflinespacing"))	defLinespacing	= json.getInt("deflinespacing");
		if(json.has("deflinesize"))		defLinesize		= json.getInt("deflinesize");
		if(json.has("defpitch"))		defPitch		= json.getInt("defpitch");
		if(json.has("defautoreturn"))	defAutoreturn	= json.getBoolean("defautoreturn");
		if(json.has("defalign"))		defAlign		= (char)json.getInt("defalign");

		if(json.has("deflinkcolor"))	defLinkColor	= json.getInt("deflinkcolor");
		if(json.has("deflinkopacity"))	defLinkColor	= json.getInt("deflinkopacity");

		if(json.has("margin_top"))		margin_top		= json.getInt("margin_top");
		if(json.has("margin_right"))	margin_right	= json.getInt("margin_right");
		if(json.has("margin_bottom"))	margin_bottom	= json.getInt("margin_bottom");
		if(json.has("margin_left"))		margin_left		= json.getInt("margin_left");

		if(json.has("marginrch"))		marginRCh		= json.getInt("marginrch");

		if(json.has("opacity"))			opacity			= json.getInt("opacity");
		if(json.has("layeropacity"))	layerOpacity	= json.getInt("layeropacity");

		if(json.has("framecolor"))		frameColor		= json.getInt("framecolor");

		if(json.has("pageglyph"))pageglyph.loadLayer(context, json.getJSONObject("pageglyph"), nowTime);
		if(json.has("lineglyph"))lineglyph.loadLayer(context, json.getJSONObject("lineglyph"), nowTime);

		setVisible(visible);
		setPos(x, y);

		setSize(width, height);
		setMargin(margin_top, margin_right, margin_bottom, margin_left);

		setBothOpacity(opacity, layerOpacity);
		setFrameColor(frameColor);

//		clearMessage();
	}

	/**
	 * デバッグ用
	 * @param log
	 */
	public void debug_printLayerState(String log)
	{
		log += " pos=" + x + "," + y;
		log += " size=" + width + "," + height;
		log += " opacity=" + opacity + "," + paint.getAlpha();
		log += " layerOpacity=" + layerOpacity;
		log += " visible=" + visible;
		Util.log(log);
	}

	//------------------------------------------------------------
	// セッター・ゲッター

	/**
	 * vertical
	 */
	public void setVertical(boolean vertical)
	{
		this.vertical = vertical;
		line.vertical = vertical;
	}

	/**
	 * visible
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	/**
	 * align
	 * @param align
	 */
	public void setAlign(char align)
	{
		this.align = align;
	}

	/**
	 * x および y
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
		initAllLinePos();
		initAllLinkPos();
		resetGlyphPos();
	}

	/**
	 * x
	 * @param x
	 */
	public void setX(int x)
	{
		this.x = x;
		initAllLinePos();
		initAllLinkPos();
		resetGlyphPos();
	}

	/**
	 * y
	 * @param y
	 */
	public void setY(int y)
	{
		this.y = y;
		initAllLinePos();
		initAllLinkPos();
		resetGlyphPos();
	}

	/**
	 * width および height
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * width
	 * @param width
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}

	/**
	 * height
	 * @param height
	 */
	public void setHeight(int height)
	{
		this.height = height;
	}

	/**
	 * margin
	 * @param margin
	 */
	public void setMargin(int margin)
	{
		setMargin(margin, margin, margin, margin);
	}

	/**
	 * margin
	 * @param top
	 * @param right
	 * @param bottom
	 * @param left
	 */
	public void setMargin(int top, int right, int bottom, int left)
	{
		margin_top = top;
		margin_right = right;
		margin_bottom = bottom;
		margin_left = left;
	}

	/**
	 * margin_top
	 * @param top
	 */
	public void setMarginTop(int top)
	{
		margin_top = top;
	}

	/**
	 * margin_right
	 * @param right
	 */
	public void setMarginRight(int right)
	{
		margin_right = right;
	}

	/**
	 * margin_bottom
	 * @param bottom
	 */
	public void setMarginBottom(int bottom)
	{
		margin_bottom = bottom;
	}

	/**
	 * margin_left
	 * @param left
	 */
	public void setMarginLeft(int left)
	{
		margin_left = left;
	}

	/**
	 * color
	 * @param color
	 */
	public void setColor(int color)
	{
		this.color = color;
	}

	/**
	 * frameColor
	 * @param color
	 */
	public void setFrameColor(int color)
	{
		this.frameColor = color;
		paint.setColor(color + 0xff000000);
		setBothOpacity(opacity, layerOpacity);
	}

	/**
	 * shadowColor
	 * @param color
	 */
	public void setShadowColor(int color)
	{
		this.shadowColor = color;
	}

	/**
	 * shadow
	 * @param shadow
	 */
	public void setShadow(boolean shadow)
	{
		this.shadow = shadow;
	}

	/**
	 * edgeColor
	 * @param color
	 */
	public void setEdgeColor(int color)
	{
		this.edgeColor = color;
	}

	/**
	 * edge
	 * @param edge
	 */
	public void setEdge(boolean edge)
	{
		this.edge = edge;
	}

	/**
	 * bold
	 * @param bold
	 */
	public void setBold(boolean bold)
	{
		this.bold = bold;
	}

	/**
	 * italic
	 * @param italic
	 */
	public void setItalic(boolean italic)
	{
		this.italic = italic;
	}

	/**
	 * opacity
	 * @param opacity
	 */
	public void setOpacity(int opacity)
	{
		this.opacity = opacity;
		int o = (int)(opacity * (layerOpacity/255.0));
		if(o < 0) o = 0;
		else if(o > 255) o = 255;
		paint.setAlpha(o);
	}

	/**
	 * layerOpacity
	 * @param layerOpacity
	 */
	public void setLayerOpacity(int layerOpacity)
	{
		this.layerOpacity = layerOpacity;
		setOpacity(this.opacity);
	}

	/**
	 * opacity および layerOpacity
	 * @param opacity
	 * @param layerOpacity
	 */
	public void setBothOpacity(int opacity, int layerOpacity)
	{
		this.opacity = opacity;
		this.layerOpacity = layerOpacity;
		paint.setAlpha((int)(opacity * (layerOpacity/255.0)));
	}

	/**
	 * fontSize
	 * @param size
	 */
	public void setFontSize(float size)
	{
		fontSize = size;
		// linesizeの補正
		if(this.linesize < size)
			setLineSize((int)size);
	}

	/**
	 * pitch
	 * @param pitch
	 */
	public void setPitch(int pitch)
	{
		this.pitch = pitch;
	}

	/**
	 * linespacing
	 * @param lineSpacing
	 */
	public void setLineSpacing(int lineSpacing)
	{
		this.linespacing = lineSpacing;
	}

	/**
	 * linesize
	 * @param linesize
	 */
	public void setLineSize(int linesize)
	{
		this.linesize = linesize;
	}

	/**
	 * autoreturn
	 * @param autoreturn
	 */
	public void setAutoReturn(boolean autoreturn)
	{
		this.autoreturn = autoreturn;
	}

	@Override
	public String toString()
	{
		return "MessageLayer";
	}

}
