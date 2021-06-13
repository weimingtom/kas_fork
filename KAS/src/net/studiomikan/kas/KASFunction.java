package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.studiomikan.moka.MOKA_VALUE_TYPE;
import net.studiomikan.moka.Moka;
import net.studiomikan.moka.MokaFunction;
import net.studiomikan.moka.MokaFunction.BuiltInFunction;
import net.studiomikan.moka.MokaValue;

import org.json.JSONException;

/**
 * KAS 操作用の Moka 関数
 * @author okayu
 */
class KASFunction
{
	public static MokaValue reValue = new MokaValue();

	/**
	 * 関数を登録
	 */
	static final void define(Moka moka)
	{
		// Moka 組み込み関数の上書き
		MokaFunction.addFunction("fread", new FileRead());
		MokaFunction.addFunction("fwrite", new FileWrite());

		// システム関係
		MokaFunction.addFunction("kasTag", new KASTag());
		MokaFunction.addFunction("fileExists", new FileExists());
		MokaFunction.addFunction("closeGame", new CloseGame());

		MokaFunction.addFunction("saveDataExists", new SaveDataExists());
		MokaFunction.addFunction("getSaveDataText", new GetSaveDataText());
		MokaFunction.addFunction("getSaveDataDate", new GetSaveDataDate());
		MokaFunction.addFunction("deleteSaveData", new DeleteSaveData());

		MokaFunction.addFunction("setFullScreen", new SetFullScreen());
		MokaFunction.addFunction("getFullScreen", new GetFullScreen());
		MokaFunction.addFunction("setEasyDirection", new SetEasyDirection());
		MokaFunction.addFunction("getEasyDirection", new GetEasyDirection());

		// テキスト関連
		MokaFunction.addFunction("setUserTextSpeed", new SetUserTextSpeed());
		MokaFunction.addFunction("getUserTextSpeed", new GetUserTextSpeed());
		MokaFunction.addFunction("setAutoSpeed", new SetAutoSpeed());
		MokaFunction.addFunction("getAutoSpeed", new GetAutoSpeed());
		MokaFunction.addFunction("setSkipAll", new SetSkipAll());
		MokaFunction.addFunction("getSkipAll", new GetSkipAll());

		// 音声関連
		MokaFunction.addFunction("setBgmVolume", new SetBgmVolume());
		MokaFunction.addFunction("getBgmVolume", new GetBgmVolume());
		MokaFunction.addFunction("setBgmGVolume", new SetBgmGVolume());
		MokaFunction.addFunction("getBgmGVolume", new GetBgmGVolume());

		MokaFunction.addFunction("setSeVolume", new SetSeVolume());
		MokaFunction.addFunction("getSeVolume", new GetSeVolume());
		MokaFunction.addFunction("setSeGVolume", new SetSeGVolume());
		MokaFunction.addFunction("getSeGVolume", new GetSeGVolume());

		// その他雑多
		MokaFunction.addFunction("get2bnum", new Get2ByteNum());
	}



	// -------------------------------------------------------------------------
	//		Moka 組み込み関数上書き
	// -------------------------------------------------------------------------

