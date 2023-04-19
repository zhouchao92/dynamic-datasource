package com.zhou.repository.mapper;

import com.zhou.repository.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * UserMapper
 *
 * @author 周超
 * @since 2023/4/18 8:39
 */
@Mapper
public interface UserMapper {

    @Select("select * from tb_user")
    List<UserDTO> listUsers();

}
