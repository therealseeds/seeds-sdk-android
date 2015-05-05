package ly.count.android.sdk.inappmessaging;

public enum Gender {
	MALE("m"),
	FEMALE("f");
	
	private String param;
	
	Gender(String param) {
		this.param = param;
	}
	
	public String getServerParam() {
		return this.param;
	}
}
