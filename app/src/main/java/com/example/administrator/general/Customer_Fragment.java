package com.example.administrator.general;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.example.administrator.general.db.SerialDao;
import com.example.administrator.general.filemanager.FileManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Administrator on 2018\11\14 0014.
 */

public class Customer_Fragment extends Fragment {


    SerialDao serialDao;
    @BindView(R.id.btn_data_daoru)
    LinearLayout btnDataDaoru;
    Unbinder unbinder;
    private ExcelManager em = new ExcelManager();
    private String excelFile = "";
    public boolean isError = true;
    private Handler myHandler;
    private ProgressDialog m_Dialog;
    private boolean isCloseDialog = false;

    public static Customer_Fragment newInstance() {
        Customer_Fragment fragment = new Customer_Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serialDao = new SerialDao(getActivity());
        myHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle bundle = new Bundle();
                bundle = msg.getData();
                excelFile = bundle.getString("xuanze");
                if (isError == false) {
                    if (em.isEXL(excelFile)) {
                        if (excelToSheet(excelFile) == true) {
                            isCloseDialog = true;
                            isError = true;
                            myHandler.sendEmptyMessageDelayed(0, 100);
                            Toast.makeText(getActivity(), "数据导入成功",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            isCloseDialog = true;
                            isError = true;
                            myHandler.sendEmptyMessageDelayed(0, 100);
                            Toast.makeText(getActivity(), "数据导入失败",
                                    Toast.LENGTH_SHORT).show();
                            // m_Dialog.dismiss();
                        }
                    } else {
                        isCloseDialog = true;
                        isError = true;
                        myHandler.sendEmptyMessageDelayed(0, 100);
                        Toast.makeText(getActivity(), "你打开的EXCEL文件格式不对。",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (isCloseDialog) {
                        m_Dialog.dismiss();
                        isCloseDialog = false;
                        isError = false;
                    } else {
                        myHandler.sendEmptyMessageDelayed(0, 100);
                        isError = true;
                    }
                }
            }
        };
    }

    private void openFileManager() {
        // 调用文件资源管理器
        Intent intent = new Intent();
        intent.setClass(getActivity(), FileManager.class);
        // 向资源管理器传递参数
        Bundle bundle = new Bundle();
        bundle.putString("xuanze", "");
        bundle.putBoolean("iserror", true);
        intent.putExtras(bundle);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case MainActivity.RESULT_OK:
                // 取得来自文件管理器传递的文件名
                Message msg = new Message();
                Bundle bundle = data.getExtras();
                excelFile = bundle.getString("xuanze");
                isError = bundle.getBoolean("iserror");
                msg.setData(bundle);
//				myHandler.sendEmptyMessageDelayed(0, 100);
                myHandler.sendMessage(msg);
                m_Dialog = ProgressDialog.show(getActivity(), "请等待...",
                        "正在导入数据...", true);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * excel表格数据进行导入
     */
    private boolean excelToSheet(String file) {
        boolean isErr = false;
        List<DataBean> list = em.findMereExcelRecord(file, DataBean.class);
        if (list.size() > 0) {
            isErr = true;
        }
        DataBean bean=new DataBean();
        for (DataBean dataBean : list) {
            bean.setName(dataBean.getName());
            bean.setQuantity(dataBean.getQuantity());
            bean.setNumber(dataBean.getNumber());
            serialDao.insert(bean);
        }
        return isErr;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_data_daoru)
    public void onViewClicked() {
        openFileManager();
    }
}
