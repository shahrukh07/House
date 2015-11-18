package jc.house.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jc.house.JCListView.XListView;
import jc.house.R;
import jc.house.activities.WebActivity;
import jc.house.adapters.ListAdapter;
import jc.house.global.Constants;
import jc.house.global.FetchType;
import jc.house.models.ModelType;
import jc.house.models.News;
import jc.house.utils.ParseJson;
import jc.house.views.CircleView;

public class NewsFragment extends JCNetFragment implements CircleView.CircleViewOnClickListener {
    private static final int[] imageReIds = {R.drawable.caodi,
            R.drawable.chengbao, R.drawable.caodi};
//	private static final String[] imageUrls = {"123", "456"};
	private static final String TAG = "NewsFragment";
    private static final int PAGE_SIZE = 8;
    private boolean isOver = false;
    private List<News> news;
    private ListAdapter<News> adapter;
    public NewsFragment() {
        super();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.xlistView = (XListView) view.findViewById(R.id.list);
        news = new ArrayList<News>();
        CircleView circleView = new CircleView(this.getActivity());
        circleView.setAutoPlay(true);
        circleView.setTimeInterval(3.6f);
        circleView.setImageReIds(imageReIds);
        circleView.setOnCircleViewItemClickListener(this);
        news.add(new News(1, "", "最近房价的走势,哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈", "楠楠", "2015/11/18"));
        news.add(new News(1, "", "最近房价的走势", "楠楠", "2015/11/18"));
        news.add(new News(1, "", "最近房价的走势", "楠楠", "2015/11/18"));
        news.add(new News(1, "", "最近房价的走势", "楠楠", "2015/11/18"));
        this.adapter = new ListAdapter<News>(this.getActivity(), news, ModelType.NEWS, circleView);
        this.xlistView.setAdapter(adapter);
        this.xlistView.setXListViewListener(this);
        this.xlistView.setPullLoadEnable(true);
        this.xlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = (View) inflater.inflate(R.layout.common_list, container, false);
        return this.view;
    }

    @Override
    protected void handleResponse(int statusCode, JSONObject response, FetchType fetchtype) {
        resetXListView();
        if (200 == statusCode && null != response) {
            try {
                int code = response.getInt("code");
                if (Constants.CODE_SUCCESS == code) {
                    JSONArray array = response.getJSONArray("result");
                    List<News> lists = ParseJson.parseNews(array);
                    if (null != lists && lists.size() > 0) {
                        if (fetchtype == FetchType.FETCH_TYPE_REFRESH) {
                            news.clear();
                        }
                        if (lists.size() < PAGE_SIZE) {
                            isOver = true;
                        }
                    }
                    news.addAll(lists);
                    adapter.notifyDataSetChanged();
                } else {
                    handleCode(code, TAG);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void fetchDataFromServer(final FetchType fetchtype) {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
                resetXListView();
//            }
//        }, 1000);
        /*
        Map<String, String> params = new HashMap<String, String>();
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        if (FetchType.FETCH_TYPE_LOAD_MORE == fetchtype) {
            if (!this.isOver) {
                if (news.size() > 0) {
                    params.put("id", String.valueOf(news.get(0).getID()));
                }
            } else {
                resetXListView();
                return;
            }
        }
        this.client.post("", new RequestParams(params), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                handleResponse(statusCode, response, fetchtype);
                LogUtils.debug(TAG, "statusCode is " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                LogUtils.debug(TAG, "statusCode is " + statusCode);
            }
        });
        */
    }

    @Override
    public void onCircleViewItemClick(View v, int index) {
        Intent intent = new Intent(getActivity(), WebActivity.class);
        startActivity(intent);
    }
}
