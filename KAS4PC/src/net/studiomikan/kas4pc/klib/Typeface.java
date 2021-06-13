package net.studiomikan.kas4pc.klib;

import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;

import net.studiomikan.kas.ResourceManager;
import net.studiomikan.kas.Util;
import net.studiomikan.kas4pc.gui.KGUIUtil;

public class Typeface
{
	public static final Typeface DEFAULT = new Typeface();
	public static final Typeface DEFAULT_BOLD = new Typeface();
	public static final Typeface MONOSPACE = new Typeface();
	public static final int NORMAL = 0;
	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int BOLD_ITALIC = 3;

	public int opt = NORMAL;

	public static Font deffont = null;
	public Font font = null;

	public Typeface()
	{
		if(deffont == null) deffont = KGUIUtil.getGUIFont(Util.mainFrame);
		font = deffont;
	}

	public Typeface(int opt)
	{
		if(deffont == null) deffont = KGUIUtil.getGUIFont(Util.mainFrame);
		this.opt = opt;
		font = deffont;
	}

	public static Typeface create(Typeface face, int opt)
	{
		Typeface t = new Typeface(opt);
		t.font = face.font;
		return t;
	}

	public static Font loadFont(String filename)
	{
		FileInputStream is = ResourceManager.loadStream(filename);;
		Font font = null;
		if(is == null)
			font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		else
		{
			try
			{
				font = Font.createFont(Font.TRUETYPE_FONT, is);
				font = font.deriveFont(12);
				is.close();
			}
			catch (Exception e)
			{
				try { is.close(); }
				catch (IOException e1) { e1.printStackTrace(); }
				font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			}
		}
		return font;
	}

	public static Typeface createFromAsset(Object assets, String filename)
	{
		Typeface face = new Typeface();
		face.font = Typeface.loadFont(filename);
		return face;
	}



}
