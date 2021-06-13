;--------------------------------------------------------------------
;	右クリック（メニューボタン）サブルーチンのサンプル
;--------------------------------------------------------------------

@systembutton page=fore enabled=false visible=true
@rclick enabled=false
@history output=false enabled=true
@position layer=message1 page=fore top=0 left=0 width=800 height=450 opacity=128 color=0x000000 visible=false
@current layer=message1 page=fore
@defstyle align=center linespacing=14 linesize=26
@deffont size=26
@er

@nowait
@locate y=20

@link target=*rclick_save
　　セーブ　　
@endlink
@r
@link target=*rclick_load
　　ロード　　
@endlink
@r
@link target=*rclick_auto
　オートモード　
@endlink
@r
@link target=*rclick_skip
　　スキップ　　
@endlink
@r
@link target=*rclick_log
　　バックログ　　
@endlink
@r
@link target=*rclick_config
　　環境設定　　
@endlink
@r
@link target=*rclick_hide
メッセージを隠す
@endlink
@r
@link target=*rclick_title
タイトルへ戻る
@endlink
@r
@link target=*rclick_exitGame
　　　終了　　　
@endlink
@r
@link target=*rclick_return
　　　戻る　　　
@endlink
@r

@endnowait

@rclick storage=rclick.ks target=*rclick_return call=false jump=true enabled=true
@layopt layer=message1 page=fore visible=true

@s

;セーブ
*rclick_save
@jump storage=rclicksave.ks target=*start

;ロード
*rclick_load
@jump storage=rclickload.ks target=*start

; オートモード
*rclick_auto
@position layer=message1 page=fore visible=false
@startautomode
@jump target=*rclick_return
@s

; スキップ
*rclick_skip
@position layer=message1 page=fore visible=false
@startskip
@jump target=*rclick_return
@s

; バックログ
*rclick_log
@position layer=message1 page=fore visible=false
@showhistory
@jump target=*rclick_return
@s

; メッセージを隠す
*rclick_hide
@position layer=message1 page=fore visible=false
@hidemessage
@jump target=*rclick_return
@s

; 環境設定
*rclick_config
@jump storage=rclickconfig.ks

; タイトルへ戻る
*rclick_title
@position layer=message1 page=fore visible=false
@gotostart ask=true
@jump target=*rclick_return
@s


; 終了
*rclick_exitGame
@position layer=message1 page=fore visible=false
@close ask=true
@jump target=*rclick_return
@s


; 戻る
*rclick_return
@position layer=message1 page=fore visible=false
@rclick storage=rclick.ks target="" call=true jump=false enabled=true 
@history output=true enabled=true
@current layer=message0 page=fore
@systembutton page=fore visible=true enabled=true
@unlocksnapshot
@return
