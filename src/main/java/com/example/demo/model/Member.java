package com.example.demo.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class Member {

    @NotBlank(message = "名前を入力してください")
    private String name;
    
    @NotNull(message = "年齢を入力してください")
    @Min(value = 0, message = "年齢は0歳以上で入力してください")
    @Max(value = 120, message = "年齢は120歳以下で入力してください")
    private Integer age;

    @NotBlank(message = "会員種別を入力してください")
    private String memberType;
    private Long id;

    public Member() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}