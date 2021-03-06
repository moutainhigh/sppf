package com.linkmoretech.common.config;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.linkmoretech.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * Service - Redis操作
 * @author jiaohanbin
 * @version 2.0
 *
 */
@Service 
public class RedisService {

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	/**
	 * 随机从set集合中获取一个数
	 * @param key
	 * @return
	 */
	public Object pop(String key) {
		return  redisTemplate.opsForSet().pop(key);
	}
	/**
	 * 获取set集中中长度
	 * @param key
	 * @return
	 */
	public Long size(String key) {
		return (Long) redisTemplate.opsForSet().size(key);
	}
	/**
	 * 获取集合中所有成员元素
	 * @param key
	 * @return
	 */
	public Set<Object> members(String key){
		return redisTemplate.opsForSet().members(key);
	}
	/**
	 * 返回删除数量
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void remove(String key, Object value) {
		SetOperations<String, Object> set = redisTemplate.opsForSet();
		set.remove(key, value);
	}

	/**
	 * 写入缓存
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(final String key, Object value) {
		boolean result = false;
		try {
			@SuppressWarnings("unchecked")
			ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 写入缓存设置时效时间
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean set(final String key, Object value, long expireTime) {
		boolean result = false;
		try {
			ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 批量删除对应的value
	 * 
	 * @param keys
	 */
	public void remove(final String... keys) {
		for (String key : keys) {
			remove(key);
		}
	}

	/**
	 * 批量删除key
	 * 
	 * @param pattern
	 */
	@SuppressWarnings("unchecked")
	public void removePattern(final String pattern) {
		Set<Serializable> keys = redisTemplate.keys(pattern);
		if (keys.size() > 0) {
			redisTemplate.delete(keys);
		}
	}

	/**
	 * 删除对应的value
	 * 
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	public void remove(final String key) {
		if (exists(key)) {
			redisTemplate.delete(key);
		}
	}

	/**
	 * 判断缓存中是否有对应的value
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean exists(final String key) {
		return redisTemplate.hasKey(key);
	}
	
	/**
	 * 读取缓存
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object get(final String key) {
		Object result = null;
		ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
		result = operations.get(key);
		return result;
	}

	/**
	 * 哈希 添加
	 * 
	 * @param key
	 * @param hashKey
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void hmSet(String key, Object hashKey, Object value) {
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		hash.put(key, hashKey, value);
	}

	/**
	 * 哈希获取数据
	 * 
	 * @param key
	 * @param hashKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object hmGet(String key, Object hashKey) {
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		return hash.get(key, hashKey);
	}

	/**
	 * 列表添加
	 * 
	 * @param k
	 * @param v
	 */
	@SuppressWarnings("unchecked")
	public void lPush(String k, Object v) {
		ListOperations<String, Object> list = redisTemplate.opsForList();
		list.rightPush(k, v);
	}

	/**
	 * 列表获取
	 * 
	 * @param k
	 * @param l
	 * @param l1
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object> lRange(String k, long l, long l1) {
		ListOperations<String, Object> list = redisTemplate.opsForList();
		return list.range(k, l, l1);
	}

	/**
	 * 集合添加
	 * 
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void add(String key, Object value) {
		SetOperations<String, Object> set = redisTemplate.opsForSet();
		set.add(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public void addAll(String key, Set<Object> values) {
		SetOperations<String, Object> set = redisTemplate.opsForSet(); 
		Iterator<Object> it = values.iterator();
		while (it.hasNext()) {
			set.add(key, it.next());
		} 
	}

	/**
	 * 集合获取
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Object> setMembers(String key) {
		SetOperations<String, Object> set = redisTemplate.opsForSet();
		return set.members(key);
	}

	/**
	 * 有序集合添加
	 * 
	 * @param key
	 * @param value
	 * @param scoure
	 */
	@SuppressWarnings("unchecked")
	public void zAdd(String key, Object value, double scoure) {
		ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
		zset.add(key, value, scoure);
	}

	/**
	 * 有序集合获取
	 * 
	 * @param key
	 * @param scoure
	 * @param scoure1
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
		ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
		return zset.rangeByScore(key, scoure, scoure1);
	}
	
	/**
	 * 获取自增长数
	 * @param key 键
	 * @param step 计步
	 * @return
	 */
	public Long increment(String key,int step) {
		Long increment = redisTemplate.opsForValue().increment(key, step);
		return increment;
	}
	public void hDel(String key, Object... hashKeys) {
		HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
		hash.delete(key, hashKeys);
	}

	public Boolean lock(String key,String value,Long timeout){
		if(redisTemplate.opsForValue().setIfAbsent(key,value)){
			redisTemplate.expire(key,timeout == null ? Constants.ExpiredTime.STALL_LOCK_BOOKING_EXP_TIME.time :timeout ,TimeUnit.SECONDS);
			return true;
		}
		return false;
	}

	public Boolean upLock(String key,String value){
		Object o = redisTemplate.opsForValue().get(key);
		if(o == null){
			return true;
		}else if(o.toString().equals(value)){
			return true;
		}else{
			return false;
		}
	}
}
