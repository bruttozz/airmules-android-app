package com.example.asthana.airmuleschat.bean;


public class WeChatInfo {

    private String openid;

    private int sex;

    private String nickname;

    private String headimgurl;

    private String province;

    private String language;

    private String country;

    private String unionid;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getSex() {
        return (sex == 0) ? "男" : "女";
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    @Override
    public String toString() {
//        return "WeChatInfo{" +
//                "openid='" + openid + '\'' +
//                ", sex='" + getSex() + '\'' +
//                ", nickname='" + nickname + '\'' +
//                ", headimgurl='" + headimgurl + '\'' +
//                ", province='" + province + '\'' +
//                ", language='" + language + '\'' +
//                ", country='" + country + '\'' +
//                ", unionid='" + unionid + '\'' +
//                '}';
        return nickname;
    }
}