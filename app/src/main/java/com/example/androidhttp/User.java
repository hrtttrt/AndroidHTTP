package com.example.androidhttp;

public class User {
    private String userId;
    private String userName;
    private int age;
    private String major;

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public int getAge() {
        return age;
    }

    public String getMajor() {
        return major;
    }

    @Override
    public String toString() {
        return "用户信息：\n"
                + "用户ID：" + userId + "\n"
                + "姓名：" + userName + "\n"
                + "年龄：" + age + "\n"
                + "专业：" + major;
    }
}
