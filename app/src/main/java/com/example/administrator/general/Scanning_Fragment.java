package com.example.administrator.general;

import android.app.Fragment;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.general.common.CommonAdapter;
import com.example.administrator.general.common.ViewHolder;
import com.example.administrator.general.db.SerialDao;
import com.github.jdsjlzx.interfaces.OnItemClickListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Administrator on 2018\11\14 0014.
 */

public class Scanning_Fragment extends Fragment {


    @BindView(R.id.scanning_tv)
    TextView scanningTv;
    Unbinder unbinder;
    @BindView(R.id.scanning_lrv)
    LRecyclerView scanningLrv;
    @BindView(R.id.scanning_btn)
    Button scanningBtn;
    private SweetAlertDialog chongfuDialog;
    SerialDao serialDao;
    private LRecyclerViewAdapter lRecyclerViewAdapter = null;
    private CommonAdapter<DataBean> adapter;
    private List<DataBean> datas = new ArrayList<>(); //PDA机屏幕上的List集合
    private SweetAlertDialog sweetAlertDialog;
    private File file;
    private ArrayList<ArrayList<String>> recordList;
    private static String[] title = {"商品单号", "商品名称", "数量"};


    public static Scanning_Fragment newInstance() {
        Scanning_Fragment fragment = new Scanning_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanning, container, false);
        unbinder = ButterKnife.bind(this, view);
        //注册订阅者
        EventBus.getDefault().register(this);
        chongfuDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.ERROR_TYPE);
        chongfuDialog.setCancelable(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serialDao = new SerialDao(getActivity());
        intiView();
        initAdapter();
    }

    private void intiView() {

    }


    //接受扫码消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        String string = messageEvent.getMessage();
        string.replace(" ", "");
        scanningTv.setText(string);
        initData(string);
    }




    private void initData(String data) {

        List<DataBean> list = serialDao.select1(data);
        Log.d("feng", list.toString());
        int error = 0;
        for (int i = 0; i < datas.size(); i++) {
            if (data.equals(datas.get(i).getNumber())) {
                error = 1;
//                return;
            }
        }
        switch (error) {
            case 1:
                chongfuDialog
                        .setTitleText("重复录入...");
                chongfuDialog.setConfirmText("确定");
                chongfuDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        chongfuDialog.dismiss();
                    }
                });
                chongfuDialog.show();
                break;
            default:
                for (int i = 0; i < list.size(); i++) {
                    datas.add(list.get(i));
                    lRecyclerViewAdapter.notifyDataSetChanged();
                }
                chongfuDialog.dismiss();
                break;

        }
    }

    private void initAdapter() {
        adapter = new CommonAdapter<DataBean>(getActivity(), R.layout.adapter_scanning, datas) {
            @Override
            public void setData(ViewHolder holder, DataBean dataBean) {
                holder.setText(R.id.adapter_waibu_tv, dataBean.getNumber());
                holder.setText(R.id.adapter_aoshi_tv, "商品名称："+dataBean.getName());
                holder.setText(R.id.adapter_fenyun_tv, "数量"+dataBean.getQuantity());
            }
        };


        scanningLrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        lRecyclerViewAdapter = new LRecyclerViewAdapter(adapter);
        scanningLrv.setAdapter(lRecyclerViewAdapter);
        scanningLrv.setLoadMoreEnabled(false);
        scanningLrv.setPullRefreshEnabled(false);
        lRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                sweetAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.showCancelButton(true);
                sweetAlertDialog.setCancelText("取消");
                sweetAlertDialog.setTitleText("确定删除此条信息?");
                sweetAlertDialog.setConfirmText("确定");
                sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                });
                sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        datas.remove(position);
                        lRecyclerViewAdapter.notifyDataSetChanged();
                        sweetAlertDialog.dismiss();
                    }
                });
                sweetAlertDialog.show();
            }
        });
    }

    @OnClick({R.id.scanning_btn,R.id.scanning_tv})
    public void onViewClicked(View view) {
        switch (view.getId()){
            case R.id.scanning_btn:
                if (datas.size()!=0){
                    String time= DateUtils.getCurrentTime3();
                    if (time.equals(SPUtils.get(getActivity(),"time",""))){
                        exportExcel(time);
                    }else {
                        SPUtils.remove(getActivity(), "fileName");
                        exportExcel(time);
                    }
                    datas.clear();
                    lRecyclerViewAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(getActivity(),"信息错误",Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.scanning_tv:
//                SPUtils.clear(getActivity());
//                Toast.makeText(getActivity(),"清除",Toast.LENGTH_SHORT).show();
                break;
        }

    }

    /**
     * 导出excel
     *
     * @param
     */
    public void exportExcel(String excelName) {
        file = new File(getSDPath() + "/Record");
        makeDir(file);
        String fileName = (String) SPUtils.get(getActivity(), "fileName", "");
        if (fileName.equals("")) {
            String excelFile = file.toString() + "/" + excelName + ".xls";
            ExcelUtils.initExcels(getRecordData(), excelFile, title, excelName, getActivity());
//            ExcelUtils.writeObjListToExcels(getRecordData(), fileName, excelName,  getActivity());
        } else {
            ExcelUtils.writeObjListToExcels(getRecordData(), fileName, excelName,  getActivity());
        }
    }
    /**
     * 将数据集合 转化成ArrayList<ArrayList<String>>
     *
     * @return
     */
    private ArrayList<ArrayList<String>> getRecordData() {
        recordList = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            DataBean dataBean = datas.get(i);
            ArrayList<String> beanList = new ArrayList<String>();
//            beanList.add("1");
            beanList.add(dataBean.getNumber());
            beanList.add(dataBean.getName());
            beanList.add(dataBean.getQuantity());
            beanList.add(DateUtils.getCurrentTime2());
            recordList.add(beanList);

        }
        return recordList;
    }

    private String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;
    }

    public void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        //解除订阅者
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
