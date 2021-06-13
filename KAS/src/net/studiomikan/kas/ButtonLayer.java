package net.studiomikan.kas;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * ボタンクリック時に実行するメソッド
 * @author okayu
 */
interface ButtonFunc
{
	public void func(ButtonFunc b);
}

/**
 * 画像を使ったボタンのクラスです。<br>
 * ButtonLayer は、MessageLayer に所属し、管理されます。
 * 各種 on イベントは、MessageLayer から呼び出されます。
 * @author okayu
 */
class ButtonLayer
{
	/** 表示非表示 */
	public boolean visible = false;
	/** ボタンの幅 */
	protected int width = 100;
	/** ボタンの高さ */
	protected int height = 100;
	/** ボタンの透明度 */
	protected int opacity = 255;

	/** 表示する画像 */
	protected Bitmap image = null;
	/** 画像ファイル名 */
	protected String filename;
	/** 画像上の描画元矩形 */
	protected Rect rectSrc = null;
	/** 描画先矩形 */
	protected Rect rectDst = null;
	/** 画像描画用 Paint */
	protected Paint paint = null;

	/** ボタンクリック時の効果音ファイル名 */
	protected String clickse;
	/** ボタンクリック時の効果音バッファ */
	protected Sound clickbuf = null;

	/** ジャンプ先のシナリオファイル名 */
	protected String storage;
	/** ジャンプ先のラベル */
	protected String target;
	/** countpage属性 */
	protected boolean countpage;
	/** スクリプト */
	protected String exp;

	/** ボタンクリック時に実行するメソッド */
	public ButtonFunc func = null;
	/** 押されているかどうかのフラグ */
	protected boolean downed = false;

	/** 読み込みスケール */
	protected float scale = 1;

	/**
	 * コンストラクタ
	 * @param resources リソース
	 * @param filename 画像ファイル名
	 * @param scale 画像ファイルの解像度
	 * @param width 高さ
	 * @param height 幅
	 * @param clickse クリック時の効果音ファイル名
	 * @param clickbuf クリック時の効果音バッファ
	 * @param storage ジャンプ先のシナリオファイル名
	 * @param target ジャンプ先のラベル
	 * @param countpage countpage属性
	 * @param exp スクリプト
	 * @param func クリック時に実行するメソッド
	 */
	public ButtonLayer(
			Resources resources, String filename, float scale,
			int width, int height,
			String clickse, Sound clickbuf,
			String storage, String target, boolean countpage,
			String exp,
			ButtonFunc func)
	{
		this.filename = filename;
		this.scale = scale;
		this.image = ResourceManager.loadBitmap(resources, filename, scale);
		this.width = width;
		this.height = height;

		if(image == null)
			rectSrc = new Rect(0, 0, width, height);
		else
			rectSrc = new Rect(0, 0, image.getWidth()/2, image.getHeight());
		rectDst = new Rect(0, 0, width, height);

		paint = new Paint();
		paint.setAlpha(opacity);

		setPos(0, 0);
		setOpacity(opacity);

		this.clickse = clickse;
		this.clickbuf = clickbuf;

		this.storage = storage;
		this.target = target;
		this.countpage = countpage;
		this.exp = exp;

		this.func = func;
	}

	/**
	 * コンストラクタ
	 * @param resources リソース
	 * @param scale 画像の解像度
	 * @param src コピー元の ButtonLayer
	 */
	public ButtonLayer(Resources resources, float scale, ButtonLayer src)
	{
		this(resources, src.filename, scale,
				src.width, src.height,
				src.clickse, src.clickbuf,
				src.storage, src.target, src.countpage,
				src.exp,
				src.func);
	}

	/**
	 * 描画イベント
	 * @param c キャンバス
	 */
	public void onDraw(Canvas c)
	{
		if(visible)
		{
			if(image != null && image.isRecycled())
				image = ResourceManager.loadBitmap(Util.context.getResources(), filename, scale);
			if(image != null && !image.isRecycled())
				c.drawBitmap(image, rectSrc, rectDst, null);
		}
	}

	/**
	 * 指定座標が矩形の中にいるかどうか
	 * @param rect 矩形
	 * @param x x 座標
	 * @param y y 座標
	 * @return 矩形の中なら true 外なら false
	 */
	static protected final boolean inside(Rect rect, int x, int y)
	{
		if(rect != null)
		{
			if(rect.left <= x && x < rect.right &&
				rect.top <= y && y < rect.bottom )
				return true;
		}
		return false;
	}

	/**
	 * onDown イベント
	 * @param x
	 * @param y
	 */
	public void onDown(int x, int y)
	{
		downed = true;
		if(inside(rectDst, x, y))
			on();
		else
			off();
	}

	/**
	 * onMove イベント
	 * @param x
	 * @param y
	 */
	public void onMove(int x, int y)
	{
		if(inside(rectDst, x, y))
			on();
		else
			off();
	}

	/**
	 * onUp イベント
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean onUp(int x, int y)
	{
		if(downed && inside(rectDst, x, y))	// ボタンが押された
		{
			if(clickse != null && clickbuf != null)
			{
				clickbuf.playSound(Util.context, clickse, false);
			}
			if(func != null)
			{
				func.func(func);
				off();
				return true;
			}
			else if(storage != null || target != null)
			{
				if(Util.mainView != null)
					Util.mainView.clickedLink(storage, target, countpage, exp);
				off();
				return true;
			}
			return false;
		}
		else
		{
			off();
			return false;
		}
	}

	/**
	 * 画像をクリアする
	 */
	public void clear()
	{
		ResourceManager.freeImage(image);
		image = null;
	}

	/**
	 * 画像をオン状態にする
	 */
	public void on()
	{
		int width = image.getWidth();
		rectSrc.set(width/2, 0, width, image.getHeight());
	}

	/**
	 * 画像をオフ状態にする
	 */
	public void off()
	{
		int width = image.getWidth();
		rectSrc.set(0, 0, width/2, image.getHeight());
	}

	/**
	 * ボタンの座標を設定する
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y)
	{
		rectDst = new Rect(x, y, x + width, y + height);
	}

	/**
	 * ボタンの濃度を設定する
	 * @param opacity 濃度
	 */
	public void setOpacity(int opacity)
	{
		this.opacity = opacity;
		paint.setAlpha(opacity);
	}

	/**
	 * クリック時メソッドを設定する
	 * @param func
	 */
	public void setFunction(ButtonFunc func)
	{
		this.func = func;
	}

}
