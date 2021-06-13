package net.studiomikan.kas;

import java.util.List;
import java.util.Map;

import net.studiomikan.kas4pc.gui.MainActivity;
import net.studiomikan.kas4pc.gui.MenuItemData;

//------------------------------------------------------------------
//	Config クラス
//
//	KASの諸設定を指定するクラス
//	このファイルの内容を変更することでKASをカスタマイズできます
//------------------------------------------------------------------
public class Config
{
	//-------------------------------------------------- リソース関連の設定 ----

	public static final void ResourceManager_config(MokaScript moka)
	{
		if(moka.conf.ResourceManager_config()) return;

		List<String> url = ResourceManager.netResourceUrl;

		// ◆ ネットワークリソースのアドレス
		// ネットワークリソース（KPCファイル）の URL を指定します。
		// ここで指定した URL のファイルは、初回起動時に SD カードへとダウンロードされます。
		// ネットワークリソースを利用しない場合は空文字 "" を指定します。
		//
		// 例：url.add("http://www.xxxxxxxxx.xxx/data.kpc");
		//
		// 複数の URL を指定することもできます。
		//
		// 例：url.add("http://www.xxxxxxxxx.xxx/data.kpc");
		//     url.add("http://www.yyyyyyyyy.yyy/data.kpc");
		//     url.add("http://www.zzzzzzzzz.zzz/data.kpc");
		//
		// 複数の URL を指定した場合、どれか一つがランダムに選ばれてダウンロードされます。
		// ダウンロード先を分割することでサーバーの負荷を軽減できます。
		// 複数指定をする場合は、全ての URL に同じファイルを置くようにしてください。
		url.add("");


		// ◆ ネットワークリソースのファイルサイズ
		// ネットワークリソースのファイルサイズを、バイト単位で指定します。
		// 主にファイルのダウンロードが完了しているかどうかのチェックに使用されます。
		ResourceManager.netResourceFileSize = 0;

	}

	//---------------------------------------------------------- 全体の設定 ----

