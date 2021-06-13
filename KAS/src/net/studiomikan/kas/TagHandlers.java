package net.studiomikan.kas;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * タグハンドラ用
 * @author okayu
 */
interface TagObject
{
	/**
	 * タグの内容
	 * @param elm 属性
	 */
	public void run(HashMap<String, String> elm);
}

/**
 * タグの実行を管理してあげるクラス
 * @author okayu
 */
class TagHandlers
{
	/** タグマップ */
	private TreeMap<String, TagObject> tagHandlersMap = null;
	/** 親 */
	private MainSurfaceView owner = null;

	/**
	 * コンストラクタ
	 * @param owner 親
	 */
	public TagHandlers(MainSurfaceView owner)
	{
		tagHandlersMap = new TreeMap<String, TagObject>();
		getTagHandlers(owner);
	}

	/**
	 * タグを返す
	 * @param tagName タグ名
	 * @return タグ
	 */
	public TagObject getTagObject(String tagName)
	{
		return tagHandlersMap.get(tagName);
	}

	/**
	 * タグの内容を登録する
	 * @param owner 親
	 */
	public void getTagHandlers(MainSurfaceView owner)
	{
		if(owner == null)
			return;
		this.owner = owner;
		getTagHandlers();
	}

	/**
	 * タグを追加する
	 * @param name
	 * @param tag
	 */
	public void addTag(String name, TagObject tag)
	{
		tagHandlersMap.put(name, tag);
	}

