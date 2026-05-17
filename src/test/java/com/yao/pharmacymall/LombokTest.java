package com.yao.pharmacymall;

import lombok.Data;
import org.junit.jupiter.api.Test;

@Data
class TestUser {
    private String name;
    private int age;
}

public class LombokTest {

    @Test
    public void testLombok() {
        TestUser user = new TestUser();
        user.setName("Test");
        user.setAge(20);
        System.out.println("Name: " + user.getName());
        System.out.println("Age: " + user.getAge());
        System.out.println("Lombok is working!");
    }
}
