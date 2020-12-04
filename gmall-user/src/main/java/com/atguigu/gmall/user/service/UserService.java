package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.beans.UmsMember;
import com.atguigu.gmall.user.beans.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAlltUser();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);
}
