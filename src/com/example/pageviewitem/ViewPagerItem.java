package com.example.pageviewitem;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.example.eventmap.R;

import com.example.pageviewitem.ViewPagerAdapter;;

public class ViewPagerItem extends Fragment implements OnClickListener,OnPageChangeListener{
	//定义ViewPager对象
	private ViewPager viewPager;
	
	//定义ViewPager适配器
	private ViewPagerAdapter vpAdapter;
	
	//定义一个ArrayList来存放View
	private ArrayList<View> views;

	//引导图片资源
    private static final int[] pics = {R.drawable.guide1,R.drawable.guide2,R.drawable.guide3,R.drawable.guide4};
    
    //底部小点的图片
    private ImageView[] points;
    
    //记录当前选中位置
    private int currentIndex;
    /*public ViewPagerItem()
    {
    	setContentView(R.layout.layoutpager);
    }*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
                                                                                                                                                                                                                                                                                                                      
        View view = inflater.inflate(R.layout.layoutpaper, null);  
        views = new ArrayList<View>();
		
		//实例化ViewPager
		viewPager = (ViewPager) view.findViewById(R.id.viewpager);
		
		//实例化ViewPager适配器
		vpAdapter = new ViewPagerAdapter(views);
		System.out.println("hehehehhehehehhehe");
        //初始化引导图片列表
        for(int i=0; i<pics.length; i++) {
        	ImageView iv = new ImageView(getActivity());
            //iv.setLayoutParams(mParams);
            iv.setImageResource(pics[i]);
            views.add(iv);
        } 
        System.out.println("hahahhahahahhahahaha1");
        //设置数据
        viewPager.setAdapter(vpAdapter);
 
        //设置监听
        viewPager.setOnPageChangeListener(this);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.ll);       
        
        points = new ImageView[pics.length];

        //循环取得小点图片
        for (int i = 0; i < pics.length; i++) {
        	//得到一个LinearLayout下面的每一个子元素
        	points[i] = (ImageView) linearLayout.getChildAt(i);
        	//默认都设为灰色
        	points[i].setEnabled(true);
        	//给每个小点设置监听
        	points[i].setOnClickListener(this);
        	//设置位置tag，方便取出与当前位置对应
        	points[i].setTag(i);
        }
        
        //设置当面默认的位置
        currentIndex = 0;
        //设置为白色，即选中状态
        points[currentIndex].setEnabled(false);
        //System.out.println("hahahhahahahhahahaha");
        //初始化底部小点
        //initPoint();
		//initData();	
        return view;
    }
    
    private void initData(){
		//定义一个布局并设置参数
		/*LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                														  LinearLayout.LayoutParams.FILL_PARENT);*/
       //System.out.println("hehehehhehehehhehe");
        //初始化引导图片列表
        for(int i=0; i<pics.length; i++) {
        	//System.out.println("hahahhahahahhahahaha1");
            ImageView iv = (ImageView) getView().findViewById(pics[i]);
            //iv.setLayoutParams(mParams);
            
            views.add(iv);
        } 
        System.out.println("hahahhahahahhahahaha2");
        //设置数据
        viewPager.setAdapter(vpAdapter);
        //System.out.println("hahahhahahahhahahaha");
        //设置监听
        viewPager.setOnPageChangeListener(this);
        
        //初始化底部小点
        initPoint();
	}

    private void initPoint(){
		LinearLayout linearLayout = (LinearLayout) getView().findViewById(R.id.ll);       
		
        points = new ImageView[pics.length];

        //循环取得小点图片
        for (int i = 0; i < pics.length; i++) {
        	//得到一个LinearLayout下面的每一个子元素
        	points[i] = (ImageView) linearLayout.getChildAt(i);
        	//默认都设为灰色
        	points[i].setEnabled(true);
        	//给每个小点设置监听
        	points[i].setOnClickListener(this);
        	//设置位置tag，方便取出与当前位置对应
        	points[i].setTag(i);
        }
        
        //设置当面默认的位置
        currentIndex = 0;
        //设置为白色，即选中状态
        points[currentIndex].setEnabled(false);
	}
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		setCurDot(position);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int position = (Integer)v.getTag();
        setCurView(position);
        setCurDot(position);
	}
	
	private void setCurView(int position){
        if (position < 0 || position >= pics.length) {
            return;
        }
        viewPager.setCurrentItem(position);
    }

    /**
    * 设置当前的小点的位置
    */
   private void setCurDot(int positon){
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }
        points[positon].setEnabled(false);
        points[currentIndex].setEnabled(true);

        currentIndex = positon;
    }

}