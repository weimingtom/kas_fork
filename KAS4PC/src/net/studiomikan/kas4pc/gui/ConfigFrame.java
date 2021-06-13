package net.studiomikan.kas4pc.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.studiomikan.kas.MainSurfaceView;
import net.studiomikan.kas.Util;


/**
 * 設定画面
 * @author okayu
 */
public class ConfigFrame extends ModalFrame
{
	private static final long serialVersionUID = -3564618391649750501L;
	public static final int WIDTH = 700;
	public static final int HEIGHT = 300;
	public TextSpeed textSpeed;
	public MySlider autoSpeed;
	public MySlider bgmVolume;
	public MySlider seVolume;
	public JCheckBox skip;
	public JCheckBox direction;

	public ConfigFrame()
	{
		super();
		// メインのパネル生成
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		GridLayout layout = new GridLayout();
		layout.setColumns(1);
		layout.setRows(5);
		p.setLayout(layout);


		// 文字速度
		p.add(textSpeed = new TextSpeed());
		// オート速度
		p.add(autoSpeed = new MySlider("オートモード速度", 2000, 100));
		// BGM
		p.add(bgmVolume = new MySlider("BGM 音量", 100, 10));
		// SE
		p.add(seVolume = new MySlider("SE 音量", 100, 10));

		JPanel box = new JPanel();
		FlowLayout boxlayout = new FlowLayout(FlowLayout.LEFT, 10, 15);
		box.setLayout(boxlayout);
		skip = new JCheckBox("全文スキップ");
		direction = new JCheckBox("簡易演出");
		box.add(skip);
		box.add(direction);
		p.add(box);


		// サイズ設定
		Container contentPane = getContentPane();
		contentPane.add(p);
		pack();
		setResizable(false);//サイズ変更不可
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		if(visible)
		{
			setTitle("【設定】" + Util.gameTitle);
			setValue();
		}
		else
			apply();
	}

	/**
	 * 値を設定
	 */
	public void setValue()
	{
		MainSurfaceView mainView = Util.mainView;
		if(mainView != null)
		{
			setValue(
					mainView.getUserTextInterval(),
					mainView.autoModeInterval,
					mainView.skipModeType,
					(int)mainView.getBgmGlobalVol(),
					(int)mainView.getSeGlobalVol(0),
					Util.fullscreen,
					mainView.layerImageScale,
					mainView.easyDirection);
		}
	}

	/**
	 * 値を設定
	 * @param textInterval
	 * @param autoModeSpeed
	 * @param skipModeType
	 * @param bgmVol
	 * @param seVol
	 * @param fullscreen
	 * @param graphicScale
	 * @param direction
	 */
	public void setValue(
			int textInterval,
			int autoModeSpeed,
			int skipModeType,
			int bgmVol,
			int seVol,
			boolean fullscreen,
			float graphicScale,
			boolean direction)
	{
		this.textSpeed.setSpeed(textInterval);
		this.autoSpeed.set(autoModeSpeed);
		this.bgmVolume.set(bgmVol);
		this.seVolume.set(seVol);
		if(skipModeType == 2)
			this.skip.setSelected(true);
		else
			this.skip.setSelected(false);
		if(direction)
			this.direction.setSelected(true);
		else
			this.direction.setSelected(false);
	}

	/**
	 * 値を反映
	 */
	public void apply()
	{
		if(Util.mainView == null) return;

		MainSurfaceView main = Util.mainView;

		// 文字表示速度
		main.setUserTextInterval(textSpeed.getSpeed());
		// オート
		main.autoModeInterval = autoSpeed.get();
		// BGM
		main.setBgmGlobalVol(bgmVolume.get());
		// SE
		main.setAllSeGlobalVol(seVolume.get());
		// 全文スキップ
		if(skip.isSelected())
			main.skipModeType = 2;
		else
			main.skipModeType = 1;
		// 簡易演出
		main.easyDirection = direction.isSelected();
	}


	/**
	 * 文字表示速度
	 * @author okayu
	 */
	public static class TextSpeed extends JPanel
	{
		private static final long serialVersionUID = 1603270834370540624L;
		public JRadioButton[] button = new JRadioButton[5];
		public ButtonGroup group = new ButtonGroup();

		public TextSpeed()
		{
			setBorder(new TitledBorder(new EtchedBorder(), "文字表示速度"));

			GridLayout layout = new GridLayout();
			setLayout(layout);

			for(int i = 0; i < button.length; i++)
				button[i] = new JRadioButton();

			button[0].setText("ノーウェイト");
			button[1].setText("高速");
			button[2].setText("普通");
			button[3].setText("低速");
			button[4].setText("超低速");

			for(int i = 0; i < button.length; i++)
			{
				group.add(button[i]);
				add(button[i]);
			}
		}

		public void setSpeed(int speed)
		{
			if(speed < 30)
				button[0].setSelected(true);
			else if(speed < 60)
				button[1].setSelected(true);
			else if(speed < 90)
				button[2].setSelected(true);
			else if(speed < 120)
				button[3].setSelected(true);
			else
				button[4].setSelected(true);
		}

		public int getSpeed()
		{
			for(int i = 0; i < button.length; i++)
			{
				if(button[i].isSelected())
					return 30 * i;
			}
			return 0;
		}
	}

	/**
	 * スライダー
	 * @author okayu
	 */
	public static class MySlider extends JPanel
	{
		private static final long serialVersionUID = 1L;
		public JSlider slider;

		public MySlider(String title, int max, int tick)
		{
			setBorder(new TitledBorder(new EtchedBorder(), title));

			BorderLayout layout = new BorderLayout();
			setLayout(layout);

			slider = new JSlider();
			slider.setPaintLabels(true);
			slider.setPaintTicks(true);
			slider.setMaximum(max);
			slider.setMajorTickSpacing(tick);
			slider.setSnapToTicks(true);

			add(slider);
		}

		public void set(int num)
		{
			slider.setValue(num);
		}

		public int get()
		{
			return slider.getValue();
		}

	}

}
