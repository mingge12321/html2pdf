package tech.alei.html2pdf.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.alei.html2pdf.util.HtmlToPdfUtil;
import tech.alei.html2pdf.util.JsonToHtmlUtil;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ConvertController {

    /**
     * 首页测试
     * @return
     */
    @GetMapping("")
    public String index() {
        return "hello world";
    }

    /**
     * 根据json格式的数据和html模板内容，生成html
     * @param paramMap
     * @return html string
     */
    @PostMapping(value="/html", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String getHtml(@RequestBody HashMap<String, String> paramMap) {
        // 示例代码，可以删除
        String jsonData2 = JsonToHtmlUtil.getJsonFromFile();
        String template2 = JsonToHtmlUtil.getTemplateFromFile();
        String html2 = JsonToHtmlUtil.getHtmlFromString(jsonData2, template2);
        System.out.println("html2*******************************");
        System.out.println(html2);

        // 真实代码
//        String jsonData = paramMap.get("jsonData");
//        String template = paramMap.get("template");
//        String html = JsonToHtmlUtil.getHtmlFromString(jsonData, template);
//        System.out.println("html==========================");
//        System.out.println(html);

        return html2;
    }

    /**
     * 根据json格式的数据和html模板内容，生成pdf
     * @param paramMap
     * @param response
     * @return
     */
    @PostMapping(value="/pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void getPdf(@RequestBody HashMap<String, String> paramMap, HttpServletResponse response) {
        String jsonData = paramMap.get("jsonData");
        String template = paramMap.get("template");
        String html = JsonToHtmlUtil.getHtmlFromString(jsonData, template);

        String fileName = "C:\\space\\911test.pdf";
        HtmlToPdfUtil.toPdfFile(html, fileName);
        // TODO 返回的pdf打开显示空白，需修正
        HtmlToPdfUtil.toPdfResponse(html, response);
    }

    /**
     * 从MySQL数据库读取体检数据，批量生成PDF
     * @param limit 查询的记录数限制
     * @return 生成结果
     */
    @GetMapping("/medical-exam/generate-pdf")
    public Map<String, Object> generateMedicalExamPdf(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        List<String> generatedFiles = new ArrayList<>();
        
        // MySQL数据库连接信息
        String url = "jdbc:mysql://106.12.166.102:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String username = "ds";
        String password = "Yizhixingyun!123";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // 加载JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立数据库连接
            conn = DriverManager.getConnection(url, username, password);
            
            // 创建Statement对象
            stmt = conn.createStatement();
            
            // 查询指定数量的病人体检数据
            String sql = "SELECT * FROM medical_exam ORDER BY id LIMIT " + limit;
            rs = stmt.executeQuery(sql);
            
            ObjectMapper objectMapper = new ObjectMapper();
            int count = 0;
            
            // 轮询每个病人记录，生成PDF
            while (rs.next()) {
                // 构建体检数据的Map结构
                Map<String, Object> examData = new HashMap<>();
                
                // 基本信息
                examData.put("patientName", rs.getString("patient_name"));
                examData.put("gender", rs.getString("gender"));
                examData.put("age", rs.getInt("age"));
                examData.put("idCard", rs.getString("id_card"));
                examData.put("phone", rs.getString("phone"));
                examData.put("examDate", rs.getDate("exam_date") != null ? rs.getDate("exam_date").toString() : "");
                examData.put("hospital", rs.getString("hospital"));
                examData.put("department", rs.getString("department"));
                
                // 体格检查
                Map<String, Object> basicInfo = new HashMap<>();
                basicInfo.put("height", rs.getDouble("height"));
                basicInfo.put("weight", rs.getDouble("weight"));
                basicInfo.put("bmi", rs.getDouble("bmi"));
                basicInfo.put("bloodPressure", rs.getString("blood_pressure"));
                basicInfo.put("heartRate", rs.getInt("heart_rate"));
                examData.put("basicInfo", basicInfo);
                
                // 血常规检查
                Map<String, Object> bloodTest = new HashMap<>();
                bloodTest.put("whiteBloodCell", rs.getString("white_blood_cell"));
                bloodTest.put("redBloodCell", rs.getString("red_blood_cell"));
                bloodTest.put("hemoglobin", rs.getString("hemoglobin"));
                bloodTest.put("platelet", rs.getString("platelet"));
                examData.put("bloodTest", bloodTest);
                
                // 生化检查
                Map<String, Object> biochemicalTest = new HashMap<>();
                
                Map<String, Object> liverFunction = new HashMap<>();
                liverFunction.put("alt", rs.getString("alt"));
                liverFunction.put("ast", rs.getString("ast"));
                liverFunction.put("bilirubin", rs.getString("bilirubin"));
                biochemicalTest.put("liverFunction", liverFunction);
                
                Map<String, Object> kidneyFunction = new HashMap<>();
                kidneyFunction.put("creatinine", rs.getString("creatinine"));
                kidneyFunction.put("urea", rs.getString("urea"));
                kidneyFunction.put("uricAcid", rs.getString("uric_acid"));
                biochemicalTest.put("kidneyFunction", kidneyFunction);
                
                Map<String, Object> lipidProfile = new HashMap<>();
                lipidProfile.put("totalCholesterol", rs.getString("total_cholesterol"));
                lipidProfile.put("triglyceride", rs.getString("triglyceride"));
                lipidProfile.put("hdl", rs.getString("hdl"));
                lipidProfile.put("ldl", rs.getString("ldl"));
                biochemicalTest.put("lipidProfile", lipidProfile);
                
                examData.put("biochemicalTest", biochemicalTest);
                
                // 影像学检查
                Map<String, Object> imagingReport = new HashMap<>();
                imagingReport.put("chestXray", rs.getString("chest_xray"));
                imagingReport.put("ecg", rs.getString("ecg"));
                imagingReport.put("b超", rs.getString("b_ultrasound"));
                examData.put("imagingReport", imagingReport);
                
                // 医生信息
                examData.put("doctorAdvice", rs.getString("doctor_advice"));
                examData.put("doctorName", rs.getString("doctor_name"));
                examData.put("doctorTitle", rs.getString("doctor_title"));
                
                // 将Map转换为JSON字符串
                String jsonData = objectMapper.writeValueAsString(examData);
                
                // 使用模板生成HTML
                String html = JsonToHtmlUtil.getHtml(jsonData, "medical-exam");
                
                // 生成PDF文件名（包含病人姓名和ID）
                String fileName = "/Users/mac/Documents/gjsl/github/medical-exam-" + rs.getLong("id") + "-" + rs.getString("patient_name") + ".pdf";
                
                // 转换为PDF
                HtmlToPdfUtil.toPdfFile(html, fileName);
                
                generatedFiles.add(fileName);
                count++;
            }
            
            result.put("success", true);
            result.put("count", count);
            result.put("files", generatedFiles);
            result.put("message", "成功生成 " + count + " 个体检报告PDF");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "生成失败: " + e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }

    /**
     * 从IRIS数据库读取体检数据，批量生成PDF
     * @param limit 查询的记录数限制
     * @return 生成结果
     */
    @GetMapping("/medical-exam/generate-pdf-iris")
    public Map<String, Object> generateMedicalExamPdfFromIris(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        List<String> generatedFiles = new ArrayList<>();
        
        // IRIS数据库连接信息（请根据实际情况修改）
        String url = "jdbc:IRIS://localhost:1972/USER";
        String username = "SuperUser";
        String password = "SYS";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // 加载IRIS JDBC驱动
            Class.forName("com.intersystems.jdbc.IRISDriver");
            
            // 建立数据库连接
            conn = DriverManager.getConnection(url, username, password);
            
            // 创建Statement对象
            stmt = conn.createStatement();
            
            // 查询指定数量的病人体检数据
            String sql = "SELECT * FROM medical_exam ORDER BY id LIMIT " + limit;
            rs = stmt.executeQuery(sql);
            
            ObjectMapper objectMapper = new ObjectMapper();
            int count = 0;
            
            // 轮询每个病人记录，生成PDF
            while (rs.next()) {
                // 构建体检数据的Map结构
                Map<String, Object> examData = new HashMap<>();
                
                // 基本信息
                examData.put("patientName", rs.getString("patient_name"));
                examData.put("gender", rs.getString("gender"));
                examData.put("age", rs.getInt("age"));
                examData.put("idCard", rs.getString("id_card"));
                examData.put("phone", rs.getString("phone"));
                examData.put("examDate", rs.getDate("exam_date") != null ? rs.getDate("exam_date").toString() : "");
                examData.put("hospital", rs.getString("hospital"));
                examData.put("department", rs.getString("department"));
                
                // 体格检查
                Map<String, Object> basicInfo = new HashMap<>();
                basicInfo.put("height", rs.getDouble("height"));
                basicInfo.put("weight", rs.getDouble("weight"));
                basicInfo.put("bmi", rs.getDouble("bmi"));
                basicInfo.put("bloodPressure", rs.getString("blood_pressure"));
                basicInfo.put("heartRate", rs.getInt("heart_rate"));
                examData.put("basicInfo", basicInfo);
                
                // 血常规检查
                Map<String, Object> bloodTest = new HashMap<>();
                bloodTest.put("whiteBloodCell", rs.getString("white_blood_cell"));
                bloodTest.put("redBloodCell", rs.getString("red_blood_cell"));
                bloodTest.put("hemoglobin", rs.getString("hemoglobin"));
                bloodTest.put("platelet", rs.getString("platelet"));
                examData.put("bloodTest", bloodTest);
                
                // 生化检查
                Map<String, Object> biochemicalTest = new HashMap<>();
                
                Map<String, Object> liverFunction = new HashMap<>();
                liverFunction.put("alt", rs.getString("alt"));
                liverFunction.put("ast", rs.getString("ast"));
                liverFunction.put("bilirubin", rs.getString("bilirubin"));
                biochemicalTest.put("liverFunction", liverFunction);
                
                Map<String, Object> kidneyFunction = new HashMap<>();
                kidneyFunction.put("creatinine", rs.getString("creatinine"));
                kidneyFunction.put("urea", rs.getString("urea"));
                kidneyFunction.put("uricAcid", rs.getString("uric_acid"));
                biochemicalTest.put("kidneyFunction", kidneyFunction);
                
                Map<String, Object> lipidProfile = new HashMap<>();
                lipidProfile.put("totalCholesterol", rs.getString("total_cholesterol"));
                lipidProfile.put("triglyceride", rs.getString("triglyceride"));
                lipidProfile.put("hdl", rs.getString("hdl"));
                lipidProfile.put("ldl", rs.getString("ldl"));
                biochemicalTest.put("lipidProfile", lipidProfile);
                
                examData.put("biochemicalTest", biochemicalTest);
                
                // 影像学检查
                Map<String, Object> imagingReport = new HashMap<>();
                imagingReport.put("chestXray", rs.getString("chest_xray"));
                imagingReport.put("ecg", rs.getString("ecg"));
                imagingReport.put("b超", rs.getString("b_ultrasound"));
                examData.put("imagingReport", imagingReport);
                
                // 医生信息
                examData.put("doctorAdvice", rs.getString("doctor_advice"));
                examData.put("doctorName", rs.getString("doctor_name"));
                examData.put("doctorTitle", rs.getString("doctor_title"));
                
                // 将Map转换为JSON字符串
                String jsonData = objectMapper.writeValueAsString(examData);
                
                // 使用模板生成HTML
                String html = JsonToHtmlUtil.getHtml(jsonData, "medical-exam");
                
                // 生成PDF文件名（包含病人姓名和ID）
                String fileName = "/Users/mac/Documents/gjsl/github/medical-exam-iris-" + rs.getLong("id") + "-" + rs.getString("patient_name") + ".pdf";
                
                // 转换为PDF
                HtmlToPdfUtil.toPdfFile(html, fileName);
                
                generatedFiles.add(fileName);
                count++;
            }
            
            result.put("success", true);
            result.put("count", count);
            result.put("files", generatedFiles);
            result.put("message", "成功从IRIS数据库生成 " + count + " 个体检报告PDF");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "生成失败: " + e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }

    /**
     * 从MySQL数据库读取常州友谊医院体检数据，批量生成PDF（第2、11、12页）
     * @param limit 查询的记录数限制
     * @return 生成结果
     */
    @GetMapping("/medical-exam/friendship/generate-pdf")
    public Map<String, Object> generateFriendshipExamPdf(@RequestParam(defaultValue = "3") int limit) {
        Map<String, Object> result = new HashMap<>();
        List<String> generatedFiles = new ArrayList<>();
        
        // MySQL数据库连接信息
        String url = "jdbc:mysql://106.12.166.102:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String username = "ds";
        String password = "Zjw11763bigdata!";
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // 加载JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 建立数据库连接
            conn = DriverManager.getConnection(url, username, password);
            
            // 创建Statement对象
            stmt = conn.createStatement();
            
            // 查询指定数量的病人体检数据
            String sql = "SELECT * FROM friendship_medical_exam ORDER BY id LIMIT " + limit;
            rs = stmt.executeQuery(sql);
            
            ObjectMapper objectMapper = new ObjectMapper();
            int count = 0;
            
            // 轮询每个病人记录，生成PDF
            while (rs.next()) {
                // 构建体检数据的Map结构
                Map<String, Object> examData = new HashMap<>();
                
                // 基本信息
                examData.put("patientName", rs.getString("patient_name"));
                examData.put("gender", rs.getString("gender"));
                examData.put("age", rs.getInt("age"));
                examData.put("idCard", rs.getString("id_card"));
                examData.put("phone", rs.getString("phone"));
                examData.put("examDate", rs.getDate("exam_date") != null ? rs.getDate("exam_date").toString() : "");
                examData.put("hospital", rs.getString("hospital"));
                examData.put("department", rs.getString("department"));
                examData.put("reportNo", rs.getString("report_no"));
                
                // 第2页数据
                Map<String, Object> page2 = new HashMap<>();
                page2.put("title", "健康体检报告");
                
                Map<String, Object> personalInfo = new HashMap<>();
                personalInfo.put("name", rs.getString("patient_name"));
                personalInfo.put("gender", rs.getString("gender"));
                personalInfo.put("age", rs.getInt("age"));
                personalInfo.put("birthday", rs.getDate("birthday") != null ? rs.getDate("birthday").toString() : "");
                personalInfo.put("idCard", rs.getString("id_card"));
                personalInfo.put("phone", rs.getString("phone"));
                personalInfo.put("workUnit", rs.getString("work_unit"));
                personalInfo.put("occupation", rs.getString("occupation"));
                personalInfo.put("maritalStatus", rs.getString("marital_status"));
                personalInfo.put("address", rs.getString("address"));
                page2.put("personalInfo", personalInfo);
                
                Map<String, Object> examInfo = new HashMap<>();
                examInfo.put("examDate", rs.getDate("exam_date") != null ? rs.getDate("exam_date").toString() : "");
                examInfo.put("reportDate", rs.getDate("report_date") != null ? rs.getDate("report_date").toString() : "");
                examInfo.put("type", rs.getString("exam_type"));
                examInfo.put("doctor", rs.getString("doctor"));
                examInfo.put("reviewer", rs.getString("reviewer"));
                examInfo.put("printDate", rs.getDate("print_date") != null ? rs.getDate("print_date").toString() : "");
                page2.put("examInfo", examInfo);
                examData.put("page2", page2);
                
                // 第11页数据
                Map<String, Object> page11 = new HashMap<>();
                page11.put("title", "生化检查");
                
                // 肝功能
                Map<String, Object> liver = new HashMap<>();
                liver.put("title", "肝功能");
                List<Map<String, Object>> liverItems = new ArrayList<>();
                liverItems.add(createTestItem("谷丙转氨酶(ALT)", rs.getString("liver_alt"), "U/L", "0-40", "正常"));
                liverItems.add(createTestItem("谷草转氨酶(AST)", rs.getString("liver_ast"), "U/L", "0-40", "正常"));
                liverItems.add(createTestItem("总胆红素(TBIL)", rs.getString("liver_tbil"), "μmol/L", "3.4-17.1", "正常"));
                liverItems.add(createTestItem("直接胆红素(DBIL)", rs.getString("liver_dbil"), "μmol/L", "0-6.8", "正常"));
                liverItems.add(createTestItem("间接胆红素(IBIL)", rs.getString("liver_ibil"), "μmol/L", "0-14", "正常"));
                liverItems.add(createTestItem("总蛋白(TP)", rs.getString("liver_tp"), "g/L", "60-85", "正常"));
                liverItems.add(createTestItem("白蛋白(ALB)", rs.getString("liver_alb"), "g/L", "35-55", "正常"));
                liverItems.add(createTestItem("球蛋白(GLB)", rs.getString("liver_glb"), "g/L", "20-35", "正常"));
                liverItems.add(createTestItem("白球比(A/G)", rs.getString("liver_ag"), "", "1.5-2.5", "正常"));
                liver.put("items", liverItems);
                page11.put("liver", liver);
                
                // 肾功能
                Map<String, Object> kidney = new HashMap<>();
                kidney.put("title", "肾功能");
                List<Map<String, Object>> kidneyItems = new ArrayList<>();
                kidneyItems.add(createTestItem("尿素(BUN)", rs.getString("kidney_bun"), "mmol/L", "2.9-8.2", "正常"));
                kidneyItems.add(createTestItem("肌酐(CREA)", rs.getString("kidney_crea"), "μmol/L", "44-133", "正常"));
                kidneyItems.add(createTestItem("尿酸(UA)", rs.getString("kidney_ua"), "μmol/L", "208-428", "正常"));
                kidneyItems.add(createTestItem("胱抑素C(Cys-C)", rs.getString("kidney_cysc"), "mg/L", "0.51-1.09", "正常"));
                kidney.put("items", kidneyItems);
                page11.put("kidney", kidney);
                
                // 血脂
                Map<String, Object> lipid = new HashMap<>();
                lipid.put("title", "血脂");
                List<Map<String, Object>> lipidItems = new ArrayList<>();
                lipidItems.add(createTestItem("总胆固醇(TC)", rs.getString("lipid_tc"), "mmol/L", "0-5.7", "正常"));
                lipidItems.add(createTestItem("甘油三酯(TG)", rs.getString("lipid_tg"), "mmol/L", "0-1.7", "正常"));
                lipidItems.add(createTestItem("高密度脂蛋白胆固醇(HDL-C)", rs.getString("lipid_hdl"), "mmol/L", "1.03-1.55", "正常"));
                lipidItems.add(createTestItem("低密度脂蛋白胆固醇(LDL-C)", rs.getString("lipid_ldl"), "mmol/L", "0-3.37", "正常"));
                lipid.put("items", lipidItems);
                page11.put("lipid", lipid);
                
                // 血糖
                Map<String, Object> sugar = new HashMap<>();
                sugar.put("title", "血糖");
                List<Map<String, Object>> sugarItems = new ArrayList<>();
                sugarItems.add(createTestItem("空腹葡萄糖(GLU)", rs.getString("sugar_glu"), "mmol/L", "3.9-6.1", "正常"));
                sugar.put("items", sugarItems);
                page11.put("sugar", sugar);
                examData.put("page11", page11);
                
                // 第12页数据
                Map<String, Object> page12 = new HashMap<>();
                page12.put("title", "血常规及其他检查");
                
                // 血常规
                Map<String, Object> blood = new HashMap<>();
                blood.put("title", "血常规");
                List<Map<String, Object>> bloodItems = new ArrayList<>();
                bloodItems.add(createTestItem("白细胞计数(WBC)", rs.getString("blood_wbc"), "×10⁹/L", "4-10", "正常"));
                bloodItems.add(createTestItem("红细胞计数(RBC)", rs.getString("blood_rbc"), "×10¹²/L", "4.0-5.5", "正常"));
                bloodItems.add(createTestItem("血红蛋白(HGB)", rs.getString("blood_hgb"), "g/L", "120-160", "正常"));
                bloodItems.add(createTestItem("红细胞压积(HCT)", rs.getString("blood_hct"), "L/L", "0.40-0.50", "正常"));
                bloodItems.add(createTestItem("平均红细胞体积(MCV)", rs.getString("blood_mcv"), "fL", "80-100", "正常"));
                bloodItems.add(createTestItem("平均红细胞血红蛋白量(MCH)", rs.getString("blood_mch"), "pg", "27-34", "正常"));
                bloodItems.add(createTestItem("平均红细胞血红蛋白浓度(MCHC)", rs.getString("blood_mchc"), "g/L", "320-360", "正常"));
                bloodItems.add(createTestItem("红细胞分布宽度(RDW)", rs.getString("blood_rdw"), "%", "11-16", "正常"));
                bloodItems.add(createTestItem("血小板计数(PLT)", rs.getString("blood_plt"), "×10⁹/L", "100-300", "正常"));
                bloodItems.add(createTestItem("淋巴细胞百分数(LY)", rs.getString("blood_ly_pct"), "%", "20-40", "正常"));
                bloodItems.add(createTestItem("淋巴细胞绝对值(LY#)", rs.getString("blood_ly_abs"), "×10⁹/L", "0.8-4", "正常"));
                bloodItems.add(createTestItem("中性粒细胞百分数(NE)", rs.getString("blood_ne_pct"), "%", "50-70", "正常"));
                bloodItems.add(createTestItem("中性粒细胞绝对值(NE#)", rs.getString("blood_ne_abs"), "×10⁹/L", "2-7", "正常"));
                bloodItems.add(createTestItem("单核细胞百分数(MO)", rs.getString("blood_mo_pct"), "%", "3-10", "正常"));
                bloodItems.add(createTestItem("单核细胞绝对值(MO#)", rs.getString("blood_mo_abs"), "×10⁹/L", "0.12-1", "正常"));
                bloodItems.add(createTestItem("嗜酸性粒细胞百分数(EO)", rs.getString("blood_eo_pct"), "%", "0.5-5", "正常"));
                bloodItems.add(createTestItem("嗜酸性粒细胞绝对值(EO#)", rs.getString("blood_eo_abs"), "×10⁹/L", "0.02-0.5", "正常"));
                bloodItems.add(createTestItem("嗜碱性粒细胞百分数(BA)", rs.getString("blood_ba_pct"), "%", "0-1", "正常"));
                bloodItems.add(createTestItem("嗜碱性粒细胞绝对值(BA#)", rs.getString("blood_ba_abs"), "×10⁹/L", "0-0.1", "正常"));
                blood.put("items", bloodItems);
                page12.put("blood", blood);
                
                // 尿常规
                Map<String, Object> urine = new HashMap<>();
                urine.put("title", "尿常规");
                List<Map<String, Object>> urineItems = new ArrayList<>();
                urineItems.add(createTestItem("颜色", rs.getString("urine_color"), "", "淡黄色", "正常"));
                urineItems.add(createTestItem("透明度", rs.getString("urine_clarity"), "", "透明", "正常"));
                urineItems.add(createTestItem("酸碱度(pH)", rs.getString("urine_ph"), "", "5.4-8.4", "正常"));
                urineItems.add(createTestItem("比重(SG)", rs.getString("urine_sg"), "", "1.003-1.030", "正常"));
                urineItems.add(createTestItem("白细胞(WBC)", rs.getString("urine_wbc"), "", "阴性", "正常"));
                urineItems.add(createTestItem("隐血(BLD)", rs.getString("urine_bld"), "", "阴性", "正常"));
                urineItems.add(createTestItem("蛋白质(PRO)", rs.getString("urine_pro"), "", "阴性", "正常"));
                urineItems.add(createTestItem("葡萄糖(GLU)", rs.getString("urine_glu"), "", "阴性", "正常"));
                urineItems.add(createTestItem("酮体(KET)", rs.getString("urine_ket"), "", "阴性", "正常"));
                urineItems.add(createTestItem("胆红素(BIL)", rs.getString("urine_bil"), "", "阴性", "正常"));
                urineItems.add(createTestItem("尿胆原(URO)", rs.getString("urine_uro"), "", "正常", "正常"));
                urineItems.add(createTestItem("亚硝酸盐(NIT)", rs.getString("urine_nit"), "", "阴性", "正常"));
                urineItems.add(createTestItem("维生素C(VC)", rs.getString("urine_vc"), "", "阴性", "正常"));
                urineItems.add(createTestItem("镜检白细胞", rs.getString("urine_micro_wbc"), "/HP", "0-5", "正常"));
                urineItems.add(createTestItem("镜检红细胞", rs.getString("urine_micro_rbc"), "/HP", "0-3", "正常"));
                urineItems.add(createTestItem("镜检上皮细胞", rs.getString("urine_micro_epi"), "", "少量", "正常"));
                urineItems.add(createTestItem("镜检管型", rs.getString("urine_micro_cast"), "", "无", "正常"));
                urineItems.add(createTestItem("镜检结晶", rs.getString("urine_micro_crystal"), "", "无", "正常"));
                urine.put("items", urineItems);
                page12.put("urine", urine);
                examData.put("page12", page12);
                
                // 将Map转换为JSON字符串
                String jsonData = objectMapper.writeValueAsString(examData);
                
                // 使用模板生成HTML
                String html = JsonToHtmlUtil.getHtml(jsonData, "medical-exam-friendship");
                
                // 生成PDF文件名（包含病人姓名和ID）
                String fileName = "/Users/mac/Documents/gjsl/github/friendship-exam-" + rs.getLong("id") + "-" + rs.getString("patient_name") + ".pdf";
                
                // 转换为PDF
                HtmlToPdfUtil.toPdfFile(html, fileName);
                
                generatedFiles.add(fileName);
                count++;
            }
            
            result.put("success", true);
            result.put("count", count);
            result.put("files", generatedFiles);
            result.put("message", "成功从MySQL数据库生成 " + count + " 份常州友谊医院体检报告PDF");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "生成失败: " + e.getMessage());
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    private Map<String, Object> createTestItem(String name, String result, String unit, String refRange, String status) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("result", result);
        item.put("unit", unit);
        item.put("refRange", refRange);
        item.put("status", status);
        return item;
    }
}
