package com.taihuoniao.shop.utils;  
  
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
  
public class AsynImageLoader {  
    private static final String TAG = "AsynImageLoader";  
    // 缓存下载过的图片的Map  
    private Map<String, SoftReference<Bitmap>> caches;  
    // 异步任务,对于一个ImageView采用一个任务来管理
    private SparseArray<DownloadTask> taskQueue;
  
    public static final String CACHE_DIR = "imagecache";
   
    
    static class MyReference<T>{
    	T t;
        public MyReference() {
            t = null;
        }
        public MyReference(T r) {
            t = r;
        }
        public T get(){
        	return t;
        }
        public void clear(){
        	t = null;
        }
    }
    
    private class DownloadTask extends AsyncTask<String, Bitmap, Bitmap>{
    	private MyReference<ImageView> mViewRef;
    	private String TAG;
    	public DownloadTask(ImageView imageView,String urlArg){
    		mViewRef =  new MyReference<ImageView>(imageView);
    		url = urlArg;
    		TAG = AsynImageLoader.TAG + imageView.hashCode();
    	}
    	public boolean equalUrl(String urlArg){
    		if(url == urlArg)
    			return true;
    		if(url != null && urlArg != null){
    			return url.equalsIgnoreCase(urlArg);
    		}
    		return false;
    	}
    	private String url;
		@Override
		protected Bitmap doInBackground(String... params) {
			if(params != null && params.length > 0)
				url = params[0];
			else
				url = null;			
			if(url == null || url.isEmpty())
				return null;
			Log.i(TAG, "doInBackground begin url:" + url);			
            File cacheFile = FileUtil.getCacheFile(url);
            Bitmap bitmap = null;
            if(cacheFile.exists()){//直接读取cache
            	try{
            		bitmap = BitmapFactory.decodeFile(cacheFile.getCanonicalPath());
            		Log.i(TAG, "BitmapFactory.decodeFile file_path:"+cacheFile.getCanonicalPath() + " bitmap:" + bitmap);
            		if(bitmap != null){
        				synchronized (caches) {
        					caches.put(url, new SoftReference<Bitmap>(bitmap));
        				}           			
            			publishProgress(bitmap);//把本地读取的文件先显示出来            			
            		}else
            			cacheFile.delete();
            	}catch(IOException e){
            		e.printStackTrace();
            		cacheFile.delete();
            	}
            }
            Log.i(TAG, "doInBackground network gitbitmap url:" + url);
            Bitmap bitmap_net = PicUtil.getbitmap(url);//使用PicUtil的方法来获取网络的图片
            if(bitmap_net != null){//把图片写入本地文件中
				synchronized (caches) {
					caches.put(url, new SoftReference<Bitmap>(bitmap_net));
				}
            	bitmap = bitmap_net;
            	try{
            	    FileOutputStream out = new FileOutputStream(cacheFile);
            	    bitmap.compress(Bitmap.CompressFormat.PNG,100,out);
            	    out.flush();
            	    out.close();
            	}catch (FileNotFoundException e){
            	    e.printStackTrace();
            	}catch (IOException e){
            	    e.printStackTrace();
            	}
            }
			if(bitmap == null){
		    	Log.w(TAG, "downlaod null bitmap url:"+url);				
			}
            Log.i(TAG, "doInBackground end url:" + url + "return bitmap:"+bitmap);
			return bitmap;
		}
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			ImageView imageView = mViewRef.get();
			if(imageView != null && bitmap != null){
				imageView.setImageBitmap(bitmap);
				Log.i(TAG, "onPostExecute setImageBitmap imageView:" + imageView + " bitmap:" + bitmap);
			}else{
				Log.w(TAG, "onPostExecute failure imageView:" + imageView + " bitmap:" + bitmap);				
			}
		}
		@Override
		protected void onProgressUpdate(Bitmap... values) {
			super.onProgressUpdate(values);
			Bitmap bitmap;
			if(values != null && values.length > 0)
				bitmap = values[0];
			else
				bitmap = null;
			ImageView imageView = mViewRef.get();
			if(imageView != null && bitmap != null){
				imageView.setImageBitmap(bitmap);
				Log.i(TAG, "onProgressUpdate setImageBitmap imageView:" + imageView + " bitmap:" + bitmap);
			}else{
				Log.w(TAG, "onProgressUpdate failure imageView:" + imageView + " bitmap:" + bitmap);
			}
		}
		public void setImageViewNull(){
			Log.w(TAG, "setImageViewNull clear imageView:" + mViewRef.get() );
			mViewRef.clear();
		}
    }
    
    
    public AsynImageLoader(){  
        // 初始化变量  
        caches = new HashMap<String, SoftReference<Bitmap>>();  
        taskQueue = new SparseArray<DownloadTask>();
    }        
    //异步加载图片需要考虑以下问题：
    //对于同一个imageView进行一系列的图片加载，需要保证显示的是最后一个图片
    //为了避免由于其中一个图片存在问题，导致线程堵塞，应该使用多个加载线程，这种情况下显然无法保证最后的图片被最后加载到imageview中
    //解决方案，对于同一个imageview 使用同一个加载任务，开始新的加载任务之前清除之前的加载任务
    public void showImageAsyn(ImageView imageView,String url){
    	showImageAsyn(imageView,url,R.drawable.loading_tile_android);
    }
    public void showImageAsyn(ImageView imageView, String url, int resId){  
    	if (url == null || url.isEmpty()){
        	Log.w(TAG+imageView.hashCode(), "showImageAsyn setImageResource imageView:"+imageView.hashCode() + " url:"+url + " resId:"+resId);
    		imageView.setImageResource(resId);
    		return;
    	}
		SoftReference<Bitmap> rf = null;
    	synchronized (caches) {
        	if(caches.containsKey(url)){
                rf = caches.get(url);  
        	}   	   	
    	}
    	if(rf != null){
            // 通过软引用，获取图片  
            Bitmap bitmap = rf.get();  
            // 如果该图片已经被释放，则将该path对应的键从Map中移除掉  
            if(bitmap != null){
                // 如果图片未被释放，直接返回该图片  
            	imageView.setImageBitmap(bitmap);
            	Log.i(TAG+imageView.hashCode(), "showImageAsyn caches imageView:"+imageView.hashCode() + " url:"+url + " resId:"+resId);
            	return;
            }    		
    	} 
    	imageView.setImageResource(resId);
    	DownloadTask task = taskQueue.get(imageView.hashCode());    
    	if(task != null){
    		if(task.equalUrl(url)){//同样的任务，不需要重复
    			Log.i(TAG+imageView.hashCode(), "showImageAsyn same task imageView:"+imageView.hashCode() + " url:"+url + " resId:"+resId);
    			return;
    		}
    		//boolean cancel = task.cancel(true);
    		//Log.v(TAG,"task status:" + task.getStatus() +" cancel:"+cancel);
    		if(task.getStatus()==Status.RUNNING||task.getStatus()==Status.FINISHED){
    			task.setImageViewNull();
        		task = new DownloadTask(imageView,url);
        		taskQueue.put(imageView.hashCode(), task);
    		}
    	}else{
    		task = new DownloadTask(imageView,url);
    		taskQueue.append(imageView.hashCode(), task);
    	}
    	task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    	//task.execute(url);
    	Log.i(TAG+imageView.hashCode(), "showImageAsyn task imageView:"+imageView.hashCode() + " url:"+url + " resId:"+resId);
    }
    //static private ExecutorService executorService = Executors.newFixedThreadPool(6); 
}
//只使用其中    getbitmap 方法
class PicUtil {  
    private static final String TAG = "PicUtil";  
  
