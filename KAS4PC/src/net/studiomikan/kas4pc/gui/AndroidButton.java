package net.studiomikan.kas4pc.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.studiomikan.kas.Util;


/**
 * アンドロイドの戻るキーとかのボタンっぽいもの
 * @author okayu
 */
public class AndroidButton extends JPanel
{
	private static final long serialVersionUID = 1L;
	/** メニューボタン */
	public JButton menu;
	/** 戻るボタン */
	public JButton back;

	/**
	 * コンストラクタ
	 */
	public AndroidButton()
	{
		menu = new JButton("　Menu　");
		menu.addActionListener(new AndroidMenuButton());
		this.add(menu, BorderLayout.WEST);

		back = new JButton("　Back　");
		back.addActionListener(new AndroidBackButton());
		this.add(back, BorderLayout.EAST);
	}

}

/**
 * Menu ボタンの動作
 * @author okayu
 */
class AndroidMenuButton implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(Util.builtInOptionMenu)
		{
			if(MainActivity.menuVisible)
			{
				if(Util.menuFrame.isVisible() && Util.mainView.inStable())
					Util.menuFrame.setVisible(false);
				else
					Util.menuFrame.setVisible(true);
			}
		}
		else
		{
    		if(Util.mainView != null)
    			Util.mainView.onClickMenu();
		}
	}
}

/**
 * Back ボタンの動作
 * @author okayu
 *
 */
class AndroidBackButton implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Util.closeGameWithAsk(Util.ask_exit);
	}
}
