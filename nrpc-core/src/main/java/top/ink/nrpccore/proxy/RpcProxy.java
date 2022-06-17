package top.ink.nrpccore.proxy;
/**
 *@author 林北
 *@description
 *@date 2022-06-16 16:41
 */
public interface RpcProxy {

    /**
     * 获取代理对象
     * @author longxun.wang
     * @date 2022/6/16 16:42
     * @param clazz
     * @return T
     */
    <T> T getProxy(Class<T> clazz);


    /**
     * 创建代理对象
     * @author longxun.wang
     * @date 2022/6/16 17:05
     * @param clazz
     * @return java.lang.Object
     */
    Object createProxy(Class<?> clazz);
}
