package jc.house.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.PathUtil;

import java.io.File;

import de.greenrobot.event.EventBus;
import jc.house.R;
import jc.house.chat.event.NewMessageEvent;
import jc.house.chat.util.CommonUtils;
import jc.house.chat.widget.ChatExtendMenu;
import jc.house.chat.widget.ChatInputMenu;
import jc.house.chat.widget.ChatMessageList;
import jc.house.utils.LogUtils;
import jc.house.utils.ToastUtils;
import jc.house.views.TitleBar;


/**
 * 2015-10-31
 */
public class ChatActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener,
        ChatMessageList.MessageListItemClickListener {
    public static final String TAG = "ChatActivity";
    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;
    static final int ITEM_LOCATION = 3;

    /**
     * 发送图片、照相、地图位置
     */
    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected File cameraFile;


    public static ChatActivity instance = null;

    protected int[] itemStrings = { R.string.attach_take_pic, R.string.attach_picture, R.string.attach_location };
    protected int[] itemsDrawables = { R.drawable.jc_chat_takepic_selector, R.drawable.jc_chat_image_selector,
            R.drawable.jc_chat_location_selector};
    protected int[] itemIds = { ITEM_TAKE_PICTURE, ITEM_PICTURE, ITEM_LOCATION };

    private TitleBar titleBar;

    private String toChatUserName;

    private ChatMessageList chatMsgList;
    private SwipeRefreshLayout swipeRefreshLayout;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    private ChatInputMenu inputMenu;
    protected MyItemClickListener extendMenuItemClickListener;

    private boolean isEventBusRegister = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**this must be called before setContentView() method**/
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_chat);
        init();
        initChatMsgList();
        instance = this;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerEventBus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        unregisterEventBus();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterEventBus();
    }

    /**
     * 初始化各种视图
     */
    private void init(){
        this.titleBar = (TitleBar)findViewById(R.id.titlebar);
        this.titleBar.setTitle("会话");
        this.toChatUserName = getIntent().getStringExtra("toChatUserName");

        /**chat message ListView init**/
        this.chatMsgList = (ChatMessageList)findViewById(R.id.message_list);

        /**swipe refresh layout init**/
        this.swipeRefreshLayout = chatMsgList.getSwipeRefreshLayout();
        this.swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright, R.color.holo_green_light,
                R.color.holo_orange_light, R.color.holo_red_light);
        this.swipeRefreshLayout.setOnRefreshListener(this);

        //下方扩展菜单栏的监听器
        extendMenuItemClickListener = new MyItemClickListener();
        inputMenu = (ChatInputMenu)findViewById(R.id.input_menu);
        //注册扩展菜单项
        registerExtendMenuItem();
        inputMenu.init();
        inputMenu.setChatInputMenuListener(new ChatInputMenu.ChatInputMenuListener() {

            @Override
            public void onSendMessage(String content) {
                // 发送文本消息
                sendTxtMessage(content,toChatUserName);
            }

            //发送语音
            @Override
            public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
                Toast.makeText(ChatActivity.this, "按住说话", Toast.LENGTH_SHORT).show();
//                return voiceRecorderView.onPressToSpeakBtnTouch(v, event, new EaseVoiceRecorderCallback() {
//
//                    @Override
//                    public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
//                        // 发送语音消息
//                        sendVoiceMessage(voiceFilePath, voiceTimeLength);
//                    }
//                });
                return false;
            }
        });
    }

    /**
     * 注册底部菜单扩展栏item; 覆盖此方法时如果不覆盖已有item，item的id需大于3
     */
    protected void registerExtendMenuItem(){
        for(int i = 0; i < itemStrings.length; i++){
            inputMenu.registerExtendMenuItem(itemStrings[i], itemsDrawables[i], itemIds[i], extendMenuItemClickListener);
        }
    }

    /**
     * 初始化聊天对话列表
     */
    private void initChatMsgList(){
        LogUtils.debug(TAG, "初始化ChatMsgList");
        LogUtils.debug(TAG, "和" + toChatUserName + "的聊天对话中有" +
                EMChatManager.getInstance().getConversation(toChatUserName).getUnreadMsgCount() +
                " 条未读");
        this.chatMsgList.init(toChatUserName, 0);
        this.chatMsgList.setItemClickListener(this);
    }

    /**
     *发送TXT消息
     * @param content：发送的内容
     * @param toChatUserName：发送给谁
     */
    private void sendTxtMessage(String content,String toChatUserName){
        EMConversation conversation = EMChatManager.getInstance().getConversation(toChatUserName);
        //创建一条文本消息
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
        TextMessageBody txtBody = new TextMessageBody(content);
        message.addBody(txtBody);
        message.setReceipt(toChatUserName);
        //把消息加入到此会话对象中
        conversation.addMessage(message);
        EMChatManager.getInstance().sendMessage(message, new EMCallBack() {
            @Override
            public void onSuccess() {
                LogUtils.debug(TAG, "发送成功！");
                /**refresh chatMsgList**/
                ChatActivity.this.chatMsgList.refresh();
            }

            @Override
            public void onError(int i, String s) {
                LogUtils.debug(TAG, "发送失败！");
            }

            @Override
            public void onProgress(int i, String s) {
                LogUtils.debug(TAG, "正在发送！");
            }
        });
    }

    /**
     * 发送图片消息
     * @param imagePath
     */
    protected void sendImageMessage(String imagePath) {
        //不是发送的原图
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUserName);
        EMChatManager.getInstance().sendMessage(message, null);
        ChatActivity.this.chatMsgList.refresh();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // 发送拍照的照片
                if (cameraFile != null && cameraFile.exists())
                    sendImageMessage(cameraFile.getAbsolutePath());
            } else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            } else if (requestCode == REQUEST_CODE_MAP) { // 地图
                double latitude = data.getDoubleExtra("latitude", 0);
                double longitude = data.getDoubleExtra("longitude", 0);
                String locationAddress = data.getStringExtra("address");
                if (locationAddress != null && !locationAddress.equals("")) {
//                    sendLocationMessage(latitude, longitude, locationAddress);
                } else {
                    Toast.makeText(this, "不能显示地图信息", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 扩展菜单栏item点击事件
     */
    class MyItemClickListener implements ChatExtendMenu.ChatExtendMenuItemClickListener {

        @Override
        public void onClick(int itemId, View view) {
//            if(chatFragmentListener != null){
//                if(chatFragmentListener.onExtendMenuItemClick(itemId, view)){
//                    return;
//                }
//            }
            switch (itemId) {
                case ITEM_TAKE_PICTURE: // 拍照
                    selectPicFromCamera();
                    break;
                case ITEM_PICTURE:
                    selectPicFromLocal(); // 图库选择图片
                    break;
                case ITEM_LOCATION: // 位置
//                    startActivityForResult(new Intent(getActivity(), EaseBaiduMapActivity.class), REQUEST_CODE_MAP);
                    break;
                default:
                    break;
            }
        }

    }

    private void registerEventBus(){
        if(!isEventBusRegister){
            EventBus.getDefault().register(this);
            isEventBusRegister = true;
        }
    }

    private void unregisterEventBus(){
        if(isEventBusRegister){
            EventBus.getDefault().unregister(this);
            isEventBusRegister = false;
        }
    }

    /**
     * called when new message is coming!
     * @param event new message event
     */
    public void onEventMainThread(NewMessageEvent event){
        Intent intent = event.getIntent();
        if(intent == null)
            return;
        String msgId = intent.getStringExtra("msgid");
        String from = intent.getStringExtra("from");
        //if receive other person's message ignore
        if(!from.equals(toChatUserName))
            return;
        EMMessage message = EMChatManager.getInstance().getMessage(msgId);
        LogUtils.debug(TAG, "收到消息" + msgId);
        Toast.makeText(ChatActivity.this, "收到来自" + message.getFrom() + "的消息！", Toast.LENGTH_SHORT).show();
        //refresh ListView
        chatMsgList.refresh();
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    protected void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImageMessage(file.getAbsolutePath());
        }

    }

    /**
     * 从图库获取图片
     */
    protected void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    /**
     * 照相获取图片
     */
    protected void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            Toast.makeText(this, "手机没有存储卡，不能拍照!", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFile = new File(PathUtil.getInstance().getImagePath(), EMChatManager.getInstance().getCurrentUser()
                + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    @Override
    public void onRefresh() {
        //swipe refresh goes here
        Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        this.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResendClick(EMMessage message) {

    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onBubbleLongClick(EMMessage message) {
        Toast.makeText(this, "long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserAvatarClick(String username) {

    }
}

