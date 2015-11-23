package com.taihuoniao.shop.widget;

import com.taihuoniao.shop.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;


public class SwipeListView extends ListView{
	private static final String TAG = "SwipeListView";
    public static final int X = 0;
    public static final int Y = 1;	


    private static LinearInterpolator sLinearInterpolator = new LinearInterpolator();

    private static final boolean SLOW_ANIMATIONS = false; // DEBUG;
    private float SWIPE_ESCAPE_VELOCITY = 100f; // dp/sec
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 200; // ms
    private int MAX_ESCAPE_ANIMATION_DURATION = 400; // ms
    private int MAX_DISMISS_VELOCITY = 2000; // dp/sec
    private static final int SNAP_ANIM_LEN = SLOW_ANIMATIONS ? 1000 : 150; // ms
	
    private SwipeItemView mCurrView;
    private float mPagingTouchSlop;
    private int mSwipeDirection;
    private float mTouchSlop;
    private VelocityTracker mVelocityTracker;

    private float mInitialTouchPos;
    private float mInitialTouchOtherPos;
    private boolean mDragging;
    private boolean mOtherDragging;
    private float mDensityScale;
    
	public SwipeListView(Context context) {
		super(context);
		Init(context);
	}

	public SwipeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init(context);
	}

	public SwipeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Init(context);
	}
	private void Init(Context context){
		mDensityScale =  getResources().getDisplayMetrics().density;
        final ViewConfiguration configuration = ViewConfiguration.get(mContext);
        mPagingTouchSlop = configuration.getScaledPagingTouchSlop();
        mPagingTouchSlop = configuration.getScaledTouchSlop();
        mSwipeDirection = X;
        mVelocityTracker = VelocityTracker.obtain();
	}
	@Override
	public void setAdapter(ListAdapter adapter){
		SwipeListAdapter thisAdapter = new SwipeListAdapter(adapter);
		super.setAdapter(thisAdapter);
	}
	public interface OnDeleteItemListener{
		void onDeleteItem(int position,View content);
	}
	public void setOnDeleteItemListener(OnDeleteItemListener listener){
		mOnDeleteItemListener = listener;
	}
	private OnDeleteItemListener mOnDeleteItemListener;
	
	private class SwipeItemView extends FrameLayout{
		private View mContent;
		private Button mDelete;
		private int mPosition;
		private final float swipe_size;
		private final float swipe_size_over;
		private static final int BUTTON_WIDTH_MARGIN_LEFT = 20;
		private static final int BUTTON_WIDTH = 90;
		private final float mDensity;
		public SwipeItemView(Context context,View content,int position) {
			super(context);
			mDensity = getResources().getDisplayMetrics().density;
			mContent = content;
			swipe_size = -mDensity *(BUTTON_WIDTH - BUTTON_WIDTH_MARGIN_LEFT);
			swipe_size_over = mDensity *BUTTON_WIDTH_MARGIN_LEFT;
			mPosition = position;			
			Init(context);
		}
		public View getContentView(){
			return mContent;
		}
		private void Init(Context context){
			this.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			this.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			this.setBackground(null);
			LayoutParams params = new LayoutParams((int)(BUTTON_WIDTH * mDensity),LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.RIGHT;
			mDelete = new Button(context);
			mDelete.setPadding((int)swipe_size_over, 0, 0, 0);
			mDelete.setTextColor(getResources().getColor(R.color.colorWhite));
			mDelete.setText(R.string.delete);
			mDelete.setOnClickListener(mClickDeleteListener);
			mDelete.setEnabled(false);
			mDelete.setBackgroundColor(getResources().getColor(R.color.colorBkRed));
			
			addView(mDelete,params);
			addView(mContent,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));			
		}
	    private OnClickListener mClickDeleteListener = new OnClickListener() {		
			@Override
			public void onClick(View v) {
				Log.e(TAG, "onClick mPosition:" + mPosition);
				if(mOnDeleteItemListener!=null)
					mOnDeleteItemListener.onDeleteItem(mPosition,mContent);
			}
		};	

		//获取需要显示的滑动距离
		public float getSwipeSize(){
			return swipe_size;
		}
		public float getSwipeOverSize(){
			return swipe_size_over;
		}		
		public void setTranslation(float translate){
			mContent.setTranslationX(translate);
			mDelete.setEnabled(Math.abs(translate)<Math.abs(getSwipeSize()/10)?false:true);
		}
		//获取当前的滑动的位置
		public float getTranslation(){
			return mContent.getTranslationX();
		}
		//创建滑动到指定位置的动画
		public ObjectAnimator createTranslationAnimation(float newPos){
			return ObjectAnimator.ofFloat(mContent,"translationX", newPos);
		}
	}
	private class SwipeListAdapter implements ListAdapter{
		ListAdapter targetAdapter;
		public SwipeListAdapter(ListAdapter adapter) {
			targetAdapter = adapter;
		}
		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			targetAdapter.registerDataSetObserver(observer);
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			targetAdapter.unregisterDataSetObserver(observer);
		}

		@Override
		public int getCount() {
			return targetAdapter.getCount();
		}

		@Override
		public Object getItem(int position) {
			return targetAdapter.getItem(position);
		}

		@Override
		public long getItemId(int position) {
			return targetAdapter.getItemId(position);
		}

		@Override
		public boolean hasStableIds() {
			return targetAdapter.hasStableIds();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			SwipeItemView swipe;
			if(convertView == null){
				View content = targetAdapter.getView(position, convertView, parent);
				swipe = new SwipeItemView(parent.getContext(),content,position);
				swipe.setTag(content.getTag());
			}else{
				swipe = (SwipeItemView)convertView;
				View content = targetAdapter.getView(position, swipe.getContentView(), parent);
				if(content != swipe.getContentView()){
					swipe = new SwipeItemView(parent.getContext(),content,position);
					swipe.setTag(content.getTag());
				}
			}
			return swipe;
		}

		@Override
		public int getItemViewType(int position) {
			return targetAdapter.getItemViewType(position);
		}

		@Override
		public int getViewTypeCount() {
			return targetAdapter.getViewTypeCount();
		}

		@Override
		public boolean isEmpty() {
			return targetAdapter.isEmpty();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return targetAdapter.areAllItemsEnabled();
		}

		@Override
		public boolean isEnabled(int position) {
			return targetAdapter.isEnabled(position);
		}
	}
	
    private boolean onMyInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragging = false;
                mOtherDragging = false;
                mCurrView = getItemViewAtPosition(ev);
                mVelocityTracker.clear();
                if (mCurrView != null) {
                    mVelocityTracker.addMovement(ev);
                    mInitialTouchPos = getPos(ev);
                    mInitialTouchOtherPos = getOtherPos(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mDragging && mCurrView != null && !mOtherDragging) {
                    mVelocityTracker.addMovement(ev);
                    float pos = getPos(ev);
                    float delta = pos - mInitialTouchPos;
                    if (Math.abs(delta) > mPagingTouchSlop) {
                        onBeginDrag();
                        mDragging = true;
                        mInitialTouchPos = getPos(ev) - mCurrView.getTranslation();
                    }
                    float otherPos = getOtherPos(ev);
                    float otherDelta = otherPos - mInitialTouchOtherPos;
                    if (Math.abs(otherDelta) > mPagingTouchSlop) {
                    	mOtherDragging = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                mOtherDragging = false;
                mCurrView = null;
                break;
        }
        return mDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging || mOtherDragging) {
            return super.onTouchEvent(ev);
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    float delta = getPos(ev) - mInitialTouchPos;
                    // don't let items that can't be dismissed be dragged more than
                    // maxScrollDistance
                    //       
                    float deltaReal;
                    float minDelta = Math.min(0, mCurrView.getSwipeSize());
                    float maxDelta = Math.max(0, mCurrView.getSwipeSize());
                    if (delta < minDelta || delta > maxDelta) {//超出滑动范围
                        float size = maxDelta - minDelta;
                        float maxScrollDistance = mCurrView.getSwipeOverSize();//0.15f * size;//缩小移动距离
                        if (delta < minDelta - size) {
                        	deltaReal = minDelta - maxScrollDistance;
                        } else if( delta > maxDelta + size){
                        	deltaReal = maxDelta + maxScrollDistance;
                        }else if(delta < minDelta){
                        	deltaReal = minDelta-maxScrollDistance * (float) Math.sin((minDelta - delta)/size*(Math.PI/2));
                        }else{//delte > maxDelta
                        	deltaReal = maxDelta+maxScrollDistance * (float) Math.sin((delta - maxDelta)/size*(Math.PI/2));
                        }
                    }else
                    	deltaReal = delta;
                    mCurrView.setTranslation(deltaReal);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrView != null) {
                    float velocity = getVelocity(mVelocityTracker);
                    swipeChild(velocity);
                    onDragCancelled();
                }
                break;
        }
        return true;
    }
    
    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    	boolean ret = super.dispatchTouchEvent(ev);
    	onMyInterceptTouchEvent(ev);
	    return ret;
    };
    
    private void swipeChild(float velocity) {
        float maxVelocity = MAX_DISMISS_VELOCITY * mDensityScale;
        mVelocityTracker.computeCurrentVelocity(1000 /* px/sec */, maxVelocity);
        float escapeVelocity = SWIPE_ESCAPE_VELOCITY * mDensityScale;
        float perpendicularVelocity = getPerpendicularVelocity(mVelocityTracker);
        // Decide whether to dismiss the current view
        boolean childSwipedFastEnough = (Math.abs(velocity) > escapeVelocity) &&
                (Math.abs(velocity) > Math.abs(perpendicularVelocity)) &&
                (velocity > 0) == (mCurrView.getTranslation() > 0);
        float newPos;
        int duration;
        if(childSwipedFastEnough){
        	//滑动的方向和目标方向一致，则滑向目标，否则，滑向0
        	newPos = velocity * mCurrView.getSwipeSize() < 0 ? 0:mCurrView.getSwipeSize();
        	if(velocity != 0){
            	duration = MAX_ESCAPE_ANIMATION_DURATION;
	            duration = Math.min(duration,
	                    (int) (Math.abs(newPos - mCurrView.getTranslation()) * 1000f / Math
	                            .abs(velocity)));   
        	}else{
        		duration = DEFAULT_ESCAPE_ANIMATION_DURATION;
        	}
	    }else{//非滑动状态下，看离哪个位置更加近
        	newPos = Math.abs(mCurrView.getTranslation()) < Math.abs(mCurrView.getTranslation()-mCurrView.getSwipeSize()) ? 0 : mCurrView.getSwipeSize();
        	duration = SNAP_ANIM_LEN;
        }
        ObjectAnimator anim = mCurrView.createTranslationAnimation(newPos);
        anim.setInterpolator(sLinearInterpolator);
        anim.setDuration(duration);
        final SwipeItemView v=mCurrView;
        final float pos = newPos;
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
            	v.setTranslation(pos);
            }
        });
        anim.start();
    }
    
	public SwipeItemView getItemViewAtPosition(MotionEvent ev) {
        final float x = ev.getX() + getScrollX();
        final float y = ev.getY() + getScrollY();
        for (int i = 0; i < getChildCount(); i++) {
            View item = getChildAt(i);
            if (item.getVisibility() == View.VISIBLE
                    && x >= item.getLeft() && x < item.getRight()
                    && y >= item.getTop() && y < item.getBottom()) {
            	if(item instanceof SwipeItemView)
            		return (SwipeItemView)item;
            	else
            		break;
            }
        }
        return null;
	}
    private float getPos(MotionEvent ev) {
        return mSwipeDirection == X ? ev.getX() : ev.getY();
    }
    private float getOtherPos(MotionEvent ev) {
        return mSwipeDirection == X ? ev.getY():ev.getX();
    }
    private float getVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getXVelocity() :
                vt.getYVelocity();
    }    
    private float getPerpendicularVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getYVelocity() :
                vt.getXVelocity();
    }
	private void onBeginDrag() {
		//requestDisallowInterceptTouchEvent(true);
	}
	private void onDragCancelled() {

	}
}
