package com.example.pageviewitem;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.eventmap.MainActivity;
import com.example.eventmap.MapActivity;
import com.example.eventmap.R;
import com.example.pageviewitem.ViewPagerAdapter;;

public class ViewPagerItem extends FragmentActivity implements OnClickListener, OnPageChangeListener{
	//����ViewPager����
	private ViewPager viewPager;
	
	//����ViewPager������
	private ViewPagerAdapter vpAdapter;
	
	//����һ��ArrayList�����View
	private ArrayList<View> views;

	//����ͼƬ��Դ
    private static final int[] pics = {R.drawable.guide1,R.drawable.guide2,R.drawable.guide3,R.drawable.guide4};
    
    //�ײ�С���ͼƬ
    private ImageView[] points;
    
    //��¼��ǰѡ��λ��
    private int currentIndex;
    
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.layoutpaper);
		initView();
		
		initData();	
    }
    private void initView(){
		//ʵ����ArrayList����
		views = new ArrayList<View>();
		
		//ʵ����ViewPager
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		
		//ʵ����ViewPager������
		vpAdapter = new ViewPagerAdapter(views);
	}
	
	/**
	 * ��ʼ������
	 */
	private void initData(){
		//����һ�����ֲ����ò���
		//LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
          //      														  LinearLayout.LayoutParams.FILL_PARENT);
       
        //��ʼ������ͼƬ�б�
        for(int i=0; i<pics.length; i++) {
            ImageView iv = new ImageView(this);
            //iv.setLayoutParams(mParams);
            iv.setImageResource(pics[i]);
            views.add(iv);
        } 
        
        //��������
        viewPager.setAdapter(vpAdapter);
        //���ü���
        viewPager.setOnPageChangeListener(this);
        
        //��ʼ���ײ�С��
        initPoint();
	}
   
    

	private void initPoint(){
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);       
		
        points = new ImageView[pics.length];

        //ѭ��ȡ��С��ͼƬ
        for (int i = 0; i < pics.length; i++) {
        	//�õ�һ��LinearLayout�����ÿһ����Ԫ��
        	points[i] = (ImageView) linearLayout.getChildAt(i);
        	//Ĭ�϶���Ϊ��ɫ
        	points[i].setEnabled(true);
        	//��ÿ��С�����ü���
        	points[i].setOnClickListener(this);
        	//����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
        	points[i].setTag(i);
        }
        
        //���õ���Ĭ�ϵ�λ��
        currentIndex = 0;
        //����Ϊ��ɫ����ѡ��״̬
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
    * ���õ�ǰ��С���λ��
    */
   private void setCurDot(int positon){
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }
        points[positon].setEnabled(false);
        points[currentIndex].setEnabled(true);

        currentIndex = positon;
    }
   @Override
	public void onBackPressed() {
	    Intent myIntent = new Intent(ViewPagerItem.this, MainActivity.class);
	    setResult(1,myIntent);
	    finish();
	}

}