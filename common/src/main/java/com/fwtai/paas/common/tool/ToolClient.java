package com.fwtai.paas.common.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fwtai.paas.common.config.ConfigFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端请求|服务器端响应工具类
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2017年1月11日 19:20:50
 * @QQ号码 444141300
 * @主页 http://www.fwtai.com
*/
public final class ToolClient implements Serializable{

	private final static long serialVersionUID = 1L;
	/**
	 * 生成简单类型json字符串,仅用于查询返回,客户端只需判断code是否为200才操作,仅用于查询操作,除了list集合之外都可以用data.map获取数据,list的是data.listData,字符串或数字对应是obj
	 * @作者 田应平
	 * @注意 如果传递的是List则在客户端解析listData的key值,即data.listData;是map或HashMap或PageFormData解析map的key值,即data.map;否则解析obj的key值即data.obj或data.map
	 * @用法 解析后判断data.code == AppKey.code.code200 即可
	 * @返回值类型 返回的是json字符串,内部采用JSONObject封装,不可用于redis缓存value
	 * @创建时间 2017年1月11日 上午10:27:53
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	public static String queryJson(final Object object){
        if(object == null || object.toString().trim().length() <= 0){
            return queryEmpty();
        }
        final JSONObject json = new JSONObject(3);
        if (object instanceof Exception) {
            json.put(ConfigFile.code,ConfigFile.code204);
            json.put(ConfigFile.msg,ConfigFile.msg204);
            json.put(ConfigFile.data,object);
            return json.toJSONString();
        }
        if(object instanceof Map<?,?>){
            final Map<?,?> map = (Map<?,?>) object;
            if(map == null || map.size() <= 0){
                queryEmpty();
            }else {
                json.put(ConfigFile.code,ConfigFile.code200);
                json.put(ConfigFile.msg,ConfigFile.msg200);
                json.put(ConfigFile.data,object);
                return json.toJSONString();
            }
        }
        if(object instanceof List<?>){
            final List<?> list = (List<?>) object;
            if(list == null || list.size() <= 0){
                return queryEmpty();
            }else {
                if (list.get(0) == null){
                    return queryEmpty();
                }else {
                    json.put(ConfigFile.code,ConfigFile.code200);
                    json.put(ConfigFile.msg,ConfigFile.msg200);
                    json.put(ConfigFile.data,object);
                    json.put(ConfigFile.record,((List<?>) object).size());
                    final String jsonObj = json.toJSONString();
                    final JSONObject j = JSONObject.parseObject(jsonObj);
                    final String listData = j.getString(ConfigFile.data);
                    if (listData.equals("[{}]")){
                        return queryEmpty();
                    }
                    return jsonObj;
                }
            }
        }
        if(String.valueOf(object).toLowerCase().equals("null") || String.valueOf(object).replaceAll("\\s*", "").length() == 0){
            return queryEmpty();
        }else {
            json.put(ConfigFile.code,ConfigFile.code200);
            json.put(ConfigFile.msg,ConfigFile.msg200);
            json.put(ConfigFile.data,object);
            final String jsonObj = json.toJSONString();
            final JSONObject j = JSONObject.parseObject(jsonObj);
            final String obj = j.getString(ConfigFile.data);
            if (obj.equals("{}")){
                return queryEmpty();
            }
            return jsonObj;
        }
	}

	/**
	 * 查询时得到的数据为空返回的json字符串
	 * @作者 田应平
	 * @返回值类型 String
	 * @创建时间 2017年1月11日 下午9:40:21
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	private static String queryEmpty(){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,ConfigFile.code201);
		json.put(ConfigFile.msg,ConfigFile.msg201);
		return json.toJSONString();
	}

	/**
	 * 生成json字符串对象,直接采用JSONObject封装,执行效率会更高;适用于为增、删、改操作时,判断当前的rows是否大于0来确定是否操作成功,一般在service调用,偷懒的人可以使用本方法
	 * @param rows 执行后受影响的行数
	 * @用法 解析后判断data.code == AppKey.code.code200即可操作
	 * @作者 田应平
	 * @创建时间 2016年12月25日 下午5:44:23
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String executeRows(final int rows){
		final JSONObject json = new JSONObject(3);
		if(rows > 0){
			json.put(ConfigFile.code,ConfigFile.code200);
			json.put(ConfigFile.msg,ConfigFile.msg200);
			json.put(ConfigFile.data,rows);
			return json.toJSONString();
		}else{
			json.put(ConfigFile.code,ConfigFile.code199);
			json.put(ConfigFile.msg,ConfigFile.msg199);
			return json.toJSONString();
		}
	}

    /**
     * 操作成功生成json字符串对象,失败信息是ConfigFile.msg199,直接采用JSONObject封装,执行效率会更高;适用于为增、删、改操作,一般在service调用
     * @param
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2020/1/19 11:31
    */
    public static String executeRows(final int rows,final String success){
        final JSONObject json = new JSONObject(3);
        if(rows > 0){
            json.put(ConfigFile.code,ConfigFile.code200);
            json.put(ConfigFile.msg,success);
            json.put(ConfigFile.data,rows);
            return json.toJSONString();
        }else{
            json.put(ConfigFile.code,ConfigFile.code199);
            json.put(ConfigFile.msg,ConfigFile.msg199);
            return json.toJSONString();
        }
    }

