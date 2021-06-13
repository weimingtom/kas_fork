package net.studiomikan.kas;

import net.studiomikan.kas4pc.klib.Canvas;
import net.studiomikan.kas4pc.klib.Color;
import net.studiomikan.kas4pc.klib.MotionEvent;
import net.studiomikan.kas4pc.klib.Paint;
import net.studiomikan.kas4pc.klib.Typeface;

/**
 * 履歴の管理・表示
 * @author okayu
 */
class History
{
//	/** 親 */
//	private MainSurfaceView owner = null;
	/** 表示・非表示 */
	public boolean enabled = true;
	/** 履歴に出力するかどうか */
	public boolean output = true;

	/** メッセージ */
	private String[] message = null;
	/** 最新のメッセージ位置 */
	private int point = 0;
	/** メッセージを何行保存できるか */
	public int messageLineMax = 250;
	/** 一行が何文字か */
	private int lineStringNum = 0;
	/** 一画面に何行か */
	private int linesNum = 0;
	/** 閉じるボタン用 */
	private Paint paint_title = null;
	/** メッセージ用 */
	private Paint paint = null;

	/** フォントサイズ */
	public float fontSize = 24f;
	/** 行の高さ */
	public int linespacing = 35;

	/** 上位置 */
	public int top = 20;
	/** 左位置 */
	public int left = 10;
	/** 底の位置 */
	private int bottom = 440;
	/** 幅 */
	public int width = 720;
	/** 高さ */
	public int height = 420;

	/** 閉じるボタン */
	public ButtonLayer closeButton = null;
	/** 次のページボタン */
	public ButtonLayer nextPageButton = null;
	/** 前のページボタン */
	public ButtonLayer prevPageButton = null;

	/** 閉じるボタンの画像ファイル */
	public String closeFilename = "history_close.png";
	/** 閉じるボタンの幅 */
	public int closeWidth = 32;
	/** 閉じるボタンの高さ */
	public int closeHeight = 32;
	/** 閉じるボタンの上位置 */
	public int closeTop = 0;
	/** 閉じるボタンの左位置 */
	public int closeLeft = 750;

	/** 次のページボタンの画像ファイル */
	public String nextFilename = "history_next.png";
	/** 次のページボタンの幅 */
	public int nextPageWidth = 32;
	/** 次のページボタンの高さ */
	public int nextPageHeight = 32;
	/** 次のページボタンの上位置 */
	public int nextPageTop = 0;
	/** 次のページボタンの左位置 */
	public int nextPageLeft = 0;

	/** 前のページボタンの画像ファイル */
	public String prevFilename = "history_prev.png";
	/** 前のページボタンの幅 */
	public int prevPageWidth = 32;
	/** 前のページボタンの高さ */
	public int prevPageHeight = 32;
	/** 前のページボタンの上位置 */
	public int prevPageTop = 0;
	/** 前のページボタンの左位置 */
	public int prevPageLeft = 0;

	/** 現在の行の文字数 */
	private int charCount = 0;

	/** タッチされているかどうか */
	boolean downFlag = false;
	/** タッチされた場所 */
	int touchX = 0;
	/** タッチされた場所 */
	int touchY = 0;

	/**
	 * コンストラクタ
	 * @param owner 親
	 */
	public History(MainSurfaceView owner)
	{
//		this.owner = owner;
		final MainSurfaceView _owner = owner;

		paint = new Paint();
		paint_title = new Paint();

		Config.History_config(this, Util.getMokaScript());

		closeButton = new ButtonLayer(
				Util.context.getResources(), closeFilename, 1,
				closeWidth, closeHeight,
				null, null, null, null, false, null,
				new ButtonFunc(){
					@Override
					public void func(ButtonFunc b){ _owner.endHistory(); }
				});
		nextPageButton = new ButtonLayer(
				Util.context.getResources(), nextFilename, 1,
				nextPageWidth, nextPageHeight,
				null, null, null, null, false, null,
				new ButtonFunc(){
					@Override
					public void func(ButtonFunc b){ nextPage(); }
				});
		prevPageButton = new ButtonLayer(
				Util.context.getResources(), prevFilename, 1,
				prevPageWidth, prevPageHeight,
				null, null, null, null, false, null,
				new ButtonFunc(){
					@Override
					public void func(ButtonFunc b){ prevPage(); }
				});
		closeButton.setPos(closeLeft, closeTop);
		nextPageButton.setPos(nextPageLeft, nextPageTop);
		prevPageButton.setPos(prevPageLeft, prevPageTop);
		closeButton.visible = true;
		nextPageButton.visible = true;
		prevPageButton.visible = true;


		message = new String[messageLineMax];
		for(int i = 0; i < message.length; i++)
			message[i] = null;
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setTextSize(fontSize);

		paint_title.setColor(Color.WHITE);
		paint_title.setTextSize(50);
		paint_title.setAntiAlias(true);

		lineStringNum = (int)((float)width / (float)fontSize);
		linesNum = (int)((float)height / (float)linespacing);

		bottom = top + height;
	}

