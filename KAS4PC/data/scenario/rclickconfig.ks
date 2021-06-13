;--------------------------------------------------------------------
;	環境設定画面のサンプル
;--------------------------------------------------------------------

*start

@layopt layer=message1 page=fore visible=false
@current layer=message1 page=fore
@er
@backlay
@tempsave place=0
@rclick call=false jump=true storage=rclickconfig.ks target=*config_return enabled=true
@history output=false enabled=false

*config_redraw

@systembutton page=back enabled=false visible=false
@position layer=message0 page=back top=0 left=0 width=800 height=450 opacity=0 color=0x000000 visible=true marginl=20
@position layer=message1 page=back top=0 left=0 width=800 height=450 opacity=200 color=0x001c58 visible=true marginl=20


@current layer=message1 page=back
@defstyle align=left linesize=29 linespacing=14 
@deffont size=26
@er
@nowait

【ＣＯＮＦＩＧ】
@r
@font size=22

; 文字
文字速度　　：　
@link exp="setUserTextSpeed(0)" target=*config_unlock
ノーウェイト
@endlink
　　
@link exp="setUserTextSpeed(30)" target=*config_unlock
高速
@endlink
　　
@link exp="setUserTextSpeed(60)" target=*config_unlock
普通
@endlink
　　
@link exp="setUserTextSpeed(90)" target=*config_unlock
低速
@endlink
　　
@link exp="setUserTextSpeed(120)" target=*config_unlock
超低速
@endlink
@r

; オートモード
オート速度　：　
@link exp="setAutoSpeed(0)" target=*config_unlock
０　
@endlink
@link exp="setAutoSpeed(200)" target=*config_unlock
１　
@endlink
@link exp="setAutoSpeed(400)" target=*config_unlock
２　
@endlink
@link exp="setAutoSpeed(600)" target=*config_unlock
３　
@endlink
@link exp="setAutoSpeed(800)" target=*config_unlock
４　
@endlink
@link exp="setAutoSpeed(1000)" target=*config_unlock
５　
@endlink
@link exp="setAutoSpeed(1200)" target=*config_unlock
６　
@endlink
@link exp="setAutoSpeed(1400)" target=*config_unlock
７　
@endlink
@link exp="setAutoSpeed(1600)" target=*config_unlock
８　
@endlink
@link exp="setAutoSpeed(1800)" target=*config_unlock
９　
@endlink
@link exp="setAutoSpeed(2000)" target=*config_unlock
１０
@endlink
@r

; BGM
ＢＧＭ音量　：　
@link exp="setBgmGVolume(0)" target=*config_unlock
０　
@endlink
@link exp="setBgmGVolume(25)" target=*config_unlock
２５　
@endlink
@link exp="setBgmGVolume(50)" target=*config_unlock
５０　
@endlink
@link exp="setBgmGVolume(75)" target=*config_unlock
７５　
@endlink
@link exp="setBgmGVolume(100)" target=*config_unlock
１００
@endlink
@r

; SE
ＳＥ音量　　：　
@link exp="setSeGVolue(0)" target=*config_unlock
０　
@endlink
@link exp="setSeGVolue(25)" target=*config_unlock
２５　
@endlink
@link exp="setSeGVolue(50)" target=*config_unlock
５０　
@endlink
@link exp="setSeGVolue(75)" target=*config_unlock
７５　
@endlink
@link exp="setSeGVolue(100)" target=*config_unlock
１００
@endlink
@r
@r

; 全文スキップ
@if exp="getSkipAll()"
	@link exp="setSkipAll(false)" target=*config_redraw
	全文スキップ：　有効
	@endlink
@else
	@link exp="setSkipAll(true)" target=*config_redraw
	全文スキップ：　無効
	@endlink
@endif
@r

; 簡易演出
@if exp="getEasyDirection()"
	@link exp="setEasyDirection(false)" target=*config_redraw
	簡易演出　　：　有効
	@endlink
@else
	@link exp="setEasyDirection(true)" target=*config_redraw
	簡易演出　　：　無効
	@endlink
@endif
@r
@r

; 戻る
@link target=*config_return
戻る
@endlink

; 停止
@endnowait
@locklink
@trans time=300 method=crossfade
*config_unlock
@unlocklink
@s


*config_return
@tempload place=0 backlay=true bgm=false se=false
@history enabled=true output=true
@disablestore store=false restore=false
@systembutton page=back visible=true

;トランジション
@locklink
@trans time=300 method=crossfade
@unlocklink

@current layer=message0 page=fore
@rclick call=true jump=false storage=rclick.ks target="" enabled=true
@systembutton page=fore visible=true enabled=true
@return

