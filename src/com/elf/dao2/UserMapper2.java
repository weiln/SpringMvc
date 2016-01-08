/**
 * 
 */
package com.elf.dao2;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.elf.model.SysMenu;
import com.elf.model.User;
import com.elf.model.easyui.PageHelper;

/**
 * @author tfj
 * 2014-8-2
 */
public interface UserMapper2 {



	public String getUsernameById(@Param("id") int id);


}
