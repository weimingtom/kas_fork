package net.studiomikan.kas4pc.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import net.studiomikan.kas.Util;


/**
 * このウィンドウを表示中はメインウィンドウが操作不能になる
 * @author okayu
 */
public class ModalFrame extends JFrame implements WindowListener
{
	private static final long serialVersionUID = 1L;

	public ModalFrame()
	{
		super();
		// アイコン
		KGUIUtil.setIcon(this, "/icon.png");
		// 閉じたときの動作
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
	}

	@Override
	public void setVisible(boolean visible)
	{
		if(visible)
		{
			Util.mainFrame.setEnabled(false);
			setLocation(Util.mainFrame.getLocation());
//			Util.mainPanel.pause = true;
		}
		else
		{
			Util.mainFrame.setEnabled(true);
//			Util.mainPanel.pause = false;
		}
		super.setVisible(visible);
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		setVisible(false);
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

}
