package net.studiomikan.kas4pc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import net.studiomikan.kas.Util;

import org.json.JSONException;

/**
 * セーブ＆ロード画面
 * @author okayu
 */
public class SaveFrame extends ModalFrame
{
	private static final long serialVersionUID = 4326819345300905223L;

	private String mode = "save";
	private SaveData[] data;

	public SaveFrame()
	{
		super();

		JPanel panel = new JPanel();
		GridLayout layout = new GridLayout(Util.saveNumMax, 1);
		panel.setLayout(layout);

		String date, text;
		data = new SaveData[Util.saveNumMax];
		for(int i = 0; i < data.length; i++)
		{
			try
			{
				date = Util.getSaveDataDate(i);
				text = Util.getSaveDataText(i);
			}
			catch (JSONException e)
			{
				date = "--/--/-- --:--:--";
				text = "";
			}
			panel.add(data[i] = new SaveData(i, date, text));
		}

		JScrollPane jsp = new JScrollPane(panel);
		jsp.setPreferredSize(new Dimension(500, 500));
		jsp.setHorizontalScrollBar(null);

		getContentPane().add(jsp);
		pack();
	}

	/**
	 * セーブモードとロードモードの入れ替え
	 * @param mode
	 */
	public void setMode(String mode)
	{
		this.mode = mode;
		if(mode.equals("save"))
			setTitle("【セーブ】" + Util.gameTitle);
		else
			setTitle("【ロード】" + Util.gameTitle);
	}

	/**
	 * クリック時の動作
	 * @param d
	 */
	private void onItemClicked(SaveData d)
	{
		for(int i = 0; i < data.length; i++)
		{
			if(data[i] == d)
			{
				if(mode.equals("save"))
				{
					// セーブ
					Util.log("save pos=" + i);
					Util.mainView.saveGame(i);
					data[i].date.setText("No." + i + " " + Util.latestSaveDate);
					data[i].text.setText(Util.latestSaveText);
				}
				else
				{
					// ロード
					Util.log("load pos=" + i);
					Util.mainView.loadGame(i);
					this.setVisible(false);
				}
			}
		}
	}

	/**
	 * データラベル
	 * @author okayu
	 */
	public class SaveData extends JPanel implements MouseListener
	{
		private static final long serialVersionUID = 1L;
		public JLabel date = new JLabel();
		public JLabel text = new JLabel("nodata");
		private Color defcolor;

		public SaveData(int num, String date, String text)
		{
			setFocusable(true);
			addMouseListener(this);
			GridLayout layout = new GridLayout(2, 1);
			setLayout(layout);
			this.date.setPreferredSize(new Dimension(500, 20));
			this.date.setText("No." + num + " " + date);
			this.text.setPreferredSize(new Dimension(500, 20));
			this.text.setText(text);
			add(this.date);
			add(this.text);
			defcolor = getBackground();
			setBorder(new UnderBorder());
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			onItemClicked(this);
		}
		@Override
		public void mouseEntered(MouseEvent e)
		{
		}
		@Override
		public void mouseExited(MouseEvent e)
		{
		}
		@Override
		public void mousePressed(MouseEvent e)
		{
			setBackground(new Color(0xbbddff));
		}
		@Override
		public void mouseReleased(MouseEvent e)
		{
			setBackground(defcolor);
		}
	}


	/**
	 * 下線ボーダー
	 * @author okayu
	 */
	private static class UnderBorder implements Border
	{
		private Insets insets = new Insets(0, 0, 2, 0);

		@Override
		public Insets getBorderInsets(Component arg0)
		{
			return insets;
		}

		@Override
		public boolean isBorderOpaque()
		{
			return true;
		}

		@Override
		public void paintBorder(Component c, Graphics g,int x, int y, int width, int height)
		{
			Insets insets=getBorderInsets(c);
			Color colorbuff = g.getColor();
			{
				int x1, x2, y1, y2;
				x1 = x;
				x2 = x + width;
				y1 = y2 = y + height - insets.bottom/2;
				g.setColor(Color.GRAY);
				g.drawLine(x1, y1, x2, y2);
			}
			g.setColor(colorbuff);
		}

	}

}
