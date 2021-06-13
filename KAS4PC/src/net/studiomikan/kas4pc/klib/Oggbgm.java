package net.studiomikan.kas4pc.klib;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.studiomikan.kas.ResourceManager;


/**
 *
 * http://www20.atpages.jp/daimeimitei/
 *
 * OGGBGMクラス
 * VorbisSPIを使ってOGGのBGMを簡単に再生します。
 * 使い方
 * インスタンスを作らずスタティックの関数で再生できます。
 * Oggbgm.play(String filename);//で繰り返し再生
 * Oggbgm.play1(String filename);//で一回再生
 * Oggbgm.play(String filename0, String filename1);//で前奏と繰り返し部に分けて繰り返し再生
 * Oggbgm.pausing=true;//で一時停止
 * Oggbgm.stopping=true;//で停止
 * Oggbgm.vol=0.5f;で音量を半分にする
 */
public class Oggbgm extends Thread
{
	/** ファイル名 */
	public String filename = null;
	/** ループ再生 */
	public boolean loop;
	/** ボリューム */
	public float vol = 1.0f;
	/** 一時停止 */
	public boolean pausing;
	/** 停止 */
	public boolean stopping;
//	/** ogg ファイルのストリーム */
//	private InputStream is = null;
	/** 変換後ストリーム */
	private AudioInputStream ais = null;
	/** 変換後の形式 */
	private AudioFormat af = null;
	/** バッファを書き込むやつ */
	private SourceDataLine sdl = null;
	/** バッファ */
	private byte[] buf = new byte[16384];
	/** 再生完了時の動作 */
	private OnCompletionListener onComp = null;

	/** 再生完了時の動作 */
	public interface OnCompletionListener
	{
		public void onCompletion(Oggbgm p);
	}

	/**
	 * コンストラクタ
	 */
	public Oggbgm()
	{
		super();
	}

	/**
	 * ファイル読み込み
	 * @param filename ファイル名
	 * @return 成功で true 失敗で false
	 */
	public boolean loadSound(String filename)
	{
		this.filename = filename;
		return setstart(filename, true, this);
	}

	/**
	 * 再生
	 */
	public void playOgg()
	{
		if(sdl != null)
			start();
	}

	/**
	 * 停止
	 */
	public void stopOgg()
	{
		stopping = true;
	}

	/**
	 * 一時停止
	 */
	public void pauseOgg()
	{
		pausing = true;
	}

	/**
	 * 再生再開
	 */
	public void restartOgg()
	{
		pausing = false;
	}

	public void setVolume(float volume)
	{
		if(volume < 0f) volume = 0;
		else if(volume > 1f) volume = 1;
		this.vol = volume;
	}

	public void setOnCompletionListener(OnCompletionListener listner)
	{
		this.onComp = listner;
	}

	@Override
	public void run()
	{
		//本当の再生
		playn();
	}

	/**
	 * 再生の処理
	 */
	private void playn()
	{

		//繰り返し再生
		do
		{
			//まだ続きあるかの判定に使う
			int bbytes = 0;
			//初め
			sdl.start();
			try{
				//ストリーミング（終わりでなく、停止されていないとき）
				while(bbytes != -1 && !stopping)
				{
					//一時停止でないとき
					if(!pausing)
					{
						//読み込む
						bbytes = ais.read(buf, 0, buf.length);
						//音量が変えられている
						if(vol!=1.0f)
						{
							for(int b=0;b<buf.length;b++)
							{
								buf[b]*=vol;
							}
						}
						if(bbytes != -1)
						{
							//書きこむ
							sdl.write(buf, 0, bbytes);
						}
					}
					//一時停止中
					else
					{
						//定期的に待つ
						try{
							Thread.sleep(100);
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}
			//止められた
			if(stopping)
			{
				//終了
				break;
			}
			//まだ繰り返すときは
			if(loop)
			{
				//曲読んで最初に戻す
				setstart(filename, false, this);
			}
		}
		while(loop);
		//再生しきる
		sdl.drain();
		//閉じる
		sdl.close();
		try
		{
			ais.close();
//			is.close();
		} catch (IOException e){
			e.printStackTrace();
		}

		if(onComp != null)
			onComp.onCompletion(this);

	}

	/**
	 * 曲読んで変換して最初の地点に戻す関数
	 * @param filename ファイル名
	 * @param setinfoline infoとlineの再設定をするか（初回読み込み時のみ true）
	 * @param obj
	 */
	private static synchronized boolean setstart(String filename, boolean setinfoline, Oggbgm obj)
	{
		try
		{
			//読む
			InputStream is = ResourceManager.loadSound(filename);
			if(is == null)
				return false;
			AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
			//形式
			AudioFormat format = audioStream.getFormat();
			//変換後形式
			obj.af = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					format.getSampleRate(),
					16,
					format.getChannels(),
					format.getChannels()*2,
					format.getSampleRate(),
					false);
			obj.ais = AudioSystem.getAudioInputStream(obj.af, audioStream);
			//infoとlineの再設定（最初のみ）
			if(setinfoline)
			{
				//情報取得
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, obj.af);
				//ライン
				try
				{
					obj.sdl = (SourceDataLine)AudioSystem.getLine(info);
					obj.sdl.open(obj.af, obj.buf.length);
				}catch(LineUnavailableException e){
					e.printStackTrace();
					return false;
				}
			}
		}catch(UnsupportedAudioFileException e1){
			e1.printStackTrace();
			return false;
		}catch (IOException e1){
			e1.printStackTrace();
			return false;
		}
		return true;
	}


}


