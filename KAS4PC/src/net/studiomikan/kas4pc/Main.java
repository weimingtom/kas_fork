package net.studiomikan.kas4pc;

//-------------------------------------------------------------------------------
//
//  KAS4PC ( KAS for PC )
//
//  start   : 2011/10/17
//  auther  : okayu(おかゆ)
//  version : 0.4.4
//
//  This software is licensed under LGPL Version 2.1.
//
//  Copyright (c) 2011-2012 スタジオ蜜柑 All Rights Reserved.
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//  スタジオ蜜柑 : http://studiomikan.net/
//  KAS配布サイト : http://studiomikan.net/kas/
//  mail : studiomikan@gmail.com
//
//------------------------------------------------------------------------------


import java.io.IOException;

import net.studiomikan.kas.Config;
import net.studiomikan.kas.MainSurfaceView;
import net.studiomikan.kas.ResourceManager;
import net.studiomikan.kas.Util;
import net.studiomikan.kas4pc.gui.KGUIUtil;
import net.studiomikan.kas4pc.gui.MainFrame;
import net.studiomikan.kas4pc.klib.Context;



/**
 * エントリポイント
 * @author okayu
 */
public class Main
{
	/**
	 * エントリポイント
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args)
	{
		// コンテキストを生成
		Util.context = new Context();

		// Util の設定
		Config.Util_config();
		String root = null;
		if(args != null && args.length != 0 && args[0].length() != 0)
			root = args[0];
		else
			root = KGUIUtil.getRootPath(null, "./data");
		if(root == null)
		{
			KGUIUtil.okDialog(null, "終了します。" + root, "通知");
			return;
		}
		Util.rootDirectoryPath_user = root;
		Util.setRootDirctory("");
		Util.setDispSize(Util.standardDispWidth, Util.standardDispHeight);

		// リソース検索
		ResourceManager.getReady();
		Config.ResourceManager_config(Util.getMokaScript());

		// ウィンドウ生成
		MainFrame frame = new MainFrame();
		frame.setVisible(true);

		// メインのビューを生成
		MainSurfaceView mainView = new MainSurfaceView(new Context());
		Util.mainView = mainView;

		// 描画開始
		frame.initDrawPanel();
//		Thread thread = frame.newDrawThread();
//		thread.start();

		// 開始
		mainView.surfaceCreated(null);
		mainView.onStart();
		mainView.onResume();
	}
}
