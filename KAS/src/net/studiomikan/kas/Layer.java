package net.studiomikan.kas;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * 前景レイヤ
 * @author okayu
 */
class Layer extends KASLayer
{
	/** 追加画像 */
	public static class AdditionalImage
	{
		public int dx;
		public int dy;
		public int sx;
		public int sy;
		public int width;
		public int height;
		public String filename;
		public Bitmap image;
		public int opacity;
		public JSONObject json;

		public AdditionalImage(int dx, int dy, int sx, int sy, int width, int height, String filename, Bitmap image, int opacity, JSONObject json) throws JSONException
		{
			this.dx = dx;
			this.dy = dy;
			this.sx = sx;
			this.sy = sy;
			this.width = width;
			this.height = height;
			this.filename = filename;
			this.image = image;
			this.opacity = opacity;
			if(json == null)
			{
				this.json = json = new JSONObject();
				json.put("dx", dx);
				json.put("dy", dy);
				json.put("sx", sx);
				json.put("sy", sy);
				json.put("width", width);
				json.put("height", height);
				json.put("filename", filename);
				json.put("opacity", opacity);
			}
			else
				this.json = json;
		}

		public AdditionalImage(AdditionalImage src) throws JSONException
		{
			this(src.dx, src.dy, src.sx, src.sy, src.width, src.height, src.filename, src.image, src.opacity, null);
		}

		public AdditionalImage(JSONObject json) throws JSONException
		{
			this(json.getInt("dx"), json.getInt("dy"), json.getInt("sx"), json.getInt("sy"),
					json.getInt("width"), json.getInt("height"), json.getString("filename"), null,
					json.getInt("opacity"), json);
		}

		public JSONObject store()
		{
			return json;
		}
	}

	/** 画像読み込み時の倍率 */
	public static float scale = 1.0f;
	/** レイヤ状態の保存用 */
	private JSONObject json = new JSONObject();
	/** 追加読み込み画像の保存用 */
	private JSONArray aimagejson = new JSONArray();

	// これらはKASLayerから継承
//	private boolean visible = false;		// 表示
//	public char layerType = 'k';			// レイヤのタイプ
//	private int x = 0;						// 表示x座標
//	private int y = 0;						// 表示y座標
//	private int opacity = 255;				// 濃度
//	private int width = 100;				// 幅
//	private int height = 100;				// 高さ

	/** メッセージレイヤと一緒に隠すかどうか */
	public boolean autohide = false;

	/** 画像位置揃えの場所 */
	public static HashMap<String, Integer> scPositionX = new HashMap<String, Integer>();

	/** 画像 */
	private Bitmap image = null;
	/** 画像の名前 */
	private String imageFileName = "";
	/** 画像を持っているかどうか */
	private boolean loadedImage = false;
	/** 描画元矩形 */
	private Rect rect_src = null;
	/** 描画先矩形 */
	private Rect rect_dst = null;
	/** 描画用 Paint */
	private Paint paint = null;

	/** 塗りつぶしフラグ */
	private boolean fillFlag = false;
	/** 塗りつぶし用Paint */
	private Paint fillPaint = null;
	/** 塗りつぶしの色 */
	private int fillColor = 0;

	/** アクション定義ファイル */
	private AnimationScriptParcer animation = null;
	/** 現在読み込み中のアクション定義ファイル */
	private String actionFileName = "";

	/** 追加の画像 */
	private ArrayList<AdditionalImage> aimage = new ArrayList<AdditionalImage>();
	/** 追加画像描画用の Paint */
	private Paint apaint = new Paint();

	/**
	 * コンストラクタ
	 * @param context コンテキスト
	 */
	public Layer(Context context)
	{
		super();

		layerType = 'L';

		rect_src = new Rect();
		rect_dst = new Rect();
		paint = new Paint();

		fillPaint = new Paint();

		if(scPositionX.isEmpty())
		{
			scPositionX.put("left",         160);
			scPositionX.put("left_center",  280);
			scPositionX.put("center",       400);
			scPositionX.put("right_center", 520);
			scPositionX.put("right",        640);
		}
	}

	// move中のsetPos
	@Override
	public void move_setPos(int x, int y)
	{
		this.setPos(x, y);
	}

	// move中のsetOpacity
	@Override
	public void move_setOpacity(int opacity)
	{
		this.setOpacity(opacity);
	}

