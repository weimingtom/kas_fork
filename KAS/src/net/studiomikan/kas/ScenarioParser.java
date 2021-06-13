package net.studiomikan.kas;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

/**
 * シナリオのデータ単位<br>
 * @author okayu
 */
class ScenarioData
{
	/** 文字列 */
	public String str;
	/** 行 */
	public int line;

	/**
	 * シナリオのデータ単位
	 * @param line 行
	 * @param str 文字列
	 */
	public ScenarioData(int line, String str)
	{
		this.line = line;
		this.str = str;
	}
}

/**
 * シナリオファイルを読み出してパースする<br>
 * 「行」とは、シナリオファイルにおける行を言います。<br>
 * 「point」「位置」とは、種類別に分割した後のシナリオの番号を言います。
 * @author okayu
 */
class ScenarioParser
{
	/** シナリオファイルの内容 */
	private ArrayList<ScenarioData> filedata = null;
	/** 現在読込中のポイント */
	private int readpoint = 0;

	/** 属性受け渡し用マップ */
	public HashMap<String, String> map = null;

	/** 現在開いているシナリオファイル名 */
	public String fileName = "";
	/** 現在最新のラベル名 */
	public String labelName = "";
	/** 最新のラベルの行番号 */
	public int labelLineNum = 0;
	/** セーブ可能ラベルのテキスト */
	public String labelText = "スタート";
	/** 今読み込んでいるシナリオの、行番号 */
	private int lineNum = 0;
	/** 今読み込んでいるシナリオ */
	public String scenarioBuff = null;

	/** テキストのパース中なら true */
	public boolean parseTextFlag = false;
	/** テキストバッファ */
	private String textBuff = "";
	/** テキストバッファの現在の読み込み位置 */
	private int textBuffIndex = 0;

	/** 外部バッファからのパース中 */
	private boolean parseFromBuff = false;
	/** 外部バッファ */
	private String parseBuff;

	// パーサで用いる状態
	/** パース結果 テキスト */
	public static final int PARCE_RESULT_TEXT 		= 0;
	/** パース結果 タグ */
	public static final int PARCE_RESULT_TAG 		= 1;
	/** パース結果 リターン */
	public static final int PARCE_RESULT_RETURN 	= 2;
	/** パース結果 空行 */
	public static final int PARCE_RESULT_VOIDLINE 	= 3;
	/** パース結果 名前 */
	public static final int PARCE_RESULT_NAME		= 4;
	/** パース結果 ラベル */
	public static final int PARCE_RESULT_LABEL		= 5;
	/** パース結果 ファイル終端 */
	public static final int PARCE_RESULT_EOF 		= 6;
	/** パース結果 */
	public int result = PARCE_RESULT_EOF;

	/** ノーウェイトフラグ */
	public boolean nowait = false;

	/** コンテキスト */
	private Context context;

	/**
	 * コンストラクタ
	 * @param context
	 */
	public ScenarioParser(Context context)
	{
		this.context = context;

		filedata = new ArrayList<ScenarioData>();

		// map作成
		map = new HashMap<String, String>();

		// first.ksを読み込む
		loadFile("first.ks");
		initState();
	}

	/**
	 * 初期化
	 */
	public void initState()
	{
		labelLineNum = 0;
		lineNum = 0;
		labelName = "";

		textBuff = "";
		textBuffIndex = 0;
		parseTextFlag = false;
	}

	/**
	 * エラー時の動作
	 * @param message エラーメッセージ
	 */
	private void error(String message)
	{
		Util.error("line:" + readpoint + "\n" + message);
	}

