package com.example.springboottest.controller;

import com.example.springboottest.domain.Emp;
import com.example.springboottest.mapper.EmpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/emp")
public class controller {

    @Autowired
    public EmpMapper em;

    @ResponseBody
    @RequestMapping(value="/getEmp/{sid}")
    public String getEmpById(@PathVariable(name="sid") int id){
        Emp emp = em.selectByPrimaryKey(id);
/*        model.addAttribute("emp name:",emp.getEname());
        model.addAttribute("emp money:",emp.getSal());*/
        return emp.toString();
    }

    @ResponseBody
    @RequestMapping(value="/test")
    public String test(){
        return "test";
    }
}
