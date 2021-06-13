;--------------------------------------------------------------------
;	ロード画面のサンプル
;--------------------------------------------------------------------

; 再設定
*init
@systembutton page=fore enabled=false
@history output=false enabled=true
@rclick storage=rclick.ks target=*rclick_return call=false jump=true enabled=true
@position layer=message1 page=fore top=0 left=0 width=800 height=450 opacity=128 color=0x000000 visible=false
@current layer=message1 page=fore
@defstyle align=center linesize=30
@deffont size=27
@er

; 開始
*start
@position layer=message1 page=fore visible=false
@er
@eval exp="tf.load_pos = 0;"

@nowait
@locate y=100

【ロード】
@r

@if exp="saveDataExists(0);"
	@link target=*load exp="tf.load_pos = 0"
		データ１：
		@emb exp="getSaveDataText(0);"
	@endlink
@else
	データ１：ーーーーーーーー
@endif
@r

@if exp="saveDataExists(1);"
	@link target=*load exp="tf.load_pos = 1"
		データ２：
		@emb exp="getSaveDataText(1);"
	@endlink
@else
	データ２：ーーーーーーーー
@endif
@r

@if exp="saveDataExists(2);"
	@link target=*load exp="tf.load_pos = 2"
		データ３：
		@emb exp="getSaveDataText(2);"
	@endlink
@else
	データ３：ーーーーーーーー
@endif
@r

@link target=*return
戻る
@endlink

@endnowait
@layopt layer=message1 page=fore visible=true
@s

*load
@layopt layer=message1 page=fore visible=false
@load ask=true place=&tf.load_pos

*return
@position layer=message1 page=fore visible=false
@rclick storage=rclick.ks target="" call=true jump=false enabled=true 
@current layer=message0 page=fore
@systembutton page=fore visible=true enabled=true
@return