	/**
	 * 生成自定义的json对象,直接采用JSONObject封装,执行效率会更高;适用于为增、删、改操作,一般在service调用
	 * @param rows 执行后受影响的行数
	 * @param success 执行成功的提示消息
	 * @param failure 执行失败的提示消息
	 * @作者 田应平
	 * @创建时间 2016年12月25日 下午5:50:22
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String executeRows(final int rows,final String success,final String failure){
		final JSONObject json = new JSONObject(3);
		if(rows > 0){
			json.put(ConfigFile.code,ConfigFile.code200);
			json.put(ConfigFile.msg,success);
			json.put(ConfigFile.data,rows);
			return json.toJSONString();
		}else{
			json.put(ConfigFile.code,ConfigFile.code199);
			json.put(ConfigFile.msg,failure);
			return json.toJSONString();
		}
	}

	/**
	 * 生成json格式字符串,code和msg的key是固定的,直接采用JSONObject封装,执行效率会更高,用于增、删、改、查操作,一般在service层调用
	 * @作者 田应平
	 * @返回值类型 返回的是json字符串,内部采用JSONObject封装
	 * @用法 解析后判断data.code == AppKey.code.code200即可处理操作
	 * @创建时间 2016年12月25日 18:11:16
	 * @QQ号码 444141300
	 * @param code 相关参数协议
	 * @主页 http://www.fwtai.com
	*/
	public static String createJson(final int code,final String msg){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,code);
		json.put(ConfigFile.msg,msg);
		return json.toJSONString();
	}

    /**
     * 生成json格式字符串,直接采用JSONObject封装,执行效率会更高,用于增、删、改、查操作,一般在service层调用
     * @作者 田应平
     * @返回值类型 返回的是json字符串,内部采用JSONObject封装
     * @用法 解析后判断data.code == AppKey.code.code200即可处理操作
     * @创建时间 2018年7月3日 09:20:05
     * @QQ号码 444141300
     * @param code 相关参数协议
     * @主页 http://www.fwtai.com
    */
    public static String createJson(final String code,final String msg){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,code);
        json.put(ConfigFile.msg,msg);
        return json.toJSONString();
    }

    /**
     * 生成json格式字符串,直接采用JSONObject封装,执行效率会更高,用于增、删、改、查操作,一般在service层调用
     * @作者 田应平
     * @返回值类型 返回的是json字符串,内部采用JSONObject封装
     * @用法 解析后判断data.code == AppKey.code.code200即可处理操作
     * @创建时间 2018年7月3日 09:20:17
     * @QQ号码 444141300
     * @param hashMap 相关参数协议
     * @主页 http://www.fwtai.com
    */
    public static String createJson(final HashMap<String,Object> hashMap){
        final JSONObject json = new JSONObject();
        for(final String key : hashMap.keySet()){
            json.put(key,hashMap.get(key));
        }
        return json.toJSONString();
    }

    /**
     * 生成json格式字符串,直接采用JSONObject封装,执行效率会更高,用于增、删、改、查操作,一般在service层调用
     * @作者 田应平
     * @返回值类型 返回的是json字符串,内部采用JSONObject封装
     * @用法 解析后判断data.code == AppKey.code.code200即可处理操作
     * @创建时间 2018年7月3日 09:20:31
     * @QQ号码 444141300
     * @param map 相关参数协议
     * @主页 http://www.fwtai.com
    */
    public static String createJson(final Map<String,Object> map){
        final JSONObject json = new JSONObject();
        for(final String key : map.keySet()){
            json.put(key,map.get(key));
        }
        return json.toJSONString();
    }

    /**
     * 生成code为199的json格式数据且msg是提示信息
     * @param 
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019/7/29 15:00
    */
    public static String createJsonFail(final String msg){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code199);
        json.put(ConfigFile.msg,msg);
        return json.toJSONString();
    }

    /**
     * 生成code为200的json格式数据且msg是提示信息
     * @param 
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019/7/29 15:00
    */
    public static String createJsonSuccess(final String msg){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code200);
        json.put(ConfigFile.msg,msg);
        return json.toJSONString();
    }

	/**
	 * 验证密钥key的返回json格式专用,先调用方法validateKey(pageFormData)返回值false后再直接调用本方法返回json字符串
	 * @作者 田应平
	 * @返回值类型 返回的是json字符串,内部采用JSONObject封装
	 * @创建时间 2017年1月11日 下午7:38:48
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	private static String jsonValidateKey(){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,ConfigFile.code203);
		json.put(ConfigFile.msg,ConfigFile.msg203);
		return json.toJSONString();
	}

	/**
	 * 验证必要的参数字段是否为空的返回json格式专用,先调用方法validateField()返回值false后再直接调用本方法返回json字符串
	 * @作者 田应平
	 * @返回值类型 返回的是json字符串,内部采用JSONObject封装
	 * @创建时间 2017年1月11日 下午7:38:48
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	public static String jsonValidateField(){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,ConfigFile.code202);
		json.put(ConfigFile.msg,ConfigFile.msg202);
		return json.toJSONString();
	}

	/**
	 * 验证传入的值是否有值
	 * @param 
	 * @作者 田应平
	 * @QQ 444141300
	 * @创建时间 2020/5/22 11:30
	*/
    public static String validateField(final String... fields){
        if(fields == null || fields.length <= 0){
            return jsonValidateField();
        }
        boolean flag = false;
        for (final String p : fields){
            if(p == null || p.length() <= 0){
                flag = true;
                break;
            }
        }
        if(flag)return jsonValidateField();
        return null;
    }

    private static String jsonValidateInteger(){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code199);
        json.put(ConfigFile.msg,"参数类型有误");
        return json.toJSONString();
    }

	/**
	 * 生成|计算总页数
	 * @作者 田应平
	 * @返回值类型 Integer
	 * @创建时间 2016年12月2日 下午1:20:53
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	public static Integer totalPage(final Integer total,final Integer pageSize){
		return (total%pageSize) == 0 ? (total/pageSize):(total/pageSize)+1; //总页数
	}

	/**
	 * 生成json对象
	 * @param map
	 * @作者 田应平
	 * @返回值类型 String
	 * @创建时间 2017年7月30日 22:47:24
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String jsonObj(final Map<String, Object> map){
		return JSON.toJSONString(map);
	}

	/**
	 * 生成json数组
	 * @param listData
	 * @return
	 * @作者 田应平
	 * @返回值类型 String
	 * @创建时间 2017年1月12日 下午9:28:55
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String jsonArray(final List<?> listData){
		return JSON.toJSONString(listData);
	}


	/**
	 * 用于生成出现异常信息时的json固定code:204字符串提示,返回给controller层调用,一般在service层调用
	 * @作者 田应平
	 * @返回值类型 String,内部采用JSONObject封装,msg 为系统统一的‘系统出现错误’
	 * @创建时间 2017年1月10日 21:40:23
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	public static String exceptionJson(){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,ConfigFile.code204);
		json.put(ConfigFile.msg,ConfigFile.msg204);
		return json.toJSONString();
	}

	/**
	 * 用于生成出现异常信息时的json固定code:204字符串提示,返回给controller层调用,一般在service层调用
	 * @param msg 自定义提示的异常信息
	 * @作者 田应平
	 * @返回值类型 String,内部采用JSONObject封装
	 * @创建时间 2017年1月10日 21:40:23
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	public static String exceptionJson(final String msg){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,ConfigFile.code204);
		json.put(ConfigFile.msg,msg);
		return json.toJSONString();
	}

	/**
	 * 返回给客户端系统出现错误的提示信息,已返回给客户端,只能在controller层调用,用于增、删、改、查操作的异常返回给客户端
	 * @注意 不能在service层调用
	 * @作者 田应平
	 * @创建时间 2016年12月25日 下午5:07:16
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static void responseException(final HttpServletResponse response){
		responseJson(exceptionJson(),response);
    }

	/**
	 * 返回给客户端系统出现错误的提示信息,已返回给客户端,只能在controller层调用,用于增、删、改、查操作的异常返回给客户端
	 * @param msg 自定义提示的异常信息
	 * @注意 不能在service层调用
	 * @作者 田应平
	 * @创建时间 2016年12月25日 下午5:07:16
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static void responseException(final HttpServletResponse response,final String msg){
		responseJson(exceptionJson(msg),response);
    }

	/**
	 * 未登录提示信息,json格式
	 * @作者 田应平
	 * @创建时间 2017年1月14日 上午12:46:08
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String jsonNotLogin(){
		final JSONObject json = new JSONObject(2);
		json.put(ConfigFile.code,ConfigFile.code205);
		json.put(ConfigFile.msg,ConfigFile.msg205);
		return json.toJSONString();
	}

	/**
	 * 通用的响应json返回json对象,只能在是controller层调用
	 * @param json json对象
	 * @param response
	 * @注意 不能在service层调用
	 * @作者 田应平
	 * @创建时间 2016年8月18日 17:53:18
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static void responseJson(final String json,final HttpServletResponse response){
		response.setContentType("text/html;charset=utf-8");
		response.setHeader("Cache-Control","no-cache");
		PrintWriter writer = null;
        try {
			writer = response.getWriter();
            if(json == null){
                writer.write(createJson(ConfigFile.code201,ConfigFile.msg201));
                writer.flush();
            }else{
                writer.write(json);
                writer.flush();
            }
		}catch (final IOException e){}finally{
			if(writer != null){
				writer.close();
                writer = null;
			}
		}
	}

	/**
	 * 响应返回客户端字符串,该obj对象字符串不是标准的json字符串!
	 * @param
	 * @作者 田应平
	 * @QQ 444141300
	 * @创建时间 2018年1月7日 17:31:10
	*/
	public static void responseObj(final Object obj,final HttpServletResponse response){
		response.setContentType("text/html;charset=utf-8");
		response.setHeader("Cache-Control","no-cache");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.write(String.valueOf(obj));
			writer.flush();
			writer.close();
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			if(writer != null){
				writer.close();
			}
		}
	}

	/**
	 * 获取登录人的Session信息,根据key获取相应的值,有账号主键id(ConfigFile.LOGIN_KEY)、登录账号ConfigFile.LOGIN_USER
	 * @param request
	 * @return 返回登录人的Session信息,如userid
	 * @作者 田应平
	 * @创建时间 2015-8-26 下午2:30:01
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String loginKey(final HttpServletRequest request,String key){
		return (String) request.getSession(false).getAttribute(key);
	}

	/**
	 * 获取项目所在的物理路径,推荐使用,但不适合jar运行的项目,仅能通过配置固定目录
	 * @param request
	 * @作者 田应平
	 * @创建时间 2017年9月25日 下午3:47:29
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String getWebRoot(final HttpServletRequest request){
		return request.getSession().getServletContext().getRealPath(File.separator);
	}

	/**
	 * 获取访问项目的网站域名,如http://api.yinlz.com
	 * @param request
	 * @作者 田应平
	 * @返回值类型 String
	 * @创建时间 2016年1月16日 15:18:55
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static String getDomainName(final HttpServletRequest request){
		return request.getScheme()+"://"+request.getServerName();
	}

	/**
	 * 统计处理
	 * @作者 田应平
	 * @参数 List 是数据的数据条数
	 * @参数 keyTotal是count字段或该字段别名
	 * @参数 decimalFormat是统计时的数据格式化,如0、0.0、0.00
	 * @创建时间 2016年9月12日 下午7:34:01
	 * @QQ号码 444141300
	 * @主页 http://www.fwtai.com
	*/
	public static List<Map<String, Object>> statistics(final List<Map<String, Object>> list,final String keyTotal,final String decimalFormat){
		Integer total = 0;
		for(int i = 0; i < list.size(); i++){
			final Map<String, Object> map = list.get(i);
			for(String key : map.keySet()){
				if(key.equals(keyTotal)){
					total += Integer.parseInt(map.get(key).toString());//计算总数
				}
			}
		}
		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for(int i = 0; i < list.size(); i++){
			final Map<String, Object> map = list.get(i);
			final Map<String, Object> m = new HashMap<String, Object>();
			for(String key : map.keySet()){
				if(key.equals(keyTotal)){
					float f = (float)(Integer.parseInt(map.get(key).toString()))/total * 100;//求平均数
					final DecimalFormat df = new DecimalFormat(decimalFormat);//格式化小数,如0.0或0或0.00
					m.put(key,Double.parseDouble(df.format(f)));
				}else {
					m.put(key,map.get(key));
				}
			}
			result.add(m);
		}
		return result;
	}

	/**
	 * 生成带分页的参数查询参数
	 * @param params
	 * @param pageSize 每页大小
	 * @param current 当前页
	 * @作者 田应平
	 * @返回值类型 HashMap<String,Object>
	 * @创建时间 2016年12月29日 下午10:06:03
	 * @QQ号码 444141300
	 * @官网 http://www.fwtai.com
	*/
	public static HashMap<String, Object> pageParams(final HashMap<String, Object> params,final Integer pageSize,final Integer current){
		params.put(ConfigFile.section,(current - 1) * pageSize);//读取区间
		params.put(ConfigFile.pageSize,pageSize);//每页大小
		return params;
	}

    /**
     * 获取表单的请求参数,不含文件域,返回的是HashMap<String,String>
     * @param request
     * @作者:田应平
     * @创建时间 2019年11月13日 19:14:15
     * @主页 www.fwtai.com
    */
    public static HashMap<String,String> getFormParams(final HttpServletRequest request){
        final HashMap<String,String> params = new HashMap<String,String>();
        final Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()){
            final String key = paramNames.nextElement();
            if(key.equals("_"))continue;
            String value = request.getParameter(key);
            if(value != null && value.length() >0){
                value = value.trim();
                if(checkNull(value))
                    continue;
                params.put(key,value);
            }
        }
        return params;
    }

    /**
     * 获取表单的请求参数,不含文件域,返回的是线程安全的ConcurrentHashMapString,String>
     * @param request
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019/11/13 19:29
    */
    public static ConcurrentHashMap<String,String> getFormParam(final HttpServletRequest request){
        final ConcurrentHashMap<String,String> params = new ConcurrentHashMap<String,String>();
        final Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()){
            final String key = paramNames.nextElement();
            if(key.equals("_"))continue;
            String value = request.getParameter(key);
            if(value != null && value.length() > 0){
                value = value.trim();
                if(checkNull(value))
                    continue;
                params.put(key,value);
            }
        }
        return params;
    }

    private static boolean checkNull(final String value){
        if(value.length() <= 0)return true;
        if(value.equals("_"))return true;
        if(value.equalsIgnoreCase("undefined"))return true;
        if(value.equalsIgnoreCase("null"))return true;
        return false;
    }

	/**
	 * 获取表单的所有请求参数
	 * @param request
	 * @作者 田应平
	 * @QQ 444141300
	 * @创建时间 2020/1/8 21:25
	*/
    public static JSONObject getRequestData(final HttpServletRequest request){
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
            final StringBuilder sb = new StringBuilder();
            String s = "";
            while((s = in.readLine()) != null){
                sb.append(s);
            }
            in.close();
            return JSONObject.parseObject(sb.toString().trim());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

	/**
	 * 获取由HttpClient发送的数据的HttpServletRequest请求参数
	 * @作者 田应平
	 * @QQ 444141300
     * @param request 请求参数,默认的字符编码为"UTF-8"
	 * @创建时间 2018年7月3日 09:33:19
	*/
    public static String getHttpClientRequest(final HttpServletRequest request) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final InputStream is = request.getInputStream();
        final InputStreamReader isr = new InputStreamReader(is,"UTF-8");
        final BufferedReader br = new BufferedReader(isr);
        String s = "";
        while ((s = br.readLine()) != null){
            sb.append(s);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 获取由HttpClient发送的数据的HttpServletRequest请求参数
     * @作者 田应平
     * @QQ 444141300
     * @param request 请求参数
     * @param charsetName 字符编码,如 "UTF-8"
     * @创建时间 2018年7月3日 09:39:00
    */
    public static String getHttpClientRequest(final HttpServletRequest request,final String charsetName) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final InputStream is = request.getInputStream();
        final InputStreamReader isr = new InputStreamReader(is,charsetName);
        final BufferedReader br = new BufferedReader(isr);
        String s = "";
        while ((s = br.readLine()) != null){
            sb.append(s);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * 返回没有操作权限的code=401,msg=没有操作权限
     * @param
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2020/3/1 0:13
     */
    public static String notAuthorized(){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code401);
        json.put(ConfigFile.msg,ConfigFile.msg401);
        return json.toJSONString();
    }

    /**
     * token无效或已过期,请重新登录
     * @param 
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2020/4/19 21:00
    */
    public static String tokenInvalid(){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code205);
        json.put(ConfigFile.msg,ConfigFile.TOKEN_INVALID);
        return json.toJSONString();
    }

    public static String tokenInvalid(final String msg){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code205);
        json.put(ConfigFile.msg,msg);
        return json.toJSONString();
    }

    /**
     * 账号或密码错误
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2020/5/1 0:16
     */
    public static String invalidUserInfo(){
        final JSONObject json = new JSONObject(2);
        json.put(ConfigFile.code,ConfigFile.code206);
        json.put(ConfigFile.msg,ConfigFile.msg206);
        return json.toJSONString();
    }

    /**返回分页数据*/
    public static String jsonPage(final List<?> listData,final Integer total){
        final JSONObject json = new JSONObject(4);
        if(listData.isEmpty() || total == 0){
            json.put(ConfigFile.code,ConfigFile.code201);
            json.put(ConfigFile.msg,ConfigFile.msg201);
            return json.toJSONString();
        }else{
            json.put(ConfigFile.code,ConfigFile.code200);
            json.put(ConfigFile.msg,ConfigFile.msg200);
            json.put(ConfigFile.total,total);
            json.put(ConfigFile.data,listData);
            json.put(ConfigFile.record,listData.size());
            return json.toJSONString();
        }
    }

    public static String jsonPage(final Object listData,final Integer total,final List<String> permissions){
        final JSONObject json = new JSONObject(5);
        if(listData == null || total == 0){
            json.put(ConfigFile.code,ConfigFile.code201);
            json.put(ConfigFile.msg,ConfigFile.msg201);
            return json.toJSONString();
        }else{
            if(permissions != null && permissions.size() > 0){
                json.put(ConfigFile.permissions,permissions);
            }
            json.put(ConfigFile.code,ConfigFile.code200);
            json.put(ConfigFile.msg,ConfigFile.msg200);
            json.put(ConfigFile.total,total);
            json.put(ConfigFile.data,listData);
            return json.toJSONString();
        }
    }

    /**
     * 开线程访问服务器删除图片
     * @date 2019年10月31日 16:41:40
     */
    public static void delFileByThread(final String filePath) {
        try {
            new Thread(){
                public void run() {
                    final File file = new File(filePath);
                    if(file.isFile()){
                        file.delete();
                    }
                }
            }.start();
        } catch (Exception e){}
    }

    /**
     * 把URL后跟的查询字符串转成HashMap对象
    */
    public static HashMap<String,String> parseQuery(final String urlQuery) {
        final HashMap<String,String> data = new HashMap<String,String>();
        final String[] params = urlQuery.split("&");
        for (final String param : params) {
            final String[] k = param.split("=");
            final String value = k[1];
            if(value != null && value.trim().length() > 0){
                data.put(k[0],value);
            }
        }
        return data;
    }

    /**获取访问者真实的IP地址*/
    public static String getIp(final HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.length() > 0){
            if(ip.contains(",")){
                String[] ips = ip.split(ip);
                if(ips.length > 0){
                    return ips[0];
                }
            }
        }
        return ip;
    }

    /**获取访问者真实的IP地址*/
    public static String getIpAddr(final HttpServletRequest request)
        throws Exception {
        if (request == null) {
            throw (new Exception("getIpAddr method HttpServletRequest Object is null"));
        }
        String ipString = request.getHeader("x-forwarded-for");
        if (ipString == null || ipString.trim().length() <= 0 || "unknown".equalsIgnoreCase(ipString)) {
            ipString = request.getHeader("Proxy-Client-IP");
        }
        if (ipString == null || ipString.trim().length() <= 0 || "unknown".equalsIgnoreCase(ipString)) {
            ipString = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipString == null || ipString.trim().length() <= 0 || "unknown".equalsIgnoreCase(ipString)) {
            ipString = request.getRemoteAddr();
        }
        // 多个路由时，取第一个非unknown的ip
        final String[] arr = ipString.split(",");
        for (final String str : arr) {
            if (!"unknown".equalsIgnoreCase(str)) {
                ipString = str;
                break;
            }
        }
        return ipString;
    }
}