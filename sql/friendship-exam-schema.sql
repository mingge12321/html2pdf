-- 复旦大学附属中山医院体检报告表
CREATE TABLE IF NOT EXISTS friendship_medical_exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    
    -- 封面页信息
    hospital_name VARCHAR(200) DEFAULT '复旦大学附属中山医院' COMMENT '医院名称',
    health_center VARCHAR(200) DEFAULT '健康管理中心' COMMENT '健康管理中心',
    exam_no VARCHAR(50) COMMENT '体检编号',
    exam_archive_no VARCHAR(50) COMMENT '体检档案号',
    exam_date DATE COMMENT '体检日期',
    total_pages INT DEFAULT 25 COMMENT '总页数',
    
    -- 基本信息
    patient_name VARCHAR(100) NOT NULL COMMENT '病人姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    birthday DATE COMMENT '出生日期',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    work_unit VARCHAR(200) COMMENT '工作单位',
    exam_address VARCHAR(500) DEFAULT '上海市松江区龙泉山南路137弄' COMMENT '体检地点',
    
    -- 第2页：主检结论及建议
    main_conclusions TEXT COMMENT '主检结论及建议（JSON格式，多个结论）',
    
    -- 第11页：一般检查
    systolic_pressure1 VARCHAR(20) COMMENT '收缩压(第一次)',
    diastolic_pressure1 VARCHAR(20) COMMENT '舒张压(第一次)',
    bp_conclusion1 VARCHAR(100) COMMENT '血压结论(第一次)',
    systolic_pressure2 VARCHAR(20) COMMENT '收缩压(第二次)',
    diastolic_pressure2 VARCHAR(20) COMMENT '舒张压(第二次)',
    bp_conclusion2 VARCHAR(100) COMMENT '血压结论(第二次)',
    waist_circumference VARCHAR(20) COMMENT '腰围',
    hip_circumference VARCHAR(20) COMMENT '臀围',
    waist_hip_ratio VARCHAR(20) COMMENT '腰臀比',
    height VARCHAR(20) COMMENT '身高',
    weight VARCHAR(20) COMMENT '体重',
    bmi VARCHAR(20) COMMENT '体重指数',
    general_exam_summary TEXT COMMENT '一般检查小结',
    general_exam_doctor VARCHAR(50) COMMENT '一般检查报告医师',
    general_exam_reviewer VARCHAR(50) COMMENT '一般检查审核医师',
    
    -- 第12页：内科
    heart_rhythm VARCHAR(50) COMMENT '心律',
    heart_rate VARCHAR(50) COMMENT '心率',
    heart_sound VARCHAR(100) COMMENT '心音',
    heart_murmur VARCHAR(100) COMMENT '杂音',
    lung_auscultation VARCHAR(100) COMMENT '肺部听诊',
    abdomen_palpation VARCHAR(100) COMMENT '腹部触诊',
    liver VARCHAR(100) COMMENT '肝脏',
    gallbladder VARCHAR(100) COMMENT '胆囊',
    spleen VARCHAR(100) COMMENT '脾脏',
    kidney VARCHAR(100) COMMENT '肾脏',
    internal_medicine_history TEXT COMMENT '内科既往史',
    personal_history TEXT COMMENT '个人史',
    internal_medicine_summary TEXT COMMENT '内科小结',
    internal_medicine_doctor VARCHAR(50) COMMENT '内科报告医师',
    internal_medicine_reviewer VARCHAR(50) COMMENT '内科审核医师',
    internal_medicine_reviewer_signature VARCHAR(50) COMMENT '内科审核医师签名',
    
    -- 第12页：外科
    thyroid VARCHAR(100) COMMENT '甲状腺',
    lymph_node VARCHAR(100) COMMENT '淋巴结',
    spine_joints VARCHAR(100) COMMENT '脊柱四肢关节',
    surgery_other VARCHAR(100) COMMENT '外科其他',
    surgery_summary TEXT COMMENT '外科小结',
    surgery_doctor VARCHAR(50) COMMENT '外科报告医师',
    surgery_reviewer VARCHAR(50) COMMENT '外科审核医师',
    
    -- 第4页：其他检查及总检医生
    other_findings TEXT COMMENT '其他检查发现（JSON格式）',
    chief_doctor VARCHAR(50) COMMENT '总检医生',
    chief_doctor_signature VARCHAR(50) COMMENT '总检医生签名',
    chief_reviewer VARCHAR(50) COMMENT '总审医生',
    chief_reviewer_signature VARCHAR(50) COMMENT '总审医生签名',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='复旦大学附属中山医院体检报告表';