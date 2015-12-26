package jc.house.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import jc.house.R;
import jc.house.async.MThreadPool;
import jc.house.chat.ChatActivity;
import jc.house.global.Constants;
import jc.house.global.ServerResultType;
import jc.house.models.House;
import jc.house.models.HouseDetail;
import jc.house.models.ServerResult;
import jc.house.utils.LogUtils;
import jc.house.utils.ParseJson;
import jc.house.utils.ServerUtils;
import jc.house.views.MViewPager;
import jc.house.views.ViewPagerTitle;

public class HouseDetailActivity extends BaseNetActivity implements View.OnClickListener {
    private static final String TAG = "HouseDetailActivity";
    private static final String URL = Constants.SERVER_URL + "house/detail";
    private static final int[] ids = {R.id.recommend, R.id.traffic, R.id.design};
    private MViewPager viewPager;
    private List<TextView> textViews;
    private List<ViewPagerTitle> titles;
    private HouseDetail houseDetail;
    private TextView mapTextView;
    private TextView chatTextView;
    private int currentIndex;
    private ImageView houseImageView;
    private TextView tvAddress;
    private TextView tvHouseType;
    private TextView tvForceType;
    private TextView tvAvgPrice;
    private TextView tvPhone;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setJCContentView(R.layout.activity_house_detail);
        showDialog();
        if (!PRODUCT) {
//            id = this.getIntent().getIntExtra("id", 1);
            id = 1; //测试用的
            fetchDataFromServer();
        }
        initViews();
        initViewPager();
    }

    private void initViews() {
        this.houseImageView = (ImageView) findViewById(R.id.house_image_view);
        this.houseImageView.setOnClickListener(this);
        this.mapTextView = (TextView) this.getLayoutInflater().inflate(R.layout.div_titlebar_rightview, null);
        this.mapTextView.setText("地图");
        this.mapTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    mapTextView.setTextColor(Color.LTGRAY);
                } else {
                    mapTextView.setTextColor(Color.WHITE);
                }
                return false;
            }
        });
        this.mapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HouseDetailActivity.this, MapActivity.class);
                if (PRODUCT) {
                    intent.putExtra(MapActivity.FLAG_HOUSE, new House(12, "123", "456", "789", "hello", 123.12, 123.23));
                } else {
                    //TODO 跳转
                    intent.putExtra("IsSingleMarker", true);
                    intent.putExtra(MapActivity.FLAG_HOUSE, (House)houseDetail);
                }
                startActivity(intent);
            }
        });
        this.titleBar.setRightChildView(mapTextView);
        this.setTitleBarTitle("楼盘详情");

        this.chatTextView = (TextView) this.findViewById(R.id.chat);
        this.chatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到聊天页面
                Intent intent = new Intent(HouseDetailActivity.this, ChatActivity.class);
                if (!PRODUCT) {
                    if (null != houseDetail && null != houseDetail.getHelper()) {
                        intent.putExtra("toChatUserName", houseDetail.getHelper().getHxID());
                        intent.putExtra("nickName", houseDetail.getHelper().getName());
                    }
                } else {
                    intent.putExtra("toChatUserName", "admin");
                }
                startActivity(intent);
            }
        });

        this.tvAddress = (TextView) this.findViewById(R.id.address);
        this.tvHouseType = (TextView) this.findViewById(R.id.houseType);
        this.tvForceType = (TextView) this.findViewById(R.id.forceType);
        this.tvAvgPrice = (TextView) this.findViewById(R.id.avgPrice);
        this.tvPhone = (TextView) this.findViewById(R.id.phone);
    }

    private void initViewPager() {
        this.viewPager = (MViewPager) this.findViewById(R.id.viewpager);
        this.currentIndex = 0;
        this.titles = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            ViewPagerTitle title = (ViewPagerTitle) this.findViewById(ids[i]);
            title.setIndex(i);
            title.setSelected(i == currentIndex);
            title.setOnClickListener(this);
            titles.add(title);
        }
        this.textViews = new ArrayList<>(3);
        for (int i = 0; i < 3; i++) {
            TextView textView = new TextView(this);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setPadding(12, 10, 12, 5);
            textView.setTextSize(13.0f);
            textView.setTextColor(Color.rgb(120, 120, 120));
            textView.setBackgroundColor(Color.rgb(250, 250, 250));
            textView.setText("NBA卫冕冠军库里在新赛季依旧有着高光的发挥，他带领勇士队在新赛季获得16连胜，风头正劲的库里在NBA中的地位就如同梅西在足球界的地位。");
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLineSpacing(0, 1.2f);
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
            textViews.add(textView);
        }
        this.viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return textViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                TextView textView = textViews.get(position);
                container.addView(textView);
                return textView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(textViews.get(position));
            }
        });

        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != currentIndex) {
                    titles.get(currentIndex).setSelected(false);
                    titles.get(position).setSelected(true);
                    currentIndex = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setServerData() {
        if (null != this.houseDetail) {
            this.tvAddress.setText(this.houseDetail.getAddress());
            this.tvHouseType.setText(this.houseDetail.getHouseType());
            this.tvForceType.setText(this.houseDetail.getForceType());
            this.tvAvgPrice.setText(this.houseDetail.getAvgPrice());
            this.tvPhone.setText(this.houseDetail.getPhone());
            this.textViews.get(0).setText(this.houseDetail.getRecReason());
            this.textViews.get(1).setText(this.houseDetail.getTrafficLines());
            this.textViews.get(2).setText(this.houseDetail.getDesignIdea());
            this.loadImage(houseImageView, this.houseDetail.getUrl());
            hideDialog();
            if (null != houseDetail.getHelper()) {
                Toast.makeText(this, houseDetail.getHelper().getName() + houseDetail.getHelper().getHxID(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchDataFromServer() {
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(id));
        this.client.post(URL, new RequestParams(params), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                parseServerData(statusCode, response);
                LogUtils.debug("houseDetail", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                LogUtils.debug("houseDetail", responseString);
            }
        });
    }

    private void parseServerData(int statusCode, final JSONObject response) {
        if (ServerUtils.isConnectServerSuccess(statusCode, response)) {
            final ServerResult result = ServerUtils.parseServerResponse(response, ServerResultType.ServerResultTypeObject);
            if (ServerResult.CODE_SUCCESS == result.code) {
                MThreadPool.getInstance().getExecutorService().submit(new Runnable() {
                    @Override
                    public void run() {
                        houseDetail = (HouseDetail) ParseJson.jsonObjectToBaseModel(result.object, HouseDetail.class);
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                setServerData();
                            }
                        });
                    }
                });
            } else {
                handleCode(result.code, TAG);
            }
        } else {
            handleFailure();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.house_image_view) {
            Intent showOriImg = new Intent(this, PhotoViewActivity.class);
            if (!PRODUCT) {
                if (null != houseDetail) {
                    showOriImg.putExtra("image_url", Constants.IMAGE_URL + houseDetail.getUrl());
                }
            } else {
                showOriImg.putExtra("image_url", "http://www.jinchenchina.cn/uploads/allimg/150710/0-150G0124350951.jpg");
            }
            startActivity(showOriImg);
            return;
        }
        int index = ((ViewPagerTitle) v).getIndex();
        if (index != currentIndex) {
            titles.get(currentIndex).setSelected(false);
            titles.get(index).setSelected(true);
            currentIndex = index;
            viewPager.setCurrentItem(index);
        }
    }

}
