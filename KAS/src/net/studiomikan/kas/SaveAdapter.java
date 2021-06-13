package net.studiomikan.kas;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * セーブ＆ロード画面のセーブデータ情報
 * @author okayu
 */
class SaveData
{
	/** 日付 */
	public String date = "----/--/-- --:--:--";
	/** テキスト */
	public String text = "no data";
	/** 番号 */
	public int num = 0;

	/**
	 * コンストラクタ
	 * @param num 番号
	 * @param date 日付
	 * @param text テキスト
	 */
	public SaveData(int num, String date, String text)
	{
		this.num = num;
		this.date = date;
		this.text = text;
	}
}



/**
 * セーブ＆ロード画面のリストビュー用アダプタ
 * @author okayu
 *
 */
class SaveAdapter extends BaseAdapter
{
	private Context context;
	private List<SaveData> list;

	/**
	 * viewholder
	 * @author okayu
	 *
	 */
	private static class ViewHolder
	{
		TextView tv1;
		TextView tv2;
	}

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	public SaveAdapter(Context context)
	{
		super();
		this.context = context;

		list = new ArrayList<SaveData>();
		int len = Util.saveNumMax;
		SaveData data = null;
		try
		{
			for(int i = 0; i < len; i++)
			{
				data = new SaveData(i, Util.getSaveDataDate(i), Util.getSaveDataText(i));
				list.add(data);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			for(int i = 0; i < len; i++)
			{
				data = new SaveData(i, "--/--/-- --:--:--", "エラー");
				list.add(data);
			}
		}

	}

	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public Object getItem(int position)
	{
		return list.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		SaveData save = (SaveData) getItem(position);
		ViewHolder holder;

		if (convertView == null)
		{
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
//			layout.setBackgroundColor(Color.DKGRAY);
			convertView = layout;

			holder = new ViewHolder();
			holder.tv1 = new TextView(context);
			holder.tv2 = new TextView(context);
			layout.addView(holder.tv1);
			layout.addView(holder.tv2);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		holder.tv1.setText(" No." + save.num + "  " + save.date);
		holder.tv2.setText(" " + save.text);

		return convertView;
	}

	// 更新
	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
		// セーブデータ一覧を上書き
//		SaveData save;
//		for(int i = 0; i < list.size(); i++)
//		{
//			save = list.get(i);
//			Util.saveList_date[i] = save.getDate();
//			Util.saveList_text[i] = save.getText();
//			if(Util.saveList_date[i].equals("----/--/-- --:--:--"))
//				Util.saveList_save[i] = false;
//			else
//				Util.saveList_save[i] = true;
//		}
//		Util.saveSaveList();
	}


}




