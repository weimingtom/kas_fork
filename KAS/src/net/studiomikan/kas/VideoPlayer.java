package net.studiomikan.kas;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

/**
 * ビデオ再生を行うクラス<br>
 * このクラスのコンテキストを作成すると、再生用のメディアプレイヤーとサーフェイスビューが作成される。
 * <code>getLayout()</code> を使い、サーフェイスビューの収められたレイアウトを取得すれば、ビデオを表示できる。
 * @author okayu
 */
class VideoPlayer implements SurfaceHolder.Callback, OnCompletionListener, View.OnClickListener
{
	/** コンテキスト */
	private Context context = null;
	/** ビデオ再生終了リスナー */
	private OnCompletionListener onComp = null;
	/** レイアウト */
	private FrameLayout layout;
	/** 再生用メディアプレイヤー */
	private MediaPlayer player = null;
	/** 再生用サーフェイスホルダー */
	private SurfaceHolder holder = null;
	/** 再生用サーフェイスビュー */
	public SurfaceView view = null;
	/** ボリューム */
	private float volume = 100f;
	/** ファイル名 */
	private String filename = "";
	/** ループ再生 */
	private boolean loop = false;
	/** 再生開始位置 */
	private int start = 0;
	/** 表示非表示 */
	public boolean visible = false;
	/** 表示座標 */
	private int x = 0;
	/** 表示座標 */
	private int y = 0;
	/** 幅 */
	private int width = 100;
	/** 高さ */
	private int height = 100;
	/** スキップ可能かどうか */
	public boolean canskip = true;
	/** 再生中 */
	private boolean playing = false;


	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	public VideoPlayer(Context context)
	{
		this.context = context;
		this.onComp = this;
	    layout = new FrameLayout(context);

		view = new SurfaceView(context);
	    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
		view.setLayoutParams(params);

		holder = view.getHolder();
	    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    holder.setFormat(PixelFormat.TRANSPARENT);
	    holder.addCallback(this);
	    holder.setKeepScreenOn(false);

	    view.bringToFront();
		view.buildDrawingCache();
		view.setEnabled(true);
		view.invalidate();

		layout.addView(view);
		layout.setOnClickListener(this);
	}

	/**
	 * レイアウト取得
	 * @return
	 */
	public FrameLayout getLayout()
	{
		return layout;
	}

	/**
	 * 表示非表示を設定　UI スレッドで実行
	 * @param v
	 */
	public void setVisible(boolean v)
	{
		Runnable runnable;
		if(v)
		{
			runnable = new Runnable()
			{
				@Override
				public void run()
				{
					setVisibleNow(true);
				}
			};
		}
		else
		{
			runnable = new Runnable()
			{
				@Override
				public void run()
				{
					setVisibleNow(false);
				}
			};
		}
		Util.doWithHandler(runnable);
	}

	/**
	 * 表示非表示を設定　即時実行
	 * @param v
	 */
	public void setVisibleNow(boolean v)
	{
		if(v)
		{
			layout.setVisibility(FrameLayout.VISIBLE);
			layout.setFocusable(true);
			layout.requestFocus();
			view.requestFocus();
			setViewSize(100, 100);
			visible = true;
			Util.log("!onVideoVisible " + visible);
		}
		else
		{
			layout.setVisibility(FrameLayout.INVISIBLE);
			layout.setFocusable(false);
			setViewSize(0, 0);
			visible = false;
			Util.log("!onVideoVisible " + visible);
		}
	}

	/**
	 * 再生用ビューのサイズを変更。
	 * @param width
	 * @param height
	 */
	private void setViewSize(int width, int height)
	{
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
		view.setLayoutParams(params);
	}

	/**
	 * ボリューム設定
	 * @param volume
	 */
	public void setVolume(float volume)
	{
		if(volume < 0) volume = 0;
		else if(volume > 100) volume = 100f;
		this.volume = volume;
	}

