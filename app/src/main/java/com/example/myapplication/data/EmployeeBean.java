package com.example.myapplication.data;

import java.io.Serializable;
import java.math.BigDecimal;

public class EmployeeBean implements Serializable {

    private Integer eid;

    private String name;

    private Integer age;

    private Integer gender;

    private BigDecimal longitude;

    private BigDecimal latitude;
    // 登录时用手机号和密码
    private String telephone;

    private String pwd;

    private Integer dId;



    //  ???
    private int followNum,fansNum,scanNum,isFollow;

    public int getIsFollow() {
        return isFollow;
    }

    public void setIsFollow(int isFollow) {
        this.isFollow = isFollow;
    }

    public int getFollowNum() {
        return followNum;
    }
    public void setFollowNum(int followNum) {
        this.followNum = followNum;
    }
    public int getFansNum() {
        return fansNum;
    }
    public void setFansNum(int fansNum) {
        this.fansNum = fansNum;
    }
    public int getScanNum() {
        return scanNum;
    }
    public void setScanNum(int scanNum) {
        this.scanNum = scanNum;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone == null ? null : telephone.trim();
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd == null ? null : pwd.trim();
    }

    public Integer getdId() {
        return dId;
    }

    public void setdId(Integer dId) {
        this.dId = dId;
    }


    @Override
    public String toString() {
        return "Employee [eid=" + eid + ", name=" + name + ", telephone=" + telephone + ", password=" + pwd
                + ", age=" + age + ", gender=" + gender + ",longitude=" + longitude + ", latitude=" + latitude+ ", did=" + dId + "]";
    }

}
