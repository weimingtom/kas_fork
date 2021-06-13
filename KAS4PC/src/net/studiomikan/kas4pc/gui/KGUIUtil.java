package net.studiomikan.kas4pc.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;


/**
 * GUI アプリ用の色々
 * @author okayu
 */
public class KGUIUtil
{
	/** フォントのパス */
	public static String fontpath = "/MTLc3m.ttf";
	public static float fontsize = 12f;

	/**
	 * アイコンを設定
	 * @param frame
	 */
	public static final void setIcon(JFrame frame, String filename)
	{
		URL url = frame.getClass().getResource(filename);
		if(url != null)
		{
			ImageIcon icon = new ImageIcon(url);
			frame.setIconImage(icon.getImage());
		}
	}

	/**
	 * システムのフォントを変更する
	 */
	public static final void setGUIFont(JFrame frame, float fontsize)
	{
		KGUIUtil.fontsize = fontsize;
		Font font = getGUIFont(frame);
		FontUIResource fontUIResource = new FontUIResource(font);
		for(java.util.Map.Entry<?,?> entry: UIManager.getDefaults().entrySet())
		{
			if(entry.getKey().toString().toLowerCase().endsWith("font"))
				UIManager.put(entry.getKey(), fontUIResource);
		}
	}

	/**
	 * フォント取得
	 * @return フォント
	 */
	public static Font getGUIFont(JFrame frame)
	{
		InputStream is = null;
		Font font = null;
		try
		{
			is = frame.getClass().getResourceAsStream(fontpath);
			font = Font.createFont(Font.TRUETYPE_FONT, is);
			font = font.deriveFont(fontsize);
			is.close();
		}
		catch (Exception e)
		{
			try {
				if(is != null)
					is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			font = new Font(Font.SANS_SERIF, Font.PLAIN, (int)fontsize);
		}

		return font;
	}

	/**
	 * ディレクトリを削除する
	 * @param file ディレクトリ
	 */
	public static final void delete(File file)
	{
		if(file.isFile())
			file.delete();
		else
		{
			File[] files = file.listFiles();
			if(files != null)
			{
				for(int i = 0; i < files.length; i++)
					delete(files[i]);
			}
			file.delete();
		}
	}

	/**
	 * YES/NOダイアログ
	 * @param frame 親となるフレーム
	 * @param message 表示メッセージ
	 * @param title タイトル
	 * @return yes なら true , no なら false
	 */
	public static final boolean yesNoDialog(JFrame frame, String message, String title)
	{
		int re = JOptionPane.showConfirmDialog(
				frame,
				message,
				title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if(re == JOptionPane.YES_OPTION)
			return true;
		else
			return false;
	}

	/**
	 * OK ダイアログ
	 * @param frame 親となるフレーム
	 * @param message 表示メッセージ
	 * @param title タイトル
	 */
	public static final void okDialog(JFrame frame, String message, String title)
	{
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * コンポーネントの有効・無効設定
	 * @param enabled
	 */
	public static void setUIEnabled(Container container, boolean enabled)
	{
		Component[] comps = container.getComponents();
		for(int i = 0; i < comps.length; i++)
		{
			if( !(comps[i] instanceof JLabel) )
				comps[i].setEnabled(enabled);
		}
	}


	/**
	 * 下線ボーダー
	 * @author okayu
	 */
	public static class UnderBorder implements Border
	{
		private Insets insets = new Insets(0, 0, 2, 0);
		private Color color;

		public UnderBorder(Color color)
		{
			this.color = color;
		}

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
				g.setColor(color);
				g.drawLine(x1, y1, x2, y2);
			}
			g.setColor(colorbuff);
		}
	}

	/**
	 * ルートディレクトリのパスを取得する
	 * @param frame 親
	 * @param dir デフォルトのディレクトリ
	 * @return パス
	 */
	public static String getRootPath(JFrame frame, String dir)
	{
		File file = new File(dir);
		if(file.exists() && file.isDirectory())
			return file.getAbsolutePath();
		else
		{
			// 存在しなかった場合は、ダイアログを出す

			JFileChooser filechooser = new JFileChooser("./");
			filechooser.setDialogTitle("ルートディレクトリの指定");
			filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int selected = filechooser.showOpenDialog(null);
			if(selected == JFileChooser.APPROVE_OPTION)
			{
				file = filechooser.getSelectedFile();
				return file.getAbsolutePath();
			}
		}
		return null;
	}

}