	public static final void Util_config()
	{
		// ◆ バージョン
		// ゲームのバージョンを表す文字列。設定画面で表示されます。
		// setversionタグでも変更できます。
		Util.gameVersion = "unknown version";


		// ◆ タイトル
		// ゲームのタイトルです。
		// アプリケーション名（KAS Generatorで指定したもの）とは別物です。
		// ここで指定したタイトルはセーブ画面と設定画面で表示されます。
		// title タグでも指定できます。
		Util.gameTitle = "ゲームタイトル";


		// ◆ データディレクトリのパス
		// 画像などのリソースファイルや、セーブデータ等を配置するディレクトリを指定します。
		// 他のアプリケーションと被らないような名前である必要があります。
		// "" を指定すると、パッケージ名と同じになります。
		// 通常はパッケージ名を使用することをおすすめします。
		Util.rootDirectoryPath_user = "";


		// ◆ 画面のサイズ
		// 画面のサイズを指定します。
		// ここで指定したサイズで画面を構成します。
		// あまり大きな数字を指定すると、メモリ不足になる可能性が高くなります。
		// 標準は800×450（16:9）です。
		Util.standardDispWidth = 800;
		Util.standardDispHeight = 450;


		// ◆ フルスクリーン表示するかどうか
		// 画面をディスプレイサイズに拡大して表示するかどうかを指定します。
		// true を指定すると、画面を拡大してフルスクリーンで表示します。
		// false を指定すると、元のサイズのままディスプレイの中央に表示します。
		// この値は初期値です。ユーザーの設定がシステム変数に記憶されるようになっています。
		Util.fullscreen = true;


		// ◆ スリープからの復帰時にレジュームするかどうか
		// 端末がスリープから復帰した際、最後に通過したセーブ可能ラベル時点での
		// 状態を復元するかどうかを指定します。
		Util.enableAutoResume = false;


		// ◆ セーブデータを暗号化するかどうか
		// セーブデータや既読・未読データ等を暗号化するかどうかを指定します。
		// 暗号化することでプレイヤーがセーブデータの中身を閲覧できなくなります。
		// ※この設定を変えた時には、それまでのセーブデータを削除してください。
		Util.encrypt = false;


		// ◆ セーブデータを圧縮するかどうか
		// セーブデータや既読・未読データ等を圧縮保存するかどうかを指定します。
		// 圧縮することでプレイヤーがセーブデータの中身を閲覧できなくなり、
		// SD カードの容量をほんの少しですが節約できます。
		// ※この設定を変えた時には、それまでのセーブデータを削除してください。
		Util.compress = false;


		// ◆ セーブデータのプレフィックス
		// セーブデータのファイル名の先頭につける文字列です。
		// アルファベットのみの文字列を指定してください。
		// ※使用できない文字列："datasc","datasu","datatrail",
		Util.saveDataPrefix = "savedata_";


		// ◆サムネイルを保存するか
		// true を指定するとセーブの際、セーブデータとは別に
		// サムネイル画像が保存されます。
		Util.saveThumbnail = true;


		// ◆ サムネイルのプレフィックス
		// サムネイルのファイル名の先頭につける文字列です。
		// アルファベットのみの文字列を指定してください。
		// ※使用できない文字列："davasf","datatrail",
		Util.thumbnailPrefix = "thumbnail_";


		// ◆ サムネイルの保存サイズ
		// サムネイルの横幅を指定します。saveThumbnail を true に設定しないとこの
		// 設定は意味がありません。
		// 横幅はピクセル単位で指定してください。
		// 縦幅は画面サイズの比から自動的に算出されます。
		Util.thumbnailWidth = 133;


		// ◆ セーブデータにマクロを保存するかどうか
		// セーブデータにマクロの情報を記憶させるかどうかを指定します。
		// true を指定すると、マクロの情報がセーブデータに保存され、ロード時には
		// マクロが復元されます。
		// false を指定すると、マクロの情報は保存されず、ロードしてもマクロは
		// 復元されません。
		// たとえばシナリオの先頭で全マクロの定義をするような場合には、古いマクロの
		// 情報がセーブデータに残ってしまい、ロード時に新しいマクロを上書きしてしまう
		// ため、false を指定するほうが良いでしょう。
		Util.saveMacros = false;


		// ◆ 利用可能なセーブデータの数
		// 標準のセーブ＆ロード画面で、ユーザーが選択可能なセーブデータの数です。
		// save や load タグで保存可能なセーブデータの数には影響しません。
		// 標準のセーブ＆ロード画面によるセーブデータの管理を行わない場合
		// (ゲーム画面中でセーブデータ管理を行う場合など)は、この値は必要な数に設定し、
		// オプションメニューの変更でセーブとロードを非表示にして下さい。
		Util.saveNumMax = 40;


		// ◆ 終了時に尋ねるかどうか
		// メニューの「終了」および端末の戻るボタンが押されたとき、
		// ダイアログを表示するかどうか指定します。
		// true を指定すると、ダイアログが表示されます。
		// false を指定すると、ダイアログを表示せず、すぐに終了します。
		Util.ask_exit = true;


		// ◆ 存在しないタグをエラーにするかどうか
		// 存在しないタグに対して、エラーダイアログを表示するかどうかを設定します。
		// true を指定するとエラーダイアログを表示して終了します。
		// false を指定するとそのタグは無視されます。
		Util.notExistTag = false;


	}

	//---------------------------------------------------- 画面や動作の設定 ----

	public static final void Main_config(MainSurfaceView o, MokaScript moka)
	{
		if(moka.conf.Main_config(o)) return;

		// ◆ SEバッファの数
		// 利用可能な効果音バッファの数を指定します。
		// ここで指定した数の分だけ、効果音を同時に再生できます。
		int seBufferNum = 3;


		// ◆ 初期状態の前景レイヤの数
		// ゲーム起動時の前景レイヤの数を指定します。
		// 数が多いと速度が低下したりメモリを消費したりするので、
		// 必要最低限の数を指定するようにしてください。
		// 必要なければ 0 を指定してかまいません。
		// laycount タグでも変更できます。
		int numCharacterLayers = 3;


		// ◆ 前景レイヤの左右中心位置
		// +-----------+
		// |           |
		// | |   |   | |
		// | | | | | | |
		// +-----------+
		// image タグの pos 属性で指定する画像位置、および名前を指定します。
		// Layer.scPositionX.put("位置名", 中心位置); の形で指定してください。
		Layer.scPositionX.put("left",         200);
		Layer.scPositionX.put("left_center",  300);
		Layer.scPositionX.put("center",       400);
		Layer.scPositionX.put("right_center", 500);
		Layer.scPositionX.put("right",        600);

		Layer.scPositionX.put("l",            200);
		Layer.scPositionX.put("lc",           300);
		Layer.scPositionX.put("c",            400);
		Layer.scPositionX.put("rc",           500);
		Layer.scPositionX.put("r",            600);


		// ◆ 初期状態のメッセージレイヤの数
		// ゲーム起動時のメッセージレイヤの数を指定します。
		// 数が多いと速度が低下したりメモリを消費したりするので、
		// 必要最低限の数を指定するようにしてください。
		// laycount タグでも変更できます。
		int numMessageLayers = 3;


		// ◆ 初期状態でメッセージレイヤを表示するかどうか
		// true を指定するとメッセージレイヤ0が初期状態で表示状態になりますが、
		// false を指定すると初期状態ではすべてのメッセージレイヤは初期状態で非表示
		// になります。
		boolean initialMessageLayerVisible = true;


		// 色々と反映　※削除しないでください
		o.seBuffNum = seBufferNum;
		o.changeLayerCount(numCharacterLayers);
		o.changeMessageLayerCount(numMessageLayers);
		o.layer_fore[0].setVisible(initialMessageLayerVisible);
		// 色々と反映　※ここまで

	}

