package net.studiomikan.kas;

import net.studiomikan.kas4pc.klib.Canvas;
import net.studiomikan.kas4pc.klib.Context;


/**
 * レイヤのベースとなるクラス
 * @author okayu
 */
public class KASLayer
{
	/**
	 * 表示順インデックス<br>
	 * 前景レイヤ 0 が 1000、前景レイヤ 1 が 2000 ( 以降 1000 ずつ増える )、<br>
	 * メッセージレイヤ 0 が 1000000、メッセージレイヤ 1 が 1001000 ( 以降 1000 ずつ増える )<br>
	 */
	public int index = 0;
	/** 表示フラグ */
	public boolean visible = false;
	/** レイヤの種類 K:KASLayer L:Layer M:MessageLayer */
	public char layerType = 'K';

	/** x 座標 */
	protected int x = 0;
	/** y 座標 */
	protected int y = 0;
	/** 透明度 */
	protected int opacity = 255;
	/** 幅 */
	protected int width = 100;
	/** 高さ */
	protected int height = 100;

	/** move 用　移動開始前の x 座標 */
	protected int move_start_x = 0;
	/** move 用　移動開始前の y 座標 */
	protected int move_start_y = 0;
	/** move 用　移動開始前の透明度 */
	protected int move_start_o = 0;
	/** move 用　現在の移動の開始位置 */
	private int move_from_x = 0;
	/** move 用　現在の移動の開始位置 */
	private int move_from_y = 0;
	/** move 用　現在の移動の開始不透明度 */
	private int move_from_o = 0;
	/** move 用　現在の移動の目標位置 */
	private int move_to_x = 0;
	/** move 用　現在の移動の目標位置 */
	private int move_to_y = 0;
	/** move 用　現在の移動の目標透明度 */
	private int move_to_o = 0;

	/** move 用　移動パス */
	private String[] move_path = null;
	/** move 用　現在の目標位置 */
	private int move_point = 0;
	/** move 用　accel フラグ */
	private float move_accel = 0;

	/** move 用　現在移動中なら true */
	public boolean moving = false;
	/** move 用　移動にかける時間 */
	private int time = 0;
	/** move 用　全移動にかかる時間 */
	private int totalTime = 0;
	/** move 用　移動開始時間 */
	private long moveStartTime = 0;

	/**
	 * コンストラクタ
	 */
	public KASLayer()
	{
	}

	/**
	 * move の開始
	 * @param path 移動パス
	 * @param accele アクセルフラグ
	 * @param time 時間
	 * @param startTime 開始時間
	 * @throws Exception 移動パスにエラーがあると投げる
	 */
	public void startMove(String path, float accele, int time, long startTime) throws Exception
	{
		if(moving) // 移動中に追加で呼ばれたら、前回のmoveを終わらせる
		{
			move_setPos(move_to_x, move_to_y);
			this.move_setOpacity(move_to_o);
		}

		// 開始前位置を記憶
		move_start_x = move_from_x = move_getStartX();
		move_start_y = move_from_y = move_getStartY();
		move_start_o = move_from_o = move_getStartOpacity();

		// パス解読
		path = path.substring(1, path.length() - 1);	// 最初と最後の()を削除
		move_path = path.split("\\)[ \\t]*\\(");	// )(で分離して保持
		move_point = 0;

		// とりあえず一回目のを作る
		String[] buff2;
		buff2 = move_path[0].split(",");
		if(buff2.length != 3)
			throw new Exception();
		move_to_x = Integer.parseInt(buff2[0]);
		move_to_y = Integer.parseInt(buff2[1]);
		move_to_o = Integer.parseInt(buff2[2]);

		this.time = time;
		totalTime = time * move_path.length;
		moveStartTime = startTime;
		move_accel = accele;

		moving = true;
	}

	/**
	 * move の開始位置の取得
	 * @return x 座標
	 */
	protected int move_getStartX()
	{
		return x;
	}

	/**
	 * move の開始位置の取得
	 * @return y 座標
	 */
	protected int move_getStartY()
	{
		return y;
	}

	/**
	 * move の開始濃度の取得
	 * 普通は opacity でいいが、MessageLayer のみ opacity がレイヤ濃度を表さないので、
	 * これをオーバーライドするべき
	 */
	protected int move_getStartOpacity()
	{
		return opacity;
	}

