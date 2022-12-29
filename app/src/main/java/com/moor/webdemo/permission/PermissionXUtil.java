package com.moor.webdemo.permission;


import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     @author : Trial
 *     @time   : 11/23/21
 *     @desc   :
 *     @version: 1.0
 * </pre>
 */
public class PermissionXUtil {
    public static void checkPermission(final FragmentActivity activity, final OnRequestCallback callback, String... permission) {
        PermissionX.init(activity)
                .permissions(permission)
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        List<String> mList = new ArrayList<>();
                        for (String item : deniedList) {
                            mList.add(PermissionConstants.getInstance().getPermissionName(item));
                        }
                        scope.showRequestReasonDialog(deniedList, "该功能需要以下权限，才能使用" + mList, "确定", "取消");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        List<String> mList = new ArrayList<>();
                        for (String item : deniedList) {
                            mList.add(PermissionConstants.getInstance().getPermissionName(item));
                        }
                        scope.showForwardToSettingsDialog(deniedList, "请在设置中允许以下权限" + mList, "去开启", "取消");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            if (callback != null) {
                                callback.requestSuccess();
                            }
                        } else {
                            List<String> mList = new ArrayList<>();
                            for (String item : deniedList) {
                                mList.add(PermissionConstants.getInstance().getPermissionName(item));
                            }
                            Toast.makeText(activity.getApplicationContext(), "您拒绝了如下权限：" + mList, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