	/**
	 * サイズ設定
	 * @param width 幅
	 * @param height 高さ
	 */
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * 幅設定
	 * @param width 幅
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}

	/**
	 * 高さ設定
	 * @param height 高さ
	 */
	public void setHeight(int height)
	{
		this.height = height;
	}

	/**
	 * 表示座標設定
	 * @param x
	 * @param y
	 */
	public void setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * x 座標設定
	 * @param x
	 */
	public void setX(int x)
	{
		this.x = x;
	}

	/**
	 * y 座標設定
	 * @param y
	 */
	public void setY(int y)
	{
		this.y = y;
	}

	/**
	 * ループ再生設定
	 * @param loop
	 */
	public void setLoop(boolean loop)
	{
		this.loop = loop;
	}

	/**
	 * 再生開始位置の設定
	 * @param start
	 */
	public void setStart(int start)
	{
		this.start = start;
	}

	VideoPlayer ttt;

	/**
	 * 再生する
	 * @param loop ループ再生
	 */
	public void play(String storage)
	{
		filename = storage;
		ttt = this;
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				if(Util.mainView == null) return;

				// 表示する
				Util.mainView.setVisible(false);
				setVisibleNow(true);
				// プレイヤーを取得
				player = ResourceManager.loadVideo(context, filename, holder);
				if(player == null)
				{
					setVisibleNow(false);
					Util.error("ムービーが見つかりません:" + filename);
					return;
				}
				// サイズ設定
				int tmpw = width;
				int tmph = height;
				if(Util.fullscreen)	// フルスクリーン時は拡大率を考慮した値に変換
				{
					tmpw *= Util.dispRate;
					tmph *= Util.dispRate;
				}
				setViewSize(tmpw, tmph);
				Util.log("!onResetSize width=" + tmpw + " height=" + tmph);
				// 座標設定
				int tmpx = x;
				int tmpy = y;
				if(Util.fullscreen)
				{
					tmpx *= Util.dispRate;
					tmpy *= Util.dispRate;
					tmpx += Util.basePointX;
					tmpy += Util.basePointY;
				}
				else
				{
					tmpx += Util.basePointX2;
					tmpy += Util.basePointY2;
				}
				Util.log("!onResetPos x=" + tmpx + " y=" + tmpy);
				layout.setPadding(tmpx, tmpy, 0, 0);
				// ディスプレイ設定
//				Util.log("!onSetDisplay");
				player.setDisplay(view.getHolder());
				// 再生準備
				try
				{
					player.prepare();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					player = null;
					stop();
					Util.dialogMessage("KAS", "ムービーの再生に失敗しました");
					return;
				}

				player.setOnCompletionListener(onComp);
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.setVolume(volume/100f, volume/100f);
				player.setLooping(loop);
				if(start > 0)
					player.seekTo(start);
				// 再生
				player.start();
				playing = true;

			}
		};
		Util.doWithHandler(runnable);
	}

	/**
	 * 再生停止
	 */
	public void stop()
	{
		if(player != null)
		{
			if(player.isPlaying())
				player.stop();
			player.release();
			player = null;
			playing = false;
		}
		setVisible(false);
		if(Util.mainView != null)
		{
			Util.mainView.setVisible(true);
			Util.mainView.conductor.startConductor();
		}
	}

	/**
	 * 再生を停止し、VideoPlayer を破棄する。
	 * stop は MainSurfaceView を操作するが、このメソッドはそのようなことはしない。
	 * あくまで VideoPlayer の内部処理のみを行う。
	 */
	public void shutdown()
	{
		if(player != null)
		{
			if(player.isPlaying())
				player.stop();
			player.release();
			player = null;
			playing = false;
		}
	}

	/**
	 * 再生中かどうか
	 * @return
	 */
	public boolean playing()
	{
		return player != null;
	}

	/**
	 * 再生終了時の動作
	 */
	@Override
	public void onCompletion(MediaPlayer p)
	{
		stop();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Util.log("!onSurfaceChanged video");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Util.log("!onSurfaceCreated video");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Util.log("!onSurfaceDestroyed video");
	}

	/**
	 * クリック時の動作
	 */
	@Override
	public void onClick(View arg0)
	{
		if(player != null && canskip && playing && player.isPlaying())
			stop();
	}

}