	/**
	 * onMove イベント
	 * @param nowTime 現在の時間
	 * @return 動いていないなら -1 移動中なら 0 移動終了なら 1
	 */
	public int onMove(long nowTime)
	{
		if(moving)
		{
//			int elapsed = (int)(nowTime - moveStartTime);
			if(nowTime - moveStartTime >= time)	// 一つの移動が終了
			{
				// 目標地へ移動
				move_setPos(move_to_x, move_to_y);
				move_setOpacity(move_to_o);

				move_point++;	// 次
				if(move_point < move_path.length)	// 継続
				{
					// 出発点
					move_from_x = move_to_x;
					move_from_y = move_to_y;
					move_from_o = move_to_o;
					// 新しい目標地を設定
					String[] buff = move_path[move_point].split(",");
					move_to_x = Integer.parseInt(buff[0]);
					move_to_y = Integer.parseInt(buff[1]);
					move_to_o = Integer.parseInt(buff[2]);
					// 時間を初期化
					moveStartTime = nowTime;
					return 0;
				}
				else	// 全て終了した
				{
					moving = false;
					return 1;
				}
			}
			else	// 移動中
			{
				double tick = (double)(nowTime - moveStartTime);
				if(move_accel < 0)
				{
					tick = 1.0 - (tick / totalTime);
					tick = Math.pow(tick, -move_accel);
					tick = (long)( (1.0 - tick) * totalTime );
				}
				else if(move_accel > 0)
				{
					tick = tick / totalTime;
					tick = Math.pow(tick, move_accel);
					tick = (long)( tick * totalTime );
				}
				move((int)tick);
				return 0;
			}
		}
		else
			return -1;
	}

	/**
	 * 座標移動
	 * @param tick 刻み
	 */
	public void move(int tick)
	{
		int x, y, o;
		x = (int)Util.regular(move_from_x, move_to_x, time, tick);
		y = (int)Util.regular(move_from_y, move_to_y, time, tick);
		o = (int)Util.regular(move_from_o, move_to_o, time, tick);
		move_setPos(x, y);
		move_setOpacity(o);
	}

	/**
	 *  moveの終了
	 */
	public void stopMove()
	{
		if(moving)
		{
			moving = false;
			// 最終位置に移動
			String[] buff = move_path[move_path.length-1].split(",");
			int x = Integer.parseInt(buff[0]);
			int y = Integer.parseInt(buff[1]);
			int o = Integer.parseInt(buff[2]);
			move_setPos(x, y);
			move_setOpacity(o);
		}
	}

	/**
	 * move 中の場所移動　必ずオーバーライドする
	 * @param x
	 * @param y
	 */
	public void move_setPos(int x, int y)
	{
	}

	/**
	 * move 中の透明度変更　必ずオーバーライドする
	 * @param opacity 透明度
	 */
	public void move_setOpacity(int opacity)
	{
	}

	/**
	 * 描画イベント	必ずオーバーライドする
	 * @param context コンテキスト
	 * @param c キャンバス
	 * @param nowTime 現在時刻
	 */
	public void onDraw(Context context, Canvas c, long nowTime)
	{
	}

	/**
	 * move状況のコピー
	 * @param layer コピー元レイヤ
	 */
	public void copyMoveState(KASLayer layer)
	{
		if(layer.moving)
		{
			move_start_x = layer.move_start_x;
			move_start_y = layer.move_start_y;
			move_start_o = layer.move_start_o;
			move_from_x = layer.move_from_x;
			move_from_y = layer.move_from_y;
			move_from_o = layer.move_from_o;
			move_to_x = layer.move_to_x;
			move_to_y = layer.move_to_y;
			move_to_o = layer.move_to_o;

			move_path = layer.move_path;
			move_point = layer.move_point;
			move_accel = layer.move_accel;

			moving = layer.moving;
			time = layer.time;
			moveStartTime = layer.moveStartTime;
		}
	}

	/**
	 * レイヤ情報のコピー　必ずオーバーライドする
	 * @param context コンテキスト
	 * @param layer コピー元レイヤ
	 */
	public void copyLayer(Context context, KASLayer layer, long nowTime)
	{
		copyMoveState(layer);
	}

	/**
	 * タッチイベント
	 * true を返すと、何か処理が行われたと判断され、
	 * 他のタッチイベントの動作を無視する。
	 * @param x x 座標
	 * @param y y 座標
	 * @param action イベントの種類 0:down 1:up 2:move
	 * @param context コンテキスト
	 * @return 処理をしたなら true
	 */
	public boolean onTouchEvent(int x, int y, int action, Context context)
	{
		return false;
	}
}


/**
 * レイヤ描画順のソート用 Comparator
 * @author okayu
 */
class DataComparator implements java.util.Comparator<KASLayer>
{
	@Override
	public int compare(KASLayer o1, KASLayer o2)
	{
		return o1.index - o2.index;
	}
}


