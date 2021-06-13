package net.studiomikan.kas;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.studiomikan.moka.MOKA_VALUE_TYPE;
import net.studiomikan.moka.MokaValue;

/**
 * ユーザー用初期化スクリプトを適用する
 * @author okayu
 */
public class ConfigScript
{
	/** ファイル名 */
	public static final String filename = "Config.moka";
	/** 初期化スクリプトが存在するかどうか */
	public boolean exist = false;
	/** 実行器 */
	private MokaScript moka;

	public ConfigScript(MokaScript moka)
	{
		this.moka = moka;
		exist = init(moka);
		Util_config();
	}

	/**
	 * スクリプト読み込み
	 * @param moka Moka スクリプト
	 * @param 読み込み成功で true
	 */
	private final boolean init(MokaScript moka)
	{
		StringBuilder sb = new StringBuilder();
		String line;
		BufferedReader br = null;
		try
		{
			br = ResourceManager.loadScenario(filename, Util.context);
			if(br == null)
				return false;
			while(true)
			{
				line = br.readLine();
				if(line == null) break;
				sb.append(line);
				sb.append("\n");
			}
			br.close();
			// スクリプト実行
			String script = sb.toString();
			if(script.length() != 0)
			{
				MokaValue re = moka.execute(sb.toString());
				if(re != null && re.type == MOKA_VALUE_TYPE.BOOLEAN)
					return re.intValue != 0;
				else
					Util.error("設定スクリプトでエラー");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			if(br != null)
				try{ br.close(); }catch(IOException e2){}
		}
		return false;
	}

	/**
	 * リソース関連の設定
	 */
	public boolean ResourceManager_config()
	{
		if(!exist) return false;
		MokaValue value, dic = moka.execute("Res");
		if(dic == null || dic.type != MOKA_VALUE_TYPE.DIC) return false;

		// ◆ ネットワークリソースのアドレス
		List<String> url = ResourceManager.netResourceUrl;
		url.clear();
		if((value = dic.getDicValue("url")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
		{
			int size = value.array.size();
			for(int i = 0; i < size; i++)
				url.add(value.array.get(i).toString());
		}

		// ◆ ネットワークリソースのファイルサイズ
		if((value = dic.getDicValue("filesize")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			ResourceManager.netResourceFileSize = value.intValue;

		return true;
	}

	/**
	 * 全体の設定
	 */
	public void Util_config()
	{
		if(!exist) return;
		MokaValue value, dic = moka.execute("Util");
		if(dic == null || dic.type != MOKA_VALUE_TYPE.DIC) return;

		// ◆ バージョン
		if((value = dic.getDicValue("gameVersion")) != null && value.cast(MOKA_VALUE_TYPE.STRING))
			Util.gameVersion = value.stringValue;

		// ◆ タイトル
		if((value = dic.getDicValue("gameTitle")) != null && value.cast(MOKA_VALUE_TYPE.STRING))
			Util.gameTitle = value.stringValue;

		// ◆ 画面のサイズ
		if((value = dic.getDicValue("standardDispWidth")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			Util.standardDispWidth = (int)value.intValue;
		if((value = dic.getDicValue("standardDispHeight")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			Util.standardDispHeight = (int)value.intValue;

		// ◆ フルスクリーン表示するかどうか
		if((value = dic.getDicValue("fullscreen")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.fullscreen = value.intValue != 0;

		// ◆ スリープからの復帰時にレジュームするかどうか
		if((value = dic.getDicValue("enableAutoResume")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.enableAutoResume = value.intValue != 0;

		// ◆ セーブデータを暗号化するかどうか
		if((value = dic.getDicValue("encrypt")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.encrypt = value.intValue != 0;

		// ◆ セーブデータを圧縮するかどうか
		if((value = dic.getDicValue("compress")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.compress = value.intValue != 0;

		// ◆ セーブデータのプレフィックス
		if((value = dic.getDicValue("saveDataPrefix")) != null && value.cast(MOKA_VALUE_TYPE.STRING))
			Util.saveDataPrefix = value.stringValue;

		// ◆ サムネイルを保存するか
		if((value = dic.getDicValue("saveThumbnail")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.saveThumbnail = value.intValue != 0;

		// ◆ サムネイルのプレフィックス
		if((value = dic.getDicValue("thumbnailPrefix")) != null && value.cast(MOKA_VALUE_TYPE.STRING))
			Util.thumbnailPrefix = value.stringValue;

		// ◆ サムネイルの保存サイズ
		if((value = dic.getDicValue("thumbnailWidth")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			Util.thumbnailWidth = (int)value.intValue;

		// ◆ セーブデータにマクロを保存するかどうか
		if((value = dic.getDicValue("saveMacros")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.saveMacros = value.intValue != 0;

		// ◆ 利用可能なセーブデータの数
		if((value = dic.getDicValue("saveNumMax")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			Util.saveNumMax = (int)value.intValue;

		// ◆ 終了時に尋ねるかどうか
		if((value = dic.getDicValue("ask_exit")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.ask_exit = value.intValue != 0;
		;Util.ask_exit = true;

		// ◆ 存在しないタグをエラーにするかどうか
		if((value = dic.getDicValue("notExistTag")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.notExistTag = value.intValue != 0;

		// 消しとく
		moka.eval("Util = void");
	}

	/**
	 * 画面や動作の設定
	 * @param m MainSurfaceView
	 * @return 設定したなら true
	 */
	public boolean Main_config(MainSurfaceView m)
	{
		if(!exist) return false;
		MokaValue value, dic = moka.execute("Main");
		if(dic == null || dic.type != MOKA_VALUE_TYPE.DIC) return false;

		// ◆ SEバッファの数
		if((value = dic.getDicValue("seBufferNum")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.seBuffNum = (int)value.intValue;

		// ◆ 初期状態の前景レイヤの数
		if((value = dic.getDicValue("numCharacterLayers")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.changeLayerCount((int)value.intValue);

		// ◆ 前景レイヤの左右中心位置
		if((value = dic.getDicValue("scPositionX")) != null && value.type == MOKA_VALUE_TYPE.DIC)
		{
			for(Map.Entry<String, MokaValue> entry : value.dic.entrySet())
			{
				String key = entry.getKey();
				MokaValue v = entry.getValue();
				if(v.cast(MOKA_VALUE_TYPE.INTEGER))
					Layer.scPositionX.put(key, (int)v.intValue);
			}
		}

		// ◆ 初期状態のメッセージレイヤの数
		if((value = dic.getDicValue("numMessageLayers")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.changeMessageLayerCount((int)value.intValue);

		// ◆ 初期状態でメッセージレイヤを表示するかどうか
		if((value = dic.getDicValue("initialMessageLayerVisible")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.layer_fore[0].setVisible(value.intValue != 0);

		// 消しとく
		moka.eval("Main = void");

		return true;
	}

	/**
	 * メッセージレイヤの設定
	 * @param m メッセージレイヤ
	 * @return 設定したなら true
	 */
	public boolean MessageLayer_config(MessageLayer m)
	{
		if(!exist) return false;
		MokaValue value, dic = moka.execute("Mes");
		if(dic == null || dic.type != MOKA_VALUE_TYPE.DIC) return false;

		// ◆ メッセージレイヤの色
		if((value = dic.getDicValue("frameColor")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.setFrameColor((int)value.intValue);

		// ◆ メッセージレイヤの透明度
		if((value = dic.getDicValue("frameOpacity")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.setOpacity((int)value.intValue);

		// ◆ 左右上下マージン
		if((value = dic.getDicValue("marginL")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.setMarginLeft((int)value.intValue);
		if((value = dic.getDicValue("marginT")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.setMarginTop((int)value.intValue);
		if((value = dic.getDicValue("marginR")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.setMarginRight((int)value.intValue);
		if((value = dic.getDicValue("marginB")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.setMarginBottom((int)value.intValue);

		// ◆ 位置とサイズ
		int left, top, width, height;
		left = top = 20; width = 800-40; height = 450-40;
		if((value = dic.getDicValue("top")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			top = (int)value.intValue;
		if((value = dic.getDicValue("width")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			width = (int)value.intValue;
		if((value = dic.getDicValue("height")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			height = (int)value.intValue;
		m.setPos(left, top);
		m.setSize(width, height);

		// ◆ 自動改行
		if((value = dic.getDicValue("defaultAutoReturn")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.defAutoreturn = value.intValue != 0;

		// ◆ 右文字マージン
		// 禁則処理用にあけておく右端の文字数を指定します。
		if((value = dic.getDicValue("marginRCh")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.marginRCh = (int)value.intValue;

		// ◆ 文字サイズ
		if((value = dic.getDicValue("defaultFontSize")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defFontSize = (int)value.intValue;

		// ◆ 行間
		if((value = dic.getDicValue("defaultLineSpacing")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defLinespacing = (int)value.intValue;

		// ◆ 字間
		if((value = dic.getDicValue("defaultPitch")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defPitch = (int)value.intValue;

		// ◆ 文字の色
		if((value = dic.getDicValue("defaultColor")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defColor = (int)value.intValue;

		// ◆ 太字
		if((value = dic.getDicValue("defaultBold")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.defBold = value.intValue != 0;

		// ◆ 斜体
		if((value = dic.getDicValue("defaultItalic")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.defItalic = value.intValue != 0;

		// ◆ 影の色
		if((value = dic.getDicValue("defaultShadowColor")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defShadowColor = (int)value.intValue;

		// ◆ 縁取りの色
		if((value = dic.getDicValue("defaultEdgeColor")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defEdgeColor = (int)value.intValue;

		// ◆ 影
		if((value = dic.getDicValue("defaultShadow")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.defShadow = value.intValue != 0;

		// ◆ 縁取り
		if((value = dic.getDicValue("defaultEdge")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.defEdge = value.intValue != 0;

		// ◆ 行末グリフ画像
		if((value = dic.getDicValue("lineBreakGlyph")) != null && value.cast(MOKA_VALUE_TYPE.STRING))
			m.lineBreakGlyph = value.stringValue;

		// ◆ ページ末グリフ画像
		if((value = dic.getDicValue("pageBreakGlyph")) != null && value.cast(MOKA_VALUE_TYPE.STRING))
			m.pageBreakGlyph = value.stringValue;

		// ◆ クリック待ちグリフを固定した場所に表示するかどうか
		if((value = dic.getDicValue("glyphFixedPosition")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			m.glyphFixedPosition = value.intValue != 0;

		// ◆ クリック待ちグリフを固定した場所に表示するときの位置
		if((value = dic.getDicValue("glyphFixedLeft")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.glyphFixedLeft = (int)value.intValue;
		if((value = dic.getDicValue("glyphFixedTop")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.glyphFixedTop = (int)value.intValue;

		// ◆ リンクの強調色
		if((value = dic.getDicValue("defaultLinkColor")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defLinkColor = (int)value.intValue;

		// ◆ リンクの不透明度
		if((value = dic.getDicValue("defaultLinkOpacity")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			m.defLinkOpacity = (int)value.intValue;

		// 消しとく
		moka.eval("Mes");

		return true;
	}

	/**
	 * オプションメニューの設定
	 * @return 設定したなら true
	 */
	public boolean Menu_config()
	{
		if(!exist) return false;
		MokaValue value, dic = moka.execute("Menu");
		if(dic == null || dic.type != MOKA_VALUE_TYPE.DIC)
			return false;

		// ◆ オプションメニューの表示・非表示
		if((value = dic.getDicValue("menuVisible")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			MainActivity.menuVisible = value.intValue != 0;

		// ◆ 標準オプションメニューを利用するか
		if((value = dic.getDicValue("builtInOptionMenu")) != null && value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			Util.builtInOptionMenu = value.intValue != 0;

		// ◆ 各オプションメニューの設定
		Map<String, MenuItemData> menu = MainActivity.menuItemData;

		// セーブ
		if((value = dic.getDicValue("save")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("save").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// ロード
		if((value = dic.getDicValue("load")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("load").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// メッセージ履歴
		if((value = dic.getDicValue("history")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("history").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// メッセージを隠す
		if((value = dic.getDicValue("hide")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("hide").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// スキップ開始
		if((value = dic.getDicValue("skip")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("skip").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// 環境設定
		if((value = dic.getDicValue("config")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("config").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// オートモード開始
		if((value = dic.getDicValue("auto")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("auto").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// タイトルへ戻る（最初へ戻る）
		if((value = dic.getDicValue("title")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("title").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// ゲームデータ削除（ネットワークリソース削除）
		if((value = dic.getDicValue("deldata")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("deldata").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);
		// ゲーム終了
		if((value = dic.getDicValue("exit")) != null && value.type == MOKA_VALUE_TYPE.ARRAY)
			menu.get("exit").set(value.getArrayValue(0).stringValue, value.getArrayValue(1).intValue != 0);

		// 消しとく
		moka.eval("Menu");

		return true;
	}

	/**
	 * 履歴の設定
	 * @param h 履歴
	 */
	public boolean History_config(History h)
	{
		if(!exist) return false;
		MokaValue value, dic = moka.execute("History");
		if(dic == null || dic.type != MOKA_VALUE_TYPE.DIC)
			return false;

		// ◆ フォントサイズ
		if((value = dic.getDicValue("fontSize")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.fontSize = (int)value.intValue;

		// ◆ 行の高さ
		if((value = dic.getDicValue("linespacing")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.linespacing = (int)value.intValue;

		// ◆ 表示位置
		if((value = dic.getDicValue("left")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.left = (int)value.intValue;
		if((value = dic.getDicValue("top")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.top = (int)value.intValue;

		// ◆ 表示サイズ
		if((value = dic.getDicValue("width")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.width = (int)value.intValue;
		if((value = dic.getDicValue("height")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.height = (int)value.intValue;

		// ◆ 閉じるボタンの位置
		if((value = dic.getDicValue("close_left")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.close_left = (int)value.intValue;
		if((value = dic.getDicValue("close_top")) != null && value.cast(MOKA_VALUE_TYPE.INTEGER))
			h.close_top = (int)value.intValue;

		// 消しとく
		moka.eval("History");

		return true;
	}

}
