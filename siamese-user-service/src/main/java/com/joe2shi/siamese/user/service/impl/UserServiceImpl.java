package com.joe2shi.siamese.user.service.impl;

import com.alibaba.nacos.common.utils.Md5Utils;
import com.joe2shi.siamese.common.utils.IdUtils;
import com.joe2shi.siamese.user.dto.CheckDto;
import com.joe2shi.siamese.user.dto.RegisterDto;
import com.joe2shi.siamese.user.dto.AccreditDto;
import com.joe2shi.siamese.user.entity.SiameseUserEntity;
import com.joe2shi.siamese.user.mapper.UserMapper;
import com.joe2shi.siamese.user.service.UserService;
import com.joe2shi.siamese.common.constant.RegularConstant;
import com.joe2shi.siamese.common.constant.SystemConstant;
import com.joe2shi.siamese.common.enums.ResponseEnum;
import com.joe2shi.siamese.common.exception.SiameseException;
import com.joe2shi.siamese.common.vo.SiameseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

@Service
@Slf4j
@SuppressWarnings("rawtypes")
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public SiameseResult validation(CheckDto check) {
        SiameseUserEntity siameseUserEntity = new SiameseUserEntity();
        switch (check.getType()) {
            case SystemConstant.NUMBER_ONE:
                siameseUserEntity.setUsername(check.getData().trim());
                break;
            case SystemConstant.NUMBER_TWO:
                siameseUserEntity.setPhoneNumber(check.getData().trim());
                break;
            default:
                throw new SiameseException(ResponseEnum.INVALID_USER_DATA_TYPE);
        }
        return new SiameseResult<>(ResponseEnum.REQUEST_ACCEPTED, userMapper.selectCount(siameseUserEntity) == SystemConstant.NUMBER_ZERO);
    }

    @Override
    public SiameseResult register(RegisterDto register) {
        String username = register.getUsername().trim();
        String password = register.getPassword().trim();
        String phoneNumber = register.getPhoneNumber().trim();
        // Check user information
        if (!username.matches(RegularConstant.CHECK_USERNAME)) {
            throw new SiameseException(ResponseEnum.INVALID_USERNAME);
        }
        if (!password.matches(RegularConstant.CHECK_SECRET_CODE)) {
            throw new SiameseException(ResponseEnum.INVALID_PASSWORD);
        }
        if (!phoneNumber.matches(RegularConstant.CHECK_PHONE_NUMBER)) {
            throw new SiameseException(ResponseEnum.INVALID_PHONE_NUMBER);
        }
        // Check username or phone number exist
        Example example = new Example(SiameseUserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(SystemConstant.STRING_USERNAME, username);
        criteria.orEqualTo(SystemConstant.STRING_PHONE_NUMBER, phoneNumber);
        SiameseUserEntity item = userMapper.selectOneByExample(example);
        if (!ObjectUtils.isEmpty(item)) {
            if (username.equals(item.getUsername())) {
                // Username already exists
                throw new SiameseException(ResponseEnum.USERNAME_ALREADY_USE);
            }
            if (phoneNumber.equals(item.getPhoneNumber())) {
                // Phone number has been bound
                throw new SiameseException(ResponseEnum.PHONE_NUMBER_HAS_BEEN_BOUND);
            }
        }
        try {
            // Insert user information
            // Encryption password
            String md5Password = Md5Utils.getMD5(password.getBytes());
            SiameseUserEntity siameseUserEntity = new SiameseUserEntity();
            siameseUserEntity.setId(IdUtils.generateId());
            siameseUserEntity.setUsername(username);
            siameseUserEntity.setPassword(md5Password);
            siameseUserEntity.setPhoneNumber(phoneNumber);
            siameseUserEntity.setCreateTime(System.currentTimeMillis());
            int result = userMapper.insert(siameseUserEntity);
            if (result < SystemConstant.NUMBER_ONE) {
                throw new SiameseException(ResponseEnum.REGISTER_FAILED);
            }
            return new SiameseResult(ResponseEnum.REGISTER_SUCCESS);
        } catch (Exception e) {
            log.error(ResponseEnum.REGISTER_FAILED.getMessage() + SystemConstant.CHARACTER_COLON + SystemConstant.CHARACTER_SPACE + e.getMessage());
            throw new SiameseException(ResponseEnum.REGISTER_FAILED);
        }
    }

    @Override
    public SiameseResult accredit(AccreditDto accredit) {
        String username = accredit.getUsername().trim();
        String password = accredit.getPassword().trim();
        if (StringUtils.isBlank(username)) {
            throw new SiameseException(ResponseEnum.USERNAME_IS_REQUIRED);
        }
        if (StringUtils.isBlank(password)) {
            throw new SiameseException(ResponseEnum.PASSWORD_IS_REQUIRED);
        }
        Example example = new Example(SiameseUserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(SystemConstant.STRING_USERNAME, username);
        criteria.orEqualTo(SystemConstant.STRING_PHONE_NUMBER, username);
        SiameseUserEntity item = userMapper.selectOneByExample(example);
        if (ObjectUtils.isEmpty(item) || !Md5Utils.getMD5(password.getBytes()).equals(item.getPassword())) {
            throw new SiameseException(ResponseEnum.WRONG_PASSWORD);
        }
        return new SiameseResult<>(ResponseEnum.ACCREDIT_SUCCESS, item.getId());
    }

    @Override
    public SiameseResult user(String id) {
        SiameseUserEntity item = userMapper.selectByPrimaryKey(id);
        if (ObjectUtils.isEmpty(item)) {
            throw new SiameseException(ResponseEnum.RECORD_NOT_FOUND);
        }
        return new SiameseResult<>(ResponseEnum.REQUEST_ACCEPTED, item);
    }
}
