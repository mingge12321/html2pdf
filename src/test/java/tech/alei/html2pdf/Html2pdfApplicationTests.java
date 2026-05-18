package tech.alei.html2pdf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import tech.alei.html2pdf.util.HtmlToPdfUtil;
import tech.alei.html2pdf.util.JsonToHtmlUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Html2pdfApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void getJson() {
	    String text = "";
        Resource resource = new ClassPathResource("origin-flight-query.json");
        File file = null;
        try {
            file = resource.getFile();
            text = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(text);
    }

	@Test
	public void toHtml() {
        String jsonData = JsonToHtmlUtil.getJsonFromFile();
        String html = JsonToHtmlUtil.getHtml(jsonData, "origin-flight-query");
        System.out.println(html);
    }

	@Test
	public void toHtmlFromString() {
        String jsonData = JsonToHtmlUtil.getJsonFromFile();
        String template = JsonToHtmlUtil.getTemplateFromFile();
        String html = JsonToHtmlUtil.getHtmlFromString(jsonData, template);

        System.out.println(html);
    }

	@Test
	public void toPdf() {
        String jsonData = JsonToHtmlUtil.getJsonFromFile();
        String html = JsonToHtmlUtil.getHtml(jsonData, "origin-flight-query");

        // 需要修改成本机的路径
//        String fileName = "C:\\space\\911test.pdf";
        String fileName = "/Users/mac/Documents/gjsl/github/11.pdf";
        HtmlToPdfUtil.toPdfFile(html, fileName);

    }

	@Test
	public void toMedicalExamPdf() {
        String jsonData = "";
        Resource resource = new ClassPathResource("medical-exam.json");
        File file = null;
        try {
            file = resource.getFile();
            jsonData = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String html = JsonToHtmlUtil.getHtml(jsonData, "medical-exam");
        
        String fileName = "/Users/mac/Documents/gjsl/github/medical-exam2.pdf";
        HtmlToPdfUtil.toPdfFile(html, fileName);
    }

	@Test
	public void generatePdfFromMysql() {
        // MySQL数据库连接信息
        String url = "jdbc:mysql://106.12.166.102:3306/open_user?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
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
            System.out.println("数据库连接成功");
            
            // 创建Statement对象
            stmt = conn.createStatement();
            
            // 查询所有病人体检数据
            String sql = "SELECT * FROM medical_exam";
            rs = stmt.executeQuery(sql);
            
            ObjectMapper objectMapper = new ObjectMapper();
            
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
                
                System.out.println("已生成PDF: " + fileName);
            }
            
            System.out.println("所有体检报告PDF生成完成");
            
        } catch (Exception e) {
            e.printStackTrace();
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
    }

    @Test
    public void generatePdfFromMysqlFriendship() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            // 准备3个患者的体检数据
            List<Map<String, Object>> patientsData = new ArrayList<>();
            
            // 患者1 - 张三
            Map<String, Object> patient1 = new HashMap<>();
            patient1.put("hospitalName", "复旦大学附属中山医院");
            patient1.put("healthCenter", "健康管理中心");
            patient1.put("examNo", "202211250001");
            patient1.put("examArchiveNo", "DA20221125001");
            patient1.put("examDate", "2022-11-25");
            patient1.put("totalPages", 25);
            patient1.put("patientName", "张三");
            patient1.put("gender", "男");
            patient1.put("age", 52);
            patient1.put("birthday", "1970-05-15");
            patient1.put("idCard", "310101197005151234");
            patient1.put("phone", "13800138000");
            patient1.put("workUnit", "某某科技有限公司");
            patient1.put("examAddress", "上海市松江区龙泉山南路137弄");
            
            // 主检结论
            List<Map<String, String>> conclusions1 = new ArrayList<>();
            Map<String, String> c1 = new HashMap<>();
            c1.put("title", "低钾血症（血钾测定：3.0mmol/L）");
            c1.put("content", "立即门诊就诊复查血钾，必要时口服补钾药物。建议增加含钾食物摄入（如黑木耳、菌菇类、香蕉等），随访血电解质。");
            conclusions1.add(c1);
            Map<String, String> c2 = new HashMap<>();
            c2.put("title", "右上肺小结节（右上肺胸膜下微小淡薄结节，小于0.5cm，较前（18-08-30）相仿）");
            c2.put("content", "年度随访肺小结节CT。避免吸烟及吸烟环境，防止上呼吸道感染。");
            conclusions1.add(c2);
            Map<String, String> c3 = new HashMap<>();
            c3.put("title", "结肠息肉（距肛缘50cm可见一0.3cm息肉），咬除术后");
            c3.put("content", "病理诊断：（距肛缘50cm）管状腺瘤伴上皮内瘤变低级别；近期进软食，忌辛辣、刺激性食物。注意大便颜色，若有腹痛、便血或黑便等，请及时就诊。内镜门诊随访，9-12个月复查肠镜。");
            conclusions1.add(c3);
            Map<String, String> c4 = new HashMap<>();
            c4.put("title", "慢性活动性胃炎（C-1）；幽门螺杆菌阳性");
            c4.put("content", "胃窦,慢性炎症:++,活动性:-,萎缩:-,肠化:-,异型增生:-；病理诊断：（胃窦）慢性非萎缩性胃炎；消化科门诊就诊，建议抗HP治疗。规律饮食，避免辛辣刺激食物，定期复查胃镜。");
            conclusions1.add(c4);
            Map<String, String> c5 = new HashMap<>();
            c5.put("title", "空腹血糖、糖化血红蛋白升高（空腹葡萄糖：6.4mmol/L；糖化血红蛋白：6.1%）");
            c5.put("content", "饮食控制，适度规律运动，维持健康体重。随访空腹血糖、餐后2小时血糖及糖化血红蛋白，必要时可行OGTT检查明确诊断。内分泌科门诊随访。");
            conclusions1.add(c5);
            Map<String, String> c6 = new HashMap<>();
            c6.put("title", "肿瘤标志物略增高（糖基抗原CA724：11.4U/ml）");
            c6.put("content", "1-3个月复查，如持续增高，建议进一步检查。");
            conclusions1.add(c6);
            Map<String, String> c7 = new HashMap<>();
            c7.put("title", "高血压病（药物治疗中）");
            c7.put("content", "心电图：窦性心动过缓；V2 V3 导联r波递增不显著，低一肋未见明显改变；T波改变（T波在Ⅱ Ⅲ aVF导联低平、浅倒置）；低盐饮食，适量运动，控制体重，劳逸结合。继续降压药物治疗，监测血压，建议控制血压在140/90mmHG以下，心内科门诊随访。");
            conclusions1.add(c7);
            patient1.put("mainConclusions", conclusions1);
            
            // 一般检查
            patient1.put("systolicPressure1", "144mmHg");
            patient1.put("diastolicPressure1", "92mmHg");
            patient1.put("bpConclusion1", "血压偏高");
            patient1.put("systolicPressure2", "130mmHg");
            patient1.put("diastolicPressure2", "88mmHg");
            patient1.put("bpConclusion2", "正常血压");
            patient1.put("waistCircumference", "96.00cm");
            patient1.put("hipCircumference", "106.00cm");
            patient1.put("waistHipRatio", "0.91");
            patient1.put("height", "175.0cm");
            patient1.put("weight", "84.7kg");
            patient1.put("bmi", "27.7,超重");
            patient1.put("generalExamSummary", "1.收缩压(第一次)：144mmHg\n2.舒张压(第一次)：92mmHg\n3.血压结论(第一次)：血压偏高\n4.收缩压(第二次)：130mmHg\n5.舒张压(第二次)：88mmHg\n6.血压结论(第二次)：正常血压\n7.腰围：96.00cm\n8.臀围：106.00cm\n9.腰臀比：0.91\n10.身高：175.0cm\n11.体重：84.7kg\n12.体重指数：27.7,超重");
            patient1.put("generalExamDoctor", "吴梦圆");
            patient1.put("generalExamReviewer", "吴梦圆");
            
            // 内科
            patient1.put("heartRhythm", "齐");
            patient1.put("heartRate", "80次/分");
            patient1.put("heartSound", "未见明显异常");
            patient1.put("heartMurmur", "未闻及病理性杂音");
            patient1.put("lungAuscultation", "双肺呼吸音清");
            patient1.put("abdomenPalpation", "平软无压痛");
            patient1.put("liver", "未触及");
            patient1.put("gallbladder", "未触及");
            patient1.put("spleen", "未触及");
            patient1.put("kidney", "双肾未触及，肾区无叩痛");
            patient1.put("internalMedicineHistory", "既往高血压史，药物治疗。2021.3右侧面瘫，已治愈。血糖偏高，未用药。");
            patient1.put("personalHistory", "2022.8 右侧鼻窦炎手术史。");
            patient1.put("internalMedicineSummary", "1.既往高血压史，药物治疗。2021.3右侧面瘫，已治愈。血糖偏高，未用药。\n2.个人史：2022.8 右侧鼻窦炎手术史。");
            patient1.put("internalMedicineDoctor", "张丽杨");
            patient1.put("internalMedicineReviewer", "张丽杨");
            patient1.put("internalMedicineReviewerSignature", "张丽杨");
            
            // 外科
            patient1.put("thyroid", "未见明显异常");
            patient1.put("lymphNode", "未见明显异常");
            patient1.put("spineJoints", "未见明显异常");
            patient1.put("surgeryOther", "未见明显异常");
            patient1.put("surgerySummary", "外科未见明显异常");
            patient1.put("surgeryDoctor", "倪小健");
            patient1.put("surgeryReviewer", "倪小健");
            
            // 其他检查
            List<Map<String, String>> findings1 = new ArrayList<>();
            Map<String, String> f1 = new HashMap<>();
            f1.put("title", "C3-7椎间盘突出并椎管狭窄；C5-6椎间盘水平脊髓变性；颈椎退变");
            f1.put("content", "骨科门诊就诊，注意颈部姿势，避免长时间低头或伏案工作，适当颈部活动。");
            findings1.add(f1);
            Map<String, String> f2 = new HashMap<>();
            f2.put("title", "双眼视力下降");
            f2.put("content", "注意用眼卫生，避免用眼过度，阳光下配戴抗紫外线眼镜，劳逸结合。定期眼科检查。");
            findings1.add(f2);
            Map<String, String> f3 = new HashMap<>();
            f3.put("title", "尿白细胞计数增多（43/uL）");
            f3.put("content", "复查尿常规，可考虑中段尿培养、前列腺液检查，诊断明确后行治疗，泌尿科门诊随访。");
            findings1.add(f3);
            Map<String, String> f4 = new HashMap<>();
            f4.put("title", "骨钙素偏高");
            f4.put("content", "随访骨代谢指标。");
            findings1.add(f4);
            Map<String, String> f5 = new HashMap<>();
            f5.put("title", "免疫球蛋白E、免疫球蛋白G升高");
            f5.put("content", "建议行过敏原监测，随访免疫球蛋白。");
            findings1.add(f5);
            Map<String, String> f6 = new HashMap<>();
            f6.put("title", "白介素6偏高");
            f6.put("content", "建议合理饮食，适度运动，避免熬夜，保持健康规律的生活方式，随访免疫功能指标。");
            findings1.add(f6);
            patient1.put("otherFindings", findings1);
            patient1.put("chiefDoctor", "张丽杨");
            patient1.put("chiefDoctorSignature", "张丽杨");
            patient1.put("chiefReviewer", "周敬");
            patient1.put("chiefReviewerSignature", "周敬");
            
            patientsData.add(patient1);
            
            // 患者2 - 李四
            Map<String, Object> patient2 = new HashMap<>();
            patient2.put("hospitalName", "复旦大学附属中山医院");
            patient2.put("healthCenter", "健康管理中心");
            patient2.put("examNo", "202211260002");
            patient2.put("examArchiveNo", "DA20221126002");
            patient2.put("examDate", "2022-11-26");
            patient2.put("totalPages", 25);
            patient2.put("patientName", "李四");
            patient2.put("gender", "女");
            patient2.put("age", 45);
            patient2.put("birthday", "1977-08-20");
            patient2.put("idCard", "310101197708204321");
            patient2.put("phone", "13900139000");
            patient2.put("workUnit", "上海市某医院");
            patient2.put("examAddress", "上海市松江区龙泉山南路137弄");
            
            List<Map<String, String>> conclusions2 = new ArrayList<>();
            Map<String, String> c2_1 = new HashMap<>();
            c2_1.put("title", "轻度贫血");
            c2_1.put("content", "建议适当增加富含铁食物摄入（如瘦肉、猪肝等），随访血常规，必要时血液科门诊就诊。");
            conclusions2.add(c2_1);
            Map<String, String> c2_2 = new HashMap<>();
            c2_2.put("title", "双侧甲状腺结节");
            c2_2.put("content", "建议3-6个月复查甲状腺B超，内分泌科随访。");
            conclusions2.add(c2_2);
            patient2.put("mainConclusions", conclusions2);
            
            patient2.put("systolicPressure1", "118mmHg");
            patient2.put("diastolicPressure1", "76mmHg");
            patient2.put("bpConclusion1", "正常血压");
            patient2.put("systolicPressure2", "115mmHg");
            patient2.put("diastolicPressure2", "74mmHg");
            patient2.put("bpConclusion2", "正常血压");
            patient2.put("waistCircumference", "78.00cm");
            patient2.put("hipCircumference", "92.00cm");
            patient2.put("waistHipRatio", "0.85");
            patient2.put("height", "162.0cm");
            patient2.put("weight", "58.5kg");
            patient2.put("bmi", "22.3,正常");
            patient2.put("generalExamSummary", "1.收缩压(第一次)：118mmHg\n2.舒张压(第一次)：76mmHg\n3.血压结论(第一次)：正常血压\n4.收缩压(第二次)：115mmHg\n5.舒张压(第二次)：74mmHg\n6.血压结论(第二次)：正常血压\n7.腰围：78.00cm\n8.臀围：92.00cm\n9.腰臀比：0.85\n10.身高：162.0cm\n11.体重：58.5kg\n12.体重指数：22.3,正常");
            patient2.put("generalExamDoctor", "陈静");
            patient2.put("generalExamReviewer", "陈静");
            
            patient2.put("heartRhythm", "齐");
            patient2.put("heartRate", "76次/分");
            patient2.put("heartSound", "正常");
            patient2.put("heartMurmur", "未闻及杂音");
            patient2.put("lungAuscultation", "双肺呼吸音清");
            patient2.put("abdomenPalpation", "平软");
            patient2.put("liver", "未触及");
            patient2.put("gallbladder", "未触及");
            patient2.put("spleen", "未触及");
            patient2.put("kidney", "未触及");
            patient2.put("internalMedicineHistory", "无特殊病史");
            patient2.put("personalHistory", "无特殊个人史");
            patient2.put("internalMedicineSummary", "内科检查未见明显异常");
            patient2.put("internalMedicineDoctor", "李华");
            patient2.put("internalMedicineReviewer", "李华");
            patient2.put("internalMedicineReviewerSignature", "李华");
            
            patient2.put("thyroid", "未见明显异常");
            patient2.put("lymphNode", "未见明显异常");
            patient2.put("spineJoints", "未见明显异常");
            patient2.put("surgeryOther", "未见明显异常");
            patient2.put("surgerySummary", "外科检查未见明显异常");
            patient2.put("surgeryDoctor", "王芳");
            patient2.put("surgeryReviewer", "王芳");
            
            List<Map<String, String>> findings2 = new ArrayList<>();
            Map<String, String> f2_1 = new HashMap<>();
            f2_1.put("title", "双侧乳腺小叶增生");
            f2_1.put("content", "建议6个月复查乳腺B超，保持心情舒畅，乳腺外科随访。");
            findings2.add(f2_1);
            Map<String, String> f2_2 = new HashMap<>();
            f2_2.put("title", "妇科检查未见明显异常");
            f2_2.put("content", "定期妇科检查。");
            findings2.add(f2_2);
            patient2.put("otherFindings", findings2);
            patient2.put("chiefDoctor", "李华");
            patient2.put("chiefDoctorSignature", "李华");
            patient2.put("chiefReviewer", "王芳");
            patient2.put("chiefReviewerSignature", "王芳");
            
            patientsData.add(patient2);
            
            // 患者3 - 王五
            Map<String, Object> patient3 = new HashMap<>();
            patient3.put("hospitalName", "复旦大学附属中山医院");
            patient3.put("healthCenter", "健康管理中心");
            patient3.put("examNo", "202211270003");
            patient3.put("examArchiveNo", "DA20221127003");
            patient3.put("examDate", "2022-11-27");
            patient3.put("totalPages", 25);
            patient3.put("patientName", "王五");
            patient3.put("gender", "男");
            patient3.put("age", 35);
            patient3.put("birthday", "1987-03-10");
            patient3.put("idCard", "310101198703105678");
            patient3.put("phone", "13700137000");
            patient3.put("workUnit", "某银行");
            patient3.put("examAddress", "上海市松江区龙泉山南路137弄");
            
            List<Map<String, String>> conclusions3 = new ArrayList<>();
            Map<String, String> c3_1 = new HashMap<>();
            c3_1.put("title", "体重指数偏高");
            c3_1.put("content", "建议适当运动，控制饮食，减轻体重。");
            conclusions3.add(c3_1);
            Map<String, String> c3_2 = new HashMap<>();
            c3_2.put("title", "血脂轻度升高");
            c3_2.put("content", "建议低脂饮食，适量运动，3个月后复查血脂。");
            conclusions3.add(c3_2);
            patient3.put("mainConclusions", conclusions3);
            
            patient3.put("systolicPressure1", "128mmHg");
            patient3.put("diastolicPressure1", "82mmHg");
            patient3.put("bpConclusion1", "正常高值");
            patient3.put("systolicPressure2", "125mmHg");
            patient3.put("diastolicPressure2", "80mmHg");
            patient3.put("bpConclusion2", "正常血压");
            patient3.put("waistCircumference", "86.00cm");
            patient3.put("hipCircumference", "98.00cm");
            patient3.put("waistHipRatio", "0.88");
            patient3.put("height", "172.0cm");
            patient3.put("weight", "78.0kg");
            patient3.put("bmi", "26.4,偏胖");
            patient3.put("generalExamSummary", "1.收缩压(第一次)：128mmHg\n2.舒张压(第一次)：82mmHg\n3.血压结论(第一次)：正常高值\n4.收缩压(第二次)：125mmHg\n5.舒张压(第二次)：80mmHg\n6.血压结论(第二次)：正常血压\n7.腰围：86.00cm\n8.臀围：98.00cm\n9.腰臀比：0.88\n10.身高：172.0cm\n11.体重：78.0kg\n12.体重指数：26.4,偏胖");
            patient3.put("generalExamDoctor", "刘强");
            patient3.put("generalExamReviewer", "刘强");
            
            patient3.put("heartRhythm", "齐");
            patient3.put("heartRate", "72次/分");
            patient3.put("heartSound", "正常");
            patient3.put("heartMurmur", "未闻及杂音");
            patient3.put("lungAuscultation", "双肺呼吸音清");
            patient3.put("abdomenPalpation", "平软");
            patient3.put("liver", "未触及");
            patient3.put("gallbladder", "未触及");
            patient3.put("spleen", "未触及");
            patient3.put("kidney", "未触及");
            patient3.put("internalMedicineHistory", "体健，无特殊病史");
            patient3.put("personalHistory", "无特殊个人史");
            patient3.put("internalMedicineSummary", "内科检查未见明显异常");
            patient3.put("internalMedicineDoctor", "赵刚");
            patient3.put("internalMedicineReviewer", "赵刚");
            patient3.put("internalMedicineReviewerSignature", "赵刚");
            
            patient3.put("thyroid", "未见明显异常");
            patient3.put("lymphNode", "未见明显异常");
            patient3.put("spineJoints", "未见明显异常");
            patient3.put("surgeryOther", "未见明显异常");
            patient3.put("surgerySummary", "外科检查未见明显异常");
            patient3.put("surgeryDoctor", "孙丽");
            patient3.put("surgeryReviewer", "孙丽");
            
            List<Map<String, String>> findings3 = new ArrayList<>();
            Map<String, String> f3_1 = new HashMap<>();
            f3_1.put("title", "轻度脂肪肝");
            f3_1.put("content", "建议低脂饮食，适量运动，控制体重，定期复查肝脏B超。");
            findings3.add(f3_1);
            Map<String, String> f3_2 = new HashMap<>();
            f3_2.put("title", "双眼屈光不正");
            f3_2.put("content", "建议眼科就诊，必要时配镜。");
            findings3.add(f3_2);
            patient3.put("otherFindings", findings3);
            patient3.put("chiefDoctor", "赵刚");
            patient3.put("chiefDoctorSignature", "赵刚");
            patient3.put("chiefReviewer", "孙丽");
            patient3.put("chiefReviewerSignature", "孙丽");
            
            patientsData.add(patient3);
            
            // 生成PDF
            int index = 1;
            for (Map<String, Object> patientData : patientsData) {
                String jsonData = objectMapper.writeValueAsString(patientData);
                String html = JsonToHtmlUtil.getHtml(jsonData, "medical-exam-friendship");
                String fileName = "/Users/mac/Documents/gjsl/github/friendship-exam-" + index + "-" + patientData.get("patientName") + ".pdf";
                HtmlToPdfUtil.toPdfFile(html, fileName);
                System.out.println("已生成PDF: " + fileName);
                index++;
            }
            
            System.out.println("所有体检报告PDF生成完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String FRIENDSHIP_JDBC_URL =
            "jdbc:mysql://106.12.166.102:3306/demo?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    private static final String FRIENDSHIP_JDBC_USER = "ds";
    private static final String FRIENDSHIP_JDBC_PASSWORD = "Yizhixingyun!123";

    /** 基本信息：Adm 表 30-40 */
    private static final String SQL_ADM_INFO =
            "SELECT '北京xxx医院' AS hospital_name, a.PatSex AS gender, '50-60' AS age, "
                    + "a.PatName AS patient_name, DATE_ADD(a.AdmDate, INTERVAL 30 DAY) AS exam_date, a.DepDesc AS department "
                    + "FROM Adm a WHERE a.AdmId = ?";

    /** 检查结果：Result 表，按大项+细项 */
    private static final String SQL_EXAM_RESULTS =
            "SELECT r.ARCIMDesc AS category_name, r.ODDesc AS item_name, "
                    + "r.ODUnit AS unit, r.RLTResult AS item_result "
                    + "FROM Result r WHERE r.PEAdmID = ?  order by  CAST(r.STCode AS UNSIGNED) , r.ARCIMCode , r.ODCode  asc";

    /** 总检报告：generalsummarize 表 */
    private static final String SQL_GENERAL_SUMMARIZE =
            "SELECT g.DiagnoseConclusion AS diagnose_conclusion, g.Advice AS general_advice "
                    + "FROM generalsummarize g WHERE g.PEAdmID = ?";

    /**
     * 按需求文档分三条 SQL 查询，生成北京友谊医院体检报告 PDF（封面 / 总检报告 / 检查结果）。
     */
    @Test
    public void generateFriendshipExamPdfFromAdmMysql() throws Exception {
//        String admId = "7afe5a0fce27ce6b261cdd7bf7118e1c76d4c0ca";
        String admId = "1257669448f41c823a182da4b2af87192162f452";



        Class.forName("com.mysql.cj.jdbc.Driver");
        Map<String, Object> reportData = new HashMap<>();
        LinkedHashMap<String, List<Map<String, String>>> categoryItems = new LinkedHashMap<>();
        List<Map<String, String>> summaryRows = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(
                FRIENDSHIP_JDBC_URL, FRIENDSHIP_JDBC_USER, FRIENDSHIP_JDBC_PASSWORD)) {

            try (PreparedStatement ps = conn.prepareStatement(SQL_ADM_INFO)) {
                ps.setString(1, admId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        reportData.put("hospitalName", nullToEmpty(rs.getString("hospital_name")));
                        reportData.put("gender", nullToEmpty(rs.getString("gender")));
                        reportData.put("age", rs.getObject("age"));
                        reportData.put("patientName", nullToEmpty(rs.getString("patient_name")));
                        reportData.put("examDate", formatDate(rs.getObject("exam_date")));
                        reportData.put("department", nullToEmpty(rs.getString("department")));
                        reportData.put("admId", admId);
                    } else {
                        throw new IllegalStateException("未找到 Adm 记录: " + admId);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(SQL_EXAM_RESULTS)) {
                ps.setString(1, admId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String categoryName = rs.getString("category_name");
                        String itemName = rs.getString("item_name");
                        if (categoryName == null || categoryName.trim().isEmpty()
                                || itemName == null || itemName.trim().isEmpty()) {
                            continue;
                        }
                        Map<String, String> item = new HashMap<>();
                        item.put("name", itemName.trim());
                        item.put("result", nullToEmpty(rs.getString("item_result")));
                        item.put("unit", nullToEmpty(rs.getString("unit")));
                        categoryItems.computeIfAbsent(categoryName.trim(), k -> new ArrayList<>()).add(item);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(SQL_GENERAL_SUMMARIZE)) {
                ps.setString(1, admId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String diagnosis = nullToEmpty(rs.getString("diagnose_conclusion"));
                        String advice = nullToEmpty(rs.getString("general_advice"));
                        if (!diagnosis.isEmpty() || !advice.isEmpty()) {
                            summaryRows.add(summaryRow(diagnosis, advice));
                        }
                    }
                }
            }
        }

        summaryRows = normalizeSummaryRows(summaryRows);

        reportData.put("logoBase64", loadLogoBase64());
        reportData.put("summaryRows", summaryRows);
        reportData.put("examCategories", buildExamCategories(categoryItems));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = objectMapper.writeValueAsString(reportData);
        String html = JsonToHtmlUtil.getHtml(jsonData, "friendship-exam-report");

        String safeName = String.valueOf(reportData.get("patientName")).replaceAll("[\\\\/:*?\"<>|]", "_");
        String fileName = "./build/" + admId + "-" + reportData.get("examDate") + ".pdf";
        HtmlToPdfUtil.toPdfFile(html, fileName);
        assertLastPdfPageNotBlank(fileName);
        System.out.println("已生成PDF: " + fileName);
    }

    private static void assertLastPdfPageNotBlank(String fileName) throws IOException
    {
        try (PDDocument document = PDDocument.load(new File(fileName)))
        {
            int pages = document.getNumberOfPages();
            assertTrue("PDF 至少应有一页", pages >= 1);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pages);
            stripper.setEndPage(pages);
            String lastPageText = stripper.getText(document);
            assertFalse("最后一页不应为空白，当前页数=" + pages, lastPageText.replaceAll("\\s+", "").isEmpty());
            System.out.println("PDF 页数: " + pages + "，最后一页校验通过");
        }
    }

    private static String loadLogoBase64() throws IOException {
        try (InputStream in = Html2pdfApplicationTests.class.getResourceAsStream(
                "/static/images/friendship-hospital-logo.png")) {
            if (in != null) {
                byte[] bytes = readAllBytes(in);
                return Base64.getEncoder().encodeToString(bytes);
            }
        }
        // 如果logo文件不存在，使用一个简单的1x1透明PNG作为占位符
        String placeholderPngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==";
        return placeholderPngBase64;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static String formatDate(Object date) {
        if (date == null) {
            return "";
        }
        return date.toString();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * 总检报告行：左列诊断结论、右列总检建议（与截图表格一致）。
     */
    private static Map<String, String> summaryRow(String diagnosis, String advice) {
        Map<String, String> row = new HashMap<>();
        row.put("diagnosis", normalizeDiagnosisTitle(diagnosis));
        row.put("advice", advice == null ? "" : advice.trim());
        return row;
    }

    /**
     * 若库中只有一条记录且诊断结论为多行「标题+建议」交替文本，则拆成多行；
     * 若诊断、建议各为多行且行数一致，则按行对齐。
     */
    private static List<Map<String, String>> normalizeSummaryRows(List<Map<String, String>> rows) {
        if (rows.isEmpty()) {
            return rows;
        }
        if (rows.size() > 1) {
            return rows;
        }
        Map<String, String> only = rows.get(0);
        String diagnosis = only.get("diagnosis");
        String advice = only.get("advice");
        if (diagnosis.contains("\n") && advice.isEmpty()) {
            return parseAlternatingDiagnosisText(diagnosis);
        }
        if (diagnosis.contains("\n") && advice.contains("\n")) {
            String[] dLines = splitNonEmptyLines(diagnosis);
            String[] aLines = splitNonEmptyLines(advice);
            if (dLines.length == aLines.length && dLines.length > 1) {
                List<Map<String, String>> zipped = new ArrayList<>();
                for (int i = 0; i < dLines.length; i++) {
                    zipped.add(summaryRow(dLines[i], aLines[i]));
                }
                return zipped;
            }
        }
        return rows;
    }

    private static String[] splitNonEmptyLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String raw : text.split("\\r?\\n")) {
            String line = raw.trim();
            if (!line.isEmpty() && !"诊断结论".equals(line) && !"总检建议".equals(line)) {
                lines.add(line);
            }
        }
        return lines.toArray(new String[0]);
    }

    /** 单字段内「诊断标题行 + 建议说明行」交替（需求文档效果1-原始顺序） */
    private static List<Map<String, String>> parseAlternatingDiagnosisText(String text) {
        List<Map<String, String>> list = new ArrayList<>();
        String[] lines = splitNonEmptyLines(text);
        String pendingDiagnosis = null;
        for (String line : lines) {
            if (pendingDiagnosis == null) {
                pendingDiagnosis = line;
            } else {
                list.add(summaryRow(pendingDiagnosis, line));
                pendingDiagnosis = null;
            }
        }
        if (pendingDiagnosis != null) {
            list.add(summaryRow(pendingDiagnosis, ""));
        }
        return list;
    }

    /** 去掉占位后缀 XXX */
    private static String normalizeDiagnosisTitle(String title) {
        if (title == null) {
            return "";
        }
        String t = title.trim();
        if (t.endsWith("XXX")) {
            t = t.substring(0, t.length() - 3).trim();
        }
        return t;
    }

    /**
     * 检查结果：按大项（ARCIMDesc）保持查询顺序分组。
     * 该大项任一条细项有单位(ODUnit)则按检验科三列展示，否则非检验科两列。
     */
    private static List<Map<String, Object>> buildExamCategories(
            LinkedHashMap<String, List<Map<String, String>>> categoryItems) {
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, String>>> e : categoryItems.entrySet()) {
            List<Map<String, String>> items = e.getValue();
            boolean lab = false;
            for (Map<String, String> item : items) {
                String unit = item.get("unit");
                if (unit != null && !unit.isEmpty()) {
                    lab = true;
                    break;
                }
            }
            Map<String, Object> cat = new HashMap<>();
            cat.put("name", e.getKey());
            cat.put("lab", lab);
            cat.put("items", items);
            categories.add(cat);
        }
        return categories;
    }

}
