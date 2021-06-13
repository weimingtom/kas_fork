package net.studiomikan.kas;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * YesNo ダイアログ
 * @author okayu
 */
class YesNoDialog
{
	/** 表示非表示 */
	public boolean visible = false;

	/** メッセージ */
	protected String[] message;
	/** メッセージの幅 */
	protected float messageWidth = 0;
	/** 描画用 Paint */
	protected Paint paint = null;
	/** フォントサイズ */
	public static final int fontSize = 26;
	/** メッセージ描画開始位置 */
	protected int messageX = 0;
	/** メッセージ描画開始位置 */
	protected int messageY = 190;

	/** 背景画像 */
	protected Bitmap base = null;
	/** 背景画像描画元矩形 */
	protected Rect baseRectSrc = null;
	/** 背景画像描画先矩形 */
	protected Rect baseRectDst = null;

	/** yes ボタン */
	protected ButtonLayer yesButton = null;
	/** no ボタン */
	protected ButtonLayer noButton = null;

	/** yes ボタンの動作 */
	protected ButtonFunc yesFunc = null;
	/** no ボタンの動作 */
	protected ButtonFunc noFunc = null;

	/** 背景画像のファイル名 */
	public static final String baseFileName = "yesnobase";
	/** 背景画像の表示幅 */
	public static final int baseWidth = 410;
	/** 背景画像の表示の高さ */
	public static final int baseHeight = 210;
	/** 背景画像の表示位置 */
	public static final int baseX = 0;
	/** 背景画像の表示位置 */
	public static final int baseY = 120;

	/** yes ボタン画像のファイル名 */
	public static final String yesButtonFilename = "yesbutton";
	/** yes ボタンの幅 */
	public static final int yesButtonWidth = 150;
	/** yes ボタンの高さ */
	public static final int yesButtonHeight = 50;
	/** yes ボタンの表示位置 */
	public static final int yesButtonX = 230;
	/** yes ボタンの表示位置 */
	public static final int yesButtonY = 240;

	/** no ボタンの画像ファイル名 */
	public static final String noButtonFilename = "nobutton";
	/** no ボタンの幅 */
	public static final int noButtonWidth = 150;
	/** no ボタンの高さ */
	public static final int noButtonHeight = 50;
	/** no ボタンの表示位置 */
	public static final int noButtonX = 420;
	/** no ボタンの表示位置 */
	public static final int noButtonY = 240;


	/**
	 * コンストラクタ
	 * @param resources リソース
	 */
	public YesNoDialog(Resources resources)
	{
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(26);
		paint.setColor(Color.WHITE);

		base = ResourceManager.loadBitmap(resources, baseFileName, 1);
		if(base != null)
		{
			baseRectSrc = new Rect(0, 0, base.getWidth(), base.getHeight());
			baseRectDst = new Rect(baseX, baseY, baseX+baseWidth, baseY+baseHeight);
		}

		yesButton = new ButtonLayer(
				resources, yesButtonFilename, 1,
				yesButtonWidth, yesButtonHeight,
				null, null,
				null, null, false,
				null, null);
		yesButton.setPos(yesButtonX, yesButtonY);

		noButton = new ButtonLayer(
				resources, noButtonFilename, 1,
				noButtonWidth, noButtonHeight,
				null, null,
				null, null, false,
				null, null);
		noButton.setPos(noButtonX, noButtonY);
	}

	/**
	 * 尋ねる
	 * @param message メッセージ
	 * @param yesFunc yes ボタンの動作
	 * @param noFunc no ボタンの動作
	 */
	public void ask(String message, ButtonFunc yesFunc, ButtonFunc noFunc)
	{
		this.message = message.split("\n");

		this.yesFunc = yesFunc;
		this.noFunc = noFunc;

		yesButton.setFunction(yesFunc);
		noButton.setFunction(noFunc);
		yesButton.visible = true;
		noButton.visible = true;
		visible = true;
	}

	/**
	 * 描画イベント
	 * @param c キャンバス
	 */
	public void onDraw(Canvas c)
	{
		if(visible)
		{
			if(base != null && base.isRecycled())
				base = ResourceManager.loadBitmap(Util.context.getResources(), baseFileName, 1);

			if(base != null && !base.isRecycled())
				c.drawBitmap(base, baseX, baseY, null);
			yesButton.onDraw(c);
			noButton.onDraw(c);

			if(message != null)
			{
				int gap = fontSize + 7;
				int y = messageY - (message.length*gap/2);
				for(int i = 0; i < message.length; i++)
				{
					messageWidth = paint.measureText(message[i]);
					messageX = (int)((float)Util.standardDispWidth/2 - messageWidth/2);
					c.drawText(message[i], messageX, y, paint);
					y += gap;
				}
			}
		}
	}

	/**
	 * 入力イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public void onDown(int x, int y)
	{
		if(visible)
		{
			yesButton.onDown(x, y);
			noButton.onDown(x, y);
		}
	}

	/**
	 * 入力イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public void onMove(int x, int y)
	{
		if(visible)
		{
			yesButton.onDown(x, y);
			noButton.onDown(x, y);
		}
	}

	/**
	 * 入力イベント
	 * @param x 座標
	 * @param y 座標
	 */
	public void onUp(int x, int y)
	{
		if(visible)
		{
			if(yesButton.onUp(x, y))
			{
				visible = false;
				return;
			}
			if(noButton.onUp(x, y))
			{
				visible = false;
				return;
			}
		}
	}

}
