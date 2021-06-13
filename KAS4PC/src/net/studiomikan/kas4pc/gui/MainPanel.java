package net.studiomikan.kas4pc.gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import net.studiomikan.kas.Util;
import net.studiomikan.kas4pc.klib.Canvas;
import net.studiomikan.kas4pc.klib.MotionEvent;


/**
 * メインパネル<br>
 * 描画を担う
 * @author okayu
 */
public class MainPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener
{
	/** ID */
	private static final long serialVersionUID = -1819009686605765902L;

	/** ダブルバッファリング用バッファ */
	private Graphics2D dbg;
	/** ダブルバッファリング用イメージ */
	private Image dbImage = null;

	/** キャンバス */
	public Canvas canvas;

	/** 停止中 */
	public boolean pause = false;

	/**
	 * クリックイベント取得用
	 * @author okayu
	 */
	public interface onTouchEventListener
	{
		public boolean onTouchEvent(MotionEvent event);
	}

	/**
	 * コンストラクタ
	 */
	public MainPanel()
	{
		super();
		Util.mainPanel = this;
		setPreferredSize(new Dimension(Util.standardDispWidth, Util.standardDispHeight));
		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * 描画準備
	 */
	public void initDraw()
	{
		//バッファ作成
		createBuffer();
		// メインのキャンバス
		canvas = new Canvas();
		pause = false;
	}

	/**
	 * バッファ作成
	 */
	private int createBuffer()
	{
		while(true)
		{
			// バッファイメージ
			dbImage = createImage(Util.standardDispWidth, Util.standardDispHeight);
			if (dbImage == null)
				System.out.println("dbImage is null");
			else
				break;
		}
		dbg = (Graphics2D)(dbImage.getGraphics());
		return 1;
	}

	/**
	 * バッファをクリアする
	 */
	private void clearBuffer()
	{
		dbg.setColor(Color.BLACK);
		dbg.fillRect(0, 0, Util.standardDispWidth, Util.standardDispHeight);
	}

	/**
	 * バッファを準備する
	 */
	public Canvas lockCanvas()
	{
		if(pause) return null;
		clearBuffer();
		canvas.setGraphics2D(dbg, Util.standardDispWidth, Util.standardDispHeight);
		return canvas;
	}

	/**
	 * バッファを画面に描画
	 */
	public void paintScreen()
	{
		try
		{
			Graphics2D g = (Graphics2D)getGraphics(); // グラフィックオブジェクトを取得
			if ((g != null) && (dbImage != null))
				g.drawImage(dbImage, 0, 0, null); // バッファを画面に描画
			if (g != null)
				g.dispose(); // グラフィックオブジェクトを破棄
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	long buff = 0;

	@Override
	public void keyTyped(KeyEvent e)
	{

	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if(Util.mainView != null)
		{
			MotionEvent event = new MotionEvent(MotionEvent.ACTION_DOWN, e.getX(), e.getY());
			Util.mainView.onTouchEvent(event);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if(Util.mainView != null)
		{
			MotionEvent event = new MotionEvent(MotionEvent.ACTION_UP, e.getX(), e.getY());
			Util.mainView.onTouchEvent(event);
		}
	}


	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if(Util.mainView != null)
		{
			MotionEvent event = new MotionEvent(MotionEvent.ACTION_MOVE, e.getX(), e.getY());
			Util.mainView.onTouchEvent(event);
		}
	}

}
