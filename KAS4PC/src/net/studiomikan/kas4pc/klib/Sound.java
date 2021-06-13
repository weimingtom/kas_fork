package net.studiomikan.kas4pc.klib;


import net.studiomikan.kas.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 音声再生バッファ
 * @author okayu
 */
public class Sound implements Oggbgm.OnCompletionListener
{
	/** 再生用 MediaPlayer */
	public Oggbgm player = null;
	/** ファイル名 */
	public String filename = "";
	/** 大域音量 */
	public float gvolume = 100f;
	/** 音量 */
	public float volume = 100f;
	/** 再生状況  0:停止 1:再生中 2:一時停止中 */
	public int playState = 0;	// 再生状況 0:停止 1:再生中 2:一時停止中
	/** ループ再生するかどうか */
	public boolean loop = false;

	/** フェード状況　0:なし 1:フェードイン 2:フェードアウト 3:フェード */
	public int flag_fade = 0;	// 0:なし 1:フェードイン 2:フェードアウト 3:フェード
	/** フェードを開始したときの音量 */
	public float fadeStartVol = 0;
	/** フェードの目標音量 */
	public float fadeEndVol = 0;
	/** フェードの時間 */
	public int fadeTime = 0;
	/** フェードを開始した時間 */
	public long fadeStartTime = 0;

	/** データ保存用 */
	public JSONObject json = new JSONObject();

	/** ロック用 */
	private LockObject lock = new LockObject();

	/**
	 * コンストラクタ
	 */
	public Sound()
	{
	}

	/**
	 * セーブ
	 * @return 保存データ
	 * @throws JSONException
	 */
	public JSONObject save() throws JSONException
	{
		if(player != null && playState != 0 && loop)
		{
			if(flag_fade != 0)	// フェード中
			{
				if(flag_fade == 2)	// フェードアウトの時は停止扱い
					json.put("state", "nosound");
				else	// フェードイン、又はフェード
				{
					json.put("state", "playing");
					json.put("filename", filename);
					json.put("volume", fadeEndVol);
				}
			}
			else	// 通常再生
			{
				json.put("state", "playing");
				json.put("filename", filename);
				json.put("volume", volume);
			}

			// 一時停止情報
			if(playState == 2)
				json.put("pause", true);
			else
				json.put("pause", false);
		}
		else
			json.put("state", "nosound");
		return json;
	}

	/**
	 * ロード
	 * @param context コンテキスト
	 * @param data 保存データ
	 * @throws JSONException
	 */
	public void load(Context context, JSONObject data) throws JSONException
	{
		if(data == null)
			return;

		String state = data.getString("state");
		if(state.equals("playing"))
		{
			playSound(context, data.getString("filename"), true);
			setVolume((float)data.getDouble("volume"));
			if(data.getBoolean("pause"))
				pauseSound();
		}
	}

	/**
	 * フェードインの開始
	 * @param time 時間
	 * @param startTime フェード開始時間
	 */
	public void startFadeIn(int time, long startTime)
	{
		if(flag_fade != 0)
			endFade();

		fadeTime = time;
		fadeStartTime = startTime;
		flag_fade = 1;
		fadeStartVol = 0;
		fadeEndVol = this.volume;
		setVolume(0);	// 音量ゼロからスタート
	}

	/**
	 * フェードアウトの開始
	 * @param time 時間
	 * @param startTime フェード開始時間
	 */
	public void startFadeOut(int time, long startTime)
	{
		if(flag_fade != 0)
			endFade();

		fadeTime = time;
		fadeStartTime = startTime;
		flag_fade = 2;
		fadeStartVol = this.volume;
		fadeEndVol = 0;
		Util.log("フェードアウト開始：" + fadeStartVol);
	}

	/**
	 * フェードの開始
	 * @param time 時間
	 * @param volume 目標音量
	 * @param startTime フェード開始時間
	 */
	public void startFade(int time, float volume, long startTime)
	{
		if(flag_fade != 0)
			endFade();

		fadeTime = time;
		fadeStartTime = startTime;
		flag_fade = 3;
		fadeStartVol = this.volume;
		fadeEndVol = volume;
	}