	//---------------------------------------------- メッセージレイヤの設定 ----

	public static final void MessageLayer_config(MessageLayer m, MokaScript moka)
	{
		if(moka.conf.MessageLayer_config(m)) return;

		// ◆ メッセージレイヤの色
		// 0xRRGGBB 形式でメッセージレイヤの色を指定します。
		// position タグの color 属性に相当
		int frameColor = 0x000000;


		// ◆ メッセージレイヤの透明度
		// フレーム画像を指定しなかった時の、メッセージレイヤの矩形枠の透明度を
		// 0 ～ 255 の数値で指定します。
		// position タグの opacity 属性に相当
		int frameOpacity = 127;


		// ◆ 左右上下マージン
		// マージン (余白) を pixel 単位で指定します。
		// position タグの marginl, maringt, marginr, marginb 属性に対応します。
		int marginL = 10; // 左余白
		int marginT = 10; // 上余白
		int marginR = 10; // 右余白
		int marginB = 10; // 下余白


		// ◆ 位置とサイズ
		// 初期位置とサイズを指定します。
		// position タグのそれぞれ left top width height の属性に対応します。
		int left = 20;			// 左端位置
		int top = 20;			// 上端位置
		int width = 800-40;		// 幅
		int height = 450-40;	// 高さ


		// ◆ 自動改行
		// メッセージレイヤの右端に到達したとき、
		// 自動的に改行するかどうかを指定します。
		// defstyle タグの autoreturn 属性に相当
		m.defAutoreturn = true;


		// ◆ 右文字マージン
		// 禁則処理用にあけておく右端の文字数を指定します。
		m.marginRCh = 2;


		// ◆ 文字サイズ
		// デフォルトの文字サイズを指定します。
		// deffont タグの size 属性に相当
		m.defFontSize = 22;


		// ◆ 行間
		// 行間の大きさを指定します。
		// defstyle タグの linespacing に相当
		m.defLinespacing = 18;


		// ◆ 字間
		// 字間の大きさを指定します。
		// defstyle タグの pitch 属性に相当
		m.defPitch = 0;


		// ◆ 文字の色
		// デフォルトの文字の色を 0xRRGGBB で指定します。
		// deffont タグの color 属性に相当
		m.defColor = 0xffffff;


		// ◆ 太字
		// 文字を太字にするかどうかを指定します。
		// フォントによっては適用されないこともあります。
		// deffont タグの bold 属性に相当
		m.defBold = false;


		// ◆ 斜体
		// 文字を斜体にするかどうかを指定します。
		// フォントによっては適用されないこともあります。
		// deffont タグの bold 属性に相当
		m.defItalic = false;


		// ◆ 影の色
		// 影の色を 0xRRGGBB で指定します。
		// deffont タグの shadowcolor に属性に相当
		m.defShadowColor = 0x000000;


		// ◆ 縁取りの色
		// 縁取りの色を 0xRRGGBB で指定します。
		// deffont タグの edgecolor に属性に相当
		m.defEdgeColor = 0x000000;


		// ◆ 影
		// 文字の影を描画するかどうかを指定します。
		// deffont タグの shadow 属性に相当
		m.defShadow = true;


		// ◆ 縁取り
		// 文字の縁取りを描画するかどうかを指定します。
		// deffont タグの edge 属性に相当
		m.defEdge = false;


		// ◆ 行末グリフ画像
		// 行末クリック待ちで表示する画像を指定します。
		// 同じ名前のアニメーション定義ファイルがある場合はアニメーションします。
		m.lineBreakGlyph = "linebreak";


		// ◆ ページ末グリフ画像
		// ページ末クリック待ちで表示する画像を指定します。
		// 同じ名前のアニメーション定義ファイルがある場合はアニメーションします。
		m.pageBreakGlyph = "pagebreak";


		// ◆ クリック待ちグリフを固定した場所に表示するかどうか
		// false を指定すると、現在の文字表示位置に表示されます。
		// true を指定すると、glyphFixedLeft および glyphFixedTop で指定した位置に表示されます。
		m.glyphFixedPosition = false;  // glyph タグの fix 属性に相当


		// ◆ クリック待ちグリフを固定した場所に表示するときの位置
		// glyphFixedLeft にはグリフを表示する左端位置を、
		// glyphFixedTop にはグリフを表示する上端位置を指定してください。
		m.glyphFixedLeft = 0;  // glyph タグの left 属性に相当
		m.glyphFixedTop = 0;  // glyph タグの top 属性に相当


		// ◆ リンクの強調色
		// リンクを選択したときに出る半透明矩形のデフォルトの色です。
		// link タグの color 属性に相当
		m.defLinkColor = 0x00469d;


		// ◆ リンクの不透明度
		// リンクを選択したときに出る半透明矩形の不透明度です。
		m.defLinkOpacity = 127;


		// 色々と反映　※削除しないでください
		m.setFrameColor(frameColor);
		m.setOpacity(frameOpacity);
		m.setMargin(marginT, marginR, marginB, marginL);
		m.setPos(left, top);
		m.setSize(width, height);
		// 色々と反映　※ここまで

	}