	/**
	 * 画像の読み込み
	 * @param filename ファイル名
	 * @param scale 倍率
	 * @return 成功なら true 失敗なら false
	 */
	private boolean loadImage(Context context, String filename, float scale)
	{
		if(filename == null) return false;

		// 同じものかどうかチェック
		if(loadedImage && filename.equals(imageFileName))
		{
			// 同じ画像だったなら読み込まないが、
			// 追加読み込み画像は解放する
			freeAdditionalImage();
			return true;
		}

		freeImage();
		image = ResourceManager.loadBitmap(context.getResources(), filename, scale);
		if(image == null)
			return false;

		loadAnimationFile(context, filename);	// アニメーション定義ファイル読み込み

		loadedImage = true;
		imageFileName = filename;
		fillFlag = false;
		setPos(x, y);

		if(animation == null)
			setSize((int)(image.getWidth()/scale), (int)(image.getHeight()/scale));
		else
			setSize(animation.width, animation.height);

		return true;
	}

	/**
	 * 画像の読み込み　倍率等倍
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @return 成功なら trur 失敗なら false
	 */
	public boolean loadImage(Context context, String filename)
	{
		return loadImage(context, filename, Layer.scale);
	}

	/**
	 * 画像を解放
	 */
	public void freeImage()
	{
		if(image != null)
		{
			ResourceManager.freeImage(image);
			image = null;
			loadedImage = false;
			imageFileName = "";
			animation = null;
			actionFileName = "";
		}
		freeAdditionalImage();
	}

