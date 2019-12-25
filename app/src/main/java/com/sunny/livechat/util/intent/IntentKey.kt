package com.sunny.livechat.util.intent

/**
 * Desc Intent 配置清单
 * Author JoannChen
 * Mail yongzuo.chen@foxmail.com
 * Date 2019/10/31 0031 10:57
 */
object IntentKey {

    /**
     * 保存登录成功后返回的用户实体类
     * 个人中心使用
     */
    const val userInfoBean = "userInfoBean"

    /**
     * 跳转页
     */
    const val fromPage = "fromPage" //跳转页面标识
    const val fromDrawing = "fromDrawing" //图纸和资料
    const val fromEffectPicture = "fromEffectPicture" //现场效果图

    /**
     * 跳转页传集合使用
     * TaskDetailsFragment -> CargoListActivity
     */
    const val arrayList = "arrayList"
    const val objectBean = "objectBean"

    /**
     * MainActivity引用，聊天掉线后跳转登录页使用
     */
    const val MainActivity = "MainActivity"

    /**
     *  判断页面跳转类型
     *  true：项目页
     *  false：任务页
     */
    const val isFromProjectPage = "isFromProjectPage"

    /**
     * 参与人列表，用来匹配聊天人员名称
     */
    const val joinUserList = "joinUserList"

    /**
     * 参与人id列表，在修改参与人界面：按钮勾选状态处理时使用
     */
    const val joinUserIdList = "joinUserIdList"

    /**
     * 部门人员列表
     */
    const val departmentPeopleList = "departmentPeopleList"

    /**
     * 聊天界面消息ID，提醒页面用来跳转的标识
     */
    const val chatId = "chatId"

    /**
     * 消息提醒标题，当前提醒的那条消息
     */
    const val remindTitle = "remindTitle"

    /**
     * 项目负责人id / 任务创建者id
     */
    const val leaderId = "leaderId"

    /**
     * 项目名称
     */
    const val projectName = "projectName"

    /**
     * 项目id
     * 任务id
     */
    const val projectId = "projectId"

    /**
     * 到款情况
     */
    const val arrivalAccount = "arrivalAccount"

}