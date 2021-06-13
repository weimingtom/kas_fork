package net.studiomikan.kas;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * KAS プラグイン
 * @author okayu
 */
public class KASPlugin implements TagObject
{
	/** KAS プラグイン */
	public KASPlugin()
	{
		// MainSurfaceView の管理に追加
		if(Util.mainView != null)
			Util.mainView.kasPlugins.add(this);
	}

	/**
	 * レイヤを追加する。必ず表と裏をセットで登録する。
	 * @param fore 表レイヤ
	 * @param back 裏レイヤ
	 * @return 正常に追加できれば true、レイヤが足りない、又はエラーなら false
	 */
	public static boolean addLayer(KASLayer fore, KASLayer back)
	{
		if(Util.mainView == null || fore == null || back == null)
			return false;

		Util.mainView.pluginsLayer_fore.add(fore);
		Util.mainView.pluginsLayer_back.add(back);
		Util.mainView.setLayerOrder();

		return true;
	}

	/**
	 * レイヤを追加する。必ず表と裏をセットで登録する。
	 * @param fore 表レイヤ
	 * @param back 裏レイヤ
	 * @return 正常で true 失敗で false
	 */
	public static boolean addLayers(KASLayer[] fore, KASLayer[] back)
	{
		if(fore == null || back == null) return false;
		if(fore.length != back.length) return false;
		if(Util.mainView == null) return false;

		for(int i = 0; i < fore.length; i++)
		{
			if(fore[i] == null || back[i] == null)
				return false;
			Util.mainView.pluginsLayer_fore.add(fore[i]);
			Util.mainView.pluginsLayer_back.add(back[i]);
		}
		Util.mainView.setLayerOrder();
		return true;
	}

	/**
	 * スクリプトをタグとして登録する。
	 * @param name タグの名前
	 */
	public void addTag(String name)
	{
		if(Util.mainView == null) return;
		Util.mainView.addTag(name, this);
	}

	/**
	 * セーブデータを保存するときに呼ばれる
	 * @param data セーブデータの JSON オブジェクト　この JSON オブジェクトにデータを保存する
	 * @throws JSONException
	 */
	public void onStore(JSONObject data) throws JSONException
	{
	}

	/**
	 * セーブデータをロードするときに呼ばれる
	 * @param data セーブデータの JSON オブジェクト
	 * @param clear メッセージレイヤをクリアするかどうか（tmpload 時のみ false）
	 * @param toback 裏画面へのロードなら true
	 * @throws JSONException
	 */
	public void onRestore(JSONObject data, boolean clear, boolean toback) throws JSONException
	{
	}

	/**
	 * 安定（s l p タグで停止中）または走行中の状態が変化したときに呼ばれる
	 * @param stable true なら安定状態、 false なら走行状態
	 */
	public void onStableStateChanged(boolean stable)
	{
	}

	/**
	 * メッセージレイヤが隠された、あるいはそれから復帰した時に呼ばれる。
	 * @param hidden 隠される時に true、表示される時に true
	 */
	public void onMessageHiddenStateChanged(boolean hidden)
	{
	}

	/**
	 * レイヤ情報のコピーが行われる時に呼ばれる。
	 * @param toback 表から裏へのコピーなら true、逆なら false
	 */
	public void onCopyLayer(boolean toback)
	{
	}

	/**
	 * トランジションなどで、表画面と裏画面が入れ替わった後に呼ばれる。
	 */
	public void onExchangeForeBack()
	{
	}

	/**
	 * タッチイベント
	 * true を返すと、何か処理が行われたと判断され、
	 * 他のタッチイベントの動作を無視する。
	 * @param x タッチされた座標
	 * @param y タッチされた座標
	 * @param action イベントの種類 0:down 1:up 2:move
	 * @return イベントを拾って処理したなら true
	 */
	public boolean onTouchEvent(int x, int y, int action)
	{
		return false;
	}

	/**
	 * タグとして動作する際に呼ばれる。
	 * @param elm 属性
	 */
	@Override
	public void run(HashMap<String, String> elm)
	{
	}
}
