package com.ec.onlinestore.controller;

import com.ec.onlinestore.entity.Product;
import com.ec.onlinestore.entity.ProductSku;
import com.ec.onlinestore.entity.User;
import com.ec.onlinestore.mapper.ProductMapper;
import com.ec.onlinestore.mapper.ProductSkuMapper;
import com.ec.onlinestore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@SuppressWarnings("SqlResolve")
public class AdminController {

    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;

    @Autowired
    public AdminController(JdbcTemplate jdbcTemplate, UserMapper userMapper,
                           ProductMapper productMapper, ProductSkuMapper productSkuMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.productSkuMapper = productSkuMapper;
    }

    @PostMapping("/reset")
    public Map<String, Object> resetDatabase() {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            jdbcTemplate.execute("TRUNCATE TABLE orders");
            jdbcTemplate.execute("TRUNCATE TABLE product_sku");
            jdbcTemplate.execute("TRUNCATE TABLE product");
            jdbcTemplate.execute("TRUNCATE TABLE user");

            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

            insertInitialData();

            return Map.of("success", true, "message", "数据库已恢复");
        } catch (Exception e) {
            return Map.of("success", false, "message", "恢复失败: " + e.getMessage());
        }
    }

    private void insertInitialData() {
        // ========== 用户表 ==========
        // 管理员
        User admin = new User();
        admin.setUsername("系统管理员");
        admin.setPassword("123456");
        admin.setPhone("13800000000");
        admin.setRole("admin");
        admin.setAddress("上海市浦东新区世纪大道100号");
        admin.setStatus("active");
        userMapper.insert(admin);

        // 商家
        User merchant1 = new User();
        merchant1.setUsername("小米官方旗舰店");
        merchant1.setPassword("123456");
        merchant1.setPhone("13800138001");
        merchant1.setRole("merchant");
        merchant1.setAddress("北京市海淀区西二旗中路33号");
        merchant1.setStatus("active");
        userMapper.insert(merchant1);

        User merchant2 = new User();
        merchant2.setUsername("华为官方旗舰店");
        merchant2.setPassword("123456");
        merchant2.setPhone("13800138002");
        merchant2.setRole("merchant");
        merchant2.setAddress("深圳市龙岗区坂田华为基地");
        merchant2.setStatus("active");
        userMapper.insert(merchant2);

        User merchant3 = new User();
        merchant3.setUsername("联想官方旗舰店");
        merchant3.setPassword("123456");
        merchant3.setPhone("13800138003");
        merchant3.setRole("merchant");
        merchant3.setAddress("北京市海淀区西北旺东路10号");
        merchant3.setStatus("active");
        userMapper.insert(merchant3);

        User merchant4 = new User();
        merchant4.setUsername("大疆官方旗舰店");
        merchant4.setPassword("123456");
        merchant4.setPhone("13800138004");
        merchant4.setRole("merchant");
        merchant4.setAddress("广东省深圳市南山区高新南四道18号");
        merchant4.setStatus("active");
        userMapper.insert(merchant4);

        User merchant5 = new User();
        merchant5.setUsername("绿联官方旗舰店");
        merchant5.setPassword("123456");
        merchant5.setPhone("13800138005");
        merchant5.setRole("merchant");
        merchant5.setAddress("广东省深圳市龙华区民治街道");
        merchant5.setStatus("active");
        userMapper.insert(merchant5);

        // 普通用户
        User customer1 = new User();
        customer1.setUsername("曹先生");
        customer1.setPassword("2801132199");
        customer1.setPhone("13942214892");
        customer1.setRole("customer");
        customer1.setAddress("辽宁省沈阳市浑南区智慧三街178号");
        customer1.setStatus("active");
        userMapper.insert(customer1);

        User customer2 = new User();
        customer2.setUsername("张明");
        customer2.setPassword("123456");
        customer2.setPhone("13912340001");
        customer2.setRole("customer");
        customer2.setAddress("上海市浦东新区世纪大道1号");
        customer2.setStatus("active");
        userMapper.insert(customer2);

        User customer3 = new User();
        customer3.setUsername("李芳");
        customer3.setPassword("123456");
        customer3.setPhone("13912340002");
        customer3.setRole("customer");
        customer3.setAddress("广州市天河区体育西路123号");
        customer3.setStatus("active");
        userMapper.insert(customer3);

        User customer4 = new User();
        customer4.setUsername("王磊");
        customer4.setPassword("123456");
        customer4.setPhone("13912340003");
        customer4.setRole("customer");
        customer4.setAddress("深圳市南山区科技园路10号");
        customer4.setStatus("active");
        userMapper.insert(customer4);

        // ========== 商品表 ==========
        // 小米商品（商家ID=2）
        Product p1 = new Product();
        p1.setName("小米14 Ultra");
        p1.setShortDescription("徕卡四摄 骁龙8Gen3 卫星通信");
        p1.setLongDescription("小米14 Ultra 搭载第三代骁龙8处理器，性能强劲。\n徕卡四摄影像系统：50MP主摄 + 50MP超广角 + 50MP长焦 + 50MP超长焦。\n6.73英寸2K AMOLED屏幕，支持120Hz刷新率。\n内置5300mAh电池，支持90W有线快充和80W无线快充。\n支持双向卫星通信，IP68防尘防水。");
        p1.setCategory("手机");
        p1.setUserId(2);
        productMapper.insert(p1);

        Product p2 = new Product();
        p2.setName("小米手环 9 Pro");
        p2.setShortDescription("1.74英寸AMOLED 独立GPS");
        p2.setLongDescription("小米手环 9 Pro 配备1.74英寸AMOLED高清大屏，亮度可达1200尼特。\n首次加入独立GPS模块，无需手机也能记录运动轨迹。\n支持150+种运动模式，提供专业运动数据分析。\n健康监测方面，支持心率、血氧、睡眠、压力等全方位监测。\n典型续航12天，重度使用也能坚持5天。");
        p2.setCategory("穿戴");
        p2.setUserId(2);
        productMapper.insert(p2);

        // 华为商品（商家ID=3）
        Product p3 = new Product();
        p3.setName("华为 Mate60 Pro");
        p3.setShortDescription("12GB+512GB 鸿蒙系统 卫星通话");
        p3.setLongDescription("华为 Mate60 Pro 搭载全新麒麟9000S芯片，支持卫星通信功能。\n配备6.82英寸OLED曲面屏，支持1-120Hz自适应刷新率。\n后置三摄系统：5000万像素超光变主摄 + 1200万像素超广角 + 4800万像素超微距长焦。\n内置5000mAh电池，支持88W有线快充和50W无线快充。\n预装HarmonyOS 4.0系统，带来更流畅的交互体验。");
        p3.setCategory("手机");
        p3.setUserId(3);
        productMapper.insert(p3);

        Product p4 = new Product();
        p4.setName("华为 FreeBuds Pro 3");
        p4.setShortDescription("智慧动态降噪3.0 无损音质");
        p4.setLongDescription("华为 FreeBuds Pro 3 搭载麒麟A2芯片，支持全新智慧动态降噪3.0技术。\n音质方面采用超感知原声双单元，支持LDAC高清音频编解码。\n续航方面，单次聆听可达6.5小时，配合充电盒可达31小时。\n支持IP54级防尘抗水溅，提供星河蓝、陶瓷白、冰霜银三色可选。");
        p4.setCategory("耳机");
        p4.setUserId(3);
        productMapper.insert(p4);

        // 联想商品（商家ID=4）
        Product p5 = new Product();
        p5.setName("联想 ThinkBook 14+");
        p5.setShortDescription("13代i7 32G 1TB 2.8K屏");
        p5.setLongDescription("联想 ThinkBook 14+ 搭载英特尔第13代酷睿i7-13700H处理器，14核20线程。\n配备32GB LPDDR5内存和1TB PCIe 4.0固态硬盘。\n14.5英寸2.8K全面屏，16:10比例，90Hz刷新率，400尼特亮度。\n接口丰富：双USB-C、双USB-A、HDMI 2.1、RJ45网口。\n62Wh大电池，支持100W PD快充。");
        p5.setCategory("电脑");
        p5.setUserId(4);
        productMapper.insert(p5);

        // 大疆商品（商家ID=5）
        Product p6 = new Product();
        p6.setName("大疆 Mini 4 Pro");
        p6.setShortDescription("轻于249g，全向避障，4K HDR视频");
        p6.setLongDescription("大疆 Mini 4 Pro 重量仅249克，无需注册即可轻松起飞。\n配备全向视觉感知系统，可实现前后左右上下全方位避障。\n影像方面，搭载1/1.3英寸传感器，支持4K/60fps HDR视频拍摄。\n最大续航34分钟，配合增强图传模块可实现20公里高清图传。\n支持航点飞行、大师镜头等智能功能。");
        p6.setCategory("无人机");
        p6.setUserId(5);
        productMapper.insert(p6);

        Product p7 = new Product();
        p7.setName("大疆 Osmo Mobile 6");
        p7.setShortDescription("三轴手机云台，智能跟随6.0");
        p7.setLongDescription("大疆 Osmo Mobile 6 是一款手机稳定器，采用三轴机械增稳技术。\n新增可伸缩延长杆，最长215mm，自拍和低角度拍摄更轻松。\n智能跟随6.0功能升级，跟踪更稳定，即使人物短暂出画也能快速找回。\n内置补光手机夹，三档亮度和色温可调。\n支持一键成片、动态变焦等多种拍摄模式。");
        p7.setCategory("配件");
        p7.setUserId(5);
        productMapper.insert(p7);

        // 绿联商品（商家ID=6）
        Product p8 = new Product();
        p8.setName("绿联 10000mAh 充电宝");
        p8.setShortDescription("双向快充，轻薄便携，可上飞机");
        p8.setLongDescription("绿联10000mAh充电宝采用高品质锂聚合物电芯，支持双向18W PD快充。\n轻薄设计，仅约200克，轻松放入口袋。\n具备过压、过流、过温等多重安全保护，符合航空运输标准。\n双USB输出接口，可同时为两台设备充电。\nLED数字电量显示，剩余电量一目了然。");
        p8.setCategory("充电宝");
        p8.setUserId(6);
        productMapper.insert(p8);

        Product p9 = new Product();
        p9.setName("绿联 65W 氮化镓充电器");
        p9.setShortDescription("三口快充，小巧便携");
        p9.setLongDescription("绿联65W氮化镓充电器采用第三代半导体材料，体积比传统充电器缩小40%。\n配备2个Type-C口和1个USB-A口，可同时为三台设备充电。\n单口最高65W输出，支持PD/PPS/QC等多种快充协议，兼容手机、平板、笔记本。\n可折叠插脚设计，方便携带。");
        p9.setCategory("充电器");
        p9.setUserId(6);
        productMapper.insert(p9);

        // ========== 款式表 ==========
        // 小米14 Ultra
        ProductSku sku1 = new ProductSku(); sku1.setProductId(p1.getId()); sku1.setSkuName("12GB+256GB"); sku1.setPrice(new BigDecimal("5999")); sku1.setStock(10); productSkuMapper.insert(sku1);
        ProductSku sku2 = new ProductSku(); sku2.setProductId(p1.getId()); sku2.setSkuName("16GB+512GB"); sku2.setPrice(new BigDecimal("6499")); sku2.setStock(8); productSkuMapper.insert(sku2);
        ProductSku sku3 = new ProductSku(); sku3.setProductId(p1.getId()); sku3.setSkuName("16GB+1TB"); sku3.setPrice(new BigDecimal("6999")); sku3.setStock(5); productSkuMapper.insert(sku3);

        // 小米手环
        ProductSku sku4 = new ProductSku(); sku4.setProductId(p2.getId()); sku4.setSkuName("标准版"); sku4.setPrice(new BigDecimal("399")); sku4.setStock(20); productSkuMapper.insert(sku4);
        ProductSku sku5 = new ProductSku(); sku5.setProductId(p2.getId()); sku5.setSkuName("NFC版"); sku5.setPrice(new BigDecimal("449")); sku5.setStock(15); productSkuMapper.insert(sku5);

        // 华为 Mate60 Pro
        ProductSku sku6 = new ProductSku(); sku6.setProductId(p3.getId()); sku6.setSkuName("12GB+256GB"); sku6.setPrice(new BigDecimal("6499")); sku6.setStock(8); productSkuMapper.insert(sku6);
        ProductSku sku7 = new ProductSku(); sku7.setProductId(p3.getId()); sku7.setSkuName("12GB+512GB"); sku7.setPrice(new BigDecimal("6999")); sku7.setStock(5); productSkuMapper.insert(sku7);
        ProductSku sku8 = new ProductSku(); sku8.setProductId(p3.getId()); sku8.setSkuName("16GB+1TB"); sku8.setPrice(new BigDecimal("7999")); sku8.setStock(3); productSkuMapper.insert(sku8);

        // 华为耳机
        ProductSku sku9 = new ProductSku(); sku9.setProductId(p4.getId()); sku9.setSkuName("标准版"); sku9.setPrice(new BigDecimal("1499")); sku9.setStock(15); productSkuMapper.insert(sku9);
        ProductSku sku10 = new ProductSku(); sku10.setProductId(p4.getId()); sku10.setSkuName("无线充电版"); sku10.setPrice(new BigDecimal("1699")); sku10.setStock(10); productSkuMapper.insert(sku10);

        // 联想笔记本
        ProductSku sku11 = new ProductSku(); sku11.setProductId(p5.getId()); sku11.setSkuName("16GB+512GB"); sku11.setPrice(new BigDecimal("5499")); sku11.setStock(5); productSkuMapper.insert(sku11);
        ProductSku sku12 = new ProductSku(); sku12.setProductId(p5.getId()); sku12.setSkuName("32GB+1TB"); sku12.setPrice(new BigDecimal("5999")); sku12.setStock(3); productSkuMapper.insert(sku12);

        // 大疆 Mini 4 Pro
        ProductSku sku13 = new ProductSku(); sku13.setProductId(p6.getId()); sku13.setSkuName("标准版"); sku13.setPrice(new BigDecimal("4788")); sku13.setStock(10); productSkuMapper.insert(sku13);
        ProductSku sku14 = new ProductSku(); sku14.setProductId(p6.getId()); sku14.setSkuName("畅飞套装"); sku14.setPrice(new BigDecimal("5988")); sku14.setStock(5); productSkuMapper.insert(sku14);

        // 大疆 Osmo Mobile 6
        ProductSku sku15 = new ProductSku(); sku15.setProductId(p7.getId()); sku15.setSkuName("标准版"); sku15.setPrice(new BigDecimal("899")); sku15.setStock(20); productSkuMapper.insert(sku15);
        ProductSku sku16 = new ProductSku(); sku16.setProductId(p7.getId()); sku16.setSkuName("套装版"); sku16.setPrice(new BigDecimal("1099")); sku16.setStock(10); productSkuMapper.insert(sku16);

        // 绿联充电宝
        ProductSku sku17 = new ProductSku(); sku17.setProductId(p8.getId()); sku17.setSkuName("10000mAh"); sku17.setPrice(new BigDecimal("89")); sku17.setStock(50); productSkuMapper.insert(sku17);
        ProductSku sku18 = new ProductSku(); sku18.setProductId(p8.getId()); sku18.setSkuName("20000mAh"); sku18.setPrice(new BigDecimal("129")); sku18.setStock(30); productSkuMapper.insert(sku18);

        // 绿联充电器
        ProductSku sku19 = new ProductSku(); sku19.setProductId(p9.getId()); sku19.setSkuName("单头版"); sku19.setPrice(new BigDecimal("129")); sku19.setStock(30); productSkuMapper.insert(sku19);
        ProductSku sku20 = new ProductSku(); sku20.setProductId(p9.getId()); sku20.setSkuName("配1m线版"); sku20.setPrice(new BigDecimal("159")); sku20.setStock(20); productSkuMapper.insert(sku20);

        // ========== 订单表 ==========
        // 曹先生（用户ID=7）的订单
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (7, 3, '12GB+512GB', 1, 6999, 'delivered', '2026-05-28 10:30:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (7, 4, '标准版', 2, 2998, 'delivered', '2026-05-28 14:20:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (7, 8, '10000mAh', 1, 89, 'shipped', '2026-05-29 09:15:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (7, 1, '16GB+512GB', 1, 6499, 'pending', '2026-05-30 11:00:00')");

        // 张明（用户ID=8）的订单
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (8, 3, '12GB+256GB', 1, 6499, 'delivered', '2026-05-27 15:45:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (8, 5, '16GB+512GB', 1, 5499, 'delivered', '2026-05-28 09:30:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (8, 9, '单头版', 2, 258, 'pending', '2026-05-30 10:00:00')");

        // 李芳（用户ID=9）的订单
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (9, 1, '12GB+256GB', 1, 5999, 'delivered', '2026-05-26 18:20:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (9, 6, '标准版', 1, 4788, 'shipped', '2026-05-29 16:00:00')");

        // 王磊（用户ID=10）的订单
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (10, 7, '套装版', 1, 1099, 'pending', '2026-05-30 08:30:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (10, 2, 'NFC版', 2, 898, 'pending', '2026-05-30 09:00:00')");
        jdbcTemplate.execute("INSERT INTO orders (user_id, product_id, sku_name, quantity, total_price, order_status, order_time) VALUES (10, 8, '20000mAh', 1, 129, 'pending', '2026-05-30 09:30:00')");
    }
}