    /** 
     * 根据一个网络连接(URL)获取bitmapDrawable图像 
     *  
     * @param imageUri 
     * @return 
     */  
    public static BitmapDrawable getfriendicon(URL imageUri) {  
  
        BitmapDrawable icon = null;  
        try {  
            HttpURLConnection hp = (HttpURLConnection) imageUri  
                    .openConnection();  
            icon = new BitmapDrawable(hp.getInputStream());// 将输入流转换成bitmap  
            hp.disconnect();// 关闭连接  
        } catch (Exception e) {  
        }  
        return icon;  
    }  
  
    /** 
     * 根据一个网络连接(String)获取bitmapDrawable图像 
     *  
     * @param imageUri 
     * @return 
     */  
    public static BitmapDrawable getcontentPic(String imageUri) {  
        URL imgUrl = null;  
        try {  
            imgUrl = new URL(imageUri);  
        } catch (MalformedURLException e1) {  
            e1.printStackTrace();  
        }  
        BitmapDrawable icon = null;  
        try {  
            HttpURLConnection hp = (HttpURLConnection) imgUrl.openConnection();  
            icon = new BitmapDrawable(hp.getInputStream());// 将输入流转换成bitmap  
            hp.disconnect();// 关闭连接  
        } catch (Exception e) {  
        }  
        return icon;  
    }  
  
