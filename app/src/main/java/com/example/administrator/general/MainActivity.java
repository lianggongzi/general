package com.example.administrator.general;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.LinearLayout;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import org.greenrobot.eventbus.EventBus;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener{

    LinearLayout fragmentDemoLl;
    BottomNavigationBar bottomNavigationBar;
    Scanning_Fragment scanningFragment;
    Customer_Fragment customerFragment;
    private SweetAlertDialog MEIDDialog;
    private Fragment mContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_text);
        fragmentDemoLl=findViewById(R.id.fragment_demo_ll);
        bottomNavigationBar=findViewById(R.id.bottom_bar);
        MEIDDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        MEIDDialog.setCancelable(false);
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//                Log.d("feng",tm.getDeviceId());
        if (tm.getDeviceId().equals("86362903009233")) {
            intiView();
        } else {
            initDialog();
        }
//        intiView();
    }


    @Override
    public void updateCount() {

    }

    @Override
    public void updateList(String data) {
        EventBus.getDefault().post(new MessageEvent(data));
    }


    private void initDialog() {
        MEIDDialog
                .setTitleText("请联系商家");
        MEIDDialog.setConfirmText("确定");
        MEIDDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                MEIDDialog.dismiss();
                System.exit(0);
            }
        });
        MEIDDialog.show();
    }
    private void intiView() {
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.scanning, "扫码").setActiveColor("#3F51B5"))
                .addItem(new BottomNavigationItem(R.drawable.customer, "数据").setActiveColor("#3F51B5"))
                .setFirstSelectedPosition(0)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);
//        setDefaultFragment();

        setDefaultFragment();
    }

    /**
     * 设置默认的fragment，即//第一次加载界面;
     */
    private void setDefaultFragment() {
        scanningFragment = Scanning_Fragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_demo_ll, scanningFragment).commit();
        mContent = scanningFragment;
    }

    /**
     * 修改显示的内容 不会重新加载 *
     */
    public void switchContent(Fragment to) {
        FragmentManager fragmentManager = getFragmentManager();
        if (mContent != to) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (!to.isAdded()) { // 先判断是否被add过
                transaction.hide(mContent).add(R.id.fragment_demo_ll, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(mContent).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
            mContent = to;
        }
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fm = this.getFragmentManager();
        //开启事务
        FragmentTransaction transaction = fm.beginTransaction();
        switch (position) {
            case 0:
                if (scanningFragment == null) {
                    scanningFragment = Scanning_Fragment.newInstance();
                }
//                showCustomizeDialog(srttingFragmenr, (Integer) SPUtils.get(this, "position", 1));
                //将当前的事务添加到了回退栈
//                transaction.addToBackStack(null);
//                transaction.add(R.id.fragment_demo_ll, fragment1);
                switchContent(scanningFragment);
                break;
            case 1:
                if (customerFragment == null) {
                    customerFragment = Customer_Fragment.newInstance();
                }
//                transaction.add(R.id.fragment_demo_ll, fragment2);
                switchContent(customerFragment);
                break;
            default:
                break;
        }
        // 事务提交
        transaction.commit();
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }
}
