package com.mycompany.pojo;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Test4Spring {
    int id;
    @Resource(name="student01")
    Student student;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "Test4Spring{" +
                "id=" + id +
                ", student=" + student +
                '}';
    }
}
