

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/qout_info")
public class qout_info extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String ReqType = request.getParameter("Req");
			String connStr = "jdbc:postgresql://localhost:5432/qit";
			String user = "qit_admin";
			String pass = "Qwer1234";
			String strSQL = null;
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			
			JSONArray jsonArray = new JSONArray();
			JSONObject dataObj = new JSONObject();
			String ErrMsg = "";
			int ErrCode = 0;
			int Rows = 0;
			String Contact = "";
			
			try {
				Class.forName("org.postgresql.Driver");
				conn = DriverManager.getConnection(connStr, user, pass);
			}catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getClass().getName()+": "+e.getMessage());
			}
			
			if (conn != null) {
				switch (ReqType){
				case "branch": 
					String svName = request.getParameter("Service");
					if(request.getParameterMap().containsKey("PhoneNo")){
						Contact = request.getParameter("PhoneNo");
			    	}
					
					strSQL = "SELECT b_name, b_latitude, b_longtitude, count(m_qnumber) AS b_count "
							+ "FROM (SELECT DISTINCT b_name, b_latitude, b_longtitude, sv_prefix "
							+ "FROM qit_branch qb INNER JOIN qit_service qs ON qb.b_id = qs.b_id  "
							+ "WHERE qs.sv_name = '" + svName + "' "
							+ "AND qb.b_name NOT IN (SELECT m_branch FROM qit_main WHERE m_contact = '" + Contact + "' "
							+ "AND m_status = 'Pending' )) AS qbs "
							+ "LEFT JOIN  qit_main qm ON qbs.b_name = qm.m_branch AND qbs.sv_prefix = qm.m_prefix AND qm.m_status = 'Pending'"
							+ "GROUP BY qbs.b_name, qbs.b_latitude, qbs.b_longtitude "
							+ "ORDER BY qbs.b_name ";
					
					try{
						stmt = conn.createStatement();
						rs = stmt.executeQuery(strSQL);
						
						while (rs.next()) {
							Rows = rs.getRow();
							int total_cols = rs.getMetaData().getColumnCount();
				            JSONObject jsonObj = new JSONObject();
				            for (int i = 0; i < total_cols; i++) {
				            	jsonObj.put(rs.getMetaData().getColumnLabel(i + 1)
				                        .toLowerCase(), rs.getString(i+1));
				            }
				            jsonArray.put(jsonObj);
						}
						
						rs.close();
						stmt.close();
						conn.close();
					}catch (Exception e){
						ErrCode = 97;
						ErrMsg = "Server Error";
						e.printStackTrace();
						System.err.println(e.getClass().getName()+": "+e.getMessage());
					}
					//if (Rows == 0 && ErrCode == 0) {
					//	ErrCode = 1;
					//	ErrMsg = "No Record";
					//}
					//dataObj.put("data", jsonArray);
					break;
				case "service": 
					strSQL = "SELECT DISTINCT sv_name, sv_order "
							+ "FROM qit_service "
							+ "ORDER BY sv_order ";
					
					try{
						stmt = conn.createStatement();
						rs = stmt.executeQuery(strSQL);
						
						while (rs.next()) {
							Rows = rs.getRow();
							int total_cols = rs.getMetaData().getColumnCount();
				            JSONObject jsonObj = new JSONObject();
				            for (int i = 0; i < total_cols; i++) {
				            	jsonObj.put(rs.getMetaData().getColumnLabel(i + 1)
				                        .toLowerCase(), rs.getString(i+1));
				            }
				            jsonArray.put(jsonObj);
						}
						
						rs.close();
						stmt.close();
						conn.close();
					}catch (Exception e){
						ErrCode = 97;
						ErrMsg = "Server Error";
						e.printStackTrace();
						System.err.println(e.getClass().getName()+": "+e.getMessage());
					}
					//if (Rows == 0 && ErrCode == 0) {
					//	ErrCode = 1;
					//	ErrMsg = "No Record";
					//}
					//dataObj.put("data", jsonArray);
					break;
				case "ticket": 
					if(request.getParameterMap().containsKey("PhoneNo")){
						Contact = request.getParameter("PhoneNo");
			    	}
					
					strSQL = "SELECT CASE WHEN m_prefix = 6 THEN 'Counter 6' "
							+ "ELSE m_qnumber::varchar "
							+ "END AS qno, "
							+ "m_service, m_branch, "
							+ "TO_CHAR(m_datetime,'DD/MM/YYYY') AS m_date, "
							+ "TO_CHAR(m_datetime,'hh12:mi:ss AM') AS m_time "
							+ "FROM qit_main "
							+ "WHERE m_contact = '" + Contact + "' "
							+ "AND m_status = 'Pending' "
							+ "ORDER BY m_time ASC ";
					try{
						stmt = conn.createStatement();
						rs = stmt.executeQuery(strSQL);
						
						while (rs.next()) {
							Rows = rs.getRow();
							int total_cols = rs.getMetaData().getColumnCount();
				            JSONObject jsonObj = new JSONObject();
				            for (int i = 0; i < total_cols; i++) {
				            	jsonObj.put(rs.getMetaData().getColumnLabel(i + 1)
				                        .toLowerCase(), rs.getString(i+1));
				            }
				            jsonArray.put(jsonObj);
						}
						
						rs.close();
						stmt.close();
						conn.close();
					}catch (Exception e){
						ErrCode = 97;
						ErrMsg = "Server Error";
						e.printStackTrace();
						System.err.println(e.getClass().getName()+": "+e.getMessage());
					}
					break;
				case "count": 
					String Branch = "";
					String Service = "";
					String Qno = "";
					if(request.getParameterMap().containsKey("Branch")){
						Branch = request.getParameter("Branch");
			    	}	
					if(request.getParameterMap().containsKey("Service")){
						Service = request.getParameter("Service");
			    	}
					if(request.getParameterMap().containsKey("Qno")){
						Qno = request.getParameter("Qno");
			    	}	
					
					strSQL = "SELECT count(m_qnumber) AS b_count, m_branch FROM qit_main "
							+ "WHERE m_status = 'Pending' ";
					if(!Branch.equals("")) { 
						strSQL = strSQL + "AND m_branch = '" + Branch + "'";
					}
					if(!Service.equals("")) { 
						strSQL = strSQL + "AND m_prefix = (SELECT sv_prefix FROM "
								+ "qit_service WHERE sv_name = '" + Service + "' "
								+ "GROUP BY sv_prefix ) ";
					}
					if(!Qno.equals("")) { 
						strSQL = strSQL + "AND m_qnumber < " + Qno + " "
								+ "AND m_prefix = LEFT('" + Qno + "',1)::smallint ";
					}
					strSQL = strSQL + "GROUP BY m_branch "
							+ "ORDER BY m_branch ";

					try{
						stmt = conn.createStatement();
						rs = stmt.executeQuery(strSQL);
						
						while (rs.next()) {
							Rows = rs.getRow();
							int total_cols = rs.getMetaData().getColumnCount();
				            JSONObject jsonObj = new JSONObject();
				            for (int i = 0; i < total_cols; i++) {
				            	jsonObj.put(rs.getMetaData().getColumnLabel(i + 1)
				                        .toLowerCase(), rs.getString(i+1));
				            }
				            jsonArray.put(jsonObj);
						}
						
						rs.close();
						stmt.close();
						conn.close();
					}catch (Exception e){
						ErrCode = 97;
						ErrMsg = "Server Error";
						e.printStackTrace();
						System.err.println(e.getClass().getName()+": "+e.getMessage());
					}
					break;
				}
			}
			if (Rows > 0) {
				dataObj.put("data", jsonArray);
			}
			//dataObj.put("Rows", Rows);
			//dataObj.put("ErrCode", ErrCode);
			//dataObj.put("ErrMsg", ErrMsg);
			//dataObj.put("data", jsonArray);
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(dataObj);
		}catch (Exception e) {
			String ErrMsg = "Server Error";
			int ErrCode = 99;
			//JSONArray jsonArray = new JSONArray();
			JSONObject ParentObj = new JSONObject();
			
			ParentObj.put("ErrCode", ErrCode);
			ParentObj.put("ErrMsg", ErrMsg);
			//jsonArray.put(ParentObj);
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(ParentObj);
			System.err.println("Server Error: gInfo API executed with NULL pointer.");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
