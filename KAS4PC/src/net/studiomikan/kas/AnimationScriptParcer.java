package net.studiomikan.kas;

import java.util.ArrayList;

import net.studiomikan.kas4pc.klib.Bitmap;


/**
 * <p>アニメーション定義ファイル用のパーサ<br></p>
 *
 * <p>アニメーション定義ファイルは次のような形式で書く。</p>
 * <pre>
 *   imagewidth=200;
 *   imageheight=50;
 *   width=50;
 *   height=50;
 *   time=20;
 *   pos=0,0;
 *   pos=50,0;
 *   pos=100,0;
 *   pos=150,0;
 *   pos=100,0;
 *   pos=50,0;
 * </pre>
 * @author okayu
 */
class AnimationScriptParcer
{
	/** 画像の幅 */
	public int imageWidth = 100;
	/** 画像の高さ */
	public int imageHeight = 100;
	/** フレームの幅 */
	public int width = 100;
	/** フレームの高さ */
	public int height = 100;
	/** x 座標取得用 */
	private ArrayList<Integer> posx_tmp = null;
	/** y 座標取得用 */
	private ArrayList<Integer> posy_tmp = null;
	/** x 座標 */
	public int[] posx = null;
	/** y 座標 */
	public int[] posy = null;
	/** フレームの時間 */
	public ArrayList<Integer> time = new ArrayList<Integer>();
	/** 現在読み込んでいる地点のフレーム時間 */
	public int tmp_time = 20;
	/** 全フレームの合計時間 */
	public int alltime = 0;
	/** フレーム数 */
	public int count = 0;
	/** 開始時間 */
	public long startTime = -1;
	/** ループさせるかどうか */
	public boolean loop = true;
	/** レート */
	public double rate = 1.0;

	/**
	 * コンストラクタ
	 * @param image アニメーションさせる画像ファイル
	 * @param script アニメーション定義ファイルの内容
	 */
	public AnimationScriptParcer(Bitmap image, String script)
	{
		posx_tmp = new ArrayList<Integer>();
		posy_tmp = new ArrayList<Integer>();

		parce(script);
		count = posx_tmp.size();
		if(image != null)	// 画像比率算出
			rate = (double)image.getWidth() / (double)imageWidth;
		posx = new int[count];
		posy = new int[count];
		for(int i = 0; i < count; i++)
		{
			posx[i] = (int)(posx_tmp.get(i) * rate);
			posy[i] = (int)(posy_tmp.get(i) * rate);
		}
		width *= rate;
		height *= rate;

		posx_tmp.clear(); posx_tmp = null;
		posy_tmp.clear(); posy_tmp = null;
	}

	/**
	 * コピーを生成
	 * @param src 元
	 */
	public AnimationScriptParcer(AnimationScriptParcer src)
	{
		imageWidth = src.imageWidth;
		imageHeight = src.imageHeight;
		width = src.width;
		height = src.height;
		posx = src.posx;
		posy = src.posy;
		time = src.time;
		tmp_time = src.tmp_time;
		alltime = src.alltime;
		count = src.count;
		startTime = src.startTime;
		loop = src.loop;
		rate = src.rate;
	}

	/**
	 * パースする
	 * @param script
	 */
	private void parce(String script)
	{
		script = script.replace(" ", "");

		String[] lines = script.split("\n");
		String[] elms;
		int len = lines.length;

		for(int i = 0; i < len; i++)
		{
			elms = lines[i].split(";");
			for(int j = 0; j < elms.length; j++)
				parceElement(elms[j]);
		}
	}

	/**
	 * 要素一つをパースする
	 * @param elm 要素 pos=0,0 など
	 */
	private void parceElement(String elm)
	{
		if(elm.length() == 0)
			return;
		String[] op = elm.split("=");
		if(op.length == 2)
		{
			if(op[0].equals("imagewidth"))
			{
				if(Util.isInteger(op[1]))
					imageWidth = Integer.decode(op[1]);
			}
			else if(op[0].equals("imageheight"))
			{
				if(Util.isInteger(op[1]))
					imageHeight = Integer.decode(op[1]);
			}
			else if(op[0].equals("width"))
			{
				if(Util.isInteger(op[1]))
					width = Integer.decode(op[1]);
			}
			else if(op[0].equals("height"))
			{
				if(Util.isInteger(op[1]))
					height = Integer.decode(op[1]);
			}
			else if(op[0].equals("time"))
			{
				if(Util.isInteger(op[1]))
					tmp_time = Integer.decode(op[1]);
			}
			else if(op[0].equals("loop"))
			{
				if(Util.isBoolean(op[1]))
					loop = Boolean.parseBoolean(op[1]);
			}
			else if(op[0].equals("pos"))
			{
				String[] p = op[1].split(",");
				switch(p.length)
				{
				case 1:
					if(Util.isInteger(p[0]))
					{
						posx_tmp.add(Integer.decode(p[0]));
						posy_tmp.add(Integer.decode(p[0]));
						time.add(tmp_time);
						alltime += tmp_time;
					}
					break;
				case 2:
					if(Util.isInteger(p[0]) && Util.isInteger(p[1]))
					{
						posx_tmp.add(Integer.decode(p[0]));
						posy_tmp.add(Integer.decode(p[1]));
						time.add(tmp_time);
						alltime += tmp_time;
					}
					break;
				}
			}
		}
	}

	/**
	 * 現在何フレーム目かを得る
	 * @param nowTime 現在時刻
	 * @return フレーム番号
	 */
	public int getFrame(long nowTime)
	{
		if(startTime == -1) return 0;

		long diff = (nowTime-startTime);
		// ループしない場合の終了処理
		if(!loop && diff >= alltime)
		{
			startTime = -1;
			return 0;
		}
		long now = diff % alltime;
		int t = 0;
		for(int i = 0; i < count; i++)
		{
			t += time.get(i);
			if(now < t)
				return i;
		}
		return 0;
	}

	/**
	 * 停止する
	 */
	public void stop()
	{
		startTime = -1;
	}

	/**
	 * 停止しているかどうか
	 * @return 停止しているなら true
	 */
	public boolean isStoped()
	{
		return startTime == -1;
	}

}
