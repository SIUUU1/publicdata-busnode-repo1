package publicbusdata;

public class BusInfo {
	// member variable
	private int nodeno; // 정류소 번호
	private double gpslati; // 정류소 Y좌표
	private double gpslong; // 정류소 X좌표
	private String nodeid; // 정류소ID
	private String nodenm; // 정류소명
	private String curdate; // 정보수정날짜
	// constructor

	public BusInfo() {
		super();
	}

	public BusInfo(int nodeno, double gpslati, double gpslong, String nodeid, String nodenm, String curdate) {
		super();
		this.nodeno = nodeno;
		this.gpslati = gpslati;
		this.gpslong = gpslong;
		this.nodeid = nodeid;
		this.nodenm = nodenm;
		this.curdate = curdate;
	}

	// member function
	public int getNodeno() {
		return nodeno;
	}

	public void setNodeno(int nodeno) {
		this.nodeno = nodeno;
	}

	public double getGpslati() {
		return gpslati;
	}

	public void setGpslati(double gpslati) {
		this.gpslati = gpslati;
	}

	public double getGpslong() {
		return gpslong;
	}

	public void setGpslong(double gpslong) {
		this.gpslong = gpslong;
	}

	public String getNodeid() {
		return nodeid;
	}

	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}

	public String getNodenm() {
		return nodenm;
	}

	public void setNodenm(String nodenm) {
		this.nodenm = nodenm;
	}

	public String getCurdate() {
		return curdate;
	}

	public void setCurdate(String curdate) {
		this.curdate = curdate;
	}

	@Override
	public String toString() {
		return "" + nodeno + " \t " + gpslati + " \t " + gpslong + " \t " + nodeid + " \t " + nodenm + " \t " + curdate
				+ "";
	}

}
