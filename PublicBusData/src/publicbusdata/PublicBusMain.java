package publicbusdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PublicBusMain {
	public static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		// 웹접속을 통해서 버스정류장 정보를 ArrayList에 담아온다.
		ArrayList<BusInfo> busInfoList = null; // 웹에서 가져온 busInfoList
		ArrayList<BusInfo> busInfoSelectList = null; // DB에서 가져온 busInfoSelectList
		boolean exitFlag = false;
		while (!exitFlag) {
			System.out.println("1. 웹정보가져오기 2. 테이블저장하기 3. 테이블읽어오기 4. 수정하기 5. 삭제하기 6. 종료");
			System.out.print("선택>>");
			int count = Integer.parseInt(sc.nextLine());
			switch (count) {
			case 1:
				// 웹정보가져오기
				busInfoList = webConnection();
				break;
			case 2:
				// 테이블저장하기
				if (busInfoList.isEmpty()) {
					System.out.println("공공데이터로부터 가져온 자료가 없습니다.");
					continue;
				}
				insertBusInfo(busInfoList);
				break;
			case 3:
				// 테이블읽어오기
				busInfoSelectList = selectBusIfo();
				printBusInfo(busInfoSelectList);
				break;
			case 4:
				// 수정하기
				String data = updateInputNodeno();
				if (data.length() != 0) { // data 엔터만 입력 방지
					updateBusInfo(Integer.parseInt(data));
				}
				break;
			case 5:
				// 삭제하기
				deleteBusInfo();
				break;
			case 6:
				// 종료
				System.out.println("종료");
				exitFlag = true;
				break;
			}
		} // end of while
		System.out.println("The end");
	}// end of main

	// 웹정보가져오기
	public static ArrayList<BusInfo> webConnection() {
		ArrayList<BusInfo> list = new ArrayList<>();
		// 1. 요청 url 생성
		StringBuilder urlBuilder = new StringBuilder(
				"http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList");
		try {
			urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8")
					+ "=ZZK6F84oQsGUcoz%2BiNgdSgTrLL5bKPHr2ppbYMYXSKVSRBzjVqPzEyWEzKoOqVLUdNpry0wyNejJ1AnztcO2HQ%3D%3D");
			urlBuilder.append("&" + URLEncoder.encode("cityCode", "UTF-8") + "=" + URLEncoder.encode("25", "UTF-8"));
			urlBuilder.append("&" + URLEncoder.encode("nodeNm", "UTF-8") + "=" + URLEncoder.encode("전통시장", "UTF-8"));
			urlBuilder.append("&" + URLEncoder.encode("nodeNo", "UTF-8") + "=" + URLEncoder.encode("44810", "UTF-8"));
			urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8"));
			urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
			urlBuilder.append("&" + URLEncoder.encode("_type", "UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// 2. 서버주소 Connection con
		URL url = null;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlBuilder.toString()); // 웹서버주소 action
			conn = (HttpURLConnection) url.openConnection(); // 접속요청 get방식
			conn.setRequestMethod("GET"); // get방식
			conn.setRequestProperty("Content-type", "application/json");
			// System.out.println("Response code: " + conn.getResponseCode());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 3. 요청내용을 전송 및 응답 처리
		BufferedReader br = null;
		try {
			// conn.getResponseCode() 서버에서 상태코드를 알려주는 값
			int statusCode = conn.getResponseCode();
			System.out.println(statusCode);
			if (statusCode >= 200 && statusCode <= 300) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
			Document doc = parseXML(conn.getInputStream());
			// a. item 태그객체 목록으로 가져온다.
			NodeList descNodes = doc.getElementsByTagName("item");
			// b. BusInfo List객체 생성
			// List<BusInfo> list = new ArrayList<>();
			// c. 각 item 태그의 자식태그에서 정보 가져오기
			for (int i = 0; i < descNodes.getLength(); i++) {
				// item
				Node item = descNodes.item(i);
				BusInfo data = new BusInfo();
				// item 자식태그에 순차적으로 접근
				for (Node node = item.getFirstChild(); node != null; node = node.getNextSibling()) {
					System.out.println(node.getNodeName() + " : " + node.getTextContent());

					switch (node.getNodeName()) {
					case "gpslati":
						data.setGpslati(Double.parseDouble(node.getTextContent()));
						break;
					case "gpslong":
						data.setGpslong(Double.parseDouble(node.getTextContent()));
						break;
					case "nodeid":
						data.setNodeid(node.getTextContent());
						break;
					case "nodenm":
						data.setNodenm(node.getTextContent());
						break;
					case "nodeno":
						data.setNodeno(Integer.parseInt(node.getTextContent()));
						break;
					}
				}
				// d. List객체에 추가
				list.add(data);
			} // end of for
				// e.최종확인
			list.forEach(e->System.out.println(e));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	// xml을 객체로 바꿔주는 함수
	public static Document parseXML(InputStream inputStream) {
		DocumentBuilderFactory objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder objDocumentBuilder = null;
		Document doc = null;
		try {
			objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();
			doc = objDocumentBuilder.parse(inputStream);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) { // Simple API for XML e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	// 공공데이터를 테이블에 저장하기
	public static void insertBusInfo(ArrayList<BusInfo> busInfoList) {
		if (busInfoList.isEmpty()) {
			System.out.println("입력할 데이터가 없습니다.");
			return;
		}
		// 저장하기 전에 테이블에 있는 내용 삭제하기
		deleteBusInfo();

		String sql = "insert into businfo values(?, ?, ?, ?, ?, null)";
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DBUtil.makeConnection();
			for (BusInfo data : busInfoList) {
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1, data.getNodeno());
				pstmt.setDouble(2, data.getGpslati());
				pstmt.setDouble(3, data.getGpslong());
				pstmt.setString(4, data.getNodeid());
				pstmt.setString(5, data.getNodenm());
				int value = pstmt.executeUpdate();

				if (value == 1) {
					System.out.println(data.getNodenm() + " 정류장 등록완료");
				} else {
					System.out.println(data.getNodenm() + " 정류장 등록실패");
				}
			} // end of for
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 버스정보 가져오기
	public static ArrayList<BusInfo> selectBusIfo() {
		ArrayList<BusInfo> busInfoList = null;
		String sql = "select * from businfo";
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DBUtil.makeConnection();
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			busInfoList = new ArrayList<BusInfo>();
			while (rs.next()) {
				BusInfo businfo = new BusInfo();
				businfo.setNodeno(rs.getInt("NODENO"));
				businfo.setGpslati(rs.getDouble("GPSLATI"));
				businfo.setGpslong(rs.getDouble("GPSLONG"));
				businfo.setNodeid(rs.getString("NODEID"));
				businfo.setNodenm(rs.getString("NODENM"));
				businfo.setCurdate(String.valueOf(rs.getDate("CURDATE")));
				busInfoList.add(businfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return busInfoList;
	}

	// 버스정보 출력하기
	public static void printBusInfo(ArrayList<BusInfo> busInfoSelectList) {
		if (busInfoSelectList.isEmpty()) {
			System.out.println("출력할 버스 정보가 없습니다.");
			return;
		}
		System.out.println("NODENO \t GPSLATI \t GPSLONG \t NODEID \t NODENM \t CURDATE");
		busInfoSelectList.forEach(e -> System.out.println(e));
	}

	// 수정할 NODENO를 번호 선택하기
	public static String updateInputNodeno() {
		ArrayList<BusInfo> imsiBusInfoList = selectBusIfo();
		printBusInfo(imsiBusInfoList);
		System.out.print("update NODENO>>");
		String data = sc.nextLine().replaceAll("[^0-9]", ""); // 0-9이외 문자는 모두 제거한다.
		return data;
	}

	// 버스정류장정보 수정하기
	public static void updateBusInfo(int data) {
		String sql = "update businfo set curdate = TO_CHAR(SYSDATE,'YY/MM/DD') WHERE NODENO = ?";
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DBUtil.makeConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, data);
			int value = pstmt.executeUpdate();

			if (value == 1) {
				System.out.println(data + " 수정완료");
			} else {
				System.out.println(data + " 수정실패");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 버스정류장정보 삭제하기
	public static void deleteBusInfo() {
		int count = getCountBusInfo();
		if (count == 0) {
			System.out.println("버스정보내용이 없습니다.");
			return;
		}
		String sql = "delete from businfo";
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DBUtil.makeConnection();
			pstmt = con.prepareStatement(sql);
			int value = pstmt.executeUpdate();
			if (value != 0) {
				System.out.println("버스정보 삭제완료");
			} else {
				System.out.println("모든 버스정보 삭제실패");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 버스정류장 DB 카디터리 수 도출하기
	public static int getCountBusInfo() {
		int count = 0;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DBUtil.makeConnection();
			String sql = "select count(*) cnt from businfo";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt("cnt");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

}
