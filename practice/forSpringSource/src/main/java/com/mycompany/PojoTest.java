package com.mycompany;

import com.mycompany.pojo.Test4Spring;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PojoTest {
    public static void main(String[] args) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        Test4Spring ts = (Test4Spring)ac.getBean("test");
        System.out.println(ts.toString());
    }
}