	/**
	 * タグの内容を登録する
	 */
	public void getTagHandlers()
	{
		//------------------------------------------------------------------------------------------
		// システム操作

		// autoresume : KAS オリジナル
		// 自動ウェイト
		tagHandlersMap.put("autoresume",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_autoresume(elm); }});
		// autowc
		// 自動ウェイト
		tagHandlersMap.put("autowc",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_autowc(elm); }});
		// callconfig : KAS オリジナル
		// 設定画面を呼び出す
		tagHandlersMap.put("callconfig",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_callconfig(); }});
		// callload : KAS オリジナル
		// ロード画面を呼び出す
		tagHandlersMap.put("callload",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_callload(); }});
		// callsave : KA Sオリジナル
		// セーブ画面を呼び出す
		tagHandlersMap.put("callsave",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_callsave(); }});
		// clearsysvar
		// システム変数全削除
		tagHandlersMap.put("clearsysvar",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_clearsysvar(); }});
		// clickskip
		// クリックスキップ
		tagHandlersMap.put("clickskip",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_clickskip(elm); }});
		// close
		// 終了
		tagHandlersMap.put("close",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_close(elm); }});
		// hidemessage
		// メッセージレイヤの一時消去
		tagHandlersMap.put("hidemessage",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_hidemessage(); }});
		// quake
		// 画面揺らし
		tagHandlersMap.put("quake",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_quake(elm); }});
		// rclick
		// メニューボタンクリック時の動作
		tagHandlersMap.put("rclick",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_rclick(elm); }});
		// s
		// 停止
		tagHandlersMap.put("s",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_s(); }});
		// setversion : KAS オリジナル
		// ゲームのバージョンを設定
		tagHandlersMap.put("setversion",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_setversion(elm); }});
		// startautomode : KAS オリジナル
		// オートモードを開始する
		tagHandlersMap.put("startautomode",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_startautomode(); }});
		// startskip : KAS オリジナル
		// スキップを開始する
		tagHandlersMap.put("startskip",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_startskip(); }});
		// stopquake
		// 画面揺らしの停止
		tagHandlersMap.put("stopquake",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_stopquake(); }});
		// title
		// タイトル変更
		tagHandlersMap.put("title",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_title(elm); }});
		// wait
		// ウェイト
		tagHandlersMap.put("wait",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wait(elm); }});
		// waitclick
		// クリック待ち スキップ不可
		tagHandlersMap.put("waitclick",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_waitclick(); }});
		// wc
		// 文字数分のウェイト
		tagHandlersMap.put("wc",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wc(elm); }});
		// wq
		// 画面揺らしの終了を待つ
		tagHandlersMap.put("wq",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wq(elm); }});



		//------------------------------------------------------------------------------------------
		// マクロ操作

		// erasemacro
		// マクロを削除
		tagHandlersMap.put("erasemacro",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_erasemacro(elm); }});



		//------------------------------------------------------------------------------------------
		// メッセージ操作

		// cancelautomode
		// オートモードを終わる
		tagHandlersMap.put("cancelautomode",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_cancelautomode(); }});
		// cancelskip
		// スキップモードを終わる
		tagHandlersMap.put("cancelskip",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_cancelskip(); }});
		// ch
		// 文字を表示
		tagHandlersMap.put("ch",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_ch(elm); }});
		// cm
		// メッセージレイヤの消去　全レイヤを消去、カレントレイヤの変更はなし
		tagHandlersMap.put("cm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_cm(); }});
		// ct
		// メッセージレイヤの消去　全レイヤを消去、カレントレイヤをmessage0foreに
		tagHandlersMap.put("ct",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_ct(); }});
		// current
		// カレントメッセージレイヤの変更
		tagHandlersMap.put("current",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_current(elm); }});
		// deffont
		// フォント設定のデフォルト値設定
		tagHandlersMap.put("deffont",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_deffont(elm); }});
		// defstyle
		// スタイルのデフォルト値設定
		tagHandlersMap.put("defstyle",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_defstyle(elm); }});
		// delay
		// 文字表示速度
		tagHandlersMap.put("delay",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_delay(elm); }});
		// endindent
		// インデントを終了
		tagHandlersMap.put("endindent",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_endindent(); }});
		// endnowait
		// nowaitを終了
		tagHandlersMap.put("endnowait",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_endnowait(); }});
		// er
		// メッセージレイヤの消去　カレントレイヤのみ
		tagHandlersMap.put("er",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_er(); }});
		// font
		// フォント設定
		tagHandlersMap.put("font",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_font(elm); }});
		// glyph
		// クリック待ち記号の指定
		tagHandlersMap.put("glyph",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_glyph(elm); }});
		// indent
		// インデントを開始
		tagHandlersMap.put("indent",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_indent(); }});
		// l
		// 行末のクリック待ち
		tagHandlersMap.put("l",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_l(); }});
		// locate
		// 文字表示位置の指定
		tagHandlersMap.put("locate",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_locate(elm); }});
		// locklink
		// リンクのロック
		tagHandlersMap.put("locklink",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_locklink(); }});
		// nowait
		// endnowaitを呼ぶまでウェイトをゼロにする
		tagHandlersMap.put("nowait",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_nowait(); }});
		// p
		// 改ページのクリック待ち
		tagHandlersMap.put("p",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_p(); }});
		// position
		// メッセージレイヤの設定
		tagHandlersMap.put("position",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_position(elm); }});
		// r
		// 改行
		tagHandlersMap.put("r",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_r(); }});
		// resetfont
		// フォント設定のリセット
		tagHandlersMap.put("resetfont",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_resetfont(); }});
		// resetstyle
		// スタイル設定のリセット
		tagHandlersMap.put("resetstyle",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_resetstyle(); }});
		// style
		// スタイルの設定
		tagHandlersMap.put("style",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_style(elm); }});
		// unlocklink
		// リンクのロック解除
		tagHandlersMap.put("unlocklink",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_unlocklink(); }});



		//------------------------------------------------------------------------------------------
		// メッセージ履歴操作

		// clearhistory : KASオリジナル
		// 履歴のクリア
		tagHandlersMap.put("clearhistory",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_clearhistory(); }});
		// history
		// 履歴への出力等の設定
		tagHandlersMap.put("history",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_history(elm); }});
		// hr
		// 履歴の改行
		tagHandlersMap.put("hr",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_hr(elm); }});
		// showhistory
		// 履歴の表示
		tagHandlersMap.put("showhistory",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_showhistory(); }});



		//------------------------------------------------------------------------------------------
		// ラベル・ジャンプ操作

		// button
		// レイヤをコピー
		tagHandlersMap.put("button",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_button(elm); }});
		// call
		// サブルーチンの呼び出し
		tagHandlersMap.put("call",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_call(elm); }});
		// endlink
		// ハイパーリンク終了
		tagHandlersMap.put("endlink",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_endlink(); }});
		// jump
		// シナリオファイルの移動
		tagHandlersMap.put("jump",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_jump(elm); }});
		// link
		// ハイパーリンク
		tagHandlersMap.put("link",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_link(elm); }});
		// return
		// サブルーチンから戻る
		tagHandlersMap.put("return",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_return(); }});



		//------------------------------------------------------------------------------------------
		// レイヤ操作

		// animstart
		// アニメーションを開始
		tagHandlersMap.put("animstart",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_animstart(elm); }});
		// animstop
		// アニメーションを停止
		tagHandlersMap.put("animstop",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_animstop(elm); }});
		// backlay
		// レイヤ情報のコピー
		tagHandlersMap.put("backlay",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_backlay(elm); }});
		// copylay
		// レイヤをコピー
		tagHandlersMap.put("copylay",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_copylay(elm); }});
		// freeimage
		// レイヤ画像を解放
		tagHandlersMap.put("freeimage",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_freeimage(elm); }});
		// image
		// イメージの読み込み
		tagHandlersMap.put("image",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_image(elm); }});
		// laycount
		// レイヤの数を変更
		tagHandlersMap.put("laycount",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_laycount(elm); }});
		// layopt
		// レイヤの設定
		tagHandlersMap.put("layopt",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_layopt(elm); }});
		// move
		// レイヤの自動移動
		tagHandlersMap.put("move",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_move(elm); }});
		// pimage
		// レイヤの自動移動
		tagHandlersMap.put("pimage",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_pimage(elm); }});
		// stopmove
		// レイヤの自動移動を停止
		tagHandlersMap.put("stopmove",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_stopmove(); }});
		// stoptrans
		// トランジションを停止
		tagHandlersMap.put("stoptrans",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_stoptrans(); }});
		// trans
		// トランジション
		tagHandlersMap.put("trans",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_trans(elm); }});
		// wa
		// レイヤのアニメーションの終了を待つ
		tagHandlersMap.put("wa",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wa(elm); }});
		// wm
		// レイヤ自動移動の終了を待つ
		tagHandlersMap.put("wm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wm(elm); }});



		//------------------------------------------------------------------------------------------
		// 効果音・BGM・ビデオ操作

		// bgmopt
		// bgmの設定
		tagHandlersMap.put("bgmopt",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_bgmopt(elm); }});
		// fadebgm
		// BGMを指定音量までフェード
		tagHandlersMap.put("fadebgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_fadebgm(elm); }});
		// fadeinbgm
		// bgmのフェードイン
		tagHandlersMap.put("fadeinbgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_fadeinbgm(elm); }});
		// fadeinse
		// bgmのフェードアウト
		tagHandlersMap.put("fadeinse",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_fadeinse(elm); }});
		// fadeoutbgm
		// bgmのフェードアウト
		tagHandlersMap.put("fadeoutbgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_fadeoutbgm(elm); }});
		// fadeoutse
		// bgmのフェードアウト
		tagHandlersMap.put("fadeoutse",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_fadeoutse(elm); }});
		// fadese
		// SEを指定音量までフェード
		tagHandlersMap.put("fadese",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_fadese(elm); }});
		// pausebgm
		// BGMを一時停止
		tagHandlersMap.put("pausebgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_pausebgm(); }});
		// playbgm
		// bgmの再生
		tagHandlersMap.put("playbgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_playbgm(elm); }});
		// playse
		// seの再生
		tagHandlersMap.put("playse",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_playse(elm); }});
		// playvideo
		// ビデオの再生
		tagHandlersMap.put("playvideo",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_playvideo(elm); }});
		// resumebgm
		// BGMを一時停止
		tagHandlersMap.put("resumebgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_resumebgm(); }});
		// seopt
		// seの設定
		tagHandlersMap.put("seopt",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_seopt(elm); }});
		// stopbgm
		// bgmの停止
		tagHandlersMap.put("stopbgm",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_stopbgm(elm); }});
		// stopse
		// seの停止
		tagHandlersMap.put("stopse",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_stopse(elm); }});
		// wb
		// BGMのフェードを待つ
		tagHandlersMap.put("wb",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wb(elm); }});
		// wf
		// SEのフェード待ち
		tagHandlersMap.put("wf",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wf(elm); }});
		// wl
		// BGMの再生終了待ち
		tagHandlersMap.put("wl",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_wl(elm); }});
		// ws
		// SEの再生終了待ち
		tagHandlersMap.put("ws",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_ws(elm); }});



		//------------------------------------------------------------------------------------------
		// 変数・TJS 操作

		// cleartmpvar : KAS オリジナル
		// 一時変数の全消去
		tagHandlersMap.put("cleartmpvar",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_cleartmpvar(); }});
		// clearvar
		// ゲーム変数の全消去
		tagHandlersMap.put("clearvar",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_clearvar(); }});
		// emb
		// 式の評価結果を出力
		tagHandlersMap.put("emb",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_emb(elm); }});
		// eval
		// 式の評価
		tagHandlersMap.put("eval",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_eval(elm); }});
		// iscript
		// Moka スクリプトの評価
		tagHandlersMap.put("iscript",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_iscript(elm); }});



		//------------------------------------------------------------------------------------------
		// 栞・通過記録操作

		// autosave : KAS オリジナル
		// 自動セーブの設定
		tagHandlersMap.put("autosave",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_autosave(elm); }});
		// copybookmark
		// セーブデータのコピー
		tagHandlersMap.put("copybookmark",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_copybookmark(elm); }});
		// disablestore
		// セーブ機能の一時的な使用不可
		tagHandlersMap.put("disablestore",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_disablestore(elm); }});
		// erasebookmark
		// セーブデータの削除
		tagHandlersMap.put("erasebookmark",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_erasebookmark(elm); }});
		// gotostart
		// 最初に戻る
		tagHandlersMap.put("gotostart",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_gotostart(elm); }});
		// load
		// セーブデータを読み込む
		tagHandlersMap.put("load",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_load(elm); }});
		// locksnapshot
		// スナップショットのロック
		tagHandlersMap.put("locksnapshot",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_locksnapshot(); }});
		// save
		// セーブする
		tagHandlersMap.put("save",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_save(elm); }});
		// startanchor
		// 最初に戻るの位置を設定
		tagHandlersMap.put("startanchor",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_startanchor(elm); }});
		// store
		// 最初に戻るの位置を設定
		tagHandlersMap.put("store",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_store(elm); }});
		// tempload
		// メモリ上からロード
		tagHandlersMap.put("tempload",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_tempload(elm); }});
		// tempsave
		// メモリ上にセーブ
		tagHandlersMap.put("tempsave",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_tempsave(elm); }});
		// unlocksnapshot
		// スナップショットのロック解除
		tagHandlersMap.put("unlocksnapshot",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_unlocksnapshot(); }});



		//------------------------------------------------------------------------------------------
		// その他

		// assign : KASオリジナル
		// 変数に代入
		tagHandlersMap.put("assign",
		new TagObject(){ public void run(HashMap<String, String> elm){ owner.tag_assign(elm); }});
	}

}