    /** 
     * 根据一个网络连接(URL)获取bitmap图像 
     *  
     * @param imageUri 
     * @return 
     */  
    public static Bitmap getusericon(URL imageUri) {  
        // 显示网络上的图片  
        URL myFileUrl = imageUri;  
        Bitmap bitmap = null;  
        try {  
            HttpURLConnection conn = (HttpURLConnection) myFileUrl  
                    .openConnection();  
            conn.setDoInput(true);  
            conn.connect();  
            InputStream is = conn.getInputStream();  
            bitmap = BitmapFactory.decodeStream(is);  
            is.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return bitmap;  
    }  
  
    /** 
     * 根据一个网络连接(String)获取bitmap图像 
     *  
     * @param imageUri 
     * @return 
     * @throws MalformedURLException 
     */  
    public static Bitmap getbitmap(String imageUri) {  
        // 显示网络上的图片  
        Bitmap bitmap = null;  
        try {  
            URL myFileUrl = new URL(imageUri);  
            HttpURLConnection conn = (HttpURLConnection) myFileUrl  
                    .openConnection();  
            conn.setDoInput(true);  
            conn.connect();  
            InputStream is = conn.getInputStream();  
            bitmap = BitmapFactory.decodeStream(is);  
            is.close();
            //Log.v(TAG, "image download finished." + imageUri);  
        } catch (IOException e) {  
            e.printStackTrace();  
            return null;  
        }  
        return bitmap;  
    }  
  
    /** 
     * 下载图片 同时写道本地缓存文件中 
     *  
     * @param context 
     * @param imageUri 
     * @return 
     * @throws MalformedURLException 
     */  
    public static Bitmap getbitmapAndwrite(String imageUri) {  
        Bitmap bitmap = null;  
        try {  
            // 显示网络上的图片  
            URL myFileUrl = new URL(imageUri);  
            HttpURLConnection conn = (HttpURLConnection) myFileUrl  
                    .openConnection();  
            conn.setDoInput(true);  
            conn.connect();  
  
            InputStream is = conn.getInputStream();  
            File cacheFile = FileUtil.getCacheFile(imageUri);  
            BufferedOutputStream bos = null;  
            bos = new BufferedOutputStream(new FileOutputStream(cacheFile));  
            Log.i(TAG, "write file to " + cacheFile.getCanonicalPath());  
  
            byte[] buf = new byte[1024];  
            int len = 0;  
            // 将网络上的图片存储到本地  
            while ((len = is.read(buf)) > 0) {  
                bos.write(buf, 0, len);  
            }  
  
            is.close();  
            bos.close();  
  
            // 从本地加载图片  
            bitmap = BitmapFactory.decodeFile(cacheFile.getCanonicalPath());  
            //String name = MD5Util.MD5(imageUri);  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return bitmap;  
    }  
  
    public static boolean downpic(String picName, Bitmap bitmap) {  
        boolean nowbol = false;  
        try {  
            File saveFile = new File("/mnt/sdcard/download/weibopic/" + picName  
                    + ".png");  
            if (!saveFile.exists()) {  
                saveFile.createNewFile();  
            }  
            FileOutputStream saveFileOutputStream;  
            saveFileOutputStream = new FileOutputStream(saveFile);  
            nowbol = bitmap.compress(Bitmap.CompressFormat.PNG, 100,  
                    saveFileOutputStream);  
            saveFileOutputStream.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return nowbol;  
    }  
  
    public static void writeTofiles(Context context, Bitmap bitmap,  
            String filename) {  
        BufferedOutputStream outputStream = null;  
        try {  
            outputStream = new BufferedOutputStream(context.openFileOutput(  
                    filename, Context.MODE_PRIVATE));  
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * 将文件写入缓存系统中 
     *  
     * @param filename 
     * @param is 
     * @return 
     */  
    public static String writefile(Context context, String filename,  
            InputStream is) {  
        BufferedInputStream inputStream = null;  
        BufferedOutputStream outputStream = null;  
        try {  
            inputStream = new BufferedInputStream(is);  
            outputStream = new BufferedOutputStream(context.openFileOutput(  
                    filename, Context.MODE_PRIVATE));  
            byte[] buffer = new byte[1024];  
            int length;  
            while ((length = inputStream.read(buffer)) != -1) {  
                outputStream.write(buffer, 0, length);  
            }  
        } catch (Exception e) {  
        } finally {  
            if (inputStream != null) {  
                try {  
                    inputStream.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
            if (outputStream != null) {  
                try {  
                    outputStream.flush();  
                    outputStream.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return context.getFilesDir() + "/" + filename + ".jpg";  
    }  
  
    // 放大缩小图片  
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        Matrix matrix = new Matrix();  
        float scaleWidht = ((float) w / width);  
        float scaleHeight = ((float) h / height);  
        matrix.postScale(scaleWidht, scaleHeight);  
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,  
                matrix, true);  
        return newbmp;  
    }  
  
    // 将Drawable转化为Bitmap  
    public static Bitmap drawableToBitmap(Drawable drawable) {  
        int width = drawable.getIntrinsicWidth();  
        int height = drawable.getIntrinsicHeight();  
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable  
                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                : Bitmap.Config.RGB_565);  
        Canvas canvas = new Canvas(bitmap);  
        drawable.setBounds(0, 0, width, height);  
        drawable.draw(canvas);  
        return bitmap;  
  
    }  
  
    // 获得圆角图片的方法  
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {  
        if(bitmap == null){  
            return null;  
        }  
          
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),  
                bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        final RectF rectF = new RectF(rect);  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        return output;  
    }  
  
    // 获得带倒影的图片方法  
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {  
        final int reflectionGap = 4;  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
  
        Matrix matrix = new Matrix();  
        matrix.preScale(1, -1);  
  
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,  
                width, height / 2, matrix, false);  
  
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,  
                (height + height / 2), Config.ARGB_8888);  
  
        Canvas canvas = new Canvas(bitmapWithReflection);  
        canvas.drawBitmap(bitmap, 0, 0, null);  
        Paint deafalutPaint = new Paint();  
        canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);  
  
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);  
  
        Paint paint = new Paint();  
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,  
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,  
                0x00ffffff, TileMode.CLAMP);  
        paint.setShader(shader);  
        // Set the Transfer mode to be porter duff and destination in  
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));  
        // Draw a rectangle using the paint with our linear gradient  
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()  
                + reflectionGap, paint);  
  
        return bitmapWithReflection;  
    }  
  
} 
class FileUtil {  
    private static final String TAG = "FileUtil";  
  
    public static File getCacheFile(String imageUri){  
        File cacheFile = null;  
        try {  
        	File sdCardDir = null;
            if (Environment.getExternalStorageState().equals(  
                    Environment.MEDIA_MOUNTED)) {  
                sdCardDir = Environment.getExternalStorageDirectory();  
            }else{
            	sdCardDir = Environment.getDataDirectory();            	
            }
            String fileName = getFileName(imageUri);  
            File dir = new File(sdCardDir.getCanonicalPath()  
                    + "/" + AsynImageLoader.CACHE_DIR);  
            if (!dir.exists()) {  
                dir.mkdirs();  
            }  
            cacheFile = new File(dir, fileName);  
            Log.i(TAG, "getCacheFile exists:" + cacheFile.exists() + " file_path:" + cacheFile.getCanonicalPath());  
        } catch (IOException e) {  
            e.printStackTrace();  
            Log.e(TAG, "getCacheFile getCacheFileError:" + e.getMessage());  
        }
        return cacheFile;  
    }  
      
    public static String getFileName(String path) {  
        int index = path.lastIndexOf("/");  
        return path.substring(index + 1);  
    }  
}  
