package com.consts;

public final class Constants {
    private Constants() {
    }

    /**
     * 日志tag名
     */
    public static String LOG_TAG = "ivmsdemo";

    /**
     * Intent key常量
     */
    public static interface IntentKey {
        /**
         * 控制中心id
         */
        String CONTROL_UNIT_ID = "control_unit_id";
        /**
         * 区域id
         */
        String REGION_ID       = "region_id";
        /**
         * 监控点id
         */
        String CAMERA_ID       = "camera_id";
        /** 设备ID*/
        String DEVICE_ID       = "device_id";
    }

    public static interface Resource {
        /**
         * 控制中心
         */
        int TYPE_CTRL_UNIT = 1;
        /**
         * 区域
         */
        int TYPE_REGION    = 2;
        /**
         * 未知
         */
        int TYPE_UNKNOWN   = 3;
    }

    /**
     * 登录逻辑相关常量
     */
    public static interface Login {
        /**
         * 获取线路成功
         */
        int GET_LINE_SUCCESS      = 0;
        /**
         * 获取线路失败
         */
        int GET_LINE_FAILED       = 1;

        /**
         * 显示进度
         */
        int SHOW_LOGIN_PROGRESS   = 2;
        /**
         * 取消进度提示
         */
        int CANCEL_LOGIN_PROGRESS = 3;

        /**
         * 登录成功
         */
        int LOGIN_SUCCESS         = 4;
        /**
         * 登录失败
         */
        int LOGIN_FAILED          = 5;
        /**
         * 获取线路中
         */
        int GET_LINE_IN_PROCESS   = 6;
    }
}
