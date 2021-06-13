package net.studiomikan.kas;

import java.util.ArrayList;

import net.studiomikan.kas4pc.klib.Canvas;
import net.studiomikan.kas4pc.klib.Context;
import net.studiomikan.kas4pc.klib.Paint;
import net.studiomikan.kas4pc.klib.Rect;
import net.studiomikan.kas4pc.klib.Sound;

/**
 * テキストリンク関連クラス
 * @author okayu
 */
class LinkButton
{
	/** リンクの番号 */
	public int linkNum = 0;
	/** ロック */
	public boolean locked = false;

	/** 座標 */
	public int x = 0;
	/** 座標 */
	public int y = 0;
	/** 幅 */
	public int width = 0;
	/** 高さ */
	public int height = 0;

	/** 表示非表示 */
	public boolean visible = false;

	/** 描画用 Paint */
	private Paint paint = null;
	/** 矩形 */
	private ArrayList<Rect> rect = null;
	/** 矩形描画用 */
	private Rect drawRect = new Rect();

	/** ジャンプ先のシナリオファイル名 */
	public String storage = "";
	/** ジャンプ先のラベル名 */
	public String target = "";
	/** クリック時の効果音ファイル名 */
	public String clickse = "";
	/** クリック時の効果音バッファ */
	public Sound clicksebuf = null;
	/** countpage 属性 */
	public boolean countpage = false;
	/** 色 */
	public int color = 0;
	/** 濃度 */
	public int opacity = 127;
	/** スクリプト */
	public String exp = null;

	/** ボタンの状態 */
	public int state = 0;

	/**
	 * リンク生成
	 * @param linkNum リンク番号
	 * @param storage ジャンプ先のシナリオファイル名
	 * @param target ジャンプ先のラベル名
	 * @param countpage countpage 属性
	 * @param clickse クリック時の効果音ファイル名
	 * @param clicksebuf クリック時の効果音バッファ
	 * @param color 矩形の色
	 */
	public LinkButton(
			int linkNum,
			String storage,
			String target,
			boolean countpage,
			String clickse,
			Sound clicksebuf,
			int color,
			int opacity,
			String exp)
	{
		paint = new Paint();
		rect = new ArrayList<Rect>();

		this.linkNum = linkNum;
		this.storage = storage;
		this.target = target;
		this.clickse = clickse;
		this.clicksebuf = clicksebuf;
		this.countpage = countpage;
		this.color = color;
		this.opacity = opacity;
		this.exp = exp;

		paint.setColor(color + opacity*0x1000000);
	}

	/**
	 * リンクのコピーを生成
	 * @param src コピー元
	 */
	public LinkButton(LinkButton src)
	{
		paint = new Paint();
		rect = new ArrayList<Rect>();
		this.linkNum = src.linkNum;
		this.storage = src.storage;
		this.target = src.target;
		this.clickse = src.clickse;
		this.clicksebuf = src.clicksebuf;
		this.countpage = src.countpage;
		this.color = src.color;
		this.exp = src.exp;

		paint.setColor(color + 0x81000000);

		this.x = src.x;
		this.y = src.y;
		this.width = src.width;
		this.visible = src.visible;

		int size = src.rect.size();
		Rect r;
		for(int i = 0; i < size; i++)
		{
			r = src.rect.get(i);
			rect.add(new Rect(r.left, r.top, r.right, r.bottom));
		}
	}

	/**
	 * 追加
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void add(int x, int y, int width, int height)
	{
		Rect r = new Rect(x, y, x+width, y+height);
		rect.add(r);
		this.width = x + width;
		this.height = y + height;
	}

	/**
	 * 末尾に空白を追加
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void addEndSpace(int x, int y, int width, int height)
	{
		this.width = x + width;
		this.height = y + height;
	}

	/**
	 * 描画
	 * @param c キャンバス
	 * @param x 基準点
	 * @param y 基準点
	 */
	public void onDraw(Canvas c)
	{
		if(rect != null && visible)
		{
			// 0:なにもない
			// 1:タッチされてる
			switch(state)
			{
			case 0:
				break;
			case 1:
				Rect r;
				for(int i = rect.size()-1; i >= 0; i--)
				{
					r = rect.get(i);
					drawRect.set(r.left + x, r.top + y, r.right + x, r.bottom + y);
					c.drawRect(drawRect, paint);
				}
				break;
			}
		}
	}

	/**
	 * 指定の座標が矩形内にあるかどうか
	 * @param x 座標
	 * @param y 座標
	 * @return 矩形内なら true 外なら false
	 */
	private boolean inside(int x, int y)
	{
		if(rect == null)
			return false;

//		int dx = owner.x + owner.margin_left;
//		int dy = owner.y + owner.margin_top;
		int dx = this.x;
		int dy = this.y;
		Rect r, r2 = new Rect();
		for(int i = rect.size()-1; i >= 0; i--)
		{
			r = rect.get(i);
			if(r != null)
			{
				r2.set(r.left + dx, r.top + dy, r.right + dx, r.bottom + dy);
				if(r2.left <= x && x < r2.right && r2.top <= y && y < r2.bottom )
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * onDown イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public int onDown(int x, int y)
	{
		if(visible)
		{
			if(inside(x, y))
				state = 1;
			return state;
		}
		else
			return 0;
	}

	/**
	 * onUp イベント
	 * @param context コンテキスト
	 * @param x 座標
	 * @param y 座標
	 * @return 処理したなら true
	 */
	public boolean onUp(Context context, int x, int y)
	{
		if(visible)
		{
			if(state == 1)
			{
				if(clickse != null && clicksebuf != null)
				{
					clicksebuf.playSound(context, clickse, false);
					clicksebuf = null;
				}
				if(Util.mainView != null)
					Util.mainView.clickedLink(storage, target, countpage, exp);
				state = 0;
				return true;
			}
			else
				state = 0;
		}
		return false;
	}

	/**
	 * onMove イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public int onMove(int x, int y)
	{
		if(visible)
		{
			if(inside(x, y)) state = 1;
			else state = 0;
			return state;
		}
		else
			return 0;
	}

	/**
	 * クリア
	 */
	public void clear()
	{
		if(rect != null)
			rect.clear();
		clicksebuf = null;
	}

	/**
	 * visible
	 * @param visible
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

}
