package net.studiomikan.kas;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * 履歴の管理・表示
 * @author okayu
 */
class History
{
	/** 親 */
	private MainSurfaceView owner = null;
	/** 表示・非表示 */
	public boolean enabled = true;
	/** 履歴に出力するかどうか */
	public boolean output = true;

	/** メッセージ */
	private String[] message = null;
	/** 最新のメッセージ位置 */
	private int point = 0;
	/** メッセージを何行保存できるか */
	private int messageLineMax = 100;
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

	/** 閉じるボタンの上位置 */
	public int close_top = 0;
	/** 閉じるボタンの左位置 */
	public int close_left = 750;

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
		this.owner = owner;

		Config.History_config(this, Util.getMokaScript());
//		ConfigScript.History_config(this);	// Config.History_config で呼んでる

		message = new String[messageLineMax];
		for(int i = 0; i < message.length; i++)
			message[i] = "";
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setTextSize(fontSize);

		paint_title = new Paint();
		paint_title.setColor(Color.WHITE);
		paint_title.setTextSize(50);
		paint_title.setAntiAlias(true);

		lineStringNum = (int)((float)width / (float)fontSize);
		linesNum = (int)((float)height / (float)linespacing);

		bottom = top + height;
		close_top += 50;
	}

	/**
	 * 描画イベント
	 * @param c
	 */
	public void onDraw(Canvas c)
	{
		c.drawARGB(180, 0, 0, 0);
		c.drawText("×", close_left, close_top, paint_title);

		int x = left;
		int y = bottom;
		int max = point + linesNum;

		for(int i = point; i < max; i++)
		{
			c.drawText(message[i], x, y, paint);
			y -= linespacing;
		}
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
				{
					point--;
					if(point < 0)
						point++;
				}
				else
				{
					point++;
					if(point > message.length-linesNum)
						point--;
				}
				touchX = x;
				touchY = y;
			}
		}
	}

	/**
	 * onUp イベント
	 * @param x
	 * @param y
	 */
	public void onUp(int x, int y)
	{
		downFlag = false;
		if(x > 730 && y < 70)
		{
			owner.endHistory();
			point = 0;
		}
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
			message[i] = message[i-1];
		message[0] = "";
		charCount = 0;
	}

	/**
	 * 履歴メッセージを全消去
	 */
	public void clear()
	{
		for(int i = 0; i < message.length; i++)
			message[i] = "";
		charCount = 0;
	}


}