	/**
	 * Moka 組み込み関数の上書き。
	 * SD カードのファイルを操作する関数として、ここで上書きする。
	 *
	 * ファイルを読み込んでその文字列を返す<br>
	 * 第一引数をファイル名として読み込みます。引数が無い場合は null を返します。
	 * @author okayu
	 */
	static class FileRead implements BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() == 0) return MokaValue.NULL;

			MokaValue v = args.get(0);
			if(!v.cast(MOKA_VALUE_TYPE.STRING)) return MokaValue.NULL;

			String filename = v.stringValue;

			String str = Util.file2str(Util.context, filename);

			if(str == null)
				return MokaValue.NULL;
			else
			{
				reValue.assign(str);
				return reValue;
			}
		}
	}

	/**
	 * Moka 組み込み関数の上書き。
	 * SD カードのファイルを操作する関数として、ここで上書きする。
	 *
	 * ファイルに文字列を書き込みます
	 * 第一引数をファイル名、第二引数を書きこむ文字列とします。
	 * 書き込み成功で true 失敗で false を返します。
	 * @author okayu
	 */
	static class FileWrite implements BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() < 1) return MokaValue.FALSE;

			MokaValue v1 = args.get(0);
			MokaValue v2 = args.get(1);

			if(!v1.cast(MOKA_VALUE_TYPE.STRING) || !v2.cast(MOKA_VALUE_TYPE.STRING))
				return MokaValue.FALSE;

			Util.str2file(Util.context, v2.stringValue, v1.stringValue);

			return MokaValue.TRUE;
		}
	}


	// -------------------------------------------------------------------------
	//		システム関連
	// -------------------------------------------------------------------------


	/**
	 * タグを呼び出す。<br>
	 * 第一引数にタグ名を、第二引数に辞書配列を渡すと、辞書配列を属性としてタグを呼び出す。<br>
	 * 例：<code>tag("layopt", %[layer=>"0", page=>"fore", visible=>"false"]);</code>
	 * 正しく呼べれば true 失敗すれば false を返す。タグ自体が正しく動作したかどうかは判断しない。<br>
	 * @author okayu
	 */
	static class KASTag implements MokaFunction.BuiltInFunction
	{
		private static HashMap<String, String> elm = new HashMap<String, String>();
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			int size = args.size();
			if(size < 0)
				return MokaValue.FALSE;
			MokaValue tagname = args.get(0);
			MokaValue elmvalue = args.get(1);

			if(!tagname.cast(MOKA_VALUE_TYPE.STRING)) return MokaValue.FALSE;
			if(elmvalue.type != MOKA_VALUE_TYPE.DIC) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.FALSE;

			elm.clear();

			String name = tagname.stringValue;

			String key;
			MokaValue value;
			for(Map.Entry<String, MokaValue> entry : elmvalue.dic.entrySet())
			{
				key = entry.getKey();
				value = entry.getValue();
				elm.put(key, value.toString());
			}

			Util.mainView.tagHandlers(name, elm);
			return MokaValue.TRUE;
		}
	}

	/**
	 * SD カード内のファイルの存在確認<br>
	 * 第一引数にファイル名を指定する。ファイルが存在するなら true、存在しないなら false が返る。
	 * @author okayu
	 */
	static class FileExists implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.STRING))
				{
					if(Util.fileExistence(value.stringValue))
						return MokaValue.TRUE;
				}
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * ゲームを終了する。<br>
	 * 引数が true なら、ダイアログを表示してから終了する。
	 * false ならそのまま終了する。
	 * @author okayu
	 */
	static class CloseGame implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			boolean ask = false;
			if(args.size() != 0)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.BOOLEAN))
					ask = value.intValue != 0;
			}
			Util.closeGameWithAsk(ask);
			return null;
		}
	}

	/**
	 * セーブデータの存在確認<br>
	 * 第一引数にセーブ番号を指定する。ファイルが存在するなら true、存在しないなら false が返る。
	 * @author okayu
	 */
	static class SaveDataExists implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.INTEGER))
				{
					if(Util.saveDataExistence((int)value.intValue))
						return MokaValue.TRUE;
				}
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * セーブデータのテキストを取得<br>
	 * 第一引数にセーブ番号を指定する。ファイルが存在しないなら '未設定' が返る。
	 * @author okayu
	 */
	static class GetSaveDataText implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.INTEGER))
				{
					int num = (int)value.intValue;
					if(Util.saveDataExistence(num))
					{
						try
						{
							reValue.assign(Util.getSaveDataText(num));
							return reValue;
						} catch (JSONException e) { e.printStackTrace(); }
					}
				}
			}
			reValue.assign("未設定");
			return reValue;
		}
	}

	/**
	 * セーブデータの日付を取得<br>
	 * 第一引数にセーブ番号を指定する。ファイルが存在しないなら '未設定' が返る。
	 * @author okayu
	 */
	static class GetSaveDataDate implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.INTEGER))
				{
					int num = (int)value.intValue;
					if(Util.saveDataExistence(num))
					{
						try
						{
							reValue.assign(Util.getSaveDataDate(num));
							return reValue;
						} catch (JSONException e) { e.printStackTrace(); }
					}
				}
			}
			reValue.assign("未設定");
			return reValue;
		}
	}

	/**
	 * セーブデータを削除する<br>
	 * 第一引数にセーブ番号を指定する。正常に削除できれば true、失敗で false
	 * @author okayu
	 */
	static class DeleteSaveData implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.INTEGER))
				{
					int num = (int)value.intValue;
					if(Util.saveDataExistence(num))
					{
						Util.deleteSave(num);
						return MokaValue.TRUE;
					}
				}
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * フルスクリーンを設定<br>
	 * 第一引数が true なら有効に、false なら無効にする。
	 * @author okayu
	 */
	static class SetFullScreen implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() == 0) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.FALSE;
			MokaValue value = args.get(0);
			if(value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			{
				Util.mainView.setFullScreen(value.intValue != 0);
				return MokaValue.TRUE;
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * フルスクリーンを取得
	 * @author okayu
	 */
	static class GetFullScreen implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.fullscreen) reValue.assignBoolean(1);
			else reValue.assignBoolean(0);
			return reValue;
		}
	}

	/**
	 * 簡易演出を設定<br>
	 * 第一引数が true なら有効に、false なら無効にする。
	 * @author okayu
	 */
	static class SetEasyDirection implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() == 0) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.FALSE;
			MokaValue value = args.get(0);
			if(value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			{
				Util.mainView.easyDirection = value.intValue != 0;
				return MokaValue.TRUE;
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * 簡易演出を取得
	 * @author okayu
	 */
	static class GetEasyDirection implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null) return MokaValue.FALSE;
			else if(Util.mainView.easyDirection) reValue.assignBoolean(1);
			else reValue.assignBoolean(0);
			return reValue;
		}
	}


	// -------------------------------------------------------------------------
	//		テキスト関連
	// -------------------------------------------------------------------------


	/**
	 * ユーザー指定文字表示速度の設定<br>
	 * ユーザー指定文字表示速度を、第一引数の整数（ミリ秒）に設定する。
	 * @author okayu
	 */
	static class SetUserTextSpeed implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0 && Util.mainView != null)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.INTEGER))
				{
					if(Util.mainView != null)
					{
						Util.mainView.setUserTextInterval((int)value.intValue);
						return MokaValue.TRUE;
					}
				}
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * ユーザー設定文字表示速度の取得
	 * @author okayu
	 */
	static class GetUserTextSpeed implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null) reValue.assign(0);
			else reValue.assign(Util.mainView.getUserTextInterval());
			return reValue;
		}
	}

	/**
	 * オートモードの速度を設定
	 * @author okayu
	 */
	static class SetAutoSpeed implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() == 0) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.MINUSONE;
			MokaValue value = args.get(0);
			if(value.cast(MOKA_VALUE_TYPE.INTEGER))
			{
				Util.mainView.autoModeInterval = (int)value.intValue;
				return MokaValue.TRUE;
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * オートモードの速度を取得
	 * @author okayu
	 */
	static class GetAutoSpeed implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null) return MokaValue.MINUSONE;
			reValue.assign(Util.mainView.autoModeInterval);
			return reValue;
		}
	}

	/**
	 * 全文スキップの設定<br>
	 * 第一引数が true なら全文スキップ、false なら既読スキップ
	 * @author okayu
	 */
	static class SetSkipAll implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() == 0) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.FALSE;
			MokaValue value = args.get(0);
			if(value.cast(MOKA_VALUE_TYPE.BOOLEAN))
			{
				if(value.intValue != 0)
					Util.mainView.skipModeType = 2;
				else
					Util.mainView.skipModeType = 1;
				return MokaValue.TRUE;
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * 全文スキップの取得<br>
	 * @author okayu
	 */
	static class GetSkipAll implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null) return MokaValue.MINUSONE;
			else if(Util.mainView.skipModeType == 2) reValue.assignBoolean(1);
			else reValue.assignBoolean(0);
			return reValue;
		}
	}


	// -------------------------------------------------------------------------
	//		音声関連
	// -------------------------------------------------------------------------


	/**
	 * BGM の音量取得<br>
	 * @author okayu
	 */
	static class GetBgmVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null) reValue.assign(100);
			else reValue.assign(Util.mainView.getBgmVol());
			return reValue;
		}
	}

	/**
	 * BGM の大域音量取得<br>
	 * @author okayu
	 */
	static class GetBgmGVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null) reValue.assign(100);
			else reValue.assign(Util.mainView.getBgmGlobalVol());
			return reValue;
		}
	}

	/**
	 * BGM の音量を設定<br>
	 * @author okayu
	 */
	static class SetBgmVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0 && Util.mainView != null)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.REAL))
				{
					Util.mainView.setBgmVol((float)value.realValue);
					return MokaValue.TRUE;
				}
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * BGM の大域音量を設定<br>
	 * @author okayu
	 */
	static class SetBgmGVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() != 0 && Util.mainView != null)
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.REAL))
				{
					Util.mainView.setBgmGlobalVol((float)value.realValue);
					return MokaValue.TRUE;
				}
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * 効果音の音量取得<br>
	 * 第一引数の整数を効果音バッファ番号として、バッファの音量を返す
	 * @author okayu
	 */
	static class GetSeVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null || args.size() == 0)
				reValue.assign(-1);
			else
			{
				MokaValue buff = args.get(0);
				if(buff.cast(MOKA_VALUE_TYPE.INTEGER) && buff.intValue < Util.mainView.seBuffNum)
					reValue.assign( Util.mainView.seBuff[(int)buff.intValue].getVolume() );
				else
					reValue.assign(-1);
			}
			return reValue;
		}
	}

	/**
	 * 効果音の大域音量取得<br>
	 * 第一引数の整数を効果音バッファ番号として、バッファの大域音量を返す
	 * @author okayu
	 */
	static class GetSeGVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(Util.mainView == null || args.size() == 0)
				reValue.assign(-1);
			else
			{
				MokaValue buff = args.get(0);
				if(buff.cast(MOKA_VALUE_TYPE.INTEGER) && buff.intValue < Util.mainView.seBuffNum)
					reValue.assign( Util.mainView.seBuff[(int)buff.intValue].getGlobalVolume() );
				else
					reValue.assign(-1);
			}
			return reValue;
		}
	}

	/**
	 * 効果音の音量設定<br>
	 * 第一引数をバッファ番号、第二引数を音量とする
	 * @author okayu
	 */
	static class SetSeVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() < 2) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.FALSE;
			MokaValue num = args.get(0);
			MokaValue vol = args.get(1);
			if(num.cast(MOKA_VALUE_TYPE.INTEGER) && vol.cast(MOKA_VALUE_TYPE.REAL))
			{
				Util.mainView.setSeVol((int)num.intValue, (float)vol.realValue);
				return MokaValue.TRUE;
			}
			return MokaValue.FALSE;
		}
	}

	/**
	 * 効果音の大域音量設定
	 * 第一引数をバッファ番号、第二引数を音量とする
	 * @author okayu
	 */
	static class SetSeGVolume implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() < 2) return MokaValue.FALSE;
			if(Util.mainView == null) return MokaValue.FALSE;
			MokaValue num = args.get(0);
			MokaValue vol = args.get(1);
			if(num.cast(MOKA_VALUE_TYPE.INTEGER) && vol.cast(MOKA_VALUE_TYPE.REAL))
			{
				Util.mainView.setSeGlobalVol((int)num.intValue, (float)vol.realValue);
				return MokaValue.TRUE;
			}
			return MokaValue.FALSE;
		}
	}


	// -------------------------------------------------------------------------
	//		その他
	// -------------------------------------------------------------------------


	/**
	 * 数字を全角文字にした文字列を返す。
	 * @author okayu
	 */
	static class Get2ByteNum implements MokaFunction.BuiltInFunction
	{
		@Override
		public MokaValue function(ArrayList<MokaValue> args)
		{
			if(args.size() == 0)
				reValue.assign("");
			else
			{
				MokaValue value = args.get(0);
				if(value.cast(MOKA_VALUE_TYPE.STRING))
				{
					reValue.assign(value.stringValue
						.replace('0', '０')
						.replace('1', '１')
						.replace('2', '２')
						.replace('3', '３')
						.replace('4', '４')
						.replace('5', '５')
						.replace('6', '６')
						.replace('7', '７')
						.replace('8', '８')
						.replace('9', '９'));
				}
				else
					reValue.assign("");
			}
			return reValue;
		}
	}




//	static class Func implements MokaFunction.BuiltInFunction
//	{
//		@Override
//		public MokaValue function(ArrayList<MokaValue> args)
//		{
//			return null;
//		}
//	}

}