

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

@WebServlet("/qin_info")
public class qin_info extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String ReqType = request.getParameter("Req");
			String connStr = "jdbc:postgresql://localhost:5432/qit";
			String user = "qit_admin";
			String pass = "Qwer1234";
			String strSQL = null;
			Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			ResultSet rs2 = null;
			
			JSONArray jsonArray = new JSONArray();
			JSONObject dataObj = new JSONObject();
			String ErrMsg = "";
			int ErrCode = 0;
			String Branch="";
			String Service="";
			String Contact="";
			String Desc="";
			String Qno="";
			
			try {
				Class.forName("org.postgresql.Driver");
				conn = DriverManager.getConnection(connStr, user, pass);
			}catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getClass().getName()+": "+e.getMessage());
			}
			
			if (conn != null) {
				switch (ReqType){
					case "queuein": 				
						if(request.getParameterMap().containsKey("Branch")){
							Branch = request.getParameter("Branch");
				    	}	
						
						if(request.getParameterMap().containsKey("Service")){
							Service = request.getParameter("Service");
				    	}	
						if(request.getParameterMap().containsKey("PhoneNo")){
							Contact = request.getParameter("PhoneNo");
				    	}	
						if(request.getParameterMap().containsKey("Desc")){
							Desc = request.getParameter("Desc");
				    	}	
						
						strSQL = "SELECT m_qnumber FROM qit_main "
								+ "WHERE m_branch = '" + Branch + "' "
								+ "AND m_contact = '" + Contact +"' "
								+ "AND m_status = 'Pending' ";

						try{
							stmt = conn.createStatement();
							rs = stmt.executeQuery(strSQL);
							
							if (rs.next()){
								dataObj.put("data", "-");
								dataObj.put("result", "Fail");
							}else {
								strSQL = "INSERT INTO qit_main ( " 
										+ "SELECT DISTINCT CASE WHEN m_qnumber = 1 THEN (sv_prefix * 1000)+ m_qnumber ELSE m_qnumber END AS m_qnumber, "
										+ "sv_prefix, '" + Service + "', '" + Branch + "', '" + Contact + "', '" + Desc + "',"
										+ "TO_CHAR(NOW(), 'DD MON YYYY hh24:mi:ss')::timestamp, 0, 'Pending' " 
										+ "FROM (SELECT CASE WHEN MAX(m_qnumber) > 0 AND TO_CHAR(MAX(m_datetime),'DD/MM/YYYY') = TO_CHAR(NOW(),'DD/MM/YYYY') "
										+ "THEN MAX(m_qnumber)+1 " 
										+ "ELSE 1 " 
										+ "END AS m_qnumber " 
										+ "FROM qit_main "
										+ "WHERE m_branch = '" + Branch + "' "
										+ "AND m_prefix = ( "
										+ "	SELECT sv_prefix "
										+ "	FROM qit_service qs, qit_branch qb "
										+ "	WHERE qs.b_id = qb.b_id "
										+ "	AND sv_name = '" + Service + "' "
										+ "	AND b_name = '" + Branch + "') "
										+ ") qb, qit_service qs "
										+ "WHERE qs.sv_name = '" + Service + "')";
								stmt.executeUpdate(strSQL);
								
//								strSQL = "UPDATE qit_service qs "
//										+ "SET sv_counter = ( "
//										+ "	SELECT CASE WHEN MAX(sv_counter) > 0 AND sv_date = TO_CHAR(now(),'DD/MM/YYYY')::date "
//										+ "	THEN MAX(sv_counter) + 1 "
//										+ "	ELSE 1 "
//										+ "	END "
//										+ "	FROM qit_service qs, qit_branch qb "
//										+ "	WHERE qs.sv_name = '" + Service + "' "
//										+ "	AND qb.b_name = '" + Branch + "' "
//										+ "	AND qs.b_id = qb.b_id "
//										+ "	GROUP BY qs.sv_prefix, qs.sv_date), "
//										+ "	sv_date = TO_CHAR(now(),'DD/MM/YYYY')::date "
//										+ "FROM qit_branch qb "
//										+ "WHERE qs.sv_name = '" + Service + "' "
//										+ "AND qb.b_name = '" + Branch + "' "
//										+ "AND qs.b_id = qb.b_id ";
//								stmt.executeUpdate(strSQL);		

								strSQL = "SELECT CASE WHEN m_prefix = 6 THEN 'Counter 6' "
										+ "ELSE m_qnumber::varchar "
										+ "END AS qno "
										+ "FROM qit_main "
										+ "WHERE m_service = '" + Service + "' "
										+ "AND m_branch = '" + Branch + "' "
										+ "AND m_contact = '" + Contact +"' "
										+ "AND m_status = 'Pending' ";
								rs2 = stmt.executeQuery(strSQL);
								
								if (rs2.next()){
									dataObj.put("data", rs2.getString("qno"));
									dataObj.put("result", "Success");
								}else{
									dataObj.put("data", "-");
									dataObj.put("result", "Fail");
								}
								rs2.close();
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
					case "queuecheck":
						if(request.getParameterMap().containsKey("Branch")){
							Branch = request.getParameter("Branch");
				    	}	
						if(request.getParameterMap().containsKey("Service")){
							Service = request.getParameter("Service");
				    	}	
						if(request.getParameterMap().containsKey("PhoneNo")){
							Contact = request.getParameter("PhoneNo");
				    	}	
						if(request.getParameterMap().containsKey("Desc")){
							Desc = request.getParameter("Desc");
				    	}
						
						strSQL = "SELECT m_qnumber AS qno FROM qit_main "
								+ "WHERE m_service = '" + Service + "' "
								+ "AND m_branch = '" + Branch + "' "
								+ "AND m_contact = '" + Contact +"'";
						
						try{
							stmt = conn.createStatement();
							rs = stmt.executeQuery(strSQL);
							
							if (rs.next()){
								dataObj.put("data", rs.getString("qno"));
								dataObj.put("result", "Success");
							}else {
								dataObj.put("result", "Fail");
							}
						}catch (Exception e){
							ErrCode = 97;
							ErrMsg = "Server Error";
							e.printStackTrace();
							System.err.println(e.getClass().getName()+": "+e.getMessage());
						}
						break;
					case "queueout":
						if(request.getParameterMap().containsKey("Qin")){
							Qno = request.getParameter("Qin");
				    	}
						
						strSQL = "SELECT m_qnumber FROM qit_main "
								+ "WHERE m_qnumber = " + Qno;
						System.out.println(strSQL);
						
						try{
							stmt = conn.createStatement();
							rs = stmt.executeQuery(strSQL);
							
							if (rs.next()){
//								strSQL = "INSERT INTO qit_sub "
//										+ "SELECT m_qnumber,m_prefix,m_service,m_branch,m_contact,m_remark,m_datetime,m_status,m_flag FROM qit_main "
//										+ "WHERE (m_prefix * 1000) + m_qnumber = " + Qno;								
//								stmt.executeUpdate(strSQL);
								
//								strSQL = "DELETE FROM qit_main "
//										+ "WHERE (m_prefix * 1000) + m_qnumber = " + Qno;			
								strSQL = "UPDATE qit_main "
										+ "SET m_status = 'Cancelled' "
										+ "WHERE m_qnumber = " + Qno;
								stmt.executeUpdate(strSQL);
								
								dataObj.put("data", Qno);
								dataObj.put("result", "Success");
							}else {
								dataObj.put("result", "Fail");
							}
						}catch (Exception e){
							ErrCode = 97;
							ErrMsg = "Server Error";
							e.printStackTrace();
							System.err.println(e.getClass().getName()+": "+e.getMessage());
						}				
						break;
					case "queueclear":
						strSQL = "INSERT INTO qit_sub ( SELECT * FROM qit_main "
								+ "WHERE NOT(TO_CHAR(m_datetime, 'DD/MM/YYYY') = TO_CHAR(NOW(), 'DD/MM/YYYY')))";
						try{
							stmt = conn.createStatement();
							stmt.executeUpdate(strSQL);
							
							dataObj.put("result", "Success");		
						}catch (Exception e){
							ErrCode = 97;
							ErrMsg = "Server Error";
							e.printStackTrace();
							System.err.println(e.getClass().getName()+": "+e.getMessage());
						}	
						break;
				}
			}
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

}