	/**
	 * 画像の追加読み込み
	 * @param context コンテキスト
	 * @param filename ファイル名
	 * @param dx 描画先座標
	 * @param dy 描画先座標
	 * @param sx 描画元座標
	 * @param sy 描画元座標
	 * @param sw 描画元幅
	 * @param sh 描画元高さ
	 * @param opacity 描画濃度
	 * @return
	 */
	public boolean loadAdditionalImage(Context context, String filename, int dx, int dy, int sx, int sy, int sw, int sh, int opacity)
	{
		if(filename == null) return false;

		Bitmap image = ResourceManager.loadBitmap(context.getResources(), filename, 1f);
		if(image == null) return false;

		if(sw < 0) sw = image.getWidth();
		if(sh < 0) sh = image.getHeight();

		image = ResourceManager.clipBitmap(image, filename, sx, sy, sw, sh);
		if(image == null) return false;

		if(opacity < 0) opacity = 0;
		if(opacity > 255) opacity = 255;

		try
		{
			AdditionalImage data;
			data = new AdditionalImage(dx, dy, sx, sy, sw, sh, filename, image, opacity, null);
			aimage.add(data);
			return true;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 追加画像の画像のみ再読み込み
	 * @param context コンテキスト
	 * @param data データ
	 */
	public void reLoadAdditionalImage(Context context, AdditionalImage data)
	{
		if(data == null) return;

		Bitmap image = ResourceManager.loadBitmap(context.getResources(), data.filename, 1f);
		if(image == null) return;
		image = ResourceManager.clipBitmap(image, data.filename, data.sx, data.sy, data.width, data.height);
		if(image == null) return;
		data.image = image;
	}

	/**
	 * JSON から追加画像を復元
	 * @param context コンテキスト
	 * @param json JSON
	 * @throws JSONException
	 */
	public void storeAdditionalImage(Context context, JSONObject json) throws JSONException
	{
		if(json == null) return;

		AdditionalImage data = new AdditionalImage(json);
		reLoadAdditionalImage(context, data);	// 画像を再読み込み
		aimage.add(data);
	}

	/**
	 * 追加読み込みした画像を全て解放
	 */
	public void freeAdditionalImage()
	{
		AdditionalImage data;
		int size = aimage.size();
		for(int i = 0; i < size; i++)
		{
			data = aimage.get(i);
			ResourceManager.freeImage(data.image);
		}
		aimage.clear();
	}

	/**
	 * アニメーション定義ファイルを読み込み
	 * @param context コンテキスト
	 */
	public void loadAnimationFile(Context context)
	{
		loadAnimationFile(context, imageFileName);
		if(animation != null)
			setSize(animation.width, animation.height);
	}

	/**
	 * アニメーション定義ファイルをコピー
	 * @param src コピー元
	 */
	public void loadAnimationFile(String filename, AnimationScriptParcer src)
	{
		animation = new AnimationScriptParcer(src);
		actionFileName = filename;
		setSize(animation.width, animation.height);
	}

	/**
	 * アニメーション定義ファイルを読みこみ
	 * @param context コンテキスト
	 * @param filename ファイル名
	 */
	public void loadAnimationFile(Context context, String filename)
	{
		if(filename == null || filename.length() == 0 || image == null)
			return;

		animation = null;

		String[] name = new String[1];
		String[] ex = new String[1];
		Util.getExtension(filename, name, ex);
		if(name[0].length() == 0)
			return;

		filename = name[0] + ".anm";	// hoge.png なら hoge.anm
		actionFileName = filename;

		try
		{
			BufferedReader br = ResourceManager.loadScenario(filename, context);
			if(br != null)
			{
				StringBuilder sb = new StringBuilder();
				String line;
				while((line = br.readLine()) != null)
					sb.append(line);
				br.close();
				String script = sb.toString();
				if(script.length() == 0)
					animation = null;
				else
					animation = new AnimationScriptParcer(image, script);
				setSize(animation.width, animation.height);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			animation = null;
			Util.error("アニメーション定義ファイル" + filename + "の読み込みに失敗");
		}
	}

	/**
	 * アニメーションの開始
	 * @param nowTime
	 */
	public void startAnimation(long nowTime, boolean loop)
	{
		if(animation != null)
		{
			animation.startTime = nowTime;
			animation.loop = loop;
		}
	}

	/**
	 * アニメーションの停止
	 */
	public void stopAnimation()
	{
		animation.stop();
	}

	/**
	 * アニメーションが停止しているかどうか
	 * @return 停止しているなら true
	 */
	public boolean isStopedAnimation()
	{
		if(animation == null) return true;
		else return animation.isStoped();
	}

	/**
	 * アニメーションがループするかどうか
	 * @return ループするなら true
	 */
	public boolean isLoopAnimation()
	{
		return animation.loop;
	}

	/**
	 * 塗りつぶし
	 * @param color 色
	 */
	public void fillColor(int color)
	{
		fillFlag = true;
		fillPaint.setColor(color + 0xff000000);
		fillColor = color;
		freeImage();
	}

	// 描画イベント
	@Override
	public void onDraw(Context context, Canvas c, long nowTime)
	{
		if(visible)
		{
			if(fillFlag)
			{
				rect_dst.set(x, y, x+width, y+height);
				c.drawRect(rect_dst, fillPaint);
			}
			else if(loadedImage)
			{
				if(image == null || image.isRecycled())
					loadImage(context, imageFileName);

				if(image != null && !image.isRecycled())
				{
					if(animation != null)
						setAnimationPos(nowTime);
					rect_dst.set(x, y, x+width, y+height);
					c.drawBitmap(image, rect_src, rect_dst, paint);
				}
			}
			int size = aimage.size();
			AdditionalImage data;
			for(int i = 0; i < size; i++)
			{
				data = aimage.get(i);
				if(data == null) continue;

				apaint.setAlpha(data.opacity);

				if(data.image == null || data.image.isRecycled())
					reLoadAdditionalImage(context, data);
				if(data.image != null && !data.image.isRecycled())
					c.drawBitmap(data.image, data.dx, data.dy, apaint);
			}
		}
	}

	/**
	 * アニメーションのための Rect 設定
	 * @param nowTime 現在時間
	 */
	private void setAnimationPos(long nowTime)
	{
		if(animation != null && !animation.isStoped())
		{
			int frame = animation.getFrame(nowTime);
			int x = animation.posx[frame];
			int y = animation.posy[frame];
			rect_src.set(x, y, x + animation.width, y + animation.height);
		}
	}

	/**
	 * Rectの再設定
	 */
	private void resetRect()
	{
		if(image != null && loadedImage)
		{
			if(animation == null) // アニメーションが無い場合は画像全体を表示
				rect_src.set(0, 0, image.getWidth(), image.getHeight());
			else	// アニメーションがある場合はとりあえず 0 フレーム目にしておく
			{
				int x = animation.posx[0];
				int y = animation.posy[0];
				int width = animation.width, height = animation.height;
				rect_src.set(x, y, x + width, y + height);
			}
		}
		rect_dst.set(x, y, x+width, y+height);
	}

	/**
	 * 設定
	 * @param elm
	 */
	public void setOption(HashMap<String, String> elm)
	{
		String buff;
		// レイヤに設定していく
		buff = elm.get("index");
		if(buff != null && Util.isInteger(buff))
			this.index = Integer.decode(buff);
		buff = elm.get("autohide");
		if(buff != null && Util.isBoolean(buff))
			this.autohide = Boolean.valueOf(buff);
		buff = elm.get("top");
		if(buff != null && Util.isInteger(buff))
			setY(Integer.decode(buff));
		buff = elm.get("left");
		if(buff != null && Util.isInteger(buff))
			setX(Integer.decode(buff));
		buff = elm.get("width");
		if(buff != null && Util.isInteger(buff))
			setWidth(Integer.decode(buff));
		buff = elm.get("height");
		if(buff != null && Util.isInteger(buff))
			setHeight(Integer.decode(buff));
		buff = elm.get("pos");
		if(buff != null)
			setImagePos(buff);
		buff = elm.get("opacity");
		if(buff != null && Util.isInteger(buff))
			setOpacity(Integer.decode(buff));
		buff = elm.get("visible");
		if(buff != null && Util.isBoolean(buff))
			setVisible(Boolean.valueOf(buff));
	}

	// backlayとかで呼ばれる
	@Override
	public void copyLayer(Context context, KASLayer layer, long nowTime)
	{
		Layer l = (Layer)layer;
		this.index = l.index;
		setX(l.x);
		setY(l.y);
		setSize(l.width, l.height);
		setOpacity(l.opacity);
		setVisible(l.visible);

		if(l.loadedImage)
			loadImage(context, l.imageFileName);
		if(l.animation != null)
			loadAnimationFile(l.actionFileName, l.animation);

		fillPaint.set(l.fillPaint);
		fillFlag = l.fillFlag;
		fillColor = l.fillColor;

		super.copyLayer(context, layer, nowTime);
	}

	/**
	 * レイヤ状態の保存
	 * @return 保存データ
	 * @throws JSONException
	 */
	public JSONObject saveLayer() throws JSONException
	{
		json.put("visible", visible);
		json.put("index", index);
		if(moving)
		{
			json.put("x", move_start_x);
			json.put("y", move_start_y);
			json.put("opacity", move_start_o);
		}
		else
		{
			json.put("x", x);
			json.put("y", y);
			json.put("opacity", opacity);
		}
		json.put("width", width);
		json.put("height", height);
		json.put("fillflag", fillFlag);
		json.put("fillcolor", fillColor);
		json.put("loadedimage", loadedImage);
		json.put("imagefilename", imageFileName);
		if(animation != null && !animation.isStoped() && animation.loop)	// ループ中のアニメーションのみ保存
			json.put("animation", actionFileName);
		else
			json.put("animation", "");

		int size = aimage.size();
		json.put("aimagecount", size);
		AdditionalImage ai;
		for(int i = 0; i < size; i++)
		{
			ai = aimage.get(i);
			aimagejson.put(i, ai.json);
		}
		json.put("aimage", aimagejson);
		return json;
	}

	/**
	 * レイヤ状態の復元
	 * @param json 保存データ
	 * @throws JSONException
	 */
	public void loadLayer(Context context, JSONObject json, long nowTime) throws JSONException
	{
		if(json == null) return;

		if(json.has("imagefilename"))
		{
			String imageFileName = json.getString("imagefilename");
			if(imageFileName != null && imageFileName.length() != 0)
			{
				loadImage(context, imageFileName);
				if(json.has("animation"))
				{
					loadAnimationFile(context, json.getString("animation"));
					if(animation != null) startAnimation(nowTime, true);
				}
			}
		}

		if(json.has("aimagecount") && json.has("aimage"))
		{
			freeAdditionalImage();
			JSONArray array = json.getJSONArray("aimage");
			int size = json.getInt("aimagecount");
			JSONObject obj;
			for(int i = 0; i < size; i++)
			{
				obj = array.getJSONObject(i);
				storeAdditionalImage(context, obj);
			}
		}

		visible 		= json.getBoolean("visible");
		index			= json.getInt("index");
		x 				= json.getInt("x");
		y 				= json.getInt("y");
		width 			= json.getInt("width");
		height 			= json.getInt("height");
		opacity 		= json.getInt("opacity");
		fillFlag 		= json.getBoolean("fillflag");
		fillColor		= json.getInt("fillcolor");
		loadedImage		= json.getBoolean("loadedimage");

		setVisible(visible);
		setPos(x, y);
		setSize(width, height);
		setOpacity(opacity);
		if(fillFlag)
			fillColor(this.fillColor);
	}


	public void setLoadedImage(boolean loadedImage)
	{
		this.loadedImage = loadedImage;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
		resetRect();
	}

	public void setX(int x)
	{
		this.x = x;
		resetRect();
	}

	public void setY(int y)
	{
		this.y = y;
		resetRect();
	}

	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
		resetRect();
	}

	public void setWidth(int width)
	{
		this.width = width;
		resetRect();
	}

	public void setHeight(int height)
	{
		this.height = height;
		resetRect();
	}

	public void setOpacity(int opacity)
	{
		this.opacity = opacity;
		paint.setAlpha(opacity);
	}

	public void setImagePos(String pos)
	{
		this.y = Util.standardDispHeight - height;
		if(scPositionX.containsKey(pos))
			this.x = scPositionX.get(pos) - width/2;
		resetRect();
	}

	public int getX(){ return x; }
	public int getY(){ return y; }
	public int getWidth(){ return width; }
	public int getHeight(){ return height; }
	public int getOpacity(){ return opacity; }
	public boolean getVisible(){ return visible; }
	public Bitmap getImage(){ return image; }
	public boolean getLoadedImage(){ return loadedImage; }
	public String getFileName(){ return imageFileName; }

	private static final String string_for_toString = "Layer";
	@Override
	public String toString()
	{
		return string_for_toString;
	}

}

