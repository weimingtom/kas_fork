;--------------------------------------------------------------------
;	KAS サンプルシナリオ
;--------------------------------------------------------------------
*sample


; タイトル設定
@title name="KAS サンプルシナリオ"
; バージョン情報を設定
@setversion text="KAS サンプルシナリオ version 1.6"
; マクロ設定のサブルーチンをコール
@call storage=macro.ks
; クリアカウント用の変数を用意
@eval exp="tf.clearCount = 1"

; コメントを外し、Config.java の builtInOptionMenu を false にすると
; 右クリックサブルーチンが有効になる
;@rclick storage=rclick.ks target="" call=true jump=false enabled=true 

; システムボタンを設定
@systembutton page=fore visible=false
@systembutton page=back visible=true

; 「タイトルへ戻る」の先を設定
*startanchor|ＫＡＳ紹介
@startanchor

; 全部消す
@ct
@layopt layer=all page=back visible=false
@layopt layer=all page=fore visible=false

; BGMを再生します
@bgmopt volume=100
@playbgm storage=bgm0

*back

; 背景を白にしておく
@image layer=base page=back storage=white visible=true

; メッセージレイヤ表示
@position layer=message0 page=back visible=true color=0x000000 width=760 height=410 top=20 left=20 opacity=128
@trans method=crossfade time=2000 canskip=true

; オートセーブを有効にする
@autosave enabled=true
*savelabel_1|ＫＡＳ紹介

こんにちは。
@lr

@if exp="tf.clearCount != 1"
	@emb exp="get2bnum(tf.clearCount)"
	週目ですね。
@else
	ＫＡＳへようこそ。
@endif
@pcm

ＫＡＳはＡｎｄｒｏｉｄ向けのノベルゲームエンジンです。
@lr
無償で、吉里吉里／ＫＡＧに似たタグでシナリオファイルを記述します。
@pcm

現在ベータ版ですが、それなりの機能は実装されています。たとえば、
@lr

@backlay
@image layer=base page=back storage=bg0
@trans method=crossfade time=1000 canskip=true
背景を表示したり、
@lr

@backlay
@image layer=0 page=back storage=chara3 pos=center width=300 height=400 visible=true
@trans method=crossfade time=1000 canskip=true
キャラクターを表示したりできます。
@pcm

@deffont size=40
文字をでっかくしてみたり。
@pcm
@deffont size=22
@defstyle linesize=22

@position layer=message0 page=fore width=760 height=130 top=300
アドベンチャー形式のメッセージ枠にもできます。
@pcm

*save|選択肢

@animstop

選択肢も出せます。
@lr

@nowait
@link storage=sample.ks target=*sample_jump1
選択肢その１
@endlink
@r
@link storage=sample.ks target=*sample_jump2
選択肢その２
@endlink
@endnowait

@s

*sample_jump1
@er
選択肢その１が押されました。
@pcm
@jump target=*sample_jump3

*sample_jump2
@er
選択肢その２が押されました。
@pcm
@jump target=*sample_jump3

*sample_jump3

クロスフェード以外に、ユニバーサルトランジションが可能です。
@pcm

@backlay
@image layer=base page=back storage=black
@layopt layer=0 page=back visible=false
@layopt layer=message0 page=back visible=false
@trans time=2000 method=universal rule=rule_around canskip=true vague=64

@image layer=base page=back storage=bg1 visible=true
@image layer=0 page=back storage=chara2 visible=true width=300 height=400 pos=center
@trans time=2000 method=universal rule=rule_around canskip=true vague=64

サンプルは以上です。
@r
まだまだ貧弱ですが、一応これくらいの機能はありますので、
もし気が向いたら使ってみてくださいませ。
@pcm

立ち絵は「キャラクターなんとか機」、ＢＧＭは
@r
Ｓｌｉｖｅｒｄｅｓｔｅｅｌ様の素材を利用させていただきました。
@r
ありがとうございます。
@pcm

最初に戻ります。
@pcm

@layopt layer=all page=back visible=false
@image layer=base page=back storage=black
@trans time=1000 method=crossfade canskip=true
@layopt layer=0 page=back visible=false
@freeimage layer=0 page=back

; 読み切ったら変数 tf.clearCount を +1 しておきます
@eval exp="tf.clearCount += 1"

; 最初に戻る
@jump target=*back
@s
