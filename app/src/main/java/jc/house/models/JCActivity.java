package jc.house.models;

/**
 * Created by hzj on 2015/10/30.
 */
public final class JCActivity extends BaseModel {
    private String title;
    private String picUrl;
    private String detailUrl;
    private Long postTime;

    public JCActivity(int id, String picUrl, String name) {
        super(id);
        this.picUrl = picUrl;
    }

    public JCActivity() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public Long getPostTime() {
        return postTime;
    }

    public void setPostTime(Long postTime) {
        this.postTime = postTime;
    }
}
