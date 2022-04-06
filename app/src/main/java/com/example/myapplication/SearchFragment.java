package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

// 按周 月 显示打卡记录   （上班 下班的具体时间  打卡结果）
// 统计一个正常勤和异常（包括各种迟到早退的） 显示在最上面 没有就是0天
// 默认显示本月的  点击按钮可以切换最近四周和这个月 列表形式一条一条的显示出来
// https://www.cnblogs.com/AnneHan/p/9702365.html  使用ViewPager + Fragment实现滑动菜单
public class SearchFragment extends Fragment implements View.OnClickListener {
    // 2个小标题
    private TextView item_detail, item_category_report;
    private ViewPager vp;
    // 月和周的详细的明细表 用2个fragment展示出来
    private WeekFragment weekFragment;
    private MonthFragment monthFragment;

    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private FragmentAdapter mFragmentAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = View.inflate(getActivity(), R.layout.fragment_search, null);

//       初始化布局View:
        item_detail = (TextView) view.findViewById(R.id.item_detail);
        item_category_report = (TextView) view.findViewById(R.id.item_category_report);

        item_detail.setOnClickListener(this); // implements
        item_category_report.setOnClickListener(this);

        vp = (ViewPager) view.findViewById(R.id.mainViewPager);
        weekFragment = new WeekFragment();
        monthFragment = new MonthFragment();
        //给FragmentList添加数据
        mFragmentList.add(weekFragment);
        mFragmentList.add(monthFragment);


        mFragmentAdapter = new FragmentAdapter(getActivity().getSupportFragmentManager(), mFragmentList);
        vp.setOffscreenPageLimit(2);//ViewPager的缓存为2帧
        vp.setAdapter(mFragmentAdapter);
        vp.setCurrentItem(0);//初始设置ViewPager选中第一帧
        item_detail.setTextColor(Color.parseColor("#1ba0e1"));

        //ViewPager的监听事件
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                /*此方法在页面被选中时调用*/
                changeTextColor(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                /*此方法是在状态改变的时候调用，其中arg0这个参数有三种状态（0，1，2）。
                arg0==1的时辰默示正在滑动，
                arg0==2的时辰默示滑动完毕了，
                arg0==0的时辰默示什么都没做。*/
            }
        });
        return view;
    }

    /**
     * 初始化布局View
     */
//    private void initViews() {
//
//    }

    /**
     * 点击头部Text 动态修改ViewPager的内容
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_detail:
                vp.setCurrentItem(0, true);
                break;
            case R.id.item_category_report:
                vp.setCurrentItem(1, true);
                break;
        }
    }

    public class FragmentAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList = new ArrayList<Fragment>();

        public FragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

    }

    /**
     * 由ViewPager的滑动修改头部导航Text的颜色
     *
     * @param position
     */
    private void changeTextColor(int position) {
        if (position == 0) {
            item_detail.setTextColor(Color.parseColor("#1ba0e1"));
            item_category_report.setTextColor(Color.parseColor("#000000"));
        } else if (position == 1) {
            item_category_report.setTextColor(Color.parseColor("#1ba0e1"));
            item_detail.setTextColor(Color.parseColor("#000000"));
        }
    }
}