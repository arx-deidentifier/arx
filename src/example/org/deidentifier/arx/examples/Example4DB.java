package org.deidentifier.arx.examples;

import org.deidentifier.arx.*;
import org.deidentifier.arx.aggregates.HierarchyBuilderRedactionBased;
import org.deidentifier.arx.criteria.KAnonymity;

import java.util.Iterator;
/**
 * @className : Example4DB
 * @Author : liming Mu
 * @Date : 2020/12/30
 * @Version 1.0
 * @Description : TODO
 */
public class Example4DB {
    public static void main(String[] args) throws Exception {
        //1 创建数据源
        // DB2时， dbName为空
        DataSource jdbcSource = DataSource.createJDBCSource("jdbc:db2://10.245.44.125:50000/dmstest",
                "db2inst1",
                "nsfocus123",
                null,
                "JS_TEST",
                "EMPLOYEE");
        //2 添加纳入运算的列（不一定是全列）
        jdbcSource.addColumn("EMP_ID", DataType.STRING);
        jdbcSource.addColumn("NAME", DataType.STRING);
        jdbcSource.addColumn("GENDER", DataType.STRING);
        jdbcSource.addColumn("AGE", DataType.STRING);
        jdbcSource.addColumn("DEP_ID", DataType.STRING);
        jdbcSource.addColumn("PEP_CARD_ID", DataType.STRING);
        jdbcSource.addColumn("SALARY", DataType.STRING);
        jdbcSource.addColumn("EMAIL", DataType.STRING);
        jdbcSource.addColumn("BANK_CARD_ID", DataType.STRING);
        jdbcSource.addColumn("M_PHONE", DataType.STRING);
        jdbcSource.addColumn("WECHAT_ID", DataType.STRING);
        jdbcSource.addColumn("ADDRESS", DataType.STRING);
        jdbcSource.addColumn("HOMETOWN", DataType.STRING);
        jdbcSource.addColumn("WORK_TIME", DataType.STRING);

        //3 创建数据
        Data data = Data.create(jdbcSource);

        //4 拿到handle，即读取数据后的结果
        DataHandle handle = data.getHandle();

        //5 构建准标识符的泛化树
        //5.1 获取准标识符的种类
        HierarchyBuilderRedactionBased<?> builder = HierarchyBuilderRedactionBased.create(HierarchyBuilderRedactionBased.Order.LEFT_TO_RIGHT,
                HierarchyBuilderRedactionBased.Order.RIGHT_TO_LEFT,
                ' ', '*');
        //5.2 得到所有的值
        String[] gender_s =handle.getDistinctValues(handle.getColumnIndexOf("GENDER"));

        //5.3 泛化
        builder.prepare(gender_s);
        String[][] hierarchy = builder.build().getHierarchy();
//        printArray(hierarchy);
        //5.4 构建泛化树
        AttributeType.Hierarchy.DefaultHierarchy gender = AttributeType.Hierarchy.create();
        addHierarchy(gender,hierarchy);

        //--------------------------------------
        HierarchyBuilderRedactionBased<?> builder_age = HierarchyBuilderRedactionBased.create(HierarchyBuilderRedactionBased.Order.LEFT_TO_RIGHT,
                HierarchyBuilderRedactionBased.Order.RIGHT_TO_LEFT,
                ' ', '*');
        String[] age_s =handle.getDistinctValues(handle.getColumnIndexOf("AGE"));
        builder_age.prepare(age_s);
        String[][] hierarchy_sex = builder_age.build().getHierarchy();
        AttributeType.Hierarchy.DefaultHierarchy age = AttributeType.Hierarchy.create();
        addHierarchy(age,hierarchy_sex);

        //--------------------------------------
        HierarchyBuilderRedactionBased<?> builder_phone = HierarchyBuilderRedactionBased.create(HierarchyBuilderRedactionBased.Order.LEFT_TO_RIGHT,
                HierarchyBuilderRedactionBased.Order.RIGHT_TO_LEFT,
                ' ', '*');
        String[] phone_s =handle.getDistinctValues(handle.getColumnIndexOf("M_PHONE"));
        builder_phone.prepare(phone_s);
        String[][] hierarchy_phone = builder_phone.build().getHierarchy();
        AttributeType.Hierarchy.DefaultHierarchy phone = AttributeType.Hierarchy.create();
        addHierarchy(phone,hierarchy_phone);

        //--------------------------------------
        HierarchyBuilderRedactionBased<?> builder_addresses = HierarchyBuilderRedactionBased.create(HierarchyBuilderRedactionBased.Order.LEFT_TO_RIGHT,
                HierarchyBuilderRedactionBased.Order.RIGHT_TO_LEFT,
                ' ', '*');
        String[] addresses_s =handle.getDistinctValues(handle.getColumnIndexOf("ADDRESS"));
        builder_addresses.prepare(addresses_s);
        String[][] hierarchy_addresses = builder_addresses.build().getHierarchy();
        AttributeType.Hierarchy.DefaultHierarchy addresses = AttributeType.Hierarchy.create();
        addHierarchy(addresses,hierarchy_addresses);


        //6 配置列的敏感类型。（不配置的， 默认是身份标志符）
        data.getDefinition().setAttributeType("EMP_ID", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("NAME", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("PEP_CARD_ID", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("DEP_ID", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("GENDER", gender);
        data.getDefinition().setAttributeType("AGE", age);
        data.getDefinition().setAttributeType("SALARY", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("EMAIL", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("BANK_CARD_ID", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("M_PHONE",phone);
        data.getDefinition().setAttributeType("WECHAT_ID", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("ADDRESS", addresses);
        data.getDefinition().setAttributeType("HOMETOWN", AttributeType.INSENSITIVE_ATTRIBUTE);
        data.getDefinition().setAttributeType("WORK_TIME", AttributeType.INSENSITIVE_ATTRIBUTE);


        //7 创建配置文件，即匿名化的各个参数指标（主要配置敏感属性的处理策略，以及准标识符的k值）
        ARXConfiguration config = ARXConfiguration.create();
        //7.1 准标识符的K值调节
        config.addPrivacyModel(new KAnonymity(2));

        //7.2 隐私模型的策略配置
//        config.addPrivacyModel(new DDisclosurePrivacy("passportid",19d));
        // config.addPrivacyModel(new EqualDistanceTCloseness("address",2));
        //  config.addPrivacyModel(new DistinctLDiversity("id",5));

        config.setSuppressionLimit(0.5);

        //8 创建匿名对象
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setMaximumSnapshotSizeDataset(0.2);
        anonymizer.setMaximumSnapshotSizeSnapshot(0.2);
        anonymizer.setHistorySize(1);

        //9 结果输出
        ARXResult result = anonymizer.anonymize(data, config);
        DataHandle output = result.getOutput();
        Iterator<String[]> iterator = output.iterator();
        printRe(iterator);
        System.out.println(result.getOptimumFound());
    }

    // 打印结果集
    private static void printRe(Iterator<String[]> iterator){
        while (iterator.hasNext()) {
            String[] next = iterator.next();
            for (int i = 0; i < next.length; i++) {
                String string = next[i];
                System.out.print(string);
                if (i < next.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

    //添加泛化
    private static void addHierarchy(AttributeType.Hierarchy.DefaultHierarchy hierarchy, String[][] array) {
        for(String[] next:array){
            hierarchy.add(next);
        }
    }

    //打印泛化树信息
    private static void printArray(String[][] array) {
        System.out.print("{");
        for (int j = 0; j < array.length; j++) {
            String[] next = array[j];
            System.out.print("{");
            for (int i = 0; i < next.length; i++) {
                String string = next[i];
                System.out.print("\"" + string + "\"");
                if (i < next.length - 1) {
                    System.out.print(",");
                }
            }
            System.out.print("}");
            if (j < array.length - 1) {
                System.out.print(",\n");
            }
        }
        System.out.println("}");
    }
}