	/**
	 * フェードの処理
	 * @param nowTime 現在時刻
	 * @return 終了したら true 継続中なら false
	 */
	public boolean fade(long nowTime)
	{
		if(flag_fade != 0)
		{
			int elapsed = (int)(nowTime - fadeStartTime);
			setVolume(Util.regular(fadeStartVol, fadeEndVol, fadeTime, elapsed));
			if(elapsed >= fadeTime)	// 終了
			{
				endFade();
				return true;
			}
		}
		return false;
	}

	/**
	 * フェードの終了
	 */
	public void endFade()
	{
		if(flag_fade != 0)
		{
			fadeTime = 0;
			fadeStartTime = 0;
			if(flag_fade == 2)	// フェードアウトの時は開始時の音量に戻す
			{
				stopSound();
				setVolume(fadeStartVol);
			}
			else	// それ以外
				setVolume(fadeEndVol);
			flag_fade = 0;
		}
	}

	/**
	 * 音量
	 * @param volume
	 */
	public void setVolume(float volume)
	{
		if(volume < 0)
			volume = 0;
		else if(volume > 100)
			volume = 100;
		this.volume = volume;
		resetVolume();
	}

	/**
	 * 音量取得
	 * @return 音量
	 */
	public float getVolume()
	{
		return this.volume;
	}

	/**
	 * 大域音量
	 * @param gvolume
	 */
	public void setGlobalVolume(float gvolume)
	{
		if(gvolume < 0)
			gvolume = 0;
		else if(gvolume > 100)
			gvolume = 100;
		this.gvolume = gvolume;
		resetVolume();
	}

	/**
	 * 大域音量取得
	 * @return 大域音量
	 */
	public float getGlobalVolume()
	{
		return this.gvolume;
	}

	/**
	 * 音量の再設定
	 */
	public void resetVolume()
	{
		if(this.volume < 0)
			this.volume = 0;
		else if(this.volume > 100)
			this.volume = 100;
		if(gvolume < 0)
			gvolume = 0;
		else if(gvolume > 100)
			gvolume = 100;

		float vol = ( (this.volume/100f) * (gvolume/100f) ) ;

		if(player != null)
			player.setVolume(vol);
	}

	/**
	 * 再生
	 * @param context コンテキスト
	 * @param filename 音声ファイル名
	 * @param loop ループ再生するか
	 */
	public void playSound(Context context, String filename, boolean loop)
	{
		stopSound();

		this.filename = filename;
		player = new Oggbgm();
		if(!player.loadSound(filename))
			return;

//		Util.log("音楽再生：" + filename + " 音量：" + this.volume + " ループ：" + loop);
		player.loop = this.loop = loop;
		player.setOnCompletionListener(this);

		if(flag_fade != 0)
		{
			setVolume(fadeStartVol);
			flag_fade = 0;
		}
		else
			resetVolume();

		player.start();
		playState = 1;
	}

	/**
	 * 停止
	 */
	public void stopSound()
	{
		synchronized(lock)
		{
			if(player == null)
				return;

			Util.log("STOP SOUND " + this.filename);

			if(playState != 0)
				player.stopOgg();

			player = null;
			playState = 0;
			loop = false;
		}
	}

	/**
	 * 一時停止
	 */
	public void pauseSound()
	{
		if(player == null)
			return;

//		if(player.isPlaying())
		if(playState == 1)
		{
			playState = 2;
			player.pauseOgg();
		}
	}

	/**
	 * 再開
	 */
	public void restartSound()
	{
		if(player == null)
			return;

		if(playState == 2)
		{
			playState = 1;
			player.restartOgg();
		}
	}

	/**
	 * 再生完了時の動作
	 */
	@Override
	public void onCompletion(Oggbgm p)
	{
		synchronized(lock)
		{
			if(player == p)
			{
				playState = 0;
				loop = false;
				player = null;
			}
		}
	}

	/**
	 * ロック用
	 * @author okayu
	 */
	private static class LockObject
	{
	}

}
