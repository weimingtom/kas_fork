package net.studiomikan.kas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * ネットワークリソースの削除画面
 * @author okayu
 */
public class DeleteResActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.deleteres);

		Button delete = (Button)findViewById(R.id.button_delete);
		Button cancel = (Button)findViewById(R.id.button_cancel);

		delete.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onDelete();
			}
		});

		cancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onCancel();
			}
		});
	}

	private void onDelete()
	{
		Util.closeGame();
		ResourceManager.deleteNetResource();
		AlertDialog.Builder ad;
		ad = new AlertDialog.Builder(this);
		ad.setTitle("ゲームデータ削除");
		ad.setMessage("削除しました。ゲームを終了します。");
		ad.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				finish();
			}
		});
		ad.create();
		ad.show();
	}

	private void onCancel()
	{
		finish();
	}
}
