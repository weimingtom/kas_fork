package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.studiomikan.moka.MOKA_VALUE_TYPE;
import net.studiomikan.moka.Moka;
import net.studiomikan.moka.MokaFunction;
import net.studiomikan.moka.MokaValue;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * MokaPlugin と KASPlugin の橋渡し
 * @author okayu
 */
public class MokaPlugin extends KASPlugin
{
	/** Moka */
	public static Moka moka = null;
	/** 組み込み関数 dicsave の実体 */
	public static MokaFunction.BuiltInFunction dicsave;
	/** 組み込み関数 dicload の実体 */
	public static MokaFunction.BuiltInFunction dicload;
	/** プラグイン名 */
	public String name;
	/** プラグインの辞書配列 */
	private MokaValue plugin;
	/** プラグインの辞書配列の実体 */
	private HashMap<String, MokaValue> pluginMap;
	/** 引数用 */
	private ArrayList<MokaValue> args = new ArrayList<MokaValue>();
	/** セーブデータ用のキー文字列 */
	private String storeKey;
	/** セーブデータ用辞書配列 */
	private MokaValue storeData;
	/** 一時利用変数 */
	private MokaValue tmpValue[] = new MokaValue[3];

	private MokaPlugin(String name, HashMap<String, MokaValue> pluginMap)
	{
		storeData = MokaValue.newDic();
		this.plugin = new MokaValue(pluginMap);
		this.pluginMap = pluginMap;
		this.name = name;
		storeKey = "MokaPlugin::" + name;
		for(int i = 0; i < tmpValue.length; i++)
			tmpValue[i] = new MokaValue();
	}

	public static MokaPlugin createMokaPlugin(HashMap<String, MokaValue> pluginMap)
	{
		MokaValue value = pluginMap.get("name");
		MokaValue tagname = pluginMap.get("tagname");

		if(value == null) return null;

		MokaPlugin mp = new MokaPlugin(value.toString(), pluginMap);

		if(tagname != null && tagname.type != MOKA_VALUE_TYPE.NULL)
			mp.addTag(tagname.stringValue());

		return mp;
	}

	@Override
	public void onStore(JSONObject data) throws JSONException
	{
		args.clear();
		args.add(plugin);
		args.add(storeData);

		MokaValue func = pluginMap.get("onStore");
//		System.out.println("onStore");
		boolean result = moka.execFunction(func, args);
//		System.out.println("onStore after " + storeData);

		if(result)
		{
			args.clear();
			args.add(storeData);
			MokaValue re = dicsave.function(args);
			if(re.type == MOKA_VALUE_TYPE.STRING)
				data.put(storeKey, re.stringValue());
		}
	}

	@Override
	public void onRestore(JSONObject data, boolean clear, boolean toback) throws JSONException
	{
		if(!data.has(storeKey)) return;

		String str = data.getString(storeKey);

		args.clear();
		args.add(new MokaValue(str));
		MokaValue m_str = dicload.function(args);
		MokaValue m_clear = new MokaValue(clear);
		MokaValue m_toback = new MokaValue(toback);

		args.clear();
		args.add(plugin);
		args.add(m_str);
		args.add(m_clear);
		args.add(m_toback);

		MokaValue func = pluginMap.get("onRestore");
		if(func != null)
			moka.execFunction(func, args);
	}

	@Override
	public void onStableStateChanged(boolean stable)
	{
		MokaValue func = pluginMap.get("onStableStateChanged");
		if(func != null)
		{
			tmpValue[0].assign(stable);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			moka.execFunction(func, args);
		}
	}

	@Override
	public void onMessageHiddenStateChanged(boolean hidden)
	{
		MokaValue func = pluginMap.get("onMessageHiddenStateChanged");
		if(func != null)
		{
			tmpValue[0].assign(hidden);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			moka.execFunction(func, args);
		}
	}

	@Override
	public void onCopyLayer(boolean toback)
	{
		MokaValue func = pluginMap.get("onCopyLayer");
		if(func != null)
		{
			tmpValue[0].assign(toback);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			moka.execFunction(func, args);
		}
	}

	@Override
	public void onAutoMode(boolean start)
	{
		MokaValue func = pluginMap.get("onAutoMode");
		if(func != null)
		{
			tmpValue[0].assign(start);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			moka.execFunction(func, args);
		}
	}

	@Override
	public void onSkipMode(boolean start)
	{
		MokaValue func = pluginMap.get("onSkipMode");
		if(func != null)
		{
			tmpValue[0].assign(start);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			moka.execFunction(func, args);
		}
	}

	@Override
	public void onExchangeForeBack()
	{
		MokaValue func = pluginMap.get("onExchangeForeBack");
		if(func != null)
		{
			args.clear();
			args.add(plugin);
			moka.execFunction(func, args);
		}
	}

	@Override
	public boolean onTouchEvent(int x, int y, int action)
	{
		MokaValue func = pluginMap.get("onTouchEvent");
		if(func != null)
		{
			tmpValue[0].assign(x);
			tmpValue[1].assign(y);
			tmpValue[2].assign(action);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			args.add(tmpValue[1]);
			args.add(tmpValue[2]);
			boolean result = moka.execFunction(func, args);
			if(result)
			{
				MokaValue re = moka.getStackTop();
				if(re != null && re.cast(MOKA_VALUE_TYPE.BOOLEAN))
					return re.intValue != 0;
			}
		}
		return false;
	}

	@Override
	public void run(HashMap<String, String> elm)
	{
		MokaValue func = pluginMap.get("run");
		if(func != null)
		{
			HashMap<String, MokaValue> map = new HashMap<String, MokaValue>();
			Iterator<Map.Entry<String, String>> it = elm.entrySet().iterator();
			Map.Entry<String, String> entry;
			while(it.hasNext())
			{
				entry = it.next();
				map.put(entry.getKey(), new MokaValue(entry.getValue()));
			}
			tmpValue[0].assign(map);
			args.clear();
			args.add(plugin);
			args.add(tmpValue[0]);
			moka.execFunction(func, args);
		}
		super.run(elm);
	}

}
