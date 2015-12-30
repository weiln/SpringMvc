/**
 * 
 */
package com.elf.service;

import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.elf.dao.UserMapper;
import com.elf.model.SysMenu;
import com.elf.model.User;
import com.elf.model.easyui.DataGrid;
import com.elf.model.easyui.PageHelper;

/**
 * @author weiln
 * 2014-8-2
 */
/** 
 * Cacheable注解        负责将方法的返回值加入到缓存中 
 * CacheEvict注解     负责清除缓存(它的三个参数与@Cacheable的意思是一样的) 
 * ---------------------------------------------------------------------------------------------------------- 
 * value------缓存位置的名称,不能为空,若使用EHCache则其值为ehcache.xml中的<cache name="myCache"/> 
 * key--------缓存的Key,默认为空(表示使用方法的参数类型及参数值作为key),支持SpEL 
 * condition--只有满足条件的情况才会加入缓存,默认为空(表示全部都加入缓存),支持SpEL 
 * 该注解的源码位于spring-context-*.RELEASE-sources.jar中 
 * Spring针对Ehcache支持的Java源码位于spring-context-support-*.RELEASE-sources.jar中 
 * ---------------------------------------------------------------------------------------------------------- 
 */

/**
 * 
 * cxf框架 发布webservice几种方式：
 * 1.在class上添加注解@WebService,这样整个class都会被发布为webservice
 * 2.在方法上添加注解 @WebMethod,这样这个方法就会被发布成webservice
 * 
 * @WebParam 是对webservice方法中的参数进行注释：如没加之前参数名为arg0、arg1……，加在注解后参数名变成注解的名称
 *		@WebMethod(operationName = "findUserByName")
 *		public User findUserByName(@WebParam(name = "username")String username)
 *
 *cxf发布restful风格的几种方式：
 *@POST/@GET/@PUT/@DELETE 四种方式
 *	@Path("/finishProcess") 调用方法名
 *	@Produces("application/json") 传输的数据格式
 *	@Consumes("application/x-www-form-urlencoded")
 */
@Service("userService")

public class UserService {

	@Resource
	private UserMapper userMapper;
	/**
	 * @param username
	 * @return
	 */
	@WebMethod(operationName = "findUserByName")
	public User findUserByName(@WebParam(name = "username")String username) {
		return userMapper.findUserByName(username);
	}
	
	 //将查询到的数据缓存到myCache中,并使用方法名称加上参数中的userNo作为缓存的key  
    //通常更新操作只需刷新缓存中的某个值,所以为了准确的清除特定的缓存,故定义了这个唯一的key,从而不会影响其它缓存值  
    @Cacheable(value="myCache", key="#id")  
    public String getUsernameById(int id){  
        System.out.println("数据库中查到此用户号[" + id + "]对应的用户名为[" + userMapper.getUsernameById(id) + "]");  
        return userMapper.getUsernameById(id);  
    }

	/**
	 * 获取该用户权限的菜单
	 * @param userId
	 * @return
	 */
	public List<SysMenu> getMenu(int userId) {
		return userMapper.getMenuByUserId(userId);  
	}

	/**
	 * 获取用户总数
	 * @param user
	 * @return
	 */
	public Long getDatagridTotal(User user) {
		return userMapper.getDatagridTotal(user);  
	}

	/**
	 * 获取用户列表
	 * @param page
	 * @return
	 */
	public List<User> datagridUser(PageHelper page) {
		page.setStart((page.getPage()-1)*page.getRows());
		page.setEnd(page.getPage()*page.getRows());
		return userMapper.datagridUser(page);  
	}

	/**
	 * 新增用户
	 * @param user
	 */
	public void add(User user) {
		userMapper.addUser(user);  
	}

	/**
	 * 编辑用户
	 * @param user
	 */
	public void edit(User user) {
		userMapper.editUser(user);  
	}  
    
    
    
    
}