	/**
	 * ファイルを開く
	 * @param filename ファイル名
	 * @return 読み込み成功で true
	 */
	public boolean loadFile(String filename)
	{
		if(filename == null || filename.length() == 0)
			return false;

		long start, end;

		start = System.currentTimeMillis();

		// 開く
		BufferedReader br = null;
		try
		{
			br = ResourceManager.loadScenario(filename, context);
			this.fileName = filename;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			Util.log(filename + "の読み込みに失敗");
			error(filename + "の読み込みに失敗");
			return false;
		}

		if(br == null)
		{
			Util.log(filename + "の読み込みができていなかった");
			error(filename + "の読み込みに失敗");
			return false;
		}

		// 読み込み
		filedata.clear();
		readpoint = 0;
		String line;
		int lineNum = 0;
		boolean inScript = false;
		boolean first = true;

		try
		{
			while((line = br.readLine()) != null)
			{
				if(first)	// BOM の処理
				{
					first = false;
					if(line.length() != 0 && line.charAt(0) == 0xfeff)
						line = line.substring(1);
				}
				lineNum++;
				line = myTrim(line);
				if(line.length() != 0)
				{
					if(inScript)	// iscript 中はそのまま出力
					{
						if(line.equals("[endscript]") || line.equals("@endscript"))
							inScript = false;
						filedata.add(new ScenarioData(lineNum, line));
					}
					else
					{
						if(line.equals("[iscript]") || line.equals("@iscript"))
							inScript = true;
						fileLoad_parseLine(line, lineNum);
					}
				}
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			try {
				br.close();
			} catch (IOException e1) { e1.printStackTrace(); }
			Util.closeGame();
		}

		if(inScript)
			Util.error("iscript が閉じられていません。");

//		System.out.println("---------------------------------------");
//		int size = filedata.size();
//		ScenarioData sd;
//		for(int i = 0; i < size; i++)
//		{
//			sd = filedata.get(i);
//			System.out.println(sd.line + " | " + sd.str);
//		}
//		System.out.println("---------------------------------------");

		initState(); // 初期化

		end = System.currentTimeMillis();
		Util.log("シナリオ読み込み " + filename + " : " + (end-start));

		return true;
	}

	/**
	 * 行を種類別（テキスト、タグ、コメント、ラベル）に分別して登録する。
	 * @param line 行のテキスト。前後の空白は取り除いておくべき
	 * @param lineNum 行番号
	 * @return
	 */
	private final boolean fileLoad_parseLine(String line, int lineNum)
	{
		if(line == null || line.length() == 0)
			return true;

		// 行単位の処理
		switch(line.charAt(0))
		{
		case ';':	// コメント
			filedata.add(new ScenarioData(lineNum, line));
			return true;
		case '@':	// コマンド行
			filedata.add(new ScenarioData(lineNum, line));
			return true;
		case '*':	// ラベル
			filedata.add(new ScenarioData(lineNum, line));
			return true;
		}

		// テキスト、タグ
		int len = line.length();
		boolean flag = true;
		char c;
		StringBuilder sb = new StringBuilder();
		boolean inString = false;	// 文字列内部
		boolean ignore = false;		// エスケープシーケンス無視用
		boolean inTag = false;		// タグ内部
		char stringStart = '\"';

		for(int i = 0; i < len && flag; i++)
		{
			c = line.charAt(i);
			if(inTag)
			{
				if(inString)	// 文字列中
				{
					if(ignore)
						ignore = false;
					else
					{
						switch(c)
						{
						case '\'': case '\"':	// 文字列の終了
							if(c == stringStart)
								inString = false;
							break;
						case '\\':	// エスケープシーケンス
							ignore = true;
							break;
						}
					}
				}
				else
				{
					switch(c)
					{
					case ']':	// 終了
						sb.append(']');
						filedata.add(new ScenarioData(lineNum, sb.toString()));
						inTag = false;
						sb = new StringBuilder();
						continue;
					case '\'': case '\"':	// 文字列の開始
						stringStart = c;
						inString = true;
						break;
					}
				}
			}
			else	// 通常の文字
			{
				switch(c)
				{
				case '[':	// タグの開始
					inTag = true;
					if(sb.length() != 0)
					{
						filedata.add(new ScenarioData(lineNum, sb.toString()));
						sb = new StringBuilder();
					}
					break;
				default:
				}
			}
			sb.append(c);
		}

		if(sb.length() != 0)
			filedata.add(new ScenarioData(lineNum, sb.toString()));

		return true;
	}

	/**
	 * シナリオを返す
	 * @return 行
	 */
	private String getScenario()
	{
		if(readpoint < filedata.size())
		{
			ScenarioData data = filedata.get(readpoint++);
			lineNum = data.line;
			return data.str;
		}
		else
			return null;
	}

	/**
	 * シナリオを取得する。外部からの参照用。
	 * @return 行
	 */
	public String getText()
	{
		if(readpoint-1 > 0 && readpoint-1 < filedata.size())
			return getLineNumber() + "|" + filedata.get(readpoint-1).str;
		else
			return null;
	}

	/**
	 * 指定したラベルまで飛ぶ
	 * @param label ジャンプ先ラベル
	 * @return 成功すれば true 失敗で false
	 */
	public boolean searchLabel(String label)
	{
		if(label == null)
			return false;
		if(label.length() == 0)
			return true;

		int pos = -1;
		String buff = "";
		String name, text = "";
		while(true)
		{
			buff = getScenario();

			if(buff == null) // ファイル終端
			{
				// 見つからなかったのでファイルを再読込してfalseを返す
				loadFile(fileName);
				Util.log("not found label:" + label);
				return false;
			}

			pos = buff.indexOf('|');
			if(pos == -1)
				name = buff;
			else
			{
				name = buff.substring(0, pos);
				text = buff.substring(pos + 1);
			}

			if(name.equals(label)) // 見つかった
			{
				if(text != null && text.length() != 0) // セーブ名があった
					labelText = text;
				Util.log("jump to label:" + label);
				labelName = name;
				return true;
			}
		}
	}

	/**
	 * 指定された位置まで飛ぶ
	 * @param jumpPoint 位置
	 * @return 成功すれば true 失敗で false
	 */
	public boolean jumpPoint(int point)
	{
		if(0 < point && point <= filedata.size())
		{
			readpoint = point - 1;
			Util.log(fileName + " " + point + "にジャンプ:" + filedata.get(readpoint));
			return true;
		}
		else
		{
			Util.log("指定番号が大きすぎます:" + point + "/" + filedata.size());
			error("ジャンプに失敗。指定番号が不正です。 file:" + fileName + " line:" + point);
			return false;
		}
	}

	/**
	 * ファイル名を返す
	 * @return ファイル名
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * 現在のシナリオの行番号を返す
	 * @return 行番号
	 */
	public int getLineNumber()
	{
		return this.lineNum;
	}

	/**
	 * 現在の読み込み位置を返す
	 * @return 読み込み位置
	 */
	public int getPoint()
	{
		return this.readpoint;
	}

	/**
	 * 現在最新のラベルを返す
	 * @return ラベル名
	 */
	public String getLabelName()
	{
		return labelName;
	}

	/**
	 * 現在最新のラベルテキストを返す
	 * @return ラベルテキスト
	 */
	public String getLabelText()
	{
		return labelText;
	}

	/**
	 * バッファに貯めているテキストをパース
	 * @return パース結果
	 */
	private String parceTextBuff()
	{
		int i = textBuffIndex;
		textBuffIndex++;
		if(i >= textBuff.length())	// 最後の文字 バッファから返すのを終了
		{
			parseTextFlag = false;
			result = PARCE_RESULT_RETURN;
			return "RETURN";
		}
		else	// バッファから返す
		{
			if(nowait) // ノーウェイトだったら一気に返す
			{
				parseTextFlag = false;
				result = PARCE_RESULT_TEXT;
				return textBuff.substring(i);
			}
			else
			{
				result = PARCE_RESULT_TEXT;
				return String.valueOf( textBuff.charAt(i) );
			}
		}
	}

	/**
	 * 外部から受け取った文字列をパースする　マクロ用
	 * @param str 文字列
	 * @return パース結果
	 */
	public String parseFromBuff(String str)
	{
		parseBuff = str;
		parseFromBuff = true;
		return parse();
	}

	/**
	 * パースする
	 * @return パース結果。タグの場合はタグ名
	 */
	public String parse()
	{
		if(parseTextFlag) // テキストを返しているとき
		{
			return parceTextBuff();
		}
		else	// テキストのバッファがない
		{
			String buff;
			char c;
			while(true)// コメント文の復帰用while
			{
				buff = "";
				if(parseFromBuff)
				{
					buff = parseBuff;
					parseFromBuff = false;
				}
				else
					buff = getScenario();

				if(buff == null)	// ファイル終端
				{
					result = PARCE_RESULT_EOF;
					return "s";
				}

				if(buff.length() == 0)	// 空行
				{
					result = PARCE_RESULT_VOIDLINE;
					return "";
				}

				scenarioBuff = buff;		// 納めとく

				c = buff.charAt(0); // 一文字目取り出し
				if(c == '[' || c == '@') // タグ
				{
					result = PARCE_RESULT_TAG;
					String tagName = parceTag2(buff);
					// iscript スクリプト部の開始
					if(tagName.equals("iscript"))
					{
						int start = lineNum;
						StringBuilder sb = new StringBuilder();
						while(true)
						{
							buff = getScenario();
							if(buff == null) break;	// ファイル終端なら終了
							if(buff.length() == 0) continue;
							if(buff.equals("[endscript]") || buff.equals("@endscript"))
								break;
							sb.append(buff);
							sb.append('\n');
						}
						int end = lineNum;
						// iscript タグとして返す
						String script = sb.toString();
						map.put("script", script);
						map.put("line", start + "~" + end);
						return "iscript";
					}
					// 他のタグ
					return tagName;
				}
				else if(c == ';')	// コメント
				{
					continue;
				}
				else if(c == '*')	// ラベル
				{
					result = PARCE_RESULT_LABEL;
					labelLineNum = lineNum;
					return parceLabel(buff);
				}
				else // 通常の文字
				{
					if(nowait)	// ノーウェイトなら全部一気に渡す
					{
						result = PARCE_RESULT_TEXT;
						return buff;
					}
					if(buff.length() == 1) // 一文字ならそのまま
					{
						result = PARCE_RESULT_TEXT;
						return buff;
					}
					else 	// バッファに納めて、移行はバッファから返す
					{
						parseTextFlag = true;
						textBuffIndex = 1;
						textBuff = buff;
						result = PARCE_RESULT_TEXT;
						return String.valueOf( buff.charAt(0) );	// とりあえず一文字目は返す
					}
				}
				//break;
			}
		}
	}

	/**
	 * 文字列の前後のタブ、空白を削除
	 * @param str 文字列
	 * @return
	 */
	private String myTrim(String str)
	{
		return str.replaceAll("^[\\t\\n\\r]*", "").replaceAll("[\\t\\n\\r]*$", "");
	}

	/**
	 * ラベルをパースして返す
	 * @param str ラベル文字列
	 * @return ラベル名
	 */
	private String parceLabel(String str)
	{
		int pos = -1;
		String name, text = "";

		pos = str.indexOf('|');
		if(pos == -1)
			name = str;
		else
		{
			name = str.substring(0, pos);
			text = str.substring(pos + 1);
		}

		map.clear();

		labelName = name;
		map.put("labelName", labelName);
		if(text != null && text.length() != 0)// セーブ可能ラベルなら納める
		{
			labelText = text;
			map.put("labelText", labelText);
		}

		return labelName;
	}

	/**
	 * タグをパースし、mapに詰め込む
	 * @param str タグ文字列
	 * @return タグ名
	 */
	private String parceTag2(String str)
	{
		// @ や [] を削除
		if(str.endsWith("]")) // [hoge]
			str = str.substring(1, str.length()-1);
		else // @hoge
			str = str.substring(1, str.length());

		map.clear();	// mapの初期化

		int len = str.length();
		char c;
		String value = "";
		String operand = "";
		String tagName = "";
		int state = 0;
		boolean flag = true;
		boolean ignore = false;

		for(int i = 0; flag; i++)
		{
			if(i < len)
				c = str.charAt(i);
			else
			{
				c = ' ';	// 最後
				flag = false;
			}
			switch(state)
			{
			case 0:	// 初期値
				if(c == ' ')
					state = 1;
				else
					tagName += c;
				break;

			case 1:	// 属性読み込み
				switch(c)
				{
				case '=':
					state = 2;
					break;
				case ' ':	// [backlay]のように属性が無いときはここにたどり着く
					break;
				default:
					operand += c;
					break;
				}
				break;

			case 2:	// 値読み込み
				switch(c)
				{
				case ' ':
					// 登録
					map.put(operand, value);
					operand = "";
					value = "";
					state = 1;
					break;
				case '\'':
					state = 3;
					break;
				case '\"':
					state = 4;
					break;
				case '\\':
					value += c;
					state = 5;
					break;
				default:
					value += c;
					break;
				}
				break;

			case 3:	// ' を読み込んだ時
				if(ignore)
				{
					value += c;
					ignore = false;
				}
				else
				{
					switch(c)
					{
					case '\\': ignore = true; break;
					case '\'': state = 2; break;
					default: value += c; break;
					}
				}
				break;

			case 4:	// " を読み込んだ時
				if(ignore)
				{
					value += c;
					ignore = false;
				}
				else
				{
					switch(c)
					{
					case '\\': value += c; ignore = true; break;
					case '\"': state = 2; break;
					default: value += c; break;
					}
				}
				break;

			case 5:	// \ を読み込んだ時
				value += c;
				state = 2;
				break;

			}
		}

		map.put("__kas__tagName__", tagName);	// 一応タグの名前も格納しとく
		return tagName;	// タグ名を返す
	}

}
