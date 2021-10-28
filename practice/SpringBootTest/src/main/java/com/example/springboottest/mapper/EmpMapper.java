package com.example.springboottest.mapper;

import com.example.springboottest.domain.Emp;
import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

//@Mapper
public interface EmpMapper {
    @Delete({
        "delete from emp",
        "where EMPNO = #{empno,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer empno);

    @Insert({
        "insert into emp (EMPNO, ENAME, ",
        "JOB, MGR, HIREDATE, ",
        "SAL, COMM, DEPTNO)",
        "values (#{empno,jdbcType=INTEGER}, #{ename,jdbcType=VARCHAR}, ",
        "#{job,jdbcType=VARCHAR}, #{mgr,jdbcType=INTEGER}, #{hiredate,jdbcType=DATE}, ",
        "#{sal,jdbcType=DOUBLE}, #{comm,jdbcType=DOUBLE}, #{deptno,jdbcType=INTEGER})"
    })
    int insert(Emp record);

    @Select({
        "select",
        "EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO",
        "from emp",
        "where EMPNO = #{empno,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="EMPNO", property="empno", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="ENAME", property="ename", jdbcType=JdbcType.VARCHAR),
        @Result(column="JOB", property="job", jdbcType=JdbcType.VARCHAR),
        @Result(column="MGR", property="mgr", jdbcType=JdbcType.INTEGER),
        @Result(column="HIREDATE", property="hiredate", jdbcType=JdbcType.DATE),
        @Result(column="SAL", property="sal", jdbcType=JdbcType.DOUBLE),
        @Result(column="COMM", property="comm", jdbcType=JdbcType.DOUBLE),
        @Result(column="DEPTNO", property="deptno", jdbcType=JdbcType.INTEGER)
    })
    Emp selectByPrimaryKey(Integer empno);

    @Select({
        "select",
        "EMPNO, ENAME, JOB, MGR, HIREDATE, SAL, COMM, DEPTNO",
        "from emp"
    })
    @Results({
        @Result(column="EMPNO", property="empno", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="ENAME", property="ename", jdbcType=JdbcType.VARCHAR),
        @Result(column="JOB", property="job", jdbcType=JdbcType.VARCHAR),
        @Result(column="MGR", property="mgr", jdbcType=JdbcType.INTEGER),
        @Result(column="HIREDATE", property="hiredate", jdbcType=JdbcType.DATE),
        @Result(column="SAL", property="sal", jdbcType=JdbcType.DOUBLE),
        @Result(column="COMM", property="comm", jdbcType=JdbcType.DOUBLE),
        @Result(column="DEPTNO", property="deptno", jdbcType=JdbcType.INTEGER)
    })
    List<Emp> selectAll();

    @Update({
        "update emp",
        "set ENAME = #{ename,jdbcType=VARCHAR},",
          "JOB = #{job,jdbcType=VARCHAR},",
          "MGR = #{mgr,jdbcType=INTEGER},",
          "HIREDATE = #{hiredate,jdbcType=DATE},",
          "SAL = #{sal,jdbcType=DOUBLE},",
          "COMM = #{comm,jdbcType=DOUBLE},",
          "DEPTNO = #{deptno,jdbcType=INTEGER}",
        "where EMPNO = #{empno,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(Emp record);
}