	/**
	 * 履歴レイヤのフォントを変更
	 * @param filename フォントファイル名
	 */
	public void setFontFace(String filename)
	{
		Util.log("setFontFace " + filename);
		Typeface face;
		if(filename.equals("default")) face = Typeface.DEFAULT;
		else face = ResourceManager.loadTypeFace(Util.context, filename);
		paint.setTypeface(face);
	}

	/**
	 * 描画イベント
	 * @param c
	 */
	public void onDraw(Canvas c)
	{
		c.drawARGB(180, 0, 0, 0);

		int x = left;
		int y = bottom;
		int max = point + linesNum;

		for(int i = point; i < max; i++)
		{
			if(message[i] != null)
				c.drawText(message[i], x, y, paint);
			y -= linespacing;
		}

		closeButton.onDraw(c);
		nextPageButton.onDraw(c);
		prevPageButton.onDraw(c);
	}

	/** 次のページ */
	public void nextPage()
	{
		for(int i = 1; i < linesNum; i++)
			nextLine();
	}

	/** 前のページ */
	public void prevPage()
	{
		for(int i = 1; i < linesNum; i++)
			prevLine();
	}

	/** 一行進む */
	public void nextLine()
	{
		point--;
		if(point < 0) point = 0;
	}

	/** 一行戻る */
	public void prevLine()
	{
		point++;
		if(message[point] == null) point--;
	}

	/**
	 * 入力イベント
	 * @param event
	 * @return false
	 */
	public boolean onTouchEvent(int x, int y, int action)
	{
		switch(action)
		{
		case MotionEvent.ACTION_DOWN:	// ダウン
			onDown(x, y); break;
		case MotionEvent.ACTION_UP:		// アップ
			onUp(x, y);	break;
		case MotionEvent.ACTION_MOVE:
			onMove(x, y); break;
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
		downFlag = true;
		touchX = x;
		touchY = y;

		closeButton.onDown(x, y);
		nextPageButton.onDown(x, y);
		prevPageButton.onDown(x, y);
	}

	/**
	 * onMove イベント
	 * @param x
	 * @param y
	 */
	public void onMove(int x, int y)
	{
		if(downFlag)
		{
			int moveY = Math.abs(y - touchY);
			if(moveY > 50)
			{
				if(y < touchY)
					nextLine();
				else
					prevLine();

				touchX = x;
				touchY = y;
			}
		}

		closeButton.onMove(x, y);
		nextPageButton.onMove(x, y);
		prevPageButton.onMove(x, y);
	}

	/**
	 * onUp イベント
	 * @param x
	 * @param y
	 */
	public void onUp(int x, int y)
	{
		downFlag = false;
//		if(x > 730 && y < 70)
//		{
//			owner.endHistory();
//			point = 0;
//			return;
//		}

		if(closeButton.onUp(x, y)) return;
		if(nextPageButton.onUp(x, y)) return;
		if(prevPageButton.onUp(x, y)) return;
	}

	/**
	 * メッセージを追加
	 * @param message メッセージ
	 */
	public void addMessage(String message)
	{
		if(!output) return;
		if(message == null) return;
		int len = message.length();
		if(len == 0) return;

		if(this.message[0] == null) this.message[0] = "";

		char c;
		boolean flag = true;
		for(int i = 0; i < len; i++)
		{
			c = message.charAt(i);
			charCount++;

			if(charCount >= lineStringNum || c == '\n')	// 行末まで書いたら改行
				cr();
			if(c != '\n')
				this.message[0] += c;
		}
		while(flag)
		{
			flag = false;
		}
	}

	/**
	 * 改行する
	 */
	private void cr()
	{
		// メッセージを一個ずつずらす
		for(int i = message.length - 1; i > 0; i--)
		{
			message[i] = message[i-1];
		}
		message[0] = "";
		charCount = 0;
	}

	/**
	 * 履歴メッセージを全消去
	 */
	public void clear()
	{
		for(int i = 0; i < message.length; i++)
			message[i] = null;
		charCount = 0;
	}


}
