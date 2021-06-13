package net.studiomikan.kas;


import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.WindowManager;

/**
 * 設定画面のアクティビティ
 * @author okayu
 */
public class ConfigActivity extends PreferenceActivity
{
	/** テキスト表示間隔 */
	private static int textInterval = 0;
	/** オートモードスピード */
	private static int autoModeSpeed = 0;
	/** スキップモードの種類 */
	private static int skipModeType = 1;
	/** BGM ボリューム */
	private static int bgmVol = 100;
	/** SE ボリューム */
	private static int seVol = 100;
	/** フルスクリーン */
	private static boolean fullscreen = true;
	/** 画質 */
	private static float graphicScale = 1f;
	/** 簡易演出 */
	private static boolean direction = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// フルスクリーン
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// スリープを抑制
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// タイトル
		setTitle("【設定】" + Util.gameTitle);
		// 表示
		addPreferencesFromResource(R.layout.config);
		// 値をセット
		set();
		PreferenceScreen sysver = (PreferenceScreen)this.findPreference("systeminfo_key");
		if(sysver != null)
		{
			String systemVersion = Util.systemVersion;
			sysver.setSummary(systemVersion);
		}
		PreferenceScreen gamever = (PreferenceScreen)this.findPreference("versioninfo_key");
		if(gamever != null)
		{
			String version = Util.gameVersion;
			gamever.setSummary(version);
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		apply();
	}

	/**
	 * 事前に値をセット
	 */
	public static void setValue()
	{
		MainSurfaceView mainView = Util.mainView;
		if(mainView != null)
		{
			setValue(
					mainView.getUserTextInterval(),
					mainView.autoModeInterval,
					mainView.skipModeType,
					(int)mainView.getBgmGlobalVol(),
					(int)mainView.getSeGlobalVol(0),
					Util.fullscreen,
					mainView.layerImageScale,
					mainView.easyDirection);
		}
	}

	/**
	 * 事前に値をセット
	 * @param textInterval
	 * @param autoModeSpeed
	 * @param skipModeType
	 * @param bgmVol
	 * @param seVol
	 * @param fullscreen
	 * @param graphicScale
	 * @param direction
	 */
	public static void setValue(
			int textInterval,
			int autoModeSpeed,
			int skipModeType,
			int bgmVol,
			int seVol,
			boolean fullscreen,
			float graphicScale,
			boolean direction)
	{
		if(textInterval < 0)
			textInterval = 0;
		else if(textInterval > 120)
			textInterval = 120;

		if(autoModeSpeed < 0)
			autoModeSpeed = 0;
		else if(autoModeSpeed > 2000)
			autoModeSpeed = 2000;
		else if(autoModeSpeed%200 != 0)
			autoModeSpeed = 1000;

		if(skipModeType != 1 && skipModeType != 2)
			skipModeType = 1;

		if(bgmVol < 0)
			bgmVol = 0;
		else if(bgmVol > 100)
			bgmVol = 100;

		if(seVol < 0)
			seVol = 0;
		else if(seVol > 100)
			seVol = 100;

		if(graphicScale < 0)
			graphicScale = 0;
		else if(graphicScale > 1)
			graphicScale = 1;

		ConfigActivity.textInterval = textInterval;
		ConfigActivity.autoModeSpeed = autoModeSpeed;
		ConfigActivity.skipModeType = skipModeType;
		ConfigActivity.bgmVol = bgmVol;
		ConfigActivity.seVol = seVol;
		ConfigActivity.fullscreen = fullscreen;
		ConfigActivity.graphicScale = graphicScale;
		ConfigActivity.direction = direction;
	}

	/**
	 * 項目の値を反映
	 */
	public void apply()
	{
		MainSurfaceView mainView = Util.mainView;
		if(mainView == null)
			return;

		ListPreference lp;
		CheckBoxPreference cp;

		// テキスト表示スピード
		lp = (ListPreference)this.findPreference("key_textInterval");
		if(lp != null)
			mainView.setUserTextInterval(Integer.valueOf(lp.getValue()));
		// オート
		lp = (ListPreference)this.findPreference("key_autoModeSpeed");
		if(lp != null)
			mainView.autoModeInterval = Integer.valueOf(lp.getValue());
		// 全文スキップ
		cp = (CheckBoxPreference)this.findPreference("key_allSkip");
		if(cp != null)
		{
			if(cp.isChecked())
				mainView.skipModeType = 2;
			else
				mainView.skipModeType = 1;
		}
		// BGM
		lp = (ListPreference)this.findPreference("key_bgm");
		if(lp != null)
			mainView.setBgmGlobalVol( Integer.valueOf(lp.getValue()) );
		// SE
		lp = (ListPreference)this.findPreference("key_se");
		if(lp != null)
			mainView.setAllSeGlobalVol( Integer.valueOf(lp.getValue()) );
		// フルスクリーン
		cp = (CheckBoxPreference)this.findPreference("key_fullscreen");
		if(cp != null)
			mainView.setFullScreen(cp.isChecked());
		// 画像スケール
		lp = (ListPreference)this.findPreference("key_graphicScale");
		if(lp != null)
			mainView.layerImageScale = Float.valueOf(lp.getValue());
		// 簡易演出
		cp = (CheckBoxPreference)this.findPreference("key_direction");
		if(cp != null)
			mainView.easyDirection = cp.isChecked();

	}

	/**
	 * 各項目に値を設定
	 */
	public void set()
	{
		ListPreference lp;
		CheckBoxPreference cp;
		// テキスト表示スピード
		lp = (ListPreference)this.findPreference("key_textInterval");
		if(lp != null)
		{
			if(textInterval%30 != 0)
				textInterval = 30;
			lp.setValueIndex(textInterval/30);
		}
		// オート
		lp = (ListPreference)this.findPreference("key_autoModeSpeed");
		if(lp != null)
			lp.setValueIndex(autoModeSpeed/200);
		// 全文スキップ
		cp = (CheckBoxPreference)this.findPreference("key_allSkip");
		if(cp != null)
		{
			Util.log("***skipModeType = " + skipModeType);
			if(skipModeType == 2)
			{
				Util.log("checked");
				cp.setChecked(true);
			}
			else
			{
				Util.log("not checked");
				cp.setChecked(false);
			}
		}
		// BGM
		lp = (ListPreference)this.findPreference("key_bgm");
		if(lp != null)
			lp.setValueIndex(bgmVol/10);
		// SE
		lp = (ListPreference)this.findPreference("key_se");
		if(lp != null)
			lp.setValueIndex(seVol/10);
		// フルスクリーン
		cp = (CheckBoxPreference)this.findPreference("key_fullscreen");
		if(cp != null)
		{
			cp.setChecked(fullscreen);
			Util.log("フルスクリーン=" + fullscreen + " " + Util.fullscreen);
		}
		else
			Util.log("フルスクリーン=" + fullscreen + " " + Util.fullscreen);
		// 画像スケール
		lp = (ListPreference)this.findPreference("key_graphicScale");
		if(lp != null)
		{
			if(graphicScale <= 0.5f)
				lp.setValueIndex(0);
			else if(graphicScale <= 0.7f)
				lp.setValueIndex(1);
			else if(graphicScale <= 0.8f)
				lp.setValueIndex(2);
			else
				lp.setValueIndex(3);
		}
		// 簡易演出
		cp = (CheckBoxPreference)this.findPreference("key_direction");
		if(cp != null)
			cp.setChecked(direction);
	}
}

