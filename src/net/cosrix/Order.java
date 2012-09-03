package net.cosrix;

public class Order {
	   
    private String orderName;
    private String orderStatus;
    private String thumb;
    private String ytId;
    private boolean loaded;
    private String duration;
   
    public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public boolean isLoaded() {
		return loaded;
	}
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	public String getYtId() {
		return ytId;
	}
	public void setYtId(String ytId) {
		this.ytId = ytId;
	}
	public String getThumb() {
		return this.thumb;
	}
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	public String getOrderName() {
        return orderName;
    }
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }
    public String getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}