	//-------------------------------------------- オプションメニューの設定 ----

	public static final void Menu_config(MokaScript moka)
	{
		if(moka.conf.Menu_config()) return;

		Map<String, MenuItemData> menu = MainActivity.menuItemData;

		// ◆ オプションメニューの表示・非表示
		// 端末のメニューボタンを押したときのメニューを表示するかどうかを指定します。
		// trueで表示、falseで非表示になります。
		// falseの場合、メニューボタンを押しても何も表示されなくなります。
		MainActivity.menuVisible = true;


		// ◆ 標準オプションメニューを利用するか
		// システム標準のオプションメニューを利用するかどうかを指定します。
		// false を指定すると、rclick タグで動作を指定できるようになります。
		Util.builtInOptionMenu = true;


		// ◆ 各オプションメニューの設定
		// オプションメニューの各項目の表示・非表示、および表示されるテキストを指定します。
		// menu.get("hoge").set(テキスト, 表示); で指定してください。

		// セーブ
		menu.get("save").set("セーブ", true);

		// ロード
		menu.get("load").set("ロード", true);

		// メッセージ履歴
		menu.get("history").set("バックログ", true);

		// メッセージを隠す
		menu.get("hide").set("メッセージを隠す", true);

		// スキップ開始
		menu.get("skip").set("スキップ開始", true);

		// 環境設定
		menu.get("config").set("設定", true);

		// オートモード開始
		menu.get("auto").set("オートモード開始", true);

		// タイトルへ戻る（最初へ戻る）
		menu.get("title").set("タイトルへ戻る", true);

		// ゲームデータ削除（ネットワークリソース削除）
		menu.get("deldata").set("ゲームデータ削除", true);

		// ゲーム終了
		menu.get("exit").set("終了", true);

	}


	//------------------------------------------------ メッセージ履歴の設定 ----

	public static final void History_config(History h, MokaScript moka)
	{
		if(moka.conf.History_config(h)) return;

		// ◆ フォントサイズ
		// メッセージ履歴のフォントサイズを指定します。
		h.fontSize = 24;


		// ◆ 行の高さ
		// 一行の高さを指定します。
		h.linespacing = 35;


		// ◆ 表示位置
		// メッセージの表示開始位置を指定します。
		h.left = 10;	// 左位置
		h.top = 20;		// 上位置


		// ◆ 表示サイズ
		// メッセージの表示される領域のサイズを指定します。
		h.width = 720;	// 幅
		h.height = 420;	// 高さ


		// ◆ 閉じるボタンの位置
		// 閉じるボタンの位置を指定します。
//		h.close_left = 750;	// 左位置
//		h.close_top = 10;	// 上位置

	}

}
